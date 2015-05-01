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

import android.app.Application;
import android.content.res.Configuration;

/**
 * Helper class used to replace Android's {@link Application} class.<br/>
 * If you currently extend the {@link Application} class, please make your class extend this class
 * instead. Your {@link #onCreate()} function has to be renamed
 * {@link #onApplicationProcessCreate()} and the same rule applies for the other callbacks. Make
 * sure to also rename the calls to the super methods the same way to avoid an infinite loop (which
 * will trigger a <tt>java.lang.StackOverflowError</tt>).<br/>
 * These new methods are called only if the current process is not dedicated to the Capptain service,
 * avoiding unnecessary initialization in that process.<br/>
 * If you use an Application sub-class but you don't want to extend this class, you can use directly
 * {@link CapptainAgentUtils#isInDedicatedCapptainProcess(android.content.Context)} and execute your legacy
 * code only if this method return <tt>false</tt>.
 * @see CapptainAgentUtils#isInDedicatedCapptainProcess(android.content.Context)
 */
public abstract class CapptainApplication extends Application
{
  @Override
  public final void onCreate()
  {
    if (!CapptainAgentUtils.isInDedicatedCapptainProcess(this))
      onApplicationProcessCreate();
  }

  @Override
  public final void onTerminate()
  {
    if (!CapptainAgentUtils.isInDedicatedCapptainProcess(this))
      onApplicationProcessTerminate();
  }

  @Override
  public final void onLowMemory()
  {
    if (!CapptainAgentUtils.isInDedicatedCapptainProcess(this))
      onApplicationProcessLowMemory();
  }

  @Override
  public final void onConfigurationChanged(Configuration newConfig)
  {
    if (!CapptainAgentUtils.isInDedicatedCapptainProcess(this))
      onApplicationProcessConfigurationChanged(newConfig);
  }

  /**
   * Override this method instead of {@link #onCreate()} to avoid doing unnecessary operations when
   * the current process is the one dedicated to the Capptain service.
   */
  protected void onApplicationProcessCreate()
  {
    /* Sub-class template method */
  }

  /**
   * Override this method instead of {@link #onTerminate()} to avoid doing unnecessary operations
   * when the current process is the one dedicated to the Capptain service.
   */
  protected void onApplicationProcessTerminate()
  {
    /* Sub-class template method */
  }

  /**
   * Override this method instead of {@link #onLowMemory()} to avoid doing unnecessary operations
   * when the current process is the one dedicated to the Capptain service.
   */
  protected void onApplicationProcessLowMemory()
  {
    /* Sub-class template method */
  }

  /**
   * Override this method instead of {@link #onConfigurationChanged(Configuration)} to avoid doing
   * unnecessary operations when the current process is the one dedicated to the Capptain service.
   */
  protected void onApplicationProcessConfigurationChanged(Configuration newConfig)
  {
    /* Sub-class template method */
  }
}
