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

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.INTENT_ACTION_ACTION_NOTIFICATION;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.INTENT_ACTION_EXIT_NOTIFICATION;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.INTENT_EXTRA_COMPONENT;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.INTENT_EXTRA_NOTIFICATION_ID;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_AREA;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_CLOSE;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_CLOSE_AREA;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_ICON;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_IMAGE;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_MESSAGE;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_OVERLAY;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_TEXT;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.LAYOUT_NOTIFICATION_TITLE;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.METADATA_NOTIFICATION_ICON;
import static com.ubikod.capptain.android.sdk.reach.CapptainReachNotifications.METADATA_NOTIFICATION_OVERLAY;
import static com.ubikod.capptain.android.sdk.reach.v11.NotificationUtilsV11.scaleBitmapForLargeIcon;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubikod.capptain.android.sdk.CapptainActivityManager;
import com.ubikod.capptain.android.sdk.reach.v11.NotificationUtilsV11;
import com.ubikod.capptain.utils.CapptainUtils;
import com.ubikod.capptain.utils.ResourcesUtils;

/** Notifier handling the default category. */
public class CapptainDefaultNotifier implements CapptainNotifier
{
  /** Application context */
  protected final Context mContext;

  /** Notification manager */
  private final NotificationManager mNotificationManager;

  /** Icon used in notification content */
  private final int mNotificationIcon;

  /**
   * Init default notifier.
   * @param context any application context.
   */
  public CapptainDefaultNotifier(Context context)
  {
    /* Init */
    mContext = context.getApplicationContext();
    mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

    /* Get icon identifiers from AndroidManifest.xml */
    Bundle appMetaData = CapptainUtils.getMetaData(context);
    mNotificationIcon = getIcon(appMetaData, METADATA_NOTIFICATION_ICON);
  }

