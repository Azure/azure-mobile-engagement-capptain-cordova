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

#import "CapptainTableViewController.h"
#import "CapptainAgent.h"
#import "CapptainUtils.h"

@implementation CapptainTableViewController

- (void)viewDidAppear:(BOOL)animated
{
  [super viewDidAppear:animated];
  [[CapptainAgent shared] startActivity:[self capptainActivityName] extras:[self capptainActivityExtra]];
}

- (NSString*)capptainActivityName
{
  return [CapptainUtils buildCapptainActivityName:[self class]];
}

- (NSDictionary*)capptainActivityExtra
{
  return nil;
}

@end
