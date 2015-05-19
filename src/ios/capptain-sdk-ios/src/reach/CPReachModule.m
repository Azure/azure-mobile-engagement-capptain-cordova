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

#import <objc/runtime.h>
#import "CPReachModule.h"
#import "CapptainAgent.h"
#import "CPReachDataPush.h"
#import "CPReachNotifAnnouncement.h"
#import "CPReachAnnouncement.h"
#import "CPReachPoll.h"
#import "CPDefaultAnnouncementViewController.h"
#import "CPAnnouncementViewController.h"
#import "CPDefaultPollViewController.h"
#import "CPViewControllerUtil.h"
#import "CPPushMessage.h"
#import "CPModule.h"
#import "CPStorage.h"
#import "CP_TBXML.h"
#import "CPContentViewController.h"
#import "CPDefaultNotifier.h"

NSString* const kCPReachModuleName = @"ReachModule";
NSString* const kCPReachNamespace = @"urn:ubikod:ermin:reach:0";
NSString* const kCPReachDefaultCategory = @"capptain.category.default";

static NSString* const kCPReachDBName = @"capptain.reach";
static double const kCPReachDBVersion = 1.0;

/**********************************************************************************************************************/
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark -
#pragma mark Reach module implementation
#pragma mark -
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**********************************************************************************************************************/

/* Real implementation of the reach module */
@implementation CPReachModule

- (id)initWithNotificationIcon:(UIImage*)icon
{
  self = [super init];
  if (self != nil)
  {
    _state = CPReachModuleStateIdle;

    /* Storage holding reach contents */
    _db = [[CPStorage storageWithName:kCPReachDBName version:kCPReachDBVersion autoSync:YES] retain];
    [_db setDelegate:self];

    /* Default maximum number of contents */
    _maxContents = 64;

    /* Trash */
    _trash = [[NSMutableIndexSet alloc] init];

    /* Create dictionary holding announcement and poll controller classes for each category */
    _announcementControllers = [[NSMutableDictionary alloc] init];
    _pollControllers = [[NSMutableDictionary alloc] init];

    /* Create dictionary holding notification handlers for each category */
    _notifiers = [[NSMutableDictionary alloc] init];

    /* Default categories */
    _notifiers[kCPReachDefaultCategory] = [CPDefaultNotifier notifierWithIcon:icon];
    _announcementControllers[kCPReachDefaultCategory] = [CPDefaultAnnouncementViewController class];
    _pollControllers[kCPReachDefaultCategory] = [CPDefaultPollViewController class];
  }
  return self;
}

