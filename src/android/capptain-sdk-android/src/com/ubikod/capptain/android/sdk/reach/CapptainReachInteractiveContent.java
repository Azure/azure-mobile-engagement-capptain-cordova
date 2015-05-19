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

import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.REACH_NAMESPACE;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.CONTENT_DISPLAYED;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.NOTIFICATION_ACTIONED;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.NOTIFICATION_FIRST_DISPLAYED_DATE;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.NOTIFICATION_LAST_DISPLAYED_DATE;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ubikod.capptain.utils.Base64;

/** Common class to announcements and polls */
public abstract class CapptainReachInteractiveContent extends CapptainReachContent
{
  /** Title */
  private final String mTitle;

  /** Action's label */
  private final String mActionLabel;

  /** Exit label */
  private final String mExitLabel;

  /** True if this content has a notification that must be set in status bar */
  private boolean mSystemNotification;

  /** Does the notification have a resource icon in notification content? */
  private boolean mNotificationIcon;

  /** Can the notification be closed ? */
  private boolean mNotiticationCloseable;

  /** Make the telephone ring ? */
  private boolean mNotificationSound;

  /** Make the telephone vibrate ? */
  private boolean mNotificationVibrate;

  /** Notification's title */
  private String mNotificationTitle;

  /** Notification's message */
  private String mNotificationMessage;

  /** Notification's big text */
  private String mNotificationBigText;

  /** Notification's big picture */
  private String mNotificationBigPicture;

  /** Notification image base64 string */
  private String mNotificationImageString;

  /** Notification image bitmap null until requested */
  private Bitmap mNotificationImage;

  /** Behavior types */
  private enum Behavior
  {
    ANYTIME,
    SESSION,
    ACTIVITY;
  }

  /** Behavior */
  private Behavior mBehavior;

  /** Activities to restrict this content if behavior is {@link Behavior#ACTIVITY} */
  private Set<String> mAllowedActivities;

  /** True if notification actioned */
  private boolean mNotificationActioned;

  /** Get date at which the notification was displayed the first time */
  private Long mNotificationFirstDisplayedDate;

  /** Get date at which the notification was displayed the last time */
  private Long mNotificationLastDisplayedDate;

  /** True if content displayed */
  private boolean mContentDisplayed;

  CapptainReachInteractiveContent(String jid, String rawXml, Element root) throws JSONException
  {
    super(jid, rawXml, root);
    mTitle = XmlUtil.getTagText(root, "title", root.getLocalName());
    mActionLabel = XmlUtil.getTagText(root, "label", "action");
    mExitLabel = XmlUtil.getTagText(root, "label", "exit");

    /* Behavior */
    mBehavior = Behavior.ANYTIME;
    Element behavior = XmlUtil.getTag(root, "behavior", null);
    if (behavior != null)
      if (XmlUtil.getTag(behavior, "session", null) != null)
        mBehavior = Behavior.SESSION;
      else
      {
        mAllowedActivities = new HashSet<String>();
        NodeList activities = behavior.getElementsByTagNameNS(REACH_NAMESPACE, "activity");
        if (activities.getLength() > 0)
        {
          mBehavior = Behavior.ACTIVITY;
          for (int i = 0; i < activities.getLength(); i++)
            mAllowedActivities.add(XmlUtil.getText(activities.item(i)));
        }
      }

    /* Notification */
    Element notification = XmlUtil.getTag(root, "notification", null);
    if (notification != null)
    {
      /* By default, system, unless "activity" is specified */
      mSystemNotification = !"activity".equals(notification.getAttribute("type"));

      /* The notification can be closed unless closeable is set to a non "true" value */
      mNotiticationCloseable = XmlUtil.getBooleanAttribute(notification, "closeable", true);

      /* The notification has a content icon unless icon attribute set to a non "true" value. */
      mNotificationIcon = XmlUtil.getBooleanAttribute(notification, "icon", true);

      /* Sound and vibration */
      mNotificationSound = XmlUtil.getBooleanAttribute(notification, "sound", false);
      mNotificationVibrate = XmlUtil.getBooleanAttribute(notification, "vibrate", false);

      /* Parse texts */
      mNotificationTitle = XmlUtil.getTagText(notification, "title", null);
      mNotificationMessage = XmlUtil.getTagText(notification, "message", null);

      /* Get image data, we decode the bitmap in a lazy way */
      mNotificationImageString = XmlUtil.getTagText(notification, "image", null);

      /* Options */
      String options = XmlUtil.getTagText(notification, "options", null);
      if (options != null)
      {
        JSONObject jOptions = new JSONObject(options);
        mNotificationBigText = jOptions.optString("bigText", null);
        mNotificationBigPicture = jOptions.optString("bigPicture", null);
      }
    }
  }

