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

package com.ubikod.capptain.utils;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Bundle;

import com.ubikod.capptain.Capptain;
import com.ubikod.capptain.ICapptainService;

/** Utility functions */
public final class CapptainUtils
{
  private CapptainUtils()
  {
    /* Prevent instantiation */
  }

  /**
   * Check if a package holds the specified permission.
   * @param packageManager package manager.
   * @param packageName package name to test.
   * @param permission permission to test.
   * @return true iff the package holds the permission or permission is null.
   */
  private static boolean checkPermission(PackageManager packageManager, String packageName,
    String permission)
  {
    return permission == null
      || packageManager.checkPermission(permission, packageName) == PERMISSION_GRANTED;
  }

  /**
   * Get the most suitable Capptain service intent to bind to.
   * @param context any application context.
   * @return an explicit intent that can used to bind to the service or null if no such intent can
   *         be resolved.
   */
  public static Intent resolveCapptainService(Context context)
  {
    /* Build the base intent */
    String packageName = context.getPackageName();
    Intent intent = new Intent(ICapptainService.class.getName(), Uri.parse("capptain://"
      + packageName));

    /* Check if we need location permissions */
    Bundle config = getMetaData(context);
    boolean realTimeLocationReport = config.getBoolean("capptain:locationReport:realTime");
    boolean needNetworkLocation = config.getBoolean("capptain:locationReport:lazyArea")
      || realTimeLocationReport;
    boolean needGpsLocation = realTimeLocationReport
      && config.getBoolean("capptain:locationReport:realTime:fine");

    /* Check services that can handle it */
    PackageManager packageManager = context.getPackageManager();
    int maxScore = -1;
    ComponentName bestService = null;
    try
    {
      for (ResolveInfo service : packageManager.queryIntentServices(intent, GET_META_DATA))
      {
        /*
         * Check service is either the same application or exported, binding to a non exported
         * service makes an ANR.
         */
        ServiceInfo serviceInfo = service.serviceInfo;
        String servicePackageName = serviceInfo.packageName;
        if (!serviceInfo.exported && !packageName.equals(servicePackageName))
          continue;

        /*
         * To avoid ANR, also check that the service is not protected by a permission we don't have.
         * We must also ensure that the service holds its own permission otherwise the service's
         * process will crash!
         */
        String servicePermission = serviceInfo.permission;
        if (!checkPermission(packageManager, packageName, servicePermission)
          || !checkPermission(packageManager, servicePackageName, servicePermission))
          continue;

        /* Check API level/ID and required permissions */
        Bundle metaData = serviceInfo.metaData;
        boolean hasBasePermissions = checkPermission(packageManager, servicePackageName, INTERNET);
        hasBasePermissions &= checkPermission(packageManager, servicePackageName,
          ACCESS_NETWORK_STATE);
        if (hasBasePermissions && metaData != null)
        {
          /* Check API level and API id */
          int apiLevel = metaData.getInt("capptain:api:level");
          String apiId = metaData.getString("capptain:api:id");
          if (apiLevel >= Capptain.API_LEVEL && Capptain.API_ID.equals(apiId))
          {
            /* Check if this service must be skipped because not having network location */
            boolean hasNetworkLocation = checkPermission(packageManager, servicePackageName,
              ACCESS_COARSE_LOCATION);
            if (needNetworkLocation && !hasNetworkLocation)
              continue;

            /* Same test with GPS */
            boolean hasGpsLocation = checkPermission(packageManager, servicePackageName,
              ACCESS_FINE_LOCATION);
            if (needGpsLocation && !hasGpsLocation)
              continue;

            /*
             * Compute matching score: API level has the most important role in scoring: apiLevel *
             * 4. Even if we don't need location report, this will choose the same service as
             * applications that need location report. This policy raises chances of having only one
             * service running on the device.
             */
            int score = apiLevel * 4;
            score += hasNetworkLocation ? 2 : 0;
            score += hasGpsLocation ? 1 : 0;

            /* If score has been beaten */
            if (score > maxScore)
            {
              /* Keep this service */
              bestService = new ComponentName(servicePackageName, serviceInfo.name);
              maxScore = score;
            }
          }
        }
      }
    }
    catch (RuntimeException e)
    {
      /* Catch "Package manager has died": make service resolution fail in that case. */
    }

    /* if a service was found */
    if (bestService != null)
    {
      /* Make the intent explicit */
      intent.setComponent(bestService);
      return intent;
    }

    /* Otherwise return null intent */
    else
      return null;
  }

  /**
   * Get application meta-data of the current package name.
   * @param context application context.
   * @return meta-data, may be empty but never null.
   */
  public static Bundle getMetaData(Context context)
  {
    return getMetaData(context, context.getPackageName());
  }

  /**
   * Get application meta-data of a package name.
   * @param context application context.
   * @param packageName package name to get meta-data.
   * @return meta-data, may be empty but never null.
   */
  public static Bundle getMetaData(Context context, String packageName)
  {
    Bundle config;
    try
    {
      config = context.getPackageManager().getApplicationInfo(packageName,
        PackageManager.GET_META_DATA).metaData;
      if (config == null)
        config = new Bundle();
    }
    catch (Exception e)
    {
      /*
       * NameNotFoundException or in some rare scenario an undocumented "RuntimeException: Package
       * manager has died.", probably caused by a system app process crash.
       */
      config = new Bundle();
    }
    return config;
  }

  /**
   * Get activity meta-data.
   * @param activity activity to get meta-data from.
   * @return meta-data, may be empty but never null.
   */
  public static Bundle getActivityMetaData(Activity activity)
  {
    Bundle config;
    try
    {
      config = activity.getPackageManager().getActivityInfo(activity.getComponentName(),
        GET_META_DATA).metaData;
      if (config == null)
        config = new Bundle();
    }
    catch (Exception e)
    {
      /*
       * NameNotFoundException or in some rare scenario an undocumented "RuntimeException: Package
       * manager has died.", probably caused by a system app process crash.
       */
      config = new Bundle();
    }
    return config;
  }
}