  @Override
  public Boolean handleNotification(CapptainReachInteractiveContent content)
    throws RuntimeException
  {
    /* System notification case */
    if (content.isSystemNotification())
    {
      /* Big picture handling */
      Bitmap bigPicture = null;
      String bigPictureURL = content.getNotificationBigPicture();
      if (bigPictureURL != null && Build.VERSION.SDK_INT >= 16)
      {
        /* Schedule picture download if needed, or load picture if download completed. */
        Long downloadId = content.getDownloadId();
        if (downloadId == null)
        {
          NotificationUtilsV11.downloadBigPicture(mContext, content);
          return null;
        }
        else
          bigPicture = NotificationUtilsV11.getBigPicture(mContext, downloadId);
      }

      /* Generate notification identifier */
      int notificationId = getNotificationId(content);

      /* Build notification using support lib to manage compatibility with old Android versions */
      NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

      /* Icon for ticker and content icon */
      builder.setSmallIcon(mNotificationIcon);

      /*
       * Large icon, handled only since API Level 11 (needs down scaling if too large because it's
       * cropped otherwise by the system).
       */
      Bitmap notificationImage = content.getNotificationImage();
      if (notificationImage != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        builder.setLargeIcon(scaleBitmapForLargeIcon(mContext, notificationImage));

      /* Texts */
      String notificationTitle = content.getNotificationTitle();
      String notificationMessage = content.getNotificationMessage();
      String notificationBigText = content.getNotificationBigText();
      builder.setContentTitle(notificationTitle);
      builder.setContentText(notificationMessage);

      /*
       * Replay: display original date and don't replay all the tickers (be as quiet as possible
       * when replaying).
       */
      Long notificationFirstDisplayedDate = content.getNotificationFirstDisplayedDate();
      if (notificationFirstDisplayedDate != null)
        builder.setWhen(notificationFirstDisplayedDate);
      else
        builder.setTicker(notificationTitle);

      /* Big picture */
      if (bigPicture != null)
        builder.setStyle(new BigPictureStyle().bigPicture(bigPicture)
          .setBigContentTitle(notificationTitle)
          .setSummaryText(notificationMessage));

      /* Big text */
      else if (notificationBigText != null)
        builder.setStyle(new BigTextStyle().bigText(notificationBigText));

      /* Vibration/sound if not a replay */
      if (notificationFirstDisplayedDate == null)
      {
        int defaults = 0;
        if (content.isNotificationSound())
          defaults |= Notification.DEFAULT_SOUND;
        if (content.isNotificationVibrate())
          defaults |= Notification.DEFAULT_VIBRATE;
        builder.setDefaults(defaults);
      }

      /* Launch the receiver on action */
      Intent actionIntent = new Intent(INTENT_ACTION_ACTION_NOTIFICATION);
      CapptainReachAgent.setContentIdExtra(actionIntent, content);
      actionIntent.putExtra(INTENT_EXTRA_NOTIFICATION_ID, notificationId);
      Intent intent = content.getIntent();
      if (intent != null)
        actionIntent.putExtra(INTENT_EXTRA_COMPONENT, intent.getComponent());
      actionIntent.setPackage(mContext.getPackageName());
      PendingIntent contentIntent = PendingIntent.getBroadcast(mContext,
        (int) content.getLocalId(), actionIntent, FLAG_CANCEL_CURRENT);
      builder.setContentIntent(contentIntent);

      /* Also launch receiver if the notification is exited (clear button) */
      Intent exitIntent = new Intent(INTENT_ACTION_EXIT_NOTIFICATION);
      exitIntent.putExtra(INTENT_EXTRA_NOTIFICATION_ID, notificationId);
      CapptainReachAgent.setContentIdExtra(exitIntent, content);
      exitIntent.setPackage(mContext.getPackageName());
      PendingIntent deleteIntent = PendingIntent.getBroadcast(mContext, (int) content.getLocalId(),
        exitIntent, FLAG_CANCEL_CURRENT);
      builder.setDeleteIntent(deleteIntent);

      /* Can be dismissed ? */
      Notification notification = builder.build();
      if (!content.isNotificationCloseable())
        notification.flags |= Notification.FLAG_NO_CLEAR;

      /* Allow overriding */
      if (onNotificationPrepared(notification, content))

        /*
         * Submit notification, replacing the previous one if any (this should happen only if the
         * application process is restarted).
         */
        mNotificationManager.notify(notificationId, notification);
    }

    /* Activity embedded notification case */
    else
    {
      /* Get activity */
      Activity activity = CapptainActivityManager.getInstance().getCurrentActivity().get();

      /* Cannot notify in app if no activity provided */
      if (activity == null)
        return false;

      /* Get notification area */
      String category = content.getCategory();
      int areaId = getInAppAreaId(category);
      View notificationAreaView = activity.findViewById(areaId);

      /* No notification area, check if we can install overlay */
      if (notificationAreaView == null)
      {
        /* Check overlay is not disabled in this activity */
        Bundle activityConfig = CapptainUtils.getActivityMetaData(activity);
        if (!activityConfig.getBoolean(METADATA_NOTIFICATION_OVERLAY, true))
          return false;

        /* Inflate overlay layout and get reference to notification area */
        View overlay = LayoutInflater.from(mContext).inflate(getOverlayLayoutId(category), null);
        activity.addContentView(overlay, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        notificationAreaView = activity.findViewById(areaId);
      }

      /* Otherwise check if there is an overlay containing the area to restore visibility */
      else
      {
        View overlay = activity.findViewById(getOverlayViewId(category));
        if (overlay != null)
          overlay.setVisibility(View.VISIBLE);
      }

      /* Make the notification area visible */
      notificationAreaView.setVisibility(View.VISIBLE);

      /* Prepare area */
      prepareInAppArea(content, notificationAreaView);
    }

    /* Success */
    return true;
  }

  @Override
  public Integer getOverlayViewId(String category)
  {
    return getId(LAYOUT_NOTIFICATION_OVERLAY);
  }

  @Override
  public Integer getInAppAreaId(String category)
  {
    return getId(LAYOUT_NOTIFICATION_AREA);
  }

  @Override
  public void executeNotifAnnouncementAction(CapptainNotifAnnouncement notifAnnouncement)
  {
    /* Launch action intent (view activity in its own task) */
    try
    {
      Intent intent = Intent.parseUri(notifAnnouncement.getActionURL(), 0);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      doExecuteNotifAnnouncementAction(notifAnnouncement, intent);
    }
    catch (Exception e)
    {
      /*
       * Invalid/Missing Action URL: launch/resume application instead if system notification and no
       * session.
       */
      if (notifAnnouncement.isSystemNotification()
        && CapptainActivityManager.getInstance().getCurrentActivityAlias() == null)
      {
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(mContext.getPackageName());
        if (intent != null)
        {
          /*
           * Set package null is the magic enabling the same behavior than launching from Home
           * Screen, e.g. perfect resume of the task. No idea why the setPackage messes the intent
           * up...
           */
          if (intent.getComponent() != null)
            intent.setPackage(null);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          doExecuteNotifAnnouncementAction(notifAnnouncement, intent);
        }
      }
    }
  }

  /**
   * This function is called when an overlay is about to be inflated. It returns the overlay layout
   * resource identifier (R.layout... not the view identifier) for the specified category.
   * @param category content category.
   * @return overlay layout resource identifier.
   */
  protected int getOverlayLayoutId(String category)
  {
    return ResourcesUtils.getLayoutId(mContext, LAYOUT_NOTIFICATION_OVERLAY);
  }

  /**
   * This function is called when the notification area view must be prepared, e.g. change texts,
   * icon etc... based on the specified content. This is the responsibility of this method to
   * associate actions to the buttons.
   * @param content content.
   * @param notifAreaView notification area view.
   * @throws RuntimeException on any error the content will be dropped.
   */
  protected void prepareInAppArea(final CapptainReachInteractiveContent content, View notifAreaView)
    throws RuntimeException
  {
    /* Set icon */
    ImageView iconView = ResourcesUtils.getView(notifAreaView, LAYOUT_NOTIFICATION_ICON);
    if (content.hasNotificationIcon())
    {
      iconView.setVisibility(View.VISIBLE);
      iconView.setImageResource(mNotificationIcon);
    }
    else
      iconView.setVisibility(View.GONE);

    /* Set title and message */
    View textArea = ResourcesUtils.getView(notifAreaView, LAYOUT_NOTIFICATION_TEXT);
    if (content.getNotificationTitle() == null && content.getNotificationMessage() == null)
      textArea.setVisibility(View.GONE);
    else
    {
      /* Show text area */
      textArea.setVisibility(View.VISIBLE);

      /* Title */
      TextView titleView = ResourcesUtils.getView(notifAreaView, LAYOUT_NOTIFICATION_TITLE);
      if (content.getNotificationTitle() == null)
        titleView.setVisibility(View.GONE);
      else
      {
        titleView.setVisibility(View.VISIBLE);
        titleView.setText(content.getNotificationTitle());
      }

      /* Message */
      TextView messageView = ResourcesUtils.getView(notifAreaView, LAYOUT_NOTIFICATION_MESSAGE);
      if (content.getNotificationMessage() == null)
        messageView.setVisibility(View.GONE);
      else
      {
        messageView.setVisibility(View.VISIBLE);
        messageView.setText(content.getNotificationMessage());
      }
    }

    /* Set image */
    ImageView imageView = ResourcesUtils.getView(notifAreaView, LAYOUT_NOTIFICATION_IMAGE);
    Bitmap notificationImage = content.getNotificationImage();
    if (notificationImage == null)
      imageView.setVisibility(View.GONE);
    else
    {
      imageView.setVisibility(View.VISIBLE);
      imageView.setImageBitmap(notificationImage);
    }

    /* Set intent action */
    final View notificationAreaViewFinal = notifAreaView;
    notifAreaView.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        notificationAreaViewFinal.setVisibility(View.GONE);
        content.actionNotification(mContext, true);
      }
    });

