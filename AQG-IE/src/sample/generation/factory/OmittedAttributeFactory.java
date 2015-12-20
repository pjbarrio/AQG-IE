package sample.generation.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import exploration.model.enumerations.OmittedAttributeEnum;

import utils.persistence.persistentWriter;

public class OmittedAttributeFactory {

	public static List<String> generateList(persistentWriter pW,
			String relation, String whatToOmit) {

		switch (OmittedAttributeEnum.valueOf(whatToOmit)){

		case NONE: return new ArrayList<String>(0);

		case LARGE_DOMAIN: return Arrays.asList(pW.getAttributeWithLargeDomain(relation));
		
		case SMALL_DOMAIN: return Arrays.asList(pW.getAttributeWithSmallDomain(relation));
		
		default: return null;
		}

	}

}
