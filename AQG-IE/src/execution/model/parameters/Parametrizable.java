package execution.model.parameters;

import exploration.model.enumerations.ExecutionAlternativeEnum;

public interface Parametrizable {

	public Parametrizable loadParameter(ExecutionAlternativeEnum parameter);
	
	public String getString();

	public boolean containsParameter(ExecutionAlternativeEnum parameter);

}