    /*
     * Configure close button if not removed from layout (it was not mandatory in previous Reach SDK
     * version).
     */
    View closeButton = ResourcesUtils.getView(notifAreaView, LAYOUT_NOTIFICATION_CLOSE);
    if (closeButton != null)
    {
      /* Set close action if closeable */
      if (content.isNotificationCloseable())
      {
        closeButton.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(new OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            notificationAreaViewFinal.setVisibility(View.GONE);
            content.exitNotification(mContext);
          }
        });
      }

      /* Otherwise hide the close button */
      else
        closeButton.setVisibility(View.GONE);
    }

    /*
     * This optional view is used to ensure that the close button does not overlap the image or
     * text, this is an invisible area. If we hide the text area in the provided layout, the close
     * button area won't be right aligned, that's why we have both an invisible button and an actual
     * button that is always right aligned. If you know a way to avoid that without breaking other
     * possible layouts (every text or icon is optional), please contact us.
     */
    View closeButtonArea = ResourcesUtils.getView(notifAreaView, LAYOUT_NOTIFICATION_CLOSE_AREA);
    if (closeButtonArea != null)
      if (content.isNotificationCloseable())
        closeButtonArea.setVisibility(View.INVISIBLE);
      else
        closeButtonArea.setVisibility(View.GONE);
  }

  /**
   * This method is called while a system notification is being built. You can override this method
   * to specify the identifier that will be used when calling
   * {@link NotificationManager#notify(int, Notification)}.
   * @param content content to be notified.
   * @return system notification identifier.
   */
  protected int getNotificationId(CapptainReachInteractiveContent content)
  {
    return ("capptain:reach:" + content.getLocalId()).hashCode();
  }

  /**
   * This method is called just before the notification is submitted. You can override this method
   * to apply more customization like using the LED or a specific sound.
   * @param notification prepared notification that can be modified.
   * @param content content to be notified.
   * @throws RuntimeException on any error, content will be silently dropped.
   * @return true to let the system notification being notified right after this call, false to
   *         manage it yourself.
   */
  protected boolean onNotificationPrepared(Notification notification,
    CapptainReachInteractiveContent content) throws RuntimeException
  {
    return true;
  }

  /**
   * This method is called just before starting the intent associated to the click on a notification
   * only announcement: execute the action URL or launch application if no URL and clicked from a
   * system notification while application is in background. You can change flags, return a brand
   * new intent or return null to cancel the intent and manage it yourself.
   * @param notifAnnouncement notification only announcement.
   * @param intent prepared intent.
   * @return intent to launch, pass null to handle launching yourself.
   */
  protected Intent onNotifAnnouncementIntentPrepared(CapptainNotifAnnouncement notifAnnouncement,
    Intent intent)
  {
    return intent;
  }

  /**
   * Get resource identifier from its name.
   * @param name resource name.
   * @return resource identifier.
   */
  private int getId(String name)
  {
    return ResourcesUtils.getId(mContext, name);
  }

  /**
   * Get drawable resource identifier from application meta-data.
   * @param appMetaData application meta-data.
   * @param metaName meta-data key corresponding to the drawable resource name.
   * @return drawable resource identifier or 0 if not found.
   */
  private int getIcon(Bundle appMetaData, String metaName)
  {
    /* Get drawable resource identifier from its name */
    String iconName = appMetaData.getString(metaName);
    if (iconName != null)
      return ResourcesUtils.getDrawableId(mContext, iconName);
    return 0;
  }

  /** Just common code used in {@link #executeNotifAnnouncementAction(CapptainNotifAnnouncement)} */
  private void doExecuteNotifAnnouncementAction(CapptainNotifAnnouncement notifAnnouncement,
    Intent intent)
  {
    intent = onNotifAnnouncementIntentPrepared(notifAnnouncement, intent);
    if (intent != null)
      mContext.startActivity(intent);
  }
}
