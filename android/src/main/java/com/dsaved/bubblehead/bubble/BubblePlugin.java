package com.dsaved.bubblehead.bubble;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * BubblePlugin
 */
public class BubblePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context activity;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "com.dsaved.bubble.head");
        channel.setMethodCallHandler(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("startBubbleHead")) {
            startBubbleHead(result, call);
        } else if (call.method.equals("stopBubbleHead")) {
            BubbleHeadService.stopService(activity);
        } else {
            result.notImplemented();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startBubbleHead(@NonNull Result result, MethodCall  call) {
        if (Settings.canDrawOverlays(activity)) {
            boolean bounce = call.argument("bounce");
            BubbleHeadService.bounce(bounce);

            boolean showClose = call.argument("showClose");
            BubbleHeadService.shouldShowCloseButton(showClose);

            boolean dragToClose = call.argument("dragToClose");
            BubbleHeadService.dragToClose(dragToClose);

            String imageByte = call.argument("image");
            BubbleHeadService.startService(activity, imageByte);
        } else {
            //Permission is not available
            result.error("EPERMNOTGRANTED", "permission not available", "Please request permission for: android.permission.SYSTEM_ALERT_WINDOW. with out this permission you cannot launch the bubble head.");
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }
}
