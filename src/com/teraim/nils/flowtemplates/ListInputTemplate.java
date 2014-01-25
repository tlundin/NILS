package com.teraim.nils.flowtemplates;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.LinearLayout;

import com.teraim.nils.CommonVars;
import com.teraim.nils.DataTypes;
import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.CreateListEntriesBlock;
import com.teraim.nils.DataTypes.StartBlock;
import com.teraim.nils.DataTypes.VarToListConfigRow;
import com.teraim.nils.R;


public class ListInputTemplate extends BaseTemplate {
	LinearLayout fieldBg;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.template_list_input_wf);

		fieldBg = (LinearLayout)findViewById(R.id.fieldList);
		if (wf!=null)
			execute();
	}




	/**
	 * Execute the workflow.
	 */
	private void execute() {

		//LinearLayout my_root = (LinearLayout) findViewById(R.id.myRoot);

		List<Block>blocks = wf.getBlocks();
		for (Block b:blocks) {
			if (b instanceof StartBlock)
				Log.d("NILS","Startblock found");
			else if (b instanceof CreateListEntriesBlock) {
				CreateListEntriesBlock bl = (CreateListEntriesBlock)b;
				Log.d("NILS","Scanning createlistentries with filename "+bl.getFileName());
				InputStream is;
				List <VarToListConfigRow>rows=null;
				try {
					is = new FileInputStream(CommonVars.CONFIG_FILES_DIR+bl.getFileName());
					rows = DataTypes.getSingleton().scanListConfigData(is);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (rows!=null) {
					Log.d("nils","Config file had "+rows.size()+" entries");
					for(VarToListConfigRow r:rows) {
						this.addNormalInput(fieldBg, r.getVarName(), r.getVarLabel(), "facke", -1, InputType.TYPE_CLASS_NUMBER);
					}
				}
				else
					Log.d("nils","failed to scan input file");
			}
			
			
		}
		
	}



}