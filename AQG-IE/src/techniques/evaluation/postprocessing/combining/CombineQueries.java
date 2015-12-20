package techniques.evaluation.postprocessing.combining;

import java.util.List;

import sample.generation.model.SampleBuilderParameters;
import sample.generation.model.impl.DummySampleConfiguration;
import techniques.algorithms.CombinedAlgorithm;
import techniques.input.Params;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import execution.workload.impl.condition.WorkLoadCondition;
import exploration.model.Combination;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.RecommenderEnum;
import exploration.model.enumerations.UserNeighborhoodEnum;
import exploration.model.enumerations.UserSimilarityEnum;

public class CombineQueries {

	
	private static Instances data;
	private static WorkLoadCondition condition;
	private static databaseWriter pW;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
	
//		long lastStored = 18183;
		
		long generated = 1;
		
		int[] sample_number_cross = {1/*,2,3,4,5*/};
		
		double[][] configuration = {{1.0,0.0},{0.0,1.0},{0.5,0.5}};
		
		int[] sample_configurations = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		
		pW = (databaseWriter)PersistenceImplementation.getWriter();

		List<Database> crossDatabases = pW.getCrossableDatabases();
		
		SampleBuilderParameters sp = null;
		
		for (int sconf = 0; sconf < sample_configurations.length; sconf++) {
		
			for (int snc = 0; snc < sample_number_cross.length; snc++){
				
				for (Database crossDatabase : crossDatabases){
					
					pW.initializeSimpleExplorator();
					
					while (pW.hasMoreCombinations()){
						
						Combination config = pW.nextCombination();
						
						config.setQueries(pW.getQueries(config));
						
						if (config.getGeneratorSample().getDatabase().equals(crossDatabase) && config.getGeneratorSample().getSample_number() != sample_number_cross[snc])
							continue;
						
						condition = new WorkLoadCondition(config.getWorkload().getTuples(), config.getWorkload().getDescription());
						
						args = new String[5];
						
						args[0] = Integer.toString(condition.getNumberOfTuples());//"10"; //Tuples in workload
	
						int tuplesInWorkload = Integer.valueOf(args[0]); //+1 for True and False Positives in position 0 and 1
	
						args[2] = Params.getMaximumNumberOfNeighbors();//"10"; //max number of neighbors
						
						int neighbors = Integer.valueOf(args[2]);
								
						args[1] = "false"; //preserve order
						
						boolean preserveOrder = Boolean.valueOf(args[1]);
	
						args[3] = Params.getMinimumtoBeConsideredRealInCrossing(crossDatabase.getName());//"2"; //minimum value to be sure that it's a real value.
						
						int threshold = Integer.valueOf(args[3]);
						
						System.out.println("Generation: " + config.getGeneratorSample().getDatabase().getName());
						
						for (int co = 0; co < configuration.length; co++) {
							
							System.out.println("BEFORE: " + generated);
							
	//						if (generated > lastStored){
							
								double wc = configuration[co][0];
		
								double ws = 1 - wc;
							
								Sample sample_cross = Sample.getSample(crossDatabase,config.getVersion(),config.getGeneratorSample().getWorkload(),sample_number_cross[snc],new DummySampleConfiguration(sample_configurations[sconf]));
								
								args[1] = pW.getArffFullModel(sample_cross,sp);
								
								data = myArffHandler.loadInstances(args[1]);
								
								System.out.println("Cross: " + sample_cross.getDatabase().getName());
								
								new CombinedAlgorithm(config, sample_cross, tuplesInWorkload, threshold, wc, ws, preserveOrder, neighbors, UserSimilarityEnum.TANIMOTO_COEFFICIENT_SIMILARITY,UserNeighborhoodEnum.NEAREST_N_USER_NEIGHBORHOOD,RecommenderEnum.GENERIC_USER_BASED_RECOMMENDER, condition).executeAlgorithm(data, pW,sp);					
					
	//						}
							
							System.out.println("AFTER: " + generated);
							
							Runtime.getRuntime().gc();
							
							generated++;
						}
					
					}
					
				}

				
			}

			
		}
		
	}

}
