package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * Created by georgeampartzidis on 18/7/17.
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    // In case the database schema is changed, you mst increase the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
                        ProductEntry.PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                        ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                        ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0," +
                        ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL," +
                        ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL + " TEXT NOT NULL," +
                        ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT)";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        String SQL_DROP_ENTRIES =
                "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;
        db.execSQL(SQL_DROP_ENTRIES);
        onCreate(db);
    }
}
