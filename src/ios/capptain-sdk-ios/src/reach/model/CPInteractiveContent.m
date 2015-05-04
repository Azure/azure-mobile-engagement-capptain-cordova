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

#import "CPInteractiveContent.h"
#import "CPReachModule.h"
#import "CapptainAgent.h"

@implementation CPInteractiveContent

- (id)initWithElement:(CP_TBXMLElt*)root
{
  self = [super initWithElement:root];

  if (self != nil)
  {
    _allowedActivities = [[NSMutableArray alloc] init];

    /* Title */
    _title = [[CP_TBXML textForElement:[CP_TBXML childElementNamed:@"title" parentElement:root]] copy];

    /* Notification */
    CP_TBXMLElt* notifElt = [CP_TBXML childElementNamed:@"notification" parentElement:root];
    if (notifElt)
    {
      /* Parse title and message */
      _notificationTitle =
        [[CP_TBXML textForElement:[CP_TBXML childElementNamed:@"title" parentElement:notifElt]] copy];
      _notificationMessage =
        [[CP_TBXML textForElement:[CP_TBXML childElementNamed:@"message" parentElement:notifElt]] copy];

      /* Get image data */
      _notificationImageString =
        [[CP_TBXML textForElement:[CP_TBXML childElementNamed:@"image" parentElement:notifElt]] copy];

      /* The notification can be closed unless closeable is set to a non "true" value */
      _notificationCloseable = [self boolAttributeNamed:@"closeable" forElement:notifElt defaultValue:YES];

      /* The notification has a content icon unless icon attribute set to a non "true" value. */
      _notificationIcon = [self boolAttributeNamed:@"icon" forElement:notifElt defaultValue:YES];
    }

    /* Behavior */
    CP_TBXMLElt* behaviorElt = [CP_TBXML childElementNamed:@"behavior" parentElement:root];
    if (!behaviorElt || !behaviorElt->firstChild)
    {
      _behavior = CPContentBehaviorAnyTime;
    } else
    {
      NSString* elementName = [CP_TBXML elementName:behaviorElt->firstChild];
      if ([elementName isEqualToString:@"session"])
        _behavior = CPContentBehaviorSession;
      else if ([elementName isEqualToString:@"background"])
        _behavior = CPContentBehaviorBackground;
      else if ([elementName isEqualToString:@"activity"])
      {
        _behavior = CPContentBehaviorActivity;
        CP_TBXMLElt* activityElement = behaviorElt->firstChild;
        while (activityElement)
        {
          NSString* activityName = [CP_TBXML textForElement:activityElement];
          [_allowedActivities addObject:activityName];
          activityElement = activityElement->nextSibling;
        }
      } else
        _behavior = CPContentBehaviorAnyTime;
    }

    /* Action element */
    CP_TBXMLElt* actionElt = [CP_TBXML childElementNamed:@"action" parentElement:root];
    if (actionElt)
    {
      CP_TBXMLElt* actionLabelElt = [CP_TBXML childElementNamed:@"label" parentElement:actionElt];
      if (actionLabelElt)
        _actionLabel = [[CP_TBXML textForElement:actionLabelElt] copy];
    }

    /* Exit element */
    CP_TBXMLElt* exitElt = [CP_TBXML childElementNamed:@"exit" parentElement:root];
    if (exitElt)
    {
      CP_TBXMLElt* exitLabelElt = [CP_TBXML childElementNamed:@"label" parentElement:exitElt];
      if (exitLabelElt)
        _exitLabel = [[CP_TBXML textForElement:exitLabelElt] copy];
    }
  }
  return self;
}

- (void)dealloc
{
  [_title release];
  [_actionLabel release];
  [_exitLabel release];
  [_notificationTitle release];
  [_notificationMessage release];
  [_notificationImage release];
  [_notificationImageString release];
  [_allowedActivities release];
  [super dealloc];
}

- (BOOL)boolAttributeNamed:(NSString*)name forElement:(CP_TBXMLElt*)elt defaultValue:(BOOL)defaultValue
{
  NSString* value = [CP_TBXML valueOfAttributeNamed:name forElement:elt];
  return value != nil ? [value isEqualToString:@"true"] : defaultValue;
}

- (UIImage*)notificationImage
{
  /* Decode image */
  if (_notificationImageString && !_notificationImage)
  {
    /* Decode base 64 then decode as an image */
    _notificationImage = [[UIImage imageWithData:[self decodeBase64:_notificationImageString]] retain];

    /* Clear image data */
    [_notificationImageString release];
    _notificationImageString = nil;
  }

  return _notificationImage;
}

- (BOOL)canNotify:(NSString*)activity
{
  return _behavior != CPContentBehaviorActivity || [_allowedActivities containsObject:activity];
}

- (void)displayNotification
{
  /* Avoid sending feedback multiple times */
  if (!_notificationDisplayed)
  {
    [self sendFeedback:@"in-app-notification-displayed" extras:nil];
    _notificationDisplayed = YES;
  }
}

- (void)exitNotification
{
  [self process:@"in-app-notification-exited" extras:nil];
}

- (void)actionNotification
{
  [self actionNotification:YES];
}

- (void)actionNotification:(BOOL)launchAction
{
  /* Report status */
  if (!_notificationActioned)
  {
    [self sendFeedback:[NSString stringWithFormat              :@"%@-notification-actioned",
                        self.notifiedFromNativePush ? @"system":@"in-app"] extras:nil];
    _notificationActioned = YES;
  }

  /* Notify module */
  if (launchAction)
  {
    CPReachModule* module = (CPReachModule*)[[CapptainAgent shared] getModule:kCPReachModuleName];
    [module onNotificationActioned:self];
  }
}

- (void)displayContent
{
  /* Avoid sending feedback multiple times */
  if (!_contentDisplayed)
  {
    [self sendFeedback:@"content-displayed" extras:nil];
    _contentDisplayed = YES;
  }
}

@end
