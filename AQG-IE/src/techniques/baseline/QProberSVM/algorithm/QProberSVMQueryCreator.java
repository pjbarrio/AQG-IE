package techniques.baseline.QProberSVM.algorithm;
import java.util.Hashtable;

import sample.generation.model.impl.DummySampleConfiguration;
import techniques.algorithms.QProber;
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


public class QProberSVMQueryCreator {

	public static Hashtable<String, Double> weights;
	public static Hashtable<Integer,Double> weightsSVM;
	private static Instances data;
	private static double minSVMSupport;
	private static Integer min_supp;
	private static Instances dataTrue;
	private static Integer min_supp_after_update;
	private static persistentWriter pW;


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int[] sample_configuration = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};

//		String[][] database = {{"Bloomberg","randomSample"},{"TheCelebrityCafe","randomSample"},{"TheEconomist","randomSample"},{"UsNews","randomSample"},{"Variety","randomSample"},{"GlobalSample","Mixed"}};
		
		String[][] database = {{"http://www.mauconline.net/","bootstrappedSample"},{"http://www.carmeuse.com","bootstrappedSample"},{"http://diversifiedproduct.com/","bootstrappedSample"},
				{"http://joehollywood.com/","bootstrappedSample"},{"http://sociologically.net","bootstrappedSample"},{"http://northeasteden.blogspot.com/","bootstrappedSample"},
				{"http://www.paljorpublications.com/","bootstrappedSample"},{"http://www.brannan.co.uk/","bootstrappedSample"},{"http://www.improv.ca/","bootstrappedSample"},{"http://www.avclub.com/","boostrappedSample"},
				{"http://www.shopcell.com/","bootstrappedSample"},{"http://www.123aspx.com/","bootstrappedSample"},{"http://www.infoaxon.com/","bootstrappedSample"},
				{"http://www.thecampussocialite.com/","bootstrappedSample"},{"http://www.time.com/","bootstrappedSample"},{"GlobalSample-PersonCareer","Mixed"},{"News-PersonCareer-Category","ClusterMixed-Category"},{"Sports-PersonCareer-Category","ClusterMixed-Category"},
				{"Science-PersonCareer-Category","ClusterMixed-Category"},{"Games-PersonCareer-Category","ClusterMixed-Category"},{"Reference-PersonCareer-Category","ClusterMixed-Category"},
				{"Recreation-PersonCareer-Category","ClusterMixed-Category"},{"Arts-PersonCareer-Category","ClusterMixed-Category"},
				{"Health-PersonCareer-Category","ClusterMixed-Category"}}; 
		
		String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/};
		
		int[] workload = {/*1,2,3,4,5*/6};
		
		int[] sample_number = {1/*,2,3,4,5*/};
		
		pW = PersistenceImplementation.getWriter();
		
		for (int sconf = 0; sconf < sample_configuration.length; sconf++) {
			
			for (int s = 0; s < sample_number.length; s++) {
				
				for (int w = 0; w < workload.length; w++) {
					
					for (int j = 0; j < database.length; j++) {
						
						Database dbase = pW.getDatabaseByName(database[j][0]);
						
						for (int i = 0; i < version.length; i++) {
							
							args = new String[10];
							
							Sample sample = null;
							
//XXX						Sample sample = Sample.getSample(dbase,new DummyVersion(version[i]),new DummyWorkload(workload[w]),sample_number[s],new DummySampleConfiguration(sample_configuration[sconf]));
							
							args[1] = pW.getArffBooleanModel(sample);
							
							data = myArffHandler.loadInstances(args[1]);
							
//							args[2] = "4"; //threshold reduction global
							
							args[2] = Params.getThresholdReduction(database[j][0]);
							
//							args[2] = "2"; //threshold reduction
//							minSVMSupport = thresholds[j*(version.length) + i] / Double.valueOf(args[2]);
							
							minSVMSupport = Params.getSVMThreshold(pW.getSMOWekaOutput(sample,null));
							
							args[3] = "0.0"; //min Support
							
							double epsilon = Double.parseDouble(args[3]);
							
//							args[4] = "9"; //min_combination global.
							
							args[4] = Params.getMinSupport(database[j][0]);
							
//							args[4] = "3"; //min_combination.
							min_supp = Integer.valueOf(args[4]);
									
//							args[5] = "0.5";
							
							args[5] = Params.getPrecision(database[j][0]);
							
							double min_precision = Double.valueOf(args[5]);
							
							args[7] = pW.getArffBooleanTrueModel(sample);
							
							dataTrue = myArffHandler.loadInstances(args[7]);
							
//							args[8] = "3"; //Min after update global.
							
							args[8] = Params.getMinSupportAfterUpdate(database[j][0]);
							
//							args[8] = "2"; //Min after update.
							min_supp_after_update = Integer.valueOf(args[8]);
											
//							args[9] = "3"; //Max Query Size
							
							args[9] = Params.getMaxQuerySize(database[j][0]);
							
							int max_query_size = Integer.valueOf(args[9]);
							
							new QProber(sample, max_query_size, min_supp, min_supp_after_update,minSVMSupport,epsilon,min_precision,dataTrue).executeAlgorithm(data,pW,null);
							
							Runtime.getRuntime().gc();
							
						}
						
					}					
					
				}
				
			}

			
		}
		
	}

}
