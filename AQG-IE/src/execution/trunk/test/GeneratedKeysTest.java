package execution.trunk.test;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class GeneratedKeysTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		pW.prepareES(-10,-15,0,12);
		pW.prepareES(-10,-12,0,12);
		pW.prepareES(-10,-15,0,13);
		pW.prepareES(-10,-16,0,13);
		pW.prepareES(-10,-12,0,12);
		pW.prepareES(-10,-17,0,13);
		pW.batch();
		

	}

}
