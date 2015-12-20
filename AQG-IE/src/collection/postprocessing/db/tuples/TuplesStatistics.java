package collection.postprocessing.db.tuples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import execution.workload.tuple.TupleReader;


public class TuplesStatistics {

	private static Hashtable<String, Integer> unique;

	/**
	 * This will give me how many unique tuples I have...
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[1];
		
		args[0] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/MatchingTuplesWithSources1stConstrained.tuples";
		
		BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));

		String line = br.readLine();
		
		unique = new Hashtable<String, Integer>();
				
		ArrayList<String> tuples = new ArrayList<String>();
		
		while (line != null){
			
			String tuple = TupleReader.generateTuple(line.substring(line.indexOf(',')+1)).toString();
			
			if(unique.containsKey(tuple)){
				Integer l = unique.get(tuple);
				l++;
				unique.put(tuple, l);
			}else{
				unique.put(tuple, 1);
				tuples.add(tuple);
			}
			
			line = br.readLine();
			
		}
		
		br.close();
		
		Collections.sort(tuples, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				
				return Double.compare(unique.get(o1), unique.get(o2));
				
			}
			
		});
		
		for (String tuple : tuples) {

			System.out.println(unique.get(tuple) + ", " + tuple);
			
		}
		
		
	}

}
