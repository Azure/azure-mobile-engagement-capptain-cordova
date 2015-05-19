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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;

/** This class helps to track the current activity (must be an Capptain activity) */
public class CapptainActivityManager
{
  /** Interface to listen on current activity changes */
  public interface Listener
  {
    /**
     * Called when the current activity changed.
     * @param currentActivity weak reference on current activity, referent may be null if no current
     *          activity
     * @param capptainAlias current activity name as reported in Capptain logs.
     */
    void onCurrentActivityChanged(WeakReference<Activity> currentActivity, String capptainAlias);
  }

  /** Unique instance */
  private static CapptainActivityManager sInstance = new CapptainActivityManager();

  /**
   * Get unique instance.
   * @return unique instance.
   */
  public static CapptainActivityManager getInstance()
  {
    return sInstance;
  }

  /**
   * Null weak reference, this is useful for calling {@link WeakReference#get()} without having to
   * check for the null pointer on the WeakReference object...
   */
  private WeakReference<Activity> mNullActivity = new WeakReference<Activity>(null);

  /** Current activity weak reference */
  private WeakReference<Activity> mCurrentActivity = mNullActivity;

  /** Current activity alias (name) */
  private String mCurrentActivityAlias;

  /** Current activity listeners */
  private Map<Listener, Object> mListeners = new ConcurrentHashMap<Listener, Object>();

  /** Dummy value to insert in the listener map (we only use keys) */
  private Object mDummyValue = new Object();

  /**
   * Get current activity weak reference. May be null even if {@link #getCurrentActivityAlias()}
   * returns something not null.
   * @return current activity weak reference.
   */
  public WeakReference<Activity> getCurrentActivity()
  {
    return mCurrentActivity;
  }

  /**
   * Get current activity alias as reported by Capptain logs.
   * @return current activity alias as reported by Capptain logs, null if the current activity is
   *         null.
   */
  public String getCurrentActivityAlias()
  {
    return mCurrentActivityAlias;
  }

  /**
   * Set the current activity, Capptain activity classes call this in their
   * {@link Activity#onResume()}
   * @param activity current activity.
   * @param capptainAlias alias as reported in Capptain logs.
   */
  public void setCurrentActivity(Activity activity, String capptainAlias)
  {
    mCurrentActivity = new WeakReference<Activity>(activity);
    if (capptainAlias == null)
      mCurrentActivityAlias = "default";
    else
      mCurrentActivityAlias = capptainAlias.trim();
    for (Listener listener : mListeners.keySet())
      listener.onCurrentActivityChanged(mCurrentActivity, mCurrentActivityAlias);
  }

  /**
   * Remove the current activity. Capptain activity classes call this in their
   * {@link Activity#onPause()}. This will be called when switching between two activities.
   */
  public void removeCurrentActivity()
  {
    mCurrentActivity = mNullActivity;
    mCurrentActivityAlias = null;
    for (Listener listener : mListeners.keySet())
      listener.onCurrentActivityChanged(mCurrentActivity, mCurrentActivityAlias);
  }

  /**
   * Install a listener on current activity changes, this will trigger it with the current values.
   * @param listener the listener to install.
   */
  public void addCurrentActivityListener(Listener listener)
  {
    mListeners.put(listener, mDummyValue);
    listener.onCurrentActivityChanged(mCurrentActivity, mCurrentActivityAlias);
  }

  /**
   * Uninstall a listener on current activity changes.
   * @param listener the listener to uninstall.
   */
  public void removeCurrentActivityListener(Listener listener)
  {
    mListeners.remove(listener);
  }
}
