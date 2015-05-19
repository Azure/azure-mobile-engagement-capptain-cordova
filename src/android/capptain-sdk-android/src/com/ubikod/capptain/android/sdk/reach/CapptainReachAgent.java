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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Intent.CATEGORY_DEFAULT;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.CONTENT_DISPLAYED;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.DOWNLOAD_ID;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.ID;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.JID;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.NOTIFICATION_ACTIONED;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.NOTIFICATION_FIRST_DISPLAYED_DATE;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.NOTIFICATION_LAST_DISPLAYED_DATE;
import static com.ubikod.capptain.android.sdk.reach.ContentStorage.XML;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.view.View;

import com.ubikod.capptain.android.sdk.CapptainActivityManager;
import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.CapptainAgent.Callback;
import com.ubikod.capptain.android.sdk.reach.v11.NotificationUtilsV11;
import com.ubikod.capptain.storage.CapptainStorage;
import com.ubikod.capptain.storage.CapptainStorage.Scanner;

/**
 * This is the class that manages the Reach functionalities. It listen messages thanks to
 * {@link CapptainReachReceiver} and notify the user about contents. You usually don't need to
 * access this class directly, you rather integrate the {@link CapptainReachReceiver} broadcast
 * receiver in your AndroidManifest.xml file.<br/>
 * @see CapptainReachReceiver
 */
public class CapptainReachAgent
{
  /** Intent prefix */
  private static final String INTENT_PREFIX = "com.ubikod.capptain.reach.intent.";

  /** Intent action prefix */
  private static final String INTENT_ACTION_PREFIX = INTENT_PREFIX + "action.";

  /** Intent extra prefix */
  private static final String INTENT_EXTRA_PREFIX = INTENT_PREFIX + "extra.";

  /** Intent action used when a reach notification has been actioned e.g. clicked */
  public static final String INTENT_ACTION_ACTION_NOTIFICATION = INTENT_ACTION_PREFIX
    + "ACTION_NOTIFICATION";

  /**
   * Intent action used when a reach notification has been exited (clear button on notification
   * panel).
   */
  public static final String INTENT_ACTION_EXIT_NOTIFICATION = INTENT_ACTION_PREFIX
    + "EXIT_NOTIFICATION";

  /** Intent action used to react to a download timeout */
  public static final String INTENT_ACTION_DOWNLOAD_TIMEOUT = INTENT_ACTION_PREFIX
    + "DOWNLOAD_TIMEOUT";

  /** Used a long extra field in notification and view intents, containing the content identifier */
  public static final String INTENT_EXTRA_CONTENT_ID = INTENT_EXTRA_PREFIX + "CONTENT_ID";

  /**
   * Used an int extra field in notification intents, containing the system notification identifier
   * (to be able to explicitly remove the notification).
   */
  public static final String INTENT_EXTRA_NOTIFICATION_ID = INTENT_EXTRA_PREFIX + "NOTIFICATION_ID";

  /**
   * Used as an extra field in activity launch intent (see
   * {@link #INTENT_ACTION_ACTION_NOTIFICATION} action) to represent the component that will display
   * the content (an activity).
   */
  public static final String INTENT_EXTRA_COMPONENT = INTENT_EXTRA_PREFIX + "COMPONENT";

  /** Undefined intent result (used for datapush) */
  private static final int RESULT_UNDEFINED = -2;

  /** Reach XML namespace */
  static final String REACH_NAMESPACE = "urn:ubikod:ermin:reach:0";

  /** Download meta-data store */
  private static final String DOWNLOAD_SETTINGS = "capptain.reach.downloads";

  /** Unique instance */
  private static CapptainReachAgent sInstance;

  /** Activity manager */
  private static final CapptainActivityManager sActivityManager = CapptainActivityManager.getInstance();

  /** Context used for binding to the Capptain service and other Android API calls */
  private final Context mContext;

  /** Last time application was updated */
  private final long mAppLastUpdateTime;

  /** Notification handlers by category, a default one is set at init time */
  private final Map<String, CapptainNotifier> mNotifiers = new HashMap<String, CapptainNotifier>();

