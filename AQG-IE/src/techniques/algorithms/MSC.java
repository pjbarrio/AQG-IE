package techniques.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Database;
import exploration.model.Sample;

import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import techniques.baseline.QProberSVM.model.FromIntToT;
import techniques.baseline.QProberSVM.model.IterateValidator;
import techniques.baseline.QProberSVM.model.OptimalCandidateHandler;
import techniques.baseline.QProberSVM.model.StorableArray;
import techniques.newideas.MSC.algorithm.StringToMSCSetTransformer;
import techniques.newideas.MSC.model.IntegerFeatures;
import techniques.newideas.MSC.model.MSCSet;
import techniques.newideas.MSC.model.StringToFeaturesTRansformer;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.arff.myArffHandler;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.core.Instances;

public class MSC extends ExecutableSimpleAlgorithm{

	private static final int MAX_SEQ = 100000;
	private double min_precision;
	private double minimum_support_SVM;
	private int K;
	private double pow;
	private boolean uncovered;
	private ArrayList<Long> timers;
	private Instances instances;
	private Hashtable<Integer, Double> weights;
	private int minimum_support;
	private double minimum_precision;
	private int uselessSample;

	public MSC(Sample sample,int maxNumberOfTerms,
			int minSupport, int min_after_update, double min_precision,
			double minimum_support_SVM, int k, double pow, int uselessSample) {
		
		super(sample,maxNumberOfTerms,minSupport,min_after_update);
		
		this.min_precision = min_precision;
		this.minimum_support_SVM = minimum_support_SVM;
		this.K = k;
		this.pow = pow;
		this.uselessSample = uselessSample;
		
	}

	@Override
	protected List<Pair<TextQuery,Long>> execute(Instances sample, persistentWriter pW, SampleBuilderParameters sp)
			throws Exception {
		
		this.instances = sample;
		
		uncovered = true;
		
		timers = new ArrayList<Long>();
		
		String SMOWekaOutput = pW.getSMOWekaOutput(super.sample,sp,uselessSample); //Tokens
		
//		pW.setMSCAdditionalParameters(w_parameter_ID,min_precision,minimum_support_SVM,K,pow);
		
		String setsFile = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/candidates/IntegerFeatures" + database + "1stVersion" + version; //Stored Sets
		
		String mscFile = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/candidates/MSCSets" + database + "1stVersion" + version; //Store MSC
		
		new File(setsFile).delete();
		
		ArrayList<Integer> tokens = loadTokens(instances.numAttributes()-1,SMOWekaOutput);
		
		ArrayList<IntegerFeatures> prepared = prepare(tokens,min_support);
		
		tokens.clear();
		
		StorableArray<MSCSet> sets = GenerateSets(prepared,min_support,min_precision,max_query_size,setsFile,mscFile);
		
		ArrayList<MSCSet> solution = GenerateSolution(sets,K, mscFile,pow,min_support_after_update);
		
		return getOutput(solution,pW);
		
	}

	@Override
	protected String getName() {
		return "MSC";
	}

	private ArrayList<IntegerFeatures> prepare(ArrayList<Integer> tokens,double min_supp) {
		
		ArrayList<IntegerFeatures> ret = new ArrayList<IntegerFeatures>();
		
		for (Integer integer : tokens) {
			
			ArrayList<Integer> t = new ArrayList<Integer>();
			
			t.add(integer);
			
			ret.add(new IntegerFeatures(t));
			
		}
		
		return ret;
		
	}

	private List<Pair<TextQuery,Long>> getOutput(ArrayList<MSCSet> solution,persistentWriter pW) throws IOException {
		
		int i = 0;
		
		List<Pair<TextQuery,Long>> ret = new ArrayList<Pair<TextQuery,Long>>(solution.size());
		
		for (MSCSet mscSet : solution) {
			
			System.out.println(mscSet.generateQuery(instances));
			
//			pW.writeQuery(w_combination_ID,mscSet.generateQuery(instances), timers.get(i));
	
			ret.add(new Pair<TextQuery,Long>(mscSet.generateQuery(instances), timers.get(i)));
			
			i++;
			
		}
		
		return ret;
		
	}

	private ArrayList<MSCSet> GenerateSolution(StorableArray<MSCSet> sets, int K,String mscFile,double pow, int minAfterUpdate) throws IOException {
		
		ArrayList<MSCSet> ret = new ArrayList<MSCSet>();
		
		MSCSet chosenSet = GetHigherCostBenefit(sets,pow);
		
		Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
		
		timers.add(Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM));
			
		sets.remove(chosenSet);
		
		int chosen = 0;
		
		if (chosenSet == null)
			return ret;
		
