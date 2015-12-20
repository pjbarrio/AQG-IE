package techniques.evaluation.preprocessing;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import utils.id.Idhandler;
import utils.id.useful.UsefulUselessHandler;
import utils.word.extraction.WordExtractor;
import contentsummary.generator.ContentSummaryGenerator;
import exploration.model.Document;

public class UsefulContentSummaryGenerator {

	private static String idFile;
	private static String useful;
	private static String cs;
	private static ArrayList<Document> usefulDocs;
	private static WordExtractor wE;
	private static Hashtable<String, Long> contentTable;
	private static String root;
	private static Idhandler idhandler;
	private static boolean unique;
	private static boolean lowercase;
	private static boolean stemmed;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String version = "1stVersionConstrained";
		
		args = new String[8];
		
		args[0] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/TheEconomist.id";
		args[1] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/UsefulTheEconomist" + version;
		args[2] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/Useful"+ version +"TheEconomistCS.txt";
		args[4] = "/proj/db/NoBackup/pjbarrio/sites/Business/TheEconomist/";
		args[5] = "true";
		args[6] = "true";
		args[7] = "false";
		
		idFile = args[0];
		useful = args[1];
		cs = args[2];
		root = args[4];
		unique = Boolean.valueOf(args[5]);
		lowercase = Boolean.valueOf(args[6]);
		stemmed = Boolean.valueOf(args[7]);
		
		contentTable = new Hashtable<String,Long>();
		
		loadReverseIds();
		
		loadUSeful();
		
		generateCS();
		
		writeFinalFile();
		
	}

	private static void writeFinalFile() throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(cs)));
		
		for(Enumeration<String> e = contentTable.keys();e.hasMoreElements();){
			
			String word = e.nextElement();
			
			Long Frequency = contentTable.get(word);
			
			bw.write(ContentSummaryGenerator.getPairCombination(word,Frequency) + "\n");
		
		}
		
		bw.close();
		
	}

	private static void generateCS() {
		
		wE = new WordExtractor();
		
		long index = 1;
		
		for (Document document : usefulDocs) {
			
			System.out.println("Processing...: " + index++ + " Out of : " + usefulDocs.size());
			
			String[] words = wE.getWords(document, unique, lowercase, stemmed);
			
			addWords(words);
			
		}
		
	}

	private static void addWords(String[] words) {
		
			for (int i = 0; i < words.length; i++) {
				
				Long freq = contentTable.get(words[i]);
				
				if (freq==null){
					freq = new Long(0);
				}
				
				freq++;
				
				contentTable.put(words[i], freq);
				
			}
		
	}

	private static void loadUSeful() throws IOException {
		
		usefulDocs = UsefulUselessHandler.loadFile(useful,idhandler);		
		
	}

	private static void loadReverseIds() throws IOException {
		
		idhandler = new Idhandler(idFile, true);
		
	}

}
