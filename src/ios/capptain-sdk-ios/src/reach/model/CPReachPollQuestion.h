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
 * The `CPReachPollQuestion` class defines objects that represent poll's questions
 */
@interface CPReachPollQuestion : NSObject {
  @private
  NSString* _questionId;
  NSString* _title;
  NSArray* _choices;
}

/**
 * Create and return a poll's question.
 * @param qId The unique identifier for the quetion.
 * @param title The question's title.
 * @param choices The question's choices.
 */
- (id)initWithId:(NSString*)qId title:(NSString*)title choices:(NSArray*)choices;

/** The unique question identifier */
@property(readonly) NSString* questionId;

/** Localized question text */
@property(readonly) NSString* title;

/**
 * Choices.<br>
 * Contains <CPReachPollChoice> objects
 */
@property(readonly) NSArray* choices;

@end

/**
 * The `CPReachPollQuestion` class defines objects that represent poll's choices
 */
@interface CPReachPollChoice : NSObject {
  @private
  NSString* _choiceId;
  NSString* _title;
  BOOL _isDefault;
}

/**
 * Create and return a question's choice.
 * @param cId The unique identifier for the choice.
 * @param title The choice's title.
 * @param isDefault Is this the default choice for the associated question.
 */
- (id)initWithId:(NSString*)cId title:(NSString*)title isDefault:(BOOL)isDefault;

/** The unique choice identifier */
@property(readonly) NSString* choiceId;

/** The localized choice text */
@property(readonly) NSString* title;

/** YES if this choice is the default one for the associated question. */
@property(readonly) BOOL isDefault;
@end
