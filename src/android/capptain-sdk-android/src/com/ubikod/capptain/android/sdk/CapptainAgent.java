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

import static android.content.Context.BIND_AUTO_CREATE;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.ubikod.capptain.CapptainConfiguration;
import com.ubikod.capptain.CapptainNativePushToken;
import com.ubikod.capptain.ICapptainService;
import com.ubikod.capptain.utils.CapptainUtils;

/**
 * This is the main class to access Capptain features.<br/>
 * It is responsible for managing the connection between the application and the Capptain service in
 * a seamless way (so you don't have to use {@link ICapptainService} directly).<br/>
 * There is some configuration you can alter in the <tt>AndroidManifest.xml</tt> file, thanks to
 * meta-data inside the application tag:<br/>
 * <ul>
 * <li>To configure the application identifier, add
 * {@code <meta-data android:name="capptain:appId" android:value="<your_appid>"/>}.<br/>
 * If missing, the package name and the signature are used to resolve it.<br/>
 * Can be overriden at runtime via {@link #configure(CapptainConfiguration)}.</li>
 * <li>To disable crash report add {@code <meta-data android:name="capptain:reportCrash"
 * android:value="false"/>}.</li>
 * <li>By default, logs are reported in real time, if you want to report logs at regular intervals,
 * add {@code <meta-data android:name="capptain:burstThreshold"
 * android:value="<interval_in_millis>"/>}.</li>
 * <li>By default, the Capptain service establishes the connection with our servers as soon as the
 * network is available. If you want to postpone the connection, add
 * {@code <meta-data android:name="capptain:connection:delay" android:value="<delay_in_millis>"/>}.</li>
 * </li>
 * <li>To configure the session timeout (which is set to 10s by default), add
 * {@code <meta-data android:name="capptain:sessionTimeout" android:value="<duration_in_millis>"/>},
 * see {@link #endActivity()}.</li>
 * <li>To enable lazy area location report, add {@code <meta-data android:name="capptain:locationReport:lazyArea"
 * android:value="true"/>}.</li>
 * <li>To enable real time location report, add {@code <meta-data android:name="capptain:locationReport:realTime"
 * android:value="true"/>}.<br/>
 * There are sub settings:
 * <ul>
 * <li>By default, only network based locations are reported. To enable GPS, add
 * {@code <meta-data android:name="capptain:locationReport:realTime:fine" android:value="true"/>}.</li>
 * <li>By default, the reporting is done only when there is an active session. To enable background
 * mode, add {@code <meta-data android:name="capptain:locationReport:realTime:background"
 * android:value="true"/>}.<br/>
 * The background mode is only for network based locations, not GPS.<br/>
 * To make the background mode starts when the device boots, see
 * {@link CapptainLocationBootReceiver}.</li>
 * </ul>
 * </li>
 * </ul>
 * To enable test logs, you can also add meta-data inside the Capptain service tag:
 * {@code <meta-data android:name="capptain:log:test"
 * android:value="true" />}, the tag is "capptain-test".
 */
public class CapptainAgent
{
  /** Agent created action */
  public static final String INTENT_ACTION_AGENT_CREATED = "com.ubikod.capptain.intent.action.AGENT_CREATED";

  /** Maximum pending commands to keep while not bound to a service */
  private static final int MAX_COMMANDS = 200;

  /** Unbind timeout */
  private static final long BINDER_TIMEOUT = 30000;

  /** Setting key, prefixed in case of shared integration */
  private static final String ENABLED = "capptain:enabled";

  /** Unique instance */
  private static CapptainAgent sInstance;

  /** Android default crash handler */
  private static final UncaughtExceptionHandler sAndroidCrashHandler = Thread.getDefaultUncaughtExceptionHandler();

  /** Our crash handler */
  private final CrashHandler mCapptainCrashHandler = new CrashHandler();

  /** Capptain crash handler implementation */
  private final class CrashHandler implements UncaughtExceptionHandler
  {
    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
      /*
       * Report crash to the service via start service, because we can't bind to a service if not
       * already bound at this stage. We also check if the agent is enabled.
       */
      Intent intent = CapptainUtils.resolveCapptainService(mContext);
      if (intent != null && isEnabled())
      {
        /* Get crash identifier */
        CrashId crashId = CrashId.from(mContext, ex);

        /* Dump stack trace */
        String stackTrace = Log.getStackTraceString(ex);

        /* Set parameters and send the intent */
        intent.putExtra("com.ubikod.capptain.intent.extra.CRASH_TYPE", crashId.getType());
        intent.putExtra("com.ubikod.capptain.intent.extra.CRASH_LOCATION", crashId.getLocation());
        intent.putExtra("com.ubikod.capptain.intent.extra.CRASH_STACK_TRACE", stackTrace);
        mContext.startService(intent);
      }

