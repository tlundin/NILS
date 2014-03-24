package com.teraim.nils.dynamic.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;

import com.teraim.nils.ParameterSafe;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.Executor;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.types.ColumnDescriptor;
import com.teraim.nils.dynamic.types.Variable;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.workflow_realizations.WF_Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Event_OnSave;
import com.teraim.nils.dynamic.workflow_realizations.WF_Instance_List;
import com.teraim.nils.dynamic.workflow_realizations.WF_TimeOrder_Sorter;
import com.teraim.nils.utils.DbHelper;
import com.teraim.nils.utils.DbHelper.Selection;
import com.teraim.nils.utils.InputFilterMinMax;
import com.teraim.nils.utils.PersistenceHelper;
import com.teraim.nils.utils.Tools;


public class LinjePortalTemplate extends Executor  {
	List<WF_Container> myLayouts;
	VariableConfiguration al;
	DbHelper db;
	
	private PersistenceHelper ph;
	private ParameterSafe ps;
	private double[] cords;
	EditText meterEd;
	String currentYear,currentRuta,currentLinje;
	private LinearLayout aggregatePanel,fieldList,selectedPanel;
	private WF_Container root;
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("nils","in onCreateView of LinjePortalTemplate");
		View v = inflater.inflate(R.layout.template_linje_portal_wf, container, false);	
		ps = gs.getSafe();
		al = gs.getArtLista();
		db = gs.getDb();
		currentYear = al.getVariableValue(null,"Current_Year");
		currentRuta = al.getVariableValue(null,"Current_Ruta");
		currentLinje = al.getVariableValue(null,"Current_Linje");

