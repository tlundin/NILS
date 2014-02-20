package com.teraim.nils.dynamic.workflow_realizations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;

import com.teraim.nils.GlobalState;
import com.teraim.nils.dynamic.VariableConfiguration;
import com.teraim.nils.dynamic.workflow_realizations.WF_Column_Name_Filter.FilterType;


public class WF_SorterWidget extends WF_Widget {
	
	private final String[] alfabet = {
			"*","ABC","DEF","GHI","JKL",
			"MN","OPQ","RS","T","UV",
			"WXYZ","Å","Ä","Ö"};
	
	final static String Col_Familj = "Familj";

	
	WF_Filter existing;
	WF_List targetList;
	
	public WF_SorterWidget(WF_Context ctx, String type, final WF_List targetList) {
		super("SorterWidget",new LinearLayout(ctx.getContext()));
		LinearLayout buttonPanel;
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		buttonPanel = (LinearLayout) getWidget();
		buttonPanel.setOrientation(LinearLayout.VERTICAL);
		buttonPanel.setLayoutParams(lp);

		this.targetList=targetList;
		
		if (type.equals("alphanumeric_sorting_function")) {
			final OnClickListener cl = new OnClickListener(){
				@Override
				public void onClick(View v) {
					String ch = ((Button)v).getText().toString();
					Log.d("Strand","User pressed "+ch);
					//This shall apply a new Alpha filter on target.
					//First, remove any existing alpha filter.
					targetList.removeFilter(existing);
					
					//Wildcard? Do not add any filter.
					if(!ch.equals("*")) {							
						//Use ch string as unique id.
						existing = new WF_Column_Name_Filter(ch,ch,VariableConfiguration.Col_Entry_Label,FilterType.prefix);
						targetList.addFilter(existing);
					}
					//running the filters will trigger redraw.
					targetList.draw();
				}
			};
			Button b;
			for (String c:alfabet) {
				b = new Button(ctx.getContext());
				b.setLayoutParams(lp);
				b.setText(c);
				b.setOnClickListener(cl);
				buttonPanel.addView(b);
			}
			
		} else if (type.equals("familje_sorting_function")) {
			final OnClickListener dl = new OnClickListener() {
				@Override
				public void onClick(View v) {
					String ch = ((Button)v).getText().toString();
					Log.d("Strand","User pressed "+ch);
					//This shall apply a new Alpha filter on target.
					//First, remove any existing alpha filter.
					targetList.removeFilter(existing);
						existing = new WF_Column_Name_Filter(ch,ch,Col_Familj,FilterType.exact);
						//existing = new WF_Column_Name_Filter(ch,ch,Col_Art)
					targetList.addFilter(existing);
					
					//running the filters will trigger redraw.
					targetList.draw();
				}
			};
			//Generate buttons from artlista. 
			//Pick fields that are of type Familj
			VariableConfiguration al = GlobalState.getInstance(ctx.getContext()).getArtLista();
			List<String> familjer = al.getTable().getColumn(Col_Familj);
			Set<String> ren = new HashSet<String>();
			for (String f:familjer) {
				if (f==null||f.length()==0)
					continue;
				if (ren.contains(f))
					continue;
				else
					ren.add(f);
			}
			Button b;
			for (String f:ren) {
				b = new Button(ctx.getContext());
				b.setLayoutParams(lp);
				b.setText(f);
				b.setOnClickListener(dl);
				buttonPanel.addView(b);
			}	
			
		}
		else 
			Log.e("parser","Sorry, unknown filtering type");
			
			
	}
	
	public void removeExistingFilter() {
		if (existing!=null)
			targetList.removeFilter(existing);
		existing = null;
		targetList.draw();
	}

}
