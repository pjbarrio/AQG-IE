package techniques.newideas.MSC.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import searcher.interaction.formHandler.TextQuery;
import utils.SVM.Rules.Candidateable;
import utils.arff.myArffHandler;
import weka.core.Instances;

public class MSCSet implements Candidateable{

	
	private static final String SEPARATOR = "_";
	private static final String ID_SEPARATOR = " ";
	private static final String QUERY_STATS_SEPARATOR = "<-->";
	private ArrayList<Integer> query;
	private double cost;
	private double benefit;
	private String id;

	public MSCSet(ArrayList<Integer> query, double cost, double benefit) {
		
		this.query = new ArrayList<Integer>();
		
		for (Integer integer : query) {
			
			this.query.add(integer);
			
		}
		
		this.cost = cost;
		this.benefit = benefit;
	
		generateId();
		
	}

	private void generateId() {
		
		Collections.sort(query);
		
		id = "";
		
		for (Integer integer : query) {
			
			id = id + integer + SEPARATOR;
			
		}
		
	}

	public MSCSet() {
		;
	}

	public TextQuery generateQuery(Instances instances) {
		
		List<String> ret = new ArrayList<String>(query.size());
		
		for (Integer attIndex : query) {
			
			ret.add(instances.attribute(attIndex.intValue()).name());
			
		}
		
//		ret += QUERY_STATS_SEPARATOR + hMean + " " + efficiency;
		
		return new TextQuery(ret);
	}

	public double getBenefitCostRatio(double pow) {
		return Math.pow(benefit, pow)/cost;
	}

	public boolean recalculateParameters(Instances instances, int minAfterUpdate) {
		
		benefit = 0;
		cost = 0;
		
		
		ArrayList<Integer> instTP = myArffHandler.getTPInstances(instances,query);
		
		benefit += instTP.size();
		cost += instTP.size();
	
		
		instTP.clear();
		
		if (benefit<=minAfterUpdate)
			return false;
		
		ArrayList<Integer> instFP = myArffHandler.getFPInstances(instances, query);
		
		cost = cost + instFP.size();
		
		instFP.clear();
		
		return true;
	}

	public ArrayList<Integer> getQuery() {
		return query;
	}


	@Override
	public Candidateable fromDisk(String t) {
		
		String[] spl = t.split(ID_SEPARATOR);
		
		this.id = spl[0];
		
		this.cost = Double.valueOf(spl[1]);
		
		this.benefit = Double.valueOf(spl[2]);
		
		generateQueryFromId();
		
		return this;
		
	}

	private void generateQueryFromId() {
		
		String[] spl = id.split(SEPARATOR);
		
		query = new ArrayList<Integer>();
		
		for (String string : spl) {
			
			query.add(Integer.valueOf(string));
			
		}
		
	}

	@Override
	public String toDisk() {
		return id + ID_SEPARATOR + cost + ID_SEPARATOR + benefit + ID_SEPARATOR;
	}

	public static String fromStringToQuery(String line) {
		
		return line.split(QUERY_STATS_SEPARATOR)[0].trim();
		
	}


}
