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

/**
 * The `CapptainUtils` class provides utility methods used by Capptain view controllers.
 */
@interface CapptainUtils : NSObject

/**
 * Build a Capptain alias for an iOS ViewController class. This implementation takes the simple
 * name of the class and removes the "ViewController" suffix if any (e.g. "MainViewController"
 * becomes "Main").<br/>
 * This method is used by <CapptainViewController> and <CapptainTableViewController>.
 * @param clazz The class to parse.
 * @result An activity name suitable to be reported by the Capptain service.
 */
+ (NSString*)buildCapptainActivityName:(Class)clazz;

@end
