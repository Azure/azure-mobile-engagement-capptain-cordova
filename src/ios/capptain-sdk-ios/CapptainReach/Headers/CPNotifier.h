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
 * Custom notifier specification.
 *
 * You can define how a content notification is done for a set of categories by implementing this
 * protocol and registering your instances by calling <[CPReachModule registerNotifier:forCategory:]><br/>
 * It is recommended to extend the default implementation: <CPDefaultNotifier> which
 * performs most of the work and has convenient callbacks.
 */
@protocol CPNotifier <NSObject>

@required

/**
 * Handle a notification for a content.
 * @param content content to be notified.
 * @result YES to accept the content, NO to postpone the content (like overlay disabled in a
 *         specific context).
 */
- (BOOL)handleNotification:(CPInteractiveContent*)content;

/**
 * Reach module needs to control notification appearance.
 * When this method is called the notifier should clear any displayed notification for the given category.
 * @param category the category to clear. This parameter can be ignored if the notifier handles only one kind of
 * category.
 */
- (void)clearNotification:(NSString*)category;

@end
