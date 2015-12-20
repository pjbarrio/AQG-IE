package sample.generation.relation.attributeSelection.oneclass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.arff.myArffHandler;
import utils.execution.MapBasedComparator;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffLoader.ArffReader;


public class LibsvmPrimalWeight {
	private static Map<String,String> getPropertyMap(BufferedReader br) throws FileFormatException, IOException{
		String property;
		Map<String,String> propertyValues = new HashMap<String, String>();
		while((property=br.readLine())!=null && !property.equals("SV")){
			String[] propertySplit = property.split(" ", 2);
			if(propertySplit.length!=2){
				throw new FileFormatException("The file does not seem to be a Libsvm model.");
			}
			propertyValues.put(propertySplit[0], propertySplit[1]);
		}
		
		return propertyValues;
	}
	
	private static void verifyProperties(Map<String, String> propertyValues) throws FileFormatException, UnsupportedSVMTypeException, UnsupportedKernelException, UnsupportedClassNumberException{
		String svmType = propertyValues.get("svm_type");
		if(svmType==null){
			throw new FileFormatException("The file does not seem to be a Libsvm model. No svm_type property.");
		}else if(!svmType.equals("one_class")){
			throw new UnsupportedSVMTypeException("Right now, we only support one_class svm. You are using " + svmType + ".");
		}
		
		String kernel = propertyValues.get("kernel_type");
		if(kernel==null){
			throw new FileFormatException("The file does not seem to be a Libsvm model. No kernel_type property.");
		}else if(!kernel.equals("linear")){
			throw new UnsupportedKernelException("This method only works for linear kernels. You are using " + kernel + ".");
		}

		String numClass = propertyValues.get("nr_class");
		if(numClass==null){
			throw new FileFormatException("The file does not seem to be a Libsvm model. No nr_class property.");
		}else if(!numClass.equals("2")){
			throw new UnsupportedClassNumberException("Right now, we do not support multi class svm. You are using " + numClass + " classes.");
		}
	}
	
	private static Map<Integer,Double> getWeightVector(BufferedReader br) throws NumberFormatException, IOException{
		String supportVector;
		Map<Integer,Double> weights = new HashMap<Integer,Double>();
		while((supportVector=br.readLine())!=null){
			String[] supportVectorSplit = supportVector.split(" ");
			
			double alpha = Double.parseDouble(supportVectorSplit[0]);
			for(int i=1; i<supportVectorSplit.length; i++){
				String feature =  supportVectorSplit[i];
				String[] featurePair = feature.split(":");
				Integer index = Integer.parseInt(featurePair[0]);
				Double value = Double.parseDouble(featurePair[1]);
				Double wFeature=weights.get(index);
				if(wFeature==null){
					wFeature=0.0;
				}
				wFeature+=alpha*value;
				weights.put(index, wFeature);
			}
		}
		return weights;
	}
	
	private static void writeWeights(Instances instances, String file, Map<Integer, Double> weights) throws IOException{
		
		List<Integer> li = new ArrayList<Integer>(weights.keySet());
		
		MapBasedComparator<Integer, Double> comp = new MapBasedComparator<Integer, Double>(weights);
		
		Collections.sort(li, comp);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		List<Integer> features = new ArrayList<Integer>(weights.keySet());
		Collections.sort(features);
		for(Integer feature : li){
			Double weight = weights.get(feature);
			bw.write(feature + ":" + instances.attribute(feature).name() + ":" + weight + "\n");
		}
		bw.close();
	}
	
	public static void main(String[] args) throws FileFormatException, IOException, UnsupportedKernelException, UnsupportedSVMTypeException, UnsupportedClassNumberException {
		FileInputStream fstream = new FileInputStream(args[0]);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		Map<String,String> propertyValues = getPropertyMap(br);
		verifyProperties(propertyValues);
		Map<Integer,Double> weights = getWeightVector(br);
		
		in.close();
		
		Instances instances = myArffHandler.loadInstances(args[1]);
		
		writeWeights(instances, args[2], weights);
		
	}
}
