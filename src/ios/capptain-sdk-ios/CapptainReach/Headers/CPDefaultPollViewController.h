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
#import <UIKit/UIKit.h>
#import "CPPollViewController.h"

@class CPReachPoll;


/**
 * Default implementation of <CPPollViewController>.
 *
 * This view controller display a poll using layout view named `CPDefaultPollView.xib`.<br>
 * You can change the xib file to your needs as long as you keep view identifier and types.
 */
@interface CPDefaultPollViewController : CPPollViewController {
  @private
  BOOL _hasBody;
  NSMutableDictionary* _selectedChoices;
}

/** Submit's button */
@property(nonatomic, retain) UIBarButtonItem* submitButton;

/** Navigation bar displaying poll's title */
@property(nonatomic, retain) IBOutlet UINavigationBar* titleBar;

/** Table view responsible for displaying questions and choices */
@property(nonatomic, retain) IBOutlet UITableView* tableView;

/** Toolbar containing action and exit buttons */
@property(nonatomic, retain) IBOutlet UIToolbar* toolbar;

@end
