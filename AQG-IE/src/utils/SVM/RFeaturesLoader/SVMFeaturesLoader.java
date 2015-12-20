package utils.SVM.RFeaturesLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;

import weka.core.Attribute;
import weka.core.Instances;

public class SVMFeaturesLoader {

	private static final String CONSTANT_TEXT = "Machine linear: showing attribute weights, not support vectors.";
	private static final String START_THRESHOLD = " -       ";
	private static Hashtable<Integer, Double> weightsIndex;
	private static Hashtable<String,Double> weightFeat;
	private static ArrayList<String> featureValues;

	public static double getThreshold(String SMOFile) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(new File("/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/WekaOutput/SMO/Bloomberg/4/DEPENDENT_4")));
		
		String line = br.readLine();
		
		while (!line.startsWith(START_THRESHOLD)){
			
			line = br.readLine();
			
		}
		
		String value = line.replace(START_THRESHOLD, "").trim();
		
		br.close();
		
		return Double.valueOf(value);
				
	}
	
	public static void loadFeatures(File file, Instances data) throws IOException {
		
		loadFeatures(FileUtils.readFileToString(file), data);
		
	}
	
	private static void addFeatureWeight(String feature, Double weight,Instances data) {
		
		if (data != null){
			
			Attribute att = data.attribute(feature);
			
			if (att != null)
				weightsIndex.put(att.index(),weight);
		}
		
		weightFeat.put(feature, weight);
		
	}

	private static void addFeature(String feature) {
		
		featureValues.add(feature);
		
	}
	
	public static ArrayList<String> getFeatures(){
		return featureValues;
	}
	
	public static Hashtable<Integer, Double> getWeightsIndex(){
		return weightsIndex;
	}
	
	public static Hashtable<String, Double> getWeightFeature(){
		return weightFeat;
	}
	
	private static void sortDescendingbyWeight() {
		
		Collections.sort(featureValues, new Comparator<String>() {

			@Override
			public int compare(String feat1, String feat2) {
				
				return weightFeat.get(feat1).compareTo(weightFeat.get(feat2)) * -1;
				
			}
		});
		
	}

	public static void loadFeatures(String output, Instances data) throws IOException {
		
		loadFeatures(output, data,false);
		
	}

	public static void loadFeatures(String output, Instances data, boolean includeNegative) throws IOException {
		
		weightsIndex = new Hashtable<Integer, Double>();
		featureValues = new ArrayList<String>();
		weightFeat = new Hashtable<String, Double>();
		
		BufferedReader br = new BufferedReader(new StringReader(output));
		
		String line = br.readLine();
		
		while (!line.equals(CONSTANT_TEXT)){
			line = br.readLine();
		}
		
		br.readLine(); //Skip Spaces
		
		line = br.readLine();
		
		while (!line.trim().equals("")){
			
			line = line.replace(" ", "");
			line = line.replace("+-", "-");
			line = line.replace("+", "");
			line = line.replace("*(normalized)", " ");
			
			String[] split = line.split(" ");
			
			if (split.length > 1){
				if (includeNegative || !split[0].startsWith("-")){
					
					Double d = Double.parseDouble(split[0]);
					
					addFeature(split[1]);
					addFeatureWeight(split[1],d,data);
					
				}
			}
			
			line = br.readLine();
		
		}
		
		br.close();
	
		sortDescendingbyWeight();
		
	}
}
