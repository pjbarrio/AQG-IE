package techniques.baseline.QProberSVM.model;

import utils.SVM.Rules.Candidateable;

public interface AcceptQuery {

	public boolean acceptQuery(Candidateable candidate);
	
}
