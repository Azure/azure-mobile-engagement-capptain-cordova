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

#import "CPContentViewController.h"
#import "CPViewControllerUtil.h"
#import "CPInteractiveContent.h"

#define BAR_BUTTON_WIDTH 148.0f

@implementation CPContentViewController

- (CPInteractiveContent*)content
{
  /* This is an abstract method */
  [NSException raise:NSInternalInconsistencyException format:@"You must override %@ in a subclass",
   NSStringFromSelector(_cmd)];
  return nil;
}

- (void)exit
{
  [[self content] exitContent];
  [CPViewControllerUtil dismissViewController:self];
}

- (void)loadToolbar:(UIToolbar*)toolbar
{
  CPInteractiveContent* content = [self content];

  /* Hide ok and/or cancel buttons if associated labels are empty */
  BOOL showAction = [content.actionLabel length] > 0;
  BOOL showExit = [content.exitLabel length] > 0;

  /* Set action button label */
  UIBarButtonItem* actionButton =
    [[UIBarButtonItem alloc] initWithTitle:content.actionLabel style:UIBarButtonItemStyleBordered target:self action:
     @selector(actionButtonClicked:)];
  actionButton.width = BAR_BUTTON_WIDTH;
  [self actionButtonLoaded:actionButton];

  /* Set exit button label */
  UIBarButtonItem* exitButton =
    [[UIBarButtonItem alloc] initWithTitle:content.exitLabel style:UIBarButtonItemStyleBordered target:self action:
     @selector(exitButtonClicked:)];
  exitButton.width = BAR_BUTTON_WIDTH;
  [self exitButtonLoaded:exitButton];

  /* We need flexible spaces at the head and tail of the button toolbar */
  UIBarButtonItem* flexibleHead =
    [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
  UIBarButtonItem* flexibleTail =
    [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];

  /* Add needed items in the toolbar */
  NSMutableArray* items = [NSMutableArray array];
  [items addObject:flexibleHead];
  if (showExit)
    [items addObject:exitButton];
  if (showAction)
    [items addObject:actionButton];
  [items addObject:flexibleTail];

  [flexibleHead release];
  [flexibleTail release];
  [actionButton release];
  [exitButton release];

  [toolbar setItems:items];
}

- (void)actionButtonClicked:(id)sender
{
  /* Sub-classes should reimplement this method */
}

- (void)exitButtonClicked:(id)sender
{
  [self exit];
}

- (void)actionButtonLoaded:(UIBarButtonItem*)actionButton
{
  /* Nothing to do, let sub-classes reimplement this method to customize the button. */
}

- (void)exitButtonLoaded:(UIBarButtonItem*)exitButton
{
  /* Nothing to do, let sub-classes reimplement this method to customize the button. */
}

@end
