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
 * A `CPPushMessage` object represents a Capptain push message.
 *
 * Capptain push messages are passed through Capptain agent to modules and Capptain push delegate.
 *
 * **See also**
 *
 * - <[CapptainAgent setPushDelegate:]>
 * - <CPPushDelegate>
 * - <CPModule>
 */
@interface CPPushMessage : NSObject<NSCoding>

/** Message's identifier */
@property(nonatomic, copy) NSString* messageId;

/** Message's payload */
@property(nonatomic, copy) NSString* payload;

/** optional XMPP address to reply to (can be `nil`). */
@property(nonatomic, copy) NSString* replyTo;

/** YES if this message was retrieved from the cache, NO otherwise */
@property(nonatomic, assign) BOOL cached;

@end
