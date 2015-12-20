package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class DatabasesUnifier {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/";
		
		Hashtable<Integer,String> databases = loadTable(new File(rootDirectory + "finalIndexCleaned.txt"));

		Hashtable<Integer, String> category = loadTable(new File(rootDirectory + "finalIndexDatabases.txt"));
		
		Hashtable<String,ArrayList<Integer>> indexesPerDatabase = generateIndexesPerDatabase(databases);
		
		System.setOut(new PrintStream(new File(rootDirectory + "databasesCategories.txt")));
		
		for (Enumeration<String> e = indexesPerDatabase.keys(); e.hasMoreElements();){
			
			String database = e.nextElement();
			
			ArrayList<Integer> indexes = indexesPerDatabase.get(database);
			
			ArrayList<String> categories = findCategories(category,indexes);
			
			writeOutput(database,categories);
			
		}
		
	}

	private static void writeOutput(String database,
			ArrayList<String> categories) {
		

		for (String string : categories) {
			
			if (string != null)
				System.out.println(database + "," + string);
		}
		
	}

	private static ArrayList<String> findCategories(
			Hashtable<Integer, String> category, ArrayList<Integer> indexes) {
		
		ArrayList<String> ret = new ArrayList<String>();
		

		for (Integer integer : indexes) {
		
			String cat = category.get(integer);
			
			if (!ret.contains(cat)){
				
				ret.add(cat);
			
			}
			
		}
		
		return ret;
		
	}

	private static Hashtable<String, ArrayList<Integer>> generateIndexesPerDatabase(
			Hashtable<Integer, String> databases) {
		
		Hashtable<String, ArrayList<Integer>> ret = new Hashtable<String, ArrayList<Integer>>();
		
		for (Enumeration<Integer> e = databases.keys(); e.hasMoreElements();){
			
			Integer index = e.nextElement();
			
			String database = databases.get(index);
			
			if (ret.containsKey(database)){
				
				ret.get(database).add(index);
				
			} else {
				
				ArrayList<Integer> aux = new ArrayList<Integer>();
				aux.add(index);
				ret.put(database,aux);
			
			}
			
		}
		
		return ret;
		
	}

	private static Hashtable<Integer, String> loadTable(File file) throws Exception {
		
		Hashtable<Integer, String> t = new Hashtable<Integer, String>();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line = br.readLine();
		
		while (line!=null){
			
			String index = line.substring(0, line.indexOf(','));
			
			String val = line.substring(line.indexOf(',') + 1);
			
			t.put(Integer.valueOf(index), val);
			
			line = br.readLine();
		}
		
		br.close();
		
		return t;
	}

}
