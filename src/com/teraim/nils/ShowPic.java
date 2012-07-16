package com.teraim.nils;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class ShowPic extends Activity {


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		Dbhelper dbh = new Dbhelper(this);
		dbh.open();
		String dir = null;
		if (extras != null) 
			dir = extras.getString("compass");
		else
			Log.e("NILS","uhoh!! No extras!");
		Log.d("NILS","I am in showpic with compass: "+dir);
		LayoutParams params =
				new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT,
						LayoutParams.WRAP_CONTENT);
		//---create a layout---
		LinearLayout layout = new LinearLayout(this);
		ImageView tv = dbh.getImage(dir, 0);
		tv.setLayoutParams(params);
		layout.addView(tv);
		dbh.close();
	}
}
