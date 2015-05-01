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

#import "CPViewControllerUtil.h"
#import "CPAutorotateView.h"

#define ANIMATION_DURATION 0.4f

@implementation CPViewControllerUtil

static NSMutableArray* currentDisplayedControllers;

+ (void)initialize
{
  static BOOL initialized = NO;
  if (!initialized)
  {
    currentDisplayedControllers = [[NSMutableArray alloc] init];
    initialized = YES;
  }
}

+ (void)presentViewController:(UIViewController*)controller
{
  CPAutorotateView* container = [[CPAutorotateView alloc] initWithContent:controller.view];

  /* Add view to the current window */
  [[[self class] availableWindow] addSubview:container];
  [container release];

  /* Animate opening */
  container.transform = CGAffineTransformTranslate(container.transform, 0.0, container.frame.size.height);
  [UIView beginAnimations:nil context:UIGraphicsGetCurrentContext()];
  [UIView setAnimationDuration:ANIMATION_DURATION];
  container.transform = CGAffineTransformTranslate(container.transform, 0.0, -container.frame.size.height);
  [UIView commitAnimations];

  /* Keep track of the controller */
  [currentDisplayedControllers addObject:controller];
}

+ (void)dismissViewController:(UIViewController*)controller
{
  [self dismissViewController:controller animated:YES];
}

+ (void)dismissViewController:(UIViewController*)controller animated:(BOOL)animated
{
  if ([controller.view.superview isKindOfClass:[CPAutorotateView class]])
  {
    UIView* view = controller.view.superview;
    [controller dismissModalViewControllerAnimated:NO];

    if (animated)
    {
      /* Animate hiding */
      [UIView beginAnimations:nil context:UIGraphicsGetCurrentContext()];
      [UIView setAnimationDuration:ANIMATION_DURATION];
      view.transform = CGAffineTransformTranslate(view.transform, 0.0, view.frame.size.height);
      [UIView commitAnimations];

      /* Remove from view hierarchy at the end of the animation */
      [view performSelector:@selector(removeFromSuperview) withObject:nil afterDelay:ANIMATION_DURATION];
    } else
    {
      /* Remove from view hierarchy */
      [view removeFromSuperview];
    }
  }

  /* Remove controller from dictionnary */
  [currentDisplayedControllers removeObject:controller];
}

+ (UIWindow*)availableWindow
{
  /* Key window is the best choice */
  UIWindow* keyW = [[UIApplication sharedApplication] keyWindow];
  if ([[self class] canUseWindow:keyW])
    return keyW;

  /* Key window cannot be used, find another one */
  else
    for (UIWindow* w in[[UIApplication sharedApplication] windows])
      if ([[self class] canUseWindow:w])
        return w;

  /* Fallback on the first window */
  return ([UIApplication sharedApplication].windows)[0];
}

/* Return YES if the window can be used to display the notification */
+ (BOOL)canUseWindow:(UIWindow*)win
{
  return win != nil && win.windowLevel == UIWindowLevelNormal;
}

@end
