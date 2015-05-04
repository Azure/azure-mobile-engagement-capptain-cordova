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
 * The `CPReachNotifAnnouncement` class defines objects that represent a Capptain _notification only_ announcement.
 *
 * This is a special announcement used when you just want to display the notification (application banner or apple
 * push).
 * When the user clicks on the notification, the action url is launched, and the announcement is acknownledged.
 */
@interface CPReachNotifAnnouncement : CPReachAbstractAnnouncement

/**
 * Parse a notif announcement
 * @param element Parsed XML root DOM element.
 * @param params special parameters to replace in the action URL.
 * @result A new notif announcement or nil if it couldn't be parsed.
 */
+ (id)notifAnnouncementWithElement:(CP_TBXMLElt*)element params:(NSDictionary*)params;


@end
