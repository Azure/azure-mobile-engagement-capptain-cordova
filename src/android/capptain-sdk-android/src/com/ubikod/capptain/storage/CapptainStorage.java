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

package com.ubikod.capptain.storage;

import java.io.Closeable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Storage abstraction. Attempts to use SQLite and fails over in memory if an error occurs.
 */
public class CapptainStorage implements Closeable
{
  /** Storage capacity in number of entries */
  private static final int CAPACITY = 300;

  /** Application context */
  private final Context mContext;

  /** In-memory database if SQLite cannot be used */
  private Map<Long, ContentValues> mIMDB;

  /** In-memory auto increment */
  private long mIMDBAutoInc;

  /** SQLite manager */
  private final SQLiteManager mManager;

  /** Error listener */
  private final ErrorListener mErrorListener;

  /** SQLite manager specification */
  private static class SQLiteManager extends SQLiteOpenHelper
  {
    /** Database name */
    private final String mDBName;

    /** Table name */
    private final String mTableName;

    /** Schema, e.g. a specimen with dummy values to have keys and their corresponding value's type */
    private final ContentValues mSchema;

    /**
     * Init SQLite manager.
     * @param context application context.
     * @param dbName database (file) name.
     * @param version schema version.
     * @param tableName table name.
     * @param schema specimen value.
     */
    private SQLiteManager(Context context, String dbName, int version, String tableName,
      ContentValues schema)
    {
      super(context, dbName, null, version);
      mDBName = dbName;
      mTableName = tableName;
      mSchema = schema;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
      /* Generate a schema from specimen */
      StringBuilder sql = new StringBuilder("CREATE TABLE `");
      sql.append(mTableName);
      sql.append("` (id INTEGER PRIMARY KEY AUTOINCREMENT");
      for (Entry<String, Object> col : mSchema.valueSet())
      {
        sql.append(", `").append(col.getKey()).append("` ");
        Object val = col.getValue();
        if (val instanceof Double || val instanceof Float)
          sql.append("REAL");
        else if (val instanceof Number || val instanceof Boolean)
          sql.append("INTEGER");
        else if (val instanceof byte[])
          sql.append("BLOB");
        else
          sql.append("TEXT");
      }
      sql.append(");");
      db.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
      /* For now we upgrade by destroying the old table */
      db.execSQL("DROP TABLE `" + mTableName + "`");
      onCreate(db);
    }

    /**
     * Get database name.
     * @return database name.
     */
    public String getDBName()
    {
      return mDBName;
    }

    /**
     * Get table name.
     * @return table name.
     */
    private String getTableName()
    {
      return mTableName;
    }

    /**
     * Get schema.
     * @return schema as a specimen value.
     */
    private ContentValues getSchema()
    {
      return mSchema;
    }
  }

  /** Listener specification, each callback is called only once per instance */
  public interface ErrorListener
  {
    /** Notify an SQLiteException */
    void onError(String operation, SQLException sqle);
  }

  /**
   * Init a database.
   * @param context application context.
   * @param dbName database (file) name.
   * @param version schema version.
   * @param tableName table name.
   * @param schema specimen value.
   * @param errorListener optional error listener.
   */
  public CapptainStorage(Context context, String dbName, int version, String tableName,
    ContentValues schema, ErrorListener errorListener)
  {
    /* Prepare SQLite manager */
    mContext = context;
    mManager = new SQLiteManager(context, dbName, version, tableName, schema);
    mErrorListener = errorListener;
  }

  /**
   * Get SQLite database.
   * @return SQLite database.
   * @throws SQLException if an error occurs.
   */
  private SQLiteDatabase getDatabase() throws SQLException
  {
    /* Try opening database */
    try
    {
      return mManager.getWritableDatabase();
    }
    catch (SQLException sqle)
    {
      /* First error, try to delete database (may be corrupted) */
      mContext.deleteDatabase(mManager.getDBName());

      /* Retry, let exception thrown if it fails this time */
      return mManager.getWritableDatabase();
    }
  }

  /**
   * Switch to in memory management, trigger error listener.
   * @param operation operation that triggered the error.
   * @param sqle error that triggered the switch.
   */
  private void switchToInMemory(String operation, SQLException sqle)
  {
    mIMDB = new LinkedHashMap<Long, ContentValues>()
    {
      private static final long serialVersionUID = 1L;

      @Override
      protected boolean removeEldestEntry(Map.Entry<Long, ContentValues> eldest)
      {
        return size() > CAPACITY;
      };
    };
    if (mErrorListener != null)
      mErrorListener.onError(operation, sqle);
  }

  /**
   * Store an entry.
   * @param values object describing the values to store.
   * @return database identifier.
   */
  public Long put(ContentValues values)
  {
    /* Try SQLite */
    if (mIMDB == null)
      try
      {
        /* Insert data */
        long id = getDatabase().insertOrThrow(mManager.getTableName(), null, values);

        /* Purge oldest entry if capacity reached */
        Cursor cursor = getCursor();
        int count = cursor.getCount();
        if (count > CAPACITY)
        {
          cursor.moveToNext();
          delete(cursor.getLong(0));
        }
        cursor.close();

        /* Return id */
        return id;
      }
      catch (SQLException sqle)
      {
        switchToInMemory("put", sqle);
      }

    /* If failed over in-memory */
    values.put("id", mIMDBAutoInc);
    mIMDB.put(mIMDBAutoInc, values);
    return mIMDBAutoInc++;
  }

