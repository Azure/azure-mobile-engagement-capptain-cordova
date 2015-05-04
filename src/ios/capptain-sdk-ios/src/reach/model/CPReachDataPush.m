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

#import "CPReachDataPush.h"

@implementation CPReachDataPush

- (id)initWithElement:(CP_TBXMLElt*)root params:(NSDictionary*)params
{
  self = [super initWithElement:root];

  if (self != nil)
  {
    /* Data push type */
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
  }
  return self;
}

+ (id)datapushWithElement:(CP_TBXMLElt*)element params:(NSDictionary*)params
{
  return [[[self alloc] initWithElement:element params:params] autorelease];
}

- (CPDatapushType)typeFromString:(NSString*)type
{
  if ([type isEqualToString:@"text/plain"])
    return CPDatapushTypeText;
  else if ([type isEqualToString:@"text/base64"])
    return CPDatapushTypeBase64;
  else
    return CPDatapushTypeUnknown;
}

- (NSString*)kind
{
  return @"datapush";
}

- (NSData*)decodedBody
{
  if (self.type != CPDatapushTypeBase64)
    return nil;

  /* Decode from base 64 */
  return [self decodeBase64:self.body];
}

@end
