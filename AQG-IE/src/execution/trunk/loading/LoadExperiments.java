package execution.trunk.loading;

import java.util.List;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Database;

public class LoadExperiments {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();

//		pW.cleanExperiments();

		
		int first = 1;
		
		int second = 42;
		
		
		List<Database> databases = pW.getSamplableDatabases(null);
		
		for (int i = first; i <= second; i++) {
			
			for (int j = 0; j < databases.size(); j++) {

				pW.insertExperimentOnDatabase(databases.get(j).getId(),i);
				
			}
			
		}
		

	}

}
