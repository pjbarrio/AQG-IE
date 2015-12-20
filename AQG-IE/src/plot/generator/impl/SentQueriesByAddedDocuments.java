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

public class SentQueriesByAddedDocuments extends
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
		
		ret.add(SeriesGenerator.QUERY_ROUND);
		
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
		
		getQueryGeneratedTable().clear();
		
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
			
			if (!hasProcessed(hashtable.get(QUERY_GENERATED),hashtable.get(QUERY_ROUND))){
				processed++;
				getQueryGenerated(hashtable.get(QUERY_GENERATED)).add(hashtable.get(QUERY_ROUND));
			}
			
		}
		
		//not sure if I should add this.
		
		if (addedDocument > lastAddedDocument)
			ret.addPair(Pair.getPair((lastAddedDocument + 1)/normalizeXValue, processed/normalizeYValue));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}

	private boolean hasProcessed(Double queryGenerated, Double round) {
		
		return getQueryGenerated(queryGenerated).contains(round);
		
	}

	private Set<Double> getQueryGenerated(Double queryGenerated) {
		
		Set<Double> ret = getQueryGeneratedTable().get(queryGenerated);
		
		if (ret == null){
			ret = new HashSet<Double>();
			getQueryGeneratedTable().put(queryGenerated,ret);
		}
		
		return ret;
	}

	private Map<Double,Set<Double>> getQueryGeneratedTable() {
		
		if (queryGeneratedTable == null){
			queryGeneratedTable = new HashMap<Double, Set<Double>>();
		}
		return queryGeneratedTable;
	}
	
	@Override
	public String getTitle() {
		return "Sent Queries by Added Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Added Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Sent Queries";
	}

	@Override
	protected Series invokeAverage(Series ret) {
//		return ret.independentAverage();
		return ret.averageSeries();
	}

}
