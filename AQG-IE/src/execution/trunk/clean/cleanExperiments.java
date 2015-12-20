package execution.trunk.clean;

import exploration.model.enumerations.ExperimentStatusEnum;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class cleanExperiments {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int[] experimentsArray = createArray(args);
		
		//Clean experiments to be rerunned.
		
		for (int i = 0; i < experimentsArray.length; i++) {
			
			//Clean to allow restart querying 
			
//			pW.cleanExperimentStatusInStatus(experimentsArray[0],ExperimentStatusEnum.ERROR); //do not remove the broken.
		
			//Clean to allow restart querying if I know they are all done.

			pW.cleanExperimentStatusNotInStatus(experimentsArray[i],ExperimentStatusEnum.FINISHED); //do not remove the broken.
			
		}

		
		
	}

	private static int[] createArray(String[] args) {
		
		int[] ret = new int[args.length];
		
		for (int i = 0; i < args.length; i++) {
			
			ret[i] = Integer.valueOf(args[i]);
			
		}
		
		return ret;
		
	}

}
