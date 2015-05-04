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

#import "CPAnnouncementViewController.h"
#import "CPViewControllerUtil.h"

@implementation CPAnnouncementViewController

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Memory management
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (instancetype)initWithAnnouncement:(CPReachAnnouncement*)anAnnouncement
{
  self = [super init];
  if (self != nil)
  {
    self.announcement = [anAnnouncement retain];
  }
  return self;
}

- (void)dealloc
{
  [self.announcement exitContent];
  self.announcement = nil;
  [super dealloc];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Abstract method implementation
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (CPInteractiveContent*)content
{
  return self.announcement;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Actions
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)action
{
  /* If actionUrl property is defined open corresponding URL */
  NSString* actionUrl = self.announcement.actionURL;
  if (actionUrl != nil)
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:actionUrl]];

  /* Acknowledge announcement and dismiss this controller */
  [self.announcement actionContent];
  [CPViewControllerUtil dismissViewController:self animated:YES];
}

@end
