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

package com.ubikod.capptain.android.sdk.reach;

import static com.ubikod.capptain.android.sdk.reach.CapptainReachAgent.REACH_NAMESPACE;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Capptain Poll abstraction.
 */
public class CapptainPoll extends CapptainReachInteractiveContent
{
  /** Intent poll action used by the reach SDK. */
  public static final String INTENT_ACTION = "com.ubikod.capptain.intent.action.POLL";

  /** Questions as a bundle */
  private final JSONArray mQuestions;

  /** Answer form */
  private final Bundle mAnswers;

  /**
   * Parse an announcement.
   * @param jid service that sent the announcement.
   * @param xml raw XML of announcement to store in SQLite.
   * @param root parsed XML root DOM element.
   * @throws JSONException if a parsing error occurs.
   */
  CapptainPoll(String jid, String xml, Element root) throws JSONException
  {
    super(jid, xml, root);
    mAnswers = new Bundle();
    mQuestions = new JSONArray();
    NodeList questions = root.getElementsByTagNameNS(REACH_NAMESPACE, "question");
    for (int i = 0; i < questions.getLength(); i++)
    {
      Element questionE = (Element) questions.item(i);
      NodeList choicesN = questionE.getElementsByTagNameNS(REACH_NAMESPACE, "choice");
      JSONArray choicesJ = new JSONArray();
      for (int j = 0; j < choicesN.getLength(); j++)
      {
        Element choiceE = (Element) choicesN.item(j);
        JSONObject choiceJ = new JSONObject();
        choiceJ.put("id", choiceE.getAttribute("id"));
        choiceJ.put("title", XmlUtil.getText(choiceE));
        choiceJ.put("default", Boolean.parseBoolean(choiceE.getAttribute("default")));
        choicesJ.put(choiceJ);
      }
      JSONObject questionJ = new JSONObject();
      questionJ.put("id", questionE.getAttribute("id"));
      questionJ.put("title", XmlUtil.getTagText(questionE, "title", null));
      questionJ.put("choices", choicesJ);
      mQuestions.put(questionJ);
    }
  }

  @Override
  String getRootTag()
  {
    return "poll";
  }

  @Override
  Intent buildIntent()
  {
    Intent intent = new Intent(INTENT_ACTION);
    String category = getCategory();
    if (category != null)
      intent.addCategory(category);
    return intent;
  }

  /**
   * Get questions for this poll as a JSON array. Each question is a JSON object with the following
   * structure:
   * <ul>
   * <li>"id" -> String</li>
   * <li>"title" -> String</li>
   * <li>"choices" -> JSONArray
   * <ul>
   * <li>"id" -> String
   * <li>"title" -> String</li>
   * <li>"default" -> boolean</li>
   * </ul>
   * </ul>
   * </li> </ul> </li> </ul>
   * @return questions definition.
   */
  public JSONArray getQuestions()
  {
    return mQuestions;
  }

  /**
   * Fill answer for a given question. Answers are sent when calling {@link #actionContent(Context)}
   * .
   * @param questionId question id as specified in the Bundle returned by {@link #getQuestions()}.
   * @param choiceId choice id as specified in the Bundle returned by {@link #getQuestions()}.
   */
  public void fillAnswer(String questionId, String choiceId)
  {
    mAnswers.putString(questionId, choiceId);
  }

  @Override
  public void actionContent(Context context)
  {
    process(context, "content-actioned", mAnswers);
  }
}
