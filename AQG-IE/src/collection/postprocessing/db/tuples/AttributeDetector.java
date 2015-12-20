package collection.postprocessing.db.tuples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;


public class AttributeDetector {

	private static Hashtable<String, Integer> unique;
	private static Hashtable<String, Integer> values;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[3];
		
		args[0] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/MatchingTuplesWithSources1stConstrained.tuples";
		
		args[1] = "datestring,naturaldisaster,date,effect,location";
		
		args[2] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/evaluation/TuplesStatistics/";
		
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
		
		String[] spl = args[1].split(",");
		
		for (int i = 0; i < spl.length; i++) {
			
			generateDifferentValues(spl[i],args[2],tuples);
			
		}
		
	}

	private static void generateDifferentValues(String attr, String prefix,
			ArrayList<String> tuples) throws IOException {
		
		values = new Hashtable<String, Integer>();
		
		ArrayList<String> vals = new ArrayList<String>();
		
		for (String tuple : tuples) {
			
			Tuple t = TupleReader.generateTuple(tuple);
			
			Integer freq = unique.get(tuple);
			
			String value = t.getFieldValue(attr);
			
			if (value == null){
				
				value = "NULL";
			
			}
			
			value = value.trim();
			
			if (values.containsKey(value)){
				
				Integer actualFreq = values.get(value);
				
				actualFreq+=freq;
				
				values.put(value, actualFreq);
			
			} else {
				
				vals.add(value);
				
				values.put(value, freq);
				
			}
			
		}

		Collections.sort(vals, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				
				return Double.compare(values.get(o1), values.get(o2));
				
			}
		
		});
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(prefix + attr)));
		
		for (String string : vals) {
			
			bw.write(values.get(string) + ", " + string + "\n");
			
		}
		
		bw.close();
	}

}
