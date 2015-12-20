package techniques.algorithms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Database;
import exploration.model.Sample;

import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import utils.arff.myArffHandler;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.core.Instances;

public class Incremental extends ExecutableSimpleAlgorithm {

	private static final Integer NO_POSIBLE_QUERY = -1;
	private double threshold;
	private Double wfp;
	private double betaE;
	private Instances instances;
	private int totalTP;
	private List<Pair<TextQuery,Long>> qs;
	private double Pprec;
	private double Prec;
	private double Pmean;
	private double lastPerformance;

	public Incremental(Sample sample,int max_query_size,
			int min_support, int min_supp_after_update, double threshold,
			Double wfp, double betaE) {
		super(sample,max_query_size,min_support,min_supp_after_update);
		this.threshold = threshold;
		this.wfp = wfp;
		this.betaE = betaE;
	}

	@Override
	protected List<Pair<TextQuery,Long>> execute(Instances sample, persistentWriter pW, SampleBuilderParameters sp) throws IOException {
		
		qs = new ArrayList<Pair<TextQuery,Long>>();
		
		this.instances = sample;
		
//		pW.setIncrementalAdditionalParameters(w_parameter_ID,threshold,wfp,betaE);
		
		ArrayList<Integer> TP = myArffHandler.getTPInstances(instances, new ArrayList<Integer>());
		
		ArrayList<Integer> FP = myArffHandler.getFPInstances(instances, new ArrayList<Integer>());
		
		totalTP = TP.size();
		
		ArrayList<Integer> currentQuery = new ArrayList<Integer>();
		
		Integer nextKeyWord = getNextKeyWord(currentQuery,TP,FP,min_support,betaE);
		
		while (nextKeyWord != NO_POSIBLE_QUERY){
			
			if (currentQuery.size()<max_query_size && (currentQuery.size()==0 || acceptedNewKeyword(currentQuery,nextKeyWord,TP,FP,threshold,min_support,betaE))){
				
				currentQuery.add(nextKeyWord);
				
				TP = updateTP(currentQuery,nextKeyWord,TP);
					
				FP = updateFP(currentQuery,nextKeyWord,FP);
					
			} else {
					
				saveQuery(currentQuery,pW);
				
				min_support = min_support_after_update;
				
				TP = generateNewNotCoveredTP(TP);
				
				FP = myArffHandler.getFPInstances(instances, new ArrayList<Integer>());
					
				currentQuery = new ArrayList<Integer>();
				
				continue;
				
			}
			
			nextKeyWord = getNextKeyWord(currentQuery,TP,FP,min_support,betaE);
			
			if (nextKeyWord == NO_POSIBLE_QUERY && currentQuery.size() > 0){
				
				saveQuery(currentQuery,pW);
				
				TP = generateNewNotCoveredTP(TP);
				
//				TP = myArffHandler.getTPInstances(instances, new ArrayList<Integer>());
				
				FP = myArffHandler.getFPInstances(instances, new ArrayList<Integer>());
					
				currentQuery = new ArrayList<Integer>();
			
				nextKeyWord = getNextKeyWord(currentQuery,TP,FP,min_support,betaE);
			
			}
			
		}
		
		return qs;
		
	}

	@Override
	protected String getName() {
		return "Incremental";
	}

	private boolean acceptedNewKeyword(ArrayList<Integer> currentQuery,
			Integer nextKeyWord, ArrayList<Integer> TP,ArrayList<Integer> FP, double threshold, double min_supp, double beta) {
		
		double Agood = myArffHandler.getContainingAttribute(instances, nextKeyWord, TP).size();
		
		if (Agood<min_supp)
			return false;
		
		double Atotal = Agood + myArffHandler.getContainingAttribute(instances, nextKeyWord, FP).size();
		double Aprec = Agood/Atotal;
		double Arec = Agood/totalTP;
		
		double Amean = ((1.0 + Math.pow(beta, 2))*Aprec*Arec)/(Math.pow(beta, 2)*Aprec + Arec);
		
		return ((Amean/Pmean)>threshold);
	}

