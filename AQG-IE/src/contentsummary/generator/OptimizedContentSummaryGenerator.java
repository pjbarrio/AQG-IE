package contentsummary.generator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;

import utils.FileAnalyzer;
import utils.word.extraction.WordExtractor;
import contentsummary.reader.ContentSummaryReader;


public class OptimizedContentSummaryGenerator extends ContentSummaryGeneratorInterface{

	private static final int MAX_RAW_FILES_PROCESSED = 550;
	private static final int ROUND_FILES_PROCESSED = 12;
	private WordExtractor wE;
	private int processed;
	private int step;
	private int round;
	private ContentSummaryReader cr;
	private String[] files;
	private String word;

	private File actualFile;
	private File main;
	private boolean started;
	private String next;
	private String newName;

	public OptimizedContentSummaryGenerator(String swfile, String newName) {
		super(swfile);
		this.newName = newName;
	}

	public void setValues(boolean startFromZero,int step,String next){
		this.step = step;
		this.next = next;
		this.started = startFromZero;
	}
	
	@Override
	public void generateContentSummary(String dbfolder, String outputFile,
			boolean lowercase, boolean documentLevel,boolean stemmed) {
		
		Date d = new Date();
		
		wE = new WordExtractor();
		//Step 1 generate content Summaries in group of 500.
		
		newContentSummary();
		
		processed = 0;
//		step = 136;
		
//		started = false;
//		next = "/proj/db/NoBackup/pjbarrio/sites/Trip/TMZ/www.tmz.com/2009/08/18/honor-alba-warren-jessica-alba-baby-smile-video/4";
		
		new File(outputFile + "/").mkdir();
		
		generateContentSummaries(new File(dbfolder),lowercase,documentLevel,stemmed,new String (outputFile + "/AUX"));
		
		writeOutPutFile(outputFile + "/AUX" + step);
		
		newContentSummary();
		
		//Step 2 mix the files in groups of 300.
		
		step = 0;
		processed = 0;
		round = 0;
		
		main = new File(outputFile + "/");
		
		mixCS(main,new File(outputFile + "/ROUND" + round + "/"));
		
		writeOutPutFile(outputFile + "-cs.txt");
		
		Long t = new Long(new Date().getTime() - d.getTime());
		
		System.out.println("\nElapsed Time: " + t.toString());
	}

	private void mixCS(File file,File outputFile) {
		
		files = file.list();
		
		if (files.length>1){
		
			newContentSummary();
			
			outputFile.mkdir();
			
			for (String fileName : files) {
				
				actualFile = new File(file.getAbsolutePath() + "/" + fileName);		
				
				cr = new ContentSummaryReader(actualFile.getAbsolutePath());
				
				for(Enumeration<String> e = cr.enumeration();e.hasMoreElements();){
					
					word = e.nextElement();
					
					updateContentSummary(word, cr.getFrequency(word));
					
				}
				
				processed++;
				
				if (processed == ROUND_FILES_PROCESSED){
					
					processed = 0;
					
					writeOutPutFile(outputFile.getAbsolutePath() + "/AUX" + step);
					
					newContentSummary();
					
					step++;
				}
				
			}
			
			if (processed > 0) {
			
				processed = 0;
				
				writeOutPutFile(outputFile.getAbsolutePath() + "/AUX" + step);
				
				step++;
				
			}
			
			round ++;
						
			step = 0;
			
			mixCS(outputFile,new File(main.getAbsoluteFile() + "/ROUND" + round + "/"));
			
		}
		
	}

	private void generateContentSummaries(File file, boolean lowercase,
			boolean documentLevel, boolean stemmed,String auxFolderName) {
		
		System.out.println(file);
		
		if (file.isDirectory()) {			  // if a directory
		      String[] files = file.list();		  // list its files
		      Arrays.sort(files);			  // sort the files
		      for (int i = 0; i < files.length; i++)	  // recursively index them
		        generateContentSummaries(new File(file, files[i]),lowercase,documentLevel,stemmed,auxFolderName);

		 } else if (started || next.equals(file.getAbsolutePath())){
			 
			 started = true;
			 
			if (FileAnalyzer.isSummarizable(file, new String("check-" + newName))){// index .html or .htm files
		      
			System.out.println("Step: " + step + " -Processed: " + processed + " -Processing...[" + file + "]"); 
			 
			try {
				
				addWordsToSummary(wE.getWords(FileUtils.readFileToString(file), documentLevel, lowercase, stemmed),documentLevel);
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		 
			processed++;
			
			if (processed == MAX_RAW_FILES_PROCESSED){
				
				processed = 0;
				
				writeOutPutFile(auxFolderName + step);
				
				newContentSummary();
				
				step++;
			}
		 } 
		
		 } else {
			 System.out.println("Skipped...[" + file + "]"); 
		 }
	}

}
