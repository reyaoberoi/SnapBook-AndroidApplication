package com.example.snapbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ScrapbookDataManager extends SQLiteOpenHelper {

    private static final String TAG = "ScrapbookDataManager";
    private static final String DATABASE_NAME = "scrapbook.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_PAGES = "pages";
    private static final String TABLE_ITEMS = "items";

    // Pages table columns
    private static final String COLUMN_PAGE_ID = "id";
    private static final String COLUMN_PAGE_TITLE = "title";
    private static final String COLUMN_PAGE_CREATED = "created_date";
    private static final String COLUMN_PAGE_MODIFIED = "last_modified";
    private static final String COLUMN_PAGE_BACKGROUND = "background_image";
    private static final String COLUMN_PAGE_BG_COLOR = "background_color";

    // Items table columns
    private static final String COLUMN_ITEM_ID = "id";
    private static final String COLUMN_ITEM_PAGE_ID = "page_id";
    private static final String COLUMN_ITEM_TYPE = "type";
    private static final String COLUMN_ITEM_DATA = "data"; // JSON data for all item properties

    public ScrapbookDataManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create pages table
        String createPagesTable = "CREATE TABLE " + TABLE_PAGES + " (" +
                COLUMN_PAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PAGE_TITLE + " TEXT NOT NULL, " +
                COLUMN_PAGE_CREATED + " INTEGER NOT NULL, " +
                COLUMN_PAGE_MODIFIED + " INTEGER NOT NULL, " +
                COLUMN_PAGE_BACKGROUND + " TEXT, " +
                COLUMN_PAGE_BG_COLOR + " INTEGER DEFAULT " + 0xFFFAF8F5 + ")";

        // Create items table
        String createItemsTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ITEM_PAGE_ID + " INTEGER NOT NULL, " +
                COLUMN_ITEM_TYPE + " INTEGER NOT NULL, " +
                COLUMN_ITEM_DATA + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_ITEM_PAGE_ID + ") REFERENCES " + 
                TABLE_PAGES + "(" + COLUMN_PAGE_ID + ") ON DELETE CASCADE)";

        db.execSQL(createPagesTable);
        db.execSQL(createItemsTable);

        Log.d(TAG, "Database created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAGES);
        onCreate(db);
    }

    public long savePage(ScrapbookPage page) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            ContentValues pageValues = new ContentValues();
            pageValues.put(COLUMN_PAGE_TITLE, page.title);
            pageValues.put(COLUMN_PAGE_CREATED, page.createdDate);
            pageValues.put(COLUMN_PAGE_MODIFIED, System.currentTimeMillis());
            pageValues.put(COLUMN_PAGE_BACKGROUND, page.backgroundImagePath);
            pageValues.put(COLUMN_PAGE_BG_COLOR, page.backgroundColor);

            long pageId;
            if (page.id > 0) {
                // Update existing page
                db.update(TABLE_PAGES, pageValues, COLUMN_PAGE_ID + "=?", 
                         new String[]{String.valueOf(page.id)});
                pageId = page.id;
                
                // Delete existing items
                db.delete(TABLE_ITEMS, COLUMN_ITEM_PAGE_ID + "=?", 
                         new String[]{String.valueOf(pageId)});
            } else {
                // Insert new page
                pageId = db.insert(TABLE_PAGES, null, pageValues);
            }

            // Save items
            for (ScrapbookItem item : page.items) {
                saveItem(db, pageId, item);
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Page saved successfully with ID: " + pageId);
            return pageId;
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving page", e);
            return -1;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void saveItem(SQLiteDatabase db, long pageId, ScrapbookItem item) {
        try {
            JSONObject itemData = new JSONObject();
            itemData.put("x", item.x);
            itemData.put("y", item.y);
            itemData.put("width", item.width);
            itemData.put("height", item.height);
            itemData.put("rotation", item.rotation);
            itemData.put("scale", item.scale);
            
            if (item.isImage()) {
                itemData.put("imagePath", item.imagePath);
            } else if (item.isText()) {
                itemData.put("text", item.text);
                itemData.put("textColor", item.textColor);
                itemData.put("textSize", item.textSize);
                itemData.put("fontFamily", item.fontFamily);
                itemData.put("isBold", item.isBold);
                itemData.put("isItalic", item.isItalic);
            } else if (item.isDoodle()) {
                itemData.put("doodlePath", item.doodlePath);
                itemData.put("strokeColor", item.strokeColor);
                itemData.put("strokeWidth", item.strokeWidth);
            }
            
            itemData.put("backgroundColor", item.backgroundColor);
            itemData.put("hasBorder", item.hasBorder);
            itemData.put("borderColor", item.borderColor);
            itemData.put("borderWidth", item.borderWidth);
            itemData.put("cornerRadius", item.cornerRadius);

            ContentValues itemValues = new ContentValues();
            itemValues.put(COLUMN_ITEM_PAGE_ID, pageId);
            itemValues.put(COLUMN_ITEM_TYPE, item.type);
            itemValues.put(COLUMN_ITEM_DATA, itemData.toString());

            db.insert(TABLE_ITEMS, null, itemValues);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error saving item data", e);
        }
    }

    public List<ScrapbookPage> loadAllPages() {
        List<ScrapbookPage> pages = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PAGES, null, null, null, null, null, 
                                COLUMN_PAGE_MODIFIED + " DESC");

        if (cursor.moveToFirst()) {
            do {
                ScrapbookPage page = new ScrapbookPage();
                page.id = cursor.getLong(cursor.getColumnIndex(COLUMN_PAGE_ID));
                page.title = cursor.getString(cursor.getColumnIndex(COLUMN_PAGE_TITLE));
                page.createdDate = cursor.getLong(cursor.getColumnIndex(COLUMN_PAGE_CREATED));
                page.lastModified = cursor.getLong(cursor.getColumnIndex(COLUMN_PAGE_MODIFIED));
                page.backgroundImagePath = cursor.getString(cursor.getColumnIndex(COLUMN_PAGE_BACKGROUND));
                page.backgroundColor = cursor.getInt(cursor.getColumnIndex(COLUMN_PAGE_BG_COLOR));

                // Load items for this page
                page.items = loadItemsForPage(db, page.id);
                pages.add(page);
                
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        
        Log.d(TAG, "Loaded " + pages.size() + " pages");
        return pages;
    }

    public ScrapbookPage loadPage(long pageId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PAGES, null, COLUMN_PAGE_ID + "=?", 
                                new String[]{String.valueOf(pageId)}, null, null, null);

        ScrapbookPage page = null;
        if (cursor.moveToFirst()) {
            page = new ScrapbookPage();
            page.id = cursor.getLong(cursor.getColumnIndex(COLUMN_PAGE_ID));
            page.title = cursor.getString(cursor.getColumnIndex(COLUMN_PAGE_TITLE));
            page.createdDate = cursor.getLong(cursor.getColumnIndex(COLUMN_PAGE_CREATED));
            page.lastModified = cursor.getLong(cursor.getColumnIndex(COLUMN_PAGE_MODIFIED));
            page.backgroundImagePath = cursor.getString(cursor.getColumnIndex(COLUMN_PAGE_BACKGROUND));
            page.backgroundColor = cursor.getInt(cursor.getColumnIndex(COLUMN_PAGE_BG_COLOR));

            // Load items
            page.items = loadItemsForPage(db, page.id);
        }

        cursor.close();
        db.close();
        return page;
    }

    private List<ScrapbookItem> loadItemsForPage(SQLiteDatabase db, long pageId) {
        List<ScrapbookItem> items = new ArrayList<>();
        
        Cursor cursor = db.query(TABLE_ITEMS, null, COLUMN_ITEM_PAGE_ID + "=?", 
                                new String[]{String.valueOf(pageId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    ScrapbookItem item = new ScrapbookItem();
                    item.id = cursor.getLong(cursor.getColumnIndex(COLUMN_ITEM_ID));
                    item.type = cursor.getInt(cursor.getColumnIndex(COLUMN_ITEM_TYPE));
                    
                    String dataJson = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_DATA));
                    JSONObject data = new JSONObject(dataJson);
                    
                    item.x = (float) data.getDouble("x");
                    item.y = (float) data.getDouble("y");
                    item.width = (float) data.getDouble("width");
                    item.height = (float) data.getDouble("height");
                    item.rotation = (float) data.optDouble("rotation", 0);
                    item.scale = (float) data.optDouble("scale", 1);
                    
                    if (item.isImage()) {
                        item.imagePath = data.optString("imagePath");
                    } else if (item.isText()) {
                        item.text = data.optString("text");
                        item.textColor = data.optInt("textColor", 0xFF6B4423);
                        item.textSize = (float) data.optDouble("textSize", 16);
                        item.fontFamily = data.optString("fontFamily", "serif");
                        item.isBold = data.optBoolean("isBold", false);
                        item.isItalic = data.optBoolean("isItalic", false);
                    } else if (item.isDoodle()) {
                        item.doodlePath = data.optString("doodlePath");
                        item.strokeColor = data.optInt("strokeColor", 0xFF8B6914);
                        item.strokeWidth = (float) data.optDouble("strokeWidth", 3);
                    }
                    
                    item.backgroundColor = data.optInt("backgroundColor", 0x00000000);
                    item.hasBorder = data.optBoolean("hasBorder", false);
                    item.borderColor = data.optInt("borderColor", 0xFF8B6914);
                    item.borderWidth = (float) data.optDouble("borderWidth", 2);
                    item.cornerRadius = (float) data.optDouble("cornerRadius", 0);
                    
                    items.add(item);
                    
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing item data", e);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return items;
    }

    public boolean deletePage(long pageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            // Delete items first (foreign key constraint)
            db.delete(TABLE_ITEMS, COLUMN_ITEM_PAGE_ID + "=?", 
                     new String[]{String.valueOf(pageId)});
            
            // Delete page
            int rowsDeleted = db.delete(TABLE_PAGES, COLUMN_PAGE_ID + "=?", 
                                      new String[]{String.valueOf(pageId)});
            
            db.setTransactionSuccessful();
            Log.d(TAG, "Page deleted successfully");
            return rowsDeleted > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting page", e);
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public int getPageCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PAGES, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        return count;
    }
}