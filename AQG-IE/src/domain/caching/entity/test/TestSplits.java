package domain.caching.entity.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TestSplits {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int runningInstances = 3;
		
		File[] f = new File[]{new File("myfile"), new File("mysecondFile"), new File("myThirdFile"), new File("myFourthFile")};
		
		Map<Integer, File[]> map = createSplits(f, runningInstances);
		
		for (Entry<Integer,File[]> entry : map.entrySet()) {
			
			System.out.println(entry.getKey() + " - " + Arrays.toString(entry.getValue()));
			
		}
		
		Set<Long> s = new HashSet<Long>();
		s.add(1L);
//		s.add(2L);
//		s.add(3L);
//		s.add(4L);
		
		Map<Integer, Set<Long>> ss = generateSplits(s, runningInstances);
		
		for (Entry<Integer, Set<Long>> entry : ss.entrySet()) {
			
			System.out.println(entry.getKey() + " - " + entry.getValue().toString());
			
		}
	}

	private static Map<Integer, File[]> createSplits(File[] files,
			int runningInstances) {
		
		Map<Integer, File[]> ret = new HashMap<Integer, File[]>();
		
		int size = (int)Math.ceil((double)files.length / (double)runningInstances);
		
		int offset = 0;
		
		for (int i = 0; i < runningInstances; i++) {
			
			ret.put(i, Arrays.copyOfRange(files, Math.min(offset, files.length), Math.min(offset+size, files.length)));
			
			offset += size;
			
		}
		
		return ret;
	}
	
	private static Map<Integer, Set<Long>> generateSplits(Set<Long> set, int splits) {

		Map<Integer, Set<Long>> ret = new HashMap<Integer, Set<Long>>();
		
		int size = (int)Math.ceil((double)set.size() / (double)splits);
		
		List<Long> list = new ArrayList<Long>(set);
		
		int offset = 0;
		
		for (int i = 0; i < splits; i++) {
			
			ret.put(i, new HashSet<Long>(list.subList(Math.min(offset, list.size()), Math.min(offset+size, list.size()))));
			
			offset += size;
			
		}
		
		return ret;
		
	}
	
}
