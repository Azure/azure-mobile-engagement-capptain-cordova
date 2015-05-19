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

import static com.ubikod.capptain.CapptainNativePushToken.Type.GCM;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ubikod.capptain.CapptainNativePushToken;
import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.nativepush.CapptainNativePushAgent;

/**
 * This class is required to communicate the GCM registration id to the Capptain Push service and to
 * receive GCM messages. Add this to your AndroidManifest.xml:
 * 
 * <pre>
 * {@code
 * <receiver android:name="com.ubikod.capptain.android.sdk.gcm.CapptainGCMReceiver" android:permission="com.google.android.c2dm.permission.SEND">
 *   <intent-filter>
 *     <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
 *     <action android:name="com.google.android.c2dm.intent.RECEIVE" />
 *     <category android:name="<your_package_name>" />
 *   </intent-filter>
 * </receiver>
 * }
 * </pre>
 * 
 * Please ensure you have the following permissions:
 * 
 * <pre>
 * {@code
 * <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
 * <uses-permission android:name="<your_package_name>.permission.C2D_MESSAGE" />
 * <permission android:name="<your_package_name>.permission.C2D_MESSAGE" android:protectionLevel="signature" />
 * }
 * </pre>
 * 
 * You also have to integrate {@link CapptainGCMEnabler} if you don't already manage the
 * registration intent yourself.
 * @see CapptainGCMEnabler
 */
public class CapptainGCMReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    /* Registration result action */
    String action = intent.getAction();
    if ("com.google.android.c2dm.intent.REGISTRATION".equals(action))
    {
      /* Handle register if successful (otherwise we'll retry next time process is started) */
      String registrationId = intent.getStringExtra("registration_id");
      if (registrationId != null)
      {
        /* Send registration id to the Capptain Push service */
        CapptainNativePushAgent nativePushAgent = CapptainNativePushAgent.getInstance(context);
        nativePushAgent.registerNativePush(new CapptainNativePushToken(registrationId, GCM));
      }
    }

    /* Received message action */
    else if ("com.google.android.c2dm.intent.RECEIVE".equals(action)
      && "capptain.tickle".equals(intent.getStringExtra("collapse_key")))

      /* Wake up Capptain service if the message is for us */
      CapptainAgent.getInstance(context).checkIncomingMessages();
  }
}
