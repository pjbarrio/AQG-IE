package sample.generation.relation.database.stats;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class GenerateCollectionsStatistics {

	public static void main(String[] args) throws IOException {
	
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		
		String[] relations = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
				"VotingResult","ProductIssues","Quotation","PollsResult"};
		
		int splits = 1;
		
		int size = 20000;		
		
		for (int rel = 0; rel < relations.length; rel++) {
			
			File useful = new File(pW.getUsefulDocumentsForCollection(collection,relations[rel]));
			
			List<String> usefulFiles = FileUtils.readLines(useful);
			
			System.out.println(relations[rel] + ": " + usefulFiles.size());
			
		}
				
		
		
	}
	
}
