package utils.id.useful;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import exploration.model.Document;

import utils.id.Idhandler;

public class UsefulUselessHandler {

	public static void write(ArrayList<Long> array, String file) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
		
		for (Long long1 : array) {
			
			bw.write(long1 + "\n");
			
		}
		
		bw.close();
		
	}

	public static ArrayList<Document> loadFile(String use, Idhandler idhandler) throws IOException {
		
		throw new UnsupportedOperationException("IMPLEMENT!");

		
//		BufferedReader br = new BufferedReader(new FileReader(new File(use)));
//		
//		ArrayList<Document> usefulDocs = new ArrayList<Document>();
//		 
//		String line = br.readLine();
//		
//		while (line != null){
//			
//			usefulDocs.add(idhandler.getDocument(Long.valueOf(line)));
//			
//			line = br.readLine();
//		
//		}
//
//		return usefulDocs;
		
	}

	public static HashSet<Long> loadIds(String useFile) throws IOException {
		HashSet<Long> useful = new HashSet<Long>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(useFile)));
		
		String line = br.readLine();
		
		while (line!=null){
			
			useful.add(Long.valueOf(line));
			
			line = br.readLine();
			
		}
		
		br.close();
		return useful;
	}

}
