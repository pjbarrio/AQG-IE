package techniques.newideas.MSC.model;

import techniques.baseline.QProberSVM.model.Transformer;
import utils.SVM.Rules.Candidateable;

public class StringToFeaturesTRansformer extends Transformer<Candidateable> {

	@Override
	public Candidateable generateObject(String t) {
		return new IntegerFeatures().fromDisk(t);
	}

}
