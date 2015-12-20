package sample.generation.model.cardinality.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exploration.model.Database;
import sample.generation.model.SampleConfiguration;
import sample.generation.model.cardinality.CardinalityFunction;

public class SizeProportionalCardinality implements CardinalityFunction {

	private static Map<Integer,Integer> prop = null;
	private int total;
	private int usefulSize;
	private int uselessSize;
	
	@Override
	public void calculateRequests(List<Database> databases,
			SampleConfiguration sampleConfiguration) {
		
		total = -1;
		
		usefulSize = sampleConfiguration.getUsefulNumber();
		uselessSize = sampleConfiguration.getUselessNumber();
		
		for (int i = 0; i < databases.size(); i++) {
			
			total += getProps().get(databases.get(i).getId());
						
		}
				
	}

	@Override
	public int getUsefulDocuments(Database database) {
		
		double val = Math.round(((double)getProps().get(database.getId())/(double)total)*(double)usefulSize);
		
		return (int)val;
		
	}

	@Override
	public int getUselessDocuments(Database database) {
		
		double val = Math.round(((double)getProps().get(database.getId())/(double)total)*(double)uselessSize);
		
		return (int)val;
		
	}

	private Map<Integer,Integer> getProps(){
		
		has to load the proportions accordingly.
		
	}
}
