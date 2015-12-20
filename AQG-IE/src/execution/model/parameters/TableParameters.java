package execution.model.parameters;

import java.util.Hashtable;

import exploration.model.enumerations.ExecutionAlternativeEnum;

public class TableParameters implements Parametrizable {

	private Hashtable<ExecutionAlternativeEnum, Parametrizable> table;

	public TableParameters(){
		this.table = new Hashtable<ExecutionAlternativeEnum, Parametrizable>();
	}
	
	@Override
	public Parametrizable loadParameter(ExecutionAlternativeEnum parameter) {
		return table.get(parameter);
	}

	@Override
	public String getString() {
		return "NOT_STRING_AVAILABLE";
	}

	@Override
	public boolean containsParameter(ExecutionAlternativeEnum parameter) {
		
		return this.table.containsKey(parameter);
	
	}
	
	public void addParameter(ExecutionAlternativeEnum parameter, Parametrizable value){
		
		table.put(parameter, value);
	}

}
