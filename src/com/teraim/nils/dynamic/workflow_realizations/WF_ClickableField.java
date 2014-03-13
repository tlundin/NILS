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
import com.teraim.nils.dynamic.types.SpinnerDefinition;
import com.teraim.nils.dynamic.types.SpinnerDefinition.SpinnerElement;
import com.teraim.nils.dynamic.types.Table;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Variable.DataType;
import com.teraim.nils.dynamic.types.Workflow.Unit;
import com.teraim.nils.dynamic.workflow_abstracts.EventGenerator;
import com.teraim.nils.utils.DbHelper.Selection;
import com.teraim.nils.utils.Tools;

public abstract class WF_ClickableField extends WF_Not_ClickableField implements  EventGenerator {



	final LinearLayout inputContainer;

	private Map<Variable,View> myVars = new HashMap<Variable,View>();

	private GlobalState gs;
	private VariableConfiguration al;
	private static boolean HIDE=false,SHOW=true;
	private Map<Variable,String[]>values=new HashMap<Variable,String[]>();

	public abstract LinearLayout getFieldLayout();

	private final SpinnerDefinition sd;
	; 


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
			List<String> row = myVars.keySet().iterator().next().getBackingDataSet();
			switch (item.getItemId()) {
			case R.id.menu_goto:
				if (row!=null) {
					String url = al.getUrl(row);
					Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse(url));
					browse.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					gs.getContext().startActivity(browse);	        	
				}
				return true;	        
			case R.id.menu_delete:
				Iterator<Map.Entry<Variable,View>> it = myVars.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Variable,View> pairs = (Map.Entry<Variable,View>)it.next();
					Variable variable = pairs.getKey();
					DataType type = variable.getType();
					View view = pairs.getValue();
					if (type == DataType.numeric||
							type == DataType.text){
						EditText etview = (EditText)view.findViewById(R.id.edit);
						etview.setText("");
					} else				
						if (type == DataType.list) {
							LinearLayout sl = (LinearLayout)view;
							Spinner sp = (Spinner)sl.findViewById(R.id.spinner);
							if (sp.getTag(R.string.u1)!=null) {
								TextView descr = (TextView)sl.findViewById(R.id.extendedDescr);
								descr.setText("");
							}
							sp.setSelection(-1);

						} 
				}
				save();
				refreshOutputFields();
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.menu_info:
				if (row!=null) {
					new AlertDialog.Builder(myContext.getContext())
					.setTitle("Beskrivning")
					.setMessage(al.getVariableDescription(row))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							mode.finish();
						}
					})
					.setIcon(android.R.drawable.ic_dialog_info)
					.show();
				}
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
		Log.e("nils ","Creating WF_ClickableField: "+label+" "+id);
		gs = GlobalState.getInstance(context.getContext());
		sd = gs.getSpinnerDefinitions();
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
	public void addVariable(final Variable var, boolean displayOut,String format,boolean isVisible) {


		String varLabel = var.getLabel();
		String varId = var.getId();

		// Set an EditText view to get user input 
		if (displayOut && virgin) {
			virgin = false;
			Log.d("nils","Setting key variable to "+varId);
			super.setKeyRow(varId);
		}
		if (var.getType()==null) {
			o.addRow("");
			o.addRedText("VARIABLE "+var.getId()+" HAS NO TYPE. TYPE ASSUMED TO BE NUMERIC");
			var.setType(DataType.numeric);
		}
		Log.d("nils","Adding variable "+varLabel+" of type "+var.getType().name());
		Log.d("nils","var backing row: "+var.getBackingDataSet().toString());
		String unit = var.getPrintedUnit();
		switch (var.getType()) {
		case bool:
			//o.addRow("Adding boolean dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
			View view = LayoutInflater.from(myContext.getContext()).inflate(R.layout.ja_nej_radiogroup,null);
			TextView header = (TextView)view.findViewById(R.id.header);
			header.setText(varLabel);
			inputContainer.addView(view);
			myVars.put(var,view);
			break;
		case list:
			Log.d("nils","Adding spinner for label "+label);
			//o.addRow("Adding spinner field for dy-variable with label "+label+", name "+varId+", type "+var.getType().name()+" and unit "+unit.name());
			LinearLayout sl = (LinearLayout)LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_spinner, null);
			final TextView sHeader = (TextView) sl.findViewById(R.id.header);
			final TextView sDescr = (TextView) sl.findViewById(R.id.extendedDescr);
			final Spinner spinner =(Spinner) sl.findViewById(R.id.spinner);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(myContext.getContext(), android.R.layout.simple_spinner_dropdown_item,new ArrayList<String>() );		
			spinner.setAdapter(adapter);
			inputContainer.addView(sl);			
			myVars.put(var,sl);
			String[] opt=null;
			String[] val=null;
			sHeader.setText(var.getLabel());			
			String listValues = al.getTable().getElement("List Values", var.getBackingDataSet());
			//Parse 
			if (listValues.startsWith("@file")) {
				Log.d("nils","Found complex spinner");
				if (sd ==null) {
					o.addRow("");
					o.addRedText("Spinner definition file has not loaded. Spinners cannot be created!");
				} else {
					List<SpinnerElement> elems = sd.get(var.getId());
					if (elems == null) {
						Log.e("nils","No spinner elements for variable "+var.getId());
						Log.e("nils","backing row: "+var.getBackingDataSet());
						o.addRow("");
						o.addRedText("Complex Spinner variable "+var.getId()+" is not defining any elements in the configuration file" );

					} else {
						int i = 0;
						opt = new String[elems.size()];
						val = new String[elems.size()];
						for (SpinnerElement se:elems) {
							Log.d("nils","Spinner element: "+se.opt);
							opt[i] = se.opt;
							val[i++] = se.value;
						}
						spinner.setTag(R.string.u1,var.getId());
						values.put(var, val);
					}
				}
			} 
			else {
				if (listValues.startsWith("@col")) {
					spinner.setTag("dynamic");
				}
				else
				{
					Log.d("nils","Found static list definition..parsing");
					opt = listValues.split("\\|");
					if (opt==null||opt.length<2) {
						o.addRow("");
						o.addRedText("Could not split List Values for variable "+var.getId()+". Did you use '|' symbol??");					
					} else {

						if (opt[0].contains("=")) {
							Log.d("nils","found static list with value pairs");
							//we have a value. 
							Log.d("nils","List found is "+listValues+"...opt has "+opt.length+" elements.");
							val = new String[opt.length];
							int c = 0;
							String tmp[];
							for (String s:opt) {
								s=s.replace("{", "");
								s=s.replace("}", "");								
								tmp = s.split("=");
								if (tmp==null||tmp.length!=2) {
									Log.e("nils","found corrupt element: "+s);
									o.addRow("");
									o.addRedText("One of the elements in list "+var.getId()+"has a corrupt element. Comma missing?");
								} else {
									val[c]=tmp[1];
									opt[c]=tmp[0];							
								}
								c++;
							}
							values.put(var, val);
						}
					}
				}
			}
			if (opt!=null) {

				adapter.addAll(opt);
				Log.d("nils","Adapter has "+adapter.getCount()+" elements");
				adapter.notifyDataSetChanged();
				
				
				
			}
			else
				Log.e("nils","Couldnt add elements to spinner - opt was null in WF_ClickableField");



			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					//Check if this spinner has side effects.
					List<SpinnerElement> ems = sd.get((String)spinner.getTag(R.string.u1));
					List<String> curMapping = (List<String>)spinner.getTag(R.string.u2);
					if (ems!=null) {
						SpinnerElement e = ems.get(position);
						Log.d("nils","In onItemSelected. Spinner Element is "+e.opt+" with variables "+e.varMapping.toString());
						if (e.varMapping!=null) {
							//hide the views for the last selected.
							hideOrShowViews(curMapping,HIDE);
							hideOrShowViews(e.varMapping,SHOW);
							spinner.setTag(R.string.u2,e.varMapping);
							sDescr.setText(e.descr);
							Log.e("nils","DESCR TEXT SET TO "+e.descr);
						}
					}
				}

				private void hideOrShowViews(List<String> varIds,
						boolean mode) {
					if (varIds == null||varIds.size()==0)
						return;
					for (String varId:varIds) {
						if (varId!=null) {
							for(Variable v:myVars.keySet()) {
								if (v.getId().equals(varId.trim()))  {
									View gView = myVars.get(v);
									gView.setVisibility(mode?View.VISIBLE:View.GONE);
									if (gView instanceof LinearLayout) {
										EditText et =(EditText) gView.findViewById(R.id.edit);
										if (et!=null && mode==HIDE) {
											Log.e("nils","Setting view text to empty for "+v.getId());
											et.setText("");
										}
									} 
								}
							}
						}
					}
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
			inputContainer.addView(l);
			myVars.put(var,l);			
			break;
		case numeric:
			//o.addRow("Adding edit field for dy-variable with label "+label+", name "+varId+", type "+numType.name()+" and unit "+unit.name());
			l = LayoutInflater.from(myContext.getContext()).inflate(R.layout.edit_field_numeric,null);
			header = (TextView)l.findViewById(R.id.header);
			header.setText(varLabel+" ("+unit+")");
			//etview.setText(Tools.getPrintedUnit(unit));
			inputContainer.addView(l);
			myVars.put(var,l);

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
		if (!isVisible) 
			myVars.get(var).setVisibility(View.GONE);


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
				RadioGroup rbg = (RadioGroup)view.findViewById(R.id.radioG);
				//If checked set value to True.
				int id = rbg.getCheckedRadioButtonId();
				variable.setValue("1");
				if (id == -1 || id == R.id.nej)
					variable.setValue("0");
			} else 
				if (type == DataType.numeric||
				type == DataType.text){
					EditText etview = (EditText)view.findViewById(R.id.edit);
					String txt = etview.getText().toString();
					if (txt.trim().length()>0)
						variable.setValue(txt);
					else
						variable.deleteValue();
				} else				
					if (type == DataType.list) {
						LinearLayout sl = (LinearLayout)view;
						Spinner sp = (Spinner)sl.findViewById(R.id.spinner);
						int s = sp.getSelectedItemPosition();
						String v[] = values.get(variable);
						if (v!=null) {
							if (s>=0&&s<v.length) 						
								variable.setValue(v[s]);
							else
								variable.deleteValue();
						}
						else
							variable.setValue((String)sp.getSelectedItem());
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
					Log.d("nils","refreshing edittext with varid "+variable.getId());
					EditText et = (EditText)v.findViewById(R.id.edit);
					if (et!=null)
						et.setText(value==null?"":value);
					else
						Log.d("nils","WF_Clickable:view was null in refreshinput");
				} else
					if (numType==DataType.list) {
						String[] opt = null;
						Spinner sp = (Spinner)v.findViewById(R.id.spinner);
						String tag = (String) sp.getTag();
						
						String val[] = values.get(variable);
						if (val!=null) {
							for (int i=0;i<val.length;i++) {
								if (val[i].equals(variable.getValue()))
									sp.setSelection(i);
							}
						}

						
						else if (tag!=null && tag.equals("dynamic")) {
							//Get the list values
							String listValues = al.getTable().getElement("List Values", variable.getBackingDataSet());
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

							//Add dropdown.
							if (opt==null)
								Log.e("nils","OPT IS STILL NULL!!!");
							else {

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


	}
}












