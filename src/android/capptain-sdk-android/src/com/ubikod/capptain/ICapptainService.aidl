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

package com.ubikod.capptain;

import android.os.Bundle;
import com.ubikod.capptain.CapptainConfiguration;
import com.ubikod.capptain.CapptainNativePushToken;

/**
 * Capptain API, the Capptain service returns this remote interface when binding to it.<br/>
 * The binding intent has the following specification: <br/>
 * <b>action</b>: <tt>com.ubikod.capptain.ICapptainService</tt> <br/>
 * <b>URI</b>: <tt>capptain://&lt;your_application_package_name&gt; </tt><br/>
 * All calls on the returned binder will be related to the package name you provided in the intent.<br/>
 * This class is not designed to be used directly in user code, please use {@link com.ubikod.capptain.android.sdk.CapptainAgent} instead.
 * @see com.ubikod.capptain.android.sdk.CapptainAgent
 */
interface ICapptainService
{  
  oneway void startActivity(String activityName, in Bundle extras);

  oneway void endActivity();

  oneway void startJob(String name, in Bundle extras);

  oneway void endJob(String name);

  oneway void sendEvent(String name, in Bundle extras);

  oneway void sendSessionEvent(String name, in Bundle extras);

  oneway void sendJobEvent(String name, String jobName, in Bundle extras);

  oneway void sendError(String name, in Bundle extras);

  oneway void sendSessionError(String name, in Bundle extras);

  oneway void sendJobError(String name, String jobName, in Bundle extras);

  oneway void sendXMPPMessage(in Bundle msg);

  oneway void sendMessageToDevice(String deviceId, String payload, String packageName);

  String getDeviceId();

  oneway void sendAppInfo(in Bundle appInfo);

  Bundle getInfo();

  /* Replaced by {#link #registerNativePush(CapptainNativePushToken)} */
  oneway void sendC2DMRegistrationId(String registrationId, boolean gcm);

  oneway void sendReachFeedback(String kind, String contentId, String status, in Bundle extras);

  oneway void configure(in CapptainConfiguration configuration);

  oneway void registerNativePush(in CapptainNativePushToken token);
}
