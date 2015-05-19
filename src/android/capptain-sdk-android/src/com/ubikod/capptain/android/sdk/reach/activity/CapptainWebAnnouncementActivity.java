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

import android.annotation.SuppressLint;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Activity displaying a web Capptain announcement. Add this in the AndroidManifest.xml file to use
 * it:
 * 
 * <pre>
 * {@code <activity
 *   android:name="com.ubikod.capptain.android.sdk.reach.activity.CapptainWebAnnouncementActivity"
 *   android:theme="@android:style/Theme.Light">
 *     <intent-filter>
 *       <action android:name="com.ubikod.capptain.intent.action.ANNOUNCEMENT" />
 *       <category android:name="android.intent.category.DEFAULT" />
 *       <data android:mimeType="text/html" />
 *     </intent-filter>
 * </activity>}
 * </pre>
 */
public class CapptainWebAnnouncementActivity extends CapptainAnnouncementActivity
{
  @Override
  protected String getLayoutName()
  {
    return "capptain_web_announcement";
  }

  /**
   * Interface that is bound to the JavasScript object named "capptainReachContent" object.
   */
  protected class CapptainReachContentJS
  {
    /** Web view */
    private final WebView mWebView;

    /**
     * Init.
     * @param webView web view.
     */
    protected CapptainReachContentJS(WebView webView)
    {
      mWebView = webView;
    }

    /**
     * Called by web view's JavaScript function capptainReachContent.actionContent() (not in the U.I
     * thread).
     */
    @JavascriptInterface
    public void actionContent()
    {
      mWebView.post(new Runnable()
      {
        @Override
        public void run()
        {
          action();
        }
      });
    }

    /**
     * Called by web view's JavaScript function capptainReachContent.exitContent() (not in the U.I
     * thread).
     */
    @JavascriptInterface
    public void exitContent()
    {
      mWebView.post(new Runnable()
      {
        @Override
        public void run()
        {
          exit();
        }
      });
    }
  }

  @Override
  @SuppressLint("SetJavaScriptEnabled")
  protected void setBody(String body, View bodyView)
  {
    /* Init web view with JavaScript enabled */
    WebView webView = (WebView) bodyView;
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebViewClient(new WebViewClient()
    {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url)
      {
        try
        {
          /* Launch activity */
          executeActionURL(url);

          /* Report action on success */
          onAction();
          return true;
        }
        catch (Exception e)
        {
          /* If it fails, fail over default behavior */
          return false;
        }
      }
    });

    /* Bind methods for the content */
    webView.addJavascriptInterface(new CapptainReachContentJS(webView), "capptainReachContent");

    /*
     * Render HTML. The loadData method won't work with some characters since Android 2.0, we use
     * loadDataWithBaseURL instead.
     */
    webView.loadDataWithBaseURL(null, body, "text/html", "utf-8", null);
  }
}
