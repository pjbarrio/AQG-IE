package techniques.evaluation.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.arff.myArffHandler;
import weka.core.Instances;

public class MinSupportCalculator {

	private static ArrayList<String> featureValues;
	private static Instances data;
	private static Hashtable<Integer, Double> weights;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[3];
		
		args[0] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/WekaOutput/SMO2ndVersion";
		args[1] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/arff/randomSample2ndVersionReady.arff";
		args[2] = "0.065"; //min Support
		
		double min_weight = Double.valueOf(args[2]);
		
		data = myArffHandler.loadInstances(args[1]);
		
		SVMFeaturesLoader.loadFeatures(new File(args[0]),data);
		
		featureValues = SVMFeaturesLoader.getFeatures();
		weights = SVMFeaturesLoader.getWeightsIndex();
		
		ArrayList<Integer> aux = new ArrayList<Integer>();
		
		int supp, min_supp = data.numInstances();
		String min_feat = null;
		Integer min_index = null;
		
		for (String string : featureValues) {
			
			if (weights.get(string)<min_weight)
				continue;
			
			aux.clear();
			
			int index = data.attribute(string).index();
			
			aux.add(index);
			
			supp = myArffHandler.getTP(data, aux);
			
			if (supp < min_supp){
				min_supp = supp;
				min_feat = string;
				min_index = index;
			}
			
		}
		
		System.out.println("Minimum Feature: " + min_feat);
		System.out.println("Minimum Index: " + min_index);
		System.out.println("Minimum Support: " + min_supp);
		
	}

}
