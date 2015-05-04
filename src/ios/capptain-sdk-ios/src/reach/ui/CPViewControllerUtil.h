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

/**
 * The `CPViewControllerUtil` provides utility methods to present and dismiss view controllers on screen.
 */
@interface CPViewControllerUtil : NSObject

/**
 * Present the controller's view inside the key window using a vetical cover animation:
 * The view slides up from the bottom of the screen.
 * The given controller will be retained until method <dismissViewController:> is called.
 * @param controller The view controller to present.
 */
+ (void)presentViewController:(UIViewController*)controller;

/**
 * Dismiss the given view controller. Remove the view from it's parent using a vertical slide animation.
 * The controller is released.
 * @param controller The view controller to dismiss.
 * @see dismissViewController:animated:
 */
+ (void)dismissViewController:(UIViewController*)controller;

/**
 * Dismiss the given view controller.
 * The controller is released.
 * @param controller The view controller to dismiss
 * @param animated If YES, animates the view; otherwise, does not.
 * @see dismissViewController:
 */
+ (void)dismissViewController:(UIViewController*)controller animated:(BOOL)animated;

/**
 * Get an available window for this application.
 */
+ (UIWindow*)availableWindow;

@end
