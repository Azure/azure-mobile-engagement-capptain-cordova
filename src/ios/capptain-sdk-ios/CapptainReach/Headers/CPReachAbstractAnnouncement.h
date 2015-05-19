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
 * The `CPReachAbstractAnnouncement` is a base class for all kind of announcements.
 */
@interface CPReachAbstractAnnouncement : CPInteractiveContent
{
  @private
  NSString* _actionURL;
}

/**
 * Initialize an abstract announcement. Should only be called by subclasses.
 * @param element Parsed XML root DOM element.
 * @param params special parameters to replace in the action URL.
 * @result An initialized abstract announcement or nil if it couldn't be parsed.
 */
- (id)initWithElement:(CP_TBXMLElt*)element params:(NSDictionary*)params;

/** URL to launch as an action */
@property(readonly) NSString* actionURL;

@end
