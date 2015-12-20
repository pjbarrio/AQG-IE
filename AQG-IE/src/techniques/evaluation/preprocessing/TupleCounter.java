package techniques.evaluation.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

public class TupleCounter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[2];
		
		args[0] = "/home/pjbarrio/workspace/Experiments/workload/DuplicatedMatchingTuples";
		
		args[1] = "/home/pjbarrio/workspace/Experiments/workload/UniqueMatchingTuples";
		
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		
		String line = br.readLine();
		
		Hashtable<String,Long> ht = new Hashtable<String,Long>();
		
		while (line!=null){
			
			line = line.split("\\<sourceDocument\\>")[0];
			
			Long l = ht.get(line);
			
			if (l == null){
				
				l = new Long(0);
				
			}
			
			l++;
			
			ht.put(line, l);
			
			line = br.readLine();
		}
		
		br.close();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(args[1])));
		
		bw.write("FREQUENCY,TUPLE");
		
		for (Enumeration<String> e = ht.keys();e.hasMoreElements();) {
			
			String el = e.nextElement();
			
			bw.write("\n" + ht.get(el) + "," + el);
			
		}
	
		bw.close();
	}

}
