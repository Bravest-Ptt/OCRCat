package bravest.ptt.ocrcat.utils;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 12/27/16.
 */

public class PermissionUtils {

    private static final String TAG = "PermissionUtils";

    /**
     * 用于在6.0之后的请求危险权限
     * @param activity
     * @param requestCode
     * @param permissions
     * @return
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static boolean requestPermissions(Activity activity, int requestCode, String[] permissions) {
        List<String> deniedPermissions = findDeniedPermissions(activity, permissions);
        if (deniedPermissions.size() > 0) {
            activity.requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            return false;
        }
        return true;
    }

    /**
     * 用于6.0之后的检查哪些权限还没赋予
     * @param activity
     * @param permissions
     * @return
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static List<String> findDeniedPermissions(Activity activity, String... permissions) {
        List<String> denyPermissions = new ArrayList<>();
        for (String value : permissions) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    public static final int REQUIRE_DRAW_OVERLAY = 0x1111;

    /**
     * 用于6.0之后的请求悬浮窗权限
     * @param context
     */
    public static void showOverlayConfirmDialog(final Activity context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Permission Required")
                        .setMessage("Open Overlay Window Permission")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Go settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent.setData(Uri.parse("package:bravest.ptt.ocrcat"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivityForResult(intent, REQUIRE_DRAW_OVERLAY);
                            }
                        });
                builder.setCancelable(false);
                builder.show();
            }
        }
    }

    /**
     * 通过反射检查当前应用的辅助功能是否开启
     * @param context
     * @param accessibilityServiceClass
     * @return
     */
    public static boolean isAccessibilitySettingsOn(Context context, Class<? extends AccessibilityService> accessibilityServiceClass) {
        int accessibilityEnabled = 0;
        // TestService为对应的服务
        final String service = context.getPackageName() + "/" + accessibilityServiceClass.getCanonicalName();
        Log.i(TAG, "service:" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    /**
     * 开启当前应用的辅助功能
     * @param context
     */
    public static void powerAccessibilityPermission(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static final int REQUEST_MEDIA_PROJECTION = 0x1112;
    /**
     * 用于5.0之后请求全范围的截图权限
     * @return false是请求失败， true是成功
     */
    @RequiresApi(value = Build.VERSION_CODES.LOLLIPOP)
    public static boolean requestCapturePermission(Activity activity, MediaProjectionManager mpm) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图
            return false;
        }

        if (activity == null || mpm == null) return false;

        activity.startActivityForResult( mpm.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
        return true;

    }
}
