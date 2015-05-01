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

/**
 * Activity displaying a plain text Capptain announcement. Add this in the AndroidManifest.xml file
 * to use it:
 * 
 * <pre>
 * {@code <activity
 *   android:name="com.ubikod.capptain.android.sdk.reach.activity.CapptainTextAnnouncementActivity"
 *   android:theme="@android:style/Theme.Light">
 *     <intent-filter>
 *       <action android:name="com.ubikod.capptain.intent.action.ANNOUNCEMENT" />
 *       <category android:name="android.intent.category.DEFAULT" />
 *       <data android:mimeType="text/plain" />
 *     </intent-filter>
 * </activity>}
 * </pre>
 */
public class CapptainTextAnnouncementActivity extends CapptainAnnouncementActivity
{
  @Override
  protected String getLayoutName()
  {
    return "capptain_text_announcement";
  }
}