	private Integer getNextKeyWord(ArrayList<Integer> currentQuery,
			ArrayList<Integer> tP, ArrayList<Integer> fP, int minSupport, double beta) {
		
		Integer ret = NO_POSIBLE_QUERY;
		
		ArrayList<Integer> atts = myArffHandler.getAttributesWithMinSupport(instances,tP,minSupport);
		
		for (Integer integer : currentQuery) {
			atts.remove(integer);
		}
		
		int index = 0;
		
		double[] performances = new double[atts.size()];
		
		for (Integer integer : atts) {
		
			evaluateAttribute(currentQuery,integer,tP,fP);
			
			performances[index] = lastPerformance;
			
			System.out.println(instances.attribute(integer).name() + ":  " + lastPerformance );
			
			index++;

		}
		
		double maxValue = -1.0;
		
		double value;
		
		for (int i = 0; i < atts.size(); i++){
			
			value = performances[i];
			
			if (value > maxValue){
				
				ret = atts.get(i);
				
				maxValue = value;
				
			}
			
		}
		
		if (currentQuery.isEmpty()){
			
			ArrayList<Integer> query = new ArrayList<Integer>();
			
			query.add(ret);
			
			double TP = (double)myArffHandler.getTPInstances(instances, query).size();
			
			double FP = (double)myArffHandler.getFPInstances(instances, query).size();
			
			Pprec = TP / (TP + FP);
			Prec = TP / totalTP;
			
			Pmean = ((1.0 + Math.pow(beta, 2))*Pprec*Prec)/(Math.pow(beta, 2)*Pprec + Prec);
		
		}
		
		return ret;
		
	}

	private void evaluateAttribute(
			ArrayList<Integer> currentQuery, Integer newWord,
			ArrayList<Integer> tP, ArrayList<Integer> fP) {
		
		ArrayList<Integer> auxQuery = new ArrayList<Integer>(currentQuery);
		auxQuery.add(newWord);
		
		double[] values = new double[1];
		double[] weights = new double[1];
		
		double NumberTP = 0.0;
		
		
		NumberTP = myArffHandler.getContainingAttribute(instances, newWord, tP).size();
			
		double tpRate = (NumberTP)/(double)tP.size();
		
		double fpRate = 0.0;
		
		double NumberFP = 0.0;
		
		if (fP.size() > 0){
			
			NumberFP = myArffHandler.getContainingAttribute(instances, newWord, fP).size();
				
			fpRate = (NumberFP)/(double)fP.size();
		
		}
		
		values[0] = fpRate;
		
		weights[0] = wfp;
		
		lastPerformance =  calculateHMean(values,weights,tpRate);
		
	}

	private double calculateHMean(double[] values,double[] weights, double tpRate) {
		
		double sum = 0.0;
		
		double totalweight = 0.0;
		
		for (int i = 0; i < values.length; i++) {
			
			sum += (weights[i]/(CumulativeNormalStandard(tpRate,values[i])));
			
			totalweight+=weights[i];
		
		}
		
		return (totalweight/sum);
	}

	private static double CumulativeNormalStandard(double tpRate, double rate) {
		NormalDistributionImpl ndi = new NormalDistributionImpl();
		
		try {
			
			return (ndi.cumulativeProbability(tpRate)-ndi.cumulativeProbability(rate));

		} catch (MathException e) {
			
			e.printStackTrace();
		
		}
		
		return 0;
	}

	private ArrayList<Integer> generateNewNotCoveredTP(
			ArrayList<Integer> tP) {
		
		ArrayList<Integer> arr = myArffHandler.getTPInstances(instances, new ArrayList<Integer>());
		
		totalTP = arr.size();
		
		return arr;
	
	}

	private void saveQuery(ArrayList<Integer> currentQuery,persistentWriter pW) throws IOException {
		
		ArrayList<Integer> inst = myArffHandler.getFPInstances(instances, currentQuery);
		
		inst.addAll(myArffHandler.getTPInstances(instances, currentQuery));
		
		myArffHandler.removeInstances(instances, inst);
		
		TextQuery q = generateQuery(instances,currentQuery);
		
		Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
		
//		if (!qs.contains(q)){
//			qs.add(q);
//		} else {
//			return;
//		}

		qs.add(new Pair<TextQuery,Long>(q,Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM)));
		
//		pW.writeQuery(w_combination_ID, q,Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM));
		
	}

	private TextQuery generateQuery(Instances instances2,
			ArrayList<Integer> currentQuery) {
		
		List<String> ret = new ArrayList<String>(currentQuery.size());
		
		for (Integer integer : currentQuery) {
		
			ret.add(instances.attribute(integer).name());
			
		}
		
		return new TextQuery(ret);
	}

	private ArrayList<Integer> updateTP(ArrayList<Integer> currentQuery, Integer nextKeyWord,
			ArrayList<Integer> tP) {
		
		ArrayList<Integer> query = new ArrayList<Integer>(currentQuery);
//		
		query.add(nextKeyWord);
//		
		return myArffHandler.getTPInstances(instances,query);
//		
	}

	private ArrayList<Integer> updateFP(ArrayList<Integer> currentQuery,
			Integer nextKeyWord, ArrayList<Integer> fP) {

	
		return myArffHandler.getContainingAttribute(instances,nextKeyWord,fP);
	

	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize, int minSupport,
			int minSuppAfterUpdate) {
		return pW.getIncrementalParameter(maxQuerySize,minSupport,minSuppAfterUpdate,threshold,wfp,betaE);
	}

	

}
