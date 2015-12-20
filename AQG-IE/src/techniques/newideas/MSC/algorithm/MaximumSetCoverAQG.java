package techniques.newideas.MSC.algorithm;

import java.util.List;

import sample.generation.model.impl.DummySampleConfiguration;
import techniques.algorithms.MSC;
import techniques.input.Params;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;


public class MaximumSetCoverAQG {

	
	private static Instances instances;
	private static double minimum_support_SVM;

	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		pW = PersistenceImplementation.getWriter();
		
//		int[] sample_configuration = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
//
//		String[][] database = {{"http://www.mauconline.net/","bootstrappedSample"},{"http://www.carmeuse.com","bootstrappedSample"},{"http://diversifiedproduct.com/","bootstrappedSample"},
//				{"http://joehollywood.com/","bootstrappedSample"},{"http://sociologically.net","bootstrappedSample"},{"http://northeasteden.blogspot.com/","bootstrappedSample"},
//				{"http://www.paljorpublications.com/","bootstrappedSample"},{"http://www.brannan.co.uk/","bootstrappedSample"},{"http://www.improv.ca/","bootstrappedSample"},{"http://www.avclub.com/","boostrappedSample"},
//				{"http://www.shopcell.com/","bootstrappedSample"},{"http://www.123aspx.com/","bootstrappedSample"},{"http://www.infoaxon.com/","bootstrappedSample"},
//				{"http://www.thecampussocialite.com/","bootstrappedSample"},{"http://www.time.com/","bootstrappedSample"},{"GlobalSample-PersonCareer","Mixed"},{"News-PersonCareer-Category","ClusterMixed-Category"},{"Sports-PersonCareer-Category","ClusterMixed-Category"},
//				{"Science-PersonCareer-Category","ClusterMixed-Category"},{"Games-PersonCareer-Category","ClusterMixed-Category"},{"Reference-PersonCareer-Category","ClusterMixed-Category"},
//				{"Recreation-PersonCareer-Category","ClusterMixed-Category"},{"Arts-PersonCareer-Category","ClusterMixed-Category"},
//				{"Health-PersonCareer-Category","ClusterMixed-Category"}}; 
//		
//		String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/};
//		
//		int[] workload = {/*1,2,3,4,5,*/6};
//		
//		int[] sample_number = {1/*,2,3,4,5*/};
		
		String[] version = {"INDEPENDENT"};
		
		List<Integer> sample_configuration = pW.getActiveSampleConfigurationIds();
		
		List<Integer> workload = pW.getWorkloadIds();
		
		List<Database> database = pW.getSamplableDatabases(null);
		
		int[] sample_number = {1,2,3,4,5};
		
		for (int sconf = 0; sconf < sample_configuration.size(); sconf++) {
			
			for (int s = 0; s < sample_number.length; s++) {
				
				for (int w = 0; w < workload.size(); w++) {
					
					for (int j = 0; j < database.size(); j++) {
						
						Database dbase = database.get(j);
						
						for (int i = 0; i < version.length; i++) {

							args = new String[12];
							
							Sample sample = null;
							
//XXX						Sample sample= Sample.getSample(dbase,new DummyVersion(version[i]),new DummyWorkload(workload.get(w)),sample_number[s],new DummySampleConfiguration(sample_configuration.get(sconf)));
							
							args[0] = pW.getArffBooleanModel(sample,null); //Model
							
							args[1] = "0.5"; //Min Precision
							
							double min_precision = Double.valueOf(args[1]).doubleValue();

//							args[2] = "9"; //Min Support global
							
							args[2] = Params.getMinSupport(dbase.getName());
							
//							args[2] = "3"; //Min Support

							int minSupport = Integer.valueOf(args[2]);
							
//							args[3] = "4"; //Threshold reduction global
							
							args[3] = Params.getThresholdReduction(dbase.getName());
							
//							args[3] = "2"; //Threshold reduction

//							minimum_support_SVM = thresholds[j*(version.length) + i] / (double)Integer.valueOf(args[3]);

							minimum_support_SVM = Params.getSVMThreshold(pW.getSMOWekaOutput(sample,null));
							
//							args[4] = "3"; //min after update global.
							
							args[4] = Params.getMinSupportAfterUpdate(dbase.getName());
							
//							args[4] = "2"; //min after update.

							int min_after_update = Integer.valueOf(args[4]);
							
//							args[5] = "3"; //Max number of terms in query
							
							args[5] = Params.getMaxQuerySize(dbase.getName());
							
							int maxNumberOfTerms = Integer.valueOf(args[5]);

//							args[6] = "400"; //Max Number of queries to generate
							
							args[6] = Params.getMaxNumberOfQueries(dbase.getName());
							
							int K = Integer.valueOf(args[6]);
							
//							args[7] = "1"; //pow for benefit
							
							args[7] = Params.getPowerForBenefit(dbase.getName());
							
							double pow = Double.valueOf(args[7]);

							instances = loadElements(args[0]);
							
							new MSC(sample, maxNumberOfTerms, minSupport, min_after_update,min_precision,minimum_support_SVM,K,pow).executeAlgorithm(instances,pW,null);
							
							Runtime.getRuntime().gc();
							
						}
						
					}
					
				}
				
			}
			
		}
		
		
			
	}

	private static Instances loadElements(String arffFile) {
		return myArffHandler.loadInstances(arffFile);
	}

	
}
