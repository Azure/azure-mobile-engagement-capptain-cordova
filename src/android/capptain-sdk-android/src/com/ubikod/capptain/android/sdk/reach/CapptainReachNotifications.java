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

/** Constants related to Reach notifications */
public class CapptainReachNotifications
{
  /** Reach notification content icon meta-data */
  public static final String METADATA_NOTIFICATION_ICON = "capptain:reach:notification:icon";

  /** Overlay enabled meta-data */
  public static final String METADATA_NOTIFICATION_OVERLAY = "capptain:notification:overlay";

  /** Prefix for all notification layout constants */
  private static final String LAYOUT_NOTIFICATION_PREFIX = "capptain_notification_";

  /** ID of the icon view */
  public static final String LAYOUT_NOTIFICATION_ICON = LAYOUT_NOTIFICATION_PREFIX + "icon";

  /**
   * Layout name of the in-app notification area, this is also the view identifier of the area
   * itself.
   */
  public static final String LAYOUT_NOTIFICATION_AREA = LAYOUT_NOTIFICATION_PREFIX + "area";

  /** Layout name and root view id for in-app notification overlay. */
  public static final String LAYOUT_NOTIFICATION_OVERLAY = LAYOUT_NOTIFICATION_PREFIX + "overlay";

  /** ID of the close button in in-app notifications */
  public static final String LAYOUT_NOTIFICATION_CLOSE = LAYOUT_NOTIFICATION_PREFIX + "close";

  /**
   * ID of the optional area to fill space for the close button in the linear layout, this is a
   * layout trick.
   */
  public static final String LAYOUT_NOTIFICATION_CLOSE_AREA = LAYOUT_NOTIFICATION_PREFIX
    + "close_area";

  /** ID of the view containing notification title and message */
  public static final String LAYOUT_NOTIFICATION_TEXT = LAYOUT_NOTIFICATION_PREFIX + "text";

  /** ID of the view containing notification message */
  public static final String LAYOUT_NOTIFICATION_MESSAGE = LAYOUT_NOTIFICATION_PREFIX + "message";

  /** ID of the view containing notification title */
  public static final String LAYOUT_NOTIFICATION_TITLE = LAYOUT_NOTIFICATION_PREFIX + "title";

  /** ID of the view containing notification image */
  public static final String LAYOUT_NOTIFICATION_IMAGE = LAYOUT_NOTIFICATION_PREFIX + "image";
}
