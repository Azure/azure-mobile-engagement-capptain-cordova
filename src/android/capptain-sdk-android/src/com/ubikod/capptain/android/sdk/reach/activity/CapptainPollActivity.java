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

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.ubikod.capptain.android.sdk.reach.CapptainPoll;

/**
 * Activity displaying a plain text Capptain poll. Add this in the AndroidManifest.xml file to use
 * it:
 * 
 * <pre>
 * {@code <activity
 *   android:name="com.ubikod.capptain.android.sdk.reach.activity.CapptainPollActivity"
 *   android:theme="@android:style/Theme.Light">
 *     <intent-filter>
 *       <action android:name="com.ubikod.capptain.intent.action.POLL"/>
 *       <category android:name="android.intent.category.DEFAULT"/>
 *     </intent-filter>
 * </activity>}
 * </pre>
 */
public class CapptainPollActivity extends CapptainContentActivity<CapptainPoll>
{
  /** Radio button groups */
  private final Collection<RadioGroup> mRadioGroups = new ArrayList<RadioGroup>();

  /** Radio button check watcher */
  private final OnCheckedChangeListener mRadioGroupListener = new OnCheckedChangeListener()
  {
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
      updateActionState();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    /* Init layout */
    super.onCreate(savedInstanceState);

    /* If no content, nothing to do, super class already called finish */
    if (mContent == null)
      return;

    /* Render questions */
    LinearLayout questionsLayout = getView("questions");
    JSONArray questions = mContent.getQuestions();
    LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    try
    {
      for (int i = 0; i < questions.length(); i++)
      {
        /* Get question */
        JSONObject question = questions.getJSONObject(i);

        /* Inflate question layout */
        LinearLayout questionLayout = (LinearLayout) layoutInflater.inflate(
          getLayoutId("capptain_poll_question"), null);

        /* Set question's title */
        TextView questionTitle = (TextView) questionLayout.findViewById(getId("question_title"));
        questionTitle.setText(question.getString("title"));

        /* Set choices */
        RadioGroup choicesView = (RadioGroup) questionLayout.findViewById(getId("choices"));
        choicesView.setTag(question);
        JSONArray choices = question.getJSONArray("choices");
        int choiceViewId = 0;
        for (int j = 0; j < choices.length(); j++)
        {
          /* Get choice */
          JSONObject choice = choices.getJSONObject(j);

          /* Inflate choice layout */
          RadioButton choiceView = (RadioButton) layoutInflater.inflate(
            getLayoutId("capptain_poll_choice"), null);

          /* Each choice is a radio button */
          choiceView.setId(choiceViewId++);
          choiceView.setTag(choice.getString("id"));
          choiceView.setText(choice.getString("title"));
          choiceView.setChecked(choice.getBoolean("default"));
          choicesView.addView(choiceView);
        }

        /* Add to parent layouts */
        questionsLayout.addView(questionLayout);

        /* Watch state */
        mRadioGroups.add(choicesView);
        choicesView.setOnCheckedChangeListener(mRadioGroupListener);
      }
    }
    catch (JSONException jsone)
    {
      /* Won't happen */
    }

    /* Disable action if a choice is not selected */
    updateActionState();
  }

  @Override
  protected String getLayoutName()
  {
    return "capptain_poll";
  }

  @Override
  protected void onAction()
  {
    /* Scan U.I radio button states */
    LinearLayout questionsLayout = getView("questions");
    JSONArray questions = mContent.getQuestions();
    try
    {
      for (int i = 0; i < questions.length(); i++)
      {
        /* Get question */
        JSONObject question = questions.getJSONObject(i);

        /* Get radio group by question */
        String questionId = question.getString("id");
        RadioGroup choicesView = (RadioGroup) questionsLayout.findViewWithTag(question);

        /* Get selected choice id */
        int selectedViewId = choicesView.getCheckedRadioButtonId();
        RadioButton selectedChoiceView = (RadioButton) choicesView.findViewById(selectedViewId);
        String choiceId = selectedChoiceView.getTag().toString();

        /* Fill answer */
        mContent.fillAnswer(questionId, choiceId);
      }
    }
    catch (JSONException jsone)
    {
      /* Won't happen */
    }

    /* Submit answers */
    mContent.actionContent(getApplicationContext());
  }

  /** Enable action button if all choices selected. Disable otherwise. */
  private void updateActionState()
  {
    boolean checked = true;
    for (RadioGroup group : mRadioGroups)
      checked &= group.getCheckedRadioButtonId() != -1;
    mActionButton.setEnabled(checked);
  }
}
