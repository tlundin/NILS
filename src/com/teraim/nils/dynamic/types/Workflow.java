package com.teraim.nils.dynamic.types;

import java.io.Serializable;
import java.util.List;

import android.app.Fragment;
import android.util.Log;

import com.teraim.nils.dynamic.blocks.Block;
import com.teraim.nils.dynamic.blocks.PageDefineBlock;
import com.teraim.nils.dynamic.blocks.StartBlock;
import com.teraim.nils.dynamic.templates.DefaultTemplate;
import com.teraim.nils.dynamic.templates.FixPunktTemplate;
import com.teraim.nils.dynamic.templates.ListInputTemplate;

//Workflow
public class Workflow implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8806673639097744371L;
	private List<Block> blocks;
	private String name=null;


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

	
	public Fragment createFragment() {
		Fragment f = null;
		try {
			Class<?> cs = Class.forName("com.teraim.nils.dynamic.templates."+getType());
			f = (Fragment)cs.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e2) {
			e2.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		}
	return f;
}

public String getType() {
	for (Block b:blocks) {
		if (b instanceof PageDefineBlock) {
			PageDefineBlock bl = (PageDefineBlock)b;
			return bl.getPageType();
		}
	}
	Log.e("NILS","Could not find PageDefineBlock for workflow "+this.getName()+" Will default to Default type");
	return "DefaultTemplate";
}



}
