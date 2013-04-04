package com.teraim.nils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * 	
 * @author Terje
 * This function will guide the user in taking pictures of the current area.
 * 
 */
public class TakePicture extends Activity implements GeoUpdaterCb {

	private Button mittpunktB;
	ProvYtaGeoUpdater pyg;
	final int TAKE_PICTURE = 133;
	final String oldPicFolder = CommonVars.cv().getCurrentPictureBasePath()+"/gamla/";
	GridView gridViewNew;
	int selectedPic;
	//Broadcastreceiver for messages coming from other device.
	private BroadcastReceiver receiver;
	
	
	
	
	
	//Standard onCreate
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.takepicture);

		gridViewNew = (GridView) findViewById(R.id.gridview_new);
		ProvytaView provytaV = (ProvytaView) findViewById(R.id.provytaF);
		mittpunktB = (Button) findViewById(R.id.mittpunktB);
		final Context ctx = this;
		
		receiver = new BroadcastReceiver() {

	        @Override
	        public void onReceive(Context context, Intent intent) {
	            Toast.makeText(ctx, intent.getExtras().getString("MSG"), Toast.LENGTH_SHORT).show();

	        }
	    };

		pyg = new ProvYtaGeoUpdater(this,provytaV,this);
		gridViewNew.setAdapter(new NewImagesAdapter(this));
		gridViewNew.setOnItemClickListener(new OnItemClickListener()
		{


			public void onItemClick(AdapterView<?> parent,
					View v, int position, long id)
			{
				Toast.makeText(getBaseContext(),
						"pic" + (position + 1) + " selected",
						Toast.LENGTH_SHORT).show();
				selectedPic = position;
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+
						CommonVars.NILS_BASE_DIR;
				File file = new File(tempFolder, "temp.png");
				CommonVars.createFoldersIfMissing(file);
				Toast.makeText(getBaseContext(),
						"Folder: "+tempFolder,
						Toast.LENGTH_SHORT).show();
				Uri outputFileUri = Uri.fromFile(file);

				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

				startActivityForResult(intent, TAKE_PICTURE);


			}
		});


		GridView gridViewOld = (GridView) findViewById(R.id.gridview_old);
		TextView oldPicHeader = (TextView) findViewById(R.id.gamlabilder);

		File directory = new File(oldPicFolder);

		//If no old pictures, do not show the header!
		//TODO: Check this again in production code.
		/*
		 * 
		 *	File[] contents = directory.listFiles();
			if (contents == null) {
			gridViewOld.setVisibility(View.GONE);
			
			oldPicHeader.setVisibility(View.GONE);
		}
		*/

		gridViewOld.setAdapter(new OldImagesAdapter(this));
		gridViewOld.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent,
					View v, int position, long id)
			{
				Toast.makeText(getBaseContext(),
						CommonVars.compassToPicName(position) + " picture selected",
						Toast.LENGTH_SHORT).show();

				Intent myIntent = new Intent(getBaseContext(),PictureZoom.class);

				String picName = CommonVars.cv().getCurrentPictureBasePath()+"/gamla/"+
						CommonVars.compassToPicName(position)+".png";
				myIntent.putExtra("pos", position);
				myIntent.putExtra("picpath", picName);
				startActivity(myIntent);

			}
		});
		


	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){

		if (requestCode == TAKE_PICTURE){
			if (resultCode == Activity.RESULT_OK) 
			{
				Toast.makeText(this, "RESULT OK", Toast.LENGTH_LONG).show();
				final BitmapFactory.Options options = new BitmapFactory.Options();
			    // Calculate inSampleSize
			    options.inSampleSize = 6;
				//Save file in temporary storage.
				Bitmap bip = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+CommonVars.NILS_BASE_DIR+"/temp.png",options);		
				int w = bip.getWidth();
				int h = bip.getHeight();
				bip = Bitmap.createScaledBitmap(bip, w/6, h/6, false);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
				//			bip.compress(CompressFormat.JPEG, 0, outputStream); 
				byte[] bytes = outputStream.toByteArray(); 
				Log.d("NILS","The picture has a width of "+w+" and a height of "+h);
				//oldPic.setImageURI(outputFileUri);
				//newPictureImageView.setImageBitmap(bip);
				OutputStream fOut = null;
				String fileName = CommonVars.compassToPicName(selectedPic)+".png";
				File file = new File(CommonVars.cv().getCurrentPictureBasePath()+"/nya/", 
						fileName);
				CommonVars.createFoldersIfMissing(file);
				Log.d("NILS", "trying to save pic as: "+CommonVars.cv().getCurrentPictureBasePath()+"/nya/"+fileName);
				try {
					fOut = new FileOutputStream(file);
					bip.compress(Bitmap.CompressFormat.PNG, 100, fOut);
					fOut.flush();
					fOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				gridViewNew.invalidateViews();

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "RESULT CANCELLED", Toast.LENGTH_LONG).show();
			}

		}

	}






	final static int PIC_SIZE_X = 195;
	final static int PIC_SIZE_Y = 195;
	private static final int REQUEST_ENABLE_BT = 0;


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
				//picId = getResources().getIdentifier(empty_pic_names[i], "drawable", context.getPackageName());
				
				pic[i] = CommonVars.decodeSampledBitmapFromResource(getResources(), 
						getResources().getIdentifier(empty_pic_names[i], "drawable", context.getPackageName()), 100,100);
				//BitmapFactory.decodeResource(context.getResources(),
				//		picId);
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
			String picPath = CommonVars.cv().getCurrentPictureBasePath();

			Bitmap bm = BitmapFactory.decodeFile(picPath+"/nya/"+
					CommonVars.compassToPicName(position)+".png");

			if (bm==null)
				imageView.setImageBitmap(pic[position]);
			else
				imageView.setImageBitmap(bm);

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
			String picPath = CommonVars.cv().getCurrentPictureBasePath();

			Bitmap bm = BitmapFactory.decodeFile(picPath+"/gamla/"+
					CommonVars.compassToPicName(position)+".png");
			if (bm==null)
				try {

			        imageView.setImageBitmap(
			        	    CommonVars.decodeSampledBitmapFromResource(getResources(), 
			        	    		R.drawable.class.getField(CommonVars.compassToPicName(position)+"_demo").getInt(null), 200,200));

//					bm = BitmapFactory.decodeResource(getResources(),
//							R.drawable.class.getField(CommonVars.compassToPicName(position)+"_demo").getInt(null));
				} catch (Exception e) {
					// Will never happen..static naming..
				}
			else
				imageView.setImageBitmap(bm);
			//imageView.setImageBitmap(bm[position]);
			return imageView;
		}

	}

	//callback for Button setMittpunkt.
	public void setMittpunkt(View v) {
		//get the ui element to display text.
		TextView mt = (TextView)findViewById(R.id.mittText);
		Location pos = pyg.getCurrentPosition();
		if(pos!=null)
			mt.setText("Mittpunkt satt till: "+pos.getLatitude()+" "+pos.getLongitude());
	}

	//callback for Button setRiktpunkter
