package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;

public class UsefulDocumentsByQueryPosition extends SampleGenerationSeriesGenerator {

	@Override
	protected String getNormalizedYAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	public List<String> generateAttributes() {
		
		List<String> ret = new ArrayList<String>(2);
		
		ret.add(SeriesGenerator.QUERY_POSITION);
		
		ret.add(SeriesGenerator.DOCUMENT_IN_QUERY_POSITION);
		
		ret.add(SeriesGenerator.USEFUL_TUPLES);
		
		return ret;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>(2);
		
		ret.add(SeriesGenerator.QUERY_POSITION);
		ret.add(SeriesGenerator.DOCUMENT_IN_QUERY_POSITION);
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		Series ret = new Series(execution.toString());
		
		if (list.isEmpty())
			return ret;
		
		int lastQuery = (int)(double)list.get(list.size()-1).get(QUERY_POSITION);
		
		double lastIssuedQuery = NUMBER_OF_ISSUED_QUERIES;
		
		double useful = 0.0;
		double cant = 0.0;
					
		for (int i = 1; i <= lastQuery; i++) {
			
			if (i > lastIssuedQuery){
				
				if (cant>0)
					ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, (useful/cant)/normalizeYValue));
				else
					ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, 0.0));
								
				lastIssuedQuery += NUMBER_OF_ISSUED_QUERIES;

				useful = 0.0;
				
				cant = 0.0;
				
			}
			
			while (!list.isEmpty() && list.get(0).get(QUERY_POSITION) == i){			
				
				double doc_in_qp = list.get(0).get(DOCUMENT_IN_QUERY_POSITION);
				
				double tuples = list.remove(0).get(USEFUL_TUPLES);
				
				if (doc_in_qp > 0){
				
					cant++;
					
					if (tuples>0){
						useful++;
					}

				}
				
			}
		
		}
		
		if (cant>0)
			ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, (useful/cant)/normalizeYValue));
		else
			ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, 0.0));

		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
	
	}

	@Override
	public String getTitle() {
		return "Useful Documents by Sent Queries";
	}

	@Override
	public String getAxisXTitle() {
		return "Sent queries";
	}

	@Override
	public String getAxisYTitle() {
		return "Useful Documents";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}

}
