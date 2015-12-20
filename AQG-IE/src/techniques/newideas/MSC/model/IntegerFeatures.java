package techniques.newideas.MSC.model;

import java.util.ArrayList;
import java.util.Collections;

import utils.SVM.Rules.Candidateable;

public class IntegerFeatures implements Candidateable {

	private static final String SEPARATOR = "_";
	public static final int INTEGER_FEATURES_A_FILE = 7500;
	private String id;
	
	private ArrayList<Integer> features;

	public IntegerFeatures(ArrayList<Integer> t) {
		
		features = new ArrayList<Integer>();
		
		for (Integer integer : t) {
			
			features.add(integer);
			
		}
		
		Collections.sort(features);
		
		generateId();
		
	}

	private void generateId() {
		
		id = "";
		
		for (Integer integer : features) {
			
			id = id + integer + SEPARATOR;
			
		}
		
	}

	public IntegerFeatures() {
		;
	}

	@Override
	public Candidateable fromDisk(String t) {
		
		features = new ArrayList<Integer>();
		
		for (String feat : t.split(SEPARATOR)) {
			
			features.add(Integer.valueOf(feat));
			
		}
		
		id = t;
		
		return this;
		
	}

	@Override
	public String toDisk() {
		
		return id;
	}

	public ArrayList<Integer> getFeatures() {
		
		return features;

	}

	public boolean equals(Object o){
		
		return (id.equals(((IntegerFeatures)o).id));
		
	}
	
	public int hashCode(){
		return id.hashCode();
	}

}
