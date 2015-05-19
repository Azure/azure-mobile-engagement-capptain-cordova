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

/** Constants used in SQLite */
final class ContentStorage
{
  /** Content identifier */
  static final String ID = "id";

  /** Raw XML */
  static final String XML = "xml";

  /** Replyto */
  static final String JID = "jid";

  /** Download identifier for attached file */
  static final String DOWNLOAD_ID = "download_id";

  /** Notification first displayed date */
  static final String NOTIFICATION_FIRST_DISPLAYED_DATE = "notification_first_displayed_date";

  /** Notification last displayed date */
  static final String NOTIFICATION_LAST_DISPLAYED_DATE = "notification_last_displayed_date";

  /** Notification actioned flag */
  static final String NOTIFICATION_ACTIONED = "notification_actioned";

  /** Content displayed flag */
  static final String CONTENT_DISPLAYED = "content_displayed";
}
