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

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

/**
 * Data presented to devices is signed by the Push Service, this class handles verifying it.
 */
class DataVerifier
{
  /** Push service public key */
  private static final String PUBLIC_KEY = "305c300d06092a864886f70d0101010500034b003048024100d1f4dcc52abb4ca2f06ea2a0e47018e2c5566649275924faf346e506dd4da6192d0fc86193c1411610dc63a53d9742ab3e90b7ffdb6f11cc44bea11b3cf4a3190203010001";

  /** Decode an hexadecimal string to its binary form */
  private static byte[] decodeHex(String data)
  {
    int len = data.length();
    if ((len & 0x01) != 0)
      throw new IllegalArgumentException("Odd number of characters.");
    byte[] out = new byte[len >> 1];

    /* Two characters form the hex value */
    for (int i = 0, j = 0; j < len; i++)
    {
      int f = toDigit(data.charAt(j), j) << 4;
      j++;
      f = f | toDigit(data.charAt(j), j);
      j++;
      out[i] = (byte) (f & 0xFF);
    }

    return out;
  }

  /** Convert an hexadecimal digit to its decimal value (e.g. 'a' -> 10) */
  private static int toDigit(char ch, int index)
  {
    int digit = Character.digit(ch, 16);
    if (digit == -1)
      throw new IllegalArgumentException("Illegal hexadecimal charcter " + ch + " at index "
        + index);
    return digit;
  }

  /**
   * Verify the signature of a data.
   * @param data data to verify the signature.
   * @param signature hexadecimal signature.
   * @return true if the signature is valid, false otherwise.
   */
  public static boolean verify(String data, String signature)
  {
    try
    {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = keyFactory
        .generatePublic(new X509EncodedKeySpec(decodeHex(PUBLIC_KEY)));
      Signature signer = Signature.getInstance("MD5WithRSA");
      signer.initVerify(publicKey);
      signer.update(data.getBytes("utf-8"));
      return signer.verify(decodeHex(signature));
    }
    catch (Exception e)
    {
      return false;
    }
  }
}
