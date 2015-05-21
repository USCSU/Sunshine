/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        Students: Uncomment this test once you've written the code to create the Location
        table.  Note that you will have to have chosen the same column names that I did in
        my solution for this test to compile, so if you haven't yet done that, this is
        a good time to change your column names to match mine.

        Note that this only tests that the Location table has the correct columns, since we
        give you the code for the weather table.  This test does not look at the
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {
        // get db bridge which connect contract and db
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        //get db read handler or write handler
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create insert content: contextContent;
        ContentValues locationValue = TestUtilities.createNorthPoleLocationValues();

        //insert into database
        db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,locationValue);

        //query to get cursor which is result set
        Cursor locaitonCursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,null,null,null,null,null,null,null);
        // subtest : test if any data in the database table
        assertTrue("No message in the table", locaitonCursor.moveToFirst());
        //subTest: compare data
        TestUtilities.validateCurrentRecord("Records not matched",locaitonCursor,locationValue);
        //subTest : check if there is only one record
        assertFalse("More than one records found!",locaitonCursor.moveToNext());
        //close
        locaitonCursor.close();;
        db.close();
        dbHelper.close();

    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */

    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        long locationRowId = insertLocation();

        // Make sure we have a valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", locationRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Weather): Create weather values
        ContentValues weatherValues = com.example.android.sunshine.data.TestUtilities.createWeatherValues(locationRowId);

        // Third Step (Weather): Insert ContentValues into database and get a row ID back
        long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = db.query(
                WeatherContract.WeatherEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from location query", weatherCursor.moveToFirst());

        // Fifth Step: Validate the location Query
        com.example.android.sunshine.data.TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                weatherCursor, weatherValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from weather query",
                weatherCursor.moveToNext());

        // Sixth Step: Close cursor and database
        weatherCursor.close();
        dbHelper.close();
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */

    //Weather table, location table. Weather table has foreign key to the location table
//     public void testWeatherTable1(){
//         //first insert location table so that weather table is workable
//         //Step 1: insert location table
//         //1.1 get dbhelper and handler to db
//         WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
//         SQLiteDatabase db = dbHelper.getWritableDatabase();
//         //1.2 prepare content values
//         ContentValues values = new ContentValues();
//         values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, "94112");
//         values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, "North Pole");
//         values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, 64.7488);
//         values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, -147.353);
//         //1.3 insert table
//        long rowid =  db.insert(WeatherContract.LocationEntry.TABLE_NAME,null,values);
//        assertTrue(rowid!=-1);
//         //Step 2: insert weather table
//         ContentValues weatherValues = new ContentValues();
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, rowid);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, 1419033600L);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, 1.1);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, 1.2);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, 1.3);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5);
//         weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
//         long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
//         assertTrue(weatherRowId!=-1);
//
//         //query
//         Cursor queryCursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME,null,null,null,null,null,null,null);
//         assertTrue("no data found", queryCursor.moveToFirst());
//         //step3: validate cursor value and content value:
//         //3.1 streightforward way to do data validation
//        // TestUtilities.validateCurrentRecord("recorss not matched",queryCursor,weatherValues);
//         //3.2 detail process of data validation
//         //3.2.1 get Content value which is a key-value pair
//         Set <Map.Entry<String, Object>> contentValueSet = weatherValues.valueSet();
//         //3.2.2 for each entry, get column name so that to get cursor value
//         //3.2.3 get column name, get column index with colname, get value with colIndex
//         for(Map.Entry<String,Object> entry:contentValueSet){
//             String colName = entry.getKey();
//             int index = queryCursor.getColumnIndex(colName);
//             assertFalse("No record found in result set", index==-1);
//             String contentRecord = entry.getValue().toString();
//             String cursorRecord = queryCursor.getString(index);
//             assertEquals("Contetnt record: "+contentRecord +" did NOT match cursor record '" + cursorRecord +" ' ",contentRecord,cursorRecord);
//         }
//         //check if more than one data found
//         assertFalse("more than one data found", queryCursor.moveToNext());
//         //step 4: close all the handlers
//         queryCursor.close();
//         db.close();
//         dbHelper.close();
//     }
    public long insertLocation() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = com.example.android.sunshine.data.TestUtilities.createNorthPoleLocationValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        com.example.android.sunshine.data.TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return locationRowId;
    }
}