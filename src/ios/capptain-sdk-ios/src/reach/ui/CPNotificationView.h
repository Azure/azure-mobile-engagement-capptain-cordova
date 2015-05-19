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
 * A `CPNotificationView` object represnts a view that is responsible for displaying reach notifications used by the
 * default notifier <CPDefaultNotifier>.
 *
 * This view is using the layout named `CPDefaultNotificationView.xib`.
 * You can change the xib file to your needs as long as you keep view identifier and types.<br>
 * This class overrides the method `layoutSubviews` to move and resize subviews when some of them are hidden.
 */
@interface CPNotificationView : UIView
{
  @private
  UILabel* _titleView;
  UILabel* _messageView;
  UIImageView* _iconView;
  UIImageView* _imageView;
  UIButton* _notificationButton;
  UIButton* _closeButton;
}

/** Returns the title view of the notification. */
@property(nonatomic, readonly) UILabel* titleView;

/** Returns the message view of the notification. */
@property(nonatomic, readonly) UILabel* messageView;

/** Returns the icon view of the notification. */
@property(nonatomic, readonly) UIImageView* iconView;

/** Returns the image view of the notification. */
@property(nonatomic, readonly) UIImageView* imageView;

/** Returns the main button of the notification. */
@property(nonatomic, readonly) UIButton* notificationButton;

/** Returns the close button of the notification. */
@property(nonatomic, readonly) UIButton* closeButton;

@end
