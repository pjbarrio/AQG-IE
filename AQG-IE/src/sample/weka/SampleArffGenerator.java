package sample.weka;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;

import domain.caching.candidatesentence.CachCandidateSentencesRelationship;

import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractorAbs;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.RuleStats;
import weka.core.FastVector;
import weka.core.Instances;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;
import exploration.model.Document;
import exploration.model.DocumentHandle;


public class SampleArffGenerator extends JRip{

	
	
	/**
	 * 
	 */
	private Instances data;
	private String stopWordsFile;
	private ArrayListLoader al;
	private WordExtractorAbs usefulWE;
	private WordExtractorAbs generalWE;

	public SampleArffGenerator(String stopWords, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) {
		
		stopWordsFile = stopWords;
			
		this.usefulWE = usefulWE;
			
		this.generalWE = generalWE;
		
	}

	public SampleArffGenerator() {
		// TODO Auto-generated constructor stub
	}

	public void learn(ContentExtractor ce, ContentLoader cl, persistentWriter pW, Collection<Document> sample, Collection<Document> useful, String arffOuputFile, Set<Tuple> tuples, boolean frequency,boolean stemmed, boolean lowercase, boolean createInstances) {
		// sample is the whole dataset (contains useful)
		// useful are the useful documents.
		// cl applies to all so far. Unless otherwise indicated.
		
		al = new ArrayListLoader(pW, sample,useful,stopWordsFile,tuples, frequency,stemmed, lowercase, createInstances,usefulWE,generalWE);
		
		if (arffOuputFile != null){
			al.printFile(arffOuputFile);
		}
		
	}

	


	public String toString() {
		
	    StringBuffer sb = new StringBuffer("");
	    try {
	    for(int j=0; j<getRuleset().size(); j++){
	      RuleStats rs = (RuleStats)getRuleStats(j);
	      FastVector rules = rs.getRuleset();
	      for(int k=0; k<rules.size(); k++){
		double[] simStats = rs.getSimpleStats(k);
		sb.append(((JRip.RipperRule)rules.elementAt(k)).toString(data.attribute(0))
			  + " ("+simStats[0]+"/"+simStats[4]+")\n");
	      }
	    }
	    
	    }catch (java.lang.ArrayIndexOutOfBoundsException e){
	    	;
	    }
	    return sb.toString();
	  }

	public Instances getInstances() {
		try {
			return al.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
