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
#import "CPAnnouncementViewController.h"
#import "CPWebAnnouncementJsBridge.h"

@class CPAnnouncementViewController;

/**
 * Default implementation of CPAnnouncementViewController.
 *
 * This view controller display an announcement using layout view named `CPDefaultAnnouncementView.xib`.<br>
 * You can change the xib file to your needs as long as you keep view identifier and types.
 *
 * This class is using the Javascript bridge CPWebAnnouncementJsBridge to perform actions when
 * a recognized Javascript function is called inside a web announcement.
 */
@interface CPDefaultAnnouncementViewController : CPAnnouncementViewController<CPWebAnnouncementActionDelegate>

/** Navigation bar displaying announcement's title */
@property(nonatomic, retain) IBOutlet UINavigationBar* titleBar;

/** Text announcement's content goes in this view. */
@property(nonatomic, retain) IBOutlet UITextView* textView;

/** Web announcement's content goes in this view. */
@property(nonatomic, retain) IBOutlet UIWebView* webView;

/** Toolbar containing action and exit buttons */
@property(nonatomic, retain) IBOutlet UIToolbar* toolbar;

/**
 * Javascript bridge
 */
@property(nonatomic, retain) CPWebAnnouncementJsBridge* jsBridge;

@end
