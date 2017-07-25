package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import android.util.Log;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import static android.R.string.no;
import static com.example.android.inventory.R.id.quantity;

/**
 * Created by georgeampartzidis on 20/7/17.
 */

public class ProductProvider extends ContentProvider {
    /**
     * Database helper object
     */
    private ProductDbHelper mDbHelper;

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    /**
     * Constant variable for the query of the products table
     */
    public static final int PRODUCTS = 100;

    /**
     * Constant variable for the query of specific row/s of the products table
     */
    public static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed to the constructor represents the code to return for the root URI.
     * It's common to pass NO_MATCH as the input.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer, runs the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.inventory/products" will
        // map to the integer code {@link #PRODUCTS}. This URI is used to provide access
        // to MULTIPLE rows of the products table.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);

        // The content URI of the form "content://com.example.android.inventory/products/#" will
        // map to the integer code {@link #PRODUCT_ID}. This URI is used to provide access to
        // ONE single row of the products table. "#" can be substituted for an integer.
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the query result
        Cursor cursor;

        // Figure out if the URI matcher matches the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection etc. The cursor could contain multiple rows
                // of the products table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, we extract out the product ID from the URI.
                // The selection be in the form of "_id=?" and the selectionArgs will be a
                // String Array containing the actual ID of the row. Each "?" represents an ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Perform the query on the products table for the specific row with the given ID
                // to return a Cursor containing that row of the table.
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        return cursor;
    }


    /**
     * Insert new data into the provider with the given ContentValues
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            // Checking only if the URI Matcher matches the PRODUCTS code. We are not checking
            // the PRODUCT_ID case, because we are inserting a new product (hence no PRODUCT_ID yet).
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for  URI " + uri);

        }
    }

    /**
     * Helper method to insert a new product into the database with the given content values.
     * Returns the new content URI for the specific row (product) that was inserted.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // check that the name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product name required");
        }
        // Check that the quantity is not negative. In case it is null we are ok because it gets
        // the default value of 0.
        Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires a valid quantity");
        }
        //Check that the price is not negative or null.
        Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }
        // Check that the supplier is not null
        String supplier = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Please provide a supplier for the product");
        }
        // Check that the supplier email is not null
        String supplierEmail = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Please provide an email for the supplier");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        // If the id is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Error inserting row for " + uri);
            return null;
        }
        // If 1 or more rows were updated, then notify all the listeners that the data at the
        // given URI has changed.
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);

    }

    /**
     * Updates the data at the given selection and selectargs, with the new content values
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // Extract out the ID from the URI so we know which row to update.
                // selection will be "_id=?" and selectArgs will be a String Array containing the ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Helper method that updates the database with the given content values. Apply the changes to
     * the rows specified in the selection and selection arguments.
     * return the number of rows affected.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectArgs) {
        // if the {@link ProductEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product name required");
            }
        }

        // if the {@link ProductEntry#COLUMN_PRODUCT_QUANTITY} key is present,
        // check that the quantity value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product quantity must be a valid number");
            }
        }
        // if the {@link ProductEntry#COLUMN_PRODUCT_PRICE} key is present,
        // check that the quantity value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product price must be a valid number");
            }
        }
        // if the {@link ProductEntry#COLUMN_PRODUCT_SUPPLIER} key is present,
        // check that the supplier value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER)) {
            String supplier = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Product supplier required");
            }
        }
        // if the {@link ProductEntry#COLUMN_PRODUCT_SUPPLIER_EMAIL} key is present,
        // check that the supplierEmail value is not null.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL)) {
            String supplierEmail = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Product supplier email required");
            }
        }

        // If there are no values to update, then return early without updating the database.
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writable database and update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update and return the number of rows affected.
        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, values, selection, selectArgs);
        // If 1 or more rows were updated, then notify all the listeners that the data at the
        // given URI has changed.
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all the rows that match the selection and selection args
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete the row with the ID given by the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 ore more rows were deleted, notify all listeners that the data at the given
        // URI was changed (deleted).
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
