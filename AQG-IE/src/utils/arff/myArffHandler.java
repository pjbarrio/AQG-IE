package utils.arff;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import searcher.interaction.formHandler.TextQuery;
import utils.query.QueryParser;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.SparseToNonSparse;

public class myArffHandler {

	public static double YES_VALUE = 1.0;
	public static double NO_VALUE = 0.0;
	private static int NOT_CALCULATED = -1;
	
	
	public static ArrayList<Long> Currentinstances;
	private static int last_TP = NOT_CALCULATED;
	private static int last_FP = NOT_CALCULATED;
	
	public static Instances loadInstances(String string) {
	
		DataSource source;
		
		try {

			source = new DataSource(string);
			
			Instances data = source.getDataSet();
			// setting class attribute if the data format does not provide this information
			// E.g., the XRFF format saves the class attribute information as well
			if (data.classIndex() == -1){
			  int index = data.attribute("class").index();
			  if (index>0) //if it's not the first one.
				  index = data.numAttributes()-1;
			  data.setClassIndex(index);
			  
			}
			
			return data;
		
		} catch (Exception e) {

			e.printStackTrace();
		}
		
		return null;
	}

	public static int getPosition(Instances instances, Attribute attribute) {
		
		int position = instances.attribute(attribute.name()).index();
		
		if (position>0) //Class is 0 or last index.
			return position;

		for (int i = 1; i < instances.numAttributes(); i++) {
			
			Attribute att = instances.attribute(i);
			
			if (attribute.equals(att))
				return i;
			
		}
		
		return -1;
	}

	public static ArrayList<Long> getInstancesContainingFeatures(Instances data, ArrayList<Integer> feats,
			double classValue, double attValue) {
		
		Currentinstances = new ArrayList<Long>();
		
		int i = 0;
		
		Instance instance;
		
		while (i<data.numInstances()){
			
			instance = data.instance(i);
			
			if (instance.classValue() == classValue){
				
				if (containsAllFeatures(data,instance,feats,attValue)){
					
					Currentinstances.add(new Long(i));
			
				}
				
			}
				
			i++;
			
		}
		
		return Currentinstances;
	}

	private static boolean containsAllFeatures(Instances data, Instance instance,
			ArrayList<Integer> SupFeat, double attValue) {
		
		double falseValue;
		
		if (attValue == YES_VALUE){
			falseValue = NO_VALUE;
		}
		else{
			falseValue = YES_VALUE;
		}
		boolean ret = true;
		
		for (Integer feat : SupFeat) {
			if (instance.value(data.attribute(feat)) == falseValue)
				return false;
			
		}
	
		return ret;
		
	}

	public static long yesInstances(Instances data) {
		
		return data.numInstances()-noInstances(data);
		
	}

	public static int noInstances(Instances data) {
		
		return data.attributeStats(data.classIndex()).nominalCounts[0];
		
	}

	public static int getTP(Instances instances, ArrayList<Integer> query) {
		
		if (last_TP==NOT_CALCULATED){
			calculatePositivesNegatives(instances, query);
		}
		
		int aux = last_TP;
		
		last_TP = NOT_CALCULATED;
		
		return aux;
	}

	public static int getFP(Instances instances, ArrayList<Integer> query) {
		
		if (last_FP==NOT_CALCULATED){
			calculatePositivesNegatives(instances, query);
		}
		
		int aux = last_FP;
		
		last_FP = NOT_CALCULATED;
		
		return aux;
	}
	
	public static void calculatePositivesNegatives(Instances instances,
			ArrayList<Integer> query) {
		
		last_FP=0;
		last_TP=0;
		
		if (query==null)
			return;
		
		for (int i = 0; i < instances.numInstances(); i++) {
			
			Instance instance = instances.instance(i);
			
			if (containsAll(instance,query)){
				if (instance.classValue()==YES_VALUE)
					last_TP++;
				if (instance.classValue()==NO_VALUE)
					last_FP++;
			}
			
		}
		
	}

	private static boolean containsAll(Instance instance,
			ArrayList<Integer> query) {

		for (Integer integer : query) {
			if (instance.value(integer)!=YES_VALUE)
				return false;
		}
	
		return true;
		
	}

	public static void cleanPositivesNegatives() {
		
		last_TP=NOT_CALCULATED;
		last_FP=NOT_CALCULATED;
		
	}

	public static ArrayList<Integer> getTPInstances(Instances instances,
			ArrayList<Integer> query) {
		
		if (query==null){
			return new ArrayList<Integer>();
		}
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < instances.numInstances(); i++) {
			
			Instance instance = instances.instance(i);
			
			if (instance.classValue()==YES_VALUE){
				
				if (containsAll(instance, query)){
					ret.add(i);
				}
				
			}
			
		}
		
