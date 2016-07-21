package com.amti.vela.bluetoothlegatt;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;

public class NotificationService extends NotificationListenerService {

    Context context;
    public static boolean notificationsBound;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();

        // check to see if the enabledNotificationListeners String contains our package name
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName))
        {
            notificationsBound = false;
        }
        else
        {
            notificationsBound = true;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Intent msgrcv = new Intent("Msg");

        LocalBroadcastManager.getInstance(context).sendBroadcast(msgrcv);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder mIBinder = super.onBind(intent);
        notificationsBound = true;
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        boolean mOnUnbind = super.onUnbind(intent);
        notificationsBound = true;
        return mOnUnbind;
    }
}