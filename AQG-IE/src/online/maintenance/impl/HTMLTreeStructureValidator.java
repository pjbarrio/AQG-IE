package online.maintenance.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;

import exploration.model.Database;

import online.maintenance.HTMLValidator;
import online.maintenance.ctm.ClusteredTreeMatching;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;

public class HTMLTreeStructureValidator extends HTMLValidator {

	private double threshold;
	private ClusteredTreeMatching clusteredTreeMatching;

	public HTMLTreeStructureValidator(Database website,
			persistentWriter persistentWriter) {
		
		threshold = persistentWriter.getHTMLValidatorThreshold(website,getName());
		
		clusteredTreeMatching = new ClusteredTreeMatching();
		
	}

	@Override
	public String getName() {
		
		return "CTM";
		
	}

	@Override
	public boolean isValid(Node n1, Node n2) {
		
		return clusteredTreeMatching.clusteredTreeMatching(n1, n2, 1, 1) >= threshold;
		
	}

	public double getThreshold(){
		return threshold;
	}
	
	
}
