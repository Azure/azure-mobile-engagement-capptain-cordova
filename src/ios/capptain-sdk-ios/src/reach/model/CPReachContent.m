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

#import "CPReachContent.h"
#import "CPReachModule.h"
#import "CapptainAgent.h"

@implementation CPReachContent

- (id)initWithElement:(CP_TBXMLElt*)root
{
  self = [super init];

  if (self != nil)
  {
    /* Content identifier */
    _contentId = [[CP_TBXML valueOfAttributeNamed:@"id" forElement:root] copy];

    /* Category */
    _category = [[CP_TBXML valueOfAttributeNamed:@"category" forElement:root] copy];

    /* Expiry */
    CP_TBXMLElt* expiry = [CP_TBXML childElementNamed:@"expiry" parentElement:root];
    if (expiry)
    {
      NSScanner* scanner = [NSScanner scannerWithString:[CP_TBXML textForElement:expiry]];

      long long expiryTimestamp;
      if ([scanner scanLongLong:&expiryTimestamp])
      {
        _expiryDate = [[NSDate dateWithTimeIntervalSince1970:(NSTimeInterval)(expiryTimestamp/1000L)] retain];
        _expiryLocaltz = [[CP_TBXML valueOfAttributeNamed:@"localtz" forElement:expiry] isEqualToString:@"true"];
      }
    }

    /* Body */
    _body = [[CP_TBXML textForElement:[CP_TBXML childElementNamed:@"body" parentElement:root]] copy];

    /* Required fields */
    if (!_contentId)
    {
      [self release];
      self = nil;
    }
  }
  return self;
}

- (void)dealloc
{
  [_contentId release];
  [_category release];
  [_body release];
  [_expiryDate release];
  [super dealloc];
}

- (BOOL)isExpired
{
  if (_expiryDate != nil)
  {
    /* Remove current timezone offset from expiry date to get the date in GMT */
    NSDate* expiryDate = [[_expiryDate copy] autorelease];
    if (_expiryLocaltz)
      expiryDate =
        [expiryDate dateByAddingTimeInterval:-[[NSTimeZone defaultTimeZone] secondsFromGMTForDate:expiryDate]];

    /* If expiry date has been reached, content is expired */
    if ([[expiryDate earlierDate:[NSDate date]] isEqualToDate:expiryDate])
      return YES;
  }

  return NO;
}

- (void)sendFeedback:(NSString*)status extras:(NSDictionary*)extras
{
  /* If feedback enabled */
  if (_feedback)
  {
    /* Build feedback dictionnary */
    NSMutableDictionary* feedback = [[NSMutableDictionary alloc] initWithCapacity:3];
    feedback[@"kind"] = [self kind];
    feedback[@"id"] = _contentId;
    feedback[@"status"] = status;

    if (extras)
      feedback[@"extras"] = extras;

    /* Reply */
    [[CapptainAgent shared] sendReachFeedback:feedback];

    /* Release payload */
    [feedback release];
  }
}

- (void)process:(NSString*)status extras:(NSDictionary*)extras
{
  if (!_processed)
  {
    /* Send feedback if any */
    if (status != nil)
      [self sendFeedback:status extras:extras];

    /* Mark content as processed */
    CPReachModule* module = (CPReachModule*)[[CapptainAgent shared] getModule:kCPReachModuleName];
    [module markContentProcessed:self];
    _processed = YES;
  }
}

- (NSData*)decodeBase64:(NSString*)str
{
  NSData* data = [NSData alloc];
  if ([data respondsToSelector:@selector(initWithBase64EncodedString:options:)])
    return [data initWithBase64EncodedString:str options:0];
  else
    return [data initWithBase64Encoding:str];
  return [data autorelease];
}

- (NSString*)kind
{
  [NSException raise:NSInternalInconsistencyException format:@"You must override %@ in a subclass",
   NSStringFromSelector(@selector(getContentTag))];
  return nil;
}

- (void)drop
{
  [self process:@"dropped" extras:nil];
}

- (void)actionContent
{
  [self process:@"content-actioned" extras:nil];
}

- (void)exitContent
{
  [self process:@"content-exited" extras:nil];
}

@end
