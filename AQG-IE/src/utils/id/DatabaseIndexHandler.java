package utils.id;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DatabaseIndexHandler {

	public static Map<String, Integer> loadDatabaseIndex(File databaseIndex) throws IOException {

		Map<String, Integer> indexTable = new HashMap<String, Integer>();
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(databaseIndex));
		
		while ((line=br.readLine())!=null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			indexTable.put(website,index);
		
		}

		br.close();
		
		return indexTable;
		
	}

	public static Map<Integer, String> loadInvertedDatabaseIndex(File databaseIndex) throws IOException {
		
		Map<Integer, String> indexTable = new HashMap<Integer, String>();
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(databaseIndex));
		
		while ((line=br.readLine())!=null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			indexTable.put(index,website);
		
		}

		br.close();
		
		return indexTable;
		
	}
	
}
