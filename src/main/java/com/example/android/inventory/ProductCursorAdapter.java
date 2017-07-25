package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

/**
 * {@link ProductCursorAdapter} is an adapter for a list that uses
 * a {@link Cursor} of product data as its data source. The adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter{

    private static final String LOG_TAG = ProductCursorAdapter.class.getName();
    /**
     *
     * @param context The app context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c){
        super(context, c, 0 /*flags */);
    }

    /**
     * Makes a new blank list item. No data is inserted (bound) in the views yet.
     *
     * @param context   The app context
     * @param cursor    The cursor from which to get the data. The cursor is already
     *                  moved to the correct position.
     * @param parent    The parent view to which the new view is attached to.
     * @return          The newly created list.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * this method binds the product data (in the current row pointed to by the cursor) to the given
     * list item layout.
     *
     * @param view      Existing view, returned earlier by the newView() method.
     * @param context   The app context
     * @param cursor    The cursor from which to get the data. The cursor is already
     *                  moved to the correct position.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find the views for the product name, quantity and price to populate in the inflated template
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        Button sellButton = (Button) view.findViewById(R.id.sell_button);

        // Find the columns of the attributes we are interested in
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        // Read the attributes from the cursor for the current product
        final int productId = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                sellProduct(context, productQuantity, currentProductUri);
            }
        });

        // Update the text views with the attributes
        nameView.setText(productName);
        quantityView.setText(String.valueOf(productQuantity));
        priceView.setText(String.valueOf(productPrice));
    }
        int sellProduct(Context context, int quantity, Uri uri){
        if(quantity==0){
            Toast.makeText(context, R.string.product_out_of_stock, Toast.LENGTH_SHORT).show();
            return 0;
        }
        else{
            int newQuantity= quantity-1;
            ContentValues values= new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
            int rowsAffected= context.getContentResolver().update(uri, values, null, null);
            context.getContentResolver().notifyChange(uri, null);
            Log.v(LOG_TAG, "rows affected: " + rowsAffected);
            Log.v(LOG_TAG, "rows affected quantity now is " + newQuantity);
            return rowsAffected;
        }
    }
}
