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

#import "CPDefaultPollViewController.h"
#import "CPReachPoll.h"
#import "CPReachPollQuestion.h"
#import "CPViewControllerUtil.h"

#define FONT_SIZE 17.0f
#define CELL_CONTENT_WIDTH 300.0f
#define CELL_CONTENT_MARGIN 10.0f

@implementation CPDefaultPollViewController

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Memory management
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (id)initWithPoll:(CPReachPoll*)reachPoll
{
  self = [super init];
  if (self != nil)
  {
    /* Keep poll */
    self.poll = reachPoll;

    /* Body is empty or not */
    _hasBody = [self.poll.body length] > 0;

    /* Keep user selected choices in a dictionnary */
    _selectedChoices = [[NSMutableDictionary alloc] init];
  }
  return self;
}

- (void)viewDidLoad
{
  [super viewDidLoad];

  /* Init poll's title */
  self.titleBar.topItem.title = self.poll.title;

  /* Init toolbar */
  [self loadToolbar:self.toolbar];

  /* Init default choices */
  for (CPReachPollQuestion* question in self.poll.questions)
  {
    for (int i = 0; i < [question.choices count]; i++)
    {
      CPReachPollChoice* choice = (question.choices)[i];
      if (choice.isDefault)
        _selectedChoices[question.questionId] = @(i);
    }
  }

  /* Enable submit button if all questions have been answered */
  [self updateSubmitButtonState];

  /* Reload table */
  [self.tableView reloadData];
}

- (void)actionButtonLoaded:(UIBarButtonItem*)actionButton
{
  self.submitButton = actionButton;
  [self updateSubmitButtonState];
}

- (void)dealloc
{
  self.titleBar = nil;
  self.tableView = nil;
  self.toolbar = nil;
  self.submitButton = nil;
  [_selectedChoices release];
  [super dealloc];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Orientation changes
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
  return YES;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Actions
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (void)actionButtonClicked:(id)sender
{
  /* Retrieve submitted answers */
  NSMutableDictionary* answers = [[NSMutableDictionary alloc] init];
  for (CPReachPollQuestion* question in self.poll.questions)
  {
    NSNumber* selectedChoice = _selectedChoices[question.questionId];
    if (selectedChoice)
    {
      CPReachPollChoice* choice = question.choices[[selectedChoice intValue]];
      answers[question.questionId] = choice.choiceId;
    }
  }

  /* Submit answers */
  if ([answers count] > 0)
    [self submitAnswers:answers];

  /* Release allocated object */
  [answers release];
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark Private methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/* Enable the submit button when all questions have been answered. Otherwise, the button stays disabled.  */
- (void)updateSubmitButtonState
{
  self.submitButton.enabled = ([_selectedChoices count] == [self.poll.questions count]);
}

/**
 * Retrieve a poll's choice from a given index path.
 * @param indexPath Index path in the table view.
 */
- (CPReachPollChoice*)choiceFromIndexPath:(NSIndexPath*)indexPath
{
  CPReachPollChoice* choice = ([self questionFromSection:indexPath.section].choices)[indexPath.row];
  return choice;
}

/**
 * Retieve a poll's question from a given table view section.
 * @param section Index of the section in the table view.
 */
- (CPReachPollQuestion*)questionFromSection:(NSInteger)section
{
  NSInteger index = section - (_hasBody ? 1 : 0);
  CPReachPollQuestion* question = (self.poll.questions)[index];
  return question;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
#pragma mark TableView methods
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

- (NSInteger)numberOfSectionsInTableView:(UITableView*)tableView
{
  return [self.poll.questions count] + (_hasBody ? 1 : 0);
}

- (CGFloat)tableView:(UITableView*)tableView heightForRowAtIndexPath:(NSIndexPath*)indexPath
{
  NSString* text = nil;
  CPReachPollChoice* choice = [self choiceFromIndexPath:indexPath];
  text = choice.title;

  /* Compute row height  */
  CGSize constraint = CGSizeMake(CELL_CONTENT_WIDTH - (CELL_CONTENT_MARGIN * 2), 20000.0f);
  CGSize size = [text sizeWithFont:[UIFont systemFontOfSize:FONT_SIZE]
                 constrainedToSize:constraint lineBreakMode:UILineBreakModeWordWrap];
  CGFloat height = MAX(size.height, 24);
  return height + (CELL_CONTENT_MARGIN * 2);
}

- (NSString*)tableView:(UITableView*)tableView titleForHeaderInSection:(NSInteger)section
{
  if (section == 0 && _hasBody)
    return self.poll.body;
  else
  {
    CPReachPollQuestion* question = [self questionFromSection:section];
    return question.title;
  }
  return nil;
}

/**
 * Customize the number of rows in the table view.
 */
- (NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section
{
  if (section == 0 && _hasBody)
    return 0;
  else
  {
    CPReachPollQuestion* question = [self questionFromSection:section];
    return [question.choices count];
  }
}

/**
 * Customize the appearance of table view cells.
 */
- (UITableViewCell*)tableView:(UITableView*)aTableView cellForRowAtIndexPath:(NSIndexPath*)indexPath
{
  static NSString* cellId = @"CellId";
  UITableViewCell* cell = [self.tableView dequeueReusableCellWithIdentifier:cellId];
  if (!cell)
  {
    cell =
      [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellId] autorelease];
    cell.textLabel.numberOfLines = 5;
    cell.textLabel.lineBreakMode = UILineBreakModeWordWrap;
  }

  CPReachPollQuestion* question = [self questionFromSection:indexPath.section];
  CPReachPollChoice* choice = (question.choices)[indexPath.row];
  NSNumber* selectedChoiceIndex = _selectedChoices[question.questionId];
  cell.accessoryType =
    (selectedChoiceIndex &&
     ([selectedChoiceIndex intValue] ==
      indexPath.row)) ? UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
  cell.textLabel.text = choice.title;
  [cell setSelected:NO animated:NO];

  return cell;
}

- (void)tableView:(UITableView*)aTableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath
{
  /* Get selected row */
  UITableViewCell* cell = [self.tableView cellForRowAtIndexPath:indexPath];
  [cell setSelected:NO animated:NO];

  /* Check selected row is in a section containing choices */
  if (indexPath.section > 0 || !_hasBody)
  {
    /* Retrieve associated question */
    CPReachPollQuestion* question = [self questionFromSection:indexPath.section];

    /* If a choice has been selected for this question, deselect it */
    NSNumber* choiceIndex = _selectedChoices[question.questionId];
    if (choiceIndex)
    {
      /* Remove accessory type for the current selected choice */
      UITableViewCell* selectedCell =
        [self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:[choiceIndex intValue] inSection:indexPath.
                                               section
         ]];
      selectedCell.accessoryType = UITableViewCellAccessoryNone;
    }

    /* Display a checkmark for the new selected choice */
    cell.accessoryType = UITableViewCellAccessoryCheckmark;
    _selectedChoices[question.questionId] = @(indexPath.row);

    /* Enable submit button if all questions have been answered */
    [self updateSubmitButtonState];
  }
}

@end
