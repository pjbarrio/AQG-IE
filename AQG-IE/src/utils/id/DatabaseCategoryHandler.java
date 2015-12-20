package utils.id;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseCategoryHandler {

	public static Map<String, String> loadDatabaseCategory(File databaseIndex) throws IOException {

		Map<String, String> indexTable = new HashMap<String, String>();
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(databaseIndex));
		
		while ((line=br.readLine())!=null){
			
			String website= line.substring(0,line.indexOf(','));
			
			String category = line.substring(line.indexOf(',')+1);
			
			indexTable.put(website,category);
		
		}

		br.close();
		
		return indexTable;
		
	}
	
	public static Map<String, List<String>> loadDatabaseCategoryIngerted(File databaseIndex) throws IOException {

		Map<String, List<String>> indexTable = new HashMap<String, List<String>>();
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(databaseIndex));
		
		while ((line=br.readLine())!=null){
			
			String website= line.substring(0,line.indexOf(','));
			
			String category = line.substring(line.indexOf(',')+1);
			
			List<String> cats = indexTable.remove(website);
			
			if (cats == null){
				cats = new ArrayList<String>();
			}
			
			if (!cats.contains(category))
				cats.add(category);
			
			indexTable.put(website,cats);
		
		}

		br.close();
		
		return indexTable;
		
	}
	
}
