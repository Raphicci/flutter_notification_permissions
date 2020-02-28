package com.vanethos.notification_permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class NotificationPermissionsPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin {
  private static final String PERMISSION_GRANTED = "granted";
  private static final String PERMISSION_DENIED = "denied";

  private Context context = null;

  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
    final MethodChannel channel = new MethodChannel(binding.getBinaryMessenger(), "app_settings");
    context = binding.getApplicationContext();
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {}

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    if ("getNotificationPermissionStatus".equalsIgnoreCase(call.method)) {
      result.success(getNotificationPermissionStatus());
    } else if ("requestNotificationPermissions".equalsIgnoreCase(call.method)) {
      if (PERMISSION_DENIED.equalsIgnoreCase(getNotificationPermissionStatus())) {
        if (context != null) {
          final Uri uri = Uri.fromParts("package", context.getPackageName(), null);

          final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
          intent.setData(uri);

          context.startActivity(intent);

          result.success(null);
        } else {
          result.error(call.method, "context is null", null);
        }
      } else {
        result.success(null);
      }
    } else {
      result.notImplemented();
    }
  }

  private String getNotificationPermissionStatus() {
    return context != null ? ((NotificationManagerCompat.from(context).areNotificationsEnabled())
        ? PERMISSION_GRANTED
        : PERMISSION_DENIED) : PERMISSION_DENIED;
  }
}