		if (currentLinje == null) {
			AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
			alert.setTitle("Ingen linje angiven!");
			alert.setMessage("Den här menyn går inte att köra utan att en linje valts under ProvytaMenyn.");
			alert.setPositiveButton("Jag förstår", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			});
			alert.setIcon(android.R.drawable.ic_dialog_alert);
			alert.show();
		}
		else {
			myContext.emptyContianers();

			root = new WF_Container("root", (LinearLayout)v.findViewById(R.id.root), null);
			aggregatePanel = (LinearLayout)v.findViewById(R.id.aggregates);
			fieldList = (LinearLayout)v.findViewById(R.id.fieldList);
			//ListView selectedList = (ListView)v.findViewById(R.id.SelectedL);
			selectedPanel = (LinearLayout)v.findViewById(R.id.selected);
			myLayouts = new ArrayList<WF_Container>();
			myLayouts.add(root);
			myLayouts.add(new WF_Container("Field_List_panel_1", fieldList, root));
			myLayouts.add(new WF_Container("Aggregation_panel_3", aggregatePanel, root));
			myLayouts.add(new WF_Container("Filter_panel_4", (LinearLayout)v.findViewById(R.id.filterPanel), root));
			myLayouts.add(new WF_Container("Field_List_panel_2", selectedPanel, root));
			myContext.addContainers(getContainers());

			//
			Log.d("nils","year: "+currentYear+" Ruta: "+currentRuta+" Linje: "+currentLinje);

			Map<String,String> keySet = Tools.createKeyMap(VariableConfiguration.KEY_YEAR,currentYear,"ruta",currentRuta,"linje",currentLinje);

			Selection selection = db.createSelection(keySet,"linjeobjekt_diva");

			List<ColumnDescriptor> columns = new ArrayList<ColumnDescriptor>();
			columns.add(new ColumnDescriptor("meter",true,false,true));
			columns.add(new ColumnDescriptor("value",false,true,false));
			WF_Instance_List selectedList = new WF_Instance_List("selected_list", true, myContext,columns, selection,"linjeobjekt_diva",keySet);

			selectedList.addSorter(new WF_TimeOrder_Sorter());

			selectedPanel.addView(selectedList.getWidget());

			//Trigger null event for redraw.
			selectedList.onEvent(null);

			Variable linjeObj = al.getVariableInstance("linjeobjekt_diva");

			List<String>objTypes = al.getListElements(linjeObj.getBackingDataSet());
			if (objTypes!=null)
				Log.d("nils","Found objTypes! "+objTypes.toString());

			//Generate buttons.
			Button b;
			for (final String linjeObjLabel:objTypes) {
				b = new Button(this.getActivity());
				LayoutParams params = new LayoutParams();
				params.width = LayoutParams.MATCH_PARENT;
				params.height = LayoutParams.WRAP_CONTENT;
				b.setLayoutParams(params);
				b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
				b.setText(linjeObjLabel);
				b.setOnClickListener(new OnClickListener() {			
					@Override
					public void onClick(View v) {
						LinearLayout numTmp = (LinearLayout)inflater.inflate(R.layout.edit_field_numeric, null);
						meterEd = (EditText)numTmp.findViewById(R.id.edit);
						meterEd.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "200")});
						AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
						alert.setTitle(linjeObjLabel);
						alert.setMessage("Ange metertalet (0-200)");
						alert.setPositiveButton("Spara", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								Editable metS=meterEd.getText();
								if (metS!=null && metS.length()>0) {
									Log.d("nils","Got meters: "+meterEd.getText());
									//Create new linjeobjekt_diva with the meters.
									String meter = (meterEd.getText().toString());
									Variable currentMeter = al.getVariableInstance("Current_Meter");
									if (currentYear==null||currentRuta==null||currentLinje==null||currentMeter==null) {
										o.addRow("");
										o.addRedText("Could not start workflow "+linjeObjLabel+
												"_wf, since no value exist for one of [Current_year, Current_ruta, Current_Linje, Current_Meter]");
									} else {
										currentMeter.setValue(meter);
										//check if the variable exist. If so - no deal.
										Map<String,String> key = Tools.createKeyMap(VariableConfiguration.KEY_YEAR,currentYear,"ruta",currentRuta,"linje",currentLinje,"meter",meter,"value",linjeObjLabel);
										gs.setKeyHash(key);
										Variable v = al.getVariableInstance("linjeobjekt_diva");
										if (v.getValue() != null)
											Log.d("nils","Variable already exists");
										else {
											v.setValue(linjeObjLabel);
											Log.d("nils","Stored "+linjeObjLabel+" under meter "+meter);
											myContext.registerEvent(new WF_Event_OnSave("Template"));											
										}
										//Start workflow here.
										Log.d("nils","Trying to start workflow "+"wf_"+linjeObjLabel);
										Workflow wf = gs.getWorkflow("wf_"+linjeObjLabel);
										if (wf!=null) {
											Fragment f = wf.createFragment();
											if (f == null) {
												o.addRow("");
												o.addRedText("Couldn't create new fragment...Workflow was named"+wf.getName());
											}
											Bundle b = new Bundle();
											b.putString("workflow_name", "wf_"+linjeObjLabel); //Your id
											f.setArguments(b); //Put your id to your next Intent
											//save all changes
											final FragmentTransaction ft = myContext.getActivity().getFragmentManager().beginTransaction(); 
											ft.replace(myContext.getRootContainer(), f);
											ft.addToBackStack(null);
											ft.commit(); 
											Log.d("nils","Should have started "+"wf_"+linjeObjLabel);
										}
									}
								}
							}

						});
						alert.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {

							}
						});	

						Dialog d = alert.setView(numTmp).create();
						d.setCancelable(false);
						d.show();
					}
				});
				fieldList.addView(b);
			}


			//WF_ClickableField_Selection aggNo = new WF_ClickableField_Selection_OnSave("Avslutade Rutor:", "De rutor ni avslutat",
			//		myContext, "AvslRutor",true);
			//aggregatePanel.addView(aggNo.getWidget());
		}
		return v;

	}



	/* (non-Javadoc)
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public void onResume() {
		super.onResume();

	}
	@Override
	public void onPause() {

		super.onPause();
	}






	@Override
	protected List<WF_Container> getContainers() {
		return myLayouts;
	}

	public void execute(String name, String target) {
		if (name.equals("template_function_export"))
			gs.getDb().export("ruta",gs.getArtLista().getVariableInstance("Current_Ruta").getValue());
	}



}