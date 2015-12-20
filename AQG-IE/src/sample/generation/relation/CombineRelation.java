package sample.generation.relation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import execution.workload.tuple.Tuple;
import extraction.relationExtraction.impl.RDFRelationExtractor;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class CombineRelation {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		//First Algorithm of the sequence (After Generate Split). Detects Useful Documents. Continue to GenerateDocumentList

		persistentWriter pW = PersistenceImplementation.getWriter();

		String collection = "TREC";

		String[] combined = new String[]{"Indictment-Arrest-Trial"};
		
		String[][] rels = new String[][]{{"Indictment","Arrest","Trial"}};

		for (int rel = 0; rel < combined.length; rel++) {

			Set<String> usefulFiles = new HashSet<String>();

			Set<String> uselessFiles = new HashSet<String>();
			
			List<String> mswt = new ArrayList<String>();
			
			for (int i = 0; i < rels[rel].length; i++) {
				
				usefulFiles.addAll(FileUtils.readLines(new File(pW.getUsefulDocumentsForCollection(collection,rels[rel][i]))));
				
				uselessFiles.addAll(FileUtils.readLines(new File(pW.getUselessDocumentsForCollection(collection,rels[rel][i]))));
				
				mswt.addAll(FileUtils.readLines(new File(pW.getMatchingTuplesWithSources(collection, rels[rel][i]))));
				
			}			
			
			uselessFiles.removeAll(usefulFiles);
			
			File useful = new File(pW.getUsefulDocumentsForCollection(collection,combined[rel]));

			File useless = new File(pW.getUselessDocumentsForCollection(collection,combined[rel]));

			File matchingsWithT = new File(pW.getMatchingTuplesWithSources(collection,combined[rel]));
			
			FileUtils.writeLines(useful, usefulFiles,true);

			FileUtils.writeLines(useless, uselessFiles,true);

			FileUtils.writeLines(matchingsWithT, mswt,true);

		}


	}

}
