package fr.wcs.bdsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.ContentValues.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.bastienwcs.sqllite", appContext.getPackageName());
    }

    @Test
    public void testWriteTweet() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(DatabaseContract.UserEntry.TABLE_NAME);
        TweetDbHelper dBHelper = new TweetDbHelper(context);
        SQLiteDatabase db = dBHelper.getWritableDatabase();

        ContentValues user = new ContentValues();
        user.put(DatabaseContract.UserEntry.COLUMN_NAME_NAME, "bastienwcs");
        user.put(DatabaseContract.UserEntry.COLUMN_NAME_EMAIL, "bastien@wildcodeschool.fr");
        long newUserId = db.insert(DatabaseContract.UserEntry.TABLE_NAME, null, user);
        assertNotEquals(-1, newUserId);

        ContentValues organization = new ContentValues();
        organization.put(DatabaseContract.OrganizationEntry.COLUMN_NAME_NAME, "Wild Code School");
        organization.put(DatabaseContract.OrganizationEntry.COLUMN_NAME_WEBSITE_URL, "https://wildcodeschool.fr");
        long newOrganizationId = db.insert(DatabaseContract.OrganizationEntry.TABLE_NAME, null, organization);
        assertNotEquals(-1, newOrganizationId);

        ContentValues belong = new ContentValues();
        belong.put(DatabaseContract.BelongEntry.COLUMN_NAME_USER_ID, newUserId);
        belong.put(DatabaseContract.BelongEntry.COLUMN_NAME_ORGANIZATION_ID, newOrganizationId);
        long newBelongId = db.insert(DatabaseContract.BelongEntry.TABLE_NAME, null, belong);
        assertNotEquals(-1, newBelongId);

        String content = "Mon super tweet n° : ";
        for (int i = 0; i < 10; i++) {
            ContentValues tweet = new ContentValues();
            tweet.put(DatabaseContract.TweetEntry.COLUMN_NAME_CONTENT, content + String.valueOf(i + 1));
            tweet.put(DatabaseContract.TweetEntry.COLUMN_NAME_USER_ID, newUserId);
            long newTweetId = db.insert(DatabaseContract.TweetEntry.TABLE_NAME, null, tweet);
            assertNotEquals(-1, newTweetId);
        }
    }

    @Test
    public void testReadTweet() throws Exception {
        Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(DatabaseContract.UserEntry.TABLE_NAME);
        TweetDbHelper dBHelper = new TweetDbHelper(context);
        SQLiteDatabase db = dBHelper.getWritableDatabase();

        // récupération de l'id du dernier user ajouté
        Cursor cursor = db.rawQuery("SELECT user_id FROM " + DatabaseContract.UserEntry.TABLE_NAME + " ORDER BY user_id DESC LIMIT 1", null);
        long lastUserId = 0;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            lastUserId = cursor.getLong(cursor.getColumnIndex("user_id"));
        }

        String[] projection = {
                DatabaseContract.TweetEntry.COLUMN_NAME_ID,
                DatabaseContract.TweetEntry.COLUMN_NAME_CONTENT,
                DatabaseContract.TweetEntry.COLUMN_NAME_USER_ID
        };
        // on filtre sur l'id du dernier user
        String selection = DatabaseContract.TweetEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {
                String.valueOf(lastUserId)
        };

        cursor = db.query(
                DatabaseContract.TweetEntry.TABLE_NAME, // The table to query
                projection,                             // The columns to return
                selection,                              // The columns for the WHERE clause
                selectionArgs,                          // The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                null                                    // The sort order
        );

        while(cursor.moveToNext()) {
            String content = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseContract.TweetEntry.COLUMN_NAME_CONTENT));
            assertNotEquals(null, content);
            Log.i(TAG, "testReadTweet: " + content);
        }
        cursor.close();
    }
}