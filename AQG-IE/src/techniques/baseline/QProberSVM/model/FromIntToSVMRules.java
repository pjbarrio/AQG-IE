package techniques.baseline.QProberSVM.model;

import java.util.ArrayList;
import java.util.Hashtable;

import utils.SVM.Rules.SVMRule;

public class FromIntToSVMRules implements FromIntToT {

	
	private Hashtable<Integer, Double> weights;

	public FromIntToSVMRules(Hashtable<Integer, Double> weights) {
		this.weights = weights;
	}

	@Override
	public Object createObject(ArrayList<Integer> generateValues) {
		
		return new SVMRule(generateValues,weights);
	
	}

}
