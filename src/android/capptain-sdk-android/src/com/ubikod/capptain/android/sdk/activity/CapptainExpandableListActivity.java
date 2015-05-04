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

package com.ubikod.capptain.android.sdk.activity;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.CapptainAgentUtils;

/**
 * Helper class used to replace Android's android.app.ExpandableListActivity
 * class.
 */
public abstract class CapptainExpandableListActivity extends ExpandableListActivity
{
  private CapptainAgent mCapptainAgent;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mCapptainAgent = CapptainAgent.getInstance(this);

    /* FIXME temporary empty adapter to avoid side effects with Reach */
    if (getExpandableListAdapter() == null)
    {
      /* This will trigger required initialization */
      setListAdapter(new BaseExpandableListAdapter()
      {
        public boolean isChildSelectable(int groupPosition, int childPosition)
        {
          return false;
        }

        public boolean hasStableIds()
        {
          return false;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
          ViewGroup parent)
        {
          return null;
        }

        public long getGroupId(int groupPosition)
        {
          return 0;
        }

        public int getGroupCount()
        {
          return 0;
        }

        public Object getGroup(int groupPosition)
        {
          return null;
        }

        public int getChildrenCount(int groupPosition)
        {
          return 0;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
          View convertView, ViewGroup parent)
        {
          return null;
        }

        public long getChildId(int groupPosition, int childPosition)
        {
          return 0;
        }

        public Object getChild(int groupPosition, int childPosition)
        {
          return null;
        }
      });

      /*
       * We can now safely reset the adapter to null to avoid side effect with
       * 3rd party code testing the null pointer.
       */
      setListAdapter(null);
    }
  }

  @Override
  protected void onResume()
  {
    mCapptainAgent.startActivity(this, getCapptainActivityName(), getCapptainActivityExtra());
    super.onResume();
  }

  @Override
  protected void onPause()
  {
    mCapptainAgent.endActivity();
    super.onPause();
  }

  /**
   * Get the Capptain agent attached to this activity.
   * @return the Capptain agent
   */
  public final CapptainAgent getCapptainAgent()
  {
    return mCapptainAgent;
  }

  /**
   * Override this to specify the name reported by your activity. The default
   * implementation returns the simple name of the class and removes the
   * "Activity" suffix if any (e.g. "com.mycompany.MainActivity" -> "Main").
   * @return the activity name reported by the Capptain service.
   */
  protected String getCapptainActivityName()
  {
    return CapptainAgentUtils.buildCapptainActivityName(getClass());
  }

  /**
   * Override this to attach extra information to your activity. The default
   * implementation attaches no extra information (i.e. return null).
   * @return activity extra information, null or empty if no extra.
   */
  protected Bundle getCapptainActivityExtra()
  {
    return null;
  }
}
