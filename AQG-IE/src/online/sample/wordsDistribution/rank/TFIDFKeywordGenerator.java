package online.sample.wordsDistribution.rank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import online.sample.wordsDistribution.HashBasedComparator;
import online.sample.wordsDistribution.WordsDistribution;
import online.sample.wordsDistribution.WordsDistributionLoader;
import utils.execution.IsInListFileFilter;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class TFIDFKeywordGenerator {

	public static void main(String[] args) throws IOException {
		
		String type = "alltypes";
		
		String now = "testSet";
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<String> all = FileUtils.readLines(new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/" + type));
		
		List<String> mine = FileUtils.readLines(new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/" + type));
		
		all.removeAll(mine);
		
//		File output = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/"+type+"/");
		
		File output = new File(pW.getTfIdfFolder());
		
		File distribution = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/WordsDistribution/"+type+"/");
		
		WordsDistributionLoader wdl = new WordsDistributionLoader();
		
		wdl.load(distribution.listFiles(new IsInListFileFilter(all)));

		Map<String,Double> df = new HashMap<String, Double>();
		
		double size = (double)wdl.getFiles().size();
		
		WordsDistributionLoader wdlt = new WordsDistributionLoader();
		
		wdlt.load(distribution.listFiles(new IsInListFileFilter(mine)));
		
		for (String word : wdl.getWords()) {
			
//			idf.put(word, Math.log(((double)size)/((double)wdl.getDocumentFrequency(word))));
	
			df.put(word, (double)wdl.getDocumentFrequency(word));
			
		}
			
		Map<Integer,Map<String,Double>> t = new HashMap<Integer, Map<String,Double>>();

		Set<Integer> files = wdlt.getFiles();
		
		for (Integer index : files) {
			
			System.out.println("File: " + index);
			
			Map<String,Double> tt = new HashMap<String, Double>();
			
			for (Entry<String, Long> entry : wdlt.getWordMap(index).entrySet()) {
				
				double tf = (double)entry.getValue();
				
				Double val = df.get(entry.getKey());
				
				double ddf;
				
				if (val==null)
					ddf = 0;
				else
					ddf = val.doubleValue();
				
				
				double tfIdf = Math.log((size+1.0)/(ddf+1.0))*tf;
				
				tt.put(entry.getKey(), tfIdf);
				
			}
			
			t.put(index, tt);
		}
		
		for (Entry<Integer,Map<String,Double>> toPrint : t.entrySet()) {
			
			write(new File(output,toPrint.getKey().toString()+".txt"),toPrint.getValue());
			
		}
		
	}

	private static void write(File file, Map<String, Double> map) throws IOException {
		
		List<String> words = new ArrayList<String>(map.keySet());
		
		Comparator<String> c = new HashBasedComparator(map);
		
		Collections.sort(words, c);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		for (String word : words) {
			
			bw.write(word + WordsDistribution.SEPARATOR + map.get(word));
			
			bw.newLine();
			
		}
		
		bw.close();
	}
	
}
