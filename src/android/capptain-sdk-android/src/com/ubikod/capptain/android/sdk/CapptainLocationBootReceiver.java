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

package com.ubikod.capptain.android.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ubikod.capptain.utils.CapptainUtils;

/**
 * This receiver is needed to make the real time location reporting starts in background as soon as
 * the device boots. To use it in your application you need to add the following section in your
 * AndroidManifest.xml file:
 * 
 * <pre>
 * {@code <receiver android:name="com.ubikod.capptain.android.sdk.CapptainLocationBootReceiver">
 *   <intent-filter>
 *     <action android:name="android.intent.action.BOOT_COMPLETED" />
 *   </intent-filter>
 * </receiver>}
 * 
 * If missing, add the following permission:<br/>
 * {@code <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />}
 */
public class CapptainLocationBootReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    /* Just ensure the service starts and restore background location listening if enabled */
    Intent serviceIntent = CapptainUtils.resolveCapptainService(context);
    if (serviceIntent != null)
      context.startService(serviceIntent);
  }
}