//	int tst = 0;
	RemoteDevice myRemoteDevice;
	
	public void startCollect(View v) {
		//Check that all "markslag" has been set by the other device.
		//This is a sync point. 
		/*new AlertDialog.Builder(this)
	    .setTitle("Inte implementerat.")
	    .setMessage("Den dynamiska insamlingsdelen är ännu inte klar.")
	    .setPositiveButton("Okej, jag förstår.", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	           
	        }
	     })
	     .show();
	     */
		Intent intent = new Intent(this, FlowEngineActivity.class);
		Bundle b = new Bundle();
		b.putString("workflow_id", "1"); //Your id
		intent.putExtras(b); //Put your id to your next Intent
		startActivity(intent);
		
	}
	public void setRiktpunkter(View v) {
		Intent intent = new Intent(this,RiktpunktActivity.class);
		startActivity(intent);
/*		Location l = new Location("");
		switch (tst) {
		case 0:
			l.setLatitude(59.304384);
			l.setLongitude(17.987441);
			break;
		case 1:
			l.setLatitude(59.304783);
			l.setLongitude(17.976277);
			break;
		case 2:
			l.setLatitude(59.302773);
			l.setLongitude(17.986969);
			break;
		case 3:
			l.setLatitude(59.302576);
			l.setLongitude(17.982935);
			break;
		}
		pyg.onLocationChanged(l);
		tst++;
*/
	}

	@Override
	protected void onPause() {
		pyg.onPause();
		unregisterReceiver(receiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		pyg.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.teraim.nils.bluetooth");
       	registerReceiver(receiver, filter);
        Log.d("NILS","READY TO RECEIVE");
		super.onResume();
	}



	public void onLocationUpdate(double dist, double rikt2,int wx, int wy) {
		mittpunktB.setEnabled(dist<=ProvYtaGeoUpdater.InnerRadiusInMeters);
		
	}







	

}
