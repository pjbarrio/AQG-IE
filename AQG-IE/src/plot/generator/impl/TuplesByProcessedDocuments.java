package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;


public class TuplesByProcessedDocuments extends ExecutionSeriesGenerator {

	@Override
	public String getAxisXTitle() {
		return "Processed Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Tuples Recall";
	}

	@Override
	public String getTitle() {
		return "Tuples by Processed Documents";
	}

	@Override
	public List<String> generateAttributes() {

		List<String> ret = new ArrayList<String>();
		
		ret.add(USEFUL_TUPLES);
		
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
		
		double i = 1.0;
		
		double sum = 0.0;
		
		int norm = (int) Math.round(NUMBER_OF_PROCESSED_DOCUMENTS * normalizeXValue);
		
		double lastIndex = NUMBER_OF_PROCESSED_DOCUMENTS;
		
		boolean needsOneMore = true;
		
		for (Hashtable<String, Double> hashtable : list) {
			
			sum += hashtable.get(USEFUL_TUPLES);
			
			needsOneMore = true;
			
			if (i % norm == 0.0){
				ret.addPair(Pair.getPair(lastIndex,sum/normalizeYValue));
				lastIndex += NUMBER_OF_PROCESSED_DOCUMENTS;
				needsOneMore = false;
			}
			
			i++;
			
		}
		
		if (needsOneMore)
			ret.addPair(Pair.getPair(lastIndex, sum/normalizeYValue));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_PROCESSED_DOCUMENTS);

		return ret;
		
	}

	@Override
	protected String getNormalizedYAttribute() {
		return USEFUL_TOTAL_TUPLES;
//		return NO_NORMALIZATION;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return TOTAL_DOCUMENTS;
//		return NO_NORMALIZATION;
	}

	@Override
	public boolean getPercentX() {
		return true;
	}

	
}
