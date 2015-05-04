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
#import "CPModule.h"

/** Tracking with SmartAd */
extern NSString* const kCPTrackModuleSmartAd;

/**
 * A `CPTrackModule` object represents a Capptain module that is used to track application installations.
 *
 * The tracking module will report a list of UDIDs used by the corresponding Ad Server to our backend in order to find
 * the
 * correspondence between the user who clicks on the Ad Server campaign and the user who first launched the application.
 * The module currently support the following list of Ad Servers:
 *
 * - `kCPTrackModuleSmartAd`: [SmartAd](http://smartadserver.fr/).
 *
 *
 * *Example of a basic integration:*
 *
 *  - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
 *
 *      CPTrackModule* track = [CPTrackModule moduleWithAdServers:@[kCPTrackModuleSmartAd]];
 *      [CapptainAgent registerApp:@"YOUR_APPID" identifiedBy:@"YOUR_SDK_KEY" modules:track, nil];
 *
 *      ...
 *
 *      return YES;
 *  }
 *
 */
@interface CPTrackModule : NSObject<CPModule>

/**
 * Instantiate a new tracking module.
 *
 * @param adServers a list of third party Ad Servers that you want to support. The tracking module currently supports
 * the following Ad Servers:
 *
 * - `kCPTrackModuleSmartAd`: [SmartAd](http://smartadserver.fr/).
 */
+ (instancetype)moduleWithAdServers:(NSArray*)adServers;

/**
 * If the Ad Server you use is not currently supported by the tracking module, you can use this function to report the
 * Advertising Identifier that could
 * be used to perform the matching between the user who clicked on your Ad Server campaign and the user who first
 * launched the application.
 *
 * This method MUST be called right after the init method moduleWithAdServers:.
 *
 * @param enableIDFA Set to YES to report the IDFA, NO otherwise (default value). The IDFA is not reported if the user
 * has limited
 * the advertising in the settings.
 */
- (void)setIdfaReportingEnabled:(BOOL)enableIDFA;

@end
