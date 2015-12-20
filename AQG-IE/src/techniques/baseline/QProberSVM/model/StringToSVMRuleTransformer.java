package techniques.baseline.QProberSVM.model;

import utils.SVM.Rules.Candidateable;
import utils.SVM.Rules.SVMRule;


public class StringToSVMRuleTransformer extends Transformer<Candidateable> {

	@Override
	public Candidateable generateObject(String t) {
		
		return new SVMRule().fromDisk(t);
		
	}

}
