package collection.processing.indetifying;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import utils.id.TuplesLoader;

public class GeneratingUsefulfromMatchingTuples {

	private static Hashtable<Long, ArrayList<String>> docTuplesTable;
	private static HashSet<Long> usefuls;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] version = {"","Constrained"};
		
		String[] database = {"Bloomberg","TheCelebrityCafe","TheEconomist","UsNews","Variety"};
		
		for (int i = 0; i < database.length; i++) {
			
			for (int j = 0; j < version.length; j++) {
				
				String matchingTuples = "/proj/db/NoBackup/pjbarrio/Experiments/workload/MatchingTuplesWithSources"+database[i]+"1st"+ version[j] +".tuples";
				
				String useful = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useful" + database[i] + "1stVersion" + version[j];
				
				docTuplesTable = TuplesLoader.loadIdtuplesTable(matchingTuples);
				
				usefuls = loadUsefuls(useful);
				
				System.out.println(database[i] + " - " + version[j]);
				
				fix(useful);
				
			}
			
		}
		
		
		
	}

	private static void fix(String useful) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(useful)));
		
		for (Enumeration<Long> e = docTuplesTable.keys();e.hasMoreElements();){
			
			Long id = e.nextElement();
			
			bw.write(id + "\n");
			
			usefuls.remove(id);
			
		}
		
		bw.close();
		
		for (Long id : usefuls) {
			System.out.println(id);
		}
		
	}

	private static HashSet<Long> loadUsefuls(String useful) throws IOException {
		
		HashSet<Long> ret = new HashSet<Long>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(useful)));
		
		String line = br.readLine();
		
		while (line!=null){
			
			ret.add(Long.valueOf(line));
			
			line = br.readLine();
			
		}
		
		br.close();
		
		return ret;
		
	}

}
