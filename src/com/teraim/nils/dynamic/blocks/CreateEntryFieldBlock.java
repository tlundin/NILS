package com.teraim.nils.dynamic.blocks;

import com.teraim.nils.dynamic.types.Workflow.Unit;

public class CreateEntryFieldBlock extends Block {

	String name,type,label,purpose,containerId;
	Unit unit;
	
	public CreateEntryFieldBlock(String name, String type, String label,
			String purpose, Unit unit,String containerId) {
		super();
		this.name = name;
		this.type = type;
		this.label = label;
		this.purpose = purpose;
		this.unit = unit;
		this.containerId=containerId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}
	
	
	public void create() {
		
		
		
	}
	
}
