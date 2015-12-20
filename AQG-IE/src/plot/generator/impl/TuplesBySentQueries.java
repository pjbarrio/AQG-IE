package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;

public class TuplesBySentQueries extends ExecutionSeriesGenerator {

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
		
		double issuedQuery = 1;
		
		double lastIssuedQuery = NUMBER_OF_ISSUED_QUERIES;
		
		double sum = 0;
		
		
		for (Hashtable<String, Double> hashtable : list) {
			
			issuedQuery = Math.max(issuedQuery,hashtable.get(EXECUTED_QUERY_POSITION));
						
			if (issuedQuery > lastIssuedQuery){
				
				ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, sum/normalizeYValue));
				
				
				lastIssuedQuery += NUMBER_OF_ISSUED_QUERIES;
				
			}
			
			sum += hashtable.get(USEFUL_TUPLES);
			
		}
		
		ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, sum/normalizeYValue));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}

	@Override
	public String getTitle() {
		return "Tuples by Issued Queries";
	}

	@Override
	public String getAxisXTitle() {
		return "Issued queries";
	}

	@Override
	public String getAxisYTitle() {
		return "Tuples Recall";
	}

	@Override
	protected String getNormalizedYAttribute() {
		return USEFUL_TOTAL_TUPLES;
//		return NO_NORMALIZATION;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}



}
