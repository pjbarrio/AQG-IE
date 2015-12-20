package techniques.algorithms.factory;

import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import exploration.model.enumerations.RecommenderEnum;

public class RecommenderFactory {

	public static Recommender getRecommender(RecommenderEnum recommenderName,
			DataModel dm, UserNeighborhood neighborhood,
			UserSimilarity userSimilarity) {
		
		switch (recommenderName) {
		case GENERIC_USER_BASED_RECOMMENDER:

			return new GenericUserBasedRecommender(dm, neighborhood, userSimilarity);

		default:
			return null;
		}
		
	}

}