  /**
   * Get content's title.
   * @return content's title
   */
  public String getTitle()
  {
    return mTitle;
  }

  /**
   * Get action's label.
   * @return action's label.
   */
  public String getActionLabel()
  {
    return mActionLabel;
  }

  /**
   * Get exit label.
   * @return exit label.
   */
  public String getExitLabel()
  {
    return mExitLabel;
  }

  /**
   * Check whether this content has a system notification.
   * @return true if this content has to be notified in the status bar, false to embed the
   *         notification in activity.
   */
  @Override
  public boolean isSystemNotification()
  {
    return mSystemNotification;
  }

  /**
   * Check whether the notification can be closed without looking at the content.
   * @return true if the notification can be closed without looking at the content, false otherwise.
   */
  public boolean isNotificationCloseable()
  {
    return mNotiticationCloseable;
  }

  /**
   * Check whether the notification has a resource icon in notification content.
   * @return true if the notification has a resource icon in notification content, false otherwise.
   */
  public boolean hasNotificationIcon()
  {
    return mNotificationIcon;
  }

  /**
   * Check whether the notification makes the telephone ring.
   * @return true iff the notification makes the telephone ring.
   */
  public boolean isNotificationSound()
  {
    return mNotificationSound;
  }

  /**
   * Check whether the notification makes the telephone vibrate.
   * @return true iff the notification makes the telephone vibrate.
   */
  public boolean isNotificationVibrate()
  {
    return mNotificationVibrate;
  }

  /**
   * Get notification's title.
   * @return notification's title
   */
  public String getNotificationTitle()
  {
    return mNotificationTitle;
  }

  /**
   * Get notification's message.
   * @return notification's message.
   */
  public String getNotificationMessage()
  {
    return mNotificationMessage;
  }

  /**
   * Get notification big text message (displayed only on Android 4.1+).
   * @return notification's big text message.
   */
  public String getNotificationBigText()
  {
    return mNotificationBigText;
  }

  /**
   * Get notification big picture URL (displayed only on Android 4.1+).
   * @return notification big picture URL.
   */
  public String getNotificationBigPicture()
  {
    return mNotificationBigPicture;
  }

