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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.ubikod.capptain.utils.CapptainUtils;

/**
 * This is an helper broadcast receiver used to process connection and disconnection events for the
 * Capptain connection. Since several Capptain services can run on the same device, this helper
 * class take cares of checking the origin of the intent.<br/>
 * To use it in your application you need to add the following section in your AndroidManifest.xml
 * file: {@code
 * <receiver android:name="<your_sub_class_name>">
 * <intent-filter>
 * <action android:name="com.ubikod.capptain.intent.action.CONNECTED"/>
 * <action android:name="com.ubikod.capptain.intent.action.DISCONNECTED"/>
 * </intent-filter>
 * </receiver>}<br/>
 * Note that the Capptain service may send the CONNECTED intent several times during the application
 * lifetime, even if no disconnection occurred.
 */
public abstract class CapptainConnectionReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    /* Check if this is the service we are bound or supposed to bind to */
    String intentServicePackageName = intent.getStringExtra("com.ubikod.capptain.intent.extra.SERVICE_PACKAGE");

    /* If we are bound or binding to the service, check what's the component name */
    ComponentName expectedService = CapptainAgent.getInstance(context).getBindingService();
    if (expectedService == null)
    {
      /* Not yet bound or binding, get component name by resolving the intent */
      Intent resolvedIntent = CapptainUtils.resolveCapptainService(context);
      if (resolvedIntent != null)
        expectedService = resolvedIntent.getComponent();
    }

    /* If a compatible service is known, compare with the intent sender */
    if (expectedService != null
      && expectedService.getPackageName().equals(intentServicePackageName))
    {
      /* Check action and trigger corresponding callback */
      String action = intent.getAction();
      if ("com.ubikod.capptain.intent.action.CONNECTED".equals(action))
        onCapptainConnected(context);
      else if ("com.ubikod.capptain.intent.action.DISCONNECTED".equals(action))
        onCapptainDisconnected(context);
    }
  }

  /**
   * Called every time the connection with the backend is established.
   * @param context the context in which the receiver is running.
   */
  public abstract void onCapptainConnected(Context context);

  /**
   * Called every time the connection with the backend is closed or lost.
   * @param context the context in which the receiver is running.
   */
  public abstract void onCapptainDisconnected(Context context);
}
