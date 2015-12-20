package utils.OpenProjectSummarizer.finalFiles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.id.DatabaseCategoryHandler;
import utils.id.DatabaseIndexHandler;

public class GenerateWebsiteIndexCategory {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		File databaseCategories = new File("/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/databasesCategoriesReduced.txt");
		
		File databaseIndex = new File("/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Forms/FinalSelection/FinalDataIndex.txt");
		
		File output = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/SampleGeneration/FilteredCategories/CategoryIndex.txt");
				
		Map<String, Integer> index = DatabaseIndexHandler.loadDatabaseIndex(databaseIndex);
		
		Map<String, List<String>> catIndex = DatabaseCategoryHandler.loadDatabaseCategoryIngerted(databaseCategories);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		
		for (Entry<String,Integer> entry : index.entrySet()) {
			
			String db = entry.getKey();
			
			List<String> cat = catIndex.get(db);
			
			if (cat!=null){
				
				for (String category : cat) {
					bw.write(db + "," + category);
					bw.newLine();
				}

			}
			else
				System.err.println(entry.getKey());
		}
		
		bw.close();
		
	}

}
