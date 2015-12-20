package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;

public class TuplesByProcessingTime extends ExecutionSeriesGenerator {

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
		
		double lastIndex = NUMBER_OF_SECONDS_INTERVAL;
		
		boolean needsOneMore = true;

		
		for (Hashtable<String, Double> hashtable : list) {
			
			sum += hashtable.get(USEFUL_TUPLES);
			
			time = hashtable.get(CURRENT_TIME) / 1000.0;
			
			needsOneMore = true;
			
			if (time >= lastIndex){
				ret.addPair(Pair.getPair(lastIndex/normalizeXValue,sum/normalizeYValue));
				lastIndex += NUMBER_OF_SECONDS_INTERVAL;
				needsOneMore = false;
			}
			
		}
		
		if (needsOneMore){
			ret.addPair(Pair.getPair(lastIndex/normalizeXValue, sum/normalizeYValue));
		}
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_SECONDS_INTERVAL);

		return ret;
		
	}

	@Override
	public String getTitle() {
		return "Tuples Recall by Processing Time";
	}

	@Override
	public String getAxisXTitle() {
		return "Time (seconds)";
	}

	@Override
	public String getAxisYTitle() {
		return "Tuples Recall";
	}

	@Override
	protected String getNormalizedYAttribute() {
//		return USEFUL_TOTAL_TUPLES;
		return NO_NORMALIZATION;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}

}
