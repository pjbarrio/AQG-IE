package techniques.newideas.SVM.algorithm;
import java.util.Hashtable;

import sample.generation.model.impl.DummySampleConfiguration;
import techniques.algorithms.Optimistic;
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


public class SVMQueryCreatorReloaded {

	public static Hashtable<String, Double> weights;
	public static Hashtable<Integer, Double> weightsSVM;
	private static Instances data;
	private static double supp;
	private static Double min_precision;
	private static Integer min_supp;
	private static Instances dataTrue;
	private static Integer min_support_after_update;
	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int[] sample_configuration = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};

		
		String[][] database = {{"http://www.mauconline.net/","bootstrappedSample"},{"http://www.carmeuse.com","bootstrappedSample"},{"http://diversifiedproduct.com/","bootstrappedSample"},
				{"http://joehollywood.com/","bootstrappedSample"},{"http://sociologically.net","bootstrappedSample"},{"http://northeasteden.blogspot.com/","bootstrappedSample"},
				{"http://www.paljorpublications.com/","bootstrappedSample"},{"http://www.brannan.co.uk/","bootstrappedSample"},{"http://www.improv.ca/","bootstrappedSample"},{"http://www.avclub.com/","boostrappedSample"},
				{"http://www.shopcell.com/","bootstrappedSample"},{"http://www.123aspx.com/","bootstrappedSample"},{"http://www.infoaxon.com/","bootstrappedSample"},
				{"http://www.thecampussocialite.com/","bootstrappedSample"},{"http://www.time.com/","bootstrappedSample"},{"GlobalSample-PersonCareer","Mixed"},{"News-PersonCareer-Category","ClusterMixed-Category"},{"Sports-PersonCareer-Category","ClusterMixed-Category"},
				{"Science-PersonCareer-Category","ClusterMixed-Category"},{"Games-PersonCareer-Category","ClusterMixed-Category"},{"Reference-PersonCareer-Category","ClusterMixed-Category"},
				{"Recreation-PersonCareer-Category","ClusterMixed-Category"},{"Arts-PersonCareer-Category","ClusterMixed-Category"},
				{"Health-PersonCareer-Category","ClusterMixed-Category"}}; 
		
		String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/};
		
		int[] workload = {/*1,2,3,4,5,*/6};
		
		int[] sample_number = {1/*,2,3,4,5*/};
		
		pW = PersistenceImplementation.getWriter();
		
		for (int sconf = 0; sconf < sample_configuration.length; sconf++) {
			
			for (int s = 0; s < sample_number.length; s++) {
				
				for (int w = 0; w < workload.length; w++) {
					
					for (int j = 0; j < database.length; j++) {
						
						Database dbase = pW.getDatabaseByName(database[j][0]);
						
						for (int i = 0; i < version.length; i++) {
							
							args = new String[12];
							
							Sample sample = null;
							
//							Sample sample = Sample.getSample(dbase,new DummyVersion(version[i]),new DummyWorkload(workload[w]),sample_number[s],new DummySampleConfiguration(sample_configuration[sconf]));
							
							args[1] = pW.getArffBooleanModel(sample,null);
							data = loadDataInstances(args[1]);
							
							args[2] = pW.getArffBooleanTrueModel(sample);
							dataTrue = loadDataInstances(args[2]);
											
//							args[3] = "0.85"; //threshold lost
							
							args[3] = Params.getThresholdLost(database[j][0]);
							
							double threshold = Double.valueOf(args[3]);  
							
//							args[4] = "9"; //min_support global
							
							args[4] = Params.getMinSupport(database[j][0]);
							
//							args[4] = "3"; //min_support
							min_supp = Integer.valueOf(args[4]);
							
//							args[5] = "4"; //threshold reduction global
							
							args[5] = Params.getThresholdReduction(database[j][0]);
							
//							args[5] = "2"; //threshold reduction
							
//							supp = thresholds[j*(version.length) + i] / Double.valueOf(args[5]);
							
							supp = Params.getSVMThreshold(pW.getSMOWekaOutput(sample,null));
							
							args[6] = "0.0"; //min weight of features to be used. 438
							double min_weight = Double.valueOf(args[6]);
							
//							args[8] = "3"; // min_support after updating global.

							args[8] = Params.getMinSupportAfterUpdate(database[j][0]);
							
//							args[8] = "2"; // min_support after updating.
							min_support_after_update = Integer.valueOf(args[8]);
							
//							args[9] = "0.5"; //min_precision
							
							args[9] = Params.getPrecision(database[j][0]);
							min_precision = Double.valueOf(args[9]);
							
							args[10] = Params.getMaxQuerySize(database[j][0]);
							
//							args[10] = "3"; //number of terms
							int max_query_size = Integer.valueOf(args[10]);
							
							new Optimistic(sample, max_query_size, min_supp, min_support_after_update,threshold,min_weight,min_precision,supp,dataTrue).executeAlgorithm(data,pW,null);
											
							Runtime.getRuntime().gc();
							
						}
						
					}
					
				}
				
			}
			
		}
		
		
		
		
			
	}

	private static Instances loadDataInstances(String arffFile) throws Exception {
		
		return myArffHandler.loadInstances(arffFile);
		
	}

}
