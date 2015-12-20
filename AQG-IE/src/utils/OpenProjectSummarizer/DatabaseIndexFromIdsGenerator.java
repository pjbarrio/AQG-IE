package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

public class DatabaseIndexFromIdsGenerator {

	private static Hashtable<Integer, String> indexTable;

	public static void main(String[] args) throws IOException {
		
		String idsToProcess = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/NoResponseIds";
		
		String databaseIndex = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/FinalDataIndex.txt";
		
		String outputIndex = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/NoResponseDataIndex.txt";
		
		loadDatabaseIndex(databaseIndex);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(idsToProcess)));
		
		String line;
		
		System.setOut(new PrintStream(new File(outputIndex)));
		
		while ((line = br.readLine())!=null){
			
			int index = Integer.valueOf(line);
			
			System.out.println(index + "," + indexTable.get(index));
			System.err.println(indexTable.get(index));
		}
		
		br.close();
		
	}

	private static void loadDatabaseIndex(String databaseIndex) throws IOException {
		
		indexTable = new Hashtable<Integer, String>();
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(databaseIndex));
		
		while ((line=br.readLine())!=null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			indexTable.put(index,website);
			
		}
		
		br.close();
		
	}
	
}
