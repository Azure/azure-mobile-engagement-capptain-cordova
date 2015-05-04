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

/**
 * Custom notifier specification.<br/>
 * You can define how a content notification is done for a set of categories by implementing this
 * class and registering your instances by calling
 * {@link CapptainReachAgent#registerNotifier(CapptainNotifier, String...)}.<br/>
 * It is recommended to extend the default implementation: {@link CapptainDefaultNotifier} which
 * performs most of the work and has convenient callbacks.
 */
public interface CapptainNotifier
{
  /**
   * Handle a notification for a content.
   * @param content content to be notified.
   * @return true to accept the content, false to postpone the content (like overlay disabled in a
   *         specific activity).<br/>
   *         You can also return null to accept the content but not reporting the notification as
   *         displayed yet, this is generally used when a system notification needs some background
   *         task to be completed before it can be submitted (like downloading a big picture). null
   *         can also be returned for in app notifications, in that case, the Reach agent will stop
   *         trying to display that notification on activity changes. When returning null you are
   *         responsible for calling
   *         {@link CapptainReachAgent#notifyPendingContent(CapptainReachInteractiveContent)} once
   *         the notification is ready to be processed again.
   * @throws RuntimeException on any error, the content is dropped.
   */
  public Boolean handleNotification(CapptainReachInteractiveContent content)
    throws RuntimeException;

  /**
   * The Reach SDK needs to control overlays visibility for in-app notifications. When notifiers
   * customize overlays, they must provide a view identifier for each category they manage by
   * implementing this function. The same identifier can be used for several categories but all
   * notifications of a specified category must use the same overlay identifier.
   * @param category category.
   * @return overlay view identifier, can be null if overlays are not used in this notifier for the
   *         specified category (for example they use dialogs, toasts or widgets).
   */
  public Integer getOverlayViewId(String category);

  /**
   * The Reach SDK needs to control notification area visibility for in-app notifications (an
   * overlay may not be used like an embedded notification area in a list activity). When notifiers
   * customize notification areas, they must provide a view identifier for each category they manage
   * by implementing this function. The same identifier can be used for several categories but all
   * notifications of a specified category must use the same notification area view identifier.
   * @param category category.
   * @return area view identifier, can be null if notification areas are not used in this notifier
   *         for the specified category (for example they use dialogs, toasts or widgets).
   */
  public Integer getInAppAreaId(String category);

  /**
   * Called when a notification only announcement is clicked. Implementor is supposed to execute
   * action URL if specified or provide a default action in some scenarii otherwise (like launching
   * application if the notification is a system one and application is in background).
   * @param notifAnnouncement notification only announcement.
   */
  public void executeNotifAnnouncementAction(CapptainNotifAnnouncement notifAnnouncement);
}
