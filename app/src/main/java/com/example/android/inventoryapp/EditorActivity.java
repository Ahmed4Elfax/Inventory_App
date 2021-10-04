package com.example.android.inventoryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.android.inventoryapp.data.ItemContract.ItemEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int SELECT_PHOTO = 100;
    private static final int EXISTING_URI = 0;
    ImageView inventoryImage;
    private Bitmap inventory_image;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;
    private EditText mEmailEditText;

    private Uri inventoryUri;

    private boolean mInventoryHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        } else {
            return null;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        inventoryUri = getIntent().getData();

        if (inventoryUri == null) {
            setTitle(getString(R.string.add_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_product));
            getLoaderManager().initLoader(EXISTING_URI, null, this);
        }


        inventoryImage = (ImageView) findViewById(R.id.product_image);
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mEmailEditText = (EditText) findViewById(R.id.edit_product_email);

        FloatingActionButton gallery = (FloatingActionButton) findViewById(R.id.add_image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);


        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                trySelector();
            }
        });

    }

    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intentType));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectPicture)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    // SELECT_PHOTO:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();

            try {
                inventory_image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            inventoryImage.setImageBitmap(inventory_image);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:

                String stringName = mNameEditText.getText().toString().trim();
                String stringPrice = mPriceEditText.getText().toString().trim();
                String stringQuantity = mQuantityEditText.getText().toString().trim();
                String stringSupplier = mSupplierEditText.getText().toString().trim();
                String stringEmail = mEmailEditText.getText().toString().trim();

                byte[] img = getBytes(inventory_image);

                if ((stringName.length() < 3) || (stringPrice.length() < 1 || stringPrice.length() > 10) || (stringQuantity.length() < 1 || stringQuantity.length() > 10) || (stringSupplier.length() < 3) || (stringEmail.length() < 3))
                    showToast(getString(R.string.add_all_info));

                if ((img == null)&& (inventoryUri == null))  {
                    showToast(getString(R.string.selectPicture));
                }

                else {
                    saveProduct();
                    finish();
                }
                return true;

            case android.R.id.home:
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                NavUtils.navigateUpFromSameTask(EditorActivity.this);

                            }
                        };

                showUnsavedChangedDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }





    @Override
    public void onBackPressed() {
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                    }
                };

        showUnsavedChangedDialog(discardButtonClickListener);
    }

    private void showUnsavedChangedDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.discard_changes));
        builder.setPositiveButton(getString(R.string.discard), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveProduct() {

        String stringName = mNameEditText.getText().toString().trim();
        String stringPrice = mPriceEditText.getText().toString().trim();
        String stringQuantity = mQuantityEditText.getText().toString().trim();
        String stringSupplier = mSupplierEditText.getText().toString().trim();
        String stringEmail = mEmailEditText.getText().toString().trim();

        byte[] img = getBytes(inventory_image);

        if (inventoryUri == null &&
                TextUtils.isEmpty(stringName) && TextUtils.isEmpty(stringPrice)
                && TextUtils.isEmpty(stringQuantity) && TextUtils.isEmpty(stringSupplier) && TextUtils.isEmpty(stringEmail)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, stringName);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, stringPrice);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, stringQuantity);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, stringSupplier);
        values.put(ItemEntry.COLUMN_ITEM_EMAIL, stringEmail);

        if (img != null) {
            values.put(ItemEntry.COLUMN_ITEM_IMAGE, img);

        }

        if (inventoryUri == null) {

            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                showToast(getString(R.string.data_insert_failed));
            } else {
                showToast(getString(R.string.data_insert_successful));
            }
        } else {

            int rowsAffected = getContentResolver().update(inventoryUri, values, null, null);

            if (rowsAffected == 0) {
                showToast(getString(R.string.data_update_failed));
            } else {
                showToast(getString(R.string.data_update_successful));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_EMAIL,
                ItemEntry.COLUMN_ITEM_IMAGE
        };


        return new CursorLoader(this,
                inventoryUri,
                projection,
                null,
                null,
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            do {
                int intName = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
                int intPrice = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
                int intQuantity = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
                int intSupplier = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
                int intEmail = data.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL);
                int intImage = data.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);

                String name = data.getString(intName);
                int price = data.getInt(intPrice);
                String supplier = data.getString(intSupplier);
                String email = data.getString(intEmail);
                int quantity = data.getInt(intQuantity);
                byte[] b = data.getBlob(intImage);

                if (b == null) {
                    inventoryImage.setImageResource(R.drawable.baseline_image_black_48);
                } else {
                    Bitmap image = BitmapFactory.decodeByteArray(b, 0, b.length);
                    inventoryImage.setImageBitmap(image);

                }
                mNameEditText.setText(name);
                mPriceEditText.setText(String.valueOf(price));
                mSupplierEditText.setText(supplier);
                mEmailEditText.setText(email);
                mQuantityEditText.setText(String.valueOf(quantity));
            }
            while (data.moveToNext());
        }

    }

    private void showToast(String string) {

        Toast.makeText(EditorActivity.this, string, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        inventoryImage.setImageResource(R.drawable.baseline_image_black_48);
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mEmailEditText.setText("");
        mQuantityEditText.setText("");

    }
}