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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.teraim.nils.DataTypes.Provyta;
import com.teraim.nils.DataTypes.Ruta;

/**
 * @author Terje
 * Activity for selecting an sub-area (del-yta).
 */
public class SelectYta extends Activity {


	DataTypes rd=null;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.selectyta);
		rd = DataTypes.getSingleton(this);

		FrameLayout main = (FrameLayout) findViewById(R.id.ytselect);

		//Calculate size of display
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		double widthOfOneGraphicalRepresentationOfAProvyta=50;
		double heightOfOneGraphicalRepresentationOfAProvyta=50;

		double Menus = 125; //pixels.
		double margin = 50; //about twice the size of one graphical elem.
		double width = size.x-(margin+widthOfOneGraphicalRepresentationOfAProvyta);
		double height = size.y-(Menus+margin);

		//still need to subtract about 200 pixels from y because of system menus.
		//height = height - 200;
		//subtract at least width of circle button from X.

		//width = width -100;
		Log.d("NILS","Device screen in pixel: "+width+", "+height);
		//Size of the RUTA is 5 kilometer.
		//Ruta is 2125 meters from provyta 1.

		//First get the Ruta data.
		//Bundle bu = getIntent().getExtras();
		//String rutaId = bu.getString("ruta");
		Ruta ruta = CommonVars.cv().getRuta();
		Ruta.Sorted s = ruta.sort();

		//long=y 
		double disty = (s.getMax_N_sweref_99()-s.getMin_N_sweref_99());
		//lat=x
		double distx = (s.getMax_E_sweref_99()-s.getMin_E_sweref_99());

		//Set a 10% margin from the longest distance.
		double Margin_Percentage = 0;
		boolean landscape = false;
		double lengthRel;

		if (distx>disty) {
			landscape = true;
			lengthRel = disty/distx;
		}
		else {
			lengthRel = distx/disty;
		}
		distx+= distx*Margin_Percentage;
		disty+= disty*Margin_Percentage;

		//which of x,y has least space on the screen?
		Log.d("NILS","Lat (x) in meters: "+distx+" Long (Y) in meters:"+disty);

		//scale it to screen size

		double screenZoom = Math.min(height/disty,width/distx);

		Log.d("NILS","skalfaktor "+screenZoom);

		ArrayList<Provyta> ytor = ruta.getAllProvYtor();
		for(final Provyta yta:ytor) {

			//subtract min value from the coordinate to get normalized values
			double normx = (yta.E-s.getMin_E_sweref_99());
			double normy = (yta.N-s.getMin_N_sweref_99());

			//multiply with scale factor to get pixel size
			float cordx = (float)(normx*screenZoom);
			float cordy = (float)(normy*screenZoom);
			//Reverse position.
			cordy = (float)height - cordy;

			//add back half the margin that was subtracted to center...
			cordx+=margin/2+(width-distx*screenZoom)/2;
			cordy+=margin/2;//+(landscape?(height-disty*screenZoom)/2:0);
			cordy-=(height-disty*screenZoom)/2;
			Log.d("NILS","X Y SKÄRM "+cordx+" "+cordy);

			Button b = new Button(this);
			b.setText(yta.getId());

			b.setX(cordx);
			b.setY(cordy);


			b.setBackgroundDrawable(getResources().getDrawable(R.drawable.roundshape));
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int)widthOfOneGraphicalRepresentationOfAProvyta
					,(int) heightOfOneGraphicalRepresentationOfAProvyta);

			b.setLayoutParams(layoutParams);
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					provytaDialog(((Button)v).getText());
				}
			});
			main.addView(b);  
		}
	}




	@Override
	protected void onResume() {
		super.onResume();
		refreshStatusRow();
	}

	CommonVars cv = CommonVars.cv();
	protected void provytaDialog(CharSequence ytID) {
		Log.d("NILS","clicked button with id "+ytID);
		if (ytID!=null)
			cv.setProvyta(cv.getRuta().findProvYta(ytID.toString()));
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

	MenuItem mnu3=null,mnu4=null;
	private void CreateMenu(Menu menu)
	{

		mnu3 = menu.add(0, 2, 2, "");
		mnu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		mnu4 = menu.add(0, 3, 3,"");
		mnu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItem mnu5 = menu.add(0, 4, 4, "Item 5");
		mnu5.setIcon(android.R.drawable.ic_menu_preferences);
		//R.drawable.ic_menu_preferences
		mnu5.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItem mnu2 = menu.add(0, 1, 1, "Item 2");
		mnu2.setIcon(android.R.drawable.ic_menu_mylocation);
		//R.drawable.ic_menu_preferences
		mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		refreshStatusRow();
	}

	private void refreshStatusRow() {

		if (mnu3!=null)
			mnu3.setTitle("Användare: "+CommonVars.cv().getUserName());
		if (mnu4!=null)
			mnu4.setTitle("Färg: "+CommonVars.cv().getDeviceColor());
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
