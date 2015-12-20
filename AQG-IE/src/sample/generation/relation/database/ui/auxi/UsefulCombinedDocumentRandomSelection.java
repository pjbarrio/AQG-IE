package sample.generation.relation.database.ui.auxi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class UsefulCombinedDocumentRandomSelection {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		
		String[][] relations = new String[][]{{"Indictment","Arrest","Trial"},{"VotingResult","PollsResult"}};

		int selectedNumber = 650;
		
		for (int i = 0; i < relations.length; i++) {
			
			Set<String> fileset = new HashSet<String>();
			
			for (int j = 0; j < relations[i].length; j++) {
				
				File usefuls = new File(pW.getUsefulDocumentsForCollection(collection, relations[i][j]));
				
				List<String> tmp = FileUtils.readLines(usefuls);
				
				System.out.println(relations[i][j] + " - " + tmp.size());
				
				fileset.addAll(tmp);
				
			}
			
			System.out.println(Arrays.toString(relations[i]) + " - " + fileset.size());
			
			File selectedUsefuls = new File(pW.getSelectedUsefulDocumentsForCollection(collection, getName(relations[i]), selectedNumber));
			
			List<String> files = new ArrayList<String>(fileset);
			
			Collections.shuffle(files);
			
			List<String> selectedFiles = files.subList(0, selectedNumber);
			
			System.out.println(Arrays.toString(relations[i]) + " - " + selectedFiles.size());
			
			FileUtils.writeLines(selectedUsefuls, selectedFiles);
			
		}
		
	}

	private static String getName(String[] relations) {
		
		String name = relations[0];
		
		for (int i = 1; i < relations.length; i++) {
			
			name += "-" + relations[i];
			
		}
		
		return name;
		
	}

}
