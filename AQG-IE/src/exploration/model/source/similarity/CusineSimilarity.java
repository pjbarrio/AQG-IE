package exploration.model.source.similarity;

import java.util.Map;

import exploration.model.enumerations.SimilarityFunctionEnum;

public class CusineSimilarity extends SimilarityFunction {

	public CusineSimilarity(SimilarityFunctionEnum enumValue) {
		super(enumValue);		
	}

	@Override
	public <T> double calculate(Map<String, T> wf1, Map<String, T> wf2) {
		// TODO Auto-generated method stub
		return 0;
	}

}
