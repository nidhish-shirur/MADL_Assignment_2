package com.nid.madl02_49; // Ensure this matches your package name!

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 1. Define Database Schema Rules
    private static final String DATABASE_NAME = "NotesDB_49.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "notes_49";

    // Column Names
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESC = "description";
    private static final String COL_IMAGE_PATH = "image_path";
    private static final String COL_DATE = "date";
    private static final String COL_PRIORITY = "priority"; // Personalized Field!

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 2. Create the Table Query
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DESC + " TEXT, " +
                COL_IMAGE_PATH + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_PRIORITY + " TEXT)"; // E.g., High, Medium, Low

        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 1. Delete a Note
    public void deleteNote(int id) {
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        db.delete("notes_49", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // 2. Update an existing Note
    public boolean updateNote(int id, String title, String description, String imagePath, String priority) {
        android.database.sqlite.SQLiteDatabase db = this.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("image_path", imagePath);
        values.put("priority", priority);

        int result = db.update("notes_49", values, "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // 3. Method to Insert a New Note
    public boolean insertNote(String title, String description, String imagePath, String date, String priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_TITLE, title);
        contentValues.put(COL_DESC, description);
        contentValues.put(COL_IMAGE_PATH, imagePath);
        contentValues.put(COL_DATE, date);
        contentValues.put(COL_PRIORITY, priority);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // Returns true if insertion was successful
    }

    // 4. Method to Retrieve All Notes
    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}