package techniques.newideas.Incremental.algorithm;

import sample.generation.model.impl.DummySampleConfiguration;
import techniques.algorithms.Incremental;
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

public class IncrementalProbabilistic {

	/**
	 * Uses: Bi-Nomial Separation: An Extensive Empirical Study of Feature Selection Metrics for Text Classification
	 */
	
	private static Instances instances;
	private static Double wfp;
	private static persistentWriter pW;
	/**
	 * @param args
	 * @param timeFile 
	 * @param beta 
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
		
		int[] workload = {/*1,2,3,4,5*/6};
		
		int[] sample_number = {1/*,2,3,4,5*/};
		
		pW = PersistenceImplementation.getWriter();
		
		for (int sconf = 0; sconf < sample_configuration.length; sconf++) {
			
			for (int s = 0; s < sample_number.length; s++) {
				
				for (int w = 0; w < workload.length; w++) {
					
					for (int i = 0; i < database.length; i++) {
						
						Database dbase = pW.getDatabaseByName(database[i][0]);
						
						for (int j = 0; j < version.length; j++) {
							
							args = new String[8];
							
							Sample sample = null;
							
//XXX							Sample sample = Sample.getSample(dbase,new DummyVersion(version[j]),new DummyWorkload(workload[w]),sample_number[s],new DummySampleConfiguration(sample_configuration[sconf]));
							
							args[0] = pW.getArffBooleanModel(sample);
							
//							args[1] = "3"; //Max Size of queries
							
							args[1] = Params.getMaxQuerySize(database[j][0]);
							
							int max_query_size = Integer.valueOf(args[1]);
							
//							args[2] = "9"; //Min Support Global

							args[2] = Params.getMinSupport(database[j][0]);
							
//							args[2] = "3"; //Min Support
							
							int min_support = Integer.valueOf(args[2]);
							
							args[3] = Params.getPerformanceThreshold(database[j][0]);
							
//							args[3] = "0.75"; //Performance threshold
							
							double threshold = Double.valueOf(args[3]);
							
//							args[4] = "3"; //Min after update global
							
							args[4] = Params.getMinSupportAfterUpdate(database[j][0]);
							
//							args[4] = "2"; //Min after update

							
							int min_supp_after_update = Integer.valueOf(args[4]);
							
//							args[5] = "0.85"; //FP
							
							args[5] = Params.getFPIncremental(database[j][0]);
							
							wfp = Double.valueOf(args[5]);
							
//							args[6] = "0.5"; //Beta Efficiency
					
							args[6] = Params.getBetaEfficiency(database[j][0]);
							
							double betaE = Double.valueOf(args[6]);

							instances = myArffHandler.loadInstances(args[0]);
							
							new Incremental(sample,  max_query_size,min_support,min_supp_after_update,threshold,wfp,betaE).executeAlgorithm(instances,pW,null);
							
							Runtime.getRuntime().gc();				
						
						}
						
					}
					
				}
				
			}
			
		}
		
		
		
		
		
		
	
	}


	
}
