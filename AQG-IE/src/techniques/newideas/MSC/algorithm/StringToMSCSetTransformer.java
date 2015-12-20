package techniques.newideas.MSC.algorithm;

import techniques.baseline.QProberSVM.model.Transformer;
import techniques.newideas.MSC.model.MSCSet;
import utils.SVM.Rules.Candidateable;

public class StringToMSCSetTransformer extends Transformer<Candidateable> {

	@Override
	public Candidateable generateObject(String t) {
		
		return new MSCSet().fromDisk(t);
	}

}
