package org.telegram.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.telegram.messenger.FileLog;
import org.telegram.ui.Components.Favorite;

import java.util.ArrayList;

import static android.R.attr.id;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favourites";
    private static final String TABLE_FAVS = "tbl_favs";

    private static final String KEY_ID = "id";
    private static final String KEY_CHAT_ID = "chat_id";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVS_TABLE = "CREATE TABLE " + TABLE_FAVS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CHAT_ID + " INTEGER" + ")";
        db.execSQL(CREATE_FAVS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVS);
        onCreate(db);
    }

    public ArrayList<Long> getList(){
        //Log.e(TAG,"getList");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        ArrayList<Long> data = null;
        try{

            data = new ArrayList<>();

            cursor = db.query(
                    TABLE_FAVS,
                    new String[]{KEY_CHAT_ID},
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);

            if (cursor.moveToFirst()){
                do {
                    data.add(cursor.getLong(0));
                } while (cursor.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            if(cursor != null) cursor.close();
            FileLog.e(e);
        } finally {
            if(cursor != null) cursor.close();
        }
        return data;
    }

    public void addFavorite(Long id) {
        //Log.e(TAG,"addFavorite " + id);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CHAT_ID, id);
        db.insert(TABLE_FAVS, null, values);
        db.close();
    }

    public void deleteFavorite(Long chat_id) {
        //Log.e(TAG,"deleteFavorite " + id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVS, KEY_CHAT_ID + " = ?", new String[] { String.valueOf(chat_id) });
        db.close();
    }

}
