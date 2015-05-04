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
 * Helper class used to replace iOS's `UIViewController` class.
 */
@interface CapptainViewController : UIViewController {

}

/**
 * Override this to specify the name reported by your activity. The default implementation returns
 * the simple name of the class and removes the "ViewController" suffix if any (e.g.
 * "Tab1ViewController" -> "Tab1").
 * @result the activity name reported by the Capptain service.
 */
- (NSString*)capptainActivityName;

/**
 * Override this to attach extra information to your activity. The default implementation attaches
 * no extra information (i.e. return nil).
 * @result activity extra information, nil if no extra.
 */
- (NSDictionary*)capptainActivityExtra;

@end