      /* Do not prevent android from doing its job (or another crash handler) */
      sAndroidCrashHandler.uncaughtException(thread, ex);
    }
  }

  /** Settings */
  private final SharedPreferences mSettings;

  /** Settings listener */
  private final OnSharedPreferenceChangeListener mSettingsListener;

  /** True if crash are reported */
  private final boolean mReportCrash;

  /** Capptain service binder */
  private ICapptainService mCapptainService;

  /** Last configuration sent to service */
  private CapptainConfiguration mCapptainConfiguration;

  /** Last configuration sent to service was null */
  private boolean mCapptainConfigurationSentAsNull;

  /** Last bound service */
  private ComponentName mLastBoundService;

  /** Context used for binding to the Capptain service */
  private final Context mContext;

  /** Calls made before the service was bound */
  private final Queue<Runnable> mPendingCmds = new LinkedList<Runnable>();

  /** Unbind task */
  private final Runnable mUnbindTask = new Runnable()
  {
    @Override
    public void run()
    {
      /* Guard against failed cancel */
      if (mCapptainService == null || !mUnbindScheduled)
        return;

      /* Unbind from Capptain service */
      mContext.unbindService(mServiceConnection);

      /* Store unbound state */
      mCapptainService = null;

      /* Task done */
      mUnbindScheduled = false;
    }
  };

  /** True if unbind has been scheduled */
  private boolean mUnbindScheduled;

  /** Not null if binding, value is the component name describe the specific service */
  private ComponentName mBindingService;

  /** Handler for unbind timeouts */
  private final Handler mHandler;

  /** Service connection */
  private final ServiceConnection mServiceConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
      /* Cast the binder into the proper API and keep a reference */
      mCapptainService = ICapptainService.Stub.asInterface(service);

      /* We are not binding anymore */
      mBindingService = null;

      /* Be sure to submit configuration again if we changed service during this process lifetime */
      if (!name.equals(mLastBoundService)
        && (mCapptainConfiguration != null || mCapptainConfigurationSentAsNull))
        configure(mCapptainConfiguration);
      mLastBoundService = name;

      /* Send pending commands */
      for (Runnable cmd : mPendingCmds)
        cmd.run();
      mPendingCmds.clear();

      /* Schedule unbind (if not in session) */
      scheduleUnbind();
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
      /* We are not bound anymore */
      mCapptainService = null;

      /*
       * Simulate disconnected intent targeting the current package name since the capptain process
       * has been killed.
       */
      Intent disconnectedIntent = new Intent("com.ubikod.capptain.intent.action.DISCONNECTED");
      disconnectedIntent.putExtra("com.ubikod.capptain.intent.extra.SERVICE_PACKAGE",
        name.getPackageName());
      disconnectedIntent.setPackage(mContext.getPackageName());
      mContext.sendBroadcast(disconnectedIntent);

      /* If the service is still resolvable */
      Intent intent = CapptainUtils.resolveCapptainService(mContext);
      if (intent != null)
      {
        /* Mark we are binding to it */
        mBindingService = intent.getComponent();

        /* If it's a different one, bind to it */
        if (!mBindingService.equals(name))
        {
          cancelUnbind();
          mContext.unbindService(mServiceConnection);
          mContext.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        }

        /*
         * If it's the same: either it will be reconnected automatically if it has been killed, or
         * we wait for its re-installation to be complete (bind will fail before).
         */
      }
    }
  };

  /** Interface for retrieving results via callback */
  public interface Callback<T>
  {
    /**
     * Called when the function has been executed.
     * @param result the function result.
     */
    void onResult(T result);
  }

  /**
   * Init the agent.
   * @param context application context.
   */
  private CapptainAgent(Context context)
  {
    /* Store application context, we'll use this to bind */
    mContext = context;

    /* Create main thread handler */
    mHandler = new Handler(Looper.getMainLooper());

    /* Retrieve configuration */
    Bundle config = CapptainUtils.getMetaData(context);
    mReportCrash = config.getBoolean("capptain:reportCrash", true);
    String settingsFile = config.getString("capptain:agent:settings:name");
    int settingsMode = config.getInt("capptain:agent:settings:mode", 0);
    if (TextUtils.isEmpty(settingsFile))
      settingsFile = "capptain.agent";

    /* Watch preferences */
    mSettings = context.getSharedPreferences(settingsFile, settingsMode);
    mSettingsListener = new OnSharedPreferenceChangeListener()
    {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
      {
        /* Cancel all commands and unbind if agent disabled */
        if (ENABLED.equals(key) && !isEnabled())
        {
          mPendingCmds.clear();
          scheduleUnbind();
        }
      }
    };
    mSettings.registerOnSharedPreferenceChangeListener(mSettingsListener);

    /* Install Capptain crash handler if enabled */
    if (mReportCrash)
      Thread.setDefaultUncaughtExceptionHandler(mCapptainCrashHandler);

    /* Register installations for the whole process lifetime (no unregistering) */
    IntentFilter installIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    installIntentFilter.addDataScheme("package");
    mContext.registerReceiver(new BroadcastReceiver()
    {
      @Override
      public void onReceive(Context context, Intent intent)
      {
        /* If we were targeting this package */
        String packageName = intent.getData().getSchemeSpecificPart();
        if (mBindingService != null && packageName.equals(mBindingService.getPackageName()))

          /* Remove binding marker */
          mBindingService = null;

        /* Try to bind to the Capptain service if needed */
        if (mCapptainService == null && mPendingCmds.size() > 0 && isEnabled())
          bind();
      }
    }, installIntentFilter);

    /* Broadcast intent for Capptain modules */
    Intent agentCreatedIntent = new Intent(INTENT_ACTION_AGENT_CREATED);
    agentCreatedIntent.setPackage(context.getPackageName());
    context.sendBroadcast(agentCreatedIntent);
  }

  /**
   * Get the unique instance.
   * @param context any valid context
   */
  public static synchronized CapptainAgent getInstance(Context context)
  {
    /* Always check this even if we instantiate once to trigger null pointer in all cases */
    if (sInstance == null)
      sInstance = new CapptainAgent(context.getApplicationContext());
    return sInstance;
  }

  ComponentName getBindingService()
  {
    return mBindingService;
  }

  /**
   * Bind the agent to the Capptain service if not already done. This method opens the connection to
   * the Capptain service. Calling this method is required before calling any of the other methods
   * of the Capptain agent. This cancels unbind.
   */
  private void bind()
  {
    /* Cancel unbind */
    cancelUnbind();

    /* Bind to the Capptain service if not already done or being done */
    if (mCapptainService == null && mBindingService == null)
    {
      Intent intent = CapptainUtils.resolveCapptainService(mContext);
      if (intent != null)
      {
        mBindingService = intent.getComponent();
        mContext.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
      }
    }
  }

  /**
   * The service is automatically unbound when not used after a timeout. This method starts the
   * timer. Calling {@link #bind()} will cancel the timer. This method has no effect if the user is
   * in an session.
   */
  private void scheduleUnbind()
  {
    /* If not in session or disabled and not already unbinding */
    if (!mUnbindScheduled && (!isInSession() || !isEnabled()))
    {
      /* Schedule unbind */
      mHandler.postDelayed(mUnbindTask, BINDER_TIMEOUT);
      mUnbindScheduled = true;
    }
  }

  /**
   * Cancel a call to {@link #scheduleUnbind()}.
   */
  private void cancelUnbind()
  {
    mHandler.removeCallbacks(mUnbindTask);
    mUnbindScheduled = false;
  }

  /**
   * Check if a session is running.
   * @return true if a session is running, false otherwise.
   */
  private boolean isInSession()
  {
    return CapptainActivityManager.getInstance().getCurrentActivityAlias() != null;
  }

  /**
   * Call the Capptain Service API if bound, otherwise keep the call for later use.
   * @param cmd the task using the Capptain Service API.
   */
  private void sendCapptainCommand(final Runnable cmd)
  {
    /* The command needs to run on the main thread to avoid race conditions */
    runOnMainThread(new Runnable()
    {
      @Override
      public void run()
      {
        /* Nothing to do if disabled */
        if (!isEnabled())
          return;

        /* Bind if needed */
        bind();

        /* If we are not bound, spool command */
        if (mCapptainService == null)
        {
          mPendingCmds.offer(cmd);
          if (mPendingCmds.size() > MAX_COMMANDS)
            mPendingCmds.remove();
        }

        /* Otherwise call API and set unbind timer */
        else
        {
          cmd.run();
          scheduleUnbind();
        }
      }
    });
  }

  /** Check calling thread, if main, run now, otherwise post in main thread */
  private void runOnMainThread(Runnable task)
  {
    if (Thread.currentThread() == mHandler.getLooper().getThread())
      task.run();
    else
      mHandler.post(task);
  }

  /**
   * Notify the start of a new activity within the current session. A session being a sequence of
   * activities, this call sets the current activity within the current session. If there is no
   * current session, this call starts a new session.
   * @param activity current activity instance, may be null. Capptain modules may need to watch
   *          activity changes and may want to add content to the current view, passing null will
   *          prevent these modules to behave like expected.
   * @param activityName the name of the current activity for the current session, can be null for
   *          default name (but cannot be empty). Name is limited to 64 characters.
   * @param extras the extra details associated with the activity. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   * @see #endActivity()
   */
  public void startActivity(Activity activity, final String activityName, final Bundle extras)
  {
    CapptainActivityManager.getInstance().setCurrentActivity(activity, activityName);
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.startActivity(activityName, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Notify the end of the current activity within the current session. A session being a sequence
   * of activities, this call sets the current session idle. The current session is ended only if no
   * call to {@link #startActivity(Activity, String, Bundle)} follows this call within a time equal
   * to the session timeout (which is set to 10s by default). You can configure the session timeout
   * by adding {@code <meta-data android:name="capptain:sessionTimeout"
   * android:value="<duration_in_millis>"/>} under the application tag in your AndroidManifest.xml
   * file.
   * @see #startActivity(Activity, String, Bundle)
   */
  public void endActivity()
  {
    CapptainActivityManager.getInstance().removeCurrentActivity();
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.endActivity();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Notify the start of a new job.
   * @param name unique job name, two jobs with the same name can't run at the same time, if a job
   *          is started twice, the second version of the job will replace the first one. Name is
   *          limited to 64 characters and cannot be empty.
   * @param extras the extra details associated with this job. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   * @see #endJob(String)
   */
  public void startJob(final String name, final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.startJob(name, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Notify the end of a job. This has no effect if no job is running for the specified name.
   * @param name the name of a job that has been started with {@link #startJob(String, Bundle)
   *          startJob}
   * @see #startJob(String, Bundle)
   */
  public void endJob(final String name)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.endJob(name);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send an event unrelated to any session or job.
   * @param name event name/tag. Name is limited to 64 characters and cannot be empty.
   * @param extras the extra details associated with this event. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   */
  public void sendEvent(final String name, final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendEvent(name, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send an event related to the current session. This has no effect if the session has not been
   * started.
   * @param name event name/tag. Name is limited to 64 characters and cannot be empty.
   * @param extras the extra details associated with this event. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   */
  public void sendSessionEvent(final String name, final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendSessionEvent(name, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send an event related to a running job. This has no effect if no job is running for the
   * specified name.
   * @param eventName event name/tag. Name is limited to 64 characters and cannot be empty.
   * @param jobName job name.
   * @param extras the extra details associated with this event. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   */
  public void sendJobEvent(final String eventName, final String jobName, final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendJobEvent(eventName, jobName, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send an error unrelated to any session or job.
   * @param name error name/tag. Name is limited to 64 characters and cannot be empty.
   * @param extras the extra details associated with this error. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   */
  public void sendError(final String name, final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendError(name, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send an error related to the current session. This has no effect if the session has not been
   * started.
   * @param name error name/tag. Name is limited to 64 characters and cannot be empty.
   * @param extras the extra details associated with this error. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   */
  public void sendSessionError(final String name, final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendSessionError(name, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send an error related to a running job. This has no effect if no job is running for the
   * specified name.
   * @param eventName error name/tag.
   * @param jobName job name.
   * @param extras the extra details associated with this error. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   */
  public void sendJobError(final String eventName, final String jobName, final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendJobError(eventName, jobName, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send a message to an XMPP service.
   * @param msg a Bundle with the following structure, all the keys are optional except the "to"
   *          key.
   *          <ul>
   *          <li>"to" : (String) service's JID (required)</li>
   *          <li>"subject" : (String) message subject</li>
   *          <li>"thread" : (String) message thread</li>
   *          <li>"body" : (String) message body</li>
   *          <li>"type" : (String) message type like "chat" or "heading" or none</li>
   *          <li>"extensions" : (Bundle) A sub-bundle containing XMPP extensions, keys are
   *          namespaces and values are the corresponding XML strings.</li>
   *          </ul>
   */
  public void sendXMPPMessage(final Bundle msg)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendXMPPMessage(msg);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send a message to another device using Capptain. The device sending the message must be
   * connected to the network. The device receiving the message must have its Capptain service
   * running and be connected to the network. The message will be dropped otherwise.
   * @param deviceId device identifier as returned by {@link #getDeviceId(Callback)} on the device
   *          receiving the message.
   * @param payload message content as a String. For binaries you have to encode them (for example
   *          in Base64). Leading and trailing whitespace characters will be dropped.
   * @param packageName optional package name to send the message to a different application, pass
   *          <tt>null</tt> to target the same application. If the targeting application is not an
   *          Android one, the application ID must be passed instead of the package name. Don't pass
   *          application ID for Android applications.
   */
  public void sendMessageToDevice(final String deviceId, final String payload,
    final String packageName)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendMessageToDevice(deviceId, payload, packageName);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Get the identifier used by Capptain to identify this device.
   * @param callback a callback to retrieve the result.
   */
  public void getDeviceId(final Callback<String> callback)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          callback.onResult(mCapptainService.getDeviceId());
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send application specific information.
   * @param appInfo application information as a Bundle. Keys must match the
   *          <tt>^[a-zA-Z][a-zA-Z_0-9]*</tt> regular expression. Extras are encoded into JSON
   *          before being sent to the server, the encoded limit is 1024 characters.
   */
  public void sendAppInfo(final Bundle appInfo)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendAppInfo(appInfo);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Get information that the Capptain service sends.
   * @param callback a callback to retrieve the result.
   */
  public void getInfo(final Callback<Bundle> callback)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          callback.onResult(mCapptainService.getInfo());
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Send feedback to reach about a content.
   * @param kind content kind e.g. announcement, poll or datapush.
   * @param contentId content identifier.
   * @param status feedback status e.g. ok or cancelled.
   * @param extras extra information like poll answers.
   */
  public void sendReachFeedback(final String kind, final String contentId, final String status,
    final Bundle extras)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.sendReachFeedback(kind, contentId, status, extras);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Configure or reconfigure. Non <tt>null</tt> values override the settings set in the
   * <tt>AndroidManifest.xml</tt> file. The configuration change is immediate and can be cached by
   * the service (but you should not rely on it): you should submit configuration at least once per
   * application process lifetime. This call is not incremental: the configuration must contain all
   * the desired values every time this function is called. As a consequence, a <tt>null</tt> value
   * reset the corresponding setting to its default value and behavior. This means you can reset all
   * configuration by submitting an empty configuration object.
   * @param configuration full configuration.
   */
  public void configure(final CapptainConfiguration configuration)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.configure(configuration);
          mCapptainConfiguration = configuration;
          mCapptainConfigurationSentAsNull = configuration == null;
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Register this device for Native Push.
   * @param token native push token (describing registration identifier and service type).
   */
  public void registerNativePush(final CapptainNativePushToken token)
  {
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          mCapptainService.registerNativePush(token);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Check if the Capptain Push service has messages for the current device. This method is used
   * only to wake up the Android Capptain service. You don't need to call this function, it's used
   * only for integration with C2DM or when the persistent mode is used for Reach (in this mode we
   * start the service when the device boots).
   */
  public void checkIncomingMessages()
  {
    /*
     * Needed by Reach when the device boots or when C2DM wakes up the application. We bind to the
     * service to force it to connect, fetch and deliver messages to this application. We'll only
     * stay bound for 30s unless someone else use the API. If we declare the binder persistent in
     * AndroidManifest.xml, the service will stay connected.
     */
    sendCapptainCommand(new Runnable()
    {
      @Override
      public void run()
      {
        /* Nothing to do */
      }
    });
  }

  /**
   * Enable or disable the agent. The change is persistent. As an example you don't need to call
   * this function every time the application is launched to disable the agent.<br/>
   * You can also integrate this setting in a preference activity, the preference file name is by
   * default "capptain.agent" (with mode 0) but this can be configured using the following meta-data
   * tags under the application tag in the AndroidManifest.xml file:
   * 
   * <pre>
   * {@code <meta-data
   *   android:name="capptain:agent:settings:name"
   *   android:value="capptain.agent" />
   * <meta-data
   *   android:name="capptain:agent:settings:mode"
   *   android:value="0" />
   * }
   * </pre>
   * 
   * The key within the preference file is "capptain:enabled" and is a boolean. You can use a
   * section like the following one in your preference layout:
   * 
   * <pre>
   * {@code <CheckBoxPreference
   *   android:key="capptain:enabled"
   *   android:defaultValue="true"
   *   android:title="Use Capptain"
   *   android:summaryOn="Capptain is enabled."
   *   android:summaryOff="Capptain is disabled." />}
   * </pre>
   * @param enabled true to enable, false to disable.
   */
  public void setEnabled(boolean enabled)
  {
    mSettings.edit().putBoolean(ENABLED, enabled).commit();
  }

  /**
   * Check if the agent is enabled.
   * @return true if the agent is enabled, false otherwise.
   */
  public boolean isEnabled()
  {
    return mSettings.getBoolean(ENABLED, true);
  }
}
