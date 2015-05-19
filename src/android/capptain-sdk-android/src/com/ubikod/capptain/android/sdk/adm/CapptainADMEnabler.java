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

package com.ubikod.capptain.android.sdk.adm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ubikod.capptain.android.sdk.nativepush.CapptainNativePushAgent;
import com.ubikod.capptain.utils.CapptainUtils;

/**
 * This broadcast receiver is required for ADM to work. Add this section in your AndroidManifest.xml
 * file:
 * 
 * <pre>
 * {@code <receiver android:name="com.ubikod.capptain.android.sdk.gcm.CapptainADMEnabler">
 *   <intent-filter>
 *     <action android:name="com.ubikod.capptain.intent.action.APPID_GOT" />
 *   </intent-filter>
 * </receiver>}
 * </pre>
 * 
 * Additionally and unless you manage ADM initialization yourself, you must configure ADM like this:
 * 
 * <pre>
 * {@code <meta-data android:name="capptain:adm:register" android:value="true" />}
 * </pre>
 * 
 * If not already done, configure ADM as being optional or required in your application (inside
 * application tag).
 * 
 * <pre>
 * {@code <amazon:enable-feature android:name="com.amazon.device.messaging" android:required="false" />}
 * </pre>
 * 
 * For this to work, you need to add the following attribute to the root manifest tag:
 * 
 * <pre>
 * {@code xmlns:amazon="http://schemas.amazon.com/apk/res/android"}
 * </pre>
 * 
 * If not already done, you also need to store your ADM API Key as an Asset: create a file named
 * <tt>api_key.text</tt> in the assets folder of your Android project and put the API Key there,
 * without any whitespace. API Key is not needed in release if you let Amazon sign your application.
 * <p>
 * Note that this receiver is mandatory whether you configure it to call {@code ADM.startRegister}
 * or not.
 * </p>
 * <p>
 * Capptain does not need the ADM lib to be in build path, it uses reflection to gracefully degrade
 * if ADM is unavailable.
 * </p>
 * <p>
 * You must also integrate {@link CapptainADMReceiver}.
 * </p>
 * @see CapptainADMReceiver
 */
public class CapptainADMEnabler extends BroadcastReceiver
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
       * Request ADM registration identifier if enabled, this is asynchronous, the response is made
       * via a broadcast intent with the <tt>com.amazon.device.messaging.intent.REGISTRATION</tt>
       * action.
       */
      if (CapptainUtils.getMetaData(context).getBoolean("capptain:adm:register"))
        try
        {
          Class<?> admClass = Class.forName("com.amazon.device.messaging.ADM");
          Object adm = admClass.getConstructor(Context.class).newInstance(context);
          admClass.getMethod("startRegister").invoke(adm);
        }
        catch (Exception e)
        {
          /* Abort if ADM not available */
        }
    }
  }
}
