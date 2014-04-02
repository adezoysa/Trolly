/**
 * Added By: Hantao Zhao
 * Class: TrollyAdapterForList
 * Functionality: New Adapter for list performance
 *
 * Modified by Menaka Kiriwattuduwa
 * Moved nested TrollyAdapterForList class from inside Trolly class 
 * to its own file to promote better organization.
 *
 */

package caldwell.ben.trolly;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import caldwell.ben.provider.Trolly.ShoppingList;
import caldwell.ben.trolly.R;

public class TrollyAdapterForList extends SimpleCursorAdapter
implements Filterable {

	private ContentResolver mContent;
	private String[] mLISTPROJECTION;
	
	ArrayList<String> listsarray = new ArrayList<String>();
	
	public TrollyAdapterForList(Context context, int layout, Cursor c,
			String[] from, int[] to, String[] proj) {
		super(context, layout, c, from, to);
		mContent = context.getContentResolver();
		mLISTPROJECTION = proj;
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
			buffer.append(ShoppingList.LISTNAME);
			buffer.append(") GLOB ?");
			args = new String[] { "*" + constraint.toString().toUpperCase()
					+ "*" };
		}
	
		return mContent.query(ShoppingList.CONTENT_URI, mLISTPROJECTION,
				buffer == null ? null : buffer.toString(), args, null);
	}
	
	@Override
	// bindView
	public void bindView(View view, Context context, Cursor cursor) {
		TextView item = (TextView) view.findViewById(R.id.item);
		String toAdd = cursor.getString(cursor
				.getColumnIndex(ShoppingList.LISTNAME));
	
		if (!listsarray.contains(toAdd)) {
			listsarray.add(toAdd);
			item.setText(toAdd);
		}
	}
	
	@Override
	public CharSequence convertToString(Cursor cursor) {
		String toAdd = cursor.getString(cursor
				.getColumnIndex(ShoppingList.LISTNAME));
		if (!listsarray.contains(toAdd)) {
			return toAdd;
		} else
			return null;
	}
}