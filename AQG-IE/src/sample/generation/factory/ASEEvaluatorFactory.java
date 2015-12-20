package sample.generation.factory;

import sample.generation.relation.attributeSelection.impl.ChiSquaredWithYatesCorrectionAttributeEval;
import sample.generation.relation.attributeSelection.impl.SMOAttributeEval;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.InfoGainAttributeEval;

public class ASEEvaluatorFactory {

	public static ASEvaluation generateInstance(String name) {
		
		if (name.equals("SMO")){
			return new SMOAttributeEval();
		}
		if (name.equals("CHIYC")){
			return new ChiSquaredWithYatesCorrectionAttributeEval();
		}
		if (name.equals("INFOGAIN")){
			return new InfoGainAttributeEval();
		}
		
		return null;
	}

}
