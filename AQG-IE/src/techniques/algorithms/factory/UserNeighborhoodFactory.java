package techniques.algorithms.factory;

import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import exploration.model.enumerations.UserNeighborhoodEnum;

public class UserNeighborhoodFactory {

	public static UserNeighborhood getInstance(UserNeighborhoodEnum neighborhoodName,
			
			int neighbors, UserSimilarity userSimilarity, DataModel dm) {
		
		switch (neighborhoodName) {
		case NEAREST_N_USER_NEIGHBORHOOD:
			
			return new NearestNUserNeighborhood(neighbors, userSimilarity, dm);

		default:

			return null;
			
		}
		
		
	}

}
