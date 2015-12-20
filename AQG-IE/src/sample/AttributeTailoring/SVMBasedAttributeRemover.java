package sample.AttributeTailoring;

import java.io.File;
import java.util.ArrayList;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import exploration.model.Sample;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;

public class SVMBasedAttributeRemover {

	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String[] version = {"INDEPENDENT","DEPENDENT"}; 
		
		String[][] database = {{"Trip","TheCelebrityCafe","randomSample"}};

		pW = PersistenceImplementation.getWriter();
		
		int workload = 1;
		
		int sample_number = 1;
		
		for (int k = 0; k < database.length; k++) {
			
			System.out.println("DATABASE: " + database[k][1]);
			
			for (int j = 0; j < version.length; j++) {
				
				args = new String[4];
				
				Sample sample = null;
				
//XXX			Sample sample = Sample.getSample(pW.getDatabaseByName(database[k][1]), new DummyVersion(version[j]), new DummyWorkload(workload), sample_number,new DummySampleConfiguration(1));
				
				args[0] = pW.getSMOWekaOutput(sample,null);
				
				args[1] = "700";
				
				args[2] = pW.getArffTailoredModel(sample,null);
				
				args[3] = pW.getArffBooleanModel(sample);
				
				Instances data = myArffHandler.loadInstances(args[2]);
				
				int cant = Integer.valueOf(args[1]);
				
				SVMFeaturesLoader.loadFeatures(new File(args[0]),data);
				
				ArrayList<String> feats = SVMFeaturesLoader.getFeatures();

				int[] noRemove = new int[cant+1];
				
				int i;
				
				for (i = 0; i < cant; i++) {
					
					System.out.println(i + " - Feature: " + feats.get(i));
					
					noRemove[i] = data.attribute(feats.get(i)).index();
					
				}
				
				noRemove[i]= data.classIndex();
				
				Remove r = new Remove();
				
				r.setInvertSelection(true);
				
				r.setAttributeIndicesArray(noRemove);
				
				r.setInputFormat(data);
				
				Instances save = Filter.useFilter(data, r);
				
				myArffHandler.saveInstances(args[3], save);
				
			}
			
		}
		
		
		
		
	}

}
