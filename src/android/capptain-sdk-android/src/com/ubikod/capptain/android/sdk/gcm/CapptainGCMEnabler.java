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

package com.ubikod.capptain.android.sdk.gcm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ubikod.capptain.android.sdk.nativepush.CapptainNativePushAgent;
import com.ubikod.capptain.utils.CapptainUtils;

/**
 * This broadcast receiver is required for GCM to work. Add this section in your AndroidManifest.xml
 * file:
 * 
 * <pre>
 * {@code <receiver android:name="com.ubikod.capptain.android.sdk.gcm.CapptainGCMEnabler">
 *   <intent-filter>
 *     <action android:name="com.ubikod.capptain.intent.action.APPID_GOT" />
 *   </intent-filter>
 * </receiver>}
 * </pre>
 * 
 * Additionally and unless you send the <tt>com.google.android.c2dm.intent.REGISTER</tt> yourself,
 * you must configure the GCM sender like this:
 * 
 * <pre>
 * {@code <meta-data android:name="capptain:gcm:sender" android:value="<projectID\n>" />}
 * </pre>
 * 
 * Warning: the <tt>\n</tt> in the sender value is required, otherwise a parsing problem occurs. <br/>
 * <br/>
 * Note that this receiver is mandatory whether you configure it to use the sender for you or not.
 * You must also integrate {@link CapptainGCMReceiver}.
 * @see CapptainGCMReceiver
 */
public class CapptainGCMEnabler extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    /* Once the application identifier is known */
    if ("com.ubikod.capptain.intent.action.APPID_GOT".equals(intent.getAction()))
    {
      /* Init the native push agent */
      String appId = intent.getStringExtra("appId");
      CapptainNativePushAgent.getInstance(context).onAppIdGot(appId);

      /*
       * Request GCM registration identifier, this is asynchronous, the response is made via a
       * broadcast intent with the <tt>com.google.android.c2dm.intent.REGISTRATION</tt> action.
       */
      String sender = CapptainUtils.getMetaData(context).getString("capptain:gcm:sender");
      if (sender != null)
      {
        /* Launch registration process */
        Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        registrationIntent.setPackage("com.google.android.gsf");
        registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
        registrationIntent.putExtra("sender", sender.trim());
        try
        {
          context.startService(registrationIntent);
        }
        catch (RuntimeException e)
        {
          /* Abort if the GCM service can't be accessed. */
        }
      }
    }
  }
}