  /** Storage for contents */
  private final CapptainStorage mDB;

  /** List of parameters to inject in announcement's action URL and body */
  private final Map<String, String> mInjectedParams = new HashMap<String, String>();

  /** States */
  private enum State
  {
    /** When we are waiting for new content */
    IDLE,

    /** A content is being notified in-app */
    NOTIFYING_IN_APP,

    /** A content is being shown */
    SHOWING
  }

  /** Current state */
  private State mState = State.IDLE;

  /** True if in the process of scanning */
  private boolean mScanning;

  /**
   * The current content (identifier) being shown (in a viewing activity), set when mState ==
   * State.SHOWING.
   */
  private Long mCurrentShownContentId;

  /**
   * Notifications (content identifiers) that are pending (for example because of a background
   * download). Used to avoid processing them again at each activity change.
   */
  private final Set<Long> mPendingNotifications = new HashSet<Long>();

  /**
   * Content LRU RAM cache, generally contains {@link #mCurrentShownContent} and the ones in
   * {@link #mPendingNotifications}.
   */
  private final LruCache<Long, CapptainReachContent> mContentCache = new LruCache<Long, CapptainReachContent>(
    10);

  /** Last activity weak reference that the agent is aware of */
  private WeakReference<Activity> mLastActivity = new WeakReference<Activity>(null);

  /**
   * Activity listener, when current activity changes we try to show a content notification from
   * local database.
   */
  private final CapptainActivityManager.Listener mActivityListener = new CapptainActivityManager.Listener()
  {
    @Override
    public void onCurrentActivityChanged(WeakReference<Activity> currentActivity,
      String capptainAlias)
    {
      /* Hide notifications when entering new activity (it may contain areas embedded in the layout) */
      Activity activity = currentActivity.get();
      Activity lastActivity = mLastActivity.get();
      if (activity != null && !activity.equals(lastActivity))
        hideInAppNotifications(activity);

      /* If we were notifying in activity and exit that one */
      if (mState == State.NOTIFYING_IN_APP && lastActivity != null
        && !lastActivity.equals(activity))
      {
        /* Hide notifications */
        hideInAppNotifications(lastActivity);

        /* We are now idle */
        setIdle();
      }

      /* Update last activity (if entering a new one) */
      mLastActivity = currentActivity;

      /* If we are idle, pick a content */
      if (mState == State.IDLE)
        scanContent(false);
    }

    /**
     * Hide all possible overlays and notification areas in the specified activity.
     * @param activity activity to operate on.
     */
    private void hideInAppNotifications(Activity activity)
    {
      /* For all categories */
      for (Map.Entry<String, CapptainNotifier> entry : mNotifiers.entrySet())
      {
        /* Hide overlays */
        String category = entry.getKey();
        CapptainNotifier notifier = entry.getValue();
        Integer overlayId = notifier.getOverlayViewId(category);
        if (overlayId != null)
        {
          View overlayView = activity.findViewById(overlayId);
          if (overlayView != null)
            overlayView.setVisibility(View.GONE);
        }

        /* Hide areas */
        Integer areaId = notifier.getInAppAreaId(category);
        if (areaId != null)
        {
          View areaView = activity.findViewById(areaId);
          if (areaView != null)
            areaView.setVisibility(View.GONE);
        }
      }
    }
  };

  /** Datapush campaigns being broadcasted */
  private final Set<Long> mPendingDataPushes = new HashSet<Long>();

