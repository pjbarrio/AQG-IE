package sample.generation.relation.attributeSelection.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;

import sample.generation.utils.SampleGenerationUtils;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.classifiers.functions.SMO;
import weka.core.Instances;

public class SMOAttributeEval extends ASEvaluation implements AttributeEvaluator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8954509226993874642L;
	private Hashtable<Integer, Double> weights;

	@Override
	public void buildEvaluator(Instances data) throws Exception {
		
		SMO scheme = new SMO();

		scheme.buildClassifier(data);
		
		String output = scheme.toString();
		
		SVMFeaturesLoader.loadFeatures(output, data,true);
		
		weights = SVMFeaturesLoader.getWeightsIndex();
		
	}

	@Override
	public double evaluateAttribute(int attribute) throws Exception {
		
		Double d = weights.remove(attribute);
		
		if (d == null)
			d = 0.0;
		
		return d;
		
	}

}
