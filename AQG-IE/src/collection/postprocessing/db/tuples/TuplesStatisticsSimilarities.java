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
import java.util.HashSet;
import java.util.Hashtable;

import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;


public class TuplesStatisticsSimilarities {

	private static Hashtable<String, Integer> unique;
	private static Hashtable<String, Hashtable<String, HashSet<String>>> attributesList;
	private static ArrayList<String> uniqueTuples;
	private static Hashtable<String, ArrayList<String>> matchingTuples;
	private static ArrayList<ArrayList<String>> processedMatches;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		uniqueTuples = new ArrayList<String>();
		
		args = new String[3];
		
		args[0] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/MatchingTuplesWithSources1stConstrained.tuples";
		
		args[1] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/evaluation/TuplesStatistics/Similarities/"; //prefix
		
		args[2] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/evaluation/TuplesStatistics/Similarities/Cluster";
		
		String loc = args[1] + "location";
		
		String eff = args[1] + "effect";
		
		String dat = args[1] + "date";
		
		
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
		
		attributesList = new Hashtable<String, Hashtable<String, HashSet<String>>>();		
		
		attributesList.put("location", generateSimilaritiesTable(loc));
		
		attributesList.put("effect", generateSimilaritiesTable(eff));
		
		attributesList.put("date", generateSimilaritiesTable(dat));
		
		matchingTuples = new Hashtable<String, ArrayList<String>>();
		
		for (String tuple1 : tuples) {
			
			Tuple t1 = TupleReader.generateTuple(tuple1);
			
			ArrayList<String> tuplesmatching = new ArrayList<String>();
			
			for (String tuple2 : tuples) {
				
				Tuple t2 = TupleReader.generateTuple(tuple2);
				
				if (matches(t1,t2)){
					
					tuplesmatching.add(t2.toString());
				}
				
			}
			
			uniqueTuples.add(t1.toString());
			
			Collections.sort(tuplesmatching);
			
			matchingTuples.put(t1.toString(), tuplesmatching);
			
		}
		
		saveResults(args[2]);
		
	}

	private static void saveResults(String file) throws IOException {
		
		processedMatches = new ArrayList<ArrayList<String>>();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
		
		Collections.sort(uniqueTuples, new Comparator<String>(){

			@Override
			public int compare(String t1, String t2) {
				
				return Double.compare(matchingTuples.get(t1).size(), matchingTuples.get(t2).size());
			
			}
			
		});
		
		int i = 0;
		
		for (String str : uniqueTuples) {
			
			ArrayList<String> al = matchingTuples.get(str);
			
			if (!processed(al)){
				
				i++;
				
				processedMatches.add(al);
				
				long freq = getFrequency(str);
				
				System.out.println(freq + "," + str);
				
				bw.write("\nTUPLE: " + freq + "\n" + unique.get(str) + "," + str + "\n");
				
				for (String string : al) {
					
					if (!string.equals(str))
						bw.write(unique.get(string) + "," + string + "\n");
					
				}
			}
			
		}
		
		System.out.println("Tuples: " + i);
		
		bw.close();
		
	}

	private static long getFrequency(String str) {
		
		long ret = unique.get(str);
		
		for (String tupleMatched : matchingTuples.get(str)) {
			
			if (!str.equals(tupleMatched)){
				
				ret += unique.get(tupleMatched);
				
			}
			
		}
		
		return ret;
		
	}

	private static boolean processed(ArrayList<String> al) {
		
		for (ArrayList<String> alreadyProcessed : processedMatches) {
			
			if (alreadyProcessed.size() == al.size()){
				if (isEqual(alreadyProcessed,al)){
					return true;
				}
			}
			
		}
		
		return false;
		
	}

	private static boolean isEqual(ArrayList<String> alreadyProcessed,
			ArrayList<String> al) {
		
		for (int i = 0; i < alreadyProcessed.size(); i++) {
			
			if (!alreadyProcessed.get(i).equals(al.get(i))){
				return false;
			}
			
		}
		
		return true;
		
	}

	private static boolean matches(Tuple t1, Tuple t2) {
		
		String[] fields = t1.getFieldNames();
		
		for (int i = 0; i < fields.length; i++) {
			
			String v1 = t1.getFieldValue(fields[i]);
			
			String v2 = t2.getFieldValue(fields[i]);
			
			if (!matchesAttribute(fields[i],v1,v2)){
				
				return false;
				
			}
			
		}
		
		return true;
		
	}

	private static boolean matchesAttribute(String field, String v1, String v2) {
		
		if (v2==null)
			return false;
		
		if (v1.trim().equals(v2.trim()))
			return true;
		
		if (!attributesList.containsKey(field)){
			return false;
		}
		
		HashSet<String> mmatches = attributesList.get(field).get(v1);
		
		return (mmatches!=null && mmatches.contains(v2));
		
	}

	private static Hashtable<String,HashSet<String>> generateSimilaritiesTable(String string) throws IOException {
		
		Hashtable<String, HashSet<String>> ret = new Hashtable<String, HashSet<String>>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(string)));
		
		String line = br.readLine();
		
		while (line != null){
			
			while (line!= null && line.trim().equals("")){ //skipping white lines.
				line = br.readLine();
			}
			
			if (line == null){
				break;
			}
			
			HashSet<String> arrayList = new HashSet<String>();
			
			String first = line;
			
//			arrayList.add(line); //is equal to itself
			
			line = br.readLine(); 
			
			while (line!= null && !line.trim().equals("")){
				
				arrayList.add(line);
				
				line = br.readLine();
				
			}
			
			ret.put(first, arrayList);
			
		}
		
		br.close();
		
		return ret;
		
	}

}
