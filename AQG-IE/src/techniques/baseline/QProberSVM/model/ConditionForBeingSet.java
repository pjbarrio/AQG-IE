package techniques.baseline.QProberSVM.model;

import utils.SVM.Rules.Candidateable;

public abstract class ConditionForBeingSet {

	public abstract boolean accept (Candidateable union);
	
}
