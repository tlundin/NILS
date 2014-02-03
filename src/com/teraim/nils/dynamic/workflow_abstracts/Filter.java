package com.teraim.nils.dynamic.workflow_abstracts;

import java.util.List;

public interface Filter {
	public List<? extends Listable> filter(List<? extends Listable> list);
}
