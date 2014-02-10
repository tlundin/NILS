package com.teraim.nils.dynamic.types;

import java.io.Serializable;
import java.util.List;

import android.app.Fragment;
import android.util.Log;

import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.PageDefineBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.templates.DefaultTemplate;
import com.teraim.nils.dynamic.templates.ListInputTemplate;
import com.teraim.nils.dynamic.types.Workflow.Type;

//Workflow
public class Workflow implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8806673639097744371L;
	private List<Block> blocks;
	private String name=null;

	public enum Type
	{
		plain,
		variable_selection
	}
	public enum Unit {
		percentage,
		dm,
		nd

	};

	public List<Block> getBlocks() {
		return blocks;
	}
	public void addBlocks(List<Block> _blocks) {
		blocks = _blocks;
	}

	public String getName() {
		if (name==null) {
			if (blocks!=null && blocks.size()>0)
				name = ((StartBlock)blocks.get(0)).getName();

		}
		return name;
	}

	public Fragment createFragment(){
		Fragment ret = null;
		switch (getType()) {
		case plain:
			ret = new DefaultTemplate();
			break;
		case variable_selection:
			ret = new ListInputTemplate();
			break;

		}
		return ret;
	}

	private Type getType() {
		for (Block b:blocks) {
			if (b instanceof PageDefineBlock) {
				PageDefineBlock bl = (PageDefineBlock)b;
				final String type = bl.getPageType();
				if (type.equals("plain"))
					return Type.plain;
				else 
					if (type.equals("variable_selection"))
						return Type.variable_selection;
					else {
						Log.e("NILS","Type of page not recognized in workflow "+this.getName()+" Will default to plain");
					}
				return Type.plain;	
			}

		}
		Log.e("NILS","Could not find PageDefineBlock for workflow "+this.getName()+" Will default to plain type");
		return Type.plain;
	}
	public Type getTemplateType() {
		return getType();
	}


}
