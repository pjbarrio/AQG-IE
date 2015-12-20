package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;

public class UsefulDocumentsBySentQueries extends ExecutionSeriesGenerator {

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
			
			if (hashtable.get(USEFUL_TUPLES) > 0)
				sum++;
			
		}
		
		ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, sum/normalizeYValue));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
	
	}

	@Override
	public String getTitle() {
		return "Useful Documents by Issued queries";
	}

	@Override
	public String getAxisXTitle() {
		return "Issued Queries";
	}

	@Override
	public String getAxisYTitle() {
		return "Useful Documents Recall";
	}

	@Override
	protected String getNormalizedYAttribute() {
		return USEFUL_TOTAL_DOCUMENTS;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}



}
