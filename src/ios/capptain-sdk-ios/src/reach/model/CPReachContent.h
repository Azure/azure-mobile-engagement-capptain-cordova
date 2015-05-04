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
#import "CP_TBXML.h"

/**
 * Abstract class for reach contents such as announcements and polls.
 */
@interface CPReachContent : NSObject
{
  @private
  NSString* _contentId;
  NSUInteger _localId;
  NSString* _category;
  NSString* _body;
  BOOL _processed;
  BOOL _feedback;
  NSDate* _expiryDate;
  BOOL _expiryLocaltz;
}

/**
 * Parse a reach content from a given xml element
 * @param root Parsed XML root DOM element
 * @result The parsed reach content or nil if content couldn't be parsed.
 */
- (id)initWithElement:(CP_TBXMLElt*)root;

/** The unique reach content identifier */
@property(readonly) NSString* contentId;

/** Local storage identifier */
@property(assign) NSUInteger localId;

/**
 * Category of this content. You usually don't need to read this value by yourself.
 * Instead, you should use the method <[CPReachModule registerAnnouncementController:forCategory:]>
 * or <[CPReachModule registerPollController:forCategory:]> to tell the reach module
 * which controller to display for a given category.
 */
@property(readonly) NSString* category;

/** Reach content's body */
@property(nonatomic, copy) NSString* body;

/** Feedback required ? */
@property BOOL feedback;

/** Drop content. */
- (void)drop;

/** Report content has been actioned. */
- (void)actionContent;

/** Report content has been exited.  */
- (void)exitContent;

/** @result YES if content is expired and should be dropped, NO otherwise */
- (BOOL)isExpired;

/**
 * Utility method to decode base64 data.
 * @param str The string to decode.
 */
- (NSData*)decodeBase64:(NSString*)str;

/**
 * Send feedback to reach about this content.
 * @param status The feedback status.
 * @param extras Extra information like poll answers.
 */
- (void)sendFeedback:(NSString*)status extras:(NSDictionary*)extras;

/**
 * Send content reply to the service that sent it, after that new contents can be notified.
 * @param status The feedback status.
 * @param extras Extra information like poll answers.
 */
- (void)process:(NSString*)status extras:(NSDictionary*)extras;

@end
