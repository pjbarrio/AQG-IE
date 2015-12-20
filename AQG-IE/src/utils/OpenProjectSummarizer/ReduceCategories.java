package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;

public class ReduceCategories {

	private static String df;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/";

		df = "/proj/dbNoBackup/pjbarrio/sites/Directory/Top/";
		
		BufferedReader br = new BufferedReader(new FileReader(new File(rootDirectory + "databasesCategories.txt")));
		
		String line = br.readLine();
		
		HashSet<String> things = new HashSet<String>();
		
		while (line != null){
			
			things.add(cleanCategory(line, 3));
			
			line = br.readLine();
			
		}

		System.setOut(new PrintStream(new File(rootDirectory + "databasesCategoriesReduced.txt")));

		for (String string : things) {
			
			System.out.println(string);
			
		}
		
		br.close();
		
	}

	private static String cleanCategory(String line, int levels) {
		
		String web = line.substring(0, line.indexOf(','));
		
		String cat = line.substring(line.indexOf(',') + 1);
		
		String ret = df;
		
		cat = cat.replaceAll(df, "");
		
		for (int i = 0; i < levels && cat.contains("/"); i++) {
			
			String first = cat.substring(0, cat.indexOf('/'));
			
			ret += first + "/";
			
			cat = cat.substring(cat.indexOf('/') + 1);
			
		}
		
		return web + "," + ret;
	}

}
