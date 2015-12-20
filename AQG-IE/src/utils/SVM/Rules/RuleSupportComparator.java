package utils.SVM.Rules;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import utils.arff.myArffHandler;
import weka.core.Instances;

public class RuleSupportComparator implements Comparator<SVMRule> {

	private Hashtable<String, Double> supp;

	public RuleSupportComparator(List<SVMRule> list, Instances data){
		
		long totalTp = myArffHandler.yesInstances(data);
		
		supp = new Hashtable<String, Double>();
		
		long tp,fp;
		
		for (SVMRule svmRule : list) {
			
			myArffHandler.calculatePositivesNegatives(data, svmRule.getFeatures());
			
			tp = myArffHandler.getTP(data, svmRule.getFeatures());
			
			fp = myArffHandler.getFP(data, svmRule.getFeatures());
			
			myArffHandler.cleanPositivesNegatives();
			
			supp.put(svmRule.getId(), new Double(getFMeasure(tp,fp,totalTp)));
			
		}
		
	}
	
	private double getFMeasure(long tp, long fp, long totalTp) {
		double precision = (double)tp/((double)tp+fp);
		double recall = (double)tp/(double)totalTp;
		
		return (2.0 * ((precision*recall)/(precision+recall)));
	}

	@Override
	public int compare(SVMRule arg0, SVMRule arg1) {
		
		return supp.get(arg1.getId()).compareTo(supp.get(arg0.getId()));
	
	}

	public void clear() {
		
		supp.clear();
		
	}

}
