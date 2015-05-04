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

#import "CPContentViewController.h"

@class CPReachPoll;

/**
 * Abstract view controller displaying a Capptain poll.
 *
 * By inheriting from this class you can create your own view controller to display polls.
 */
@interface CPPollViewController : CPContentViewController

/**
 * Init the view controller with the given poll.
 * Subclasses should re-implement this method.
 * @param poll Poll to display
 */
- (instancetype)initWithPoll:(CPReachPoll*)poll;

/**
 * Submit answers for the associated poll and dismiss this view controller.<br>
 * Dictionary keys must be the question ids and values must be the associated choice ids.
 * Should be called by subclasses when the user clicks on the 'action' button associated to
 * the poll.
 * @param answers The poll answers to submit.
 */
- (void)submitAnswers:(NSDictionary*)answers;

/**
 * Use this property to store poll information when the initWithPoll:
 * method is called.
 * Subclasses should also read this property to init their subviews.
 */
@property(nonatomic, retain) CPReachPoll* poll;

@end
