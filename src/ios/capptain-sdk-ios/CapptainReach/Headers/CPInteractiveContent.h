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

#import <UIKit/UIKit.h>
#import "CPReachContent.h"
#import "CP_TBXML.h"

/**
 * Reach content behavior.
 */
typedef NS_ENUM (NSInteger, CPContentBehavior)
{
  /** A reach content that can be displayed at any time. */
  CPContentBehaviorAnyTime = 1,

  /** A reach content that can be displayed only when the application is in background. */
  CPContentBehaviorBackground = 2,

  /** A reach content that can be displayed during application session time. */
  CPContentBehaviorSession = 3,

  /** A reach content that can be displayed only during some activities. */
  CPContentBehaviorActivity = 4
};

/**
 * Abstract class for reach contents that can be displayed to the end-user.
 */
@interface CPInteractiveContent : CPReachContent
{
  @private
  NSString* _title;
  NSString* _actionLabel;
  NSString* _exitLabel;
  NSMutableArray* _allowedActivities;
  CPContentBehavior _behavior;
  NSString* _notificationTitle;
  NSString* _notificationMessage;
  BOOL _notificationIcon;
  BOOL _notificationCloseable;
  NSString* _notificationImageString;
  UIImage* _notificationImage;
  BOOL _notificationDisplayed;
  BOOL _notificationActioned;
  BOOL _contentDisplayed;
  BOOL _notifiedFromNativePush;
}

/**
 * Test if this content can be notified in the current UI context.
 * @param activity Current activity name, null if no current activity.
 * @result YES if this content can be notified in the current UI context.
 */
- (BOOL)canNotify:(NSString*)activity;

/** Report notification has been displayed */
- (void)displayNotification;

/**
 * Action the notification: this will display the announcement or poll, or will
 * launch the action URL associated to the notification, depending of the content kind.
 */
- (void)actionNotification;

/**
 * Action the notification: this will display the announcement or poll, or will
 * launch the action URL associated to the notification, depending of the content kind.
 * @param launchAction YES to launch associated action, NO to just report the notification action.
 */
- (void)actionNotification:(BOOL)launchAction;

/** Exit this notification. */
- (void)exitNotification;

/** Report content has been displayed */
- (void)displayContent;

/** Reach content's title */
@property(nonatomic, readonly) NSString* title;

/** The text label of the action button */
@property(nonatomic, readonly) NSString* actionLabel;

/** The text label of the exit button */
@property(nonatomic, readonly) NSString* exitLabel;

/** Reach content behavior (when to display the notification?) */
@property(readonly) CPContentBehavior behavior;

/** Notification's title */
@property(readonly) NSString* notificationTitle;

/** Notification's message */
@property(readonly) NSString* notificationMessage;

/** @result YES if the notification has a resource icon in notification content, NO otherwise */
@property(readonly) BOOL notificationIcon;

/** @result YES if the notification can be closed without looking at the content, NO otherwise */
@property(readonly) BOOL notificationCloseable;

/** @result notification image */
@property(readonly) UIImage* notificationImage;

/** @result YES if the content was notified from a native Apple Push Notification. */
@property BOOL notifiedFromNativePush;

@end
