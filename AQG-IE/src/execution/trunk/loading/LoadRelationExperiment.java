package execution.trunk.loading;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class LoadRelationExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int[] relConfs = new int[]{1,2,3,4,5,6};

		int[] infExtId = new int[]{6,7,8,9,16};
		
		for (int i = 0; i < relConfs.length; i++) {
			
			for (int j = 0; j < infExtId.length; j++) {
				
				pW.insertRelationExperiment(relConfs[i],infExtId[j]);
				
			}
			
		}
		
	}

}
