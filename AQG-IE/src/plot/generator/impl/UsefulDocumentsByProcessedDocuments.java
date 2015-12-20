package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;

public class UsefulDocumentsByProcessedDocuments extends ExecutionSeriesGenerator {

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
			
			if (hashtable.get(USEFUL_TUPLES) > 0)
				sum++;
			
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
	public String getTitle() {
		return "Useful Documents by Processed Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Processed Documents";
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
		return TOTAL_DOCUMENTS;
	}
	
	@Override
	public boolean getPercentX() {
		return true;
	}

}
