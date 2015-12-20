package plot.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.sample.wordsDistribution.HashBasedComparator;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;

public class QueriesWithNoResults extends SampleGenerationSeriesGenerator {

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
		
		List<String> ret = new ArrayList<String>();
		
		ret.add(SeriesGenerator.QUERY_POSITION);
		
		ret.add(SeriesGenerator.QUERY_GENERATED);
		
		ret.add(SeriesGenerator.QUERY_ROUND);
		
		return ret;
		
	}

	@Override
	public List<String> generateGroupBy(){
		
		List<String> ret = new ArrayList<String>();
		
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
		
		Map<Integer,Integer> freq = new HashMap<Integer, Integer>();
		
		List<Integer> distances = new ArrayList<Integer>();
		
		int lastUsefulQuery = 0;
		
		int queryPosition = 0;
		
		for (Hashtable<String, Double> hashtable : list){
			
			if (!hasProcessed(hashtable.get(QUERY_GENERATED),hashtable.get(QUERY_ROUND))){
				queryPosition++;
				getQueryGenerated(hashtable.get(QUERY_GENERATED)).add(hashtable.get(QUERY_ROUND));
			}
			
			int max = (int)Double.valueOf(hashtable.get(QUERY_MAX)).doubleValue();
			
			if (max > 0){
				
				int distance = queryPosition - lastUsefulQuery;
				
				lastUsefulQuery = queryPosition;
				
				Integer frequ = freq.remove(distance);
				
				if (frequ == null){
					
					frequ = 0;
					
					distances.add(distance);
					
				}
				
				freq.put(distance, frequ + 1);
				
			}
			
		}
		
		if (!distances.isEmpty()){
		
			Collections.sort(distances);
			
			for (int i = 1; i < distances.get(distances.size()-1); i++) {
				
				double value = 0;
				
				if (distances.get(0).equals(i)){
					
					distances.remove(0);
					
					value = freq.get(i);
					
				}
				
				ret.addPair(Pair.getPair(i,value));
	
			}
			
		}
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
		return "Useless queries in between useful queries";
	}

	@Override
	public String getAxisXTitle() {
		return "Number of useless queries";
	}

	@Override
	public String getAxisYTitle() {
		return "Number of samples";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}


	
}
