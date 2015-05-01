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
#import "CPContentViewController.h"
#import "CPReachAnnouncement.h"

@class CPReachAnnouncement;

/**
 * Abstract view controller displaying a Capptain announcement.
 *
 * By inheriting from this class you can create your own view controller to display announcements.
 * If you plan to display Web announcements using this controller, make sure to use an object of type
 * CPWebAnnouncementJsBridge
 * as a delegate to your `UIWebView`.
 */
@interface CPAnnouncementViewController : CPContentViewController

/**
 * Init the view controller with the given announcement.
 * Subclasses should re-implement this method.
 * @param anAnnouncement Announcement to display
 */
- (instancetype)initWithAnnouncement:(CPReachAnnouncement*)anAnnouncement;

/**
 * Report the announcement as actioned and dismiss this view controller.
 * Should be called by subclasses when the user clicks on the 'action' button associated to
 * the announcement.
 */
- (void)action;

/**
 * Use this property to store announcement information when the <initWithAnnouncement:>
 * method is called.
 * Subclasses should also read this property to init their subviews.
 */
@property(nonatomic, retain) CPReachAnnouncement* announcement;

@end
