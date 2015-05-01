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

#import "CPIdfaProvider.h"
#import <AdSupport/AdSupport.h>

@implementation CPIdfaProvider

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Singleton method
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

+ (id)shared
{
  static dispatch_once_t once;
  static id sharedInstance;
  dispatch_once(&once, ^{
    sharedInstance = [[self alloc] init];
  });
  return sharedInstance;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Public methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (NSNumber*)isIdfaEnabled
{
#ifndef CAPPTAIN_DISABLE_IDFA
  ASIdentifierManager* sharedManager = [self ASIdentifierManager];
  if (sharedManager)
  {
    return [NSNumber numberWithBool:[sharedManager isAdvertisingTrackingEnabled]];
  }
#endif
  return nil;
}

- (NSString*)idfa
{
#ifndef CAPPTAIN_DISABLE_IDFA
  ASIdentifierManager* sharedManager = [self ASIdentifierManager];
  if (sharedManager)
  {
    if ([sharedManager isAdvertisingTrackingEnabled])
    {
      return [[sharedManager advertisingIdentifier] UUIDString];
    }
  }
#endif
  return nil;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Private methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#ifndef CAPPTAIN_DISABLE_IDFA
- (ASIdentifierManager*)ASIdentifierManager
{
  Class ASIdentifierManagerClass = NSClassFromString(@"ASIdentifierManager");
  if (ASIdentifierManagerClass)
  {
    return [ASIdentifierManagerClass sharedManager];
  }
  return nil;
}

#endif

@end
