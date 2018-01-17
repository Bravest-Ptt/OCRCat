package bravest.ptt.ocrcat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import bravest.ptt.ocrcat.network.UdpInterface;
import bravest.ptt.ocrcat.receiver.ScreenshotObserver;
import bravest.ptt.ocrcat.service.OcrCatService;
import bravest.ptt.ocrcat.utils.PermissionUtils;
import bravest.ptt.ocrcat.utils.ToastUtils;

public class MainActivity extends PreferenceActivity
        implements Preference.OnPreferenceClickListener {

    private static final String TAG = "MainActivity";

    public static final int REQUEST_CODE = 100;

    private static final String KEY_OVERLAY_WINDOW = "switch_window_overlay";
    private static final String KEY_SHOW_WINDOWS = "switch_open";
    private static final String KEY_SCREEN_SHOT = "check_screen_shot";

    private SwitchPreference mOverlayWindowPreference;
    private SwitchPreference mShowWindowsPreference;
    private Preference mScreenShotPreference;

    private ScreenshotObserver mScreenshotObserver;
    private SharedPreferences mSp;
    private MediaProjectionManager mMpm;
    private OcrCatService mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            mService = ((OcrCatService.OcrCatBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.activity_main);
        bindService();
        initVariables();
        initPreferences();
    }

    private void bindService() {
        Intent intent = new Intent(this, OcrCatService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        startService(intent);
    }

    private void initVariables() {
        mScreenshotObserver = new ScreenshotObserver(this, null);
        mMpm = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
        PermissionUtils.requestCapturePermission(this, mMpm);
    }

    private void initPreferences() {
        mOverlayWindowPreference = (SwitchPreference) findPreference(KEY_OVERLAY_WINDOW);
        mOverlayWindowPreference.setOnPreferenceClickListener(this);

        mShowWindowsPreference = (SwitchPreference) findPreference(KEY_SHOW_WINDOWS);
        mShowWindowsPreference.setOnPreferenceClickListener(this);

        mScreenShotPreference = findPreference(KEY_SCREEN_SHOT);
        mScreenShotPreference.setOnPreferenceClickListener(this);

        mSp = getPreferenceScreen().getSharedPreferences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        this.getContentResolver().registerContentObserver(imageUri, false, mScreenshotObserver);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
        boolean enabled = Settings.canDrawOverlays(this);
        mOverlayWindowPreference.setChecked(enabled);
        mSp.edit().putBoolean(KEY_OVERLAY_WINDOW, enabled).apply();

        if (!enabled) {
            mShowWindowsPreference.setEnabled(false);
            mShowWindowsPreference.setChecked(false);
            mSp.edit().putBoolean(KEY_SHOW_WINDOWS, false).apply();
            showWindows(false);
        } else {
            boolean opened = mSp.getBoolean(KEY_SHOW_WINDOWS, false);
            mShowWindowsPreference.setEnabled(true);
            mShowWindowsPreference.setChecked(opened);
        }
    }

    private boolean checkPermissions() {
        return PermissionUtils.requestPermissions(this, REQUEST_CODE, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE) return;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            if (TextUtils.equals(permissions[i], Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Write External Storage permission required",
                        Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.equals(permissions[i], Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Read External Storage permission required",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionUtils.REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                MediaProjection mp = mMpm.getMediaProjection(resultCode, data);
                if (mService != null && mp != null) {
                    mService.setUpMediaProjection(mp);
                    Log.d(TAG, "require screen shot success");
                    ToastUtils.showToast(this, "请求截屏权限成功");
                } else {
                    Log.d(TAG, "require screen shot failed");
                    ToastUtils.showToast(this, "请求截屏权限失败");
                }
            } else {
                Log.d(TAG, "require screen shot failed");
                ToastUtils.showToast(this, "请求截屏权限失败");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.getContentResolver().unregisterContentObserver(mScreenshotObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mOverlayWindowPreference && Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                PermissionUtils.showOverlayConfirmDialog(this);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:bravest.ptt.ocrcat"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            return true;
        } else if (preference == mShowWindowsPreference) {
            if (!mShowWindowsPreference.isChecked()) {
                mSp.edit().putBoolean(KEY_SHOW_WINDOWS, false).apply();
                showWindows(false);
            } else {
                mSp.edit().putBoolean(KEY_SHOW_WINDOWS, true).apply();
                showWindows(true);
            }
            return true;
        } else if (preference == mScreenShotPreference && Build.VERSION.SDK_INT >= 21) {
            PermissionUtils.requestCapturePermission(this, mMpm);
        }
        return false;
    }

    /**
     * Show windows through service
     * @param show
     */
    private void showWindows(boolean show) {
        Log.d(TAG, "showWindows + " + show);
        if (mService == null) {
            ToastUtils.showToast(this, "Service is not ready");
            Log.d(TAG, "showWindows: mService is null");
        } else {
            mService.showWindows(show);
        }
    }

    /**
     * get windows showing state through service
     * @return
     */
    private boolean isWindowsShowing() {
        Log.d(TAG, "isWindowsShowing: ");
        if (mService == null) {
            ToastUtils.showToast(this, "Service is not ready");
            Log.d(TAG, "showWindows: mService is null");
        } else {
            return mService.isWindowsShowing();
        }
        return false;
    }
}
