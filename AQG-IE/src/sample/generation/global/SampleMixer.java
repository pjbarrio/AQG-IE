package sample.generation.global;

import java.io.File;
import java.util.List;
import java.util.Map;

import exploration.model.Database;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentHandler.contentLoader.impl.FileContentLoader;

import sample.generation.combination.CombinationGenerator;
import sample.generation.combination.impl.ClusterCombination;
import sample.generation.combination.impl.GlobalCombination;
import sample.generation.combining.CombinedSampleGeneratorThread;
import utils.id.DatabaseIndexHandler;
import utils.persistence.databaseWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;

public class SampleMixer {


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String type = "allPaper";
		
		String nofilteringFields = "";
		
		databaseWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");

		String name = "Category";
		
		ContentExtractor ce = new TikaContentExtractor();
		
		ContentLoader cl = new FileContentLoader();
		
//		Map<String, Integer> index = DatabaseIndexHandler.loadDatabaseIndex(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt"));

		int split = 88;
		
//		CombinationGenerator cg = new ClusterCombination(pW,new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/SampleGeneration/Clusters/"+name+"/" + type),name);

		
		//When using global, the index is not necesary.
		CombinationGenerator cg = new GlobalCombination(pW,split);
		
		int thread = 0;
				
		int firstId = 2854; //Global
		
		int limit = cg.numberOfCombinations();
		
		int start = 0;
		
		WordExtractorAbs usefulWE = new WordExtractor(ce,cl);
		
		WordExtractorAbs generalWE = new WordExtractor(ce,cl);
		
		for (int i = start; i < limit; i++) {

			List<Database> database = cg.getCombination(i);

			String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/};

			String[] combined = new String[2];
			
			combined[0] = cg.getDatabaseName(i);
			
			combined[1] = cg.getSampleType(i);

			boolean frequency = false;

			boolean stemmed = false;

			int[] relation = {-1};
			
			int[] workload = {-1/*17,18,19,20,21,22*/};

			int[] sample_number = {-1/*1,2,3,4,5*/};

			int[] relationConf = {-1};
			
			int[] informationExtractionSystem = {-1};
			
			for (int ie = 0; ie < informationExtractionSystem.length; ie++) {
				
				for (int rel = 0; rel < relation.length; rel++) {
					
					for (int rc = 0; rc < relationConf.length; rc++) {
						
						for (int w = 0; w < workload.length; w++) {
	
							for (int k = 0; k < sample_number.length ; k++){
	
								for (int j = 0; j < version.length; j++) {
	
	//								startCombinedSample(workload[w],version[j],sample_number[k],combined[0],database,pW, thread, frequency, stemmed, relation, nofilteringFields,usefulWE,generalWE);
	
									pW.insertDatabase(firstId, combined[0], -1, "", combined[1], 1, 0, "", cg.isGlobal(), cg.isCluster(),0);
									
									for (int comb = 0; comb < database.size(); comb++) {
										
										pW.insertClusteredDatabase(firstId,database.get(comb).getId(),cg.getClusteredFunction(),1,sample_number[k],relation[rel],workload[w],relationConf[rc],informationExtractionSystem[ie]);
										
									}
									
									firstId++;
									
									thread++;
								}
	
							}
	
	
						}
						
					}

				}
				
			}
			
			

			System.out.println("COMBINATION: " + i + " - " + combined[0]);
						
		}
	}

	private static void startCombinedSample(int workload,String version, int sample_number, String databaseName, String[] database, databaseWriter pW, int thread, boolean frequency, boolean stemmed, String relation, String nofilteringFields, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) throws Exception {

		//		new GlobalSampleGeneratorThread(workload,version,sample_number,databaseName,database,pW, thread).run();		

		Thread t = new Thread(new CombinedSampleGeneratorThread(workload,version,sample_number,databaseName,database,pW, thread, relation, frequency, stemmed,nofilteringFields,usefulWE,generalWE));

		t.start();
		
//		t.join();
		
	}	

}
