package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public class RandomSiteSelector {

	private static Hashtable<String, ArrayList<String>> table;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String categories = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/SelectedCategories.txt";
		
		String databaseCategories = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/databasesCategoriesReduced.txt";

		int sitesbycategory = 8;
		
		loadDatabaseCategories(new File(databaseCategories));
		
		BufferedReader br = new BufferedReader(new FileReader(new File(categories)));
		
		String category = br.readLine();
		
		System.setOut(new PrintStream(new File("/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/randomWebSelector-2.txt")));
				
		while (category != null){
			
			System.out.println("CATEGORY: " + category);
			
			ArrayList<String> tab = table.get(category + "/");
			
			for (int i = 0; i < sitesbycategory; i++) {
				
				int index = (int) Math.floor((Math.random()*tab.size()));
				
				String website = tab.remove(index);
				
				System.out.println(website);
				
			}
			
			
			category = br.readLine();
		}
		
		br.close();
		
	}

	private static void loadDatabaseCategories(File file) throws IOException {
		
		table = new Hashtable<String, ArrayList<String>>();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line = br.readLine();
		
		while (line!=null){
			
			String[] spl = line.split(",/proj/dbNoBackup/pjbarrio/sites/Directory/Top/");
			
			String website = spl[0];
			
			String category = "/proj/dbNoBackup/pjbarrio/sites/Directory/Top/" + spl[1];
			
			ArrayList<String> rr = table.get(category);
			
			if (rr == null){
				
				rr = new ArrayList<String>();
				
				table.put(category,rr);
				
			}
			
			rr.add(website);
			
			line = br.readLine();
		
		}
		
		br.close();
		
	}

}
