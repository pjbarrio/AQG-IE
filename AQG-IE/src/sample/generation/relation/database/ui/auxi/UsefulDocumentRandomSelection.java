package sample.generation.relation.database.ui.auxi;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class UsefulDocumentRandomSelection {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		
		String[] relations = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
				"VotingResult","ProductIssues","Quotation","PollsResult"};

		int selectedNumber = 650;
		
		for (int i = 0; i < relations.length; i++) {
			
			File usefuls = new File(pW.getUsefulDocumentsForCollection(collection, relations[i]));
			
			List<String> files = FileUtils.readLines(usefuls);
			
			System.out.println(relations[i] + " - " + files.size());
			
			File selectedUsefuls = new File(pW.getSelectedUsefulDocumentsForCollection(collection, relations[i], selectedNumber));
			
			Collections.shuffle(files);
			
			List<String> selectedFiles = files.subList(0, selectedNumber);
			
			System.out.println(relations[i] + " - " + selectedFiles.size());
			
			FileUtils.writeLines(selectedUsefuls, selectedFiles);
			
		}
		
	}

}
