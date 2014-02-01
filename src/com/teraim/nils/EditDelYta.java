package com.teraim.nils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import com.teraim.nils.dynamic.types.Delyta;
import com.teraim.nils.dynamic.types.Provyta;

public class EditDelYta extends Activity {

	int[] dotIds = { 
			R.id.dy_e1,R.id.dy_e2,R.id.dy_e3,R.id.dy_e4,R.id.dy_e5,R.id.dy_e6,R.id.dy_e7,R.id.dy_e8,
			R.id.dy_e9,R.id.dy_e10,R.id.dy_e11,R.id.dy_e12,R.id.dy_e13,R.id.dy_e14,R.id.dy_e15,R.id.dy_e16
	};
	Provyta p; 
	int index = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Get the current provyta
		p = GlobalState.getInstance(this).getCurrentProvyta();
		Intent intent = getIntent();
		//Get the index selected.
		index = intent.getIntExtra("com.teraim.nils.addRow", -1);
		setContentView(R.layout.editdelyta);
		//Index -1 means that a new row should be added.
		//Otherwise, if index postivie, it contains reference to a row that should be edited.	
		if (index!=-1) {
			
			Delyta d = p.getDelytor().get(index);
			int points[][] = d.getPoints();
			if (points !=null) {
			//Insert the existing values into the edit fields.
			EditText et;
			int i = 0;
			boolean avstand = true;
			for (int ide:dotIds) {
				et = (EditText)findViewById(ide);
				//Set value to avstånd.
				int value =  points[i][0];
				//If bool avstand = false, set value to riktning instead.
				
				if (!avstand)  {			
					value =  points[i][1];	
					//increase tåg-counter.
					i++;
				}  
				//swap.
				avstand = !avstand;
				et.setText(Integer.toString(value));
				
				//exit if i==length of points array.
				
				if (i==points.length)
					break;
					
			}
			}
		}
				
	}
	
	
	public void onSave(View v) {
		String id;
		if (p.getDelytor()==null || p.getDelytor().size()==0)
			id = "1";
		//Is row index set? then this is an edit.
		else if (index != -1)
			id = p.getDelytor().get(index).getId();
		//else this is a new row.
		else	{
			int size = p.getDelytor().size();
			String lastId = p.getDelytor().get(size-1).getId();
			int lastId_int;
			try {
				lastId_int = Integer.parseInt(lastId);
			} catch (NumberFormatException e) {
				lastId_int = -1;
			}
			//New id is the id of the last element on the list + 1, if numeric.
			if (lastId_int>0)
				id = Integer.toString(++lastId_int);
			else
				//IF non-numeric ID - improvise.
				id = lastId+"_"+size;
		}
		//Transfer values from the edit fields to the data structure.
		String[] tag=new String[16];
		for (int i=0;i<tag.length;i++)
			tag[i++]="-1";
		EditText et;
		int i=0;
		for (int ide:dotIds) {
			et = (EditText)findViewById(ide);
			Editable ett = et.getText();
			if (ett==null||ett.toString().equals("")) 
				break;
			else 
				//Get the avstånd och riktning values given by user.
				tag[i++]=ett.toString();
			
		}
		//Check the data.
		//Only one pair of riktning,avstånd? That is not ok..
		if (i<3) {
			new AlertDialog.Builder(this)
		    .setTitle("Fel")
		    .setMessage("Ett tåg måste ha åtminstone två punkter.")
		    .setPositiveButton("Okej, jag förstår.", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		           
		        }
		     })
		     .show();
		} else {
			if (index != -1) {
				p.updateDelyta(index, tag);
			} else
				p.addDelyta(id, tag);
		finish();
		}
	}
	
	public void onCancel(View v) {
		finish();
	}
	

	   
}
