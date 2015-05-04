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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.utils.CapptainUtils;

/**
 * This is the implementation of the installation tracking in the Capptain SDK. You don't need to
 * use this class directly, instead you can integrate {@link CapptainTrackReceiver}.<br/>
 * @see CapptainTrackReceiver
 */
public class CapptainTrackAgent
{
  /** Storage file name (without file extension which is .xml) */
  private static final String STORAGE_FILE = "capptain.track";

  /** Format of keys to store which identifier we already reported: <appId>/<aduid> */
  private static final String STORAGE_KEY_FORMAT = "%s/%s";

  /**
   * Format of tag keys for identifiers to report: aduid_<id_type>, we also have
   * aduid_<id_type>_<hash_type|adserver_type> but not yet used.
   */
  private static final String ADUID_KEY_SIMPLE = "aduid_%s";

  /** Application meta-data key for Ad Servers configuration */
  private static final String ADSERVERS_CONFIG = "capptain:track:adservers";

  /** Supported Ad Servers */
  private enum AdServer
  {
    SMARTAD;

    /** Get identifiers to report for this Ad Server */
    Iterable<ADUID> getADUIDs()
    {
      switch (this)
      {
        case SMARTAD:
          return Collections.singleton(ADUID.ANDROIDID);
      }
      throw new UnsupportedOperationException();
    }
  }

  /** Ids to report */
  private enum ADUID
  {
    /** ANDROID ID */
    ANDROIDID("and");

    /** ID type suffix */
    private final String idType;

    /** Init */
    private ADUID(String idSuffix)
    {
      this.idType = idSuffix;
    }

    /** Get tag key for this identifier, to use for {@link CapptainAgent#sendAppInfo(Bundle)}. */
    String getTagKey()
    {
      switch (this)
      {
        case ANDROIDID:
          return String.format(ADUID_KEY_SIMPLE, idType);
      }
      throw new UnsupportedOperationException();
    }

    /** Get tag value for this identifier, to use for {@link CapptainAgent#sendAppInfo(Bundle)}. */
    String getTagValue(Context context)
    {
      switch (this)
      {
        case ANDROIDID:
          return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
      }
      throw new UnsupportedOperationException();
    }
  }

  /** Unique instance */
  private static CapptainTrackAgent sInstance;

  /** Application context */
  private final Context mContext;

  /** Capptain Agent */
  private final CapptainAgent mCapptainAgent;

  /** Shared preference file to remember which ad server identifiers we already reported */
  private final SharedPreferences mStorage;

  /**
   * Get the unique instance.
   * @param context any valid context
   */
  public static CapptainTrackAgent getInstance(Context context)
  {
    /* Always check this even if we instantiate once to trigger null pointer in all cases */
    if (sInstance == null)
      sInstance = new CapptainTrackAgent(context.getApplicationContext());
    return sInstance;
  }

  /**
   * Init.
   * @param context application context.
   */
  private CapptainTrackAgent(Context context)
  {
    /* Init */
    mContext = context;
    mCapptainAgent = CapptainAgent.getInstance(context);
    mStorage = context.getSharedPreferences(STORAGE_FILE, 0);

    /* Wait for the application identifier to be known, i.e. wait call to onAppIdGot() */
  }

  /**
   * Notify when the application identifier is known.
   * @param appId application identifier.
   */
  void onAppIdGot(String appId)
  {
    /* Check param */
    if (TextUtils.isEmpty(appId))
      throw new IllegalArgumentException("empty appId");

    /* Get ad servers configuration */
    Bundle config = CapptainUtils.getMetaData(mContext);
    String adServers = config.getString(ADSERVERS_CONFIG);

    /* If ad servers configured */
    if (adServers != null)
    {
      /* Ad servers are comma separated, iterate */
      EnumMap<ADUID, Void> aduids = new EnumMap<ADUID, Void>(ADUID.class);
      TextUtils.StringSplitter adServerSplitter = new TextUtils.SimpleStringSplitter(',');
      adServerSplitter.setString(adServers);
      for (String adServerSubString : adServerSplitter)
        try
        {
          /* Parse ad server */
          AdServer adServer = AdServer.valueOf(adServerSubString.toUpperCase(Locale.US));

          /* Get identifiers to report */
          for (ADUID aduid : adServer.getADUIDs())
            aduids.put(aduid, null);
        }
        catch (RuntimeException e)
        {
          /* Thrown by AdServers.valueOf: ignore invalid ad servers */
        }

      /* Build tags */
      if (!aduids.isEmpty())
      {
        /* For each identifier to report */
        Bundle tags = new Bundle();
        Editor storageEdit = mStorage.edit();
        for (ADUID aduid : aduids.keySet())
        {
          /* Check if we already registered this identifier with the current appId */
          String storageKey = String.format(STORAGE_KEY_FORMAT, appId, aduid);
          if (!mStorage.getBoolean(storageKey, false))
          {
            tags.putString(aduid.getTagKey(), aduid.getTagValue(mContext));
            storageEdit.putBoolean(storageKey, true);
          }
        }

        /* Send tags and commit storage modifications */
        if (!tags.isEmpty())
        {
          mCapptainAgent.sendAppInfo(tags);
          storageEdit.commit();
        }
      }
    }
  }
}
