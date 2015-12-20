package utils.word.extraction.test;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import exploration.model.Document;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.contentLoader.impl.FileContentLoader;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import weka.core.tokenizers.WordTokenizer;

public class WordExtractorTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String website = "http://www.worldenergy.org";
		
//		String website = "http://travel.state.gov/";

		Hashtable<Long, Document> id = pW.getDocumentsTable(pW.getDatabaseByName(website));
		
		ContentExtractor gce = new TikaContentExtractor();
		
		FileContentLoader fcl = new FileContentLoader();
		
		WordExtractor we = new WordExtractor(gce, fcl);
		
		for (Entry<Long,Document> entry : id.entrySet()) {
			
			System.out.println(entry.getKey());
			
			String[] words = we.getWords(entry.getValue(), true, true, false);
			
		}
		
//		File toExtract = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/0/1947/RESULTS/CHNH/TEDW/ALLLINKS/criticisms/0/4.html");
//		
//		WordTokenizer wt = new WordTokenizer();
//
//		try {
//			wt.tokenize(FileUtils.readFileToString(toExtract));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		  // Iterate through tokens, perform stemming, and remove stopwords
//		  // (if required)
//		  while (wt.hasMoreElements()) {
//		    String word = ((String)wt.nextElement()).intern();
//		    System.out.println(word);
//		  }
		
//		we.getWords(toExtract, true, true, false, "");
		
	}

}
