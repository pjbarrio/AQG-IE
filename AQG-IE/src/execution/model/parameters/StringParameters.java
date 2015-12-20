package execution.model.parameters;

import exploration.model.enumerations.ExecutionAlternativeEnum;

public class StringParameters implements Parametrizable{

	private ExecutionAlternativeEnum parameter;
	private String value;

	public StringParameters(ExecutionAlternativeEnum parameter, String value) {
		this.parameter = parameter;
		this.value = value;
	}

	@Override
	public Parametrizable loadParameter(ExecutionAlternativeEnum parameter) {
		if (this.parameter.equals(parameter)){
			return this;
		}
		return null;
	}

	@Override
	public String getString() {
		return value;
	}

	@Override
	public boolean containsParameter(ExecutionAlternativeEnum parameter) {
		return this.parameter.equals(parameter);
	}

}
