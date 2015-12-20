package sample.generation.relation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import execution.workload.tuple.Tuple;
import extraction.relationExtraction.RelationExtractionSystem;

import sample.AttributeTailoring.In_FrequentRemovalFilter;
import utils.arff.myArffHandler;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.SparseToNonSparse;

public class ReduceArffSample {


	public static void main(String[] args) throws Exception {
		
		//Forth Algorithm. Continuation of ArffGenerator. 
		
		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
		
		int db = Integer.valueOf(args[3]); //3000
		
		int size = Integer.valueOf(args[4]);
		
		int[] spl = {Integer.valueOf(args[5])};//{1,2,3,4,5/*,6,7,8,9,10*/};

		persistentWriter pW = PersistenceImplementation.getWriter();
		
//		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
//				"VotingResult","ProductIssues","Quotation","PollsResult",*/"Indictment-Arrest-Trial"};
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment, pW, ieSystem, true, false, db, new SgmlContentExtraction());

		
		double minFrequency = 0.003;
		
		double maxFrequency = 0.9;
		
//		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
//				"VotingResult","ProductIssues","Quotation","PollsResult"*/"Indictment-Arrest-Trial"};
		
		String[] relations = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		int splits = 1;
		
		
		boolean[] tuplesAsStopWords = {Boolean.valueOf(args[6])};//{true,false};
		
		for (int rel = 0; rel < relations.length; rel++) {
			
			for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {
				
				for (int a=0;a<spl.length; a++) {
					
					System.gc();
					
					int j = spl[a];
					
					System.out.println("Split: " + j);
					
					for (int i = 1; i <= splits; i++) {
						
						int realsize = size * i;
						
						System.out.println("Size: " + realsize);
						
						File input = new File(pW.getArffModelForSplit(collection,relations[rel],realsize,j,tuplesAsStopWords[tasw],tr.getName()));
						
						File output = new File(pW.getReducedArffModelForSplit(collection,relations[rel],realsize,j,tuplesAsStopWords[tasw],tr.getName()));
							
						if (output.exists())
							continue;
							
						reduce(input,output,minFrequency,maxFrequency);
						
					}
					
					
				}

			}

			
		}
		
		
	}

	private static void reduce(File input, File output, double minFrequency,
			double maxFrequency) throws Exception {
		
		Instances data = myArffHandler.loadInstances(input.getAbsolutePath());

		In_FrequentRemovalFilter ifr = new In_FrequentRemovalFilter();
		
		ifr.setMinFrequencyvalue(minFrequency);
		ifr.setMaxFrequencyvalue(maxFrequency);
		
		ifr.setInputFormat(data);
		
		Instances res = ifr.process(data);
				
		myArffHandler.saveInstances(output.getAbsolutePath(), res);
		
		
	}
	
}
