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

import java.util.Map;

import org.json.JSONException;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;

/**
 * Capptain Notification Announcement abstraction.
 */
public class CapptainNotifAnnouncement extends CapptainAbstractAnnouncement
{
  /**
   * Parse a notification announcement.
   * @param jid service that sent the announcement.
   * @param xml raw XML of announcement to store in SQLite.
   * @param root parsed XML root DOM element.
   * @param params special parameters to inject in the action URL of the announcement.
   * @throws JSONException if a parsing error occurs.
   */
  CapptainNotifAnnouncement(String jid, String xml, Element root, Map<String, String> params)
    throws JSONException
  {
    super(jid, xml, root, params);
  }

  @Override
  String getRootTag()
  {
    return "notifAnnouncement";
  }

  @Override
  Intent buildIntent()
  {
    return null;
  }

  @Override
  public void actionNotification(Context context, boolean launchIntent)
  {
    /* Normal behavior */
    super.actionNotification(context, launchIntent);

    /* This is the final step in this content kind */
    process(context, null, null);
  }

  @Override
  public void actionContent(Context context)
  {
    /* Forbid this action on notification only announcements */
    forbidAction();
  }

  @Override
  public void exitContent(Context context)
  {
    /* Forbid this action on notification only announcements */
    forbidAction();
  }

  /**
   * Throws an exception to indicate the caller that the call is forbidden.
   * @throws UnsupportedOperationException always throws it.
   */
  private void forbidAction() throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("This is a notification only announcement.");
  }
}
