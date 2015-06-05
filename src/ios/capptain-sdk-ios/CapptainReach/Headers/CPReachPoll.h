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
#import "CPInteractiveContent.h"

/**
 * The `CPReachPoll` class defines objects that represent generic Capptain poll.
 *
 * You usually have to use this class when you implement your own poll
 * view controller.
 *
 * **See also**
 *
 * - <CPPollViewController>
 */
@interface CPReachPoll : CPInteractiveContent {
  @private
  NSArray* _questions;
  NSMutableDictionary* _answers;
}

/**
 * Poll questions.<br>
 * Contains <CPReachPollQuestion> objects.
 */
@property(readonly) NSArray* questions;

/**
 * Parse a poll
 * @param element Parsed XML root DOM element.
 * @result A new poll or nil if it couldn't be parsed.
 */
+ (id)pollWithElement:(CP_TBXMLElt*)element;

/**
 * Fill answer for a given question. Answers are sent when calling <[CPReachContent actionContent]>.
 * @param qid Question id as specified in <[CPReachPollQuestion questionId]>.
 * @param cid Choice id as specified in <[CPReachPollChoice choiceId]>.
 * @see questions
 */
- (void)fillAnswerWithQuestionId:(NSString*)qid choiceId:(NSString*)cid;

@end
