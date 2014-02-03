package com.teraim.nils.dynamic.workflow_abstracts;

import com.teraim.nils.dynamic.workflow_realizations.WF_Filter;

public interface Filterable {		
	public void addFilter(WF_Filter f);
	public void removeFilter(WF_Filter f);
	public void runFilters();
}
