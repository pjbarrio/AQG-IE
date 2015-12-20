package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.ExecutionSeriesGenerator;
import plot.generator.SeriesGenerator;

public class QueryPrecisionByProcessedDocuments extends ExecutionSeriesGenerator {

	private Hashtable<Integer, Integer> processedTable;
	private Hashtable<Integer, Integer> usefulTable;
	private Hashtable<Integer, Series> querySeriesTable;
	
	@Override
	protected String getNormalizedYAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	public List<String> generateAttributes() {
		
		List<String> ret = new ArrayList<String>();
		ret.add(EXECUTED_QUERY_POSITION);
		ret.add(PROCESSED_DOCUMENT_POSITION);
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

		querySeriesTable = null;
		processedTable = null;
		usefulTable = null;
		
		Series ret = new Series(execution.toString());

		for (Hashtable<String, Double> hashtable : list) {
			
			double tuples = hashtable.get(USEFUL_TUPLES);
			double executed_query_position = hashtable.get(EXECUTED_QUERY_POSITION);
			
			addProcessed(executed_query_position);
			
			if (tuples>0)			
				addUseful(executed_query_position);
			
			Series byq = getQuerySeries(executed_query_position,ret);

			double useful = 0;
			
			if (getUsefulTable().containsKey((int)executed_query_position)){
				useful = getUsefulTable().get((int)executed_query_position);
			}
			
			byq.addPair(Pair.getPair(getProcessedTable().get((int)executed_query_position),useful/(double)getProcessedTable().get((int)executed_query_position)));
			
		}
		
		ret = ret.averageSeries();
		
		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_PROCESSED_DOCUMENTS_BY_QUERY);

	}

	private Series getQuerySeries(double executed_query_position, Series ret2) {
		
		Series ret = getQuerySeriesTable().get((int)executed_query_position);
		
		if (ret == null){
			
			ret = new Series(Double.toString((int)executed_query_position));
			
			ret2.addSeries(ret);
			
			getQuerySeriesTable().put((int)executed_query_position, ret);
		}
		
		return ret;
	}

	private Hashtable<Integer, Series> getQuerySeriesTable() {
		
		if (querySeriesTable == null){
			querySeriesTable = new Hashtable<Integer,Series>();
		}
		return querySeriesTable;
	}
	

	private void addUseful(double executed_query_position) {
		
		Integer useful = getUsefulTable().get((int)executed_query_position);
		
		if (useful == null){
			useful = 0;
		}
		
		getUsefulTable().put((int)executed_query_position,useful+1);
		
	}

	private Hashtable<Integer, Integer> getUsefulTable() {
		if (usefulTable == null){
			usefulTable = new Hashtable<Integer, Integer>();
		}
		return usefulTable;
	}

	private void addProcessed(double executed_query_position) {
		
		Integer processed = getProcessedTable().get((int)executed_query_position);
		
		if (processed == null){
			processed = 0;
		}
		
		getProcessedTable().put((int)executed_query_position,processed+1);
	
	}

	private Hashtable<Integer,Integer> getProcessedTable() {
		
		if (processedTable == null){
			processedTable = new Hashtable<Integer, Integer>();
		}
		return processedTable;
	}

	@Override
	public String getTitle() {
		return "Query Precision By Processed Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Processed Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Query Precision";
	}

	@Override
	protected String getNormalizedXAttribute() {
		return DATABASE_LIMIT;
	}

	@Override
	public boolean getPercentX() {
		return true;
	}
	
}
