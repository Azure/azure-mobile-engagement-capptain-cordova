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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Capptain configuration. This contains configuration that can be dynamically changed at runtime.
 * All the fields are optional.
 */
public class CapptainConfiguration implements Parcelable
{
  /** Parcelable factory */
  public static final Parcelable.Creator<CapptainConfiguration> CREATOR = new Parcelable.Creator<CapptainConfiguration>()
  {
    @Override
    public CapptainConfiguration createFromParcel(Parcel in)
    {
      return new CapptainConfiguration(in);
    }

    @Override
    public CapptainConfiguration[] newArray(int size)
    {
      return new CapptainConfiguration[size];
    }
  };

  /** Application identifier */
  private String mAppId;

  /** Init. */
  public CapptainConfiguration()
  {
  }

  /**
   * Unmarshal a parcel.
   * @param in parcel.
   */
  private CapptainConfiguration(Parcel in)
  {
    mAppId = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeString(mAppId);
  }

  @Override
  public int describeContents()
  {
    return 0;
  }

  /**
   * Get configured application identifier.
   * @return configured application identifier.
   */
  public String getAppId()
  {
    return mAppId;
  }

  /**
   * Prepare configuration of the application identifier. This is optional, if missing, the
   * application identifier is either read from the <tt>AndroidManifest.xml</tt> file, or determined
   * by the package name & signature combination.
   * @param appId configured application identifier.
   */
  public void setAppId(String appId)
  {
    mAppId = appId;
  }
}
