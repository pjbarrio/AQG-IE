package sample.generation.relation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.RDFRelationExtractor;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;

import utils.document.DocumentHandler;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.SerializationHelper;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;

public class GenerateSplitForRelSys {

	private static final int ERROR = 0;
	private static final int USEFUL = 1;
	private static final int USELESS = -1;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		
		
		//First Algorithm of the sequence. Detects Useful Documents. Continue to GenerateDocumentList

		int ieSystem = Integer.valueOf(args[0]); //17 to 20

		int relationExperiment = Integer.valueOf(args[1]);

		String collection = args[2];//"TREC";

		int db = Integer.valueOf(args[3]); //3000

		boolean hasAll = Boolean.valueOf(args[4]);

		int belowSplit = Integer.valueOf(args[5]);

		boolean cached = Boolean.valueOf(args[6]);
		
		boolean takeCareOfSaving = Boolean.valueOf(args[7]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();

		//		String[] relations = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","PersonTravel",
		//				"VotingResult","Indictment-Arrest-Trial"};

		String relationExtracted = RelationConfiguration.getRelationName(relationExperiment);

		String[] relationsExtracted = new String[]{RelationConfiguration.getRelationName(relationExperiment)};

		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment,pW,ieSystem,cached,takeCareOfSaving,db,new SgmlContentExtraction());

		Database datb = pW.getDatabaseById(db);

		DocumentHandler dh = new DocumentHandler(datb, 1, pW);

		List<Document> documents = null;

		List<String> usefulFiles = new ArrayList<String>();

		List<String> uselessFiles = new ArrayList<String>();


		if (hasAll){
			documents = new ArrayList<Document>(dh.getDocuments());
		}else{

			List<Long> docIds = pW.getDocumentsBelowSplit(db,pW.getExperiment(RelationConfiguration.getRelationConf(relationExperiment),RelationConfiguration.getInformationExtractionBaseIdFromTuples(ieSystem)),belowSplit);

			documents = new ArrayList<Document>(docIds.size());

			for (int i = 0; i < docIds.size(); i++) {
				documents.add(dh.getDocument(datb,docIds.get(i)));
			}

			List<Long> useless = pW.getNotContainingCandidateSentence(db,RelationConfiguration.getRelationConf(relationExperiment));

			for (int i = 0; i < useless.size(); i++) {
				uselessFiles.add(dh.getDocument(datb,useless.get(i)).getFilePath(pW).getAbsolutePath());
			}

		}



		//		for (int rel = 0; rel < relations.length; rel++) {


		int processed = 0;

//		BufferedWriter bw = new BufferedWriter(new FileWriter(pW.getMatchingTuplesWithSources(collection,relationExtracted,tr.getName())));

		Map<String,List<Tuple>> map = new HashMap<String, List<Tuple>>();
		
		for (Document doc : documents) {

			processed++;

			if (processed % 1000 == 0)
				System.out.println("Processed: " + processed);

			Tuple[] tups = tr.execute(doc,relationsExtracted);

			if (tups.length > 0){
				usefulFiles.add(doc.getFilePath(pW).getAbsolutePath());
				
				List<Tuple> t = new ArrayList<Tuple>(tups.length);
				
				for (int i = 0; i < tups.length; i++) {
//					bw.write(doc.getId() + "," + tups[i].toString());
//					bw.newLine();
					t.add(tups[i]);
				}
				map.put(doc.getFilePath(pW).getAbsolutePath(), t);
			}else{
				uselessFiles.add(doc.getFilePath(pW).getAbsolutePath());
			}

		}

		SerializationHelper.write("/proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/extractions/" + relationExtracted + "." + RelationConfiguration.getExtractorName(ieSystem)+ "." + collection, map);
		
//		bw.close();
//
//		File useful = new File(pW.getUsefulDocumentsForCollection(collection,relationExtracted,tr.getName()));
//
//		File useless = new File(pW.getUselessDocumentsForCollection(collection,relationExtracted,tr.getName()));
//
//		FileUtils.writeLines(useful, usefulFiles);
//
//		FileUtils.writeLines(useless, uselessFiles);


		//		}


	}

}