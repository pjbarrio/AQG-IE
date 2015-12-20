package plot.selector;

import java.util.List;

public abstract class ExecutionSelector implements Selector {

	@Override
	public List<Integer> getSelected() {
		return getExecutions();
	}

	@Override
	public abstract String getName();
	
	public abstract List<Integer> getExecutions();

}
