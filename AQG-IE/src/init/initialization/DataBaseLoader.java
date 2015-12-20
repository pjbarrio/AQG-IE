package init.initialization;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import utils.id.DatabaseIndexHandler;
import utils.persistence.databaseWriter;

public class DataBaseLoader {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String index = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt";
		
		//1024,278,410,1911,1364,438
		
		databaseWriter dW = new databaseWriter("");
		
		Map<String, Integer> indexTable = DatabaseIndexHandler.loadDatabaseIndex(new File(index));
		
		for (Entry<String,Integer> entry : indexTable.entrySet()) {
			
			if (entry.getValue() == 1012){
			
				dW.insertDatabase(entry.getValue().intValue(),entry.getKey(),0,getClass(entry.getKey()),"boostrappedSample",1,1,"",0,0,0);
			}
		}
		
	}

	private static String getClass(String website) {
		
		return "General";
		
	}

}