  /**
   * Init the reach agent.
   * @param context application context.
   */
  private CapptainReachAgent(Context context)
  {
    /* Keep application context */
    mContext = context;

    /* Get app last update time */
    long appLastUpdateTime;
    try
    {
      appLastUpdateTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
    }
    catch (Exception e)
    {
      /* If package manager crashed, assume no upgrade */
      appLastUpdateTime = 0;
    }
    mAppLastUpdateTime = appLastUpdateTime;

    /* Install default category notifier, can be overridden by user */
    mNotifiers.put(CATEGORY_DEFAULT, new CapptainDefaultNotifier(context));

    /* Open reach database */
    ContentValues schema = new ContentValues();
    schema.put(XML, "");
    schema.put(JID, "");
    schema.put(DOWNLOAD_ID, 1L);
    schema.put(NOTIFICATION_FIRST_DISPLAYED_DATE, 1L);
    schema.put(NOTIFICATION_LAST_DISPLAYED_DATE, 1L);
    schema.put(NOTIFICATION_ACTIONED, 1);
    schema.put(CONTENT_DISPLAYED, 1);
    mDB = new CapptainStorage(context, "capptain.reach.db", 6, "content", schema, null);

    /* Retrieve device id */
    CapptainAgent.getInstance(context).getDeviceId(new Callback<String>()
    {
      @Override
      public void onResult(String deviceId)
      {
        /* Update parameters */
        mInjectedParams.put("{deviceid}", deviceId);

        /*
         * Watch current activity, if we still have not exited the constructor we have to delay the
         * call so that singleton is set. It can happen in the unlikely scenario where getDeviceId
         * returns synchronously the result.
         */
        if (sInstance != null)
          sActivityManager.addCurrentActivityListener(mActivityListener);
        else
          new Handler().post(new Runnable()
          {
            @Override
            public void run()
            {
              sActivityManager.addCurrentActivityListener(mActivityListener);
            }
          });
      }
    });
  }

  /**
   * Get the unique instance.
   * @param context any valid context
   */
  public static CapptainReachAgent getInstance(Context context)
  {
    /* Always check this even if we instantiate once to trigger null pointer in all cases */
    if (sInstance == null)
      sInstance = new CapptainReachAgent(context.getApplicationContext());
    return sInstance;
  }

  /**
   * Register a custom notifier for a set of content categories. You have to call this method in
   * {@link Application#onCreate()} because notifications can happen at any time.
   * @param notifier notifier to register for a set of categories.
   * @param categories one or more category.
   */
  public void registerNotifier(CapptainNotifier notifier, String... categories)
  {
    for (String category : categories)
      mNotifiers.put(category, notifier);
  }

  /**
   * Get content by its local identifier.
   * @param localId the content local identifier.
   * @return the content if found, null otherwise.
   */
  @SuppressWarnings("unchecked")
  public <T extends CapptainReachContent> T getContent(long localId)
  {
    /* Return content from cache if possible */
    CapptainReachContent cachedContent = mContentCache.get(localId);
    if (cachedContent != null)
      try
      {
        return (T) cachedContent;
      }
      catch (ClassCastException cce)
      {
        /* Invalid type */
        return null;
      }

    /*
     * Otherwise fetch in SQLite: required if the application process has been killed while clicking
     * on a system notification or while fetching another content than the current one.
     */
    else
    {
      /* Fetch from storage */
      ContentValues values = mDB.get(localId);
      if (values != null)
        try
        {
          return (T) parseContent(values);
        }
        catch (ClassCastException cce)
        {
          /* Invalid type */
        }
        catch (Exception e)
        {
          /*
           * Delete content that cannot be parsed, may be corrupted data, we cannot send "dropped"
           * feedback as we need the Reach contentId and kind.
           */
          deleteContent(localId, values.getAsLong(DOWNLOAD_ID));
        }

      /* Not found, invalid type or an error occurred */
      return null;
    }
  }

  /**
   * Get content by its intent (containing the content local identifier such as in intents
   * associated with the {@link #INTENT_ACTION_ACTION_NOTIFICATION} action).
   * @param intent intent containing the local identifier under the
   *          {@value #INTENT_EXTRA_CONTENT_ID} extra key (as a long).
   * @return the content if found, null otherwise.
   */
  public <T extends CapptainReachContent> T getContent(Intent intent)
  {
    return getContent(intent.getLongExtra(INTENT_EXTRA_CONTENT_ID, 0));
  }

