package sample.generation.factory;

import sample.generation.model.cardinality.CardinalityFunction;
import sample.generation.model.cardinality.impl.SameCardinalityFunction;
import sample.generation.model.cardinality.impl.SizeProportionalCardinality;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.CardinalityFunctionEnum;
import exploration.model.enumerations.CardinalityFunctionLimitEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;

public class CardinalityFunctionFactory {

	public static CardinalityFunction generateInstance(String string,
			Parametrizable parameters) {
		
		switch (CardinalityFunctionEnum.valueOf(string)) {
			case SIZE_BASED_CARDINALITY:
				
				return new SizeProportionalCardinality();
	
			case SAME_CARDINALITY:
				
				CardinalityFunctionLimitEnum limit = CardinalityFunctionLimitEnum.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_LIMIT).getString());
				
				return new SameCardinalityFunction(limit);
				
			default:
				break;
		}
			
		return null;
		
	}

}
