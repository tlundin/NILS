package com.teraim.nils;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.teraim.nils.Delningsdata.Delyta;

public class HittaYta extends Activity {

	@Override
	protected void onPause() {
		pyg.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		pyg.onResume();
		super.onResume();
	}

	ProvYtaGeoUpdater pyg;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.hittayta);	
		ProvytaView provytaV = (ProvytaView) findViewById(R.id.provyta);
		GridView gridView = (GridView) findViewById(R.id.picgridview);
		TableLayout tagtabell = (TableLayout) findViewById(R.id.tagtabell);
		gridView.setAdapter(new ImageAdapter(this));
		Delningsdata dd = Delningsdata.getSingleton(this);

		pyg = new ProvYtaGeoUpdater(this,provytaV,null);


		ArrayList<Delyta> dy = dd.getDelytor(CommonVars.cv().getRutaId(), CommonVars.cv().getProvytaId());
		provytaV.setDelytor(dy);

		Log.d("NILS","ruta: "+CommonVars.cv().getRutaId()+" provyta: "+CommonVars.cv().getProvytaId());
		createTagTabell(tagtabell,dy);


		provytaV.invalidate();

	}

	private void createTagTabell(TableLayout tagtabell, ArrayList<Delyta> dy) {

		if (dy==null)
			return;

		tagtabell.setStretchAllColumns(true);  
		tagtabell.setShrinkAllColumns(true);
		//Add title
		tagtabell.addView(addTitle("T�GTABELL"));
		TableRow rowTitle = new TableRow(this);
		rowTitle.setGravity(Gravity.LEFT);  
		TextView title;
		TableRow.LayoutParams params;
		TableRow rowHeader = new TableRow(this);
		params= new TableRow.LayoutParams();  
		rowHeader.addView(addHeader(""), params);
		for(int i=1;i<=8;i++) {
			title = addHeader("R"+i);
			
			rowHeader.addView(title, params); 	
			title = addHeader("A"+i);
			params = new TableRow.LayoutParams();  
			rowHeader.addView(title, params); 	    

		}
		
		tagtabell.addView(rowHeader);
		
		//add delytet�g
		
		for(Delyta del:dy) {
			if (del!=null) {
				
				//ps is a int[][] containing riktning/avst�nd for up to 8 points.
				int[][] ps = del.getPoints();
				if (ps!=null && ps.length!=0) {
					TableRow row = new TableRow(this);
					Button b = new Button(this);
					b.setLongClickable(true);
					//Tabs to setup markslag.
					final Intent intent = new Intent(this,MarkslagsActivity.class);
					
					 b.setOnLongClickListener(new OnLongClickListener() {
			            public boolean onLongClick(View v) {
				                Toast.makeText(getBaseContext(), "Long CLick", Toast.LENGTH_SHORT).show();
				                
								return true;
				        }
					 });
					  b.setOnClickListener(new OnClickListener() {
				            public void onClick(View v) {
					                Toast.makeText(getBaseContext(), "Short CLick", Toast.LENGTH_SHORT).show();
					                
					                startActivity(intent);
					        }
						 });
					 
					
					b.setText("Delyta "+del.getId());
					row.addView(b);
					int r,a;
					for (int i=0;i<ps.length;i++) {
						r = ps[i][0];
						a = ps[i][1];
						Log.d("NILS","Rikt: AVST: "+r+" "+a);
						title = addNum(r);
						params= new TableRow.LayoutParams();  
						row.addView(title, params); 	
						title = addNum(a);
						params = new TableRow.LayoutParams();  
						row.addView(title, params); 	    

						
					}
					
					tagtabell.addView(row);
				}
			}
		}

		
	}
	
	
	private TextView addNum(int header) {
		TextView title = new TextView(this);
		title.setText(String.valueOf(header));  
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);  
		title.setGravity(Gravity.LEFT);  
		title.setTypeface(Typeface.SERIF, Typeface.ITALIC);
		return title;
	}

	private TextView addHeader(String header) {
		TextView title = new TextView(this);
		title.setText(header);  
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);  
		title.setGravity(Gravity.LEFT);  
		title.setTypeface(Typeface.SERIF, Typeface.BOLD);
		return title;
	}
	private TableRow addTitle(String titleS) {
		TableRow rowTitle = new TableRow(this);
		rowTitle.setGravity(Gravity.LEFT);  
		TextView title = new TextView(this);
		title.setText(titleS);  
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);  
		title.setGravity(Gravity.CENTER);  
		title.setTypeface(Typeface.SERIF, Typeface.BOLD);  
		TableRow.LayoutParams params = new TableRow.LayoutParams();  
		rowTitle.addView(title, params); 
		return rowTitle;
	}
	
	
	public void addRow(View v) {
		Intent intent = new Intent(this,EditDelYta.class);
		startActivity(intent);
		
	}
	
	public class ImageAdapter extends BaseAdapter
	{

		String[] imageNames = {

				"ost",
				"vast",
				"norr",
				"syd"
		};
		private Context context;
		public ImageAdapter(Context c)
		{
			context = c;
		}
		//---returns the number of images---
		public int getCount() {
			return imageNames.length;
		}
		//---returns the item---
		public Object getItem(int position) {
			return position;
		}
		//---returns the ID of an item---
		public long getItemId(int position) {
			return position;
		}
		//---returns an ImageView view---
		public View getView(int position, View convertView,
				ViewGroup parent)
		{
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(context);
				imageView.setLayoutParams(new
						GridView.LayoutParams(195, 195));
				imageView.setScaleType(
						ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(10, 5, 5, 5);
			} else {
				imageView = (ImageView) convertView;
			}
			//TODO:Replace with RutaId!
			String picPath = Environment.getExternalStorageDirectory()+
					CommonVars.NILS_BASE_DIR+"/delyta/"+
					"1"+"/bilder";

			Bitmap bm = BitmapFactory.decodeFile(picPath+"/gamla/"+
					CommonVars.compassToPicName(position)+".png");

			imageView.setImageBitmap(bm);
			//imageView.setImageBitmap(bm[position]);
			return imageView;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return MenuChoice(item);
	}

	private void CreateMenu(Menu menu)
	{
		MenuItem mnu3 = menu.add(0, 2, 2, "Anv�ndare: "+CommonVars.cv().getUserName());
		mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItem mnu4 = menu.add(0, 3, 3, "F�rg: "+CommonVars.cv().getDeviceColor());
		mnu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItem mnu5 = menu.add(0, 4, 4, "Item 5");
		mnu5.setIcon(android.R.drawable.ic_menu_preferences);
		mnu5.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
	}
	private boolean MenuChoice(MenuItem item)
	{
		switch (item.getItemId()) {
		case 0:
			//Toast.makeText(this, "You clicked on Item 1",
			//		Toast.LENGTH_LONG).show();
		case 1:
			//Toast.makeText(this, "You clicked on Item 2",
			//		Toast.LENGTH_LONG).show();
		case 2:
			Toast.makeText(this, "�ndra anv�ndare",
					Toast.LENGTH_LONG).show();
		case 3:
			Toast.makeText(this, "�ndra f�rg",
					Toast.LENGTH_LONG).show();
		case 4:
			Intent intent = new Intent(getBaseContext(),ConfigMenu.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

}
