package utils.execution.listGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.comparator.SizeFileComparator;

public class DatabaseSortBySize {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String type = "smalltraining"; //"training", "test"
		
		File database = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/CleanQueryResult/"+type+"/");
		
		File outputFolder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/CleanSizeSortedList/"+type+"/");
		
		File[] dbs = database.listFiles();
		
		for (int i = 0; i < dbs.length; i++) {
			
			System.out.println("DATABASE: " + i + " out of: " + dbs.length);
			
			String name = dbs[i].getName();
			
			File[] files = dbs[i].listFiles();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFolder,name + ".list"))); 
			
			Arrays.sort(files, SizeFileComparator.SIZE_REVERSE);
			
			for (int j = 0; j < files.length; j++) {
				
				bw.write(files[j].getName() + "\n");
				
			}
			
			bw.close();
			
		}
		

	}

}
