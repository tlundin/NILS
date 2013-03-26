package com.teraim.nils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.teraim.nils.DataTypes.Provyta;

public class StartMenuActivity extends MenuActivity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	
	//Spinner f_spinner,r_spinner,flow_spinner;
	//DataTypes T;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.startmenu);
		 final Intent i_map =  new Intent(getBaseContext(),MapSelect.class);
		 final Intent i_pic =  new Intent(getBaseContext(),TakePicture.class);
		 final Intent i_find = new Intent(getBaseContext(),HittaYta.class);
 		 final Intent i_wf = new Intent(this, FlowEngineActivity.class);

		

		/*
		//f_spinner = (Spinner) findViewById(R.id.farg_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		//        R.array.deviceColors, R.layout.spinneritem);
		// Specify the layout to use when the list of choices appears
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		//f_spinner.setAdapter(adapter);
		//f_spinner.setSelection(adapter.getPosition(CommonVars.cv().getDeviceColor()));
		
		
		r_spinner = (Spinner) findViewById(R.id.rut_spinner);
		flow_spinner = (Spinner) findViewById(R.id.flow_spinner);
		*/
		// Create an ArrayAdapter using the string array and a default spinner layout
		//T = DataTypes.getSingleton();	  
		    //Get the IDs
		//String[]  values = T.getRutIds();
		//if (values == null) {
		//    	values = new String[1];
		//    	values[0]="Oops...no data found";
		//    }

		//adapter = new ArrayAdapter<CharSequence>(this,
		//        R.layout.spinneritem, values);
	
		// Specify the layout to use when the list of choices appears
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		//r_spinner.setAdapter(adapter);
		//r_spinner.setSelection(adapter.getPosition(CommonVars.cv().getRuta().getId()));
		
/*
		values = CommonVars.cv().getWorkflowNames();
		
		adapter = new ArrayAdapter<CharSequence>(this,
		        R.layout.spinneritem, values);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		flow_spinner.setAdapter(adapter);
		flow_spinner.setSelection(adapter.getPosition("main"));
	*/
		
		OnItemSelectedListener menuRefresh = new OnItemSelectedListener() {
			
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	refreshStatusRow();	
		    	StartMenuActivity me = StartMenuActivity.this;
		    	me.invalidateOptionsMenu();
		    	me.closeOptionsMenu();
		    	me.openOptionsMenu();
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		        
		    }

		};
	
		//flow_spinner.setOnItemSelectedListener(menuRefresh);
		//f_spinner.setOnItemSelectedListener(menuRefresh);
		//r_spinner.setOnItemSelectedListener(menuRefresh);

		
		
		//Grid
		GridView gridview = (GridView) findViewById(R.id.startmenu_grid);
	    gridview.setAdapter(new ImageAdapter(this));
	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            //Map = 3rd button
	        	switch (position)  {
	        	
	        	case 0:
	        		if (provytaSelected()) 
	        			startActivity(i_find);
	        		else
	        			alertP();
	        			break;
	        	case 1:
	        		if (provytaSelected()) 
	        			startActivity(i_pic);
	        		else
	        			alertP();
	        		break;	      
	        		
	        	case 2:
	        		Bundle b = new Bundle();
	        		b.putString("workflow_name", "Main"); //Your id
	        		i_wf.putExtras(b); //Put your id to your next Intent	
	        		startActivity(i_wf);
	        		break;
	        		
	        	case 3:
	        		startActivity(i_map);
	        		break;
	        		
	        	
	        	}
	        	
	        		
	        	
	        }

			private void alertP() {
				(new AlertDialog.Builder(
                        StartMenuActivity.this)
                .setTitle("Provyta ID saknas")
                .setMessage("Du måste först välja provyta (klicka kartfunktionen)")
                .setInverseBackgroundForced(true)
                .setPositiveButton("Jag förstår",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        })
              
                .create()).show();
			}

			private boolean provytaSelected() {
				Provyta py= CommonVars.cv().getProvyta();
				if (py !=null) {
					Log.d("NILS","Provyta var inte null");
				return true;
				}
				else
					return false;
			}
	    });
	}
	
	
	
	
/*
	public void onWorkflowButton(View view) {
	    String flow = (String)flow_spinner.getSelectedItem();
		Intent intent = new Intent(this, FlowEngineActivity.class);
		Bundle b = new Bundle();
		b.putString("workflow_name", flow); //Your id
		Log.d("NILS ","Workflow "+flow+" selected with length "+flow.length());
		intent.putExtras(b); //Put your id to your next Intent	
		startActivity(intent);
	}
	
	public void onButton(View view) {
	     String rutId = (String)r_spinner.getSelectedItem();
	     String deviceCol = (String)f_spinner.getSelectedItem();
	     
	     //Persist this selection
	     CommonVars.cv().putG("ruta_id", rutId);
	     CommonVars.cv().setRuta(T.findRuta(rutId));
	     
	     //Set device color
	     CommonVars.cv().setDeviceColor(deviceCol);
	     
	     //Jump to provyteselection.
//	     Intent intent = new Intent(getBaseContext(),SelectYta.class);
	     Intent intent = new Intent(getBaseContext(),MapSelect.class);
	     startActivity(intent);
	 }
	
	
*/
	/**
	 * Ask user before closing the application.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						StartMenuActivity.this.finish();
						//kill the synkservice if running
						Intent intent = new Intent(getBaseContext(),BluetoothRemoteDevice.class);
						stopService(intent);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Avsluta program")
			.setMessage("Vill du verkligen avsluta?").setPositiveButton("Ja", dialogClickListener)
			.setNegativeButton("Nej", dialogClickListener).show();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private class ImageAdapter extends BaseAdapter {
	    private Context mContext;

	    public ImageAdapter(Context c) {
	        mContext = c;
	    }

	    public int getCount() {
	        return mThumbIds.length;
	    }

	    public Object getItem(int position) {
	        return null;
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ImageView imageView;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	            imageView = new ImageView(mContext);
	            //imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
	            //imageView.setPadding(8, 8, 8, 8);
	            imageView.setAdjustViewBounds(true);
	        } else {
	            imageView = (ImageView) convertView;
	        }

	        imageView.setImageResource(mThumbIds[position]);
	        return imageView;
	    }

	    // references to our images
	    private Integer[] mThumbIds = {
	            R.drawable.orientera, R.drawable.kamera,
	            R.drawable.fixpunkter, R.drawable.karta,
	
	    };
	}

}
