package plot.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;

public class RoundBySampleSize extends SampleGenerationSeriesGenerator {

	private Map<Double, Map<Double, Double>> queriesTable;
	
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
		
		List<String> ret = new ArrayList<String>(3);
		
		ret.add(SeriesGenerator.QUERY_GENERATED);
		
		ret.add(SeriesGenerator.QUERY_ROUND);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);
		
		return ret;
	
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>(3);
		
		ret.add(SeriesGenerator.QUERY_POSITION);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION);
		
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		getQueriesTable().clear();
		
		Series ret = new Series(execution.toString());
		
		double addedDocument = 0;
	
		double lastAddedDocument = NUMBER_OF_ADDED_DOCUMENTS-1;
		
		double actualRound = 0;
		
		double analyzedRound = 0;
		
		boolean needOneMore = false;
		
		for (Hashtable<String, Double> hashtable : list) {
			
			addedDocument = Math.max(addedDocument,hashtable.get(DOCUMENT_POSITION_IN_SAMPLE));
			
			if (addedDocument > lastAddedDocument){
				
				needOneMore = false;
				
				ret.addPair(Pair.getPair((lastAddedDocument + 1)/normalizeXValue, actualRound/normalizeYValue));
								
				lastAddedDocument += NUMBER_OF_ADDED_DOCUMENTS;
				
			}
			
			Double round = getRound(hashtable.get(SeriesGenerator.QUERY_GENERATED),hashtable.get(SeriesGenerator.QUERY_ROUND));
			
			if (round == null){
				
				needOneMore = true;
				
				analyzedRound++;
				
				setRound(hashtable.get(SeriesGenerator.QUERY_GENERATED),hashtable.get(SeriesGenerator.QUERY_ROUND),analyzedRound);
				
				round = analyzedRound;
				
			}
			
			if (round > actualRound){
				actualRound = round;
			}
						
			
		}
		
		if (needOneMore)
			ret.addPair(Pair.getPair((lastAddedDocument + 1)/normalizeXValue, actualRound/normalizeYValue));
		
		return ret;
		
	}

	private void setRound(double queryGen, double round, double value) {
		
		getRounds(queryGen).put(round, value);
		
	}

	private Double getRound(double queryGen, double round) {
		
		return getRounds(queryGen).get(round);
		
	}

	private Map<Double,Double> getRounds(double queryGen) {
		
		Map<Double,Double> ret = getQueriesTable().get(queryGen);
		
		if (ret == null){
			
			ret = new HashMap<Double, Double>();
			
			getQueriesTable().put(queryGen,ret);
			
		}
		
		return ret;
	}

	private Map<Double,Map<Double,Double>> getQueriesTable() {
		
		if (queriesTable == null){
			queriesTable = new HashMap<Double, Map<Double,Double>>();
		}
		return queriesTable;
	}
	
	@Override
	public String getTitle() {
		return "Round By Sample Size";
	}

	@Override
	public String getAxisXTitle() {
		return "Sample Size";
	}

	@Override
	public String getAxisYTitle() {
		return "Round";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}

}
