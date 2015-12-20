package plot.selector.impl;

import java.util.List;

import plot.selector.ExecutionSelector;
import plot.selector.Selector;
import utils.persistence.persistentWriter;

public class ExecutionAlternativeCombinationSelector extends ExecutionSelector {

	private int executionAlternative;
	private persistentWriter pW;
	private List<Integer> executions;
	private String executionAlternativeString;

	public ExecutionAlternativeCombinationSelector(int executionAlternative, String executionAlternativeDescription, persistentWriter pW){
		executions = null;
		this.executionAlternative = executionAlternative;
		this.pW = pW;
		this.executionAlternativeString = executionAlternativeDescription;
		
	}
	
	@Override
	public List<Integer> getExecutions(){
		
		if (executions == null){
			executions = pW.getExecutionsId(executionAlternative);
		}
		
		return executions;
		
	}

	@Override
	public String getName() {
		return executionAlternativeString;
	}

}