  /**
   * Update an entry.
   * @param id existing entry identifier.
   * @param values values to update.
   * @return true if update was successful, false otherwise.
   */
  public boolean update(long id, ContentValues values)
  {
    /* Try SQLite */
    if (mIMDB == null)
      try
      {
        /* Update data */
        int updated = getDatabase().update(mManager.getTableName(), values, "id = " + id, null);

        /* Return success */
        return updated > 0;
      }
      catch (SQLException sqle)
      {
        switchToInMemory("update", sqle);
      }

    /* If failed over in-memory */
    ContentValues existing = mIMDB.get(id);
    if (existing == null)
      return false;
    existing.putAll(values);
    return true;
  }

  /**
   * Delete an entry from the database.
   * @param id database identifier.
   */
  public void delete(long id)
  {
    /* Try SQLite */
    if (mIMDB == null)
      try
      {
        getDatabase().delete(mManager.getTableName(), "id = " + id, null);
      }
      catch (SQLException sqle)
      {
        switchToInMemory("delete", sqle);
      }

    /* If failed over in-memory */
    else
      mIMDB.remove(id);
  }

  /**
   * Get an entry by its identifier.
   * @param id identifier.
   * @return entry or null if not found.
   */
  public ContentValues get(long id)
  {
    /* Try SQLite */
    if (mIMDB == null)
      try
      {
        Cursor cursor = getDatabase().query(mManager.getTableName(), null, "id = " + id, null,
          null, null, null);
        ContentValues values;
        if (cursor.moveToFirst())
          values = buildValues(cursor);
        else
          values = null;
        cursor.close();
        return values;
      }
      catch (SQLException sqle)
      {
        switchToInMemory("get", sqle);
        return null;
      }

    /* If failed over in-memory */
    else
      return mIMDB.get(id);
  }

  /**
   * Check if database is empty.
   * @return true if database is empty, false otherwise.
   */
  public boolean isEmpty()
  {
    Scanner scanner = getScanner();
    boolean empty = !scanner.iterator().hasNext();
    scanner.close();
    return empty;
  }

  /**
   * Get a scanner to iterate over all values.
   * @return a scanner to iterate over all values.
   */
  public Scanner getScanner()
  {
    return new Scanner();
  }

  /** Convert a cursor to a content values */
  private ContentValues buildValues(Cursor cursor)
  {
    ContentValues values = new ContentValues();
    for (int i = 0; i < cursor.getColumnCount(); i++)
    {
      if (cursor.isNull(i))
        continue;
      String key = cursor.getColumnName(i);
      if (key.equals("id"))
        values.put(key, cursor.getLong(0));
      else
      {
        Object specimen = mManager.getSchema().get(key);
        if (specimen instanceof byte[])
          values.put(key, cursor.getBlob(i));
        else if (specimen instanceof Double)
          values.put(key, cursor.getDouble(i));
        else if (specimen instanceof Float)
          values.put(key, cursor.getFloat(i));
        else if (specimen instanceof Integer)
          values.put(key, cursor.getInt(i));
        else if (specimen instanceof Long)
          values.put(key, cursor.getLong(i));
        else if (specimen instanceof Short)
          values.put(key, cursor.getShort(i));
        else
          values.put(key, cursor.getString(i));
      }
    }
    return values;
  }

  /**
   * Get a cursor on all database rows.
   * @return cursor on all database rows.
   * @throws SQLException if an error occurs.
   */
  private Cursor getCursor() throws SQLException
  {
    return getDatabase().query(mManager.getTableName(), null, null, null, null, null, null);
  }

  /**
   * Scanner specification.
   */
  public class Scanner implements Iterable<ContentValues>, Closeable
  {
    /** SQLite cursor */
    private Cursor cursor;

    @Override
    public void close()
    {
      /* If was using SQLite, close cursor */
      if (cursor != null)
        try
        {
          cursor.close();
        }
        catch (SQLException sqle)
        {
          switchToInMemory("scan", sqle);
        }
    }

    @Override
    public Iterator<ContentValues> iterator()
    {
      /* Try SQLite */
      if (mIMDB == null)
        try
        {
          /* Open cursor, or reset previous one */
          if (cursor == null)
            cursor = getCursor();
          else
            cursor.requery();

          /* Wrap cursor as iterator */
          return new Iterator<ContentValues>()
          {
            /** If null, hasNext is not known yet */
            Boolean hasNext;

            @Override
            public boolean hasNext()
            {
              if (hasNext == null)
                try
                {
                  hasNext = cursor.moveToNext();
                }
                catch (SQLException sqle)
                {
                  /* Consider no next on error */
                  hasNext = false;

                  /* Make close do nothing */
                  cursor = null;

                  /* Switch to in memory DB */
                  switchToInMemory("scan", sqle);
                }
              return hasNext;
            }

            @Override
            public ContentValues next()
            {
              /* Check next */
              if (!hasNext())
                throw new NoSuchElementException();
              hasNext = null;

              /* Build object */
              return buildValues(cursor);
            }

            @Override
            public void remove()
            {
              throw new UnsupportedOperationException();
            }
          };
        }
        catch (SQLException sqle)
        {
          switchToInMemory("scan", sqle);
        }

      /* Fail over in-memory */
      return mIMDB.values().iterator();
    }
  }

  /**
   * Clear database.
   */
  public void clear()
  {
    /* Try SQLite */
    if (mIMDB == null)
      try
      {
        getDatabase().delete(mManager.getTableName(), null, null);
      }
      catch (SQLException sqle)
      {
        switchToInMemory("clear", sqle);
      }

    /* If failed over in-memory */
    else
      mIMDB.clear();
  }

  @Override
  public void close()
  {
    /* Try SQLite */
    if (mIMDB == null)
      try
      {
        getDatabase().close();
      }
      catch (SQLException sqle)
      {
        switchToInMemory("close", sqle);
      }

    /* Close in memory database */
    else
    {
      mIMDB.clear();
      mIMDB = null;
    }
  }
}
