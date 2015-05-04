/*
 * Copyright (c) Microsoft Corporation.  All rights reserved.
 * Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.
 */

#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>
#import "Capptain.h"
#import "CapptainAgent.h"
#import "CPReachModule.h"
#import "CPPushDelegate.h"
#import "AppDelegate.h"

@interface AppDelegate (Capptain)
- (void)application:(UIApplication *)application customdidRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
- (void)application:(UIApplication *)application customdidFailToRegisterForRemoteNotificationsWithError:(NSError *)error;
- (void)application:(UIApplication *)application customdidReceiveRemoteNotification:(NSDictionary *)userInfo;
@end

@interface Capptain : CDVPlugin <CPPushDelegate>
{
    NSString* capptainAppId ;
    NSString* capptainAppKey;
    BOOL enableLog;
    NSString* capptainReachIcon;
}

- (void)startActivity:(CDVInvokedUrlCommand*)command;
- (void)endActivity:(CDVInvokedUrlCommand*)command;
- (void)sendAppInfo:(CDVInvokedUrlCommand*)command;
- (void)sendEvent:(CDVInvokedUrlCommand*)command;
- (void)startJob:(CDVInvokedUrlCommand*)command;
- (void)endJob:(CDVInvokedUrlCommand*)command;
- (void)checkRedirect:(CDVInvokedUrlCommand*)command;
- (void)getStatus:(CDVInvokedUrlCommand*)command;
- (void)handleOpenURL:(NSNotification*)notification;


@end
