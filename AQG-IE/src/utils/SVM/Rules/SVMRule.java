package utils.SVM.Rules;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import searcher.interaction.formHandler.TextQuery;
import utils.arff.myArffHandler;
import weka.core.Instances;


public class SVMRule implements Candidateable{

	public static final int SVMRULES_A_FILE = 7500;
	private static final String AND_STRING = "^";
	private static final String SPLIT_STRING = " <-> ";
	private static final String FINALLY_STRING = " -> ";
	private static final String FINALLY_RETRIEVE_STRING = " -> \\^";
	private static final long NO_VALUE = -1;
	private ArrayList<Integer> features;
	private double precision;
	private double support;
	private long TP;
	private String id;
	private long FP;
	private double fMeasure;
	private double recall;
	private long reducedTP;
	private ArrayList<SVMRule> parents;
	public static Instances dataSt = null;
	public static Hashtable<Integer, Double> weightsSt = null;
	
	public SVMRule(ArrayList<Integer> feats) {
		parents = null;
		TP = NO_VALUE;
		FP = NO_VALUE;
		reducedTP = NO_VALUE;
		
		features = new ArrayList<Integer>();
		
		for (Integer string : feats) {

			features.add(string);
		
		}

		support = 0;
		
		id = "";
		
		Collections.sort(features);
		
		for (Integer feature : features) {
			
			support += weightsSt.get(feature);
			
			id = id + AND_STRING + feature;
			
		}
		
	}

	public long getTP(Instances data){
		
		TP = myArffHandler.getInstancesContainingFeatures(data,features, myArffHandler.YES_VALUE, myArffHandler.YES_VALUE).size();
		
		return TP;
	}
	
	public long getFP(Instances data){

		FP = myArffHandler.getInstancesContainingFeatures(data,features, myArffHandler.NO_VALUE, myArffHandler.YES_VALUE).size();
		
		return FP;
	}
	
	public SVMRule(double prec, double sup, long tp2, String id2, double fMea,
			double rec, long red, ArrayList<Integer> feats) {
		
		precision = prec;
		support = sup;
		this.TP = tp2;
		this.id = id2;
		fMeasure = fMea;
		recall = rec;
		reducedTP = red;
		features = feats;
		
	}

	public SVMRule() {
		precision = support = TP = (long) (fMeasure = recall = reducedTP = NO_VALUE);
	}

	public SVMRule(ArrayList<Integer> generateValues,
			Hashtable<Integer, Double> weights) {

		TP = NO_VALUE;
		FP = NO_VALUE;
		reducedTP = NO_VALUE;
		
		features = new ArrayList<Integer>();
		
		for (Integer string : generateValues) {

			features.add(string);
		
		}

		support = 0;
		
		id = "";
		
		Collections.sort(features);
		
		for (Integer feature : features) {
			
			support += weights.get(feature);
			
			id = id + AND_STRING + feature;
			
		}
	}

	public ArrayList<Integer> getFeatures() {
		
		return features;
	
	}

	public String toString(){
		
		String str = Double.toString(precision) + SPLIT_STRING + Double.toString(recall) + SPLIT_STRING + Double.toString(fMeasure) + FINALLY_STRING;
		
		str += id;
		
		return str;
		
	}

	public void setPrecision(double d) {
		
		this.precision = d;
		
	}

	public double getSupport() {
		return support;
	}
	
	public boolean equals(Object o){
		
		SVMRule svm = (SVMRule)o;
		
		return this.getId().equals(svm.getId());
		
	}

	public int hashCode(){
		return id.hashCode();
	}
	
	public String getId() {
		return id;
	}

	public void calculatePrecision(Instances data) {
		
		long posCant = this.getTP(data);
		
		long negCant = this.getFP(data);
		
		if (posCant==0 && negCant==0)
			this.setPrecision(0.0);
		else
			this.setPrecision((double)posCant / ((double)posCant+(double)negCant));
		
	}
	
	public boolean matchAllButK(SVMRule svmRule, int k) {
		
		int i = k;
		
		for (Integer feat : features) {
			if (!svmRule.features.contains(feat))
				i--;
			if (i<0) //More than K Non-Matchings
				return false;
		}
		
		if (i==0) //Only one not matching
			return true;
		
		return false; //All Matching
	}

	public double getPrecision() {
		
		return precision;
		
	}

	public void setFMeasure(double fMeasureA) {
		
		this.fMeasure = fMeasureA;
		
	}

	public void setRecall(double rA) {
		
		this.recall = rA;
		
	}

	public static String[] getWords(String line) {

		System.out.println(line);
		
		String[] spl = line.split(FINALLY_RETRIEVE_STRING);
		
		spl = spl[1].replaceAll(" ", "").split("\\^");

		return spl;
	}

	public ArrayList<SVMRule> getAncestors() {
		
		ArrayList<SVMRule> ret = new ArrayList<SVMRule>();
		
		ArrayList<Integer> aux = new ArrayList<Integer>();
		
		for (Integer feat : features) {
			
			aux.clear();
			
			aux.add(feat);
			
			ret.add(new SVMRule(aux));
		}
		
		return ret;
	}

	public long getTP(Instances data, int t, boolean updatedModel) {
		
		if (updatedModel || reducedTP==NO_VALUE){
			reducedTP = myArffHandler.getTP(data, getFeatures(),t);
		}
		
		return reducedTP;		
	
	}

	@Override
	public Candidateable fromDisk(String t) {
		
		String[] spl = t.split(" ");

		id = spl[0];

		support = Double.parseDouble(spl[1]);
		
		features = new ArrayList<Integer>();
		
		for (int i = 2; i < spl.length; i++) {
			
			features.add(Integer.parseInt(spl[i]));
			
		}

		this.precision = NO_VALUE;
		this.TP = NO_VALUE;
		this.fMeasure = NO_VALUE;
		this.recall = NO_VALUE;
		this.reducedTP = NO_VALUE;
		
		return this;

		
	}

	@Override
	public String toDisk() {
		
		String ret = "";
		
		ret = id + " " + support;
		
		for (Integer feature : features) {
		
		ret = ret + " " + feature;
		
		}
		
		return ret;
		
	}

	public ArrayList<SVMRule> getParents() {

		if (parents != null)
			return parents;
		
		
		ArrayList<SVMRule> ret = new ArrayList<SVMRule>();
		
		ArrayList<Integer> featsParent = new ArrayList<Integer>();
		
		for (int leaveOut = features.size()-1; leaveOut >= 0; leaveOut--) {
			
			featsParent.clear();
			
			for (int i = 0; i < features.size(); i++) {
				
				if (i!=leaveOut)
					featsParent.add(features.get(i));
				
			}
			
			ret.add(new SVMRule(featsParent));
			
		}
	
		parents = ret;
		
		return ret;
		
	}

	public TextQuery toQuery() {
		
		List<String> ret = new ArrayList<String>(features.size());
		
		for (Integer feat : features) {
			ret.add(dataSt.attribute(feat).name());
		}
		return new TextQuery(ret);
	}
	
	public int size(){
		return features.size();
	}
	
}
