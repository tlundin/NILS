package com.teraim.nils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/** 
 * 	
 * @author Terje
 * This function will guide the user in taking pictures of the current area.
 * 
 */
public class TakePicture extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		setContentView(R.layout.takepicture);


		GridView gridViewNew = (GridView) findViewById(R.id.gridview_new);
		gridViewNew.setAdapter(new NewImagesAdapter(this));
		gridViewNew.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent,
					View v, int position, long id)
			{
				Toast.makeText(getBaseContext(),
						"pic" + (position + 1) + " selected",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getBaseContext(),TakePictureActivity.class);
				intent.putExtra("selectedpic", position);
				startActivity(intent);
			}
		});

		GridView gridViewOld = (GridView) findViewById(R.id.gridview_old);
		gridViewOld.setAdapter(new OldImagesAdapter(this));
		gridViewOld.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent,
					View v, int position, long id)
			{
				Toast.makeText(getBaseContext(),
						"pic" + (position + 1) + " tsssselected",
						Toast.LENGTH_SHORT).show();

				Intent myIntent = new Intent(getBaseContext(),PictureZoom.class);
				String picPath = Environment.getExternalStorageDirectory()+
						CommonVars.NILS_BASE_DIR+"/delyta/"+
						"1"+"/bilder/gamla/"+
						CommonVars.compassToPicName(position)+".png";


				myIntent.putExtra("picpath", picPath);
				startActivity(myIntent);


				//startActivity(intent);
			}
		});

	}

	final static int PIC_SIZE_X = 195;
	final static int PIC_SIZE_Y = 195;
	

	private abstract class ImageAdapter extends BaseAdapter {


		//---returns the number of images---
		public abstract int getCount();

		//---returns the item---
		public Object getItem(int position) {
			return position;
		}
		//---returns the ID of an item---
		public long getItemId(int position) {
			return position;
		}
		//---returns an ImageView view---
		public abstract View getView(int position, View convertView,
				ViewGroup parent);

	}

	public class NewImagesAdapter extends ImageAdapter {


		public final String[] empty_pic_names = {"empty_west","empty_north","empty_south","empty_east"};
		private final Bitmap[] pic = new Bitmap[empty_pic_names.length];
		private Context context;

		public NewImagesAdapter(Context c)
		{
			context = c;
			int picId=-1;
			for (int i=0;i<empty_pic_names.length;i++) {
				picId = getResources().getIdentifier(empty_pic_names[i], "drawable", context.getPackageName());
				pic[i] = BitmapFactory.decodeResource(context.getResources(),
						picId);
			}
		}
		//---returns the number of images---
		public int getCount() {
			return empty_pic_names.length;
		}
		//---returns an ImageView view---
		public View getView(int position, View convertView,
				ViewGroup parent)
		{
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(context);
				imageView.setLayoutParams(new
						GridView.LayoutParams(PIC_SIZE_X, PIC_SIZE_Y));
				imageView.setScaleType(
						ImageView.ScaleType.FIT_CENTER);
				imageView.setPadding(10, 5, 5, 5);
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageBitmap(pic[position]);
			//imageView.setImageBitmap(bm[position]);
			return imageView;
		}
	}

	public class OldImagesAdapter extends ImageAdapter {


		final Bitmap[] pic = new Bitmap[4];
		private Context context;

		public OldImagesAdapter(Context c)
		{
			context = c;
		}
		//---returns the number of images---
		public int getCount() {
			return 4;
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
			//TODO: Get rid of 1 below!!
			String picPath = CommonVars.cv().getCurrentPictureBasePath();

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
		MenuItem mnu3 = menu.add(0, 2, 2, "Användare: "+CommonVars.cv().getUserName());
		mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItem mnu4 = menu.add(0, 3, 3, "Färg: "+CommonVars.cv().getDeviceColor());
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
			Toast.makeText(this, "Ändra användare",
					Toast.LENGTH_LONG).show();
		case 3:
			Toast.makeText(this, "Ändra färg",
					Toast.LENGTH_LONG).show();
		case 4:
			Intent intent = new Intent(getBaseContext(),ConfigMenu.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

}
