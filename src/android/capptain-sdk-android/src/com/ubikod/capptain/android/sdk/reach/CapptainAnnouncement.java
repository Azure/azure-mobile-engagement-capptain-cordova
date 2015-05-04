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

import android.content.Intent;

/**
 * Capptain Announcement abstraction.
 */
public class CapptainAnnouncement extends CapptainAbstractAnnouncement
{
  /** Intent action used by the Reach SDK. */
  public static final String INTENT_ACTION = "com.ubikod.capptain.intent.action.ANNOUNCEMENT";

  /** MIME type */
  private final String mType;

  /**
   * Parse an announcement.
   * @param jid service that sent the announcement.
   * @param xml raw XML of announcement to store in SQLite.
   * @param root parsed XML root DOM element.
   * @param params special parameters to inject in the action URL and body of the announcement.
   * @throws JSONException if a parsing error occurs.
   */
  CapptainAnnouncement(String jid, String xml, Element root, Map<String, String> params)
    throws JSONException
  {
    super(jid, xml, root, params);
    mType = XmlUtil.getAttribute(root, "type");

    for (Map.Entry<String, String> param : params.entrySet())
    {
      if (mBody != null)
        mBody = mBody.replace(param.getKey(), param.getValue());
    }
  }

  @Override
  String getRootTag()
  {
    return "announcement";
  }

  @Override
  Intent buildIntent()
  {
    Intent intent = new Intent(INTENT_ACTION);
    intent.setType(getType());
    String category = getCategory();
    if (category != null)
      intent.addCategory(category);
    return intent;
  }

  /**
   * Get the mime type for this announcement. This is useful to interpret the text returned by
   * {@link #getBody()}. This type will also be set in the intent that launches the viewing
   * activity.
   * @return mime type.
   */
  public String getType()
  {
    return mType;
  }
}
