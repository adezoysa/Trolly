/**
 * AutoFillAdapter is an adapter for the AutoCompleteTextView at the top of the Trolly Activity
 * @author Ben Caldwell
 *
 * Modified by Menaka Kiriwattuduwa
 * Moved nested AutoFillAdapter class to its own file to promote better organization.
 *
 */

package caldwell.ben.trolly;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;
import caldwell.ben.provider.Trolly.ShoppingList;

public class AutoFillAdapter extends CursorAdapter implements Filterable {

	private ContentResolver mContent;
	private String[] mPROJECTION;
	
	public AutoFillAdapter(Context context, Cursor c, String[] proj) {
		super(context, c);
		mContent = context.getContentResolver();
		mPROJECTION = proj;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((TextView) view).setText(cursor.getString(cursor.getColumnIndex(ShoppingList.ITEM)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(context);
		final TextView view = (TextView)inflater.inflate(
											android.R.layout.simple_dropdown_item_1line, 
											parent,false);
		view.setText(cursor.getString(cursor.getColumnIndex(ShoppingList.ITEM)));
		return view;
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(ShoppingList.ITEM)); 
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
}
