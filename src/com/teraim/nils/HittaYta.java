package com.teraim.nils;
import java.util.ArrayList;

import com.teraim.nils.Delningsdata.Delyta;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class HittaYta extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.hittayta);	
		GridView gridView = (GridView) findViewById(R.id.picgridview);
		Provyta provyta = (Provyta) findViewById(R.id.provyta);
		TableLayout tagtabell = (TableLayout) findViewById(R.id.tagtabell);
		gridView.setAdapter(new ImageAdapter(this));
		tagtabell.setStretchAllColumns(true);  
		tagtabell.setShrinkAllColumns(true); 
		TableRow rowTitle = new TableRow(this);
		rowTitle.setGravity(Gravity.CENTER_HORIZONTAL);  
		TextView title = new TextView(this);
		title.setText("Tåg-Tabell");  
	    title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);  
	    title.setGravity(Gravity.CENTER);  
	    title.setTypeface(Typeface.SERIF, Typeface.BOLD);  
	    TableRow.LayoutParams params = new TableRow.LayoutParams();  
	    Delningsdata dd = Delningsdata.getSingleton(this);
	    ArrayList<Delyta> dy = dd.getDelytor(CommonVars.getRutaId(), CommonVars.getProvytaId());
	    provyta.setDelytor(dy);
	    
	    Log.d("NILS","ruta: "+CommonVars.getRutaId()+" provyta: "+CommonVars.getProvytaId());
	    
	    provyta.invalidate();	    
	    params.span = 6;  
	    rowTitle.addView(title, params); 
	    tagtabell.addView(rowTitle);
	    
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
			String picPath = Environment.getExternalStorageDirectory()+
					CommonVars.NILS_BASE_DIR+"/delyta/"+
					CommonVars.getCurrentYtID()+"/bilder";
			
			Bitmap bm = BitmapFactory.decodeFile(picPath+"/gamla/"+
					CommonVars.compassToString(position)+".png");

			imageView.setImageBitmap(bm);
			//imageView.setImageBitmap(bm[position]);
			return imageView;
		}
	}
}
