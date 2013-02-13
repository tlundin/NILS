package com.teraim.nils;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.ButtonBlock;
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
		String id = b.getString("workflow_id");
		if (id!=null) 
			wf = CommonVars.cv().getWorkflow(id);

		if (wf==null||id==null) {
			Log.e("NILS","Workflow "+id+" NOT found! ID: "+id+" WF: "+wf);
			return;
		} else {
			Log.d("NILS","Now executing workflow "+wf.name);
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


		}

	}

}
