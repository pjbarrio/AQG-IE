package execution.trunk.test;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class BatchTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		pW.prepareES(-10,-15,0,5);
		pW.prepareES(-10,-12,0,5);
		pW.prepareES(-10,-15,0,5);
		pW.prepareES(-10,-16,0,5);
		pW.prepareES(-10,-12,0,5);
		
		pW.batch();
		
	}

}
