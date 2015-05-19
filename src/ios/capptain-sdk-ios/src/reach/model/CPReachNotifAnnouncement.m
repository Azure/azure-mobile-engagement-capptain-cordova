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

#import "CPReachNotifAnnouncement.h"
#import "CPReachModule.h"
#import "CapptainAgent.h"

#define forbidden() NSLog(@"[Capptain][ERROR] Unsupported operation '%@': This is a notification only announcement.", \
                          NSStringFromSelector(_cmd))

@implementation CPReachNotifAnnouncement

+ (id)notifAnnouncementWithElement:(CP_TBXMLElt*)element params:(NSDictionary*)params
{
  return [[[self alloc] initWithElement:element params:params] autorelease];
}

- (NSString*)kind
{
  return @"notifAnnouncement";
}

- (void)actionNotification:(BOOL)launchAction
{
  [super actionNotification:launchAction];

  /* This is the final step in this content kind */
  [self process:nil extras:nil];
}

- (void)actionContent
{
  forbidden();
}

- (void)exitContent
{
  forbidden();
}

@end
