package plot.selector.impl;

import java.util.List;

import plot.selector.ExecutionSelector;
import plot.selector.Selector;

public class FixedSelection extends ExecutionSelector {

	private List<Integer> list;

	public FixedSelection(List<Integer> list) {
		this.list = list;
	}

	@Override
	public List<Integer> getExecutions() {
		return list;
	}

	@Override
	public String getName() {
		return list.get(0).toString();
	}

}
