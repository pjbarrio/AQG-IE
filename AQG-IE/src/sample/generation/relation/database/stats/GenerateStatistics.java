package sample.generation.relation.database.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;

import org.apache.commons.io.FileUtils;

import sample.generation.relation.database.TuplesEval;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import weka.attributeSelection.ASEvaluation;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Database;
import exploration.model.DocumentHandle;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;

public class GenerateStatistics {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		
ASEvaluation eval = new TuplesEval();
		
		String versionName = "INDEPENDENT";
		
		String collection = "TREC";
		
		int workloadModelBase = 6;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String[][] databases = {{"http://www.mauconline.net/","bootstrappedSample"},{"http://www.carmeuse.com","bootstrappedSample"},{"http://diversifiedproduct.com/","bootstrappedSample"},
				{"http://joehollywood.com/","bootstrappedSample"},{"http://northeasteden.blogspot.com/","bootstrappedSample"},
				{"http://www.paljorpublications.com/","bootstrappedSample"},{"http://www.brannan.co.uk/","bootstrappedSample"},{"http://www.improv.ca/","bootstrappedSample"},{"http://www.avclub.com/","boostrappedSample"},
				{"http://www.shopcell.com/","bootstrappedSample"},{"http://www.123aspx.com/","bootstrappedSample"},{"http://www.infoaxon.com/","bootstrappedSample"},
				{"http://www.thecampussocialite.com/","bootstrappedSample"},{"http://www.time.com/","bootstrappedSample"},{"http://www.ddj.com/","bootstrappedSample"},{"http://www.biostat.washington.edu/","bootstrappedSample"},{"http://micro.magnet.fsu.edu/","bootstrappedSample"},
				{"http://www.worldenergy.org","bootstrappedSample"},{"http://travel.state.gov/","bootstrappedSample"},{"http://www.aminet.net/","bootstrappedSample"},{"http://www.codecranker.com/","bootstrappedSample"},{"http://www.eulerhermes.com/","bootstrappedSample"},{"http://www.pokkadots.com/","boostrappedSample"},
				{"http://www.muffslap.com/","bootstrappedSample"},{"http://keep-racing.de","bootstrappedSample"},{"http://www.canf.org/","bootstrappedSample"},{"http://www.jamesandjames.com","bootstrappedSample"}};

		String[][] relationses = {{"PersonCareer"},{"NaturalDisaster"},{"ManMadeDisaster"},{"Indictment"},{"Arrest"},{"Trial"},{"PersonTravel"},
				{"VotingResult"},{"ProductIssues"},{"Quotation"},{"PollsResult"}};
		
		int size = 20000;

		int[] spl = {1/*,2,3,4,5,6,7,8,9,10*/};
		
		boolean[] tuplesAsStopWords = {true/*,false*/};
		
		for (int rel = 0; rel < relationses.length; rel++){
			
			int workloadModelId = workloadModelBase + rel;
			
			WorkloadModel model = pW.getWorkloadModel(workloadModelId);

			Version version = Version.generateInstance(versionName, model);
			
			int total = 0;
			
			int nonZero = 0;
			
			for (int i = 0; i < databases.length; i++){

				Database database = pW.getDatabaseByName(databases[i][0]);
				
					for (int sp = 0; sp < spl.length; sp++) {

						int realsize = size * spl[sp];

						File tupls = pW.getForTrainingMatchingTuplesWithSourcesFile(database, version, model,eval);
						
						if (!tupls.exists())
							continue;
						
						List<String> lines = FileUtils.readLines(tupls);
					
						total += lines.size();
						
						if (!lines.isEmpty())
							nonZero++;
						
					}
				
			}
			
			System.out.println("Relation: " + relationses[rel][0] + "  - Total: " + total + " - NonZero: " + nonZero);
			
		}
		

	}

}
