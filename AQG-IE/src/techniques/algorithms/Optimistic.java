package techniques.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Database;
import exploration.model.Sample;

import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import techniques.baseline.QProberSVM.model.FromIntToSVMRules;
import techniques.baseline.QProberSVM.model.FromIntToT;
import techniques.baseline.QProberSVM.model.IterateValidator;
import techniques.baseline.QProberSVM.model.OptimalCandidateHandler;
import techniques.baseline.QProberSVM.model.StringToSVMRuleTransformer;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.SVM.Rules.RuleSupportComparator;
import utils.SVM.Rules.SVMRule;
import utils.arff.myArffHandler;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.core.Attribute;
import weka.core.Instances;

public class Optimistic extends ExecutableSimpleAlgorithm {

	private double threshold;
	private double min_weight;
	private Double min_precision;
	private double supp;
	private Instances dataTrue;
	private ArrayList<String> featureValues;
	private Hashtable<String, Double> weights;
	private Hashtable<Integer, Double> weightsSVM;
	private Hashtable<String, Double> weightsFeat;
	private Instances data;
	private ArrayList<Integer> aux;
	private OptimalCandidateHandler<SVMRule> candidates;
	private ArrayList<SVMRule> remove;
	private ArrayList<SVMRule> auxRules;
	private int uselessSample;

	public Optimistic(Sample sample, int max_query_size,
			Integer min_supp, Integer min_support_after_update,
			double threshold, double min_weight, Double min_precision,
			double supp, Instances dataTrue, int uselessSample) {

		super(sample,max_query_size,min_supp,min_support_after_update);
		this.threshold = threshold;
		this.min_weight = min_weight;
		this.min_precision = min_precision;
		this.supp = supp;
		this.dataTrue = dataTrue;
		this.uselessSample = uselessSample;
		
	}

	@Override
	protected List<Pair<TextQuery,Long>> execute(Instances sample, persistentWriter pW, SampleBuilderParameters sp)
			throws Exception {
		
		this.data = sample;
		
		String SMOWekaOutput = pW.getSMOWekaOutput(super.sample,sp,uselessSample);
		SVMFeaturesLoader.loadFeatures(new File(SMOWekaOutput),dataTrue);
		
		
		String svmCandidates = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/candidates/SVMReloadedCandidates" + database + "1stVersion" +  version;
		
		
//		pW.setOptimistic(w_parameter_ID,threshold,min_weight,min_precision,supp);
		
		featureValues = SVMFeaturesLoader.getFeatures();
		weights = SVMFeaturesLoader.getWeightFeature();
		weightsSVM = SVMFeaturesLoader.getWeightsIndex();
		weightsFeat = SVMFeaturesLoader.getWeightFeature();
		
		for (String feat : featureValues) {
			
			System.out.println(feat + " ---> " + weights.get(feat));
			
		}
						
		SVMRule.weightsSt = SVMFeaturesLoader.getWeightsIndex();
		SVMRule.dataSt = dataTrue;
		
		return GenerateRulesOptimistic(min_support,threshold,min_weight,svmCandidates,max_query_size,pW);

	}

	@Override
	protected String getName() {
		return "Optimistic";
	}

	private List<Pair<TextQuery,Long>> GenerateRulesOptimistic(int min_supp,double threshold, double minweight, String candidatesFile, int maxQuerySize, persistentWriter pW) throws Exception {
		
		aux = new ArrayList<Integer>();
		
		for (String feature : featureValues) {
			
			if (weightsFeat.get(feature) < minweight)
				continue;
			
			Attribute att = data.attribute(feature);
			
			if (att == null || att.index() == data.classIndex())				
				continue;

			int index = att.index();
			
			aux.add(index);
			
		}
		
		FromIntToT converter = new FromIntToSVMRules(weightsSVM);
		
		candidates = new OptimalCandidateHandler<SVMRule>(aux.size(), min_supp, converter, dataTrue, new IterateValidator<SVMRule>() {

			private RuleSupportComparator comp;

			@Override
			public boolean validate(SVMRule candidate) {
				
				double support = candidate.getSupport();
				
				if (support < supp){ 
					
					return false;

				}
				
				return true;
			}

			@Override
			public Comparator<SVMRule> getComparator(List<SVMRule> list) {
				
				comp = new RuleSupportComparator(list, dataTrue);
				
				return comp;
				
			}

			@Override
			public void clean() {
				
				comp.clear();
				
			}
		},candidatesFile,SVMRule.SVMRULES_A_FILE,new StringToSVMRuleTransformer());
				
		featureValues.clear();
		
		for (Integer feat : aux) {
			candidates.addF1Itemset(feat);
		}
		
		candidates.doneWithF1Itemset();
		
		double support;
		
		remove = new ArrayList<SVMRule>();
		
		for (SVMRule feature : candidates) {
			
			support = feature.getTP(dataTrue);
			
			if (support < min_supp)
				remove.add(feature);
				
		}
		 
		for (SVMRule feature : remove) {
			
			System.out.println("Removing!");
			
			candidates.remove(feature.getFeatures());
			
		}
		
		int k = 1;
		
		ArrayList<SVMRule> notAccepted = new ArrayList<SVMRule>();
		ArrayList<SVMRule> Accepted = new ArrayList<SVMRule>();
		
		while (candidates.size()>0 && k != maxQuerySize){
			
			System.out.println("New Loop: " + candidates.size());
		
			remove.clear();
		
			long j = 0;
			
			candidates.generateNextFrequentItemset();
			
			for (SVMRule features : candidates) {
				
				j++;
				
				System.out.println("Features: " + j + " out of: " + candidates.sizeOfIterator());
				
				if (!acceptableLoss(features,min_supp,threshold)){
					
					notAccepted.add(features);
					
					remove.add(features);
					
				} else if (k == maxQuerySize-1){
					
					Accepted.add(features);
					
				}
				
			}
			
			for (SVMRule svmRule : remove) {

				candidates.remove(svmRule.getFeatures()); //Remove rule that doesn't work joined.
				
			}

			k = k + 1;
			
			System.out.println(candidates.size() + "    ---    " + k);
			
		}
		
		return generateFinalRules(notAccepted,Accepted,pW);
		
	}
	
