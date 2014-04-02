/**
 * TrollyAdapter allows crossing items off the list and filtering
 * on user text input.
 * @author Ben
 *
 * Modified by Menaka Kiriwattuduwa
 * Moved nested TrollyAdapter class to its own file to promote better organization.
 *
 */


package caldwell.ben.trolly;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import caldwell.ben.provider.Trolly.ShoppingList;

public class TrollyAdapter extends SimpleCursorAdapter implements Filterable {

	private ContentResolver mContent;
	private String[] mPROJECTION;
	
	public TrollyAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String[] PROJ) {
		super(context, layout, c, from, to);
		mContent = context.getContentResolver();
		mPROJECTION = PROJ;
	}
	
	@Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

        StringBuilder buffer = null;
        String[] args = null;
        if (constraint != null) {
            buffer = new StringBuilder();
            buffer.append("UPPER(");
            buffer.append(ShoppingList.ITEM);
            
            buffer.append(") GLOB ?");
            args = new String[] { "*" + constraint.toString().toUpperCase() + "*" };
        }

        return mContent.query(ShoppingList.CONTENT_URI, mPROJECTION,
                buffer == null ? null : buffer.toString(), args,
                null);
    }

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView item = (TextView)view.findViewById(R.id.item);
		item.setText(cursor.getString(cursor.getColumnIndex(ShoppingList.ITEM)));
	
		TextView quantity = (TextView)view.findViewById(R.id.quantity);
		quantity.setText(cursor.getString(cursor.getColumnIndex(ShoppingList.QUANTITY)));
		
		TextView units = (TextView)view.findViewById(R.id.units);
		units.setText(cursor.getString(cursor.getColumnIndex(ShoppingList.UNITS)));
		
		TextView price = (TextView)view.findViewById(R.id.price);
		price.setText(cursor.getString(cursor.getColumnIndex(ShoppingList.TOTALPRICE)));
		
		TextView priority = (TextView)view.findViewById(R.id.priority);
		priority.setText(cursor.getString(cursor.getColumnIndex(ShoppingList.PRIORITY)));
		
		switch(cursor.getInt(cursor.getColumnIndex(ShoppingList.STATUS))){
		case ShoppingList.OFF_LIST:
			item.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			quantity.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			units.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			price.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			priority.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			
			item.setTextColor(Color.DKGRAY);
			quantity.setTextColor(Color.DKGRAY);
			units.setTextColor(Color.DKGRAY);
			price.setTextColor(Color.DKGRAY);
			priority.setTextColor(Color.DKGRAY);
			break;
		case ShoppingList.ON_LIST:
			item.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			quantity.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			units.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			price.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			priority.setPaintFlags(item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			
			item.setTextColor(Color.GREEN);
			quantity.setTextColor(Color.WHITE);
			units.setTextColor(Color.WHITE);
			price.setTextColor(Color.WHITE);
			priority.setTextColor(Color.GREEN);
			break;
		case ShoppingList.IN_TROLLEY:
			item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			quantity.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			units.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			price.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			priority.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			
			item.setTextColor(Color.GRAY);
			quantity.setTextColor(Color.GRAY);
			units.setTextColor(Color.GRAY);
			price.setTextColor(Color.GRAY);
			priority.setTextColor(Color.GRAY);
			break;
		}
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(ShoppingList.ITEM));
	}
	
}
	