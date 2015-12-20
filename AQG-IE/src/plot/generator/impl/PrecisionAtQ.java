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

public class PrecisionAtQ extends SampleGenerationSeriesGenerator {

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
		
		List<String> ret = new ArrayList<String>(4);
		
		ret.add(SeriesGenerator.QUERY_GENERATED);
		
		ret.add(SeriesGenerator.QUERY_POSITION);
		
		ret.add(SeriesGenerator.QUERY_ROUND);
		
		ret.add(QUERY_MAX);
		
		return ret;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>(3);
		
		ret.add(SeriesGenerator.QUERY_POSITION);
		
		ret.add(SeriesGenerator.QUERY_GENERATED);
		
		ret.add(SeriesGenerator.QUERY_ROUND);
		
		return ret;
		
	}

	@Override
	public List<String> generateGroupBy(){
		
		List<String> ret = new ArrayList<String>(3);
		
		ret.add(SeriesGenerator.QUERY_GENERATED);
		
		ret.add(SeriesGenerator.QUERY_POSITION);
		
		ret.add(SeriesGenerator.QUERY_ROUND);
	
		return ret;
		
	}
	
	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		getQueryGeneratedTable().clear();
		
		Series ret = new Series(execution.toString());
		
		double issuedQuery = 0;
		
		double lastIssuedQuery = NUMBER_OF_ISSUED_QUERIES;
		
		double count = 0;
		
		
		for (Hashtable<String, Double> hashtable : list) {
			
			if (!hasProcessed(hashtable.get(QUERY_GENERATED),hashtable.get(QUERY_ROUND))){
				issuedQuery++;
				getQueryGenerated(hashtable.get(QUERY_GENERATED)).add(hashtable.get(QUERY_ROUND));
			}
						
			if (issuedQuery > lastIssuedQuery){
				
				ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, count/lastIssuedQuery));
								
				lastIssuedQuery += NUMBER_OF_ISSUED_QUERIES;
				
			}
			
			double aux = hashtable.get(QUERY_MAX);
			
			if (aux > 0){
				count++;
			}
			
		}
		
		ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, count/lastIssuedQuery));
		
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
		return "Precision at Q";
	}

	@Override
	public String getAxisXTitle() {
		return "Number of queries Sent";
	}

	@Override
	public String getAxisYTitle() {
		return "Precision";
	}

	@Override
	protected Series invokeAverage(Series ret) {
//		return ret.averageSeries();
		return ret.independentAverage();
	}

}
