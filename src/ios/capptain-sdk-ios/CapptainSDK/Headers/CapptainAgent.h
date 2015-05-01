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

#import <Foundation/Foundation.h>

extern NSString* const kCapptainAgentEnabled;

@protocol CPPushDelegate;
@protocol CPModule;

/**
 * # The main Capptain class #
 *
 * Initialize the agent when your application starts:
 *
 *     - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
 *     {
 *        [...]
 *        [CapptainAgent registerApp:@"YOUR_APPID" identifiedBy:@"YOUR_SDK_KEY"];
 *        [...]
 *     }
 */
@interface CapptainAgent : NSObject

/**
 * ---------------------------
 * @name Initializing Capptain
 * ---------------------------
 */

/**
 * This information is used so that the backend can recognized your application.
 * Call this method when the application starts.
 * @param appid The application identifier that you can find in the Capptain
 * frontend.
 * @param sdkKey The SDK key provided with your Capptain application.
 */
+ (void)registerApp:(NSString*)appid identifiedBy:(NSString*)sdkKey;

/**
 * Register application with the given appid and SDK key associated to the
 * application.
 * Also register optional Capptain modules (See module documentation for more
 * information).
 * @param appid The application identifier that you can find in the Capptain
 * frontend.
 * @param sdkKey The SDK key provided with your Capptain application.
 * @param firstModule Optional Capptain module.
 * @param ... A comma-separated list of Capptain modules.
 */
+ (void)registerApp:(NSString*)appid identifiedBy:(NSString*)sdkKey modules:(id<CPModule>)firstModule, ...;

/**
 * ---------------------
 * @name General options
 * ---------------------
 */

/**
 * Capptain agent can be configured to produce test logs.
 * @param value Set to YES to enable test logs, NO otherwise.
 */
+ (void)setTestLogEnabled:(BOOL)value;

/**
 * Enable or disable the agent. The change is persistent. As an example you
 * don't need to call
 * this function every time the application is launched to disable the agent.
 *
 * You can also integrate this setting in your `Settings.bundle` file using the
 * key 'capptain_agent_enabled'
 * (also available as a constant string: `kCapptainAgentEnabled`).
 *
 * @param enabled Set to YES to enable the agent, NO otherwise.
 * @see enabled
 */
- (void)setEnabled:(BOOL)enabled;

/**
 * Set the delay between each burst of analytics reported to the backend.
 * Passing a value equal to
 * or below 0 means that analytics are reported in real time. Default value is
 * 0.
 * @param threshold Delay in seconds between each analytic burst.
 */
- (void)setBurstThreshold:(double)threshold;

/**
 * By default, Capptain will report your application crashes. If you want to
 * disable crash reports, you can
 * use this function.
 * @attention If you disable crash reports, the Capptain session will not be
 * closed when the application gets killed
 * abruptly.
 * @param enabled Set to NO to disable crash reports, YES otherwise (default
 * value).
 */
- (void)setCrashReport:(BOOL)enabled;

/**
 * ----------------------
 * @name Location options
 * ----------------------
 */

/**
 * Enable lazy area reports. This will only reports the geographic location at a
 * city level.
 * @param enabled YES to enable location reporting, NO to disable (default
 * value).
 */
- (void)setLazyAreaLocationReport:(BOOL)enabled;

/**
 * Set real-time location reports. This will report location using a low level
 * of accuracy to avoid
 * draining the battery.
 * @param enabled YES to enable real-time location reporting, NO to disable
 *(default value).
 */
- (void)setRealtimeLocationReport:(BOOL)enabled;

/**
 * Enable location reports using the highest-level of accuracy.
 * Real-time location reporting is mandatory to enable this option.
 * @param enabled YES to enable fine location reporting, NO to disable (default
 * value).
 * @see setRealtimeLocationReport:
 */
- (void)setFineRealtimeLocationReport:(BOOL)enabled;

