package techniques.baseline.QProberSVM.model;

import utils.SVM.Rules.Candidateable;

public class StringToBitIntSetTransformer extends Transformer<Candidateable> {

	@Override
	public Candidateable generateObject(String t) {
		return new BitIntSet().fromDisk(t);
	}

}
