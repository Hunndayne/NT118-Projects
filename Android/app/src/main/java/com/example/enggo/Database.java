package com.example.enggo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class Database {

    /* ========================= Database ========================= */

    public static final class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "saved_content";
        public static final String COLUMN_NAME_TITLE = "Token";
        public static final String COLUMN_NAME_DATE_TOKEN = "DateToken";
        public static final String COLUMN_NAME_IS_ADMIN = "IsAdmin";
    }

    /* ========================= Model ========================= */

    public static class Item {
        public long id;
        public String token;
        public long dateToken; // epoch millis
        public int isAdmin;    // 0/1

        public Item() {}

        public Item(long id, String token, long dateToken, int isAdmin) {
            this.id = id;
            this.token = token;
            this.dateToken = dateToken;
            this.isAdmin = isAdmin;
        }
    }

    /* ========================= DB Helper ========================= */

    public static class DbHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "app.db";
        // v1: có cột URL; v2: bỏ cột URL
        private static final int DB_VERSION = 2;

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        private static final String SQL_CREATE_ENTRIES_V2 =
                "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FeedEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                        FeedEntry.COLUMN_NAME_DATE_TOKEN + " INTEGER NOT NULL, " +
                        FeedEntry.COLUMN_NAME_IS_ADMIN + " INTEGER NOT NULL DEFAULT 0" +
                        ");";

        private static final String SQL_CREATE_INDEX_TOKEN =
                "CREATE INDEX IF NOT EXISTS idx_saved_token ON " +
                        FeedEntry.TABLE_NAME + "(" +
                        FeedEntry.COLUMN_NAME_TITLE + ");";

        private static final String SQL_DROP_ENTRIES =
                "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_V2);
            db.execSQL(SQL_CREATE_INDEX_TOKEN);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                migrateV1ToV2(db);
            }
        }

        private void migrateV1ToV2(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                String tempTable = FeedEntry.TABLE_NAME + "_new";
                // Create the new table
                db.execSQL("CREATE TABLE " + tempTable + " (" +
                        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FeedEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                        FeedEntry.COLUMN_NAME_DATE_TOKEN + " INTEGER NOT NULL, " +
                        FeedEntry.COLUMN_NAME_IS_ADMIN + " INTEGER NOT NULL DEFAULT 0" +
                        ");");

                // Copy the data from the old table to the new table.
                db.execSQL("INSERT INTO " + tempTable + " (" +
                        BaseColumns._ID + ", " +
                        FeedEntry.COLUMN_NAME_TITLE + ", " +
                        FeedEntry.COLUMN_NAME_DATE_TOKEN + ") " +
                        "SELECT " +
                        BaseColumns._ID + ", " +
                        FeedEntry.COLUMN_NAME_TITLE + ", " +
                        FeedEntry.COLUMN_NAME_DATE_TOKEN + " " +
                        "FROM " + FeedEntry.TABLE_NAME);

                // Drop the old table
                db.execSQL("DROP TABLE " + FeedEntry.TABLE_NAME);

                // Rename the new table to the old table name
                db.execSQL("ALTER TABLE " + tempTable + " RENAME TO " + FeedEntry.TABLE_NAME);

                db.execSQL(SQL_CREATE_INDEX_TOKEN);
                
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }



    public static class Dao {

        private final DbHelper dbHelper;

        public Dao(Context context) {
            this.dbHelper = new DbHelper(context.getApplicationContext());
        }

        // Thêm mới
        public long insert(Item item) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = toContentValues(item, false);
            return db.insert(FeedEntry.TABLE_NAME, null, values);
        }

        // Cập nhật theo _ID
        public int update(Item item) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = toContentValues(item, false);
            String where = BaseColumns._ID + " = ?";
            String[] args = { String.valueOf(item.id) };
            return db.update(FeedEntry.TABLE_NAME, values, where, args);
        }

        // Xóa theo _ID
        public int deleteById(long id) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String where = BaseColumns._ID + " = ?";
            String[] args = { String.valueOf(id) };
            return db.delete(FeedEntry.TABLE_NAME, where, args);
        }

        // Xóa tất cả
        public int deleteAll() {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.delete(FeedEntry.TABLE_NAME, null, null);
        }

        // Lấy 1 bản ghi theo _ID
        public Item getById(long id) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] cols = projectionAll();
            String sel = BaseColumns._ID + " = ?";
            String[] selArgs = { String.valueOf(id) };
            try (Cursor c = db.query(FeedEntry.TABLE_NAME, cols, sel, selArgs, null, null, null)) {
                if (c.moveToFirst()) return map(c);
            }
            return null;
        }

        // Lấy theo Token
        public Item getByToken(String token) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] cols = projectionAll();
            String sel = FeedEntry.COLUMN_NAME_TITLE + " = ?";
            String[] args = { token };
            try (Cursor c = db.query(FeedEntry.TABLE_NAME, cols, sel, args, null, null, null)) {
                if (c.moveToFirst()) return map(c);
            }
            return null;
        }

        // Upsert theo Token
        public long upsertByToken(Item item) {
            Item existing = getByToken(item.token);
            if (existing == null) {
                return insert(item);
            } else {
                item.id = existing.id;
                update(item);
                return item.id;
            }
        }

        // Lấy tất cả (mới nhất trước)
        public List<Item> getAll() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] cols = projectionAll();
            List<Item> list = new ArrayList<>();
            try (Cursor c = db.query(
                    FeedEntry.TABLE_NAME,
                    cols,
                    null,
                    null,
                    null,
                    null,
                    FeedEntry.COLUMN_NAME_DATE_TOKEN + " DESC, " + BaseColumns._ID + " DESC"
            )) {
                while (c.moveToNext()) list.add(map(c));
            }
            return list;
        }

        public long count() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + FeedEntry.TABLE_NAME, null)) {
                if (c.moveToFirst()) return c.getLong(0);
            }
            return 0;
        }


        private static ContentValues toContentValues(Item item, boolean includeId) {
            ContentValues v = new ContentValues();
            if (includeId) v.put(BaseColumns._ID, item.id);
            v.put(FeedEntry.COLUMN_NAME_TITLE, item.token);
            v.put(FeedEntry.COLUMN_NAME_DATE_TOKEN, item.dateToken);
            v.put(FeedEntry.COLUMN_NAME_IS_ADMIN, item.isAdmin);
            return v;
        }

        private static String[] projectionAll() {
            return new String[] {
                    BaseColumns._ID,
                    FeedEntry.COLUMN_NAME_TITLE,
                    FeedEntry.COLUMN_NAME_DATE_TOKEN,
                    FeedEntry.COLUMN_NAME_IS_ADMIN
            };
        }

        private static Item map(Cursor c) {
            Item item = new Item();
            item.id = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
            item.token = c.getString(c.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE));
            item.dateToken = c.getLong(c.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_DATE_TOKEN));
            item.isAdmin = c.getInt(c.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_IS_ADMIN));
            return item;
        }
    }
}
