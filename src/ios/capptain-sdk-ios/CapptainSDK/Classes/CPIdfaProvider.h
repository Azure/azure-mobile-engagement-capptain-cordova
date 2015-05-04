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

/**
 * Provider for the Apple IDFA.
 *
 * This class must be integrated into your project whether you want Capptain to collect the IDFA or not. By default,
 * this class will return the actual
 * advertising identifier (on iOS 6+) using the AdSupport framework. This can be disabled by adding the preprocessor
 * macro named `CAPPTAIN_DISABLE_IDFA`.
 *
 */
@interface CPIdfaProvider : NSObject

/**
 *  The singleton instance of the IDFA provider.
 *
 *  @return a shared instance of `CPIdfaProvider`.
 */
+ (id)shared;

/**
 * Check if IDFA reporting is enabled and ad tracking is enabled or not.
 *
 * @return A Boolean NSNumber that indicates whether the user has limited ad tracking or nil
 *         if IDFA reporting is not enabled.
 */
- (NSNumber*)isIdfaEnabled;

/* Return the identifier for advertisers or nil if not enabled/available. */

/**
 * Get the advertising identifier UUID value.
 *
 * @return the value of the advertising identifier or nil if ad tracking is disabled
 *         or IDFA collection is disabled.
 */
- (NSString*)idfa;

@end