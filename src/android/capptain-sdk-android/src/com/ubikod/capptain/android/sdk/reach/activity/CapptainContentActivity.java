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

package com.ubikod.capptain.android.sdk.reach.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ubikod.capptain.android.sdk.activity.CapptainActivity;
import com.ubikod.capptain.android.sdk.reach.CapptainReachAgent;
import com.ubikod.capptain.android.sdk.reach.CapptainReachInteractiveContent;
import com.ubikod.capptain.utils.ResourcesUtils;

/**
 * Base class for all activities displaying Reach content.
 */
public abstract class CapptainContentActivity<T extends CapptainReachInteractiveContent> extends
  CapptainActivity
{
  /** Content of this activity */
  protected T mContent;

  /** Action button */
  protected TextView mActionButton;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    /* No title section on the top */
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    /* Get content */
    mContent = CapptainReachAgent.getInstance(this).getContent(getIntent());
    if (mContent == null)
    {
      /* If problem with content, exit */
      finish();
      return;
    }

    /* Inflate layout */
    setContentView(getLayoutId(getLayoutName()));

    /* Set title */
    TextView titleView = getView("title");
    String title = mContent.getTitle();
    if (title != null)
      titleView.setText(title);
    else
      titleView.setVisibility(View.GONE);

    /* Set body */
    setBody(mContent.getBody(), getView("body"));

    /* Action button */
    mActionButton = getView("action");
    String actionLabel = mContent.getActionLabel();
    if (actionLabel != null)
    {
      mActionButton.setText(actionLabel);
      mActionButton.setOnClickListener(new OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          /* Action the content */
          action();
        }
      });
    }

    /* No action label means no action button */
    else
      mActionButton.setVisibility(View.GONE);

    /* Exit button */
    Button exitButton = getView("exit");
    String exitLabel = mContent.getExitLabel();
    if (exitLabel != null)
    {
      exitButton.setText(exitLabel);
      exitButton.setOnClickListener(new OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          /* Exit the content */
          exit();
        }
      });
    }

    /* No exit label means no exit button */
    else
      exitButton.setVisibility(View.GONE);

    /* Hide spacers if only one button is visible (or none) */
    ViewGroup layout = getView("capptain_button_bar");
    boolean hideSpacer = actionLabel == null || exitLabel == null;
    for (int i = 0; i < layout.getChildCount(); i++)
    {
      View view = layout.getChildAt(i);
      if ("spacer".equals(view.getTag()))
        if (hideSpacer)
          view.setVisibility(View.VISIBLE);
        else
          view.setVisibility(View.GONE);
    }

    /* Hide button bar if both action and exit buttons are hidden */
    if (actionLabel == null && exitLabel == null)
      layout.setVisibility(View.GONE);
  }

  @Override
  protected void onResume()
  {
    /* Mark the content displayed */
    mContent.displayContent(this);
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    if (isFinishing() && mContent != null)
    {
      /*
       * Exit content on exit, this is has no effect if another process method has already been
       * called so we don't have to check anything here.
       */
      mContent.exitContent(this);
    }
    super.onPause();
  }

  @Override
  protected void onUserLeaveHint()
  {
    finish();
  }

  /**
   * Render the body of the content into the specified view.
   * @param body content body.
   * @param view body view.
   */
  protected void setBody(String body, View view)
  {
    TextView textView = (TextView) view;
    textView.setText(body);
  }

  /**
   * Get resource identifier.
   * @param name resource name.
   * @param defType resource type like "layout" or "id".
   * @return resource identifier or 0 if not found.
   */
  protected int getId(String name, String defType)
  {
    return ResourcesUtils.getId(this, name, defType);
  }

  /**
   * Get layout identifier by its resource name.
   * @param name layout resource name.
   * @return layout identifier or 0 if not found.
   */
  protected int getLayoutId(String name)
  {
    return getId(name, "layout");
  }

  /**
   * Get identifier by its resource name.
   * @param name identifier resource name.
   * @return identifier or 0 if not found.
   */
  protected int getId(String name)
  {
    return getId(name, "id");
  }

  /**
   * Get a view by its resource name.
   * @param name view identifier resource name.
   * @return view or 0 if not found.
   */
  @SuppressWarnings("unchecked")
  protected <V extends View> V getView(String name)
  {
    return (V) findViewById(getId(name));
  }

  /**
   * Get layout resource name corresponding to this activity.
   * @return layout resource name corresponding to this activity.
   */
  protected abstract String getLayoutName();

  /** Execute the action if any of the content, report it and finish the activity */
  protected void action()
  {
    /* Delegate action */
    onAction();

    /* And quit */
    finish();
  }

  /** Exit the content and report it */
  protected void exit()
  {
    /* Report exit */
    mContent.exitContent(getApplicationContext());

    /* And quit */
    finish();
  }

  /**
   * Called when the action button is clicked.
   */
  protected abstract void onAction();
}
