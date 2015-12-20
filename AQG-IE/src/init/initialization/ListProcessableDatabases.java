package init.initialization;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import utils.id.DatabaseIndexHandler;

public class ListProcessableDatabases {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String index = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Forms/FinalSelection/FinalDataIndex.txt";
		
		File toProcess = new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/randomTraining");
		
		Map<Integer, String> indexTable = DatabaseIndexHandler.loadInvertedDatabaseIndex(new File(index));
		
		List<String> files = FileUtils.readLines(toProcess);
		
//		String website = "http://www.languagehat.com/";
		
		String prefix = "String website = \"";
		
		String suffix = "\";";
		
		int pos = 1;
		
		for (String id : files) {
			
			System.out.println("/*Pos: " + pos + ", Id: " + id + "*/ " +  prefix + indexTable.get(Integer.valueOf(id)) + suffix);
			
			pos++;
		}

	}

}