  /**
   * Get content by a download identifier.
   * @param downloadId intent containing the local identifier under the
   *          {@value #INTENT_EXTRA_CONTENT_ID} extra key (as a long).
   * @return the content if found, null otherwise.
   */
  public <T extends CapptainReachContent> T getContentByDownloadId(long downloadId)
  {
    return getContent(mContext.getSharedPreferences(DOWNLOAD_SETTINGS, 0).getLong(
      String.valueOf(downloadId), 0));
  }

  /**
   * If for some reason you accepted a content in
   * {@link CapptainNotifier#handleNotification(CapptainReachInteractiveContent)} but returned null
   * to tell that the notification was not ready to be displayed, call this function once the
   * notification is ready. For example this is used once the big picture of a system notification
   * has been downloaded (or failed to be downloaded). If the content has not been shown or dropped,
   * this will trigger a new call to
   * {@link CapptainNotifier#handleNotification(CapptainReachInteractiveContent)} if the current U.I
   * context allows so (activity/session/any time filters are evaluated again).
   * @param content content to notify.
   */
  public void notifyPendingContent(CapptainReachInteractiveContent content)
  {
    /* Notification is not managed anymore can be submitted to notifiers again */
    long localId = content.getLocalId();
    mPendingNotifications.remove(localId);

    /* Update notification if not too late e.g. notification not yet dismissed */
    if (mState != State.SHOWING || mCurrentShownContentId != localId)
      try
      {
        notifyContent(content, false);
      }
      catch (RuntimeException e)
      {
        content.dropContent(mContext);
      }
  }

  /**
   * Called when a new content is received.
   * @param xml raw content's XML.
   * @param jid optional reply-to XMPP address.
   */
  void onContentReceived(String xml, String jid)
  {
    /* Check for a packet extension targeting the reach namespace */
    if (xml != null)
    {
      /* Parse content */
      CapptainReachContent content;
      try
      {
        content = parseContent(xml, jid);
      }
      catch (Exception e)
      {
        /*
         * On any parsing error, drop, we cannot send "dropped" feedback: we need contentId and
         * kind.
         */
        return;
      }

      /* Proceed */
      try
      {
        /* Store content in SQLite */
        ContentValues values = new ContentValues();
        values.put(XML, content.getXML());
        values.put(JID, content.getJID());
        long localId = mDB.put(values);
        content.setLocalId(localId);

        /*
         * If we don't know device id yet, keep it for later. If we are idle, check if the content
         * notification can be shown during the current U.I context. Datapush can be "notified" even
         * when not idle.
         */
        if (mInjectedParams.containsKey("{deviceid}"))
          notifyContent(content, false);
      }
      catch (Exception e)
      {
        /* Drop content on error */
        content.dropContent(mContext);
      }
    }
  }

  /**
   * Called when a download for a content has been scheduled.
   * @param content content.
   * @param downloadId download identifier.
   */
  void onDownloadScheduled(CapptainReachContent content, long downloadId)
  {
    /* Save download identifier */
    ContentValues values = new ContentValues();
    values.put(DOWNLOAD_ID, downloadId);
    mDB.update(content.getLocalId(), values);
    mContext.getSharedPreferences(DOWNLOAD_SETTINGS, 0)
      .edit()
      .putLong(String.valueOf(downloadId), content.getLocalId())
      .commit();
  }

  /**
   * Called when download has been completed.
   * @param content content.
   */
  void onDownloadComplete(CapptainReachInteractiveContent content)
  {
    /* Cancel alarm */
    Intent intent = new Intent(INTENT_ACTION_DOWNLOAD_TIMEOUT);
    intent.setPackage(mContext.getPackageName());
    int requestCode = (int) content.getLocalId();
    PendingIntent operation = PendingIntent.getBroadcast(mContext, requestCode, intent, 0);
    AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(operation);

    /* Update notification if not too late e.g. notification not yet dismissed */
    notifyPendingContent(content);
  }

  /**
   * Called when a download takes too much time for a content.
   * @param content content.
   */
  void onDownloadTimeout(CapptainReachInteractiveContent content)
  {
    /* Notify without downloaded data */
    notifyPendingContent(content);
  }

