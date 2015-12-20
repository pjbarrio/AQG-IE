package techniques.algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
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

public class QProber extends ExecutableSimpleAlgorithm {

	private double minSVMSupport;
	private double epsilon;
	private double min_precision;
	private Instances dataTrue;
	private ArrayList<String> featureValues;
	private Hashtable<String, Double> weights;
	private Hashtable<Integer, Double> weightsSVM;
	private ArrayList<Integer> aux;
	private Instances data;
	private OptimalCandidateHandler<SVMRule> candidates;
	private ArrayList<SVMRule> remove;
	private int uselessSample;

	public QProber(Sample sample, int max_query_size,
			Integer min_supp, Integer min_supp_after_update,
			double minSVMSupport, double epsilon, double min_precision, Instances dataTrue, int uselessSample) {
		
		super(sample,max_query_size,min_supp,min_supp_after_update);
		this.minSVMSupport = minSVMSupport;
		this.epsilon = epsilon;
		this.min_precision = min_precision;
		this.dataTrue = dataTrue;
		this.uselessSample = uselessSample;
	}

	@Override
	protected List<Pair<TextQuery,Long>> execute(Instances sample, persistentWriter pW, SampleBuilderParameters sp)
			throws Exception {
		
		this.data = sample;
		
		String SMOWekaOutput = pW.getSMOWekaOutput(super.sample,sp,uselessSample);
		
		SVMRule.dataSt = dataTrue;
		
		SVMFeaturesLoader.loadFeatures(new File(SMOWekaOutput),dataTrue);
		
		featureValues = SVMFeaturesLoader.getFeatures();
		weights = SVMFeaturesLoader.getWeightFeature();
		weightsSVM = SVMFeaturesLoader.getWeightsIndex();

		String candidatesFile = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/candidates/QProberCandidates" + database + "1stVersion" + version;
		
//		pW.setQProberAdditionalParameters(w_parameter_ID,minSVMSupport,epsilon,min_precision);
		
		for (String feat : featureValues) {
			
			System.out.println(feat + " ---> " + weights.get(feat));
			
		}
			
		return GenerateRulesQProbe(epsilon,candidatesFile,max_query_size,min_precision,pW);
		
	}

	@Override
	protected String getName() {
		return "QProber";
	}

	private List<Pair<TextQuery,Long>> GenerateRulesQProbe(double epsilon, String candidatesFile, Integer maxQuerySize, double min_precision, persistentWriter pW) throws Exception {
		
		ArrayList<SVMRule> rules = null;//new ArrayList<SVMRule>();
		
		aux = new ArrayList<Integer>();
		
		for (String feature : featureValues) {
			
			if (weights.get(feature)>=epsilon)
			
				System.out.println(feature);
				
				Attribute att = data.attribute(feature);
			
				if ((att != null) && (att.index() != data.classIndex()))
					aux.add(att.index());
				
		}
		
		FromIntToT converter = new FromIntToSVMRules(weightsSVM);
		
		candidates = new OptimalCandidateHandler<SVMRule>(aux.size(), min_support, converter,data,new IterateValidator<SVMRule>() {

			private RuleSupportComparator comp;

			@Override
			public boolean validate(SVMRule candidate) {
				
				double support = candidate.getSupport();
				
				if (support < minSVMSupport){
					
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
		
		for (Integer integer : aux) {
		
			candidates.addF1Itemset(integer);
		
		}
		
		candidates.doneWithF1Itemset();
		
		remove = new ArrayList<SVMRule>();
		
		int k = 1;
		
		List<Pair<TextQuery,Long>> ret = new ArrayList<Pair<TextQuery,Long>>();
		
		while (candidates.size()>0 && k <=maxQuerySize){
			
			System.out.println("New Loop: " + candidates.sizeOfIterator());
			
			remove.clear();
		
			long j = 0;
			
			for (SVMRule features : candidates) {
				
				if (!appearsInMoreThanTInstances(features,min_support_after_update)){
					
					continue;
				
				}
				
				features.calculatePrecision(data);
				
				if (features.getPrecision() < min_precision ){
					
					continue;
					
				}
				
				j++;
				
				System.out.println("Features: " + j + " out of: " + candidates.sizeOfIterator());
				
				Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
				
//				pW.writeQuery(w_combination_ID,features.toQuery(), Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM));
				
				ret.add(new Pair<TextQuery,Long>(features.toQuery(), Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM)));
				
				remove.add(features);
				
				updateModel(features);
				 					
				
			}
			
			j = 0;
			
			for (SVMRule feature : remove) {
				
				System.out.println("Removing: " + j++ + " out of: " + remove.size());
				
				candidates.remove(feature.getFeatures());
				
			}
			
			if (k < maxQuerySize)
				candidates.generateNextFrequentItemset();
			
			k = k + 1;
			
			System.out.println(candidates.size() + "    ---    " + k);
			
		}
		
		return ret;
		
	}

	private boolean appearsInMoreThanTInstances(SVMRule features,
			int minSupport) {
		
		return myArffHandler.getTP(dataTrue, features.getFeatures(), minSupport)>=minSupport;
		
	}

	private void updateModel(SVMRule features) {
		
		ArrayList<Integer> instances = myArffHandler.getTPInstances(data, features.getFeatures());
		
		instances.addAll(myArffHandler.getFPInstances(data, features.getFeatures()));
		
		myArffHandler.removeInstances(data,instances);
		
		instances.clear();
		
		instances = myArffHandler.getTPInstances(dataTrue, features.getFeatures());
		
		myArffHandler.removeInstances(dataTrue, instances);
		
		for (Integer transaction : instances) {
			
			candidates.removeTransaction(transaction);
		
		}
		
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getQProberParameter(maxQuerySize,minSupport,minSuppAfterUpdate,minSVMSupport,epsilon,min_precision);
	}

}
