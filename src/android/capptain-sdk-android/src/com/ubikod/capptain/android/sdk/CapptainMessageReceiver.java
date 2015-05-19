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

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * This is an helper broadcast receiver used to process incoming messages of the Capptain Push
 * Service. To process messages, you override the
 * {@link #onPushMessageReceived(Context, String, String, String)} , this class takes care of
 * verifying that the message does actually come from the Push Service by verifying the signature.<br/>
 * To use it in your application you need to add the following section in your AndroidManifest.xml
 * file: {@code
 * <receiver android:name="<your_sub_class_name>">
 * <intent-filter>
 * <action android:name="com.ubikod.capptain.intent.action.MESSAGE"/>
 * </intent-filter>
 * </receiver>}<br/>
 * This class can also help receive device messages and detect duplicates (it can happen in some
 * edge cases). You have to add the {@code <action
 * android:name="com.ubikod.capptain.intent.action.DEVICE_MESSAGE"/>} to the intent filter in order
 * for the {@link #onDeviceMessageReceived(Context, String, String)} to be called.<br/>
 * You can also receive XMPP messages sent by entities that are not devices. You have to add the
 * {@code <action
 * android:name="com.ubikod.capptain.intent.action.XMPP_MESSAGE"/>} to the intent filter in order
 * for the {@link #onXMPPMessageReceived(Context, Bundle)} to be called.
 */
public abstract class CapptainMessageReceiver extends BroadcastReceiver
{
  /** Remember ids of device messages recently received to counter the broadcast side effect */
  @SuppressWarnings("serial")
  private static final Map<String, String> deviceMessageRecentlyReceived = new LinkedHashMap<String, String>(
    16, 0.75f, true)
  {
    protected boolean removeEldestEntry(Map.Entry<String, String> eldest)
    {
      return size() > 100;
    };
  };

  @Override
  public void onReceive(Context context, Intent intent)
  {
    /* Handle push message */
    if ("com.ubikod.capptain.intent.action.MESSAGE".equals(intent.getAction()))
    {
      Bundle message = intent.getBundleExtra("com.ubikod.capptain.intent.extra.MESSAGE");
      if (message != null && verifySignature(message))
      {
        /* Deliver to callback */
        String id = message.getString("id");
        String payload = message.getString("payload");
        String replyto = message.getString("replyto");
        onPushMessageReceived(context, id, payload, replyto);
      }
    }

    /* Handle device message */
    else if ("com.ubikod.capptain.intent.action.DEVICE_MESSAGE".equals(intent.getAction()))
    {
      /*
       * If id is set, avoid duplicated messages due to an XMPP broadcast because several services
       * are running (no exact resource matching could be done).
       */
      String id = intent.getStringExtra("id");
      if (id == null || deviceMessageRecentlyReceived.put(id, id) == null)
      {
        String deviceId = intent.getStringExtra("from");
        String payload = intent.getStringExtra("payload");
        onDeviceMessageReceived(context, deviceId, payload);
      }
    }

    /* Handle other XMPP messages */
    else if ("com.ubikod.capptain.intent.action.XMPP_MESSAGE".equals(intent.getAction()))
      onXMPPMessageReceived(context, intent.getExtras());
  }

  /**
   * Verify that the message has been signed by the Push Service.
   * @param message message to verify.
   * @return true if the signature is valid, false otherwise.
   */
  private boolean verifySignature(Bundle message)
  {
    return DataVerifier.verify(message.getString("payload"), message.getString("signature"));
  }

  /**
   * This function is called when a message signed by the Push Service has been received.
   * @param context the context in which the receiver is running.
   * @param id message identifier.
   * @param payload message payload.
   * @param replyto optional XMPP address to reply to (can be <tt>null</tt>).
   */
  protected void onPushMessageReceived(Context context, String id, String payload, String replyto)
  {
    /* Optional callback */
  }

  /**
   * This function is called when a message sent by a device has been received.
   * @param context the context in which the receiver is running.
   * @param deviceId device identifier.
   * @param payload message payload.
   */
  protected void onDeviceMessageReceived(Context context, String deviceId, String payload)
  {
    /* Optional callback */
  }

  /**
   * This function is called when an XMPP message is received (but not by a device).
   * @param context the context in which the receiver is running.
   * @param message a Bundle with the following structure, all the keys are optional except the
   *          "from" key.
   *          <ul>
   *          <li>"from" : (String) JID that sent the message</li>
   *          <li>"subject" : (String) message subject</li>
   *          <li>"thread" : (String) message thread</li>
   *          <li>"body" : (String) message body</li>
   *          <li>"type" : (String) message type like "chat" or "heading" or none</li>
   *          <li>"extensions" : (Bundle) A sub-bundle containing XMPP extensions, keys are
   *          namespaces and values are the corresponding XML strings.</li>
   *          </ul>
   */
  protected void onXMPPMessageReceived(Context context, Bundle message)
  {
    /* Optional callback */
  }
}
