package domain.caching.candidatesentence;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class CandidateSentencesFileSizeStatistics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int sampleSize = 10000;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<String> candidateSentences = pW.getCandidateSentencesFileList();
		
		Collections.shuffle(candidateSentences);
		
		Map<Long,Integer> freqTable = new HashMap<Long, Integer>(); 
		
		for (int i = 0; i < candidateSentences.size() && i < sampleSize; i++) {
			
			if ( (i%1000) == 0)
				System.out.println("Processing... " + i);
			
			long size = normalize(new File(candidateSentences.get(i)).length());
			
			Integer freq = freqTable.remove(size);
			
			if (freq == null){
				freq = 0;
				
			}
			
			freq++;
			
			freqTable.put(size, freq);
			
		}
		
		for (Entry<Long,Integer> entry : freqTable.entrySet()) {
			System.out.println(entry.getKey() + ","+ entry.getValue());
		}
		
	}

	private static long normalize(long length) {
		
		return (long)Math.ceil(((double)length/1024.0)/1024.0);
		
	}

}
