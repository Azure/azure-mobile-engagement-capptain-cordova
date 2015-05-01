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

#import "CPReachPoll.h"
#import "CPReachPollQuestion.h"

@implementation CPReachPoll

- (id)initWithElement:(CP_TBXMLElt*)root
{
  self = [super initWithElement:root];
  if (self != nil)
  {
    NSMutableArray* questions = [[NSMutableArray alloc] init];

    /* Scan question elements */
    CP_TBXMLElt* questionElt = [CP_TBXML childElementNamed:@"question" parentElement:root];
    while (questionElt)
    {
      /* Parse identifier and title for this question */
      NSString* questionId = [CP_TBXML valueOfAttributeNamed:@"id" forElement:questionElt];
      NSString* questionTitle =
        [CP_TBXML textForElement:[CP_TBXML childElementNamed:@"title" parentElement:questionElt]];
      NSMutableArray* choices = [[NSMutableArray alloc] init];

      /* Scan choice elements */
      CP_TBXMLElt* choiceElt = [CP_TBXML childElementNamed:@"choice" parentElement:questionElt];
      while (choiceElt)
      {
        /* Parse identifier and title for this choice */
        NSString* choiceId = [CP_TBXML valueOfAttributeNamed:@"id" forElement:choiceElt];
        NSString* choiceTitle = [CP_TBXML textForElement:choiceElt];
        BOOL isDefault = [[CP_TBXML valueOfAttributeNamed:@"default" forElement:choiceElt] isEqualToString:@"true"];

        /* Create choice */
        CPReachPollChoice* choice =
          [[CPReachPollChoice alloc] initWithId:choiceId title:choiceTitle isDefault:isDefault];
        [choices addObject:choice];
        [choice release];

        /* Next choice */
        choiceElt = [CP_TBXML nextSiblingNamed:@"choice" searchFromElement:choiceElt];
      }

      /* Create question */
      CPReachPollQuestion* question =
        [[CPReachPollQuestion alloc] initWithId:questionId title:questionTitle choices:choices];
      [questions addObject:question];
      [question release];
      [choices release];

      /* Next question */
      questionElt = [CP_TBXML nextSiblingNamed:@"question" searchFromElement:questionElt];
    }

    /* Keep questions */
    _questions = [[NSArray alloc] initWithArray:questions];
    [questions release];

    /* Initialize answer dictionary */
    _answers = [[NSMutableDictionary alloc] init];
  }
  return self;
}

+ (id)pollWithElement:(CP_TBXMLElt*)element
{
  return [[[self alloc] initWithElement:element] autorelease];
}

- (void)fillAnswerWithQuestionId:(NSString*)qid choiceId:(NSString*)cid
{
  _answers[qid] = cid;
}

- (void)actionContent
{
  [self process:@"content-actioned" extras:_answers];
}

- (NSString*)kind
{
  return @"poll";
}

- (void)dealloc
{
  [_questions release];
  [_answers release];
  [super dealloc];
}

@end
