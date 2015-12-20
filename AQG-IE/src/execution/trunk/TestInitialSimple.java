package execution.trunk;

import java.net.InetAddress;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Combination;
import exploration.model.Database;

public class TestInitialSimple {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try{
			  String computername= InetAddress.getLocalHost().getHostName();
			  System.out.println(computername);
			  }catch (Exception e){
			  System.out.println("Exception caught ="+e.getMessage());
			  }
		
		System.exit(0);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		pW.initializeSimpleExplorator();
		
		for (Database db : pW.getCrossableDatabases()) {
			System.out.println(db.getId());

		}
		
		
		while (pW.hasMoreCombinations()){
			
			Combination config = pW.nextCombination();

			System.out.println(config.getAlgorithm().getId() + " - " + config.getGeneratorSample().getDatabase().getId());
		}
	}

}