- (void)dealloc
{
  [_db release];
  [_trash release];
  [_currentActivity release];
  [_announcementControllers release];
  [_pollControllers release];
  [_notifiers release];
  [_params release];
  [_dataPushDelegate release];
  [_displayedController release];
  [super dealloc];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Public methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

+ (id)moduleWithNotificationIcon:(UIImage*)icon
{
  return [[[[self class] alloc] initWithNotificationIcon:icon] autorelease];
}

- (void)setAutoBadgeEnabled:(BOOL)enabled
{
  _autoBadgeEnabled = enabled;
}

- (void)setMaxInAppCampaigns:(NSUInteger)maxContents
{
  _maxContents = maxContents;
}

- (void)registerAnnouncementController:(Class)clazz forCategory:(NSString*)category
{
  /* Check is not started */
  if (_isStarted)
    [NSException raise:NSInternalInconsistencyException format:
     @"Reach agent already started. Please call this method before registering Capptain application."];

  /* Check given class is a subclass of CPAnnouncementViewController */
  if (![clazz isSubclassOfClass:[CPAnnouncementViewController class]])
  {
    [NSException raise:NSInvalidArgumentException
                format:
     @"Couldn't register controller to category '%@' : %@ should inherit from CPAnnouncementViewController", category,
     NSStringFromClass(clazz)];
    return;
  }

  /* Add controller class */
  _announcementControllers[category] = clazz;
}

- (void)registerPollController:(Class)clazz forCategory:(NSString*)category
{
  /* Check is not started */
  if (_isStarted)
    [NSException raise:NSInternalInconsistencyException format:
     @"Reach agent already started. Please call this method before registering Capptain application."];

  /* Check given class is a subclass of CPAnnouncementViewController */
  if (![clazz isSubclassOfClass:[CPPollViewController class]])
  {
    [NSException raise:NSInvalidArgumentException
                format:@"Couldn't register controller to category '%@' : %@ should inherit from CPPollViewController",
     category, NSStringFromClass(clazz)];
    return;
  }

  /* Add controller class */
  _pollControllers[category] = clazz;
}

- (void)registerNotifier:(id<CPNotifier>)notifier forCategory:(NSString*)category
{
  /* Add notifier */
  _notifiers[category] = notifier;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Module methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)start
{
  /* Agent now started */
  _isStarted = YES;

  /* Set storage capacity */
  [_db setCapacity:_maxContents];

  /* Clear message received flag */
  _badgeNotificationReceived = NO;

  /* Action URL parameters */
  [_params release];
  _params = [@{@"{deviceid}": [[CapptainAgent shared] deviceId]} retain];

  /* Clear badge if needed */
  if (_autoBadgeEnabled && [[UIApplication sharedApplication] applicationIconBadgeNumber] > 0)
  {
    /* Clear badge */
    [self clearBadge];

    /* Clear badge appInfo */
    [self clearBadgeAppInfo];
  }
}

- (void)stop
{
  /* If auto badge enabled, clear badge value. This will also clear the notification center. */
  if (_autoBadgeEnabled)
  {
    /* Clear badge appInfo if a badge notification has been received during session */
    if (_badgeNotificationReceived)
      [self clearBadgeAppInfo];
    _badgeNotificationReceived = NO;
  }

  /* Exit current displayed controller */
  [_displayedController exit];
}

- (NSString*)name
{
  return kCPReachModuleName;
}

- (void)pushNotificationReceived:(NSDictionary*)notification
{
  NSString* badge = notification[@"aps"][@"badge"];
  if (badge != nil && _autoBadgeEnabled)
  {
    /* Clear badge */
    [self clearBadge];

    /* Remember that a badge notification has been received */
    _badgeNotificationReceived = YES;
  }
}

- (BOOL)displayPushMessageNotification:(CPPushMessage*)msg
{
  /* Try to parse reach content */
  CPReachContent* content = [self parseContent:msg];

  /* If it's a reach content */
  if (content)
  {
    /*
     * Only process interactive content
     */
    if ([content isKindOfClass:[CPInteractiveContent class]])
    {
      /* Exit current displayed controller */
      [_displayedController exit];

      /* Check if this content is in the db */
      for (CPStorageEntry* sdata in _db)
      {
        if ([sdata.data isKindOfClass:[CPPushMessage class]] &&
            [((CPPushMessage*)sdata.data).messageId isEqualToString:msg.messageId])
        {
          [content setLocalId:sdata.uid];
          break;
        }
      }

      /* If content not in db, and this message was already processed, disable feedbacks  */
      if (content.localId == 0 && msg.cached)
        [content setFeedback:NO];

      /* Hide any displayed notification */
      [self hideNotification];

      /* Action notification */
      _processingId = content.localId;
      ((CPInteractiveContent*)content).notifiedFromNativePush = YES;
      [((CPInteractiveContent*)content)actionNotification];
    }
  }

  return content != nil;
}

- (void)pushMessagesReceived:(NSArray*)msgs
{
  /* For each message */
  NSMutableArray* pContents = [NSMutableArray array];
  NSMutableArray* pMessages = [NSMutableArray array];
  for (CPPushMessage* msg in msgs)
  {
    /* Try to parse reach content */
    CPReachContent* content = [self parseContent:msg];

    /* If it's a reach content */
    if (content)
    {
      /* Ignore reach content if must be notified only in background and we received it while the application was
       * launched */
      if ([content isKindOfClass:[CPInteractiveContent class]] && ((CPInteractiveContent*)content).behavior ==
          CPContentBehaviorBackground)
        continue;

      /* Ignore content if expired */
      if ([content isExpired])
        continue;

      /* Process data push automatically */
      if ([content isKindOfClass:[CPReachDataPush class]])
      {
        /* Data push are not saved, because there are directly processed once received */
        [self process:content];
      }
      /* Otherwise, content must be saved and notified */
      else
      {
        [pContents addObject:content];
        [pMessages addObject:msg];
      }
    }
  }

  /* If there is content to persist */
  if (pContents.count > 0 && _maxContents > 0)
  {
    NSUInteger nb = pContents.count;

    /* If the number of contents to save exceed the maximum number of contents */
    if (nb > _maxContents)
    {
      /*
       * Drop old contents that are exceeding the maximum number of in-app contents.
       * (first content is the array is the oldest one)
       */
      NSIndexSet* dRange = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, nb - _maxContents)];
      NSArray* dContents = [pContents objectsAtIndexes:dRange];
      for (CPReachContent* content in dContents)
        [content drop];

      /* Remove dropped contents from messages to save */
      [pMessages removeObjectsAtIndexes:dRange];
    }

    /* Save messages */
    if (pMessages.count > 0)
    {
      [_db putAll:pMessages];

      /* Start displaying notifications */
      [self scan];
    }
  }
}

