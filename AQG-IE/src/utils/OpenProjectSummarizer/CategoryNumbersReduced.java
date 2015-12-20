package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class CategoryNumbersReduced {

	private static Hashtable<String, ArrayList<String>> table;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String databaseCategories = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/databasesCategoriesReduced.txt";
		
		String output = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/CategoryNumbersImproved.txt";
		
		loadDatabaseCategories(new File(databaseCategories));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		for(Enumeration<String> e = table.keys(); e.hasMoreElements();){
			
			String cat = e.nextElement();
			
			bw.write(table.get(cat).size() + "," + cat + "\n");
			
		}
		
		bw.close();

	}

private static void loadDatabaseCategories(File file) throws IOException {
		
		table = new Hashtable<String, ArrayList<String>>();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line = br.readLine();
		
		while (line!=null){
			
			String[] spl = line.split(",/proj/dbNoBackup/pjbarrio/sites/Directory/Top/");
			
			String website = spl[0];
			
			ArrayList<String> categories = getCategories(spl[1]);
			
			for (String category : categories) {
				
				ArrayList<String> rr = table.get("/proj/dbNoBackup/pjbarrio/sites/Directory/Top" + category);
				
				if (rr == null){
					
					rr = new ArrayList<String>();
					
					table.put("/proj/dbNoBackup/pjbarrio/sites/Directory/Top" + category,rr);
					
				}
				
				rr.add(website);
				
			}
						
			line = br.readLine();
		
		}
		
		br.close();
		
	}

	private static ArrayList<String> getCategories(String folder) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		ret.add("");
		
		String[] spl = folder.split("/");
		
		String last = "";
		
		for (int i = 0; i < spl.length; i++) {
			
			last = last + "/" + spl[i];
			
			ret.add(last);
			
		}

		return ret;
		
	}
}
