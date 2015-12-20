package techniques.baseline.Ripper.algorithm;

import sample.generation.model.impl.DummySampleConfiguration;
import techniques.algorithms.Ripper;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;


public class RipperAlgorithm {

	private static persistentWriter pW;
	private static Instances data;

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
		
		int[] workload = {/*1,2,3,4,5*/6};
		
		int[] sample_number = {1/*,2,3,4,5*/};
		
		pW = PersistenceImplementation.getWriter();
		
		for (int sconf = 0; sconf < sample_configuration.length; sconf++) {
			
			for (int s = 0; s < sample_number.length; s++) {
				
				for (int w = 0; w < workload.length; w++) {
					
					for (int i = 0; i < database.length; i++) {
						
						Database dbase = pW.getDatabaseByName(database[i][0]);
						
						for (int j = 0; j < version.length; j++) {

							Sample sample = null;
							
//XXX						Sample sample = Sample.getSample(dbase,new DummyVersion(version[j]),new DummyWorkload(workload[w]),sample_number[s],new DummySampleConfiguration(sample_configuration[sconf]));
							
							String arffFile = pW.getArffBooleanModel(sample,null);

							System.out.println(arffFile);
							
							data = myArffHandler.loadInstances(arffFile);
							
							int fold = 3;
							double minNo = 2.0;
							int optimizationRuns = 2;
							long seedValue = 1;
							boolean pruning = true;
							boolean checkErrorRate = true;
							
							data = myArffHandler.generateInstanceWithMissingValues(data, data.classIndex());
							
							new Ripper(sample, -1, -1, -1, fold, minNo, optimizationRuns, seedValue, pruning, checkErrorRate).executeAlgorithm(data,pW,null);
											
							Runtime.getRuntime().gc();
						}
					
					}
					
				}
				
			}
			
		}
		
		
		
		
		
		
	}

}
