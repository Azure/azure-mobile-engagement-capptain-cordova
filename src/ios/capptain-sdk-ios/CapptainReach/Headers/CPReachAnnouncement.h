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
#import "CPReachAbstractAnnouncement.h"

/**
 * Announcement's type.
 */
typedef NS_ENUM (NSInteger, CPAnnouncementType)
{
  /** Unknwon announcement type */
  CPAnnouncementTypeUnknown = -1,

  /** Announcement with a text plain content */
  CPAnnouncementTypeText = 1,

  /** Announcement with an HTML content */
  CPAnnouncementTypeHtml = 2

};

/**
 * The `CPReachAnnouncement` class defines objects that represent generic Capptain announcements.
 *
 * You usually have to use this class when you implement your own
 * announcement view controller.<br> The Capptain Reach SDK will instantiate your view controller using
 * method <[CPAnnouncementViewController initWithAnnouncement:]>.
 */
@interface CPReachAnnouncement : CPReachAbstractAnnouncement {
  @private
  CPAnnouncementType _type;
}

/**
 * Parse an announcement
 * @param element Parsed XML root DOM element.
 * @param params special parameters to replace in the action URL and body of the announcement.
 * @result A new announcement or nil if it couldn't be parsed.
 */
+ (id)announcementWithElement:(CP_TBXMLElt*)element params:(NSDictionary*)params;

/**
 * Get the mime type for this announcement. This is useful to interpret the text returned by
 * #body.
 *
 * Possible values are:
 *
 * - `CPAnnouncementTypeUnknown`: Unknown announcement type
 * - `CPAnnouncementTypeText`: A text announcement (associated mimetype is text/plain)
 * - `CPAnnouncementTypeHtml`: An HTML announcement (associated mimetype is text/html)
 */
@property(readonly) CPAnnouncementType type;

@end
