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

#import "CPReachAbstractAnnouncement.h"

@implementation CPReachAbstractAnnouncement

- (id)initWithElement:(CP_TBXMLElt*)root params:(NSDictionary*)params
{
  self = [super initWithElement:root];

  if (self != nil)
  {
    /* Action URL */
    NSString* actionUrl = nil;
    CP_TBXMLElt* actionElt = [CP_TBXML childElementNamed:@"action" parentElement:root];
    if (actionElt)
    {
      CP_TBXMLElt* actionURLElt = [CP_TBXML childElementNamed:@"url" parentElement:actionElt];
      if (actionURLElt)
        actionUrl = [CP_TBXML textForElement:actionURLElt];
    }

    /* Replace parameters in the action url */
    if (actionUrl)
    {
      NSEnumerator* keyIt = [params keyEnumerator];
      NSString* key;
      while ((key = [keyIt nextObject]))
      {
        actionUrl = [actionUrl stringByReplacingOccurrencesOfString:key withString:params[key]];
      }
    }
    _actionURL = [actionUrl copy];
  }
  return self;
}

- (void)dealloc
{
  [_actionURL release];
  [super dealloc];
}

@end
