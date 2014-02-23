package com.teraim.nils.dynamic.workflow_realizations;

import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Variable;

public class WF_ClickableField_Selection extends WF_ClickableField {

	public WF_ClickableField_Selection(String headerT, String descriptionT,
			WF_Context context, String id,boolean isVisible) {
		super(headerT,descriptionT, context, id,
				LayoutInflater.from(context.getContext()).inflate(R.layout.selection_field_normal,null,isVisible));
	}

	@Override
	public LinearLayout getFieldLayout() {
		//LayoutInflater.from(context.getContext()).inflate(R.layout.clickable_field_normal,null)
		//return 	(LinearLayout)LayoutInflater.from(ctx).inflate(R.layout.output_field,null);
		//o.setText(varId.getLabel()+": "+value);	
		//u.setText(" ("+varId.getPrintedUnit()+")");

		return (LinearLayout)LayoutInflater.from(myContext.getContext()).inflate(R.layout.output_field_selection_element,null);
	}

	@Override
	public String getFormattedText(Variable varId, String value) {
		return value;
	}


	
	

}
