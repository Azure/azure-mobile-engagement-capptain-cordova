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

#import "CPDefaultNotifier.h"
#import "CPAutorotateView.h"
#import "CPViewControllerUtil.h"
#import "CPNotificationView.h"
#import "CPReachModule.h"

#define ANIMATION_DURATION 0.4

@implementation CPDefaultNotifier

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Memory management
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (id)initWithIcon:(UIImage*)icon
{
  self = [super init];
  if (self)
  {
    _notificationIcon = [icon retain];
  }

  return self;
}

- (void)dealloc
{
  [_notificationIcon release];
  [_content release];
  [_containerView release];
  [super dealloc];
}

+ (id)notifierWithIcon:(UIImage*)icon
{
  /* Create default notification view */
  return [[[self alloc] initWithIcon:icon] autorelease];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Notifier protocol methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (BOOL)handleNotification:(CPInteractiveContent*)content
{
  /* Don't animate the notification view if the content is the same as the previous one. */
  BOOL animate = (_content == nil || content.localId != _content.localId);

  /* Keep content */
  [_content release];
  _content = [content retain];

  /* Release current view */
  [self dismissNotification];
  [_containerView release];
  _containerView = nil;

  /* Prepare view */
  UIView* notificationView = [self notificationViewForContent:content];

  /* Notification view is required */
  if (notificationView == nil)
    [NSException raise:NSInternalInconsistencyException format:@"Failed to create notification view"];

  /* Find the current application window */
  UIWindow* window = [CPViewControllerUtil availableWindow];

  /* Window is required */
  if (window == nil)
    [NSException raise:NSInternalInconsistencyException format:
     @"Could not find any available window to display notification"];

  /* If the current window contains a special area view */
  UIView* container = [window viewWithTag:NOTIFICATION_AREA_VIEW_TAG];
  if (container)
  {
    /* Configure area view */
    _containerView = [container retain];
    notificationView.frame = container.frame;
    _containerView.backgroundColor = [UIColor clearColor];
    _containerView.hidden = NO;
    notificationView.frame = container.bounds;

    /* Add notification view */
    [container addSubview:notificationView];
    [self notificationViewDidAppear:_containerView];

    /* Animate (fade in) */
    if (animate)
      [self animateAreaNoticationView:_containerView];
  }
  /* Otherwise, it's a banner overlay, add the view inside the current window */
  else
  {
    /* Wrap into an autorotate capable view */
    _containerView = [[CPAutorotateView alloc] initWithContent:notificationView];

    /* Add view to the window */
    [window addSubview:_containerView];
    [self notificationViewDidAppear:_containerView];

    /* Animate opening */
    if (animate)
      [self animateOverlayNotificationView:_containerView];
  }

  /* Invalidate current layout */
  [notificationView setNeedsLayout];

  return YES;
}

- (void)clearNotification:(NSString*)category
{
  [self dismissNotification];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Animations
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)animateOverlayNotificationView:(UIView*)view
{
  UIView* notificationView = nil;
  if ([view.subviews count] > 0)
    notificationView = (view.subviews)[0];

  /* If bottom margin is fixed */
  if (notificationView == nil || (notificationView.autoresizingMask&UIViewAutoresizingFlexibleBottomMargin) !=
      UIViewAutoresizingFlexibleBottomMargin)
  {
    view.transform = CGAffineTransformTranslate(view.transform, 0.0, view.frame.size.height);
    [UIView beginAnimations:nil context:UIGraphicsGetCurrentContext()];
    [UIView setAnimationDuration:ANIMATION_DURATION];
    view.transform = CGAffineTransformTranslate(view.transform, 0.0, -view.frame.size.height);
    [UIView commitAnimations];
  }
  /* Otherwise, if top margin is fixed */
  else if ((notificationView.autoresizingMask&UIViewAutoresizingFlexibleTopMargin) !=
           UIViewAutoresizingFlexibleTopMargin)
  {
    view.transform = CGAffineTransformTranslate(view.transform, 0.0, -view.frame.size.height);
    [UIView beginAnimations:nil context:UIGraphicsGetCurrentContext()];
    [UIView setAnimationDuration:ANIMATION_DURATION];
    view.transform = CGAffineTransformTranslate(view.transform, 0.0, view.frame.size.height);
    [UIView commitAnimations];
  }
}

- (void)animateAreaNoticationView:(UIView*)view
{
  float tmpAlpha = view.alpha;
  view.alpha = 0.0;
  [UIView beginAnimations:nil context:UIGraphicsGetCurrentContext()];
  [UIView setAnimationDuration:ANIMATION_DURATION];
  view.alpha = tmpAlpha;
  [UIView commitAnimations];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark View modifications
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (NSString*)nibNameForCategory:(NSString*)category
{
  /* Fool the linker: call a dumb method on this object otherwise the class will not be linked to the library. */
  [CPNotificationView class];

  return @"CPNotificationView";
}

- (UIView*)notificationViewForContent:(CPInteractiveContent*)content
{
  NSArray* topLevelObjects =
    [[NSBundle mainBundle] loadNibNamed:[self nibNameForCategory:content.category] owner:nil options:nil];

  /* Find a view inside the nib file  */
  UIView* view = nil;
  for (id obj in topLevelObjects)
    if ([obj isKindOfClass:[UIView class]])
    {
      view = obj;
      break;
    }

  /* No view found */
  if (!view)
  {
    [NSException raise:NSInvalidArgumentException format:@"Could not find any view inside nib named: %@",
     [self nibNameForCategory:content.category]];
    return nil;
  }

  /* Prepare the view */
  [self prepareNotificationView:view forContent:content];
  return view;
}

- (void)prepareNotificationView:(UIView*)view forContent:(CPInteractiveContent*)content
{
  UIImageView* iconView = (UIImageView*)[view viewWithTag:NOTIFICATION_ICON_TAG];
  UILabel* titleView = (UILabel*)[view viewWithTag:NOTIFICATION_TITLE_TAG];
  UILabel* messageView = (UILabel*)[view viewWithTag:NOTIFICATION_MESSAGE_TAG];
  UIImageView* imageView = (UIImageView*)[view viewWithTag:NOTIFICATION_IMAGE_TAG];
  UIButton* notificationButton = (UIButton*)[view viewWithTag:NOTIFICATION_BUTTON_TAG];
  UIButton* closeButton = (UIButton*)[view viewWithTag:NOTIFICATION_CLOSE_TAG];

  /* Notification icon */
  if (content.notificationIcon)
  {
    [iconView setHidden:NO];
    [iconView setImage:_notificationIcon];
  } else
    [iconView setHidden:YES];

  /* Notification title */
  if (content.notificationTitle)
  {
    [titleView setText:content.notificationTitle];
    [titleView setHidden:NO];
  } else
    [titleView setHidden:YES];

  /* Notification message */
  if (content.notificationMessage)
  {
    [messageView setText:content.notificationMessage];
    [messageView setHidden:NO];
  } else
    [messageView setHidden:YES];

  /* Notification image */
  if (content.notificationImage)
  {
    [imageView setHidden:NO];
    [imageView setImage:content.notificationImage];
  } else
    [imageView setHidden:YES];

  /* Notification button */
  [notificationButton removeTarget:self action:@selector(onNotificationActioned) forControlEvents:
   UIControlEventTouchDown];
  [notificationButton addTarget:self action:@selector(onNotificationActioned) forControlEvents:UIControlEventTouchDown];

  /* Close button */
  if (content.notificationCloseable)
  {
    [closeButton setHidden:NO];
    [closeButton removeTarget:self action:@selector(onNotificationExited) forControlEvents:UIControlEventTouchDown];
    [closeButton addTarget:self action:@selector(onNotificationExited) forControlEvents:UIControlEventTouchDown];
  } else
    [closeButton setHidden:YES];
}

- (void)dismissNotification
{
  if (_containerView)
  {
    /* In case of a notification area view, hide container and remove its children */
    if (_containerView.tag == NOTIFICATION_AREA_VIEW_TAG)
    {
      _containerView.hidden = YES;
      for (UIView* subview in _containerView.subviews)
        [subview removeFromSuperview];
    }
    /* Otherwise it's an overlay view */
    else
    {
      [_containerView removeFromSuperview];
    }

    [self notificationViewDidDisappear:_containerView];
  }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Actions
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)onNotificationActioned
{
  [self dismissNotification];
  CPInteractiveContent* content = [[_content retain] autorelease];
  [_content release];
  _content = nil;

  [content actionNotification];
}

- (void)onNotificationExited
{
  CPInteractiveContent* content = [[_content retain] autorelease];
  [self dismissNotification];
  [_content release];
  _content = nil;

  [content exitNotification];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Optional callbacks
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)notificationViewDidAppear:(UIView*)view
{
  /* Do nothing here, subclasses can override this method to perform additional tasks. */
}

- (void)notificationViewDidDisappear:(UIView*)view
{
  /* Do nothing here, subclasses can override this method to perform additional tasks. */
}

@end
