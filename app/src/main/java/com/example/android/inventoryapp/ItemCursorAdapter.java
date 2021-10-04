package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

import androidx.cursoradapter.widget.CursorAdapter;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
class ItemCursorAdapter extends CursorAdapter {
    private CatalogActivity activity = new CatalogActivity();
    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

       // final long id;
        final int quantity;
        final String name;
        final int price;
        //final String supplier;
       // final String email;


        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_quantity);
        //ImageView inventorySale = (ImageView) view.findViewById(R.id.list_item_button);

       // id = cursor.getLong(cursor.getColumnIndex(ItemEntry._ID));
        name = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME));
        price = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE));
        quantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));
        //supplier = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER));
       // email = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL));



        nameTextView.setText(name);
        priceTextView.setText("Price: " + String.valueOf(price) + " $");
        quantityTextView.setText("Quantity: " + String.valueOf(quantity));
    }
}
