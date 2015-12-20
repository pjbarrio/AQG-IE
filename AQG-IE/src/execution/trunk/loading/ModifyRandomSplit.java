package execution.trunk.loading;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class ModifyRandomSplit {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		List<String> dbs = FileUtils.readLines(new File("/home/pjbarrio/dbs.txt"));

		List<String> db10 = FileUtils.readLines(new File("/home/pjbarrio/db-10.txt"));
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		db10.removeAll(dbs);
		
		for(int i = 0; i < db10.size() ; i++){
			pW.updateExperimentSplit(-5,Integer.valueOf(db10.get(i)));
		}
		
	}

}
