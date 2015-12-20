package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;

public class QueryPrecisionBySentQueries extends ExecutionSeriesGenerator {

	private static final double NO_PRECISION_CALCULATED = 1;

	@Override
	protected String getNormalizedYAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	public List<String> generateAttributes() {
		List<String> ret = new ArrayList<String>();
		
		ret.add(USEFUL_TUPLES);
		
		ret.add(EXECUTED_QUERY_POSITION);
		
		return ret;
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>();
		
		ret.add(CURRENT_TIME);
		ret.add(PROCESSED_DOCUMENT_POSITION);
		
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		Series ret = new Series(execution.toString());
		
		int issuedQuery = 1;
		
		double sum = 0;
		
		double processed = 0;
		
		Hashtable<Integer, Double> precisionTable = new Hashtable<Integer, Double>();
		
		for (Hashtable<String, Double> hashtable : list) {
			
			processed++;
			
			double tuples = hashtable.get(USEFUL_TUPLES);
			
			if (tuples>0)
				sum++; 
			
			issuedQuery = (int) Math.max(issuedQuery, hashtable.get(EXECUTED_QUERY_POSITION));
			
			precisionTable.put(issuedQuery,sum/processed);
			
		}
		
		for (int i = 1; i <= issuedQuery; i++) {
			
			if (precisionTable.containsKey(i))
				ret.addPair(Pair.getPair(i,precisionTable.get(i)));
			else {
				if (i==1){
					precisionTable.put(i,NO_PRECISION_CALCULATED);
					ret.addPair(Pair.getPair(i,NO_PRECISION_CALCULATED));
				}else{
					precisionTable.put(i,precisionTable.get(i-1));
					ret.addPair(Pair.getPair(i,precisionTable.get(i-1)));
				}
				
			}
		}
				
		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);

		
	}

	@Override
	public String getTitle() {
		
		return "Query Precision By Sent Queries";
		
	}

	@Override
	public String getAxisXTitle() {
		
		return "Sent Queries";
	
	}

	@Override
	public String getAxisYTitle() {
		
		return "Query Precision";
		
	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}

}
