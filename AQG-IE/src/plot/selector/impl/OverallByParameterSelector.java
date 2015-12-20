package plot.selector.impl;

import java.util.Hashtable;
import java.util.List;

import plot.data.Series;
import plot.selector.ExecutionSelector;
import plot.selector.Selector;


public class OverallByParameterSelector implements Selector {

	private Hashtable<Double, Selector> parametersTable;
	private String name;

	public OverallByParameterSelector(String name){
		this.name = name;
		parametersTable = new Hashtable<Double, Selector>();
	}

	@Override
	public String getName(){
		return name;
	}

	public void addEntry(double parameter, Selector executionSelector) {
		
		parametersTable.put(parameter, executionSelector);
		
	}

	public Selector getExecutionSelector(double parameter) {

		return parametersTable.get(parameter);

	}

	@Override
	public List<Integer> getSelected() {
		return null;
	}



}
