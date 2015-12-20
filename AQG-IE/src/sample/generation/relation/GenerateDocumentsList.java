package sample.generation.relation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;
import extraction.relationExtraction.RelationExtractionSystem;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class GenerateDocumentsList {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//Second algorithm in the sequence. Continuation of Generate Split.
		
		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
		
		int db = Integer.valueOf(args[3]); //3000
		
		int size = Integer.valueOf(args[4]);		
		
		boolean hasAll = Boolean.valueOf(args[5]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
//		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
//				"VotingResult","ProductIssues","Quotation","PollsResult",*/"Indictment-Arrest-Trial"};
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment, pW, ieSystem, true, false, db, new SgmlContentExtraction());
		
		String[] relations = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		int splits = 1;
		
		int alts = 5;
		
		for (int rel = 0; rel < relations.length; rel++) {
			
			File useful = new File(pW.getUsefulDocumentsForCollection(collection,relations[rel],tr.getName()));
			
			List<String> usefulFiles = FileUtils.readLines(useful);
			
			File useless = new File(pW.getUselessDocumentsForCollection(collection,relations[rel],tr.getName()));

			List<String> uselessFiles = FileUtils.readLines(useless);
			
			for (int j = 1; j <= alts; j++) {
				
				System.out.println("Split: " + j);
				
				Collections.shuffle(usefulFiles);
				
				Collections.shuffle(uselessFiles);
				
				for (int i = 1; i <= splits; i++) {
					
					int realsize = size;
					
					System.out.println("Size: " + realsize);
					
					String usefulSplit = pW.getUsefulDocumentExtractionForRelation(collection,relations[rel],realsize,j,tr.getName());
					
					String uselessSplit = pW.getUselessDocumentExtractionForRelation(collection,relations[rel],realsize,j,tr.getName());
								
					FileUtils.writeLines(new File(usefulSplit), usefulFiles.subList(0, Math.min(realsize, usefulFiles.size())));
					
					FileUtils.writeLines(new File(uselessSplit), uselessFiles.subList(0, Math.min(realsize, usefulFiles.size())));
					
					if (!hasAll){ //it will remove the ones that have been used. Because of probabities of choosing the same being low.
					
						usefulFiles = usefulFiles.subList(Math.min(realsize, usefulFiles.size()), usefulFiles.size());

					}
				}
				
			}				

			
		}
		
		
	}

}
