package searcher.interaction.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ResultsWithErrorsFinder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String original = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/FinalForms/";
		
		String results = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/ResultsPage/";
		
		File[] originalFiles = new File(original).listFiles();
		
		File[] resultsFiles = new File(results).listFiles();		

		Set<String> resultNames = new HashSet<String>();
		
		for (File file : resultsFiles) {
			
			resultNames.add(file.getName());
			
		}
		
		for (int i = 0; i < originalFiles.length; i++) {
			
			if (!resultNames.contains(originalFiles[i].getName())){
				System.out.println(originalFiles[i].getName());
			}
			
		}
		
	}

}
