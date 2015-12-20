package techniques.algorithms.factory;

import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import exploration.model.enumerations.UserSimilarityEnum;

public class UserSimilarityFactory {

	public static UserSimilarity getInstance(UserSimilarityEnum userSimilarity,
			DataModel dm) {
		
		switch (userSimilarity) {
		case TANIMOTO_COEFFICIENT_SIMILARITY:
			return new TanimotoCoefficientSimilarity(dm);
			
		default:
			return null;
		}
		
	}

}
