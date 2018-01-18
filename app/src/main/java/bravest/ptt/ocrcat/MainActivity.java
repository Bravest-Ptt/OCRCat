package bravest.ptt.ocrcat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import bravest.ptt.ocrcat.service.OcrCatService;
import bravest.ptt.ocrcat.utils.PermissionUtils;
import bravest.ptt.ocrcat.utils.ToastUtils;

public class MainActivity extends PreferenceActivity
        implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "MainActivity";

    public static final int REQUEST_CODE = 100;

    private static final String KEY_OVERLAY_WINDOW = "switch_window_overlay";
    private static final String KEY_SHOW_WINDOWS = "switch_open";
    private static final String KEY_SCREEN_SHOT = "check_screen_shot";
    private static final String KEY_OCR_TESSERACT = "ocr_tesseract";
    private static final String KEY_OCR_TESSERACT_SETTINGS = "ocr_tesseract_settings";
    private static final String KEY_OCR_TS_SERVER_IP = "ocr_tesseract_server_ip";
    private static final String KEY_OCR_TS_SERVER_PORT = "ocr_tesseract_server_port";
    private static final String KEY_OCR_TS_LOCAL_PORT = "ocr_tesseract_local_port";

    private SwitchPreference mOverlayWindowPreference;
    private SwitchPreference mShowWindowsPreference;
    private Preference mScreenShotPreference;
    private CheckBoxPreference mTesseractPreferecnce;
    private Preference mTesseractSettings;
    private EditTextPreference mTsServerIp;
    private EditTextPreference mTsServerPort;
    private EditTextPreference mTsLocalPort;

    private Context mContext;
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

        initScreenShotPermissionDialog();
    }

    private void initScreenShotPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("截屏权限")
                .setMessage("请求截屏权限中，请赋予我力量吧")
                .setNegativeButton("不给", null)
                .setPositiveButton("给", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.requestCapturePermission((Activity) mContext, mMpm);
                    }
                }).show();
    }

    private void bindService() {
        Intent intent = new Intent(this, OcrCatService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        startService(intent);
    }

    private void initVariables() {
        mContext = this;
        mSp = getPreferenceScreen().getSharedPreferences();
        mMpm = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    private void initPreferences() {
        mOverlayWindowPreference = (SwitchPreference) findPreference(KEY_OVERLAY_WINDOW);
        mOverlayWindowPreference.setOnPreferenceClickListener(this);

        mShowWindowsPreference = (SwitchPreference) findPreference(KEY_SHOW_WINDOWS);
        mShowWindowsPreference.setOnPreferenceClickListener(this);

        mScreenShotPreference = findPreference(KEY_SCREEN_SHOT);
        mScreenShotPreference.setOnPreferenceClickListener(this);

        mTesseractPreferecnce = (CheckBoxPreference) findPreference(KEY_OCR_TESSERACT);
        mTesseractPreferecnce.setOnPreferenceClickListener(this);

        mTesseractSettings = findPreference(KEY_OCR_TESSERACT_SETTINGS);
        mTesseractSettings.setEnabled(mSp.getBoolean(KEY_OCR_TESSERACT, false));

        mTsServerIp = (EditTextPreference) findPreference(KEY_OCR_TS_SERVER_IP);
        mTsServerIp.setOnPreferenceChangeListener(this);

        mTsServerPort = (EditTextPreference) findPreference(KEY_OCR_TS_SERVER_PORT);
        mTsServerPort.setOnPreferenceChangeListener(this);

        mTsLocalPort = (EditTextPreference) findPreference(KEY_OCR_TS_LOCAL_PORT);
        mTsLocalPort.setOnPreferenceChangeListener(this);

        if (TextUtils.equals(mSp.getString(KEY_OCR_TS_SERVER_IP, "--"), "--")) {
            mTsServerIp.setText("104.194.94.128");
        }
        if (TextUtils.equals(mSp.getString(KEY_OCR_TS_SERVER_PORT, "--"), "--")) {
            mTsServerPort.setText("50028");
        }
        if (TextUtils.equals(mSp.getString(KEY_OCR_TS_LOCAL_PORT, "--"), "--")) {
            mTsLocalPort.setText("50028");
        }

        //init edit text p
        mTsServerIp.setSummary(mSp.getString(KEY_OCR_TS_SERVER_IP, "--"));
        mTsServerPort.setSummary(mSp.getString(KEY_OCR_TS_SERVER_PORT, "--"));
        mTsLocalPort.setSummary(mSp.getString(KEY_OCR_TS_LOCAL_PORT, "--"));
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        } else if (preference == mTesseractPreferecnce) {
            mTesseractSettings.setEnabled(mSp.getBoolean(KEY_OCR_TESSERACT, false));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTsServerIp) {
            mTsServerIp.setSummary(newValue.toString());
        } else if (preference == mTsServerPort) {
            mTsServerPort.setSummary(newValue.toString());
        } else if (preference == mTsLocalPort) {
            mTsLocalPort.setSummary(newValue.toString());
        }
        // false 不能保存值
        return true;
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
