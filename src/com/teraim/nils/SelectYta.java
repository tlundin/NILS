/**
 * 
 */
package com.teraim.nils;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.teraim.nils.Rutdata.Ruta;
import com.teraim.nils.Rutdata.Yta;

/**
 * @author Terje
 * Activity for selecting an sub-area (del-yta).
 */
public class SelectYta extends Activity {
	private static final double ZoomFactor = 250;
	Rutdata rd=null;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.selectyta);
		rd = Rutdata.getSingleton(this);

		FrameLayout main = (FrameLayout) findViewById(R.id.ytselect);

		//Calculate size of display
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		double width = size.x;
		double height = size.y;



		Log.d("NILS","Device screen in pixel: "+width+", "+height);

		//Size of the RUTA is 5 kilometer.
		//Ruta is 2125 meters from provyta 1.

		//First get the Ruta data.
		//Bundle bu = getIntent().getExtras();
		//String rutaId = bu.getString("ruta");
		String rutaId = CommonVars.cv().getRutaId();
		if (rutaId !=null) {
			Ruta ruta = rd.findRuta(rutaId);

			//Yta yta1 = ruta.findYta("1");
			//double rutaX0 = yta1.x-DistanceToOrigo;
			//double rutaY0 = yta1.y-DistanceToOrigo;

			double[] minmax = ruta.getMinMaxValues();
			double disty = (minmax[2]-minmax[0]);
			double distx = (minmax[3]-minmax[1]);

			//Add 250 meters to distance (zoom out a bit)

			distx+=ZoomFactor;
			disty+=ZoomFactor;
			Log.d("NILS","distx, disty"+distx+" ,"+disty);

			//Figure out scaling by looking at the shortest of the div between screen width/height and distance between ytor.
			double div;
			double divx = width/distx;
			double divy = height/disty;
			div = (divx>divy)?divy:divx;

			Log.d("NILS","div: "+div);
			double marginx = (ZoomFactor*div)/3;
			double marginy = (ZoomFactor*div);

			//double divx = width/distx;
			//double divy = height/disty;
			//Log.d("NILS","divxy "+divx+" "+divy);
			ArrayList<Yta> ytor = ruta.getYtor();
			for(final Yta yta:ytor) {

				//subtract min value from the coordinate to get normalized values
				//add half the zoom factor to center..
				double normx = (yta.sweLong-minmax[0]);
				double normy = (yta.sweLat-minmax[1]);
				//double normx = (yta.x-rutaX0);
				//double normy = (yta.y-rutaY0);
				Log.d("NILS","normxy "+normx+" "+normy);
				//multiply with screen size to get screen norm values
				//add a margin of 10%
				int cordx = (int)(normx*div);
				int cordy = (int)(normy*div);

				Log.d("NILS","cordxy "+cordx+" "+cordy+" id "+yta.id);
				Button b = new Button(this);
				b.setText(yta.id);
				//swap coordinates since origo for screen is topleft corner while ruta origo is bottomleft
				b.setX((float) (cordx+marginx));
				b.setY((float) (height-(cordy+marginy)));
				b.setBackgroundDrawable(getResources().getDrawable(R.drawable.roundshape));
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(50, 50);
				b.setLayoutParams(layoutParams);b.setLayoutParams(layoutParams);
				b.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						provytaDialog(((Button)v).getText());
					}
				});
				main.addView(b);  
			}
		}
	}



	protected void provytaDialog(CharSequence ytID) {
		Log.d("NILS","clicked button with id "+ytID);
		if (ytID!=null)
			CommonVars.cv().setProvytaId(ytID.toString());
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Provyta "+ytID);
		alert.setMessage("Ska ytan inventeras?");


		alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				final Intent takePictureIntent = new Intent(getBaseContext(),TakePicture.class);
				final Intent hittaYtaIntent = new Intent(getBaseContext(),HittaYta.class);


				if(CommonVars.cv().getDeviceColor().equals(CommonVars.blue())) {
					Log.d("NILS","dosa blå!"+CommonVars.blue());				
					startActivity(takePictureIntent);
				}
				else {
					startActivity(hittaYtaIntent);     

				}
				//finish();
				// Intent intent = getIntent();
				//setResult(Activity.RESULT_OK, intent);

			}
		});

		alert.setNegativeButton("Nej", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});

		alert.show();

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
