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

/**
 * The `CPReachDataPushDelegate` protocol defines methods a delegate of <CPReachModule> should implement to receive data
 * pushes.
 *
 * To process data push, you must implement method <onDataPushBase64ReceivedWithDecodedBody:andEncodedBody:>
 * if it's a file upload or a base64 data, otherwise you implement <onDataPushStringReceived:>.<br>
 * To use it in your application just call the method <[CPReachModule dataPushDelegate]> after module
 * initialization.
 */
@protocol CPReachDataPushDelegate <UIAlertViewDelegate>

@optional

/**
 * This function is called when a datapush of type text has been received.
 * @param category short string describing your data to push
 * @param body Your content.
 * @result YES to acknowledge the content, NO to cancel.
 **/
- (BOOL)didReceiveStringDataPushWithCategory:(NSString*)category body:(NSString*)body;

/**
 * This function is called when a datapush of type base64 has been received.
 * @param category short string describing your data to push
 * @param decodedBody Your base64 content decoded.
 * @param encodedBody Your base64 content encoded.
 * @result YES to acknowledge the content, NO to cancel.
 **/
- (BOOL)didReceiveBase64DataPushWithCategory:(NSString*)category decodedBody:(NSData*)decodedBody encodedBody:(NSString*)
  encodedBody;

@end
