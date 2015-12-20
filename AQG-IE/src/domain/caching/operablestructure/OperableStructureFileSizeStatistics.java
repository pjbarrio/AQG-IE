package domain.caching.operablestructure;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class OperableStructureFileSizeStatistics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int sampleSize = 10000;
		
		
//		|                             6 | RET_BONG     |
//		|                             7 | RET_SPK      |
//		|                             8 | RET_SSK      |
//		|                             9 | RET_DG       |

		
		int extractor = 8;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<String> operableStructure = pW.getOperableStructureFileList(extractor);
		
		Collections.shuffle(operableStructure);
		
		Map<Long,Integer> freqTable = new HashMap<Long, Integer>(); 
		
		for (int i = 0; i < operableStructure.size() && i < sampleSize; i++) {
			
			if ( (i%1000) == 0)
				System.out.println("Processing... " + i);
			
			long size = normalize(new File(operableStructure.get(i)).length());
			
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
