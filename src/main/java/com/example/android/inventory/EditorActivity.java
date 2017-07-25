package com.example.android.inventory;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;

import android.support.v4.app.NavUtils;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.Integer.parseInt;


/**
 * Allows the user to insert a new product or modify an existing one.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 1;

    private static final String LOG_TAG = EditorActivity.class.getName();

    private static final String STATE_URI = "STATE_URI";

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * EditText field to enter the product name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the  product quantity
     */
    private EditText mQuantityEditText;

    /**
     * Button to increase quantity
     */
    private Button mIncreaseQuantityButton;

    /**
     * Button to decrease quantity
     */
    private Button mDecreaseQuantityButton;

    /**
     * EditText to enter the product  price
     */
    private EditText mPriceEditText;

    /**
     * ImageView to enter the product image
     */
    private ImageView mImageView;

    /**
     * Button to choose photo from the device.
     */
    private Button mChoosePhotoButton;

    private String mImagePath;

    /**
     * Uri variable for the image uri.
     */
    private Uri mUri;

    /**
     * EditText to enter the product supplier
     */
    private EditText mSupplierEditText;

    /**
     * EditText to enter the product supplier e- mail
     */
    private EditText mSupplierMailEditText;

    /**
     * Button to order a product
     */
    private Button mOrderButton;

    /**
     * Boolean flag that keeps track whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;


    /**
     * The OnTouchListener listens for any user touches on a View, implying that they are modifying
     * it (modifying the attributes of a product). When this happens the mProductHasChanged is
     * changed to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to check whether we are entering a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent doesn't contain a product content URI, we are creating inserting a new product.
        if (mCurrentProductUri == null) {
            // New product so change app bar title to "Add a product".
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" men option is hidden.
            invalidateOptionsMenu();
        } else {
            // Existing pet, so set app bar title to "Edit product".
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to display the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all views that will be used to display product data (for existing product)
        // or read user input from (for a new product).
        mNameEditText = (EditText) findViewById(R.id.product_name);
        mQuantityEditText = (EditText) findViewById(R.id.product_quantity);
        mIncreaseQuantityButton= (Button) findViewById(R.id.increase_quantity);
        mDecreaseQuantityButton= (Button) findViewById(R.id.decrease_quantity);
        mPriceEditText = (EditText) findViewById(R.id.product_price);
        mImageView = (ImageView) findViewById(R.id.product_image);
        mChoosePhotoButton= (Button) findViewById(R.id.choose_photo);
        mSupplierEditText = (EditText) findViewById(R.id.product_supplier);
        mSupplierMailEditText = (EditText) findViewById(R.id.product_email);
        mOrderButton= (Button) findViewById(R.id.order);

        // Set up OnTouchListeners on all the input fields, so we can determine whether
        // the user has touched or modified them. This will let us know if there are
        // unsaved changes, in case the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mIncreaseQuantityButton.setOnTouchListener(mTouchListener);
        mDecreaseQuantityButton.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierMailEditText.setOnTouchListener(mTouchListener);
        mChoosePhotoButton.setOnTouchListener(mTouchListener);

        mChoosePhotoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
               openImageSelector();
                Log.v(LOG_TAG, "Image Button pressed");
            }
        });

      mIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
                    public void onClick(View view){
                int quantity;
                if(!TextUtils.isEmpty(mQuantityEditText.getText().toString())){
                    quantity= parseInt(mQuantityEditText.getText().toString().trim());
                    quantity++;
                }
                else {
                    quantity= 1;
                }
                mQuantityEditText.setText((Integer.toString(quantity)));
            }
        });

       mDecreaseQuantityButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(!TextUtils.isEmpty(mQuantityEditText.getText().toString()));
                {
                    int quantity = parseInt(mQuantityEditText.getText().toString().trim());

                    if (quantity == 0) {
                        Toast.makeText(view.getContext(), "Cannot further decrease quantity", Toast.LENGTH_SHORT).show();
                    } else {
                        quantity--;
                    }
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String[] supplierEmail= new String[] {mSupplierMailEditText.getText().toString().trim()};
                String productName= mNameEditText.getText().toString().trim();
                composeEmail(supplierEmail, productName);
            }
        });

    }

    // Method that opens an intent to send an email to the supplier for ordering a product.
    private void composeEmail(String[] supplierEmail, String productName) {
        Intent emailIntent= new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:")); // Only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, supplierEmail);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_placement));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.order_text) + productName);
        if (emailIntent.resolveActivity(getPackageManager()) != null){
            startActivity(emailIntent);
        }
    }

    @Override
            protected void onSaveInstanceState(Bundle outState) {
                super.onSaveInstanceState(outState);

                if (mUri != null)
                    outState.putString(STATE_URI, mUri.toString());
            }

            @Override
            protected void onRestoreInstanceState(Bundle savedInstanceState) {
                super.onRestoreInstanceState(savedInstanceState);

                if (savedInstanceState.containsKey(STATE_URI) &&
                        !savedInstanceState.getString(STATE_URI).equals("")) {
                    mUri = Uri.parse(savedInstanceState.getString(STATE_URI));

                    ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mImageView.setImageBitmap(getBitmapFromUri(mUri));
                        }
                    });
                }
            }



    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
    public void openImageSelector(){
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());

                 mImagePath= mUri.getPath();

                mImageView.setImageBitmap(getBitmapFromUri(mUri));
                Log.v(LOG_TAG, "mImagePath is :" + mImagePath);
            }
        }
    }


    private void saveProduct() {

        // Read from input fields, using trim() to eliminate whitespace before or after the input
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String supplierEmailString = mSupplierMailEditText.getText().toString().trim();

        // Check if this is a new product and check if all fields are blank
        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(supplierString)
                && TextUtils.isEmpty(supplierEmailString) && TextUtils.isEmpty(mUri.toString())) {
            // Since no fields were modified, return early because no product was created.
            return;
            // Make sure that all required fields are filled, otherwise return early.
        } else if((TextUtils.isEmpty(nameString)) || (TextUtils.isEmpty(supplierString)) ||
                (TextUtils.isEmpty(supplierEmailString)) || (mUri==null)){
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(quantityString)){
            quantityString= "0";
        }

        if(TextUtils.isEmpty(priceString)){
            priceString= "0";
        }

        // Create a ContentValues Object where column names are the keys
        // and the product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mUri.toString());


        // check whether this is a new or an existing product by checking the mCurrentProductUri.
        if (mCurrentProductUri == null) {
            // This is a new product, so insert a new product into the provider,
            // returning the contentURI for the new pet.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // Null newUri means that there was an error.
                Toast.makeText(this, getString(R.string.product_insert_fail), Toast.LENGTH_SHORT).show();
            } else {
                // The product was inserted successfully.
                Toast.makeText(this, getString(R.string.product_insert_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an existing product, so we update the product with
            // content URI: mCurrentProductUri and pass in the changed contentValues. We pass
            // null for the selection and selection args because mCurrentProductUri will already
            // identify the correct row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether the update was successful or not.
            if (rowsAffected == 0) {
                // No rows affected means that the update was unsuccessful.
                Toast.makeText(this, getString(R.string.product_insert_fail), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise the update was successful.
                Toast.makeText(this, getString(R.string.product_insert_success), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // The editor shows all the product attributes, so we define a projection
        // containing all the columns of the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };
        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the contentURI for the current product
                projection,             // Columns to include in the query
                null,                   // No selection (where) clause
                null,                   // No selection args
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // If the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (this should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            String image =cursor.getString(imageColumnIndex);

            mNameEditText.setText(name);
            mQuantityEditText.setText(String.valueOf(quantity));
            mPriceEditText.setText(String.valueOf(price));
            mSupplierEditText.setText(supplier);
            mSupplierMailEditText.setText(supplierEmail);

           if(image!=null){
               mUri=Uri.parse(image);
            mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mSupplierMailEditText.setText("");
        mImageView.setImageResource(0);

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *  Perform the deletion of the product from the database.
     */
    private void deleteProduct(){
        // Only perform the delete if this is an existing product.
        if(mCurrentProductUri!= null){
            // Call the ContentResolver to delete the product at the given content Uri.
            // Pass in null for the selection and selection args because the
            // mCurrentProductUri  content URI already identifies the product to be deleted.
            int rowsDeleted= getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the deletion was successful.
            if(rowsDeleted==0){
                Toast.makeText(this, getString(R.string.editor_product_delete_failed),Toast.LENGTH_SHORT).show();
            }
            else{

                Toast.makeText(this, getString(R.string.editor_delete_product_success), Toast.LENGTH_SHORT).show();

            }
        }
        // close the activity
        finish();
    }
}
