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

#import "CPWebAnnouncementJsBridge.h"

static NSString* const kBridgeScript = @""
                                       "var capptainReachContent = {"
                                       "  actionContent: function() {"
                                       "    window.location.href = 'capptain://reach/action';"
                                       "  },"
                                       "  exitContent: function() {"
                                       "    window.location.href = 'capptain://reach/exit';"
                                       "  }"
                                       "};";

@implementation CPWebAnnouncementJsBridge

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Memory management
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

+ (id)jsBridgeWithDelegate:(id<CPWebAnnouncementActionDelegate>)delegate
{
  return [[[self alloc] initWithDelegate:delegate] autorelease];
}

- (id)initWithDelegate:(id<CPWebAnnouncementActionDelegate>)delegate
{
  self = [super init];
  if (self)
  {
    _delegate = [delegate retain];
  }

  return self;
}

- (void)dealloc
{
  [_delegate release];
  [super dealloc];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark UIWebViewDelegate methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)webViewDidFinishLoad:(UIWebView*)webView
{
  [webView stringByEvaluatingJavaScriptFromString:kBridgeScript];
}

- (BOOL)webView:(UIWebView*)webView shouldStartLoadWithRequest:(NSURLRequest*)request navigationType:(
    UIWebViewNavigationType)navigationType
{
  NSURL* url = [request URL];
  if ([[url scheme] isEqualToString:@"capptain"])
  {
    NSString* command = [url lastPathComponent];
    if ([[url host] isEqualToString:@"reach"])
    {
      SEL commandSel = NSSelectorFromString(command);
      if ([_delegate respondsToSelector:commandSel])
        [_delegate performSelector:commandSel];
    }
    return NO;
  } else
    return YES;
}

@end
