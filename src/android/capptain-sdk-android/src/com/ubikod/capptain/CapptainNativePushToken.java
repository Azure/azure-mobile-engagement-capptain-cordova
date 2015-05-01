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

import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

/** Native push registration with Capptain */
public class CapptainNativePushToken implements Parcelable
{
  /** Native push service type */
  public enum Type
  {
    C2DM,
    GCM,
    ADM;

    @Override
    public String toString()
    {
      return name().toLowerCase(Locale.US);
    };
  }

  /** Parcelable factory */
  public static final Parcelable.Creator<CapptainNativePushToken> CREATOR = new Parcelable.Creator<CapptainNativePushToken>()
  {
    @Override
    public CapptainNativePushToken createFromParcel(Parcel in)
    {
      return new CapptainNativePushToken(in);
    }

    @Override
    public CapptainNativePushToken[] newArray(int size)
    {
      return new CapptainNativePushToken[size];
    }
  };

  /** Token value (registration identifier) */
  private final String token;

  /** Token type */
  private final Type type;

  /**
   * Init.
   * @param token registration identifier.
   * @param type service type.
   */
  public CapptainNativePushToken(String token, Type type)
  {
    this.token = token;
    this.type = type;
  }

  /**
   * Unmarshal a parcel.
   * @param in parcel.
   */
  private CapptainNativePushToken(Parcel in)
  {
    token = in.readString();
    type = typeFromInt(in.readInt());
  }

  /**
   * Get token value.
   * @return token value.
   */
  public String getToken()
  {
    return token;
  }

  /**
   * Get token native push service type.
   * @return token native push service type.
   */
  public Type getType()
  {
    return type;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags)
  {
    dest.writeString(token);
    dest.writeInt(type == null ? -1 : type.ordinal());
  }

  @Override
  public int describeContents()
  {
    return 0;
  }

  /**
   * Get token type from integer (enum ordinal).
   * @param ordinal enum ordinal.
   * @return token type, or null if ordinal is invalid.
   */
  public static Type typeFromInt(int ordinal)
  {
    Type[] values = Type.values();
    return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
  }
}
