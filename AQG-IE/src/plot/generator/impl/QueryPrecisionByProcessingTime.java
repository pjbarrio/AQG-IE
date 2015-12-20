package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;

public class QueryPrecisionByProcessingTime extends ExecutionSeriesGenerator {

	@Override
	protected String getNormalizedYAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	public List<String> generateAttributes() {
		List<String> ret = new ArrayList<String>();
		
		ret.add(USEFUL_TUPLES);
		ret.add(CURRENT_TIME);
		
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

		double time = 0;
		
		double sum = 0;
		
		double processed = 0;
		
		for (Hashtable<String, Double> hashtable : list) {
			
			processed++;
			
			double tuples = hashtable.get(USEFUL_TUPLES);
			
			if (tuples >0)
				sum++; 
			
			time = hashtable.get(CURRENT_TIME) / 1000.0;
			
			ret.addPair(Pair.getPair(time,sum/processed));
			
		}
		
		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_SECONDS_INTERVAL);

	}

	@Override
	public String getTitle() {
		return "Query Precision By Processing Time";
	}

	@Override
	public String getAxisXTitle() {
		return "Processing Time";
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
