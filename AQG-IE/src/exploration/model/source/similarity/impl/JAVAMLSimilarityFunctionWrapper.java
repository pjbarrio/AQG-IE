package exploration.model.source.similarity.impl;

import java.util.Map;

import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.AbstractSimilarity;

import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.source.similarity.SimilarityFunction;
import external.javaML.JavaMLInstanceCreator;

public class JAVAMLSimilarityFunctionWrapper extends SimilarityFunction {

	private AbstractSimilarity as;

	public JAVAMLSimilarityFunctionWrapper(SimilarityFunctionEnum enumValue, AbstractSimilarity as) {
		super(enumValue);
		this.as = as;
	}

	@Override
	public <T> double calculate(Map<String, T> wf1, Map<String, T> wf2) {
		
		JavaMLInstanceCreator.createInstances(wf1, wf2);
		
		Instance i1 = JavaMLInstanceCreator.getFirstInstance();
		
		Instance i2 = JavaMLInstanceCreator.getSecondInstance();

		return as.measure(i1, i2);
		
	}

}
