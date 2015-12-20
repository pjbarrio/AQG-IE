package contentsummary.generator;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import utils.FileAnalyzer;
import utils.word.extraction.WordExtractor;



public class ContentSummaryGenerator extends ContentSummaryGeneratorInterface {
	

	private boolean lowercase;
	private WordExtractor wE;
	private boolean documentLevel;
	private String newName;
	private boolean stemmed;
	
	
	public ContentSummaryGenerator(String swfile,String newName) {
		super(swfile);
		this.newName = newName;
	}

	public void generateContentSummary(String dbfolder,String outputFile, boolean lowercase, boolean documentLevel,boolean stemmed){
		
		newContentSummary();

		
		wE = new WordExtractor();
		
		Date d = new Date();
		
		this.lowercase = lowercase;
		
		this.documentLevel = documentLevel;
		
		this.stemmed = stemmed;
		
		generateContentSummary(new File(dbfolder));
		
		Long t = new Long(new Date().getTime() - d.getTime());
		
		System.out.println("\nElapsed Time: " + t.toString());
		
		writeOutPutFile(outputFile);
		
		newContentSummary();
		
		
	}

	private void generateContentSummary(File dbfile) {
		
		 if (dbfile.isDirectory()) {			  // if a directory
		      String[] files = dbfile.list();		  // list its files
		      Arrays.sort(files);			  // sort the files
		      for (int i = 0; i < files.length; i++)	  // recursively index them
		        generateContentSummary(new File(dbfile, files[i]));

		 } else if (FileAnalyzer.isSummarizable(dbfile, newName)){// index .html or .htm files
		      
			System.out.println("Processing...[" + dbfile + "]"); 
			 
			try {
				addWordsToSummary(wE.getWords(FileUtils.readFileToString(dbfile), documentLevel, lowercase,stemmed),documentLevel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		 
		 } 
		 
	}

	public static String getPairCombination(String word, long frequency) {
		return word + CONTENT_SUMMARY_SEPARATOR + frequency;
	}



}
