package sample.generation.othersourceGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import online.documentHandler.contentExtractor.impl.DummyContentExtractor;
import online.sample.wordsDistribution.WordsDistribution;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import utils.word.extraction.WordExtractor;

import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class OtherSourceWordListGenerator {

	private static Set<String> wordsSet;

	private static Map<String,Integer> wordsFreq;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		File prefix = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/");
		
		File toSave = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/word_distribution.txt");
		
		File toSaveWF = new File ("/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/word_distribution_wf.txt");
		
		String[] folders = {"tipster_vol_1","tipster_vol_2","tipster_vol_3","tipster_vol_5"};
		
		WordExtractor wE = new WordExtractor(new DummyContentExtractor(),null);
		
		wordsSet = new HashSet<String>();
		
		wordsFreq = new HashMap<String,Integer>();
		
		for (int i = 0; i < folders.length; i++) {
			
			process(prefix, folders[i],wE);
			
		}
		
//		FileUtils.writeLines(toSave, wordsSet);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(toSaveWF));
		
		for (Entry<String,Integer> entry : wordsFreq.entrySet()) {
			
			bw.write(entry.getKey() + WordsDistribution.SEPARATOR + entry.getValue() + "\n");
			
		}
		
		bw.close();
	}
	
	private static void process(File prefix, String child, WordExtractor wE) throws FileNotFoundException, IOException {
		
		File toExtractFrom = new File(prefix,child);
		
		File[] files = toExtractFrom.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			
			if (files[i].isDirectory()){
				System.out.println(files[i].getAbsolutePath());
				process(toExtractFrom,files[i].getName(),wE);
			}else{
				
				FileReader fr = new FileReader(files[i]);
				
				String content = new SgmlDocument(fr).getSignalText();
				
				fr.close();
				
				String[] words = wE.getWords(content, true, true, false);
				
				for (int j = 0; j < words.length; j++) {
					
					Integer freq = wordsFreq.remove(words[j].intern());
					
					if (freq == null)
						freq = 0;
					
					freq++;
					
					wordsFreq.put(words[j].intern(), freq);
					
					wordsSet.add(words[j].intern());
					
				}
				
			}
			
		}
		
	}
	
}