  /**
   * Called when a notification is reported as displayed.
   * @param content displayed content's notification.
   */
  void onNotificationDisplayed(CapptainReachInteractiveContent content)
  {
    ContentValues values = new ContentValues();
    values.put(NOTIFICATION_FIRST_DISPLAYED_DATE, content.getNotificationFirstDisplayedDate());
    values.put(NOTIFICATION_LAST_DISPLAYED_DATE, content.getNotificationLastDisplayedDate());
    mDB.update(content.getLocalId(), values);
  }

  /**
   * Called when a notification is actioned.
   * @param content content associated to the notification.
   * @param launchIntent true to launch intent.
   */
  void onNotificationActioned(CapptainReachContent content, boolean launchIntent)
  {
    /* Persist content state */
    updateContentStatusTrue(content, NOTIFICATION_ACTIONED);

    /* Update state */
    mState = State.SHOWING;
    mCurrentShownContentId = content.getLocalId();

    /* Nothing more to do if intent must not be launched */
    if (!launchIntent)
      return;

    /* Notification announcement */
    if (content instanceof CapptainNotifAnnouncement)
      getNotifier(content).executeNotifAnnouncementAction((CapptainNotifAnnouncement) content);

    /* Start a content activity in its own task */
    else
    {
      Intent intent = content.getIntent();
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
      mContext.startActivity(intent);
    }
  }

  /**
   * Called when a content is reported as displayed.
   * @param content displayed content.
   */
  void onContentDisplayed(CapptainReachContent content)
  {
    updateContentStatusTrue(content, CONTENT_DISPLAYED);
  }

  /**
   * When a content is processed, we can remove it from SQLite. We can also check if a new one can
   * be shown.
   */
  void onContentProcessed(CapptainReachContent content)
  {
    /* Delete content */
    deleteContent(content);

    /* If we were not scanning, set idle and scan for next content */
    if (!mScanning)
    {
      /* We are now idle */
      setIdle();

      /* Look for new in-app content if just exiting an in-app content */
      if (!content.isSystemNotification() && !(content instanceof CapptainDataPush))
        scanContent(false);
    }
  }

  /** Called when the device has rebooted. */
  void onDeviceBoot()
  {
    /* Replay system notifications */
    scanContent(true);
  }

  /**
   * Get notifier for a content depending on its category.
   * @param content content to notify.
   * @return notifier for a content depending on its category.
   */
  private CapptainNotifier getNotifier(CapptainReachContent content)
  {
    /* Delegate to notifiers, select the right one for the current category */
    CapptainNotifier notifier = mNotifiers.get(content.getCategory());

    /* Fail over default category if not found */
    if (notifier == null)
      notifier = mNotifiers.get(CATEGORY_DEFAULT);
    return notifier;
  }

  /**
   * Parse a content.
   * @param xml content's raw XML.
   * @param jid optional reply-to XMPP address.
   * @return content.
   * @throws Exception parsing problem, most likely invalid XML.
   */
  private CapptainReachContent parseContent(String xml, String jid) throws Exception
  {
    /* Check root element, drop it if invalid XML or invalid namespace */
    Element root = XmlUtil.parseContent(xml);
    String rootNS = root.getNamespaceURI();
    if (!REACH_NAMESPACE.equals(rootNS))
      throw new IllegalArgumentException("Unknown namespace: " + rootNS);

    /* If this is an announcement */
    String rootTagName = root.getLocalName();
    if ("announcement".equals(rootTagName))

      /* Parse the announcement */
      return new CapptainAnnouncement(jid, xml, root, mInjectedParams);

    /* If this is a poll */
    else if ("poll".equals(rootTagName))

      /* Parse the poll */
      return new CapptainPoll(jid, xml, root);

    /* If this is a notification announcement */
    else if ("notifAnnouncement".equals(rootTagName))

      /* Parse the notification announcement */
      return new CapptainNotifAnnouncement(jid, xml, root, mInjectedParams);

    /* If this is a data push */
    else if ("datapush".equals(rootTagName))

      /* Parse the datapush */
      return new CapptainDataPush(jid, xml, root, mInjectedParams);

    /* XML/Namespace valid but content is not recognized */
    throw new IllegalArgumentException("Unknown root tag: " + rootTagName);
  }

