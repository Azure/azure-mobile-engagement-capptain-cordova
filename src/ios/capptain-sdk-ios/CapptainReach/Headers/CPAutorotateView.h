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
 * A view intended to be presented on the current application window.
 *
 * Inside a window, only the first subview gets rotation events. To get around this problem,
 * this view listen to device orientation changes and resize + apply the proper transform on the view.
 *
 * This view can be used as a container to another view by using the initialization method <initWithContent:>
 * or it can be used as a replacement to the `UIView` class.
 */
@interface CPAutorotateView : UIView {
  @private
  UIView* _view;
  UIInterfaceOrientation _orientation;
}

/**
 * Initialize the view by using a child content.
 * This view will just be used as a wrapper to the provided view.
 * @param view The view to wrap.
 */
- (id)initWithContent:(UIView*)view;

/**
 * Transform to apply on the view based on the current device orientation.
 * The default implementation will return an affine transformation matrix constructed from the provided orientation.<br>
 * @param orientation Orientation that will be applied.
 * @result The new `CGAffineTransform` to apply on this view.
 */
- (CGAffineTransform)transformForOrientation:(UIInterfaceOrientation)orientation;

@end
