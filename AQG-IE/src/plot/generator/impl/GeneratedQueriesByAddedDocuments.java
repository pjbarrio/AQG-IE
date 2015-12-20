package plot.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;

public class GeneratedQueriesByAddedDocuments extends
		SampleGenerationSeriesGenerator {

	private Map<Double, Set<Double>> queryGeneratedTable;

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
		
		ret.add(SeriesGenerator.QUERY_GENERATED);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);
		
		return ret;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>(1);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION);
		
		ret.add(SeriesGenerator.DOCUMENT_IN_QUERY_POSITION);
		
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		Series ret = new Series(execution.toString());
		
		double addedDocument = 0;
		
		double lastAddedDocument = NUMBER_OF_ADDED_DOCUMENTS-1;
		
		double processed = 0;
				
		for (Hashtable<String, Double> hashtable : list) {
			
			addedDocument = Math.max(addedDocument,hashtable.get(DOCUMENT_POSITION_IN_SAMPLE));
						
			if (addedDocument > lastAddedDocument){
				
				ret.addPair(Pair.getPair((lastAddedDocument + 1)/normalizeXValue, processed/normalizeYValue));
								
				lastAddedDocument += NUMBER_OF_ADDED_DOCUMENTS;
				
			}
			
			double aux = hashtable.get(QUERY_GENERATED);
			
			if (aux > processed)
				processed = aux;
			
		}
		
		//not sure if I should add this.
		
		if (addedDocument > lastAddedDocument)
			ret.addPair(Pair.getPair((lastAddedDocument + 1)/normalizeXValue, processed/normalizeYValue));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}
		
	@Override
	public String getTitle() {
		return "Generated Queries by Added Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Added Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Generated Queries";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}


}
