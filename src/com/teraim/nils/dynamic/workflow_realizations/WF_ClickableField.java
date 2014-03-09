package com.teraim.nils.dynamic.workflow_realizations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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
import com.teraim.nils.utils.DbHelper.Selection;
import com.teraim.nils.utils.Tools;

public abstract class WF_ClickableField extends WF_Not_ClickableField implements  EventGenerator {



	final LinearLayout inputContainer;

	protected Map<Variable,View> myVars = new HashMap<Variable,View>();

	private GlobalState gs;
	private VariableConfiguration al;


	public abstract LinearLayout getFieldLayout();

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
			MenuItem x = menu.getItem(0);
			if (!Tools.isNetworkAvailable(gs.getContext()))
				x.setVisible(false);
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			List<String> row;
			switch (item.getItemId()) {
			case R.id.menu_goto:
				row = myVars.keySet().iterator().next().getBackingDataSet();
				if (row!=null) {
					String url = al.getUrl(row);
					Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse(url));
					browse.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					gs.getContext().startActivity(browse);	        	
				}
				return true;	        
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

	public  WF_ClickableField(final String label,final String descriptionT, WF_Context context,String id, View view,boolean isVisible) {
		super(label,descriptionT,context,view,isVisible);	
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
				alert.setTitle(label);
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
	public void addVariable(final Variable var, boolean displayOut,String format) {


		String varLabel = var.getLabel();
		String varId = var.getId();

		// Set an EditText view to get user input 
		if (displayOut && virgin) {
			virgin = false;
			Log.d("nils","Setting key variable to "+varId);
			super.setKeyRow(varId);
		}

		Log.d("nils","Adding variable "+varLabel);
		String unit = var.getPrintedUnit();
		switch (var.getType()) {
		case bool:
			//o.addRow("Adding boolean dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
			View view = LayoutInflater.from(myContext.getContext()).inflate(R.layout.ja_nej_radiogroup,null);
			TextView header = (TextView)view.findViewById(R.id.header);
			header.setText(varLabel);
			RadioGroup rbg = (RadioGroup)view.findViewById(R.id.radioG);
			inputContainer.addView(view);
			myVars.put(var,rbg);
			break;
		case list:
				Log.d("nils","Adding spinner for label "+label);
				//o.addRow("Adding spinner field for dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
				final Spinner spinner = (Spinner)LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_spinner, null);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(myContext.getContext(), android.R.layout.simple_spinner_dropdown_item,new ArrayList<String>() );		
				spinner.setAdapter(adapter);
				inputContainer.addView(spinner);
				myVars.put(var,spinner);
				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
						//Log.d("nils","spinner changed value, presaving");
						//var.setValueWithoutCommit((String)spinner.getItemAtPosition(position));
						refreshInputFields();
					
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {

					}

				});
			
			break;
		case text:
			//o.addRow("Adding text field for dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
			View l = LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_text,null);
			header = (TextView)l.findViewById(R.id.header);
			header.setText(varLabel+" "+unit);
			EditText etview = (EditText)l.findViewById(R.id.edit);
			inputContainer.addView(l);
			myVars.put(var,etview);			
			break;
		case numeric:
			//o.addRow("Adding edit field for dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
			l = LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_numeric,null);
			header = (TextView)l.findViewById(R.id.header);
			etview = (EditText)l.findViewById(R.id.edit);
			header.setText(varLabel+" ("+unit+")");
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
			myOutputFields.put(var,new OutC(ll,format));
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
						
						//Get the list values
						Table t = gs.getArtLista().getTable();
						String listValues = t.getElement("List Values", variable.getBackingDataSet());
						String[] opt = null;
						if (listValues == null||listValues.isEmpty()) {
							o.addRow("");
							o.addRedText("List Values empty for List variable "+variable.getId());
							opt = new String[] {"No list values in configuration file"};
						} else {
							if (listValues.startsWith("@")) {
								Log.d("nils","Found dynamic list definition..parsing");
								String[] valuePairs = listValues.split("\\|");
								if (valuePairs.length>0) {
									String [] columnSelector = valuePairs[0].split("=");
									String[] column;
									boolean error = false;
									if (columnSelector[0].equalsIgnoreCase("@col")) {
										Log.d("nils","found column selector");
										//Column to select.
										String dbColName = gs.getDb().getColumnName(columnSelector[1]);
										if (dbColName!=null) {
											Log.d("nils","Real Column name for "+columnSelector[1]+" is "+dbColName);
											column = new String[1];
											column[0]=dbColName;
										} else {
											Log.d("nils","Column referenced in List definition for variable "+variable.getLabel()+" not found: "+columnSelector[1]);
											o.addRow("");
											o.addRedText("Column referenced in List definition for variable "+variable.getLabel()+" not found: "+columnSelector[1]);
											error = true;
											break;
										}
										if (!error) {
										//Any other columns part of key?
										Map<String,String>keySet = new HashMap<String,String>();
										if (valuePairs.length>1) {
											//yes..include these in search
											Log.d("nils","found additional keys...");
											String[] keyPair;							
											for (int i=1;i<valuePairs.length;i++) {
												keyPair = valuePairs[i].split("=");
												if (keyPair!=null && keyPair.length==2) {
													String valx=al.getVariableValue(null,keyPair[1]);
													if (valx!=null) 										
														keySet.put(keyPair[0], valx);
													else {
														Log.e("nils","The variable used for dynamic list "+variable.getLabel()+" is not returning a value");
														o.addRow("");
														o.addRedText("The variable used for dynamic list "+variable.getLabel()+" is not returning a value");
													}
												} else {
													Log.d("nils","Keypair error: "+keyPair);
													o.addRow("");
													o.addRedText("Keypair referenced in List definition for variable "+variable.getLabel()+" cannot be read: "+keyPair);
												}
											}
											
										} else 
											Log.d("nils","no additional keys..only column");
											Selection s = gs.getDb().createCoulmnSelection(keySet);
											List<String[]> values = gs.getDb().getValues(column, s);
											if (values !=null) {
												Log.d("nils","Got "+values.size()+" results");
												//Remove duplicates and sort.
												SortedSet<String> ss = new TreeSet<String>(new Comparator<String>(){
									                public int compare(String a, String b){
									                    return Integer.parseInt(a)-Integer.parseInt(b);
									                }}						                         
									        );
												for (int i = 0; i<values.size();i++) 
													ss.add(values.get(i)[0]);
												opt = new String[ss.size()];
												int i = 0; 
												Iterator<String> it = ss.iterator();
												while (it.hasNext()) {
													opt[i++]=it.next();
												}
											}
										} else
											opt=new String[]{"Config Error...please check your list definitions for variable "+variable.getLabel()};


									} else
										Log.e("nils","List "+variable.getId()+" has too few parameters: "+listValues.toString());
								} else
									Log.e("nils","List "+variable.getId()+" has strange parameters: "+listValues.toString());

							} else 
							{
								Log.d("nils","Found static list definition..parsing");
								opt = listValues.split("\\|");
								if (opt==null||opt.length<2) {
									o.addRow("");
									o.addRedText("Could not split List Values for variable "+variable.getId()+". Did you use '|' symbol??");					
								}					
							}
							
							//Add dropdown.
							if (opt==null)
								Log.e("nils","OPT IS STILL NULL!!!");
							else {
								for (int i=0;i<opt.length;i++) {
									Log.d("nils","OPT "+i+":"+opt[i]);
								}
							}
							
						}
						((ArrayAdapter<String>)sp.getAdapter()).clear();
						((ArrayAdapter<String>)sp.getAdapter()).addAll(opt);
						
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












