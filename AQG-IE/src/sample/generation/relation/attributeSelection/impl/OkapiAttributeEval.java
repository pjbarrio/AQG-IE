package sample.generation.relation.attributeSelection.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import online.sample.wordsDistribution.HashBasedComparator;

import utils.arff.myArffHandler;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class OkapiAttributeEval extends ASEvaluation implements AttributeEvaluator{

	/**
	 * 
	 */
	private static final long serialVersionUID = 955610675976488668L;
	private Map<Integer, Double> weights;

	@Override
	public void buildEvaluator(Instances data) throws Exception {
		
		AttributeStats stats = data.attributeStats(data.classIndex());
		
		double R = stats.nominalCounts[1];
		
		double N = data.numInstances();
		
		weights = new HashMap<Integer, Double>();
		
		weights.put(0, -1.0);
		
		for (int att = 1; att < data.numAttributes(); att++) {
			
			stats = data.attributeStats(att);

			double n = stats.nominalCounts[1];
			
			double[] values = data.attributeToDoubleArray(att);

			double r = 0.0;

			for (int ins = 0; ins < values.length; ins++) {

				if (data.instance(ins).value(att) == 1.0)
					if (data.instance(ins).classValue() == 1.0)
						r++;

			}
			
			weights.put(att, r*getRobertsonSparkJonesTW(r,n,N,R));
			
		}
		
	}

	private double getRobertsonSparkJonesTW(double r, double n, double N,
			double R) {
		return Math.log(((r+0.5)/(R-r+0.5))/((n-r+0.5)/(N-n-R+r+0.5)));
	}

	@Override
	public double evaluateAttribute(int attribute) throws Exception {
		Double d = weights.remove(attribute);
		
		if (d == null)
			d = -1.0;
		
		return d;
	}

	public static void main(String[] args) throws Exception {
		
		String file = "test.arff";
		
		DataSource source = new DataSource(new FileInputStream(file));

		Instances data = source.getDataSet();
		
//		data = myArffHandler.generateInstanceWithMissingValues(data, 0);
//		
//		myArffHandler.saveInstances("test.arff", data);
		
		data.setClassIndex(0);		
			
		new OkapiAttributeEval().buildEvaluator(data);
		
	}
}
