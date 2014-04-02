/**
	<Trolly is a simple shopping list application for android phones.>
	Copyright (C) 2009  Ben Caldwell
 	
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
   

package caldwell.ben.trolly;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import caldwell.ben.provider.Trolly.ShoppingList;

public class Trolly extends ListActivity {
	
//	private static final String TAG = "Trolly";
	
	public static final String KEY_ITEM = "items";
	public static boolean adding = false;
	/* Added by: Hantao Zhao
     * ---Add---
     */
	public static boolean listMode = false;
	
	/* Added by: Hantao Zhao
     * currentList
     */
	public String currentList = "Default List";
	
	/**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
    	ShoppingList._ID, // 0
        ShoppingList.ITEM, // 1
        /* Added by: Achini De Zoysa
         * projection for each new column
         */
        ShoppingList.QUANTITY,//NEW
        ShoppingList.UNITS,//NEW
        ShoppingList.PRICE,//NEW
        ShoppingList.IMAGE_FILE_PATH,//NEW (Added by Menaka Kiriwattuduwa
        ShoppingList.TOTALPRICE,//NEW
        ShoppingList.PRIORITY,//NEW
        /* Added by: Hantao Zhao
         * projection for listname
         */
        ShoppingList.LISTNAME, 
        ShoppingList.STATUS, // 2
    };
    
    /* Added by: Hantao Zhao
     * ListProjection
     */
    private static final String[] LISTPROJECTION = new String[] {
		ShoppingList._ID, // 0
		ShoppingList.STATUS, // 2
		ShoppingList.LISTNAME, };

    // Menu item ids
    public static final int MENU_ITEM_DELETE = Menu.FIRST;
    public static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
    public static final int MENU_ITEM_CHECKOUT = Menu.FIRST + 2;
    public static final int MENU_ITEM_PREFERENCE = Menu.FIRST + 3;
    public static final int MENU_ITEM_ON_LIST = Menu.FIRST + 4;
    public static final int MENU_ITEM_OFF_LIST = Menu.FIRST + 5;
    public static final int MENU_ITEM_IN_TROLLEY = Menu.FIRST + 6;
    public static final int MENU_ITEM_EDIT = Menu.FIRST + 7;
    public static final int MENU_ITEM_CLEAR = Menu.FIRST + 8;
    public static final int MENU_ITEM_RESET = Menu.FIRST + 9;
    
    /* Added by: Hantao Zhao
     * Add for the button of the list
     */
 	public static final int MENU_LISTS = Menu.FIRST + 10;
 	public static final int MENU_SHOPPING = Menu.FIRST + 11;
    
    /**
     * Case selections for the type of dialog box displayed
     */
    private static final int DIALOG_DELETE = 1;
    private static final int DIALOG_EDIT = 2;
    private static final int DIALOG_CLEAR = 3;
    private static final int DIALOG_RESET = 4;
       
  //Use private members for dialog textview to prevent weird persistence problem
	private EditText mDialogEdit;
	/**Added By: Achini De Zoysa
	 * mDialog for each new columns
	**/
	private EditText mDialogEditQUANTITY;
	private EditText mDialogEditPRICE;
	private EditText mDialogEditUNITS;
	private EditText mDialogEditPRIORITY;
	private TextView mDialogText;
	private View mDialogView;
	
	/**Added By: Menaka Kiriwattuduwa
	 * mDialog for camera features, acceleration/shake detection
	**/
	private ImageButton mDialogButtonIMAGE;
	private ImageView mDialogThumbnail;

	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_LOAD_IMAGE = 2;
	private String mPhotoFilePath = null;

	private Cursor mCursor;
	private AutoCompleteTextView mTextBox;
	private Button btnAdd;
	private TrollyAdapter mAdapter;
	
	/* Added by: Hantao Zhao
     * Adapter for the list
     */
	private TrollyAdapterForList mAdapterForList;
	private SharedPreferences mPrefs;

	private Uri mUri;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(ShoppingList.CONTENT_URI);
        }
              
        setContentView(R.layout.trolly);  
        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);
               
		adding = false;
		updateList();
              
        mTextBox = (AutoCompleteTextView)findViewById(R.id.textbox);
        btnAdd = (Button)findViewById(R.id.btn_add);
        
        mTextBox.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View view) {
				//If the text box is clicked while full-list adding, stop adding
				if (adding) {
					adding = false;
					updateList();
				}
			}
	});

        btnAdd.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View view) {
			
			/*Added by: Hantao Zhao
			 * Listmode vs Shopping mode
			 */
			if (!listMode) {
				//If there is a string in the textbox then add it to the list
				
				if (mTextBox.getText().length() > 0) {
					Cursor c = getContentResolver().query(
							getIntent().getData(),
							PROJECTION,
							ShoppingList.ITEM + "='" + mTextBox.getText()
									+ "'" + " AND listname='" + currentList
									+ "'", 
							null, null);
					c.moveToFirst();
					if (c == null 
							|| c.isBeforeFirst() 
							|| c.getInt(c.getColumnIndex(ShoppingList.STATUS))==ShoppingList.ON_LIST) {
						ContentValues values = new ContentValues();
						values.put(ShoppingList.ITEM, mTextBox.getText().toString());
						/*Added by: Hantao Zhao
						 * Replace the listname on Click
						 */
						values.put(ShoppingList.LISTNAME, currentList);
						getContentResolver().insert(ShoppingList.CONTENT_URI,values);
					} else {
						ContentValues values = new ContentValues();
						values.put(ShoppingList.STATUS, ShoppingList.ON_LIST);
						long id = c.getLong(c.getColumnIndex(ShoppingList._ID));
						Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
						getContentResolver().update(uri, values, null, null);
					}
	        		mTextBox.setText("");
				} else {
					adding = !adding;
					updateList();
				}
				}
			
			/*Added By: Hantao Zhao
			 * Else statement
			 */
			// List mode to add new list Name
			else {
				if (mTextBox.getText().length() > 0) {
					Cursor c = getContentResolver().query(
							getIntent().getData(),
							PROJECTION,
							ShoppingList.ITEM + "='" + mTextBox.getText()
									+ "'" + " AND listname='" + currentList
									+ "'", 	null, null);
					c.moveToFirst();

					ContentValues values = new ContentValues();
					values.put(ShoppingList.ITEM, "");
					values.put(ShoppingList.LISTNAME, mTextBox.getText()
							.toString());
					getContentResolver().insert(ShoppingList.CONTENT_URI, values);
					updateListView();
				} else {
					adding = !adding;
					updateListView();
				}
				mTextBox.setText("");
			}
		}
        });
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (intent.hasExtra("org.openintents.intents.ShoppingListIntents.EXTRA_STRING_ARRAYLIST_SHOPPING"))
        	addExtraItems();
    }
    
	protected void updateList() { //TODO
		/*Added By: Hantao Zhao
		 * Change the set up statement to include the list name
		 */
        //set up the list cursor
		mCursor = managedQuery(getIntent().getData(), PROJECTION, adding ? "listname='" + currentList
				+ "'" + " AND item !=''": ShoppingList.STATUS + "<>" + ShoppingList.OFF_LIST
						+ " AND listname='" + currentList + "'"
						+ " AND item !=''", null,
				ShoppingList.DEFAULT_SORT_ORDER);

        //set the list adapter
		mAdapter = new TrollyAdapter(this, R.layout.shoppinglist_item, mCursor,
				
	/**
     * Changed by: Achini De Zoysa
     * Changes: Added new columns QUANTITY, UNITS, PRICE, PRIORITY, TOTALPRICE
     */
		new String[] { ShoppingList.ITEM,ShoppingList.QUANTITY,ShoppingList.UNITS,ShoppingList.IMAGE_FILE_PATH,ShoppingList.TOTALPRICE,ShoppingList.PRIORITY}, new int[] 
				{ R.id.item, R.id.quantity,R.id.units, R.string.image, R.id.price, R.id.priority}, PROJECTION);
		setListAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		adding = false;
		updateList();
		
		Cursor cAutoFill = managedQuery(getIntent().getData(), 
				PROJECTION, 
				null, 
				null,
				ShoppingList.DEFAULT_SORT_ORDER);
		
		AutoFillAdapter autoFillAdapter = new AutoFillAdapter(this, cAutoFill, PROJECTION);
		mTextBox.setAdapter(autoFillAdapter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.commit();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		//Added by : Hantao Zhao
		if (!listMode) {
			//super.onListItemClick(l, v, position, id);
			Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
			Cursor c = getContentResolver().query(uri, PROJECTION, null, null, null);
			c.moveToFirst();
			ContentValues values = new ContentValues();
			switch (c.getInt(c.getColumnIndex(ShoppingList.STATUS)))
			{
			case ShoppingList.OFF_LIST:
				//move from off the list to on the list
				values.put(ShoppingList.STATUS, ShoppingList.ON_LIST);
				getContentResolver().update(uri, values, null, null);
				break;
			case ShoppingList.ON_LIST:
				values.put(ShoppingList.STATUS, ShoppingList.IN_TROLLEY);
				getContentResolver().update(uri, values, null, null);
				break;
			case ShoppingList.IN_TROLLEY:
				//move back from in the trolley to on the list
				values.put(ShoppingList.STATUS, ShoppingList.ON_LIST);
				getContentResolver().update(uri, values, null, null);
				break;
			}
			if (adding) {
				adding = false;				
				updateList();
			}
		}

		/*Added By: Hantao Zhao
		 * Handle the click when the mode is list menu
		 * */


		else {
			Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
			Cursor c = getContentResolver().query(uri, PROJECTION, null, null,
					null);
			c.moveToFirst();

			String list = c.getString(c.getColumnIndex(ShoppingList.LISTNAME));
			currentList = list;
			updateList();
			mTextBox.setHint("Add an item");
			listMode = false;

		}		
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		if (!listMode) {
			
	
		AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }
        
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return false;
        }
        
        mUri = ContentUris.withAppendedId(getIntent().getData(), 
        									cursor.getLong(cursor.getColumnIndex(ShoppingList._ID)));
		Cursor c = getContentResolver().query(mUri, PROJECTION, null, null, null);
		c.moveToFirst();
		ContentValues values = new ContentValues();

        switch (item.getItemId()) {
	        case MENU_ITEM_ON_LIST:
                // Change to "on list" status
	        	values.put(ShoppingList.STATUS, ShoppingList.ON_LIST);
	        	getContentResolver().update(mUri, values, null, null);
	        	return true;	        	
	        case MENU_ITEM_OFF_LIST:
                // Change to "off list" status
	        	values.put(ShoppingList.STATUS, ShoppingList.OFF_LIST);
	        	getContentResolver().update(mUri, values, null, null);
                return true;
	        case MENU_ITEM_IN_TROLLEY:
	        	//Change to "in trolley" status
	        	values.put(ShoppingList.STATUS, ShoppingList.IN_TROLLEY);
	        	getContentResolver().update(mUri, values, null, null);
	        	return true;
	        case MENU_ITEM_EDIT:
	        	//Show edit dialog
	        	showDialog(DIALOG_EDIT);
	        	/**
	             * Changed by: Achini De Zoysa
	             * Changes: Added new columns QUANTITY, UNITS, PRICE, PRIORITY, TOTALPRICE to edit Dialog
	             */
	        	mDialogEdit.setText(c.getString(c.getColumnIndex(ShoppingList.ITEM)));
	        	mDialogEditQUANTITY.setText(c.getString(c.getColumnIndex(ShoppingList.QUANTITY)));
	        	mDialogEditUNITS.setText(c.getString(c.getColumnIndex(ShoppingList.UNITS)));
	        	mDialogEditPRICE.setText(c.getString(c.getColumnIndex(ShoppingList.PRICE)));
	        	mDialogEditPRIORITY.setText(c.getString(c.getColumnIndex(ShoppingList.PRIORITY)));
	        	
	        	/**
	             * Changed by: Menaka Kiriwattuduwa
	             * Changes: Update photo file path
	             */
	        	mPhotoFilePath = c.getString(c.getColumnIndex(ShoppingList.IMAGE_FILE_PATH));
	        	createThumbnail();	        	
	        	
	        	return true;
	        case MENU_ITEM_DELETE:
	        	//Show are you sure dialog then delete
	        	showDialog(DIALOG_DELETE);
	        	mDialogText.setText(c.getString(c.getColumnIndex(ShoppingList.ITEM)));
	        	return true;
        	}
        }
		/*Added By: Hantao Zhao
		 * The list interface pop out window for handling delete
		 * */
     		else {
     			AdapterView.AdapterContextMenuInfo info;
     			try {
     				info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
     			} catch (ClassCastException e) {
     				return false;
     			}

     			Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
     			if (cursor == null) {
     				// For some reason the requested item isn't available, do
     				// nothing
     				return false;
     			}

     			mUri = ContentUris.withAppendedId(getIntent().getData(),
     					cursor.getLong(cursor.getColumnIndex(ShoppingList._ID)));
     			Cursor c = getContentResolver().query(mUri, PROJECTION, null, null,
     					null);
     			c.moveToFirst();
     			ContentValues values = new ContentValues();
     			switch (item.getItemId()) {
     			case MENU_ITEM_DELETE:
     				showDialog(DIALOG_DELETE);
     				currentList = c.getString(c
     						.getColumnIndex(ShoppingList.LISTNAME));
     				mDialogText.setText(c.getString(c
     						.getColumnIndex(ShoppingList.LISTNAME)));

     				break;
     			}
     			return true;
     		}
        return false;
        
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		//Added By: Hantao
		if (!listMode) {
			
	
		AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            return;
        }
		Cursor cursor = (Cursor)getListAdapter().getItem(info.position);
		if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(ShoppingList.ITEM)));
        int status = cursor.getInt(cursor.getColumnIndex(ShoppingList.STATUS));
        
    	//Add context menu items depending on current state
    	switch (status) {
        case ShoppingList.OFF_LIST:
        	menu.add(0, MENU_ITEM_ON_LIST, 0, R.string.move_on_list);
        	menu.add(0, MENU_ITEM_IN_TROLLEY, 0, R.string.move_in_trolley);
        	break;
        case ShoppingList.ON_LIST:
        	menu.add(0, MENU_ITEM_IN_TROLLEY, 0, R.string.move_in_trolley);
        	menu.add(0, MENU_ITEM_OFF_LIST, 0, R.string.move_off_list);
        	break;
        case ShoppingList.IN_TROLLEY:
        	menu.add(0, MENU_ITEM_ON_LIST, 0, R.string.move_on_list);
        	menu.add(0, MENU_ITEM_OFF_LIST, 0, R.string.move_off_list);
        	break;
        }
    	
        // Add context menu items that are relevant for all items
    	menu.add(0, MENU_ITEM_EDIT, 0, R.string.edit_item);
    	menu.add(0, MENU_ITEM_DELETE, 0, R.string.delete_item);
		
    	/*Added By: Hantao Zhao
		 * Constructor for the Menu
		 * */
		} else {
			AdapterView.AdapterContextMenuInfo info;
			try {
				info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			} catch (ClassCastException e) {
				return;
			}
			Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
			if (cursor == null) {
				// For some reason the requested item isn't available, do
				// nothing
				return;
			}
			menu.setHeaderTitle(cursor.getString(cursor
					.getColumnIndex(ShoppingList.LISTNAME)));
			menu.add(0, MENU_ITEM_DELETE, 0, R.string.delete_item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		/*Added By: Hantao Zhao
		 *list menu within the menu
		 * */
		menu.add(0, MENU_LISTS, 2, R.string.lists).setIcon(android.R.drawable.ic_menu_more); 
		/*Added By: Hantao Zhao
		 *list menu within the menu
		 * */
		menu.add(0, MENU_SHOPPING, 2, R.string.shopping).setIcon(android.R.drawable.ic_menu_edit); 
		menu.add(0, MENU_ITEM_CHECKOUT, 2, R.string.checkout)
        .setIcon(android.R.drawable.ic_media_next);
		menu.add(0, MENU_ITEM_CLEAR, 3, R.string.clear_list)
        .setIcon(android.R.drawable.ic_menu_revert);
		menu.add (0, MENU_ITEM_PREFERENCE, 4, R.string.preferences)
        .setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_ITEM_RESET, 5, R.string.reset_list)
        .setIcon(android.R.drawable.ic_menu_delete);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/*Added By: Hantao Zhao
		 *Add new button lists to change to lists interface
		 * */
		case MENU_LISTS:
			listMode = true;
			mTextBox.setHint("Add a list");
			updateListView();
			return true;
		case MENU_SHOPPING:
			mTextBox.setHint("Add an item");
			updateList();
			break;
			
		// case MENU_LISTS			
        case MENU_ITEM_CHECKOUT:
        	//Change all items from in trolley to off list
        	checkout();
        	return true;
        case MENU_ITEM_CLEAR:
        	//Change all items to off list
        	showDialog(DIALOG_CLEAR);
        	mDialogText.setText(R.string.clear_prompt);
        	return true;
        case MENU_ITEM_RESET:
        	//Change all items to off list
        	showDialog(DIALOG_RESET);
        	mDialogText.setText(R.string.reset_prompt);
        	return true;
        case MENU_ITEM_PREFERENCE:
        	startActivity(new Intent(this,TrollyPreferences.class));
        	return true;
        }
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = LayoutInflater.from(this);
		switch (id) {
		/**
         * Changed by: Achini De Zoysa
         * Changes: Added new columns QUANTITY, UNITS, PRICE, PRIORITY, TOTALPRICE to edit dialog
         */
		case DIALOG_EDIT:
            mDialogView = factory.inflate(R.layout.dialog_edit, null);
            mDialogEdit = (EditText)mDialogView.findViewById(R.id.edit);
            mDialogEditQUANTITY = (EditText)mDialogView.findViewById(R.id.editquantity);
            mDialogEditPRICE = (EditText)mDialogView.findViewById(R.id.editprice);
            mDialogEditUNITS = (EditText)mDialogView.findViewById(R.id.editunits);
            mDialogEditPRIORITY = (EditText)mDialogView.findViewById(R.id.editpriority);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.edit_item)
                .setView(mDialogView);
            builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                	
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	/* User clicked OK so do some stuff */
                		
                		String text = mDialogEdit.getText().toString();
            			String price = mDialogEditPRICE.getText().toString();
                		String quantity = mDialogEditQUANTITY.getText().toString();
                		String priority = mDialogEditPRIORITY.getText().toString();
                		String units = mDialogEditUNITS.getText().toString();
    
                		text = text.trim();
                		ContentValues values = new ContentValues();
                		
                		values.put(ShoppingList.ITEM, text);
                		
                		// Calculate the total price according to the give quantity
                		if (price != null) {
                			values.put(ShoppingList.PRICE, price);
                			
                			try {
                				double Dprice = Double.parseDouble(price);
                				
                				if (!TextUtils.isEmpty(quantity)) {
                					double Dquantity = Double.parseDouble(quantity);
                					Dprice = Dquantity * Dprice;
                					NumberFormat nf = NumberFormat.getInstance(); // get instance
                					nf.setMaximumFractionDigits(2); // set decimal places
                					String totalprice = nf.format(Dprice);
                					values.put(ShoppingList.TOTALPRICE, totalprice);
                				
                				}else{
                					values.put(ShoppingList.TOTALPRICE,price);
                				}
                			} catch (NumberFormatException e) {
                				// do nothing
                			}
                		}
                		if (units != null) {
                			values.put(ShoppingList.UNITS, units);
                		}
                		if (quantity != null) {
                			values.put(ShoppingList.QUANTITY, quantity);
                		}
                		if (priority != null) {
                			values.put(ShoppingList.PRIORITY, priority);
                		}
                		
                		/**
                         * Camera additions
                         * Added by: Menaka Kiriwattuduwa */
                		if (mPhotoFilePath != null) {
                			values.put(ShoppingList.IMAGE_FILE_PATH, mPhotoFilePath);
                			mPhotoFilePath = null;
                		}
                       
                	getContentResolver().update(mUri, values, null, null);
                	}
                });
                builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    	mPhotoFilePath = null;
                    }
                });
                
                /**
                 * Camera additions
                 * Added by: Menaka Kiriwattuduwa
                 * Functionality: Initializes button and thumbnail (ImageView) to be used with
                 * camera. Once the camera button is clicked, the camera application is opened
                 * for the use to take a photograph.
                 */
                // Set up thumbnail
                mDialogThumbnail = (ImageView)mDialogView.findViewById(R.id.thumbnail);
        		createThumbnail();
        		
        		// Set up camera button behaviour
                mDialogButtonIMAGE = (ImageButton)mDialogView.findViewById(R.id.attachimage);
        		if (mDialogButtonIMAGE != null){
        			 mDialogButtonIMAGE.setOnClickListener(new OnClickListener() {
        			 @Override  
        	         public void onClick(View v) {  
        	            // Creates an instance of PopupMenu  
        	            PopupMenu camera_menu = new PopupMenu(Trolly.this, mDialogButtonIMAGE);   
        	            camera_menu.getMenuInflater().inflate(R.menu.camera_menu, camera_menu.getMenu());  

        	            // Registers popup with OnMenuItemClickListener  
        	            camera_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
        	            	 // Set up behaviour of popup menu items
        		             public boolean onMenuItemClick(MenuItem item) {  
        		            	 switch (item.getItemId()) {
        		            	 case R.id.take_new_pic :
        		            		// Invoke an intent to capture a photo
        		     		    	dispatchTakePictureIntent();
        		     		    	break;
        		            	 case R.id.existing_pic :
        		            		// Invoke an intent to load existing picture from gallery
        		            		dispatchUseExistingPicIntent();
        		            		break;	 
        		            	 case R.id.delete_pic :
        		            		mPhotoFilePath = null;
        		            		createThumbnail();
         		            		break;
        		            	 }	                
        		              return true;  	            	 
        		             }  
        	            });  
        	            camera_menu.show(); 
        	           }  
        		  });
        		}
        		/* End of camera additions */
                
                return builder.create();
		case DIALOG_DELETE:
            mDialogView = factory.inflate(R.layout.dialog_confirm, null);
            mDialogText = (TextView)mDialogView.findViewById(R.id.dialog_confirm_prompt);
            // Aded by: Hantao Zhao
            if (!listMode) {
	            return new AlertDialog.Builder(this)
	                .setTitle(R.string.delete_item)
	                .setView(mDialogView)
	                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
	                	public void onClick(DialogInterface dialog, int whichButton) {
	                    	/* User clicked OK so do some stuff */
	                		getContentResolver().delete(mUri, null, null);
	                	}
	                })
	                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                        /* User clicked cancel so do some stuff */
	                    }
                }).create();
			}
            /*Added By: Hantao Zhao
    		 *Handle the list interface delete action
    		 * */
			else {

				return new AlertDialog.Builder(this)
						.setTitle(R.string.delete_item)
						.setView(mDialogView)
						.setPositiveButton(R.string.dialog_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/* User clicked OK so do some stuff */
										// delete all item from the currentList
										Cursor c = managedQuery(
												getIntent().getData(),
												PROJECTION,
												adding ? null
														: ShoppingList.STATUS
																+ "<>"
																+ ShoppingList.OFF_LIST
																+ " AND listname='"
																+ currentList
																+ "'", null,
												ShoppingList.DEFAULT_SORT_ORDER);
										c.moveToFirst();
										ContentValues values = new ContentValues();
										values.put(ShoppingList.STATUS,
												ShoppingList.OFF_LIST);
										Uri uri;
										long id;
										// loop through all items in the list
										while (!c.isAfterLast()) {
											id = c.getLong(c
													.getColumnIndexOrThrow(ShoppingList._ID));
											uri = ContentUris.withAppendedId(
													getIntent().getData(), id);
											// Update the status of this item
											// (in trolley) to "off list"
											getContentResolver().update(uri,
													values, null, null);
											// Cleanup the list by deleting
											// double up items that have been
											// checked out
											getContentResolver()
													.delete(getIntent()
															.getData(),
															ShoppingList.ITEM
																	+ "='"
																	+ c.getString(c
																			.getColumnIndex(ShoppingList.ITEM))
																	+ "' AND "
																	+ ShoppingList._ID
																	+ "<>"
																	+ id
																	+ " AND listname='"
																	+ currentList
																	+ "'", null);
											c.moveToNext();
										}
										getContentResolver().delete(
												getIntent().getData(),
												"listname='" + currentList
														+ "'", null);
										mTextBox.setHint("Add an item");
										updateListView();
									}
								})
						.setNegativeButton(R.string.dialog_cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										/* User clicked cancel so do some stuff */
									}
								}).create();
			}
		case DIALOG_CLEAR:
            mDialogView = factory.inflate(R.layout.dialog_confirm, null);
            mDialogText = (TextView)mDialogView.findViewById(R.id.dialog_confirm_prompt);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.clear_list)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                		ContentValues values = new ContentValues();
                    	//Set all items status to "off list"
                    	values.put(ShoppingList.STATUS, ShoppingList.OFF_LIST);
                    	getContentResolver().update(getIntent().getData(), values, null, null);
                	}
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		case DIALOG_RESET:
            mDialogView = factory.inflate(R.layout.dialog_confirm, null);
            mDialogText = (TextView)mDialogView.findViewById(R.id.dialog_confirm_prompt);
            return new AlertDialog.Builder(this)
                .setTitle(R.string.reset_list)
                .setView(mDialogView)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog, int whichButton) {
                    	//Permanently delete all items from the list
                    	getContentResolver().delete(getIntent().getData(), null, null);
                	}
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		}
		return null;
	}
    
	/**
	 * Change all items marked as "in trolley" to "off list"
	 */
    private void checkout() {
    	Cursor c = managedQuery(getIntent().getData(), PROJECTION, null, null,
                ShoppingList.DEFAULT_SORT_ORDER);
    	c.moveToFirst();
    	ContentValues values = new ContentValues();
    	values.put(ShoppingList.STATUS, ShoppingList.OFF_LIST);
    	Uri uri;
    	int status;
    	long id;
    	//loop through all items in the list
    	while (!c.isAfterLast()) {
    		status = c.getInt(c.getColumnIndex(ShoppingList.STATUS));
    		//if the item is not in the trolley jump to the next one
    		if (status == ShoppingList.IN_TROLLEY) {
	    		id = c.getLong(c.getColumnIndexOrThrow(ShoppingList._ID));
	    		uri = ContentUris.withAppendedId(getIntent().getData(), id);
	    		//Update the status of this item (in trolley) to "off list"
	    		getContentResolver().update(uri, values, null, null);
	    		//Cleanup the list by deleting double up items that have been checked out
	    		getContentResolver().delete(getIntent().getData(),
	    										ShoppingList.ITEM + "='" 
	    										+ c.getString(c.getColumnIndex(ShoppingList.ITEM)) 
	    										+ "' AND " + ShoppingList._ID + "<>" + id
	    										+ " AND " + ShoppingList.STATUS + "=" + ShoppingList.OFF_LIST, 
	    										null);
    		}
	    	c.moveToNext();
    	}
    }
    
    /**
     * Add items received as extras in the intent to the list
     */
    private void addExtraItems() {
    	ArrayList<String> list = getIntent().getStringArrayListExtra("org.openintents.intents.ShoppingListIntents.EXTRA_STRING_ARRAYLIST_SHOPPING");
    	Cursor c;
    	long id;
    	Uri uri;
    	for (String item : list) {
    		c = getContentResolver().query(getIntent().getData(), 
    										PROJECTION, 
    										ShoppingList.ITEM + "='" + item + "'", 
    										//ShoppingList.UNITS + "='" + item + "'", 
    										null, 
    										null);

    		//If there is no match then just add the item to the list with "on list" status
    		c.moveToFirst();
    		if (c.isBeforeFirst()) {
    			ContentValues values = new ContentValues();
    			values.put(ShoppingList.ITEM, item);
    			values.put(ShoppingList.STATUS, ShoppingList.ON_LIST);
    			getContentResolver().insert(getIntent().getData(), values);
    		} else {
        	//If there is a list item that matches this item...
    			//get status of existing item
    			int status = c.getInt(c.getColumnIndex(ShoppingList.STATUS));
    			if (status == ShoppingList.OFF_LIST) {
        			//move an existing "off list" item to "on list"
        	    	ContentValues values = new ContentValues();
        			values.put(ShoppingList.STATUS, ShoppingList.ON_LIST);
        			id = c.getLong(c.getColumnIndex(ShoppingList._ID));
    				uri = ContentUris.withAppendedId(getIntent().getData(), id);
    				getContentResolver().update(uri, values, null, null);    				
    			} else { 
    				/**If an existing item already has a status of "on list" 
    				 * then create a new (duplicate) item with "on list" status.
    				 * This allows for the case where an item is already on the list
    				 * but is added again from another source.
    				 */
    				ContentValues values = new ContentValues();
        			values.put(ShoppingList.ITEM, item);
        			values.put(ShoppingList.LISTNAME, currentList);
        			values.put(ShoppingList.STATUS, ShoppingList.ON_LIST);
        			getContentResolver().insert(getIntent().getData(), values);
    			}
    		}    		
    	}
    }
    /*
     * Added By: Hantao Zhao
	 * Funcion to update the list views in the list mode interface
	 */
	public void updateListView() {
		this.mCursor = managedQuery(getIntent().getData(), LISTPROJECTION,
				" 0==0) GROUP BY ( " + ShoppingList.LISTNAME, null,
				ShoppingList.LISTNAME + " ASC");

		// set the list adapter
		this.mAdapterForList = new TrollyAdapterForList(this,
				R.layout.shoppinglist_item, mCursor,
				new String[] { ShoppingList.LISTNAME }, new int[] { R.id.item }, LISTPROJECTION);
		setListAdapter(mAdapterForList);
	}

	
	
	/**
	* Added by: Menaka Kiriwattuduwa
	* Class/Funct name: onActivityResult
	* Inputs: requestCode:Integer, resultCode:Integer, data:Intent
	* Outputs: None
	* Functionality: Handle results of various activities.
	* 	In this case the function handles the results of taking a new picture
	*  or adding an existing picture from the gallery.
	*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   // Results of taking new picture
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	   	// Add to main gallery so image is available to other apps
	   	galleryAddPic();
	   	
	   	// Create thumbnail for image preview
	   	createThumbnail();  
	   }
	   
		// Results of adding an existing picture from gallery
	   if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	       Uri selectedImage = data.getData();
	       String[] filePathColumn = { MediaStore.Images.Media.DATA };
	
	       Cursor cursor = getContentResolver().query(selectedImage,
	               filePathColumn, null, null, null);
	       cursor.moveToFirst();
	
	       int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	       String picturePath = cursor.getString(columnIndex);
	       cursor.close();
	        
	       mPhotoFilePath = picturePath;
	       // Create thumbnail for image preview
	       createThumbnail();         
	   }
	}
	
	/**
	 * Added by: Menaka Kiriwattuduwa
	 * Class/Funct name: createThumbnail
	 * Inputs: None
	 * Outputs: None
	 * Functionality: Create thumbnail of image and sets visibility.
	 */
	private void createThumbnail() {
		if (mPhotoFilePath != null){
			Bitmap myBitmap = BitmapFactory.decodeFile(mPhotoFilePath);
			mDialogThumbnail.setImageBitmap(myBitmap);
		} else {
			mDialogThumbnail.setImageResource(R.drawable.blank_thumbnail);
		}
	}
	
	/**
     * Added by: Menaka Kiriwattuduwa
     * Class/Funct name: dispatchTakePictureIntent
     * Inputs: None
     * Outputs: None
     * Functionality: Invokes an intent to capture a photo.
     * 	This function is called when the "Take new picture" 
     *  option from the camera popup menu is selected. It creates a file path for the
     * 	new image to be stored. If this location is created correctly, the camera window is opened, 
     * 	allowing the user to take a photo.
     */
	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	    	File photoFile = null;
	        try {
	        	// create location to save image file
	        	photoFile = createImageFile();
	        } catch (IOException ex) {
	        	// reset file variable
	        	photoFile = null;
	        	Toast.makeText(getApplicationContext(),"Unable to create image.", Toast.LENGTH_SHORT).show();
	        	return;
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	        	mPhotoFilePath = photoFile.getAbsolutePath();
	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	                    Uri.fromFile(photoFile));
	            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
	        }
	    }
	}
	
	/**
     * Added by: Menaka Kiriwattuduwa
     * Class/Funct name: dispatchUseExistingPicIntent
     * Inputs: None
     * Outputs: None
     * Functionality: Invokes an intent to open Android gallery to add an 
     *  existing picture to an item. This function is called when the "Use existing picture" 
     *  option from the camera popup menu is selected. Users are able to access the Android gallery
     *  and choose from the existing images.
     */
	private void dispatchUseExistingPicIntent() {
		Intent i = new Intent(
				Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);			 
		startActivityForResult(i, REQUEST_LOAD_IMAGE);
	}
	
	/**
     * Added by: Menaka Kiriwattuduwa
     * Class/Funct name: createImageFile
     * Inputs: None
     * Outputs: image:File
     * Functionality: Creates unique file name and path for a photo to be saved in.
     * 	If the function is unable to create the file name, an exception is thrown.
     */
	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "Trolly_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );
	    return image;
	}
	

	/**
     * Added by: Menaka Kiriwattuduwa
     * Class/Funct name: galleryAddPic
     * Inputs: None
     * Outputs: None
     * Functionality: Adds photo to Media Provider database where it can be accessed
     * 	in the Android Gallery and other apps.
     */
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File("file:" + mPhotoFilePath);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}
	
	
}
