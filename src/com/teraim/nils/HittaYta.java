package com.teraim.nils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teraim.nils.dynamic.types.Delyta;
import com.teraim.nils.utils.PersistenceHelper;


public class HittaYta extends Activity implements GeoUpdaterCb {

	@Override
	protected void onPause() {
		pyg.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		pyg.onResume();
		super.onResume();
		Log.d("NILS","Hitta yta onResume");
		//redraw the tågtabell.		
		tagtabell.invalidateViews();
		
	}

	ProvYtaGeoUpdater pyg;

	//TODO: REPLACE if more than one RUTA...
	
	final String picPath = Constants.NILS_ROOT_DIR+"/delyta/"+
			"1"+"/bilder/gamla/";
	private TextView userPosTextV;
	
	private Button startCollectB;
	
	private Intent editDelytaIntent;

	private ListView tagtabell;
	
	private GlobalState gs;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hittayta);	
		
		editDelytaIntent = new Intent(this,EditDelYta.class);
		gs = GlobalState.getInstance(this);
		
		ProvytaView provytaV = (ProvytaView) findViewById(R.id.provyta);
		GridView gridViewOld = (GridView) findViewById(R.id.picgridview);
		tagtabell = (ListView) findViewById(R.id.tagtabell);
		TextView gamlaBilder = (TextView) findViewById(R.id.oldpichead);
		startCollectB = (Button) findViewById(R.id.startCollectB);
		userPosTextV = (TextView)findViewById(R.id.userPosText);
		gridViewOld.setAdapter(new ImageAdapter(this));
		gridViewOld.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent,
					View v, int position, long id)
			{
				Toast.makeText(getBaseContext(),
						Constants.compassToPicName(position) + " picture selected",
						Toast.LENGTH_SHORT).show();

				Intent myIntent = new Intent(getBaseContext(),PictureZoom.class);
				String picName = gs.getCurrentPictureBasePath()+"/gamla/"+
						Constants.compassToPicName(position)+".png";
				myIntent.putExtra("picpath", picName);
				myIntent.putExtra("pos", position);
				startActivity(myIntent);
				
			}
		});
		