		while(chosen<K && uncovered){
			
			chosen++;
			
			System.out.println("Cycle: " + chosen + " out of: " + K);
			
			ret.add(chosenSet);
			
			uncovered = false;
			
			updateCovered(chosenSet);
			
			sets = updateCosts(sets,chosen,mscFile,minAfterUpdate);
			
			if (sets.size()>0){
			
				chosenSet = GetHigherCostBenefit(sets,pow);
				
				Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
				
				timers.add(Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM));
					
				sets.remove(chosenSet);

			}
			
		}
		
		return ret;
	}

	private StorableArray<MSCSet> updateCosts(StorableArray<MSCSet> sets,int round, String mscFile, int minAfterUpdate) {
		
		StorableArray<MSCSet> ret = new StorableArray<MSCSet>(mscFile + "-" + round, new StringToMSCSetTransformer(), sets.size());
		
		for (MSCSet mscSet : sets) {
			
			if (mscSet.recalculateParameters(instances,minAfterUpdate)){
				uncovered = true;
				
				ret.add(mscSet);
			
			}
			
		}
		
		sets.cleanCurrentRound();
		
		return ret;
		
	}

	private void updateCovered(MSCSet chosenSet) {
		
		System.out.println(chosenSet);
		
		ArrayList<Integer> covered = new ArrayList<Integer>();
		
		for (Integer instance : myArffHandler.getTPInstances(instances,chosenSet.getQuery())) {
		
			covered.add(instance);
		}
		
		for (Integer instance : myArffHandler.getFPInstances(instances,chosenSet.getQuery())) {
			
			covered.add(instance);
		}
		
		myArffHandler.removeInstances(instances, covered);
		
		System.out.println("Remaining: " + instances.numInstances());
		
		covered.clear();
		
	}

	private MSCSet GetHigherCostBenefit(StorableArray<MSCSet> sets,double pow) {
		
		double auxCB;
		double CBRet = -1;
		MSCSet ret = null;

		for (MSCSet mscSet : sets) {
			
			auxCB = mscSet.getBenefitCostRatio(pow);
			
			if (auxCB > CBRet){
				ret = mscSet;
				CBRet = auxCB;
			}
			
		}
		
		return ret;
		
	}

	private StorableArray<MSCSet> GenerateSets(ArrayList<IntegerFeatures> arrayList, int min_supp, 
			double min_precision,int mNoT, String setsFile, String mscFile) throws IOException {
		
		OptimalCandidateHandler<IntegerFeatures> ret = new OptimalCandidateHandler<IntegerFeatures>(arrayList.size(), min_supp, new FromIntToT() {
			
			@Override
			public Object createObject(ArrayList<Integer> generateValues) {
				
				return new IntegerFeatures(generateValues);
				
			}
		}, instances, new IterateValidator<IntegerFeatures>() {


			@Override
			public Comparator<IntegerFeatures> getComparator(List<IntegerFeatures> list) {
				return null;
			}

			@Override
			public boolean validate(IntegerFeatures candidate) {
				
				if (calculateSupportSVM(candidate)<minimum_support_SVM)
					return false;
				
				if (calculatePrecision(candidate)<minimum_precision)
					return false;
				
				return true;
			}

			@Override
			public void clean() {
				;
				
			}
		},setsFile,IntegerFeatures.INTEGER_FEATURES_A_FILE,new StringToFeaturesTRansformer());
		
		StorableArray<MSCSet> finalRet = new StorableArray<MSCSet>(mscFile, new StringToMSCSetTransformer(),MAX_SEQ);
		
		myArffHandler.cleanPositivesNegatives();
		
		long index = 0;
		
		double precision;
		
		MSCSet msc;
		
		for (IntegerFeatures query : arrayList) {
			
			System.out.println(index++ + " Out of: " + arrayList.size());
			
			precision = calculatePrecision(query); 
			
			if (calculateSupportSVM(query)>=minimum_support_SVM && precision>min_precision && calculateSupport(query)>=min_supp){
				
				msc = generateMSCSet(query.getFeatures());
				
				finalRet.add(msc);
				
			}
			
			ret.addF1Itemset(query.getFeatures().get(0));
				
			myArffHandler.cleanPositivesNegatives();
		}

		mNoT--;
		
		minimum_support = min_supp;
		minimum_precision = min_precision;
		
		ret.doneWithF1Itemset();
		
		ret.generateNextFrequentItemset();
		
		while (mNoT>0 && ret.size()>0){
			
			for (IntegerFeatures query : ret) {
				
				msc = generateMSCSet(query.getFeatures());
				
				finalRet.add(msc);
			}
			
			mNoT--;
			
			if (mNoT!=0){
				ret.generateNextFrequentItemset();
			}
			
		}
		
		return finalRet;

	}

	protected double calculateSupportSVM(IntegerFeatures candidate) {
		
		double ret = 0;
		
		for (Integer feat : candidate.getFeatures()) {
			
			ret +=weights.get(feat);
			
		}
		
		return ret;
	}

	private MSCSet generateMSCSet(ArrayList<Integer> query) {
		
		double tp = myArffHandler.getTP(instances, query);
		
		double cost = myArffHandler.getFP(instances, query) + tp;
		
		double benefit = tp;
		
		return new MSCSet(query,cost,benefit);
		
	}

	private double calculatePrecision(IntegerFeatures query) {
		
		double tp = myArffHandler.getTP(instances, query.getFeatures());
		double fp = myArffHandler.getFP(instances, query.getFeatures());
		
		return (tp/(tp+fp));
	}

	private int calculateSupport(IntegerFeatures query) {
		
		return (int) myArffHandler.getTP(instances,query.getFeatures(),minimum_support);

	}

	private ArrayList<Integer> loadTokens(int limit, String features) throws IOException {
		
		SVMFeaturesLoader.loadFeatures(new File(features),instances);
		
		weights = SVMFeaturesLoader.getWeightsIndex();
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (String feature : SVMFeaturesLoader.getFeatures()){
			
			if (instances.attribute(feature)==null || instances.attribute(feature).index() == instances.classIndex())
				continue;
			
			ret.add(instances.attribute(feature).index());
			
			if (ret.size()==limit)
				break;
		}
		
		return ret;
		
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getMSCParameter(maxQuerySize,minSupport,minSuppAfterUpdate,min_precision,minimum_support_SVM,K,pow);
	}

	
}
