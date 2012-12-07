package com.teraim.nils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class EditDelYta extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.editdelyta);
	}
	
	public void onSave(View v) {
		Toast.makeText(this, "User Pressed save",Toast.LENGTH_SHORT).show();
		finish();
	}
}
