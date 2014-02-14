package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.VarIdentifier;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;

public abstract class WF_ClickableField extends WF_Not_ClickableField implements  EventGenerator {



	final LinearLayout inputContainer;

	protected Map<VarIdentifier,View> myVars = new HashMap<VarIdentifier,View>();

	private GlobalState gs;

	public abstract LinearLayout getFieldLayout();
	public abstract String getFormattedText(VarIdentifier varId, String value);
	public abstract String getFormattedUnit(VarIdentifier varId);

	@Override
	public Set<VarIdentifier> getAssociatedVariables() {
		return myVars.keySet();
	}

	public  WF_ClickableField(final String myId,final String descriptionT, WF_Context context,String id, View view) {
		super(myId,descriptionT,context,view);	
		gs = GlobalState.getInstance(context.getContext());
		o = gs.getLogger();
		//SpannableString content = new SpannableString(headerT);
		//content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		inputContainer = new LinearLayout(ctx);
		inputContainer.setOrientation(LinearLayout.VERTICAL);
		inputContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.MATCH_PARENT,
				1));


		//TODO: SUPPORT FOR OTHER THAN EDITTEXT!!!

		//Empty all inputs and save.
		getWidget().setClickable(true);	
		getWidget().setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				for (View inf:myVars.values()) {
					if (inf!=null) {
						if (inf instanceof EditText)
							((EditText)inf).setText("");

					}
				}
				save();
				return true;
			}
		});


		getWidget().setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {

				//On click, create dialog 			
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				alert.setTitle(myId);
				alert.setMessage(descriptionT);

				alert.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {				  
						save();
						refreshValues();
						((ViewGroup)inputContainer.getParent()).removeView(inputContainer);
					}
				});
				alert.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						((ViewGroup)inputContainer.getParent()).removeView(inputContainer);
					}
				});	
				if (inputContainer.getParent()!=null)
					((ViewGroup)inputContainer.getParent()).removeView(inputContainer);
				Dialog d = alert.setView(inputContainer).create();
				d.setCancelable(false);
				//WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
				//lp.copyFrom(d.getWindow().getAttributes());
				//lp.height = WindowManager.LayoutParams.FILL_PARENT;
				//lp.height = 600;

				d.show();

				//d.getWindow().setAttributes(lp);
			}		
		});	
	}

	@Override
	public void addVariable(String varLabel, String varId, Unit unit, Variable.DataType numType, Variable.StorageType varType, boolean displayOut) {

		if (displayOut && virgin) {
			virgin = false;
			super.setKeyRow(varId);
		}

		// Set an EditText view to get user input 
		VarIdentifier varIdentifier = new VarIdentifier(ctx,varLabel,varId,numType,varType,unit);


		switch (numType) {
		case bool:
			o.addRow("Adding boolean dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
			View view = LayoutInflater.from(ctx).inflate(R.layout.ja_nej_radiogroup,null);
			TextView header = (TextView)view.findViewById(R.id.header);
			header.setText(varLabel);
			RadioGroup rbg = (RadioGroup)view.findViewById(R.id.radioG);
			inputContainer.addView(view);
			myVars.put(varIdentifier,rbg);
			break;
		case list:
			//Get the list values
			Table t = gs.getArtLista().getTable();
			List<String> row = t.getRowContaining("Variable Name", varId);
			String options = t.getElement("List Values", row);
			String[] opt = null;
			if (options == null||options.isEmpty()) {
				o.addRow("");
				o.addRedText("List Values empty for List variable "+varId);
				opt = new String[] {""};
			} else {
				opt = options.split("\\|");
				if (opt==null||opt.length<2) {
					o.addRow("");
					o.addRedText("Could not split List Values for variable "+varId+". Did you use '|' symbol??");					
				}					
			}
			//Add dropdown.
			o.addRow("Adding spinner field for dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
			final Spinner spinner = (Spinner)LayoutInflater.from(ctx).inflate(R.layout.edit_field_spinner, null);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, opt);		
			spinner.setAdapter(adapter);
			inputContainer.addView(spinner);
			myVars.put(varIdentifier,spinner);
			break;
		case text:
			o.addRow("Adding text field for dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
			View l = LayoutInflater.from(ctx).inflate(R.layout.edit_field_text,null);
			header = (TextView)l.findViewById(R.id.header);
			header.setText(varLabel);
			EditText etview = (EditText)l.findViewById(R.id.edit);
			inputContainer.addView(l);
			myVars.put(varIdentifier,etview);			
			break;
		case numeric:
			o.addRow("Adding edit field for dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
			l = LayoutInflater.from(ctx).inflate(R.layout.edit_field_numeric,null);
			header = (TextView)l.findViewById(R.id.header);
			etview = (EditText)l.findViewById(R.id.edit);
			header.setText(varLabel+" ("+varIdentifier.getPrintedUnit()+")");
			etview.setText(varIdentifier.getPrintedValue());
			inputContainer.addView(l);
			myVars.put(varIdentifier,etview);

			break;
		}


		if (displayOut) {
			LinearLayout ll = getFieldLayout();

			/*
			 TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);

			String value = varIdentifier.getPrintedValue();
			if (!value.isEmpty()) {
				o.setText(varLabel+": "+value);	
				u.setText(" ("+varIdentifier.getPrintedUnit()+")");
			}
			 */
			myOutputFields.put(varIdentifier,ll);
			outputContainer.addView(ll);
		}
		refreshInputFields();
	}



	private void save() {
		//for now only delytevariabler. 
		Iterator<Map.Entry<VarIdentifier,View>> it = myVars.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<VarIdentifier,View> pairs = (Map.Entry<VarIdentifier,View>)it.next();
			VarIdentifier varId = pairs.getKey();
			View view = pairs.getValue();
			if (varId.numType == Variable.DataType.bool) {
				//Get the yes radiobutton.
				RadioGroup rb = (RadioGroup)view;
				//If checked set value to True.
				int id = rb.getCheckedRadioButtonId();
				varId.setValue("1");
				if (id == -1 || id == R.id.nej)
					varId.setValue("0");
			} else 

				if (varId.numType == Variable.DataType.numeric||
				varId.numType == Variable.DataType.text){
					EditText et = (EditText)view;
					varId.setValue(et.getText().toString());
				} else				
					if (varId.numType == Variable.DataType.list) {
						Spinner sp = (Spinner)view;
						String s = (String)sp.getSelectedItem();
						varId.setValue(s);
					} 
		}
		myContext.registerEvent(new WF_Event_OnSave(this.getId()));
	}

	@Override
	public void refreshInputFields(){
		Log.d("nils","In refreshinputfields");
		Set<Entry<VarIdentifier, View>> vars = myVars.entrySet();
		for(Entry<VarIdentifier, View>entry:vars) {
			VarIdentifier varId = entry.getKey();
			String value = varId.getPrintedValue();

			if (value == null)
				Log.d("nils","WF_Clickable:value was null in refreshinput");
			else {
				View v = entry.getValue();

				if (varId.numType == Variable.DataType.bool) {
					RadioButton ja = (RadioButton)v.findViewById(R.id.ja);
					RadioButton nej = (RadioButton)v.findViewById(R.id.nej);
					if(value!=null) {
						if(value.equals("1"))
							ja.setEnabled(true);
						else
							nej.setEnabled(true);
						ja.setChecked(true);
					}
				} else
					if (varId.numType == Variable.DataType.numeric||
					varId.numType ==Variable.DataType.text) {
						EditText et = (EditText)v.findViewById(R.id.edit);
						if (et!=null)
							et.setText(value);
						else
							Log.d("nils","WF_Clickable:view was null in refreshinput");
					} else
						if (varId.numType==Variable.DataType.list) {
							Spinner sp = (Spinner)v;
							String item = null;
							for (int i=0;i<sp.getAdapter().getCount();i++) {
								item = (String)sp.getAdapter().getItem(i);
								if (item == null)
									continue;
								else
									if (item.equals(value))
										sp.setSelection(i);
							}

						}
			} 

		}
		refreshValues();
	}












}