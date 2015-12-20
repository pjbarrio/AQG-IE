package init.initialization;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import utils.id.DatabaseIndexHandler;
import utils.persistence.databaseWriter;

public class ExtractionTableCreator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String index = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt";
		
		File folder = new File("/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/BootstrapTest/");
		
		Map<String, Integer> indexTable = DatabaseIndexHandler.loadDatabaseIndex(new File(index));
		
		for (Integer values : indexTable.values()) {
			
			File f = new File(folder,values + ".test.table");
			
			if (!f.exists())
				f.createNewFile();
			
		}
		
	}

}
