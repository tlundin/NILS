package com.teraim.nils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

public class InputAlertBuilder {

	public static OnClickListener createAlert(final int id,final String headerT, final String bodyT, final AlertBuildHelper abh, final View outputView) {

	
		return new OnClickListener() {

			@Override
			public void onClick(View v) {

				//On click, create dialog 			
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				alert.setTitle(headerT);
				alert.setMessage(bodyT);
				final View inputView = abh.createView();
				alert.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {				  
						abh.setResult(id,inputView,outputView);
					}

				});
				alert.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});	
				Dialog d = alert.setView(inputView).create();
				//WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			    //lp.copyFrom(d.getWindow().getAttributes());
			    //lp.height = WindowManager.LayoutParams.FILL_PARENT;
			    //lp.height = 600;
			    
			    d.show();
			    
			    //d.getWindow().setAttributes(lp);
			}		
		};	

	}
	public static abstract class AlertBuildHelper {

		public Context c;
		
		public AlertBuildHelper(Context c) {
			this.c = c;
		}
		public abstract View createView();

		public abstract void setResult(int resultId, View inputView,View outputView);	
	}
	
	


}
	
	

