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

#import "CPReachContent.h"

/**
 * Data push's type.
 */
typedef NS_ENUM (NSInteger, CPDatapushType)
{
  /** Unknwon data-push type */
  CPDatapushTypeUnknown = -1,

  /** Data-push with a text content */
  CPDatapushTypeText = 1,

  /** Data-push with a base 64 encoded content */
  CPDatapushTypeBase64 = 2

};

/**
 * The `CPReachDataPush` class defines objects that represent a generic reach content.
 */
@interface CPReachDataPush : CPReachContent
{
  @private
  CPDatapushType _type;
}

/**
 * Parse an announcement
 * @param element Parsed XML root DOM element.
 * @param params special parameters to replace in the body of the datapush.
 * @result A new announcement or nil if it couldn't be parsed.
 */
+ (id)datapushWithElement:(CP_TBXMLElt*)element params:(NSDictionary*)params;

/**
 * Get the type for this data push.
 *
 * Possible values are:
 *
 * - `CPDatapushTypeUnknown`: Unknown data push type
 * - `CPDatapushTypeText`: A text data push
 * - `CPDatapushTypeBase64`: A base 64 data push
 */
@property(readonly) CPDatapushType type;

/** Get decoded body. Only apply on base 64 data pushes (return `nil` for other types). */
@property(readonly) NSData* decodedBody;

@end
