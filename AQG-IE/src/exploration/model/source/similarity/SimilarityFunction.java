package exploration.model.source.similarity;

import java.util.Map;

import net.sf.javaml.distance.CosineSimilarity;

import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.source.similarity.impl.JAVAMLSimilarityFunctionWrapper;

public abstract class SimilarityFunction {

	private SimilarityFunctionEnum enumValue;

	public SimilarityFunction(SimilarityFunctionEnum enumValue) {
		this.enumValue = enumValue;
	}

	public static SimilarityFunction generateInstance(String string) {
		
		switch (SimilarityFunctionEnum.valueOf(string)) {
		case COSINE_SIMILARITY:
			
			return new JAVAMLSimilarityFunctionWrapper(SimilarityFunctionEnum.COSINE_SIMILARITY,new CosineSimilarity());

		case COS_SIMILARITY_MAIN:
			
			return new JAVAMLSimilarityFunctionWrapper(SimilarityFunctionEnum.COS_SIMILARITY_MAIN,new CosineSimilarity());	
		
		case COS_SIMILARITY_RES:
			
			return new JAVAMLSimilarityFunctionWrapper(SimilarityFunctionEnum.COS_SIMILARITY_RES,new CosineSimilarity());
		
		case COS_SIMILARITY_DEEP_RES:
			
			return new JAVAMLSimilarityFunctionWrapper(SimilarityFunctionEnum.COS_SIMILARITY_DEEP_RES,new CosineSimilarity());	
			
		default:
			
			return null;
			
		}
		
	}

	public SimilarityFunctionEnum getEnum() {
		return enumValue;
	}

	public abstract <T> double calculate(Map<String, T> wf1, Map<String, T> wf2);

}