	private List<Pair<TextQuery,Long>> generateFinalRules(
			
		ArrayList<SVMRule> notAccepted, ArrayList<SVMRule> accepted, persistentWriter pW) {
		
		ArrayList<SVMRule> tobeAnalyzed = new ArrayList<SVMRule>();

		while (accepted.size()>0){
			
			tobeAnalyzed.add(accepted.remove(0));
			
		}
		
		ArrayList<SVMRule> parents;
		
		while (notAccepted.size()>0) {
			
			SVMRule svmRule = notAccepted.remove(0);
			
			parents = svmRule.getParents();
			
			RuleSupportComparator rsc = new RuleSupportComparator(parents, dataTrue);
			
			Collections.sort(parents, rsc);
			
			rsc.clear();
			
			for (SVMRule parent : parents){
				
				if (valid(parent)){
					
					if (!tobeAnalyzed.contains(parent))
					
						tobeAnalyzed.add(parent);
					
				}
			
			}
			
		}		
		
		Collections.sort(tobeAnalyzed,new Comparator<SVMRule>(){

			@Override
			public int compare(SVMRule arg0, SVMRule arg1) {
				
				if (arg0.size() != arg1.size()){
					
					return Double.compare(arg1.size(),arg0.size());
					
				}
				
				return Double.compare(arg1.getSupport(),arg0.getSupport());
			}
			
		});
		
		List<Pair<TextQuery,Long>> ret = new ArrayList<Pair<TextQuery,Long>>();
		
//		auxRules = new ArrayList<SVMRule>();
		
		for (int i = 0; i < tobeAnalyzed.size(); i++) {
			
			SVMRule rule = tobeAnalyzed.get(i);
			
			if (valid(rule)){
			
				if (lastOcurrence(rule,tobeAnalyzed,i+1)){
				
//					auxRules.add(rule);
				
					Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
					
//						pW.writeQuery(w_combination_ID, rule.toQuery(), Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM));
					
					ret.add(new Pair<TextQuery,Long>(rule.toQuery(), Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM)));
					
					updateModel(rule);
					
				}
			}
		}
		
		return ret;
		
	}

	private void updateModel(SVMRule svmRule) {
		
		ArrayList<Integer> instances = myArffHandler.getTPInstances(data, svmRule.getFeatures());
		
		instances.addAll(myArffHandler.getFPInstances(data, svmRule.getFeatures()));
		
		myArffHandler.removeInstances(data,instances);
		
		instances.clear();
		
		instances = myArffHandler.getTPInstances(dataTrue, svmRule.getFeatures());
		
		myArffHandler.removeInstances(dataTrue, instances);
		
		for (Integer transaction : instances) {
			
			candidates.removeTransaction(transaction);
		
		}
		
		System.out.println(dataTrue.numInstances());
		
	}



	private boolean valid(SVMRule svmRule) {

		if (svmRule.getSupport() < supp)
			return false;
		
		if (myArffHandler.getTP(dataTrue, svmRule.getFeatures(), min_support_after_update)<min_support_after_update)
			return false;		
		
		svmRule.calculatePrecision(data);
		
		if (svmRule.getPrecision()<min_precision)
			return false;
		
		return true;
		
	}



	private boolean lastOcurrence(SVMRule svmRule,
			ArrayList<SVMRule> array, int i) {
		
		for (int j = i; j < array.size(); j++) {
			
			if (array.get(j).getFeatures().containsAll(svmRule.getFeatures())){
				
				return false;
			}
			
		}
		
		return true;
		
	}



	private boolean acceptableLoss(SVMRule features, long minSupp, double threshold) {
		
		if (features.getTP(dataTrue,(int)minSupp,false) < minSupp)
			return false;
		
		features.calculatePrecision(data);
		
		long TP = getNumberofInstances(features.getAncestors(), myArffHandler.YES_VALUE,myArffHandler.YES_VALUE);
		
		double rA = (double)features.getTP(dataTrue);///totalPositives;
		
		double rB = (double)TP; ///totalPositives;
		
		return (rA/rB>threshold);
		
	}

	private long getNumberofInstances(ArrayList<SVMRule> parents, double classValue, double attributeValue) {
		
		if (parents.size() == 0){

			if (classValue == myArffHandler.YES_VALUE){
				return getYESValues();
			}
			else if (classValue == myArffHandler.NO_VALUE){
				return getNOValues();
			}
			
		}
		
		HashSet<Long> ret = new HashSet<Long>();
		
		for (SVMRule svmRule : parents) {
			
			myArffHandler.getInstancesContainingFeatures(data, svmRule.getFeatures(), classValue, attributeValue);
			
			ret.addAll(myArffHandler.Currentinstances);
			
		}
		
		return ret.size();
	}

	private long getNOValues() {
		
		return myArffHandler.noInstances(data);
		
	}

	private long getYESValues() {

		return myArffHandler.yesInstances(dataTrue);
		
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getOptimisticParameters(maxQuerySize,minSupport,minSuppAfterUpdate,threshold,min_weight,min_precision,supp);
	}

}
