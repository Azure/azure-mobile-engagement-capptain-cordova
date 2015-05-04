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

#import "CPReachPollQuestion.h"

@implementation CPReachPollQuestion

- (id)initWithId:(NSString*)qId title:(NSString*)title choices:(NSArray*)choices
{
  self = [super init];
  if (self != nil)
  {
    _questionId = [qId copy];
    _title = [title copy];
    _choices = [choices copy];
  }
  return self;
}

- (void)dealloc
{
  [_questionId release];
  [_title release];
  [_choices release];
  [super dealloc];
}

@end

@implementation CPReachPollChoice

- (id)initWithId:(NSString*)cId title:(NSString*)title isDefault:(BOOL)isDefault
{
  self = [super init];
  if (self != nil)
  {
    _choiceId = [cId copy];
    _title = [title copy];
    _isDefault = isDefault;
  }
  return self;
}

- (void)dealloc
{
  [_choiceId release];
  [_title release];
  [super dealloc];
}

@end
