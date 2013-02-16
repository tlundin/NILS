package com.teraim.nils;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.ButtonBlock;
import com.teraim.nils.DataTypes.CreateFieldBlock;
import com.teraim.nils.DataTypes.LayoutBlock;
import com.teraim.nils.DataTypes.StartBlock;
import com.teraim.nils.DataTypes.Workflow;

/**
 * 
 * @author Terje
 * Activity that runs a workflow that has a user interface.
 * Pressing Back button will return flow to parent workflow.
 */

public class FlowEngineActivity extends Activity {


	Workflow wf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Find out the name of the workflow to execute.
		Bundle b = getIntent().getExtras();
		String name = b.getString("workflow_name");
		if (name!=null) 
			wf = CommonVars.cv().getWorkflow(name);

		if (wf==null||name==null) {
			Log.e("NILS","Workflow "+name+" NOT found!");
			return;
		} else {
			Log.d("NILS","Now executing workflow "+name);
			setContentView(R.layout.wf_default);
			execute();
		}


	}

	/**
	 * Execute the workflow.
	 */
	private void execute() {
		LinearLayout my_root = (LinearLayout) findViewById(R.id.myRoot);
		List<Block>blocks = wf.getBlocks();
		for (Block b:blocks) {
			if (b instanceof StartBlock)
				Log.d("NILS","Startblock found");

			else if (b instanceof LayoutBlock) {
				Log.d("NILS","Layoutblock found");
				LayoutBlock bl = (LayoutBlock) b;
				if (bl.getLayoutDirection().equals("horizontal")) {
					LinearLayout l = new LinearLayout(this);
					l.setOrientation(LinearLayout.HORIZONTAL);
					my_root.addView(l);
					my_root = l;
				}

			}
			else if (b instanceof ButtonBlock) {
				Log.d("NILS","buttonblock found");
				ButtonBlock bl = (ButtonBlock)b;
				Button bu = new Button(this);
				Log.d("NILS","Text is "+bl.getText());
				bu.setText(bl.getText());
				my_root.addView(bu);
			}
			else if (b instanceof CreateFieldBlock) {
				Log.d("NILS","Createfield block found");
				CreateFieldBlock bl = (CreateFieldBlock)b;
				Log.d("NILS","Variable is "+bl.getVariableReference());
				Variable var = CommonVars.cv().getVariable(bl.getVariableReference());
				//Create a numeric input field.
				if (var.getType().equals(Variable.NUMERIC)) {
					EditText et = new EditText(this);
					et.setInputType(InputType.TYPE_CLASS_NUMBER);
					my_root.addView(et);
				}
					
			}
			else if (b instanceof CreateFieldBlock) {
				Log.d("NILS","Createfield block found");
				CreateFieldBlock bl = (CreateFieldBlock)b;
				Log.d("NILS","Variable is "+bl.getVariableReference());
			}


		}

	}

}
