package collection.postprocessing.db.tuples;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;


public class TupleDuplicationStats {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(new File("/proj/dbNoBackup/pjbarrio/Experiments/workload/MatchingTuples.tuples")));
		
		String line = br.readLine();
		
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		
		int total = 0;
		
		while (line != null){
			
			total++;
			
			Integer value = ht.get(line);
			
			if (value==null){
				value = new Integer(0);
			}
			
			value = value + 1;
			
			ht.put(line, value);
			
			line = br.readLine();
		}
		
		System.out.println("Total: " + total);
		
		Hashtable<Integer, Integer> freqs = new Hashtable<Integer, Integer>();
		
		for(Enumeration<String> e = ht.keys();e.hasMoreElements();){
			String key = e.nextElement();
			
			Integer val = ht.get(key);
			
			Integer freq = freqs.get(val);
			
			if (freq==null){
				freq = new Integer(0);				
			}
			freq = freq+1;
			freqs.put(val, freq);
			
		}

		for(Enumeration<String> e = ht.keys();e.hasMoreElements();){
			String key = e.nextElement();
			
			Integer val = ht.get(key);

			System.out.println(val + " , " + key);
			
		}
		int differents = 0;
		
		for(Enumeration<Integer> e = freqs.keys();e.hasMoreElements();){
		
			Integer key = e.nextElement();
			
			Integer value = freqs.get(key);
			
			System.out.println(value + "," + key);
			differents = differents + value;
		
		}
		
		System.out.println("Unique: " + differents);
		
	}

}
