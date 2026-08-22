package com.example.manga_translator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "manga_translator.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE projects (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, lang TEXT, created_at TEXT, pages_count INTEGER, status TEXT)");
        db.execSQL("CREATE TABLE pages (id INTEGER PRIMARY KEY AUTOINCREMENT, project_id INTEGER, page_number INTEGER, image_path TEXT, raw_text TEXT, translated_text TEXT, corrected_text TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS projects");
        db.execSQL("DROP TABLE IF EXISTS pages");
        onCreate(db);
    }

    public long insertProject(String name, String lang, int pagesCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("lang", lang);
        values.put("created_at", String.valueOf(System.currentTimeMillis()));
        values.put("pages_count", pagesCount);
        values.put("status", "pending");
        return db.insert("projects", null, values);
    }

    public void insertPage(long projectId, int pageNumber, String imagePath, String rawText, String translatedText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("page_number", pageNumber);
        values.put("image_path", imagePath);
        values.put("raw_text", rawText);
        values.put("translated_text", translatedText);
        values.put("corrected_text", translatedText);
        db.insert("pages", null, values);
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM projects ORDER BY created_at DESC", null);
        if (cursor.moveToFirst()) {
            do {
                projects.add(new Project(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return projects;
    }

    public List<Page> getPagesByProject(long projectId) {
        List<Page> pages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM pages WHERE project_id=? ORDER BY page_number", new String[]{String.valueOf(projectId)});
        if (cursor.moveToFirst()) {
            do {
                pages.add(new Page(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pages;
    }

    public void updatePageCorrection(long pageId, String correctedText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("corrected_text", correctedText);
        db.update("pages", values, "id=?", new String[]{String.valueOf(pageId)});
    }

    public void updateProjectStatus(long projectId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("projects", values, "id=?", new String[]{String.valueOf(projectId)});
    }
}

// ===== Model Classes =====
class Project {
    public int id;
    public String name, lang, createdAt, status;
    public int pagesCount;

    public Project(int id, String name, String lang, String createdAt, int pagesCount, String status) {
        this.id = id; this.name = name; this.lang = lang; this.createdAt = createdAt; this.pagesCount = pagesCount; this.status = status;
    }
}

class Page {
    public int id, projectId, pageNumber;
    public String imagePath, rawText, translatedText, correctedText;

    public Page(int id, int projectId, int pageNumber, String imagePath, String rawText, String translatedText, String correctedText) {
        this.id = id; this.projectId = projectId; this.pageNumber = pageNumber; this.imagePath = imagePath; this.rawText = rawText; this.translatedText = translatedText; this.correctedText = correctedText;
    }
}