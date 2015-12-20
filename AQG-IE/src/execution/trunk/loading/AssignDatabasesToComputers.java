package execution.trunk.loading;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class AssignDatabasesToComputers {

	public static void main(String[] args) throws IOException {
		
		List<String> workingDatabases = FileUtils.readLines(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/workingDatabase.list"));
		
		List<String> workingComputers = FileUtils.readLines(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/workingComputer.list"));
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		pW.cleanHostDatabases();
		
		Collections.shuffle(workingDatabases);
		
		for (int i = 0; i < workingDatabases.size(); i++) {
			
			pW.writeHostDatabase(Integer.valueOf(workingDatabases.get(i)),workingComputers.get(i % workingComputers.size()));
			
		}
		
	}
	
}
