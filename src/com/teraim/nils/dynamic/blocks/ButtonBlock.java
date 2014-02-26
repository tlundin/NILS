package com.teraim.nils.dynamic.blocks;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
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
import com.teraim.nils.dynamic.workflow_abstracts.Drawable;
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
	private boolean isVisible;

	enum Type {
		action,
		toggle
	}

	public ButtonBlock(String lbl,String action, String name,String container,String target, String type, boolean isVisible) {
		Log.d("NILS","BUTTONBLOCK type Action. Action is set to "+action);
		this.text = lbl;
		this.onClick=action;
		this.name=name;
		this.containerId = container;
		this.target=target;
		this.type=type.equals("toggle")?Type.toggle:Type.action;
		this.isVisible = isVisible;

	}


	public String getText() {
		return text;
	}


	public String getName() {
		return name;
	}
	public String getTarget() {
		return target;
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
			Log.d("nils","BUTTON TEXT:"+getText());
			button.setText(getText());

			LayoutParams params = new LayoutParams();
			params.width = LayoutParams.MATCH_PARENT;
			params.height = LayoutParams.WRAP_CONTENT;

			button.setLayoutParams(params);
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {

					//ACtion = workflow to execute.
					//Commence!

					if (onClick.startsWith("template"))
						myContext.getTemplate().execute(onClick,target);
					else if (onClick.equals("validate"))
						;//TODO
					else if (onClick.equals("Start_Workflow")) {

						Workflow wf = GlobalState.getInstance(ctx).getWorkflow(target);
						if (wf == null) {
							Log.e("NILS","Cannot find wf referenced by button "+getName());

						} else {
							o.addRow("");
							o.addRow("Action button pressed. Executing wf: "+target);
							Fragment f = wf.createFragment();
							if (f == null) {
								o.addRow("");
								o.addRedText("Couldn't create new fragment...Template was named"+wf.getName());
							}
							Bundle b = new Bundle();
							b.putString("workflow_name", target); //Your id
							f.setArguments(b); //Put your id to your next Intent
							//save all changes
							final FragmentTransaction ft = myContext.getActivity().getFragmentManager().beginTransaction(); 
							ft.replace(myContext.getRootContainer(), f);
							ft.addToBackStack(null);
							ft.commit(); 
							//Validation?
						}

					} else {
						o.addRow("");
						o.addRedText("Action button had no associated action!");
					}

				}

			});
			myContainer.add(new WF_Widget(text,button,isVisible,myContext));
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
					if(onClick==null||onClick.trim().length()==0) {
						o.addRow("");
						o.addRedText("Button "+text+" has no onClick action!");
						Log.e("nils","Button clicked ("+text+") but found no action");
					} else {

						o.addRow("Togglebutton "+text+" pressed. Executing function "+onClick);
						if (onClick.startsWith("template")) 
							myContext.getTemplate().execute(onClick,target);	
						else if (onClick.equals("toggle_visible")) {
							Log.d("nils","Executing toggle");
							Drawable d = myContext.getDrawable(target);
							if (d!=null) {
								if(d.isVisible())
									d.hide();
								else
									d.show();
							} else {
								Log.e("nils","Couldn't find target "+target+" for button");
								o.addRow("");
								o.addRedText("Target for button missing: "+target);
							}

						}
					}
				}
			});
			myContainer.add(new WF_Widget(text,toggleB,isVisible,myContext));
		}
	}
}