package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API contract for the Products App
 */

public final class ProductContract {
    // Empty constructor to make sure that the contract class cannot be accidentally constructed.
    private ProductContract(){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.product/products/ is a valid path for
     * looking at pet data. content://com.example.android.product/sellers will fail,
     * as the ContentProvider hasn't been given any information on what to do with "sellers".
     */
    public static final String PATH_PRODUCTS = "products";



    /** Inner class that defines constant values for the products database table.
     *  Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /** Name of the database table for the products */
        public static final String TABLE_NAME= "products";

        /** Unique ID number for each product (to be used only inside the datatable)
         *  TYPE: INTEGER
         * */
        public static final String PRODUCT_ID= BaseColumns._ID;

        /** Name of the product
         *  TYPE: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME= "name";
        /** Quantity of the product.
         *  TYPE: INTEGER
         */
        public static final String COLUMN_PRODUCT_QUANTITY= "quantity";

        /** Price of the product.
         *  TYPE: INTEGER
         */
        public static final String COLUMN_PRODUCT_PRICE= "price";

        /** Supplier of the product. */
        public static final String COLUMN_PRODUCT_SUPPLIER= "supplier";

        /** Supplier email for ordering the product. */
        public static final String COLUMN_PRODUCT_SUPPLIER_EMAIL= "supplier_email";

        /** Image of the product
         *  TYPE: BLOB
         */
        public static final String COLUMN_PRODUCT_IMAGE= "image";
    }
}