		return ret;
	}

	public static ArrayList<Integer> getFPInstances(Instances instances,
			ArrayList<Integer> query) {
		
		if (query==null){
			return new ArrayList<Integer>();
		}
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < instances.numInstances(); i++) {
			
			Instance instance = instances.instance(i);
			
			if (instance.classValue()==NO_VALUE){
				
				if (containsAll(instance, query)){
					ret.add(i);
				}
				
			}
			
		}
		
		return ret;
	}

	public static ArrayList<Integer> getContainingAttribute(
			Instances instances, Integer attribute, ArrayList<Integer> filteredInstances) {
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (Integer integer : filteredInstances) {
			
			if (instances.instance(integer).value(attribute) == YES_VALUE){
				ret.add(integer);
			}
			
		}
		
		return ret;
		
	}

	public static ArrayList<Integer> getAttributesWithMinSupport(
			Instances instances, ArrayList<Integer> tP, int minSupport) {
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < instances.numAttributes(); i++) {
			
			if (instances.classIndex()!=i){
				
				int cant = 0;
				
				for (Integer integer : tP) {
					
					if (instances.instance(integer).value(i)==YES_VALUE){
						cant++;
						if (cant==minSupport){
							ret.add(i);
							break;
						}
					}
				}
				
			}
			
		}
	
		return ret;
		
	}

	public static int getContainingFeatures(Instances instances,
			ArrayList<Integer> auxQuery, ArrayList<Integer> filteredInstances) {
		
		int ret = 0;
		
		boolean all;
		
		for (Integer instance : filteredInstances) {
			
			all = true;
			
			for (Integer attribute : auxQuery) {
				
				if (instances.instance(instance).value(attribute) == NO_VALUE){
					all = false;
					break;
				}
				
			}
			
			if (all){
				ret++;
			}
			
		}
		
		return ret;
	}

	public static void removeInstances(Instances data,
			List<Integer> instances) {
		
		Collections.sort(instances);
		
		int offset = 0;
		
		for (Integer integer : instances) {
			
			data.delete(integer-offset);
			
			offset++;
			
		}
		
	}

	public static long getTP(Instances instances, ArrayList<Integer> query, int t) {
		
		int val = 0;
		
		for (int i = 0; i < instances.numInstances(); i++) {
			
			Instance instance = instances.instance(i);
			
			if (containsAll(instance,query)){
				if (instance.classValue()==YES_VALUE){
					
					val++;
					if (val==t)
						return val;
				}
					
			}
			
		}	
		
		return val;
		
	}

	public static void saveInstances(String output, Instances res) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		bw.write(res.toString());
		
		bw.close();
		
	}

	public static ArrayList<Integer> getAttributes(Instances data, TextQuery query) {
		
		Attribute t;
		
		ArrayList<String> must_words = new ArrayList<String>();
		
		ArrayList<String> must_not_words = new ArrayList<String>();
		
		QueryParser.parseQuery(query, must_words, must_not_words);
		
		ArrayList<Integer> ret = new ArrayList<Integer>(must_words.size());
		
		for (String word : must_words) {
			
			t = data.attribute(word);
			
			if (t == null)
				return null;
			
			ret.add(t.index());
			
		}
		
		return ret;
	}

	public static Instances generateInstanceWithMissingValues(Instances data,
			int classIndex) throws Exception {
		
		int size = data.numAttributes();
		
		for (int i = 0; i < data.numInstances(); i++) {
			
			Instance inst = data.instance(i);
			
			if (i % 1000 == 0.0)
				System.out.println("Processing missing " + i + " out of " + data.numInstances());
			
			for (int j = 0; j < size; j++) { //because class index is 0
				if (inst.value(j) != 1.0){
					if (j != classIndex)
						inst.setMissing(j);
				}
				
			}
			
		}
		
		SparseToNonSparse stns = new SparseToNonSparse();
		
		stns.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options
		
		Instances newData = Filter.useFilter(data, stns);
		
		newData.setClassIndex(classIndex);
		
		return newData;
	}

	public static void reduceUsefulNumberOfInstances(Instances data, int expectedNumberOfUseful) {
		
		List<Integer> positiveInstances = getPositiveInstances(data);
		
		int toRemove = positiveInstances.size() - expectedNumberOfUseful;

		Collections.shuffle(positiveInstances);
		
		List<Integer> toRemoveInstances = new ArrayList<Integer>();
		
		for (int i = 0; i < toRemove; i++) {
			
			toRemoveInstances.add(positiveInstances.get(i));
			
		}
		
		myArffHandler.removeInstances(data, toRemoveInstances);
		
	}

	private static List<Integer> getPositiveInstances(Instances data) {
		
		List<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < data.numInstances(); i++) {
			
			if (data.instance(i).classValue() == YES_VALUE){
				
				ret.add(i);
				
			}
			
		}
		
		return ret;
		
	}
	
}
