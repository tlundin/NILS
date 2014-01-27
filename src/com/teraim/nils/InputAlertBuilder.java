package com.teraim.nils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class InputAlertBuilder {

	
	public static OnClickListener createAlert(final StoredVariable var,final String headerT, final String bodyT, final ViewGroup outputView) {

		
		return new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				//On click, create dialog 			
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				alert.setTitle(headerT);
				alert.setMessage(bodyT);
				final LinearLayout inputView = new LinearLayout(v.getContext());
				inputView.setOrientation(LinearLayout.VERTICAL);
	            inputView.setLayoutParams(new LinearLayout.LayoutParams(
	                                LinearLayout.LayoutParams.MATCH_PARENT, 
	                               LinearLayout.LayoutParams.MATCH_PARENT,
	                                1));

				alert.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {				  
						
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
		protected ViewGroup myView;
		
		public AlertBuildHelper(Context c) {
			this.c = c;
		}
		public abstract ViewGroup createView(ViewGroup root);
		
		
		
		public void addView(View v) {
			myView.addView(v);
		}

		public abstract void setResult(StoredVariable[] var, View inputView,View outputView);	
	}
	
	


}
	
	

