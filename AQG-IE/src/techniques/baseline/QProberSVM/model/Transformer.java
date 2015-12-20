package techniques.baseline.QProberSVM.model;

import utils.SVM.Rules.Candidateable;


public abstract class Transformer<T extends Candidateable> {
	public abstract T generateObject(String t);
}
