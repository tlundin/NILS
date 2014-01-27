package com.teraim.nils.flowtemplates;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.teraim.nils.CommonVars;
import com.teraim.nils.CommonVars.PersistenceHelper;
import com.teraim.nils.DataTypes;
import com.teraim.nils.DataTypes.Block;
import com.teraim.nils.DataTypes.CreateListEntriesBlock;
import com.teraim.nils.DataTypes.Delyta;
import com.teraim.nils.DataTypes.StartBlock;
import com.teraim.nils.DataTypes.Unit;
import com.teraim.nils.DataTypes.VarToListConfigRow;
import com.teraim.nils.R;
import com.teraim.nils.StoredVariable;
import com.teraim.nils.Variable;


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
		//TEST CODE
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
					int index = 0;
					ClickableField listRow=null;
					for(VarToListConfigRow r:rows) {
						if (r==null) {
							Log.e("nils","found null value in config file row "+index);
						} else {
							if (r.getAction().equals("create")) {
								listRow = new ClickableField(r.getEntryLabel());
								fieldBg.addView(listRow.getView());	
							} 
							if (!r.getAction().equals("add")&&!r.getAction().equals("create"))
								Log.e("nils","something is wrong...action is neither Create or Add: "+r.getAction());
							else {
								Log.d("nils","add...");
								if (listRow!=null) {
									Log.d("nils","var added "+r.getVarLabel());
									listRow.addVariable(r.getVarLabel(), r.getVarName(), r.getUnit(), r.getnumType(),r.getVarType(), r.isDisplayInList());
								}
							}
						}
						index++;
					}
				}
				else
					Log.d("nils","failed to scan input file");
			}


		}

	}





}