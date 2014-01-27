/**
 * 
 */
package com.teraim.nils;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.teraim.nils.CommonVars.PersistenceHelper;
import com.teraim.nils.DataTypes.Provyta;
import com.teraim.nils.DataTypes.Ruta;

/**
 * @author Terje
 * Activity for selecting an sub-area (del-yta).
 */
public class SelectYta extends MenuActivity {


	DataTypes rd=null;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.selectyta);
		rd = DataTypes.getSingleton();

		FrameLayout main = (FrameLayout) findViewById(R.id.ytselect);

		//Calculate size of display
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		double widthOfOneGraphicalRepresentationOfAProvyta=75;
		double heightOfOneGraphicalRepresentationOfAProvyta=75;

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
		Ruta ruta = CommonVars.cv().getCurrentRuta();
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
		//TODO: Change to real..
		final int[] ids = {R.drawable.ytcirklar_init,R.drawable.ytcirklar_ready, 
				R.drawable.ytcirklar_problem, R.drawable.ytcirklar_aktiv};
		int r=0;

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


			//b.setBackgroundDrawable(getResources().getDrawable(R.drawable.roundshape));
			b.setBackgroundDrawable(getResources().getDrawable(ids[r++%ids.length]));
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
	}

	CommonVars cv = CommonVars.cv();
	protected void provytaDialog(CharSequence ytID) {
		Log.d("NILS","clicked button with id "+ytID);
		if (ytID!=null)
			cv.ph.put(PersistenceHelper.CURRENT_PROVYTA_ID_KEY,ytID.toString());
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





}
