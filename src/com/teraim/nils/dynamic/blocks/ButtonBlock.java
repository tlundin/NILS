package com.teraim.nils.dynamic.blocks;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout.LayoutParams;
import android.widget.ToggleButton;

import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_realizations.WF_Context;
import com.teraim.nils.dynamic.workflow_realizations.WF_Widget;


/**
 * buttonblock
 * 
 * name is ID for now..
 * 
 * @author Terje
 *
 */
public  class ButtonBlock extends Block {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6454431627090793558L;
	String text,onClick,name,containerId,target;
	Type type;
	
	WF_Context myContext;

	enum Type {
		action,
		toggle
	}

	public ButtonBlock(String lbl,String action, String name,String container,String target, String type) {
		Log.d("NILS","BUTTONBLOCK type Action. Action is set to "+action);
		this.text = lbl;
		this.onClick=action;
		this.name=name;
		this.containerId = container;
		this.target=target;
		this.type=type.equals("toggle")?Type.toggle:Type.action;

	}

	
	public String getText() {
		return text;
	}

	public Action getAction() {
		return new Action();
	}

	public String getName() {
		return name;
	}
	public String getTarget() {
		return target;
	}
	public class Action {
		public final static int VALIDATE = -1;
		public final static int WF_EXECUTE = -2;

		private int type;
		public String wfName=null;
		public Action() {
			if (onClick.equals("validate"))
				type = VALIDATE;
			else if (onClick.equals("Start_Workflow"))
				type = WF_EXECUTE;
			wfName = target;
			Log.d("NILS","Workflowname in ACTION is "+target+" with length "+target.length());
		}
		public boolean isWorkflow() {
			return type==WF_EXECUTE;
		}
	}

	public void create(final WF_Context myContext) {
		o=GlobalState.getInstance(myContext.getContext()).getLogger();
		Container myContainer = myContext.getContainer(containerId);
		final Context ctx = myContext.getContext();
		
		if (type == Type.action) {
			o.addRow("Creating Action Button.");
			Button button = new Button(ctx);
			//button.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.button_bg_selector));
			//button.setTextAppearance(ctx, R.style.WF_Text);
			button.setText(getText());

			LayoutParams params = new LayoutParams();
			params.width = LayoutParams.WRAP_CONTENT;
			params.height = LayoutParams.MATCH_PARENT;
			params.gravity = Gravity.CENTER_HORIZONTAL;
			params.leftMargin = 50;
			params.rightMargin = 50;
			//Not sure about these..
			params.bottomMargin = 10;
			params.topMargin = 10;
			button.setLayoutParams(params);
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Action action = getAction();
					//ACtion = workflow to execute.
					//Commence!

					if (action!=null) {
						//Workflow?
						if (action.isWorkflow()){

							Workflow wf = GlobalState.getInstance(ctx).getWorkflow(action.wfName);
							if (wf == null) {
								Log.e("NILS","Cannot find wf referenced by button "+getName());

							} else {
								o.addRow("");
								o.addRow("Action button pressed. Executing wf: "+action.wfName);
								Fragment f = wf.createFragment();
								Bundle b = new Bundle();
								b.putString("workflow_name", action.wfName); //Your id
								f.setArguments(b); //Put your id to your next Intent
								//save all changes
								final FragmentTransaction ft = myContext.getActivity().getFragmentManager().beginTransaction(); 
								ft.replace(myContext.getRootContainer(), f);
								ft.addToBackStack(null);
								ft.commit(); 
								//Validation?
							}
						} //else
						//validate();
					} else {
						o.addRow("");
						o.addRedText("Action button had no associated action!");
					}
				}

			});
			myContainer.add(new WF_Widget(text,button));
		} else if (type == Type.toggle) {
			o.addRow("Creating Toggle Button with text: "+text);
			ToggleButton toggleB = (ToggleButton)LayoutInflater.from(ctx).inflate(R.layout.toggle_button,null);
			//ToggleButton toggleB = new ToggleButton(ctx);
			toggleB.setTextOn(text);
			toggleB.setTextOff(text);
			toggleB.setChecked(false);
			LayoutParams params = new LayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.WRAP_CONTENT;
			
			toggleB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
			toggleB.setLayoutParams(params);
			
			toggleB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						o.addRow("Togglebutton "+text+" pressed. Executing function "+onClick);
						myContext.getTemplate().execute(onClick);					
				}
			});
			myContainer.add(new WF_Widget(text,toggleB));
		}
	}
}