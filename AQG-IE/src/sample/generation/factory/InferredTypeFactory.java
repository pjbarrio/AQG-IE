package sample.generation.factory;

import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.persistence.persistentWriter;

public class InferredTypeFactory {

	public static List<String> getInferredTypes(String relation, persistentWriter pW) {
		
		return pW.getInferredTypes(relation);
		
	}

	public static List<String> getNoFilteringFields(String relation, persistentWriter pW) {
		
		return pW.getNoFilteringFields(relation);
		
	}

}