/*		final Context ctx = this;
		receiver = new BroadcastReceiver() {

	        @Override
	        public void onReceive(Context context, Intent intent) {
	            Toast.makeText(ctx, intent.getExtras().getString("MSG"), Toast.LENGTH_SHORT).show();

	        }
	    };
*/

		pyg = new ProvYtaGeoUpdater(this,provytaV,this);

		File directory = new File(picPath);
		File[] contents = directory.listFiles();
		
		//If no old pictures, do not show the header!
		if (contents == null) {
			gamlaBilder.setVisibility(View.GONE);
		}
		
		//Fetch delytor for the current ruta and provyta.
		
		ArrayList<Delyta> dy = gs.getDelytor(gs.getCurrentRuta().getId(), 
				gs.getCurrentProvyta().getId());
		provytaV.setDelytor(dy);
		
		startCollectB.setEnabled(isComplete(dy));
		
		
		
		


		registerForContextMenu(tagtabell);
	    
		final TableAdapter ta = new TableAdapter(this,dy);
		final Intent intent = new Intent(this,MarkslagsActivity.class);
		
		tagtabell.setAdapter(ta);
		//Add contect menu..
		
		tagtabell.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
               gs.getPersistence().put(PersistenceHelper.CURRENT_DELYTA_ID_KEY,ta.getItem(arg2).getId());
                startActivity(intent);
			}});
		
		provytaV.invalidate();
		tagtabell.invalidate();

	}

	private boolean isComplete(ArrayList<Delyta> dy) {
		for (Delyta d:dy)
			if (d.getVariable("markslag")==null)
				return false;
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.tagpopmenu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.tag_pop_edit:
	        	Log.d("NILS","User clicked edit "+info.id);
	        	//tell edit activity that this is edit of row x.
	    		editDelytaIntent.putExtra("com.teraim.nils.addRow",(int)info.id);
	    		startActivity(editDelytaIntent);
	    		
	            return true;
	        case R.id.tag_pop_delete:
	        	Log.d("NILS","User clicked delete "+info.id);
	        	((TableAdapter)tagtabell.getAdapter()).delete((int)info.id);
	        	tagtabell.invalidate();
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	private class TableAdapter extends ArrayAdapter<Delyta> {
		  private final Context context;
		  private final List<Delyta> values;

		  public TableAdapter(Context context, List<Delyta> values) {
		    super(context, R.layout.tag_row, values);
		    this.context = context;
		    this.values = values;
		    

		  }
		
		  public void delete(int id) {
			if(values.remove(id)!=null)
				Log.d("NILS","Succesfully removed item from listview");
			this.notifyDataSetChanged();
		}

		@Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = inflater.inflate(R.layout.tag_row, parent, false);
		    TextView title = (TextView) rowView.findViewById(R.id.tag_title);
		    TextView markslag = (TextView) rowView.findViewById(R.id.tag_markslag);
		    TextView tag = (TextView) rowView.findViewById(R.id.tag_content);
		    

		    title.setText(values.get(position).getId());
		    
		    StoredVariable m = values.get(position).getVariable("markslag");
		    
		    markslag.setText(m==null?"?":m.getValue());
			int[][] ps = values.get(position).getPoints();
			String rowS = "";
			if (ps!=null && ps.length!=0) {
				int r,a;
				for (int i=0;i<ps.length;i++) {
					r = ps[i][0];
					a = ps[i][1];
					rowS += i+": ("+r+","+a+") ";	    
				}
				
			}
		    tag.setText(rowS);
		    
		    return rowView;
		  }
		  
	}
	
	private void createTagTabell(ListView tagtabell, ArrayList<Delyta> dy) {


		
		
		//Add title
		/*
		tagtabell.addView(addTitle("TÅGTABELL"));
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
			*/

		
		
		//tagtabell.addView(rowHeader);
		
		//add delytetåg
		
		
		//for(final Delyta del:dy) {
		//	if (del!=null) {
				//TableRow row = new TableRow(this);
				//tagtabell.addView(row);
				//Tabs to setup markslag.
		//		Button b = new Button(this);
				/*b.setLongClickable(true);				
				 b.setOnLongClickListener(new OnLongClickListener() {
		            public boolean onLongClick(View v) {
			                Toast.makeText(getBaseContext(), "Long CLick", Toast.LENGTH_SHORT).show();
			                PopupMenu pop = new PopupMenu(ctx,v);
			                Menu m = pop.getMenu();
			                m.xxxx
			                m.add("EDIT");
			                m.add("DELETE");
							return true;
			        }
				 });
				 */
		/*		  b.setOnClickListener(new OnClickListener() {
			            public void onClick(View v) {
			            		
				                Toast.makeText(getBaseContext(), "Short CLick", Toast.LENGTH_SHORT).show();
				           
				                CommonVars.cv().setDelyta(del);
				                startActivity(intent);
				        }
					 });		 
				
				b.setText("Delyta "+del.getId());
				row.addView(b);
				//ps is a int[][] containing riktning/avstånd for up to 8 points.
				int[][] ps = del.getPoints();
				if (ps!=null && ps.length!=0) {
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
				}
				
			}
		}
		*/

		
	}
	
	/*
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
	
	
	*/	
	
	public void addRow(View v) {
		//Tell other side that this is a new row.
		editDelytaIntent.putExtra("com.teraim.nils.addRow", "-1");
		startActivity(editDelytaIntent);
		
	}

	
	public void startCollect(View v) {
		//Intent intent = new Intent(this,EditDelYta.class);
		//startActivity(intent);
		
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
			int h;			
			ImageView imageView;
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int height = size.y;
			final int Margins = 100;
			if (convertView == null) {
				imageView = new ImageView(context);
				
				imageView.setLayoutParams(new
						GridView.LayoutParams(LayoutParams.MATCH_PARENT, (height-Margins)/getCount()));
				imageView.setScaleType(
						ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(10, 5, 5, 5);
			} else {
				imageView = (ImageView) convertView;
			}
			//TODO: Get rid of 1 below!!
			String picPath = gs.getCurrentPictureBasePath();

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
		    
		    // Calculate inSampleSize
		    options.inSampleSize = 4;
		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    
			Bitmap bm = BitmapFactory.decodeFile(picPath+"/gamla/"+
					Constants.compassToPicName(position)+".png",options);
			
			if (bm==null)
				try {
					bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.class.getField(Constants.compassToPicName(position)+"_demo").getInt(null),options);
				} catch (Exception e) {
					// Will never happen..static naming..
				}
			imageView.setImageBitmap(bm);
			//imageView.setImageBitmap(bm[position]);
			return imageView;
		}

	}

	public void onLocationUpdate(double dist, double vinkel,int wx,int wy) {
		char c = '\u00B0';
		userPosTextV.setText("Avst: "+(int)dist+
				" m. Vinkel: "+(int)(vinkel*57.2957795)+c+
				" X:"+wx+
				" Y:"+wy);
	}
	
	

}
