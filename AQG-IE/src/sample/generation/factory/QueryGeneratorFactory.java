package sample.generation.factory;

import techniques.baseline.Ripper.queryManagement.RipperRuleQueryGenerator;
import utils.persistence.persistentWriter;
import execution.model.parameters.Parametrizable;
import execution.workload.querygeneration.QueryGenerator;
import execution.workload.querygeneration.TextQueryGenerator;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.QueryGeneratorEnum;

public class QueryGeneratorFactory {

	public static QueryGenerator generateInstance(String string,
			Parametrizable parameter, persistentWriter pW, String relation) {
		
		switch (QueryGeneratorEnum.valueOf(string)){
		
		case TUPLE_QUERY_GENERATOR:
			return new TupleQueryGenerator(InferredTypeFactory.getInferredTypes(relation,pW),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.UNIQUE).getString()).booleanValue(),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.LOWERCASE).getString()).booleanValue(),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.STEMMED).getString()).booleanValue(),OmittedAttributeFactory.generateList(pW,relation,parameter.loadParameter(ExecutionAlternativeEnum.OMMITED_ATTRIBUTES).getString()));
		case TEXT_QUERY_GENERATOR:
			return new TextQueryGenerator();
		case RIPPER_QUERY_GENERATOR:
			return new RipperRuleQueryGenerator();
		default:
			return null;
		}
				
	}

}
