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

package com.ubikod.capptain.android.sdk.track;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.utils.CapptainUtils;

/**
 * This is a bootstrap for the tracking module. To integrate this receiver in your
 * AndroidManifest.xml:
 * 
 * <pre>
 * {@code
 * <receiver android:name="com.ubikod.capptain.android.sdk.track.CapptainTrackReceiver">
 *   <intent-filter>
 *     <action android:name="com.ubikod.capptain.intent.action.APPID_GOT" />
 *     <action android:name="com.android.vending.INSTALL_REFERRER" />
 *   </intent-filter>
 * </receiver>}
 * </pre>
 * 
 * The <tt>com.android.vending.INSTALL_REFERRER</tt> action is optional and is used to receive the
 * Google Analytics referrer when available, the intent is either triggered at installation time or
 * at the first launch (depending on the Android Market application version).<br/>
 * Only one BroadcastReceiver class can be specified per application for the
 * <tt>com.android.vending.INSTALL_REFERRER</tt> action. If you would like other broadcast receivers
 * to receive this intent as well, please remove the following intent filter:
 * 
 * <pre>
 * {@code <intent-filter>
 *   <action android:name="com.android.vending.INSTALL_REFERRER" />
 * </intent-filter>}
 * </pre>
 * 
 * from all the other receivers if any (you have to keep the receiver tags, even if they become
 * empty).<br/>
 * Then you can specify the list of the broadcast receivers (their fully qualified class names
 * separated by commas) in a meta-data inside the application tag like in the following example:
 * 
 * <pre>
 * {@code <meta-data android:name="capptain:track:installReferrerForwardList"
 * android:value="com.google.android.apps.analytics.AnalyticsReceiver,com.acme.MyInstallReceiver" />}
 * </pre>
 * 
 * Additionally, you can enable Ad Server device identifiers reporting by adding a meta-data in the
 * AndroidManifest.xml. We currently only support SmartAd so the meta-data is the following:
 * 
 * <pre>
 * {@code <meta-data android:name="capptain:track:adservers" android:value="smartad" />}
 * </pre>
 */
public class CapptainTrackReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    /* Once the application identifier is known */
    if ("com.ubikod.capptain.intent.action.APPID_GOT".equals(intent.getAction()))
    {
      /* Init the tracking agent */
      String appId = intent.getStringExtra("appId");
      CapptainTrackAgent.getInstance(context).onAppIdGot(appId);
    }

    /* During installation, an install referrer may be triggered */
    else if ("com.android.vending.INSTALL_REFERRER".equals(intent.getAction()))
    {
      /* Forward this action to configured receivers */
      String forwardList = CapptainUtils.getMetaData(context).getString(
        "capptain:track:installReferrerForwardList");
      if (forwardList != null)
        for (String component : forwardList.split(","))
          if (!component.equals(getClass().getName()))
          {
            Intent clonedIntent = new Intent(intent);
            clonedIntent.setComponent(new ComponentName(context, component));
            context.sendBroadcast(clonedIntent);
          }

      /* Guess store from referrer */
      String referrer = Uri.decode(intent.getStringExtra("referrer"));
      String store;

      /* GetJar uses an opaque string always starting with z= */
      if (referrer.startsWith("z="))
        store = "GetJar";

      /* Assume Android Market otherwise */
      else
        store = "Android Market";

      /* Look for a source parameter */
      Uri uri = Uri.parse("a://a?" + referrer);
      String source = uri.getQueryParameter("utm_source");

      /*
       * Skip "androidmarket" source, this is the default one when not set or installed via web
       * Android Market.
       */
      if ("androidmarket".equals(source))
        source = null;

      /* Send app info to register store/source */
      Bundle appInfo = new Bundle();
      appInfo.putString("store", store);
      if (source != null)
      {
        /* Parse source, if coming from capptain, it may be a JSON complex object */
        try
        {
          /* Convert JSON to bundle (just flat strings) */
          JSONObject jsonInfo = new JSONObject(source);
          Iterator<?> keyIt = jsonInfo.keys();
          while (keyIt.hasNext())
          {
            String key = keyIt.next().toString();
            appInfo.putString(key, jsonInfo.getString(key));
          }
        }
        catch (JSONException jsone)
        {
          /* Not an object, process as a string */
          appInfo.putString("source", source);
        }
      }

      /* Send app info */
      CapptainAgent.getInstance(context).sendAppInfo(appInfo);
    }
  }
}
