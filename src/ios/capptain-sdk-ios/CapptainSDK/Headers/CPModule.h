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
#import <UIKit/UIKit.h>

@class CPPushMessage;

/**
 * The `CPModule` protocol defines an interface for Capptain modules
 *
 * You can pass a module in the Capptain initialization method.
 *
 * **Related methods**
 *
 * - <[CapptainAgent registerApp:identifiedBy:modules:]>
 * - <[CapptainAgent getModule:]>
 */
@protocol CPModule<NSObject>

/**
 * Start module.
 * This method is called by the <CapptainAgent> when Capptain has been initialized.<br>
 * You can start using Capptain agent methods at this point.
 */
- (void)start;

/**
 * Stop module.
 * This method is called by the <CapptainAgent> when Capptain is going to be released.<br>
 * You should release uneeded resources in this method. You won't be able to call any methods from the <CapptainAgent>
 * after this point.
 */
- (void)stop;

/**
 * The unique name of the module
 */
- (NSString*)name;


@optional

/**
 * Called when one or several push messages are received by Capptain.
 * This does not include the push message if it has been received following application launch
 * (typically when the user clicks on the Apple Push notification of the notification center).
 * This message will be passed to the method <displayPushMessageNotification:> instead.
 *
 * @param msgs A list of <CPPushMessage> objects.
 * @see CPPushMessage
 */
- (void)pushMessagesReceived:(NSArray*)msgs;

/**
 * Called after the user clicks on an Apple notification handled by Capptain.
 * The module receive the Capptain push message associated to the Apple
 * notification in order to display it to the end-user.
 * @param msg The capptain push message to display.
 * @result YES if given message has been processed and displayed, NO otherwise.
 * @see CPPushMessage
 */
- (BOOL)displayPushMessageNotification:(CPPushMessage*)msg;

/**
 * Called when Capptain has detected that the current activity has changed.
 * @param activityName The name of the new activity.
 */
- (void)activityChanged:(NSString*)activityName;

/**
 * Called when an Apple push notification is received.
 * @param notification The notification payload.
 */
- (void)pushNotificationReceived:(NSDictionary*)notification;

/**
 * Called when the deviceid or the appid has changed.
 * You should use this method to clear the storage, for example.
 * Careful : The module is not started yet.
 */
- (void)configurationChanged;

@end
