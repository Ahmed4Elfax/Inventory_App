package com.example.android.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.app.LoaderManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.inventoryapp.data.ItemContract.ItemEntry;


public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private Uri inventoryUri;
    private static final int EXISTING_INVENTORY_LOADER = 0;

    ImageView productImage;
    private TextView mName;
    private TextView mPrice;
    private TextView mQuantity;
    private TextView mSupplier;
    private TextView mEmail;
    String supplier;
    String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setTitle("Details");

        productImage = (ImageView) findViewById(R.id.details_product_image);
        mName = (TextView) findViewById(R.id.details_product_name);
        mPrice = (TextView) findViewById(R.id.details_product_price);
        mQuantity = (TextView) findViewById(R.id.details_product_quantity);
        mSupplier = (TextView) findViewById(R.id.details_product_supname);
        mEmail = (TextView) findViewById(R.id.details_product_supmail);


        inventoryUri = getIntent().getData();
        Button sellButton = (Button) findViewById(R.id.addProductButton);


        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                final EditText edittext = new EditText(v.getContext());
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setMessage(getString(R.string.category_measurement));
                builder.setView(edittext);

                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int subtract;
                        if (TextUtils.isEmpty(edittext.getText().toString().trim())) {
                            subtract = 0;
                        } else {
                            subtract = Integer.parseInt(edittext.getText().toString().trim());
                        }

                        String[] projection = {
                                ItemEntry._ID,
                                ItemEntry.COLUMN_ITEM_NAME,
                                ItemEntry.COLUMN_ITEM_QUANTITY,
                                ItemEntry.COLUMN_ITEM_PRICE,
                                ItemEntry.COLUMN_ITEM_SUPPLIER,
                                ItemEntry.COLUMN_ITEM_EMAIL,
                                ItemEntry.COLUMN_ITEM_IMAGE
                        };
                        Cursor data = getContentResolver().query(inventoryUri, projection, null, null, null);

                        if (data.moveToFirst()) {
                            do {
                                int intName = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
                                int intPrice = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
                                int intQuantity = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
                                int intSupplier = data.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
                                int intEmail = data.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL);


                                String name = data.getString(intName);
                                int price = data.getInt(intPrice);
                                supplier = data.getString(intSupplier);
                                email = data.getString(intEmail);
                                int quantity = data.getInt(intQuantity);


                                if (quantity - subtract >= 0) {
                                    int current = (quantity - subtract);

                                    ContentValues val = new ContentValues();
                                    val.put(ItemEntry.COLUMN_ITEM_NAME, name);
                                    val.put(ItemEntry.COLUMN_ITEM_PRICE, price);
                                    val.put(ItemEntry.COLUMN_ITEM_SUPPLIER, supplier);
                                    val.put(ItemEntry.COLUMN_ITEM_EMAIL, email);
                                    val.put(ItemEntry.COLUMN_ITEM_QUANTITY, current);

                                    int rowsAffected = getContentResolver().update(inventoryUri, val, null, null);
                                } else if (quantity - subtract < 0) {
                                    Toast.makeText(DetailsActivity.this, "Wrong input only " + quantity + " products available !", Toast.LENGTH_SHORT).show();
                                }

                            }
                            while (data.moveToNext());
                        }


                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_current_product:
                showDeleteConfirmationDialog();
                return true;

            case R.id.edit_current_product:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
                intent.setData(inventoryUri);
                startActivity(intent);
                return true;

            case R.id.details_order:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.ordering_products));
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{supplier});
                String message = "Dear Product Supplier! \n\nWe would like to place the following order of " + mName.getText().toString().trim() + "\n Please let us know when we can expect the delivery.";
                i.putExtra(android.content.Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(i, getString(R.string.send_email)));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete));
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        if (inventoryUri != null) {
            int rowDeleted = getContentResolver().delete(inventoryUri, null, null);


            if (rowDeleted == 0) {

                Toast.makeText(this, getString(R.string.delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, getString(R.string.delete_successful),
                        Toast.LENGTH_SHORT).show();
            }

            finish();
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
                    productImage.setImageResource(R.drawable.baseline_image_black_48);
                } else {
                    Bitmap image = BitmapFactory.decodeByteArray(b, 0, b.length);
                    productImage.setImageBitmap(image);

                }
                mName.setText(name);
                mPrice.setText(String.valueOf(price));
                mQuantity.setText(String.valueOf(quantity));
                if (TextUtils.isEmpty(supplier)) {
                    String sup = "Un Known";
                    mSupplier.setText(sup);
                } else {
                    mSupplier.setText(supplier);
                }


                if (TextUtils.isEmpty(email)) {
                    String sup = "Un Known";
                    mEmail.setText(sup);
                } else {
                    mEmail.setText(email);
                }
                mQuantity.setText(String.valueOf(quantity));
            }
            while (data.moveToNext());
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        productImage.setImageResource(R.drawable.baseline_image_black_48);
        mName.setText("");
        mPrice.setText("");
        mSupplier.setText("");
        mEmail.setText("");
        mQuantity.setText("");

    }
}