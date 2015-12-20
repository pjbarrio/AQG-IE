package utils.execution.runningExtractors.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import online.sample.wordsDistribution.rank.TFIDFKeywordGenerator;

import org.apache.commons.io.FileUtils;

public class RelationsStatistics {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		File files = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/SizeOrderedExtractions/smalltraining/TEDW/outputFiles.txt");
		
		List<String> list = FileUtils.readLines(files);
		
		int norelation = 0;
		
		int error = 0;
		
		Map<Integer,Map<String,Integer>> map = new HashMap<Integer, Map<String,Integer>>();
		
		Map<Integer,Integer> docMap = new HashMap<Integer, Integer>();
		
		Map<Integer,Integer> usefulMap = new HashMap<Integer, Integer>();
		
		for (String file : list) {
		
			File f = new File(file);
			
			Integer i = Integer.valueOf(f.getParentFile().getName());
			
			Map<String, Integer> dbMap = getMap(i,map);
			
			update(docMap,i);
			
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			String line = br.readLine();
			
			if (line.length() > 214){
				
				String shortt= line.substring(214);
				
				if (shortt.startsWith("uages") || shortt.contains("</Error>")){
					error++;
				}else{
					
					String[] spl = shortt.split(",");
					
					for (int j = 0; j < spl.length; j++) {
						
						update(dbMap,spl[j].trim(),1);
						
					}
					
					update(usefulMap,i);
					
				}
							
			}
				
			else
				norelation++;
			
			br.close();
		}

		System.out.println("No Relation: " + norelation);
		
		System.out.println("Error: " + error);
		
		Map<String,Integer> global = new HashMap<String, Integer>();
			
		Map<String,Integer> globalDB = new HashMap<String, Integer>();
		
		for (Entry<Integer, Map<String, Integer>> dbMap : map.entrySet()) {
			
			System.out.println("Database: " + dbMap.getKey() + " - " + docMap.get(dbMap.getKey()) + " - " + usefulMap.get(dbMap.getKey()));
			
			System.out.println(dbMap.getValue().toString());
			
			for (Entry<String,Integer> entry : dbMap.getValue().entrySet()) {
				
				update(global,entry.getKey(),entry.getValue());
				
				update(globalDB,entry.getKey(),1);
				
			}
			
		}
				
		System.out.println(global.toString());
		
		System.out.println(globalDB.toString());
		
	}

	private static void update(Map<Integer, Integer> docMap, Integer i) {
		
		Integer freq = docMap.get(i);
		
		if (freq == null){
			
			freq = 0;
			
		}
		
		docMap.put(i, freq+1);
		
	}

	private static void update(Map<String, Integer> dbMap, String relation, int sum) {
		
		Integer freq = dbMap.get(relation);
		
		if (freq == null){
			
			freq = 0;
			
		}

		dbMap.put(relation, freq+sum);
		
	}

	private static Map<String, Integer> getMap(Integer i,
			Map<Integer, Map<String, Integer>> map) {
		
		Map<String,Integer> ret = map.get(i);
		
		if (ret == null){
			
			ret = new HashMap<String, Integer>();
			
			map.put(i, ret);
			
		}
		
		return ret;
	}

}
