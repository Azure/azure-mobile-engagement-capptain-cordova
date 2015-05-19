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

@protocol CPWebAnnouncementActionDelegate;

/**
 *
 * Javascript bridge used by web announcements.
 *
 * Create a bridge between javascript methods embed inside a web announcement and objective c methods
 * defined in <CPWebAnnouncementActionDelegate>.<br>
 * The mapping is as follow:
 *  - *capptainReachContent.actionContent()* is mapped to <[CPWebAnnouncementActionDelegate action]>
 *  - *capptainReachContent.exitContent()* is mapped to <[CPWebAnnouncementActionDelegate exit]>
 *
 * This class must be used in association with a `UIWebView`:
 *
 *  [webView setDelegate:[CPWebAnnouncementJsBridge jsBridgeWithDelegate:self]];
 *
 */
@interface CPWebAnnouncementJsBridge : NSObject<UIWebViewDelegate>
{
  @private
  id<CPWebAnnouncementActionDelegate> _delegate;
}

/**
 * Create a bridge between Javascript functions and Objective C methods.<br>
 * Used the returned object as a delegate to an existing `UIWebView`.
 * @param delegate The delegate that will receive reach actions each time a recognized Javascript function is called.
 */
+ (id)jsBridgeWithDelegate:(id<CPWebAnnouncementActionDelegate>)delegate;

@end

/**
 * The `CPWebAnnouncementActionDelegate` protocol defines the methods a delegate of a <CPWebAnnouncementJsBridge> object
 * should implement.
 *
 * Each time a recognized Javascript method is called, the corresponding delegate method will be called.
 * See methods definition for the list of recognized actions.
 */
@protocol CPWebAnnouncementActionDelegate <NSObject>

/**
 * Mapped to the javascript function *capptainReachContent.actionContent()*<br>
 * The announcement has been actioned.
 */
- (void)action;

/**
 * Mapped to the javascript function *capptainReachContent.exitContent()*<br>
 * The announcement has been exited.
 */
- (void)exit;

@end
