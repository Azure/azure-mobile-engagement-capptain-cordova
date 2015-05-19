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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ubikod.capptain.android.sdk.CapptainAgent;
import com.ubikod.capptain.android.sdk.CapptainAgentUtils;

/**
 * Helper class used to replace Android's android.app.ListActivity class.
 */
public abstract class CapptainListActivity extends ListActivity
{
  private CapptainAgent mCapptainAgent;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    mCapptainAgent = CapptainAgent.getInstance(this);

    /* FIXME temporary empty adapter to avoid side effects with Reach */
    if (getListAdapter() == null)
    {
      /* This will trigger required initialization */
      setListAdapter(new BaseAdapter()
      {
        public View getView(int position, View convertView, ViewGroup parent)
        {
          return null;
        }

        public long getItemId(int position)
        {
          return 0;
        }

        public Object getItem(int position)
        {
          return null;
        }

        public int getCount()
        {
          return 0;
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
