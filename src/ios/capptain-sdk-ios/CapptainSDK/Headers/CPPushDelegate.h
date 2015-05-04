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
 * The `CPPushDelegate` protocol defines the messages sent to a delegate when a push message or a message from another
 * device is received.
 *
 * This delegate can be given to the Capptain agent
 * if you want to be notified of push messages coming from other devices or from the Capptain Push Service.
 *
 * **Related method**
 *
 * - <[CapptainAgent setPushDelegate:]>
 */
@protocol CPPushDelegate<NSObject>
@optional

/**
 * ----------------------------------------
 * @name Messages coming from other devices
 * ----------------------------------------
 */

/**
 * Sent when a message from another device has been received.
 * @param payload Message payload.
 * @param deviceId Device identifier who sent the message.
 */
- (void)didReceiveMessage:(NSString*)payload from:(NSString*)deviceId;

/**
 * ----------------------------------------------------
 * @name Messages coming from the Capptain Push Service
 * ----------------------------------------------------
 */

/**
 * Sent when a push message is received by the Capptain Push Service.
 * @param message An object representing the Capptain message.
 */
- (void)didReceiveMessage:(CPPushMessage*)message;

/**
 * Sent when Capptain is about to retrieve the push message that launched the application (from an Apple push
 * notification).
 * It is a good opportunity to start displaying a message to the end user indicating that data is being loaded.
 */
- (void)willRetrieveLaunchMessage;

/**
 * Sent when Capptain failed to retrieve the push message that launched the application.
 * Use this opportunity to hide any loading message and to display a dialog to the end user
 * indicating that the message could not be fetched.
 */
- (void)didFailToRetrieveLaunchMessage;

/**
 * Sent when Capptain received the push message that launched the application.
 * Use this opportunity to to hide any loading message and display appropriate content to the end user.
 * @param launchMessage An object representing the Capptain message that launched the application.
 */
- (void)didReceiveLaunchMessage:(CPPushMessage*)launchMessage;

@end
