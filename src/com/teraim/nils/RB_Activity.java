package com.teraim.nils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import com.teraim.nils.DataTypes.Delyta;
import com.teraim.nils.DataTypes.Provyta;
import com.teraim.nils.DataTypes.Ruta;

public abstract class RB_Activity extends Activity {
	
	protected int[] ids;
	protected Delyta delyta=null;
	private int c = 0;

	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Global parameter transfer of delyta.
        delyta = CommonVars.cv().getDelyta();
       
    }
    
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        
        // Check which radio button was clicked
        int id = (view.getId());
        
        for (int i = 0; i< ids.length;i++)
        	if (ids[i]==id) {
        		
        		String name = genName(i);
        		Log.d("NILS","Generated name "+name);
        		//Save the parameter.
        		delyta.put("markslag", name);            
        	}
        finish();
    }
    
    
    
    public void add(int x) {
    	ids[c++]=x;
    }
    
    protected abstract String genName(int id);
    
}
