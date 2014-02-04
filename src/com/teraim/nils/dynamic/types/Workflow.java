package com.teraim.nils.dynamic.types;

import java.util.List;

import android.util.Log;

import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.PageDefineBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.templates.DefaultTemplate;
import com.teraim.nils.dynamic.templates.ListInputTemplate;

//Workflow
public class Workflow {
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

	public Class getWfClass() {
		Class ret = null;
		switch (getType()) {
		case plain:
			ret = DefaultTemplate.class;
			break;
		case variable_selection:
			ret = ListInputTemplate.class;
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

}
