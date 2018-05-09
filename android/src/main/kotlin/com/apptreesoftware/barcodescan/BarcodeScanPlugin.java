package com.apptreesoftware.barcodescan;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.support.v4.app.ActivityCompat;

import com.apptreesoftware.barcodescan.barcode.BarcodeCaptureActivity;
import com.apptreesoftware.barcodescan.ui.CameraSource;
import com.apptreesoftware.barcodescan.util.AbstractCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterMobileVisionPlugin
 */
public class BarcodeScanPlugin implements MethodCallHandler,
        PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener {

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_BARCODE_SCAN = 9010;

    private final Activity activity;
    private Result result;

    private boolean useFlash = false;
    private boolean autoFocus = true;
    private int formats = Barcode.ALL_FORMATS;
    private boolean multiple = false;
    private boolean waitTap = false;
    private boolean showText = false;
    private int camera = CameraSource.CAMERA_FACING_BACK;
    private float fps = 15.0f;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(),
                "com.apptreesoftware.barcode_scan");

        BarcodeScanPlugin plugin = new BarcodeScanPlugin(registrar.activity());

        channel.setMethodCallHandler(plugin);

        registrar.addActivityResultListener(plugin);
    }

    private BarcodeScanPlugin(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {

        final Map<String, Object> arguments = call.arguments();

        this.result = result;

        if (arguments.containsKey("flash")) {
            useFlash = (boolean) arguments.get("flash");
        }

        if (arguments.containsKey("autoFocus")) {
            autoFocus = (boolean) arguments.get("autoFocus");
        }

        if (arguments.containsKey("formats")) {
            formats = (int) arguments.get("formats");
        }

        if (arguments.containsKey("multiple")) {
            multiple = (boolean) arguments.get("multiple");
        }

        if (arguments.containsKey("waitTap")) {
            waitTap = (boolean) arguments.get("waitTap");
        }

        if (multiple) {
            waitTap = true;
        }

        if (arguments.containsKey("showText")) {
            showText = (boolean) arguments.get("showText");
        }

        if (arguments.containsKey("camera")) {
            camera = (int) arguments.get("camera");
        }

        if (arguments.containsKey("fps")) {
            double tfps = (double) arguments.get("fps");
            fps = (float) tfps;
        }

        int rc = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new
                    String[]{Manifest.permission.CAMERA}, RC_HANDLE_CAMERA_PERM);

            rc = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            if (rc != PackageManager.PERMISSION_GRANTED) {
                result.error("No camera permission.", null, null);
            }
        }

        Intent intent;
        int res;

        if ("scan".equals(call.method)) {
            intent = new Intent(activity, BarcodeCaptureActivity.class);
            res = RC_BARCODE_SCAN;
        } else {
            result.notImplemented();
            return;
        }

        intent.putExtra(AbstractCaptureActivity.AUTO_FOCUS, autoFocus);
        intent.putExtra(AbstractCaptureActivity.USE_FLASH, useFlash);
        intent.putExtra(AbstractCaptureActivity.FORMATS, formats);
        intent.putExtra(AbstractCaptureActivity.MULTIPLE, multiple);
        intent.putExtra(AbstractCaptureActivity.WAIT_TAP, waitTap);
        intent.putExtra(AbstractCaptureActivity.SHOW_TEXT, showText);
        intent.putExtra(AbstractCaptureActivity.CAMERA, camera);
        intent.putExtra(AbstractCaptureActivity.FPS, fps);
        activity.startActivityForResult(intent, res);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_BARCODE_SCAN) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (intent != null) {
                    ArrayList<Barcode> barcodes = intent
                            .getParcelableArrayListExtra(BarcodeCaptureActivity.OBJECT);
                    if (!barcodes.isEmpty()) {
                        List<Map<String, Object>> list = new ArrayList<>();
                        for (Barcode barcode : barcodes) {
                            Rect rect = barcode.getBoundingBox();
                            Map<String, Object> ret = new HashMap<>();
                            ret.put("displayValue", barcode.displayValue);
                            ret.put("rawValue", barcode.rawValue);
                            ret.put("valueFormat", barcode.valueFormat);
                            ret.put("format", barcode.format);
                            ret.put("top", rect.top);
                            ret.put("bottom", rect.bottom);
                            ret.put("left", rect.left);
                            ret.put("right", rect.right);
                            list.add(ret);
                        }
                        result.success(list);
                        return true;
                    }
                }
                result.error("No barcode captured, intent data is null", null, null);
            } else if (resultCode == CommonStatusCodes.ERROR) {
                Exception e = intent.getParcelableExtra(BarcodeCaptureActivity.ERROR);
                result.error(e.getMessage(), null, e);
            }
        }
        return false;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions,
                                              int[] grantResults) {

        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            result.error("Got unexpected permission result: " + requestCode, null, null);
            return false;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        result.error("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"),
                null, null);

        return false;
    }
}