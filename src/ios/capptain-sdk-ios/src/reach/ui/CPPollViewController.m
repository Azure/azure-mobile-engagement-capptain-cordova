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

#import "CPPollViewController.h"
#import "CPViewControllerUtil.h"
#import "CPReachPoll.h"

@implementation CPPollViewController

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Memory management
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (instancetype)initWithPoll:(CPReachPoll*)reachPoll
{
  self = [super initWithNibName:@"DefaultPollView" bundle:nil];
  if (self != nil)
  {
    self.poll = [reachPoll retain];
  }
  return self;
}

- (void)dealloc
{
  [self.poll exitContent];
  self.poll = nil;
  [super dealloc];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Actions
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)submitAnswers:(NSDictionary*)answers
{
  for (NSString* questionId in[answers allKeys])
    [self.poll fillAnswerWithQuestionId:questionId choiceId:answers[questionId]];

  [self.poll actionContent];
  [CPViewControllerUtil dismissViewController:self animated:YES];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Abstract method implementation
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (CPInteractiveContent*)content
{
  return self.poll;
}

@end
