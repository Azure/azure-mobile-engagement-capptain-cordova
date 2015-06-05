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

import org.w3c.dom.Element;

import android.content.Intent;

/**
 * Capptain DataPush abstraction.
 */
public class CapptainDataPush extends CapptainReachContent
{
  /** Intent action used by the Reach SDK. */
  public static final String INTENT_ACTION = "com.ubikod.capptain.intent.action.DATA_PUSH";

  /** MIME type */
  private final String mType;

  /**
   * Parse a datapush.
   * @param jid service that sent the datapush
   * @param xml raw XML of datapush to store in SQLite.
   * @param root parsed XML root DOM element.
   * @param params special parameters to inject in the action URL and body of the datapush.
   */
  CapptainDataPush(String jid, String xml, Element root, Map<String, String> params)
  {
    super(jid, xml, root);
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
    return "datapush";
  }

  @Override
  Intent buildIntent()
  {
    /*
     * Unlike interactive contents whose content is either cached in RAM (current content) or
     * retrieved from SQLite from its identifier (to handle application restart for system
     * notifications), we drop the content as soon as the first broadcast receiver that handles
     * datapush acknowledges or cancel the content. We need to put data in the intent to handle
     * several broadcast receivers.
     */
    Intent intent = new Intent(INTENT_ACTION);
    intent.putExtra("category", mCategory);
    intent.putExtra("body", mBody);
    intent.putExtra("type", mType);
    return intent;
  }

  /**
   * Get encoding type for the body.
   * @return "text/plain or "text/base64".
   */
  public String getType()
  {
    return mType;
  }
}
