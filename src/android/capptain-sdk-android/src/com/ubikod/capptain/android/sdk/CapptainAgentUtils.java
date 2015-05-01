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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Process;

import com.ubikod.capptain.android.sdk.activity.CapptainActivity;

/**
 * Utility functions used by various classes of the Capptain SDK.
 */
public final class CapptainAgentUtils
{
  private CapptainAgentUtils()
  {
    /* Prevent instantiation */
  }

  /**
   * Return <tt>true</tt> if the caller runs in a process dedicated to the Capptain service.<br/>
   * Return <tt>false</tt> otherwise, e.g. if it's the application process (even if the Capptain
   * service is running in it) or another process.<br/>
   * This method is useful when the <b>android:process</b> attribute has been set on the Capptain
   * service, if this method return <tt>true</tt>, application initialization must not be done in
   * that process. This method is used by {@link CapptainApplication}.
   * @param context the application context.
   * @return <tt>true</tt> if the caller is running in a process dedicated to the Capptain service,
   *         <tt>false</tt> otherwise.
   * @see CapptainApplication
   */
  public static boolean isInDedicatedCapptainProcess(Context context)
  {
    /* Get our package info */
    PackageInfo packageInfo;
    try
    {
      packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
        PackageManager.GET_SERVICES);
    }
    catch (Exception e)
    {
      /*
       * NameNotFoundException (uninstalling?) or in some rare scenario an undocumented
       * "RuntimeException: Package manager has died.", probably caused by a system app process
       * crash.
       */
      return false;
    }

    /* Get main process name */
    String mainProcess = packageInfo.applicationInfo.processName;

    /* Get embedded Capptain process name */
    String capptainProcess = null;
    if (packageInfo.services != null)
      for (ServiceInfo serviceInfo : packageInfo.services)
        if ("com.ubikod.capptain.android.service.CapptainService".equals(serviceInfo.name))
        {
          capptainProcess = serviceInfo.processName;
          break;
        }

    /* If the embedded Capptain service runs on its own process */
    if (capptainProcess != null && !capptainProcess.equals(mainProcess))
    {
      /* The result is to check if the current process is the capptain process */
      ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      for (RunningAppProcessInfo rapInfo : activityManager.getRunningAppProcesses())
        if (rapInfo.pid == Process.myPid())
          return rapInfo.processName.equals(capptainProcess);
    }

    /* Otherwise capptain is not running in a separate process (or not running at all) */
    return false;
  }

  /**
   * Build an Capptain alias for an Android Activity class. This implementation takes the simple
   * name of the class and removes the "Activity" suffix if any (e.g. "com.mycompany.MainActivity"
   * becomes "Main").<br/>
   * This method is used by {@link CapptainActivity} and its variants.
   * @return an activity name suitable to be reported by the Capptain service.
   */
  public static String buildCapptainActivityName(Class<?> activityClass)
  {
    String name = activityClass.getSimpleName();
    String suffix = "Activity";
    if (name.endsWith(suffix) && name.length() > suffix.length())
      return name.substring(0, name.length() - suffix.length());
    else
      return name;
  }
}
