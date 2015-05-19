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

package com.ubikod.capptain.android.sdk.nativepush;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ubikod.capptain.CapptainNativePushToken;
import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.adm.CapptainADMEnabler;
import com.ubikod.capptain.android.sdk.adm.CapptainADMReceiver;
import com.ubikod.capptain.android.sdk.gcm.CapptainGCMEnabler;
import com.ubikod.capptain.android.sdk.gcm.CapptainGCMReceiver;

/**
 * <p>
 * This agent manages the life cycle of the registration identifier (called token in Capptain) used
 * in native push.
 * </p>
 * <p>
 * The token is associated to a Native Push service. A device may only have one token at any time
 * but you can safely integrate several services: at most 1 service is available at runtime.
 * <p>
 * Capptain currently supports GCM (Google Cloud Messaging) and ADM (Amazon Device Messaging).
 * Capptain cannot work with GCM if your own application code uses deprecated C2DM: the device can
 * only have one registration identifier at any time. Please migrate your application to GCM in that
 * case.
 * </p>
 * <p>
 * You don't need to use this class directly, you can instead integrate the following classes.
 * </p>
 * <ul>
 * <li>For GCM:
 * <ul>
 * <li>{@link CapptainGCMEnabler}</li>
 * <li>{@link CapptainGCMReceiver}</li>
 * </ul>
 * </li>
 * <li>For ADM:
 * <ul>
 * <li>{@link CapptainADMEnabler}</li>
 * <li>{@link CapptainADMReceiver}</li>
 * </ul>
 * </li>
 * </ul>
 */
public class CapptainNativePushAgent
{
  /** Storage file name */
  private static final String STORAGE_FILE = "capptain.nativepush";

  /** Storage key: application identifier for which we sent the token. */
  private static final String APP_ID = "appId";

  /** Storage key: token value */
  private static final String TOKEN_VALUE = "val";

  /** Storage key: token type */
  private static final String TOKEN_TYPE = "type";

  /** Storage key: sent timestamp in ms since epoch */
  private static final String SENT = "sent";

  /** Storage key: new registration identifier value */
  private static final String NEW_TOKEN_VALUE = "newVal";

  /** Storage key: new registration identifier type */
  private static final String NEW_TOKEN_TYPE = "newType";

  /**
   * We re-send the same registration identifier only if some time has elapsed, specified by this
   * constant (in ms). Value: 1 day.
   */
  private static final long SENT_EXPIRY = 86400000L;

  /** Unique instance */
  private static CapptainNativePushAgent sInstance;

  /**
   * Get the unique instance.
   * @param context any valid context.
   */
  public static CapptainNativePushAgent getInstance(Context context)
  {
    /* Always check this even if we instantiate once to trigger null pointer in all cases */
    if (sInstance == null)
      sInstance = new CapptainNativePushAgent(context.getApplicationContext());
    return sInstance;
  }

  /** Storage file */
  private final SharedPreferences mStorage;

  /** Capptain agent */
  private final CapptainAgent mCapptainAgent;

  /** Application identifier */
  private String mAppId;

  /**
   * Init.
   * @param context application context.
   */
  private CapptainNativePushAgent(Context context)
  {
    /* Init */
    mCapptainAgent = CapptainAgent.getInstance(context);
    mStorage = context.getSharedPreferences(STORAGE_FILE, 0);
  }

  /**
   * Calls {@link CapptainAgent#registerNativePush(CapptainNativePushToken)} only if the token is a
   * new one or some time has elapsed since the last time we sent it.
   * @param token token to register.
   */
  public void registerNativePush(CapptainNativePushToken token)
  {
    /* If application identifier is unknown */
    if (mAppId == null)
    {
      /*
       * Keep state until onAppIdGot is called. Possibly in the next application launch so it must
       * be persisted.
       */
      Editor edit = mStorage.edit();
      edit.putString(NEW_TOKEN_VALUE, token.getToken());
      edit.putInt(NEW_TOKEN_TYPE, token.getType().ordinal());
      edit.commit();
    }
    else
    {
      /* Get state */
      String oldAppId = mStorage.getString(APP_ID, null);
      String oldTokenValue = mStorage.getString(TOKEN_VALUE, null);
      CapptainNativePushToken.Type oldType = CapptainNativePushToken.typeFromInt(mStorage.getInt(
        TOKEN_TYPE, -1));
      long sent = mStorage.getLong(SENT, 0);
      long elapsedSinceSent = System.currentTimeMillis() - sent;

      /* If registrationId changed or enough time elapsed since we sent it */
      if (oldAppId == null || !oldAppId.equals(mAppId) || oldTokenValue == null
        || !oldTokenValue.equals(token.getToken()) || oldType == null
        || !oldType.equals(token.getType()) || elapsedSinceSent >= SENT_EXPIRY)
      {
        /* Send registration identifier */
        mCapptainAgent.registerNativePush(token);

        /* Update state */
        Editor edit = mStorage.edit();
        edit.clear();
        edit.putString(APP_ID, mAppId);
        edit.putString(TOKEN_VALUE, token.getToken());
        edit.putInt(TOKEN_TYPE, token.getType().ordinal());
        edit.putLong(SENT, System.currentTimeMillis());
        edit.commit();
      }
    }
  }

  /**
   * Notify when the application identifier is known.
   * @param appId application identifier.
   */
  public void onAppIdGot(String appId)
  {
    /* Keep identifier */
    mAppId = appId;

    /* Send pending token if any */
    String value = mStorage.getString(NEW_TOKEN_VALUE, null);
    CapptainNativePushToken.Type type = CapptainNativePushToken.typeFromInt(mStorage.getInt(
      NEW_TOKEN_TYPE, -1));
    if (value != null && type != null)
      registerNativePush(new CapptainNativePushToken(value, type));
  }
}
