package sample.generation.relation.database.ui.auxi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class TuplesStatistics {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] relations = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
				"VotingResult"/*,"ProductIssues","Quotation"*/,"PollsResult"};

		String collection = "TREC";
		
		int selectednumber = 650;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		for (int i = 0; i < relations.length; i++) {
			
			System.out.println(relations[i]);
			
			File f = new File(pW.getSavedOutputForRelationExtractionTrainingAll(collection,relations[i], selectednumber));
			
			List<String> lines = FileUtils.readLines(f);
			
			Map<String,Integer> freqTable = new HashMap<String, Integer>();
			
			while (!lines.isEmpty()) {
				
				String key = lines.remove(0);
				
				Integer freq = freqTable.remove(key);
				
				if (freq == null){
					
					freq = 0;
					
				}
				
				freqTable.put(key, freq+1);
				
			}
			
			System.out.print("GOOD: " + freqTable.get("1"));
			System.out.print(" - BAD: " + freqTable.get("3"));
			System.out.print(" - FIX: " + freqTable.get("2"));
			System.out.print(" - CO-REF: " + freqTable.get("4"));
			System.out.println(" - FIX&CO-REF: " + freqTable.get("0"));
			
		}
		
	}

}