/**
 * Enable location reports even when the application is in background. Enabling
 * this option will also enable fine
 * location reports.
 * Real-time location reporting is mandatory to enable this option.
 * @warning If you enable this option, the system will automatically relaunch
 * the application into the background if a
 * new location arrives.
 * @param enabled YES to enable background location reporting, NO to disable
 *(default value).
 * @param launchOptions options received when application is launched
 * (from `- (BOOL)application:(UIApplication *)application
 * didFinishLaunchingWithOptions:(NSDictionary
 *******************************)launchOptions`). Just pass `nil` if you want to disable this feature.
 * @see setRealtimeLocationReport:
 */
- (void)setBackgroundRealtimeLocationReport:(BOOL)enabled withLaunchOptions:(NSDictionary*)launchOptions;

/**
 * -----------------------------------------------
 * @name Accessing Capptain agent once initialized
 * -----------------------------------------------
 */

/**
 * Returns the singleton instance of the Capptain agent.
 */
+ (CapptainAgent*)shared;

/**
 * -------------------------------------------
 * @name Reporting your application statistics
 * -------------------------------------------
 */

/**
 * Report the current activity. A session is broken down into a sequence of
 * activities, this call attach the current
 * activity to the current session. If there is currently no session, this call
 * also starts a new session and the activity will be attached to the newly
 * created session.
 * @param activityName The name of the current activity within the session, can
 * be nil for
 * default name (but not empty). Name is limited to 64 characters.
 * @param extras The extra details associated with the activity. Keys must match
 * the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)startActivity:(NSString*)activityName extras:(NSDictionary*)extras;

/**
 * Report that the current activity ended. This will close the session.
 */
- (void)endActivity;

/**
 * Start a job.
 * @param name Job name, this should be unique, e.g. two jobs with the same
 * name can't run at the same time, subsequent requests with the same
 * name will end the previous job before starting the new one.
 * Name is limited to 64 characters and cannot be empty.
 * @param extras The extra details associated with this job. Keys must match the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)startJob:(NSString*)name extras:(NSDictionary*)extras;

/**
 * End a job.
 * @param name The name of job that has been started with
 * startJob:extras:
 */
- (void)endJob:(NSString*)name;

/**
 * Send an event to the backend.
 * @param name Event name/tag. Name is limited to 64 characters and cannot be
 * empty.
 * @param extras The extra details associated with this event. Keys must match
 * the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)sendEvent:(NSString*)name extras:(NSDictionary*)extras;

/**
 * Send an event related to the current session. This has no effect if the
 * session has not been started.
 * @param name Event name/tag. Name is limited to 64 characters and cannot be
 * empty.
 * @param extras The extra details associated with this event. Keys must match
 * the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)sendSessionEvent:(NSString*)name extras:(NSDictionary*)extras;

/**
 * Send an event related to a running job. This has no effect if no job is
 * running for the specified name.
 * @param name Event name/tag. Name is limited to 64 characters and cannot be
 * empty.
 * @param jobName Job name.
 * @param extras The extra details associated with this event. Keys must match
 * the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)sendJobEvent:(NSString*)name jobName:(NSString*)jobName extras:(NSDictionary*)extras;

/**
 * Send an error to the backend.
 * @param name Error name/tag. Name is limited to 64 characters and cannot be
 * empty.
 * @param extras The extra details associated with this error. Keys must match
 * the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)sendError:(NSString*)name extras:(NSDictionary*)extras;

/**
 * Send an error related to the current session. This has no effect if the
 * session has not been started.
 * @param name Error name/tag. Name is limited to 64 characters and cannot be
 * empty.
 * @param extras The extra details associated with this error. Keys must match
 * the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)sendSessionError:(NSString*)name extras:(NSDictionary*)extras;

/**
 * Send an error related to a running job. This has no effect if no job is
 * running for the specified name.
 * @param name Error name/tag. Name is limited to 64 characters and cannot be
 * empty.
 * @param jobName Job name.
 * @param extras The extra details associated with this error. Keys must match
 * the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)sendJobError:(NSString*)name jobName:(NSString*)jobName extras:(NSDictionary*)extras;

/**
 * ------------------------------------------------
 * @name Reporting specific application information
 * ------------------------------------------------
 */

