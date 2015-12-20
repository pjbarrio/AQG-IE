package sample.AttributeTailoring;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import exploration.model.Sample;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;

public class AttributePowerLawTailorer {

	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String[] version = {"INDEPENDENT","DEPENDENT"}; 
		
		String[][] database = {/*{"Business","Bloomberg","randomSample"},*/{"Trip","TheCelebrityCafe","randomSample"}/*,{"Business","TheEconomist","randomSample"},{"General","UsNews","randomSample"},{"Trip","Variety","randomSample"},{"","GlobalSample","Mixed"}*/};
		
		pW = PersistenceImplementation.getWriter();
		
		int workload = 1;
		
		int version_seed_pos = 1;
		
		int version_seed_neg = 1;
		
		for (int i = 0; i < database.length; i++) {
			
			for (int j = 0; j < version.length; j++) {
				
				args = new String[4];
				
				Sample sample = Sample.getSample(pW.getDatabaseByName(database[i][1]), new DummyVersion(version[j]), new DummyWorkload(workload), version_seed_pos, version_seed_neg, new DummySampleConfiguration(1));
				
				args[0] = pW.getArffRawFilteredModel(sample);
				
				args[1] = pW.getArffTailoredModel(sample);
				
				args[2] = "0.003"; //%min
				
				args[3] = "0.9"; //%max
								
				Instances data = myArffHandler.loadInstances(args[0]);

				double min = Double.valueOf(args[2]);//Double.valueOf(args[2])/(double)data.numInstances();
				double max = Double.valueOf(args[3]);//Double.valueOf(args[3])/(double)data.numInstances();
				
				String output = args[1];
				
				In_FrequentRemovalFilter ifr = new In_FrequentRemovalFilter();
				
				ifr.setMinFrequencyvalue(min);
				ifr.setMaxFrequencyvalue(max);
				
				ifr.setInputFormat(data);
				
				Instances res = ifr.process(data);
				
				myArffHandler.saveInstances(output,res);
				
			}
			
		}
		
		
		

		
	}

}
