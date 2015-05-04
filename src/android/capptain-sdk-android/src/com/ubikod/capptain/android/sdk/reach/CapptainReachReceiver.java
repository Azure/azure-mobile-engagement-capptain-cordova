/*
 * Copyright 2014 Capptain
 * 
 * Licensed under the CAPPTAIN SDK LICENSE (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   https://app.capptain.com/#tos
 *  
 * This file is supplied "as-is." You bear the risk of using it.
 * Capptain gives no express or implied warranties, guarantees or conditions.
 * You may have additional consumer rights under your local laws which this agreement cannot change.
 * To the extent permitted under your local laws, Capptain excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.ubikod.capptain.android.sdk.reach;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.INTENT_EXTRA_COMPONENT;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.INTENT_EXTRA_NOTIFICATION_ID;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.CapptainMessageReceiver;

/**
 * Integrating this class in the AndroidManifest.xml is required for the Reach SDK to be able to
 * process incoming contents and handle system notifications.<br/>
 * Add the following section in the AndroidManifest.xml:
 * 
 * <pre>
 * {@code
 * <receiver android:name="com.ubikod.capptain.android.sdk.reach.CapptainReachReceiver">
 *   <intent-filter>
 *     <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *     <action android:name="com.ubikod.capptain.intent.action.AGENT_CREATED"/>
 *     <action android:name="com.ubikod.capptain.intent.action.MESSAGE"/>
 *     <action android:name="com.ubikod.capptain.reach.intent.action.ACTION_NOTIFICATION"/>
 *     <action android:name="com.ubikod.capptain.reach.intent.action.EXIT_NOTIFICATION"/>
 *     <action android:name="android.intent.action.DOWNLOAD_COMPLETE"/>
 *     <action android:name="com.ubikod.capptain.reach.intent.action.DOWNLOAD_TIMEOUT"/>
 *   </intent-filter>
 * </receiver>
 * }
 * </pre>
 */
public class CapptainReachReceiver extends CapptainMessageReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    /* Boot: restore system notifications */
    String action = intent.getAction();
    if (Intent.ACTION_BOOT_COMPLETED.equals(action))
      CapptainReachAgent.getInstance(context).onDeviceBoot();

    /* Just ensure the reach agent is loaded for checking pending contents in SQLite */
    else if (CapptainAgent.INTENT_ACTION_AGENT_CREATED.equals(action))
      CapptainReachAgent.getInstance(context);

    /* Notification actioned e.g. clicked (from the system notification) */
    else if (CapptainReachAgent.INTENT_ACTION_ACTION_NOTIFICATION.equals(action))
      onNotificationActioned(context, intent);

    /* System notification exited (clear button) */
    else if (CapptainReachAgent.INTENT_ACTION_EXIT_NOTIFICATION.equals(action))
      onNotificationExited(context, intent);

    /* System notification exited (clear button) */
    else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
      onDownloadComplete(context, intent);

    /* Called when download takes too much time to complete */
    else if (CapptainReachAgent.INTENT_ACTION_DOWNLOAD_TIMEOUT.equals(action))
      onDownloadTimeout(context, intent);

    /* Delegate other intents (this handles the push messages) */
    else
      super.onReceive(context, intent);
  }

  @Override
  protected void onPushMessageReceived(Context context, String id, String payload, String replyto)
  {
    /* Make reach agent process message */
    CapptainReachAgent.getInstance(context).onContentReceived(payload, replyto);
  }

  /**
   * Cancel (remove) a system notification from the status bar.
   * @param context context.
   * @param intent notification intent (containing the notification identifier).
   */
  private void cancelNotification(Context context, Intent intent)
  {
    NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    int id = intent.getIntExtra(INTENT_EXTRA_NOTIFICATION_ID, 0);
    notificationManager.cancel(id);
  }

  /**
   * Called when a system notification for a content has been actioned.
   * @param context context.
   * @param intent intent describing the content.
   */
  private void onNotificationActioned(Context context, Intent intent)
  {
    /* Remove notification */
    cancelNotification(context, intent);

    /* Get content */
    CapptainReachAgent reachAgent = CapptainReachAgent.getInstance(context);
    CapptainReachInteractiveContent content = reachAgent.getContent(intent);

    /* If content retrieved successfully */
    if (content != null)
    {
      /* Re-apply component filter (for announcements with content and polls) */
      Intent contentIntent = content.getIntent();
      ComponentName component = intent.getParcelableExtra(INTENT_EXTRA_COMPONENT);
      if (component != null)
        contentIntent.setComponent(component);

      /* Tell reach to start the content activity */
      content.actionNotification(context, true);
    }
  }

  /**
   * Called when a notification has been exited (clear button from notification panel).
   * @param context context.
   * @param intent intent containing the content identifier to exit.
   */
  private void onNotificationExited(Context context, Intent intent)
  {
    /* Remove notification */
    cancelNotification(context, intent);

    /* Get content */
    CapptainReachAgent reachAgent = CapptainReachAgent.getInstance(context);
    CapptainReachInteractiveContent content = reachAgent.getContent(intent);

    /* Exit it if found */
    if (content != null)
      content.exitNotification(context);
  }

  @TargetApi(Build.VERSION_CODES.GINGERBREAD)
  private void onDownloadComplete(Context context, Intent intent)
  {
    /* Find content */
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
      return;
    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
    CapptainReachAgent reachAgent = CapptainReachAgent.getInstance(context);
    CapptainReachInteractiveContent content = reachAgent.getContentByDownloadId(downloadId);

    /* Delegate to agent */
    if (content != null)
      reachAgent.onDownloadComplete(content);
  }

  /**
   * Called when download times out.
   * @param context application context.
   * @param intent timeout intent containing content identifier.
   */
  private void onDownloadTimeout(Context context, Intent intent)
  {
    /* Delegate to agent */
    CapptainReachAgent reachAgent = CapptainReachAgent.getInstance(context);
    CapptainReachInteractiveContent content = reachAgent.getContent(intent);
    if (content != null)
      reachAgent.onDownloadTimeout(content);
  }
}
