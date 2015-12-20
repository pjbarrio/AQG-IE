package collection.processing.indetifying;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import extraction.com.clearforest.OpenCalaisRelationExtractor;


public class DocumentsRelationDetection {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		
		String f1 = "/proj/db/NoBackup/pjbarrio/OCOutput/CBSNews.table";

		String output = "/proj/db/NoBackup/pjbarrio/Experiments/workload/NaturalDisasterCBSNews.table";
		
		String Relation = "NaturalDisaster";

		BufferedReader bf = new BufferedReader(new FileReader(new File(f1)));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		
		String line = bf.readLine();
		
		int i = 0;
		
		while (line != null){
			
			String file = getFile(line);

			if (containsRelation(file,Relation)){
			
				bw.write(line + "\n");
				
				System.out.println(i++);
				
			}
			
			line = bf.readLine();
		}

		bf.close();
		
		bw.close();
		
	}

	private static boolean containsRelation(String file, String relation) throws IOException {
		

		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(new File(file)));
		} catch (FileNotFoundException e) {
//			System.out.println("No File: " + file);
			return false;
		}
		
		String lll = "";
		
		String line = bf.readLine();
		
		int i = 0;
		
		while (line != null && i < 10){
			
			lll = lll + line;
			
			i++;
			
		}
		
		if (lll.equals("")){
			bf.close();
			return false;
		}
		
		bf.close();
		
		return lll.contains(relation);
		
	}

	private static String getFile(String line) {
		
		String[] pair = null;//OpenCalaisRelationExtractor.processLine(line);
		
		return pair[1];

	}

}
