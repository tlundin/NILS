package com.teraim.nils.dynamic.blocks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.TableLayout.LayoutParams;

import com.teraim.nils.Constants;
import com.teraim.nils.GlobalState;
import com.teraim.nils.R;
import com.teraim.nils.dynamic.types.Workflow;
import com.teraim.nils.dynamic.workflow_abstracts.Container;
import com.teraim.nils.dynamic.workflow_abstracts.Filterable;
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
		Container myContainer = myContext.getContainer(containerId);
		if (myContainer == null) 
			return;
		final Context ctx = myContext.getContext();
		if (type == Type.action) {
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
			button.setTextSize(Constants.TEXT_LARGE);

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

								Intent intent = new Intent(ctx,wf.getWfClass());
								Bundle b = new Bundle();
								b.putString("workflow_name", action.wfName); //Your id
								intent.putExtras(b); //Put your id to your next Intent
								//save all changes

								ctx.startActivity(intent);
								//Validation?
							}
						} //else
						//validate();
					} else
						Log.e("NILS","Action was null for "+getName());
				}

			});
			myContainer.add(new WF_Widget(button));
		} else if (type == Type.toggle) {
			ToggleButton toggleB = new ToggleButton(ctx);
			toggleB.setTextOn(text);
			toggleB.setTextOff(text);
			toggleB.setChecked(false);
			LayoutParams params = new LayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.WRAP_CONTENT;
			toggleB.setTextSize(Constants.TEXT_LARGE);
			toggleB.setLayoutParams(params);
			
			toggleB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						myContext.getTemplate().execute(onClick);					
				}
			});


			myContainer.add(new WF_Widget(toggleB));
		}
	}
}