package sample.generation.qxtract;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;
import extraction.relationExtraction.RelationExtractionSystem;

import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.SparseToNonSparse;

public class GenerateRules {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
		
		int db = Integer.valueOf(args[3]); //3000
		
		int size = Integer.valueOf(args[4]);
		
		boolean[] tuplesAsStopWords = new boolean[]{Boolean.valueOf(args[5])};//{true,false};
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
//		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
//				"VotingResult","ProductIssues","Quotation","PollsResult",*/"Indictment-Arrest-Trial"};
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment, pW, ieSystem, true, false, db, new SgmlContentExtraction());

		int relationConf = RelationConfiguration.getRelationConf(relationExperiment);
		
		String[] relations = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		int[] workload = new int[]{RelationConfiguration.getWorkload(relationExperiment)};
		
	    Classifier classifier = new JRip();         // new instance of tree
	    
	    int[] spl = {1,2,3,4,5/*,6,7,8,9,10*/};
		
		int[] sizes = {1/*,2,3,4,5,6,7,8,9,10*/};
		
		
		
		for (int rel = 0; rel < relations.length; rel++) {

			String relation = relations[rel];
			
			for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {
				
				for (int a=0;a<spl.length; a++) {
					
					System.gc();
					
					int j = spl[a];
					
					System.out.println("Split: " + j);
					
					for (int b = 0; b < sizes.length; b++) {
						
						int i = sizes[b];
						
						int realsize = size * i;
						
						System.out.println("Size: " + realsize);
						
						File input = new File(pW.getReducedArffModelForSplit(collection,relation,realsize,j,tuplesAsStopWords[tasw],tr.getName()));
						
						if (input.exists()){
							
							File output = new File(pW.getRulesFromSplitModel(collection,relation,realsize,j,classifier,tuplesAsStopWords[tasw],tr.getName()));
							
							if (output.exists()){
								continue;
							}else{
								
								output.createNewFile();
								
								generateRules(input,output,classifier);
								
								pW.writeRulesFromSplitModel(collection,relationConf,workload[rel],1,realsize,j,tuplesAsStopWords[tasw],output.getAbsolutePath(),ieSystem);
								
							}
							
						}	
						
						
					}
					
					
				}
				
			}

			
		}
		
		


	}

	private static void generateRules(File input, File output,
			Classifier classifier) throws Exception {
		
		DataSource source = new DataSource(new FileInputStream(input));
		   
		Instances data = source.getDataSet();
		
		data.setClassIndex(0);
			
		myArffHandler.reduceUsefulNumberOfInstances(data,myArffHandler.noInstances(data)-1);
		
		Instances newData = myArffHandler.generateInstanceWithMissingValues(data,0);
				
		classifier.buildClassifier(newData);
		
		FileUtils.writeStringToFile(output, classifier.toString());
		
	}

}
