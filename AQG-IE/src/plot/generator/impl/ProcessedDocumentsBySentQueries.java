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

public class ProcessedDocumentsBySentQueries extends
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
		
		ret.add(SeriesGenerator.QUERY_ROUND);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION);
		
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
		
		double issuedQuery = 0;
		
		double lastIssuedQuery = NUMBER_OF_ISSUED_QUERIES;
		
		double processed = 0;
		
		
		for (Hashtable<String, Double> hashtable : list) {
			
			if (!hasProcessed(hashtable.get(QUERY_GENERATED),hashtable.get(QUERY_ROUND))){
				issuedQuery++;
				getQueryGenerated(hashtable.get(QUERY_GENERATED)).add(hashtable.get(QUERY_ROUND));
			}
						
			if (issuedQuery > lastIssuedQuery){
				
				ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, processed/normalizeYValue));
								
				lastIssuedQuery += NUMBER_OF_ISSUED_QUERIES;
				
			}
			
			double aux = hashtable.get(DOCUMENT_POSITION);
			
			if (aux > processed){
				processed = aux;
			}
			
		}
		
		ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, processed/normalizeYValue));
		
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
		return "Processed Documents by sent queries";
	}

	@Override
	public String getAxisXTitle() {
		return "Number of queries Sent";
	}

	@Override
	public String getAxisYTitle() {
		return "Processed Documents";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
//		return ret.independentAverage();
	}

}