  /**
   * Parse a content.
   * @param values content as returned by the storage.
   * @return content.
   * @throws Exception parsing problem, most likely invalid XML.
   */
  private CapptainReachContent parseContent(ContentValues values) throws Exception
  {
    /* Parse the first XML tag */
    CapptainReachContent content = parseContent(values.getAsString(XML), values.getAsString(JID));
    content.setState(values);

    /* Set local id */
    content.setLocalId(values.getAsLong(ID));
    return content;
  }

  /**
   * Update a content's status.
   * @param content content to update.
   * @param status status to set to true.
   */
  private void updateContentStatusTrue(CapptainReachContent content, String status)
  {
    ContentValues values = new ContentValues();
    values.put(status, 1);
    mDB.update(content.getLocalId(), values);
  }

  /**
   * Scan reach database and notify the first content that match the current U.I context
   * @param replaySystemNotifications true iff system notifications must be replayed.
   */
  private void scanContent(boolean replaySystemNotifications)
  {
    /* Change state */
    mScanning = true;

    /* For all database rows */
    Scanner scanner = mDB.getScanner();
    for (ContentValues values : scanner)
    {
      /* Parsing may fail */
      CapptainReachContent content = null;
      try
      {
        /* Parse content */
        content = parseContent(values);

        /* Possibly generate a notification */
        notifyContent(content, replaySystemNotifications);
      }
      catch (Exception e)
      {
        /*
         * If the content was parsed but an error occurred while notifying, send "dropped" feedback
         * and delete
         */
        if (content != null)
          content.dropContent(mContext);

        /* Otherwise we just delete */
        else
          deleteContent(values.getAsLong(ID), values.getAsLong(DOWNLOAD_ID));

        /* In any case we continue parsing */
      }
    }

    /* Close scanner */
    scanner.close();

    /* Scan finished */
    mScanning = false;
  }

  /**
   * Fill an intent with a content identifier as extra.
   * @param intent intent.
   * @param content content.
   */
  static void setContentIdExtra(Intent intent, CapptainReachContent content)
  {
    intent.putExtra(INTENT_EXTRA_CONTENT_ID, content.getLocalId());
  }

