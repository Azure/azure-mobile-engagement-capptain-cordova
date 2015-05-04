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

import java.util.TimeZone;

import org.w3c.dom.Element;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ubikod.capptain.android.sdk.CapptainAgent;

/** Abstract class for reach content such as announcements and polls. */
public abstract class CapptainReachContent
{
  /** Local identifier (which is a SQLite auto-increment) */
  private long mLocalID;

  /** Service address that send the content */
  private final String mJID;

  /** Raw XML to store in SQLite */
  private final String mXML;

  /** Backend's id */
  private final String mID;

  /** Category, null for default */
  final String mCategory;

  /** Body */
  String mBody;

  /** Expiry timestamp (ms since epoch), if any */
  private Long mExpiry;

  /** Expiry local time zone flag */
  private boolean mExpiryIsLocalTZ;

  /** Intent that must be launched for viewing this content */
  private Intent mIntent;

  /** True if replied, thus processed entirely, to avoid making that several times */
  private boolean mProcessed;

  /** Download identifier */
  private Long mDownloadId;

  /**
   * Parse an announcement.
   * @param jid service that sent the announcement.
   * @param xml raw XML of announcement to store in SQLite.
   * @param root parsed XML root DOM element.
   */
  CapptainReachContent(String jid, String rawXml, Element root)
  {
    /* Parse base fields */
    mJID = jid;
    mXML = rawXml;
    mID = XmlUtil.getAttribute(root, "id");
    mCategory = XmlUtil.getAttribute(root, "category");
    mBody = XmlUtil.getTagText(root, "body", null);

    /* Expiry part */
    Element expiry = XmlUtil.getTag(root, "expiry", null);
    if (expiry != null)
    {
      String expiryString = XmlUtil.getText(expiry);
      if (expiryString != null)
      {
        mExpiry = Long.parseLong(expiryString);
        mExpiryIsLocalTZ = XmlUtil.getBooleanAttribute(expiry, "localtz", false);
      }
    }
  }

  /** Get root XML tag name for this content */
  abstract String getRootTag();

  /**
   * Get local identifier.
   * @return the local identifier.
   */
  public long getLocalId()
  {
    return mLocalID;
  }

  /**
   * Set the local identifier.
   * @param localId the local identifier.
   */
  void setLocalId(long localId)
  {
    /* Remember id */
    mLocalID = localId;

    /*
     * We were waiting for this identifier to build the intent (notification announcements have no
     * intents).
     */
    mIntent = buildIntent();
    if (mIntent != null)
      CapptainReachAgent.setContentIdExtra(mIntent, this);
  }

  /** @return service address that send the content */
  String getJID()
  {
    return mJID;
  }

  /** @return raw XML to store in SQLite */
  String getXML()
  {
    return mXML;
  }

  /** @return backend's id */
  String getId()
  {
    return mID;
  }

  /**
   * Get the category of this content. This category is also added in the intent that launches the
   * viewing activity if any. If this method returns <tt>null</tt>, the category list in the intent
   * will only contain <tt>android.intent.category.DEFAULT</tt>.
   * @return category.
   */
  public String getCategory()
  {
    return mCategory;
  }

  /**
   * Get content's body.
   * @return content's body.
   */
  public String getBody()
  {
    return mBody;
  }

  /**
   * Get the base intent to launch to view this content. The intent will be filtered with a
   * component name.
   * @return the base intent to launch to view this content.
   */
  public Intent getIntent()
  {
    return mIntent;
  }

  /**
   * Build the intent to launch to view this content.
   * @return the intent to launch to view this content.
   */
  abstract Intent buildIntent();

  /**
   * Get content expiration date (in ms since epoch).
   * @return content expiration date or null if not specified.
   */
  public Long getExpiry()
  {
    /* Return null if not specified */
    if (mExpiry == null)
      return null;

    /* Convert to local date if local time zone must be used (relative end date) */
    long expiry = mExpiry;
    if (mExpiryIsLocalTZ)
      expiry -= TimeZone.getDefault().getOffset(mExpiry);
    return expiry;
  }

  /**
   * Check if this content is now expired.
   * @return true iff this content is expired now.
   */
  public boolean hasExpired()
  {
    /* Never expires if date not specified */
    Long expiry = getExpiry();
    if (expiry == null)
      return false;

    /* Compare */
    return System.currentTimeMillis() >= expiry;
  }

  /**
   * Drop content.
   * @param context any application context.
   */
  public void dropContent(Context context)
  {
    process(context, "dropped", null);
  }

  /**
   * Report content has been actioned.
   * @param context any application context.
   */
  public void actionContent(Context context)
  {
    process(context, "content-actioned", null);
  }

  /**
   * Report content been exited.
   * @param context any application context.
   */
  public void exitContent(Context context)
  {
    process(context, "content-exited", null);
  }

  /**
   * Check whether this content has a system notification.
   * @return true iff the content has a system notification.
   */
  public boolean isSystemNotification()
  {
    /* By default */
    return false;
  }

  /**
   * Send feedback to Reach about this content.
   * @param context application context.
   * @param status feedback status.
   * @param extras extra information like poll answers.
   */
  void sendFeedback(Context context, String status, Bundle extras)
  {
    /* Don't send feedback if replyto is empty (typically test content) */
    if (mJID != null)

      /* Reply to the Reach service that sent the announcement */
      CapptainAgent.getInstance(context).sendReachFeedback(getRootTag(), mID, status, extras);
  }

  /**
   * Dispose of this content so that new content can be notified. Possibly send feedback to the
   * service that sent it.
   * @param context application context.
   * @param status feedback status if any (null not to send anything).
   * @param extras extra information like poll answers.
   */
  void process(Context context, String status, Bundle extras)
  {
    /* Do it once */
    if (!mProcessed)
    {
      /* Send feedback if any */
      if (status != null)
        sendFeedback(context, status, extras);

      /* Mark this announcement as processed */
      mProcessed = true;

      /* Tell the reach application manager that announcement has been processed */
      CapptainReachAgent.getInstance(context).onContentProcessed(this);
    }
  }

  /**
   * Set content status.
   * @param values values from storage.
   */
  void setState(ContentValues values)
  {
    mDownloadId = values.getAsLong(ContentStorage.DOWNLOAD_ID);
  }

  /**
   * Get download identifier if a download has ever been scheduled for this content.
   * @return download identifier or null if download has never been scheduled.
   */
  public Long getDownloadId()
  {
    return mDownloadId;
  }

  /**
   * Set download identifier for this content.
   * @param context any application context.
   * @param downloadId download identifier.
   */
  public void setDownloadId(Context context, long downloadId)
  {
    mDownloadId = downloadId;
    CapptainReachAgent.getInstance(context).onDownloadScheduled(this, downloadId);
  }
}
