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
#import <UIKit/UIKit.h>
#import "CPModule.h"
#import "CPReachDataPushDelegate.h"
#import "CPNotifier.h"

@class CPStorage;
@class CPContentViewController;

/* Export module name */
extern NSString* const kCPReachModuleName;

/* Export reach xml namespace */
extern NSString* const kCPReachNamespace;

/* Export reach default category */
extern NSString* const kCPReachDefaultCategory;

/* Reach module state */
typedef enum _CPReachModuleState
{
  CPReachModuleStateIdle = 1,
  CPReachModuleStateNotifying = 2,
  CPReachModuleStateShowing = 3
} CPReachModuleState;

/**
 * The Reach Capptain module
 *
 * This is the module that manage reach functionalities. It listens push messages thanks to
 * <[CPModule pushMessagesReceived:]> and <[CPModule displayPushMessageNotification:]> and notify the user
 * about announcements and polls.<br>
 * You usually create the module using the method moduleWithNotificationIcon: and pass it when you initialize Capptain
 * (using method <[CapptainAgent registerApp:identifiedBy:modules:]>)
 *
 * *Example of a basic integration:*
 *
 *  - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
 *      CPReachModule* reach = [CPReachModule moduleWithNotificationIcon:[UIImage imageNamed:@"icon.png"]];
 *      [CapptainAgent registerApp:@"YOUR_APPID" identifiedBy:@"YOUR_SDK_KEY" modules:reach, nil];
 *
 *      ...
 *
 *      return YES;
 *  }
 *
 */
@interface CPReachModule : NSObject<CPModule>
{
  @private
  /* Storage */
  CPStorage* _db;

  /* Entries scheduled to be removed */
  NSMutableIndexSet* _trash;

  /* Scanning db storage context */
  BOOL _scanning;

  /* Current state */
  CPReachModuleState _state;

  /** The message identifier being processed */
  NSUInteger _processingId;

  /* Current activity */
  NSString* _currentActivity;

  /* Announcement controller classes by category */
  NSMutableDictionary* _announcementControllers;

  /* Poll controller classes by category */
  NSMutableDictionary* _pollControllers;

  /* Notification handlers by category */
  NSMutableDictionary* _notifiers;

  /* Special parameters to inject in announcement's action url and body */
  NSDictionary* _params;

  /* Remember if the agent has been started or not */
  BOOL _isStarted;

  /* Data push delegate */
  id<CPReachDataPushDelegate> _dataPushDelegate;

  /* Current displayed controller */
  CPContentViewController* _displayedController;

  /* Auto badge */
  BOOL _autoBadgeEnabled;
  BOOL _badgeNotificationReceived;

  /* Maximum number of contents */
  NSUInteger _maxContents;
}

/**
 * Instantiate a new reach Capptain module.
 * @param icon The image to use as the notification icon
 */
+ (id)moduleWithNotificationIcon:(UIImage*)icon;

/**
 * Enable or disable automatic control of the badge value. If enabled, the Reach module will automatically
 * clear the application badge and also reset the value stored by Capptain every time the application
 * is started or foregrounded.
 * @param enabled YES to enable auto badge, NO otherwise (Disabled by default).
 */
- (void)setAutoBadgeEnabled:(BOOL)enabled;

/**
 * Set the maximum number of in-app campaigns that can be displayed.
 * @param maxCampaigns The maximum number of in-app campaigns that can be displayed (0 to disable in-app campaigns).
 */
- (void)setMaxInAppCampaigns:(NSUInteger)maxCampaigns;

/**
 * Register an announcement category.
 * @param category The name of the category to map.
 * @param clazz The associated view controller class to instantiate when an announcement of the given
 * category is received. The controller class should inherit from <CPAnnouncementViewController>.
 */
- (void)registerAnnouncementController:(Class)clazz forCategory:(NSString*)category;

/**
 * Register a poll category.
 * @param category The name of the category to map.
 * @param clazz The associated view controller class to instantiate when an poll of the given
 * category is received. The controller class should inherit from <CPPollViewController>.
 */
- (void)registerPollController:(Class)clazz forCategory:(NSString*)category;

/**
 * Register a notifier for a given category.
 * @param notifier Notifier to register for a category.
 * @param category The name of the category.
 */
- (void)registerNotifier:(id<CPNotifier>)notifier forCategory:(NSString*)category;

/**
 * Mark given content processed. It will be removed from cache,
 * and any other waiting contents will be displayed to the user.
 * @param content The content to mark as processed.
 */
- (void)markContentProcessed:(CPReachContent*)content;

/**
 * Called when a notification is actioned.
 * @param content The content associated to the notification.
 */
- (void)onNotificationActioned:(CPReachContent*)content;

/** The delegate that will handle data pushes.  */
@property(nonatomic, retain) id<CPReachDataPushDelegate> dataPushDelegate;

@end
