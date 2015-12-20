package exploration.model.source.similarity;

import java.util.Map;

import net.sf.javaml.core.Instance;

import exploration.model.enumerations.SimilarityFunctionEnum;
import external.javaML.JavaMLInstanceCreator;

public class CosineSimilarity extends SimilarityFunction {

	private CosineSimilarity measure;

	public CosineSimilarity(SimilarityFunctionEnum enumValue) {
		
		super(enumValue);
		
	}

	@Override
	public <T> double calculate(Map<String, T> wf1, Map<String, T> wf2) {
		
		
		return 0;
		
		
	}

}
