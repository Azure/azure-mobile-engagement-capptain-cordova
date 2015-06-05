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

#import "CPReachAnnouncement.h"
#import "CPReachContent.h"
#import "CapptainAgent.h"
#import <UIKit/UIKit.h>

@implementation CPReachAnnouncement

- (id)initWithElement:(CP_TBXMLElt*)root params:(NSDictionary*)params
{
  self = [super initWithElement:root params:params];

  if (self != nil)
  {
    _type = [self typeFromString:[CP_TBXML valueOfAttributeNamed:@"type" forElement:root]];

    /* Replace parameters in the content payload */
    NSString* newBody = self.body;
    NSEnumerator* keyIt = [params keyEnumerator];
    NSString* key;
    while ((key = [keyIt nextObject]))
    {
      newBody = [newBody stringByReplacingOccurrencesOfString:key withString:params[key]];
    }
    self.body = newBody;

    /* Check for parsing errors */
    if (_type == CPAnnouncementTypeUnknown)
    {
      [self release];
      self = nil;
    }
  }
  return self;
}

+ (id)announcementWithElement:(CP_TBXMLElt*)element params:(NSDictionary*)params
{
  return [[[self alloc] initWithElement:element params:params] autorelease];
}

- (CPAnnouncementType)typeFromString:(NSString*)type
{
  if ([type isEqualToString:@"text/plain"])
    return CPAnnouncementTypeText;
  else if ([type isEqualToString:@"text/html"])
    return CPAnnouncementTypeHtml;
  else
    return CPAnnouncementTypeUnknown;
}

- (NSString*)kind
{
  return @"announcement";
}

@end
