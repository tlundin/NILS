package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Variable.DataType;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;

public abstract class WF_ClickableField extends WF_Not_ClickableField implements  EventGenerator {



	final LinearLayout inputContainer;

	protected Map<Variable,View> myVars = new HashMap<Variable,View>();

	private GlobalState gs;
	private VariableConfiguration al;


	public abstract LinearLayout getFieldLayout();
	public abstract String getFormattedText(Variable varId, String value);

	@Override
	public Set<Variable> getAssociatedVariables() {
		return myVars.keySet();
	}
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.tagpopmenu, menu);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.menu_delete:
	            	for (View inf:myVars.values()) {
						if (inf!=null) {
							if (inf instanceof EditText)
								((EditText)inf).setText("");

						}
					}
					save();
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            case R.id.menu_info:
	            	new AlertDialog.Builder(myContext.getContext())
	                .setTitle("Beskrivning")
	                .setMessage(myDescription)
	                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) { 
	                    	mode.finish();
	                    }
	                 })
	                .setIcon(android.R.drawable.ic_dialog_info)
	                 .show();
	            	
	            	return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	        mActionMode = null;
	    }
	};

	ActionMode mActionMode;

	public  WF_ClickableField(final String myId,final String descriptionT, WF_Context context,String id, View view) {
		super(myId,descriptionT,context,view,true);	
		gs = GlobalState.getInstance(context.getContext());
		al = gs.getArtLista();
		o = gs.getLogger();
		//SpannableString content = new SpannableString(headerT);
		//content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		inputContainer = new LinearLayout(context.getContext());
		inputContainer.setOrientation(LinearLayout.VERTICAL);
		inputContainer.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.MATCH_PARENT,
				1));

		//Empty all inputs and save.
		getWidget().setClickable(true);	
		getWidget().setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				
				
		        
				if (mActionMode != null) {
		            return false;
		        }

		        // Start the CAB using the ActionMode.Callback defined above
		        mActionMode = ((Activity)myContext.getContext()).startActionMode(mActionModeCallback);
		        WF_ClickableField.this.getWidget().setSelected(true);
		        return true;
				/*
				for (View inf:myVars.values()) {
					if (inf!=null) {
						if (inf instanceof EditText)
							((EditText)inf).setText("");

					}
				}
				save();
				return true;
				*/
			}
		});

		
		



		getWidget().setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {

				//On click, create dialog 			
				AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
				alert.setTitle(myId);
				alert.setMessage(descriptionT);
				refreshInputFields();

				alert.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {				  
						save();
						refreshOutputFields();
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
	public void addVariable(Variable var, boolean displayOut) {


		String varLabel = var.getLabel();
		String postLabel = "";
		String varId = var.getId();

		// Set an EditText view to get user input 
		if (displayOut && virgin) {
			virgin = false;
			super.setKeyRow(varId);
		}

		Unit unit = var.getUnit();
		switch (var.getType()) {
		case bool:
			//o.addRow("Adding boolean dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
			View view = LayoutInflater.from(myContext.getContext()).inflate(R.layout.ja_nej_radiogroup,null);
			TextView header = (TextView)view.findViewById(R.id.header);
			header.setText(varLabel+" "+postLabel);
			RadioGroup rbg = (RadioGroup)view.findViewById(R.id.radioG);
			inputContainer.addView(view);
			myVars.put(var,rbg);
			break;
		case list:
			//Get the list values
			Table t = gs.getArtLista().getTable();
			String options = t.getElement("List Values", var.getBackingDataSet());
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
			Log.d("nils","Adding spinner for label "+label);
			//o.addRow("Adding spinner field for dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
			final Spinner spinner = (Spinner)LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_spinner, null);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(myContext.getContext(), android.R.layout.simple_spinner_dropdown_item, opt);		
			spinner.setAdapter(adapter);
			inputContainer.addView(spinner);
			myVars.put(var,spinner);
			break;
		case text:
			//o.addRow("Adding text field for dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
			View l = LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_text,null);
			header = (TextView)l.findViewById(R.id.header);
			header.setText(varLabel+" "+postLabel);
			EditText etview = (EditText)l.findViewById(R.id.edit);
			inputContainer.addView(l);
			myVars.put(var,etview);			
			break;
		case numeric:
			//o.addRow("Adding edit field for dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
			l = LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_numeric,null);
			header = (TextView)l.findViewById(R.id.header);
			etview = (EditText)l.findViewById(R.id.edit);
			header.setText(varLabel+" ("+unit.name()+")"+" "+" "+postLabel);
			//etview.setText(Tools.getPrintedUnit(unit));
			inputContainer.addView(l);
			myVars.put(var,etview);

			break;
		}


		if (displayOut) {
			LinearLayout ll = getFieldLayout();

			/*
			 TextView o = (TextView)ll.findViewById(R.id.outputValueField);
			TextView u = (TextView)ll.findViewById(R.id.outputUnitField);

			String value = Variable.getPrintedValue();
			if (!value.isEmpty()) {
				o.setText(varLabel+": "+value);	
				u.setText(" ("+Variable.getPrintedUnit()+")");
			}
			 */
			myOutputFields.put(var,ll);
			outputContainer.addView(ll);
		}
		refreshInputFields();
		refreshOutputFields();
	}



	private void save() {
		//for now only delytevariabler. 
		Iterator<Map.Entry<Variable,View>> it = myVars.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Variable,View> pairs = (Map.Entry<Variable,View>)it.next();
			Variable variable = pairs.getKey();
			DataType type = variable.getType();
			View view = pairs.getValue();
			if (type == DataType.bool) {
				//Get the yes radiobutton.
				RadioGroup rb = (RadioGroup)view;
				//If checked set value to True.
				int id = rb.getCheckedRadioButtonId();
				variable.setValue("1");
				if (id == -1 || id == R.id.nej)
					variable.setValue("0");
			} else 

				if (type == DataType.numeric||
				type == DataType.text){
					EditText et = (EditText)view;
					String txt = et.getText().toString();
					if (txt.trim().length()>0)
						variable.setValue(txt);
					else
						variable.deleteValue();
				} else				
					if (type == DataType.list) {
						Spinner sp = (Spinner)view;
						String s = (String)sp.getSelectedItem();
						variable.setValue(s);
					} 
		}
		myContext.registerEvent(new WF_Event_OnSave(this.getId()));
	}

	@Override
	public void refreshInputFields(){
		DataType numType;
		Log.d("nils","In refreshinputfields");
		Set<Entry<Variable, View>> vars = myVars.entrySet();
		for(Entry<Variable, View>entry:vars) {
			Variable variable = entry.getKey();
			String value = variable.getValue();
			numType = variable.getType();

			View v = entry.getValue();

			if (numType == DataType.bool) {
				RadioButton ja = (RadioButton)v.findViewById(R.id.ja);
				RadioButton nej = (RadioButton)v.findViewById(R.id.nej);
				if(value!=null) {
					if(value == null||value.equals("1"))
						ja.setEnabled(true);
					else
						nej.setEnabled(true);
					ja.setChecked(true);
				}
			} else
				if (numType == Variable.DataType.numeric||
				numType ==DataType.text) {
					EditText et = (EditText)v.findViewById(R.id.edit);
					if (et!=null)
						et.setText(value==null?"":value);
					else
						Log.d("nils","WF_Clickable:view was null in refreshinput");
				} else
					if (numType==DataType.list) {
						Spinner sp = (Spinner)v;
						String item = null;
						if (sp.getAdapter().getCount()>0) {
							if (value!=null) {								
								for (int i=0;i<sp.getAdapter().getCount();i++) {
									item = (String)sp.getAdapter().getItem(i);
									if (item == null)
										continue;
									else
										if (item.equals(value))
											sp.setSelection(i);
								}
							}
						} else {
							o.addRow("");
							o.addRedText("Empty spinner for variable "+v+". Check your variable configuration.");
						}

					}
		} 

	}

}












