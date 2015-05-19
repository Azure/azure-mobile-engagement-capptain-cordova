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

package com.ubikod.capptain.android.sdk.reach.activity;

import android.content.Intent;

import com.ubikod.capptain.android.sdk.reach.CapptainAnnouncement;

/**
 * Base class for all announcement activities.
 */
public abstract class CapptainAnnouncementActivity extends
  CapptainContentActivity<CapptainAnnouncement>
{
  @Override
  protected void onAction()
  {
    /* Report action */
    mContent.actionContent(getApplicationContext());

    /* Action the URL if specified */
    String url = mContent.getActionURL();
    if (url != null)
      executeActionURL(url);
  }

  /**
   * Execute action URL.
   * @param url action URL (not null).
   */
  protected void executeActionURL(String url)
  {
    try
    {
      startActivity(Intent.parseUri(url, 0));
    }
    catch (Exception e)
    {
      /* Ignore */
    }
  }
}
