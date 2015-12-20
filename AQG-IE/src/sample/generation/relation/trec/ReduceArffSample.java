package sample.generation.relation.trec;

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
		
		int first = Integer.valueOf(args[0]);
		int last = Integer.valueOf(args[1]);
		
		String extractor = "OpenCalais";
		
		String collection = "TREC";
		
		int size = 5000;
		
		int[] spl = {1,2,3,4,5};//{1,2,3,4,5/*,6,7,8,9,10*/};
		
		boolean[] tuplesAsStopWords = {false};// {Boolean.valueOf(args[6])};//{true,false};

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		double minFrequency = 0.003;
		
		double maxFrequency = 0.9;
		
		int splits = 1;
		
		for (int rel = first; rel <= Math.min(last,GenerateDocumentsList.relations.length); rel++) {
			
			for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {
				
				for (int a=0;a<spl.length; a++) {
					
					System.gc();
					
					int j = spl[a];
					
					System.out.println("Split: " + j);
					
					for (int i = 1; i <= splits; i++) {
						
						int realsize = size * i;
						
						System.out.println("Size: " + realsize);
						
						File input = new File(pW.getArffModelForSplit(collection,GenerateDocumentsList.relations[rel],realsize,j,tuplesAsStopWords[tasw],extractor));
						
						File output = new File(pW.getReducedArffModelForSplit(collection,GenerateDocumentsList.relations[rel],realsize,j,tuplesAsStopWords[tasw],extractor));
							
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