  /**
   * Get notification image for in app notifications. For system notification this field corresponds
   * to the large icon (displayed only on Android 3+).
   * @return notification image.
   */
  public Bitmap getNotificationImage()
  {
    /* Decode as bitmap now if not already done */
    if (mNotificationImageString != null && mNotificationImage == null)
    {
      /* Decode base 64 then decode as a bitmap */
      byte[] data = Base64.decode(mNotificationImageString);
      if (data != null)
        try
        {
          mNotificationImage = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        catch (OutOfMemoryError e)
        {
          /* Abort */
        }

      /* On any error, don't retry next time */
      if (mNotificationImage == null)
        mNotificationImageString = null;
    }
    return mNotificationImage;
  }

  /**
   * Test if this content can be notified in the current UI context.
   * @param activity current activity name, null if no current activity.
   * @return true if this content can be notified in the current UI context.
   */
  boolean canNotify(String activity)
  {
    /*
     * If the system notification has already been displayed, always allows to replay it (for
     * example at boot) disregarding U.I. context. A system notification remains visible even if you
     * leave U.I. context that triggered it so it makes sense to replay it (would be weird to replay
     * it only when U.I context is triggered again which can happen very late or never).
     */
    if (mSystemNotification && mNotificationFirstDisplayedDate != null)
      return true;

    /* Otherwise it depends on current UI context and this campaign settings */
    switch (mBehavior)
    {
      case ANYTIME:
        return mSystemNotification || activity != null;

      case SESSION:
        return activity != null;

      case ACTIVITY:
        return mAllowedActivities.contains(activity);
    }
    return false;
  }

  @Override
  void setState(ContentValues values)
  {
    super.setState(values);
    this.mNotificationActioned = parseBoolean(values, NOTIFICATION_ACTIONED);
    this.mNotificationFirstDisplayedDate = values.getAsLong(NOTIFICATION_FIRST_DISPLAYED_DATE);
    this.mNotificationLastDisplayedDate = values.getAsLong(NOTIFICATION_LAST_DISPLAYED_DATE);
    this.mContentDisplayed = parseBoolean(values, CONTENT_DISPLAYED);
  }

  /**
   * Parse boolean from content values.
   * @param values content values.
   * @param key key.
   * @return boolean value.
   */
  private static boolean parseBoolean(ContentValues values, String key)
  {
    Integer val = values.getAsInteger(key);
    return val != null && val == 1;
  }

  /**
   * Get a status prefix equals to "in-app-notification-" or "system-notification-", depending of
   * this notification type.
   * @return "in-app-notification-" or "system-notification-".
   */
  String getNotificationStatusPrefix()
  {
    String status;
    if (isSystemNotification())
      status = "system";
    else
      status = "in-app";
    status += "-notification-";
    return status;
  }

  /**
   * Report notification has been displayed.
   * @param context any application context.
   */
  public void displayNotification(Context context)
  {
    /* Update last displayed date */
    mNotificationLastDisplayedDate = System.currentTimeMillis();

    /* First date and reach feedback the first time */
    if (mNotificationFirstDisplayedDate == null)
    {
      mNotificationFirstDisplayedDate = mNotificationLastDisplayedDate;
      sendFeedback(context, getNotificationStatusPrefix() + "displayed", null);
    }

    /* Notify reach agent */
    CapptainReachAgent.getInstance(context).onNotificationDisplayed(this);
  }

  /**
   * Get time at which the notification was displayed the first time or null if not yet displayed.
   * @return time in ms since epoch or null.
   */
  public Long getNotificationFirstDisplayedDate()
  {
    return mNotificationFirstDisplayedDate;
  }

  /**
   * Get time at which the notification was displayed the last time or null if not yet displayed.
   * @return time in ms since epoch or null.
   */
  public Long getNotificationLastDisplayedDate()
  {
    return mNotificationLastDisplayedDate;
  }

  /**
   * Action the notification: this will display the announcement or poll, or will launch the action
   * URL associated to the notification, depending of the content kind. This will also report the
   * notification has been actioned.
   * @param context any application context.
   * @param launchIntent true to launch intent, false to just report the notification action and
   *          change internal state. If you call this method passing false, be sure that the content
   *          is either a notification only announcement or that you properly manage the content
   *          display and its life cycle (by calling actionContent or exitContent when the user is
   *          done viewing the content).
   */
  public void actionNotification(Context context, boolean launchIntent)
  {
    /* Notify agent if intent must be launched */
    CapptainReachAgent.getInstance(context).onNotificationActioned(this, launchIntent);

    /* Send feedback */
    if (!mNotificationActioned)
    {
      sendFeedback(context, getNotificationStatusPrefix() + "actioned", null);
      mNotificationActioned = true;
    }
  }

  /**
   * Report notification has been exited.
   * @param context any application context.
   */
  public void exitNotification(Context context)
  {
    process(context, getNotificationStatusPrefix() + "exited", null);
  }

  /**
   * Report content has been displayed.
   * @param context any application context.
   */
  public void displayContent(Context context)
  {
    /* Guard against multiple calls */
    if (!mContentDisplayed)
    {
      sendFeedback(context, "content-displayed", null);
      CapptainReachAgent.getInstance(context).onContentDisplayed(this);
      mContentDisplayed = true;
    }
  }
}
