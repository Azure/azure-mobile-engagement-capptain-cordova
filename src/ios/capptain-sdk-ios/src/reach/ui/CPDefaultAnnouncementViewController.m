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

#import "CPDefaultAnnouncementViewController.h"
#import "CPReachAnnouncement.h"

@implementation CPDefaultAnnouncementViewController

- (instancetype)initWithAnnouncement:(CPReachAnnouncement*)anAnnouncement
{
  self = [super init];
  if (self != nil)
  {
    /* Keep announcement */
    self.announcement = anAnnouncement;

    /* Javascript bridge */
    self.jsBridge = [CPWebAnnouncementJsBridge jsBridgeWithDelegate:self];
  }
  return self;
}

- (void)dealloc
{
  [self.webView setDelegate:nil];
  self.jsBridge = nil;
  self.titleBar = nil;
  self.textView = nil;
  self.webView = nil;
  self.toolbar = nil;
  [super dealloc];
}

- (void)viewDidLoad
{

  [super viewDidLoad];

  /* Init toolbar */
  [self loadToolbar:self.toolbar];

  /* Init announcement title */
  self.titleBar.topItem.title = self.announcement.title;
  self.titleBar.hidden = [self.announcement.title length] == 0;

  /* Hide toolbar if both action and label buttons are empty */
  self.toolbar.hidden = [self.announcement.actionLabel length] == 0 && [self.announcement.exitLabel length] == 0;

  /* Move and resize other views accordingly */
  if (self.titleBar.hidden)
  {
    CGRect frame = self.textView.frame;
    frame.origin.y = 0;
    frame.size.height += self.titleBar.frame.size.height;
    self.textView.frame = frame;
    self.webView.frame = frame;
  }
  if (self.toolbar.hidden)
  {
    CGRect frame = self.textView.frame;
    frame.size.height += self.toolbar.frame.size.height;
    self.textView.frame = frame;
    self.webView.frame = frame;
  }

  /* Init announcement body based on type */
  if (self.announcement.type == CPAnnouncementTypeHtml)
  {
    self.textView.hidden = YES;
    self.webView.hidden = NO;
    [self.webView setDelegate:self.jsBridge];
    [self.webView loadHTMLString:self.announcement.body baseURL:[NSURL URLWithString:@"http://localhost/"]];
  } else
  {
    self.textView.hidden = NO;
    self.webView.hidden = YES;
    self.textView.text = self.announcement.body;
  }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
  return YES;
}

- (void)actionButtonClicked:(id)sender
{
  [self action];
}

@end