/**
 * Send application specific information.
 * @param info Application information as a dictionary. Keys must match the
 * `^[a-zA-Z][a-zA-Z_0-9]*` regular expression. Extras are encoded into JSON
 * before being sent to the server, the encoded limit is 1024 characters.
 */
- (void)sendAppInfo:(NSDictionary*)info;

/**
 * ----------------------
 * @name Sending messages
 * ----------------------
 */

/**
 * Send a message to an XMPP service.
 * @param message A dictionary with the following structure, all the keys are
 * optional except the "to"
 *        key.
 * - "to" :         (NSString) service's JID (required)
 * - "subject" :    (NSString) message subject
 * - "thread" :     (NSString) message thread
 * - "body" :       (NSString) message body
 * - "type" :       (NSString) message type like "chat" or "heading" or none
 * - "extensions" : (NSDictionnary) A sub-dictionnary containing XMPP
 * extensions, keys are
 *                  namespaces and values are the corresponding XML strings.
 */
- (void)sendXMPPMessage:(NSDictionary*)message;

/**
 * Send a message to another device using Capptain. The device sending the
 * message but be
 * connected to the network. This function will work only if both devices are
 * connected, the message will be dropped
 * otherwise.
 * @param deviceId Device identifier as returned by the method <deviceId> on the
 * device
 *        receiving the message.
 * @param payload Message content as a String. For binaries you have to encode
 * them (for example
 *        in hexadecimal or in Base64). Leading and trailing whitespace
 * characters will be
 *        dropped.
 * @param application Optional parameter to send the message to a different
 * application, pass
 *        `nil` to target the same application. <br>
 *          - If the targeted application is an Android one, its package name
 * MUST be used.
 *          - For other application types, the Capptain application ID MUST be
 * used..
 */
- (void)sendMessage:(NSString*)payload toDevice:(NSString*)deviceId app:(NSString*)application;

/**
 * Send a feedback to reach. You don't need to call this function directly, it's
 * used only by the optional reach module
 * to report feedback from announcements, polls and data pushes.
 * @param feedback A dictionary containing the feedback payload to send.
 */
- (void)sendReachFeedback:(NSDictionary*)feedback;

/**
 * ------------------------------------
 * @name Listening to incoming messages
 * ------------------------------------
 */

/**
 * Register a push delegate.
 * The given delegate will be notified when Capptain receive push messages.
 * @param delegate The delegate that will handle push messages.
 */
- (void)setPushDelegate:(id<CPPushDelegate>)delegate;

/**
 * ---------------------------------
 * @name Getting Capptain agent data
 * ---------------------------------
 */

/**
 * Get the identifier used by Capptain to identify this device.
 * @result The identifier used by Capptain to identify this device.
 */
- (NSString*)deviceId;

/**
 * Get a previously registered module.
 * @param moduleName the name of the module to retrieve.
 * @result A Capptain module or nil if there is no module with that name.
 */
- (id<CPModule>)getModule:(NSString*)moduleName;

/**
 * Check if the agent is enabled.
 * @result YES if the agent is enabled, NO otherwise.
 * @see setEnabled:
 */
- (BOOL)enabled;

/**
 * --------------------------------
 * @name Apple push related methods
 * --------------------------------
 */

/**
 * Register the device token returned by Apple servers.
 * This method is necessary to receive Apple push notifications from Capptain
 * Push Service.
 * @param token Data as returned by the application delegate callback:
 * - application:didRegisterForRemoteNotificationsWithDeviceToken:
 */
- (void)registerDeviceToken:(NSData*)token;

/**
 * If you are using the Capptain Push Service or Reach module,
 * you should call this function from the application delegate:
 * - application:didReceiveRemoteNotification:
 * @param userInfo A dictionary that contains information related to the remote
 * notification
 */
- (void)applicationDidReceiveRemoteNotification:(NSDictionary*)userInfo;

@end