  /**
   * Try to notify the content to the user.
   * @param content reach content.
   * @param replaySystemNotifications true iff system notifications must be replayed.
   * @throws RuntimeException if an error occurs.
   */
  private void notifyContent(final CapptainReachContent content, boolean replaySystemNotifications)
    throws RuntimeException
  {
    /* Check expiry */
    final long localId = content.getLocalId();
    if (content.hasExpired())
    {
      /* Delete */
      deleteContent(content);
      return;
    }

    /* If datapush, just broadcast, can be done in parallel with another content */
    final Intent intent = content.getIntent();
    if (content instanceof CapptainDataPush)
    {
      /* If it's a datapush it may already be in the process of broadcasting. */
      if (!mPendingDataPushes.add(localId))
        return;

      /* Broadcast intent */
      final CapptainDataPush dataPush = (CapptainDataPush) content;
      intent.setPackage(mContext.getPackageName());
      mContext.sendOrderedBroadcast(intent, null, new BroadcastReceiver()
      {
        @Override
        public void onReceive(Context context, Intent intent)
        {
          /* The last broadcast receiver to set a defined result wins (to determine which result). */
          switch (getResultCode())
          {
            case RESULT_OK:
              dataPush.actionContent(context);
              break;

            case RESULT_CANCELED:
              dataPush.exitContent(context);
              break;

            default:
              dataPush.dropContent(context);
          }

          /* Clean broadcast state */
          mPendingDataPushes.remove(localId);
        }
      }, null, RESULT_UNDEFINED, null, null);

      /* Datapush processed */
      return;
    }

    /* Don't notify in-app if we are already notifying in app or showing a content */
    if (mState != State.IDLE && !content.isSystemNotification())
      return;

    /* Don't process again a pending notification */
    if (mPendingNotifications.contains(localId))
      return;

    /* Not an interactive content, exit (but there is no other type left, this is just a cast guard) */
    if (!(content instanceof CapptainReachInteractiveContent))
      return;
    CapptainReachInteractiveContent iContent = (CapptainReachInteractiveContent) content;

    /* Don't replay system notification unless told otherwise. */
    if (!replaySystemNotifications && iContent.isSystemNotification()
      && iContent.getNotificationLastDisplayedDate() != null
      && iContent.getNotificationLastDisplayedDate() > mAppLastUpdateTime)
      return;

    /* Check if the content can be notified in the current context (behavior) */
    if (!iContent.canNotify(sActivityManager.getCurrentActivityAlias()))
      return;

    /* If there is a show intent */
    if (intent != null)
    {
      /* Filter intent for the target package name */
      filterIntent(intent);

      /* If the intent could not be resolved */
      if (intent.getComponent() == null)
      {
        /* If there was no category */
        if (intent.getCategories() == null)

          /* Notification cannot be done */
          throw new ActivityNotFoundException();

        /* Remove categories */
        Collection<String> categories = new HashSet<String>(intent.getCategories());
        for (String category : categories)
          intent.removeCategory(category);

        /* Try filtering again */
        filterIntent(intent);

        /* Notification cannot be done, skip content */
        if (intent.getComponent() == null)
          throw new ActivityNotFoundException();
      }
    }

    /* Delegate notification */
    Boolean notifierResult = getNotifier(content).handleNotification(iContent);

    /* Check if notifier rejected content notification for now */
    if (Boolean.FALSE.equals(notifierResult))

      /* The notifier rejected the content, nothing more to do */
      return;

    /* Cache content if accepted, it will most likely be used again soon for the next steps. */
    mContentCache.put(localId, content);

    /*
     * If notifier did not return null (e.g. returned true, meaning actually accepted the content),
     * we assume the notification is correctly displayed.
     */
    if (Boolean.TRUE.equals(notifierResult))
    {
      /* Report displayed feedback */
      iContent.displayNotification(mContext);

      /* Track in-app content life cycle: one at a time */
      if (!iContent.isSystemNotification())
        mState = State.NOTIFYING_IN_APP;
    }

    /* Track pending notifications to avoid re-processing them every time we change activity. */
    if (notifierResult == null)
      mPendingNotifications.add(localId);
  }

  /** Set idle, that means we are ready for a next content to be notified in-app. */
  private void setIdle()
  {
    mState = State.IDLE;
    mCurrentShownContentId = null;
  }

  /**
   * Filter the intent to a single activity so a chooser won't pop up.
   * @param intent intent to filter.
   */
  private void filterIntent(Intent intent)
  {
    for (ResolveInfo resolveInfo : mContext.getPackageManager().queryIntentActivities(intent, 0))
    {
      ActivityInfo activityInfo = resolveInfo.activityInfo;
      String packageName = mContext.getPackageName();
      if (activityInfo.packageName.equals(packageName))
      {
        intent.setComponent(new ComponentName(packageName, activityInfo.name));
        break;
      }
    }
  }

  /**
   * Delete content from storage and any associated download.
   * @param content content to delete.
   */
  private void deleteContent(CapptainReachContent content)
  {
    deleteContent(content.getLocalId(), content.getDownloadId());
  }

  /**
   * Delete content from storage and any associated download.
   * @param localId content identifier to delete.
   * @param downloadId download identifier to delete if any.
   */
  private void deleteContent(long localId, Long downloadId)
  {
    /* Delete all references */
    mDB.delete(localId);
    mPendingNotifications.remove(localId);
    mContentCache.remove(localId);

    /* Delete associated download if any */
    if (downloadId != null)
    {
      /* Delete mapping */
      mContext.getSharedPreferences(DOWNLOAD_SETTINGS, 0)
        .edit()
        .remove(String.valueOf(downloadId))
        .commit();

      /* Cancel download and delete file */
      NotificationUtilsV11.deleteDownload(mContext, downloadId);
    }
  }
}
