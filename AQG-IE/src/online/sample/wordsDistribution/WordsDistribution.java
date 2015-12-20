package online.sample.wordsDistribution;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import online.sample.wordsDistribution.generator.WordsDistributionGenerator;
import online.sample.wordsDistribution.generator.impl.IncrementalPageWordDistribution;
import online.sample.wordsDistribution.generator.impl.IncrementalSearchResultsWordDistributionGenerator;
import online.sample.wordsDistribution.generator.impl.SinglePageWordDistribution;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import com.google.gdata.util.common.html.HtmlToText;

public class WordsDistribution {

	public static final String SEPARATOR = " ";

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InvalidFormatException 
	 */
	public static void main(String[] args) throws InvalidFormatException, FileNotFoundException, IOException {
		
		TokenizerME tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream("/proj/dbNoBackup/pjbarrio/workspace/Wrappers/model/en-token.bin")));
		
		WordsDistributionGenerator wdg = new SinglePageWordDistribution(tokenizer);
			
		String type = "alltypes";
		
		File folder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Wrappers/Data/");
		
		File distribution = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/WordsDistribution/"+ type + "/");
		
//		WordsDistributionGenerator wdg = new IncrementalPageWordDistribution(tokenizer);
//		
//		File folder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/CleanQueryResult/smalltraining");
		
//		File bySize = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/CleanSizeSortedList/smalltraining/");
//		
//		File ordered = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/SizeOrderedExtractions/smalltraining/TEDW/total/");
//		
//		WordsDistributionGenerator wdg = new IncrementalSearchResultsWordDistributionGenerator(tokenizer, bySize, ordered);
		
//		File folder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/CleanQueryResult/smalltraining");
//		
//		File distribution = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/WordsDistribution/afterExtract/smalltraining/");

		File[] files = folder.listFiles();
		
		Arrays.sort(files);
		
		for (File file : files) {
			
			System.gc();
			
			System.out.println(file.getName());
			
			String fi = wdg.getParentFolder(file);
			
			if (fi!=null){
				
				File nf = new File(distribution,fi);
				
				if (nf.exists())
//					continue;
					;
				else
					nf.mkdirs();
				
			}
	
			wdg.generateWords(file);
			
			for (int i = 0; i < wdg.generatedDistributions(); i++) {
				
				Map<String,Integer> map = wdg.getWords(i);
				
				File f = new File(distribution, wdg.getOutPutName(i));
				
				f.getParentFile().mkdirs();
				
				save(map,f);
				
			}
						
		}
		
	}
	
	private static void save(Map<String, Integer> map, File file) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		for (Entry<String, Integer> entry : map.entrySet()) {
			
			bw.write(entry.getKey() + SEPARATOR + entry.getValue());
			
			bw.newLine();
			
		}
		
		bw.close();
	}



		
}