/* Re-scan the list of contents when the current activity has changed */
- (void)activityChanged:(NSString*)newActivity
{
  /* Keep current activity */
  [_currentActivity release];
  _currentActivity = [newActivity copy];

  /* Ensure notification is hidden */
  [self hideNotification];

  /* Re-scan */
  [self scan];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Private methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/* Trash given content id */
- (void)trash:(NSUInteger)uid
{
  if (uid > 0)
  {
    /* Bypass trash if not scanning */
    if (!_scanning)
      [_db remove:uid];
    else
      [_trash addIndex:uid];
  }
}

/* Empty trash */
- (void)emptyTrash
{
  for (NSUInteger i = [_trash firstIndex]; i != NSNotFound; i = [_trash indexGreaterThanIndex:i])
    [_db remove:i];
  [_trash removeAllIndexes];
}

- (CPReachContent*)parseContent:(CPPushMessage*)msg
{
  if (!msg.payload)
    return nil;

  CP_TBXMLElt* root = [[CP_TBXML tbxmlWithXMLString:msg.payload] rootXMLElement];

  /* Check XML namespace */
  if (root && [[CP_TBXML valueOfAttributeNamed:@"xmlns" forElement:root] isEqualToString:kCPReachNamespace])
  {
    NSString* element = [CP_TBXML elementName:root];
    CPReachContent* content = nil;

    /* Data push */
    if ([element isEqualToString:@"datapush"])
      content = [CPReachDataPush datapushWithElement:root params:_params];

    /* Notif announcement */
    if ([element isEqualToString:@"notifAnnouncement"])
      content = [CPReachNotifAnnouncement notifAnnouncementWithElement:root params:_params];

    /* Announcement */
    if ([element isEqualToString:@"announcement"])
      content = [CPReachAnnouncement announcementWithElement:root params:_params];

    /* Poll */
    else if ([element isEqualToString:@"poll"])
      content = [CPReachPoll pollWithElement:root];

    /* Should content send feedback? */
    [content setFeedback:(msg.replyTo != nil)];

    /* Content successfully parsed */
    return content;
  }

  /* Content couldn't be parsed */
  return nil;
}

/* Scan the list of contents and display the first one that can be displayed. */
- (void)scan
{
  /* If module is ready to display notifications and not already scanning */
  if (_state == CPReachModuleStateIdle && !_scanning)
  {
    _scanning = YES;
    for (CPStorageEntry* sdata in _db)
    {
      BOOL parsed = NO;

      if ([sdata.data isKindOfClass:[CPPushMessage class]])
      {
        /* If content can be notified in the current activity, display notification view */
        CPReachContent* content = [self parseContent:(CPPushMessage*)sdata.data];
        if (content)
        {
          /* Successfully parsed */
          parsed = YES;

          /* Set local id */
          [content setLocalId:sdata.uid];

          /* Remove if expired */
          if ([content isExpired])
          {
            [self trash:sdata.uid];
          }
          /* If it's a data push process it directly and continue */
          else if ([content isKindOfClass:[CPReachDataPush class]])
          {
            [self process:content];
          }
          /* Check if we can notify it and notify it, if not notified check for next content */
          else if ([content isKindOfClass:[CPInteractiveContent class]] &&
                   [((CPInteractiveContent*)content)canNotify:_currentActivity])
          {
            /* Cast */
            CPInteractiveContent* icontent = (CPInteractiveContent*)content;

            /* Get a notifier for this category */
            id<CPNotifier> notifier = _notifiers[icontent.category];
            if (!notifier)
              notifier = _notifiers[kCPReachDefaultCategory];

            /* Try to notify content */
            @try
            {
              if ([notifier handleNotification:icontent])
              {
                [icontent displayNotification];
                _state = CPReachModuleStateNotifying;
                _processingId = icontent.localId;
                break;
              }
            }@catch (NSException* exception)
            {
              NSLog(@"[Capptain][ERROR] %@", [exception description]);

              /* Cannot notify, drop content */
              [icontent drop];
            }
          }
        }
      }

      /* Remove from storage if content could not be parsed */
      if (!parsed)
      {
        [self trash:sdata.uid];
      }
    }

    /* End of scan, empty trash */
    _scanning = NO;
    [self emptyTrash];
  }
}

/* Hide any displayed notification */
- (void)hideNotification
{
  if (_state == CPReachModuleStateNotifying)
  {
    _processingId = -1;
    _state = CPReachModuleStateIdle;
    for (NSString* category in[_notifiers allKeys])
      [_notifiers[category] clearNotification:category];
  }
}

/* Clear badge application icon */
- (void)clearBadge
{
  [[UIApplication sharedApplication] setApplicationIconBadgeNumber:1];
  [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
}

/* Clear badge appInfo */
- (void)clearBadgeAppInfo
{
  [[CapptainAgent shared] sendAppInfo:@{@"badge": @"0"}];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Reach actions
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/* Mark content as processed */
- (void)markContentProcessed:(CPReachContent*)content
{
  /* Trash content */
  [self trash:content.localId];

  /* State is only for interactive reach content */
  if (_processingId == content.localId)
  {
    /* Idle now */
    _processingId = -1;
    _state = CPReachModuleStateIdle;

    /* No controller displayed anymore */
    [_displayedController release];
    _displayedController = nil;

    /* If there are still reach contents to process, continue */
    [self scan];
  }
}

- (void)onNotificationActioned:(CPReachContent*)content
{
  /* Process content */
  [self process:content];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Capptain Reach events
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/* Process the given content. */
- (void)process:(CPReachContent*)content
{
  /* If it's a data push */
  if ([content class] == [CPReachDataPush class])
  {
    /* Cast */
    CPReachDataPush* datapush = (CPReachDataPush*)content;

    /* Inform delegate */
    NSNumber* result = nil;
    if (datapush.type == CPDatapushTypeText &&
        [_dataPushDelegate respondsToSelector:@selector(didReceiveStringDataPushWithCategory:body:)])
      result = @([_dataPushDelegate didReceiveStringDataPushWithCategory:datapush.category body:datapush.body]);
    else if (datapush.type == CPDatapushTypeBase64 &&
             [_dataPushDelegate respondsToSelector:@selector(didReceiveBase64DataPushWithCategory:decodedBody:
                                                             encodedBody:)])
      result =
        @([_dataPushDelegate didReceiveBase64DataPushWithCategory:datapush.category decodedBody:datapush.decodedBody
                                                      encodedBody:datapush.body]);

    /* Report action on the content if a delegate handled the data push */
    if (result)
    {
      if ([result boolValue])
        [datapush actionContent];
      else
        [datapush exitContent];
    }
    /* Otherwise drop content */
    else
      [datapush drop];

    /* Content processed */
    return;
  }

  /* Initialize the view controller based on the content type */
  CPContentViewController* controller = nil;

  /* If it's a notif announcement */
  if ([content class] == [CPReachNotifAnnouncement class])
  {
    /* Notif announcements */
    CPReachNotifAnnouncement* notifAnnouncement = ((CPReachNotifAnnouncement*)content);

    /* Launch action url */
    if (notifAnnouncement.actionURL)
      [[UIApplication sharedApplication] openURL:[NSURL URLWithString:notifAnnouncement.actionURL]];
  }
  /* If it's an announcement */
  else if ([content class] == [CPReachAnnouncement class])
  {
    /* Cast */
    CPReachAnnouncement* announcement = (CPReachAnnouncement*)content;

    /* Default controller class */
    Class clazz = _announcementControllers[kCPReachDefaultCategory];

    /* Check category */
    if ([announcement.category length] > 0)
    {
      /* If this category has been mapped to a custom controller class */
      Class controllerClass = _announcementControllers[announcement.category];
      if (controllerClass)
        clazz = controllerClass;
    }

    /* Allocate view controller */
    id announcementController = NSAllocateObject(clazz, 0, NULL);
    announcementController = [announcementController initWithAnnouncement:announcement];
    controller = announcementController;
  }
  /* If it's a poll */
  else if ([content class] == [CPReachPoll class])
  {
    /* Cast */
    CPReachPoll* poll = (CPReachPoll*)content;

    /* Default controller class */
    Class clazz = _pollControllers[kCPReachDefaultCategory];

    /* Check category */
    if ([poll.category length] > 0)
    {
      /* If this category has been mapped to a custom controller class */
      Class controllerClass = _pollControllers[poll.category];
      if (controllerClass)
        clazz = controllerClass;
    }

    /* Allocate view controller */
    id pollController = NSAllocateObject(clazz, 0, NULL);
    pollController = [pollController initWithPoll:poll];
    controller = pollController;
  }

  /* Present view controller */
  if (controller)
  {
    _state = CPReachModuleStateShowing;
    [CPViewControllerUtil presentViewController:controller];

    [_displayedController release];
    _displayedController = [controller retain];
    [controller release];

    /* Content is displayed */
    if ([content isKindOfClass:[CPInteractiveContent class]])
      [((CPInteractiveContent*)content)displayContent];
  }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Storage eviction delegate
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)storage:(CPStorage*)storage didEvictEntry:(CPStorageEntry*)entry
{
  /* Do nothing if this content is being displayed */
  if (_state == CPReachModuleStateShowing && entry.uid == _processingId)
    return;

  /* Parse content that is going to be evicted */
  CPReachContent* content = [self parseContent:(CPPushMessage*)entry.data];
  if (content)
  {
    /* Hide notification and display a new one if this content is being notified */
    if (_state == CPReachModuleStateNotifying && entry.uid == _processingId)
    {
      [self hideNotification];
      [self scan];
    }

    /* Drop content */
    [content drop];
  }

}

@end
