package com.vanethos.notification_permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class NotificationPermissionsPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin {
  private static final String PERMISSION_GRANTED = "granted";
  private static final String PERMISSION_DENIED = "denied";

  private final String channelId = "yper_proposal_channel";
  private final String channelName = "Propositions de livraison";

  private Context context = null;

  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
    final MethodChannel channel = new MethodChannel(binding.getBinaryMessenger(), "notification_permissions");
    context = binding.getApplicationContext();
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {}

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    if ("isChannelImportant".equalsIgnoreCase(call.method)) {
      result.success(hasHighImportance(channelId, channelName));
    } else if ("getNotificationPermissionStatus".equalsIgnoreCase(call.method)) {
      result.success(getNotificationPermissionStatus());
    } else if ("requestNotificationPermissions".equalsIgnoreCase(call.method)) {
      if (PERMISSION_DENIED.equalsIgnoreCase(getNotificationPermissionStatus())) {
        if (context instanceof Activity) {
          // https://stackoverflow.com/a/45192258
          final Intent intent = new Intent();

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          // ACTION_APP_NOTIFICATION_SETTINGS was introduced in API level 26 aka Android O
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
          } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
          } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
          }

          context.startActivity(intent);

          result.success(PERMISSION_DENIED);
        } else {
          result.error(call.method, "context is null", null);
        }
      } else {
        result.success(PERMISSION_GRANTED);
      }
    } else {
      result.notImplemented();
    }
  }

  private NotificationChannel createChannel(String id, String name) {
    final NotificationManager manager = context.getSystemService(NotificationManager.class);
    final NotificationChannel channel = new NotificationChannel(id, name, NotificationManagerCompat.IMPORTANCE_HIGH);
    manager.createNotificationChannel(channel);
    return channel;
  }

  private boolean hasHighImportance(String id, String name) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel;
        final NotificationManager manager = context.getSystemService(NotificationManager.class);
        channel = manager.getNotificationChannel(id);
        if (channel == null) channel = createChannel(id, name);
        return channel.getImportance() >= NotificationManagerCompat.IMPORTANCE_DEFAULT;
      }
      else {
        final NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.getImportance() >= NotificationManagerCompat.IMPORTANCE_DEFAULT ||
                manager.getImportance() == NotificationManagerCompat.IMPORTANCE_UNSPECIFIED; 
      }
  }

  private String getNotificationPermissionStatus() {
    return context != null ? ((NotificationManagerCompat.from(context).areNotificationsEnabled())
        ? PERMISSION_GRANTED
        : PERMISSION_DENIED) : PERMISSION_DENIED;
  }
}