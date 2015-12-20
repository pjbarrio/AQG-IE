package plot.generator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import execution.trunk.chart.ChartGeneration;

import plot.data.Series;
import plot.selector.ExecutionSelector;
import plot.selector.Selector;
import plot.selector.SampleGenerationSelector;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;


public abstract class SeriesGenerator<T extends Selector> {
	
	protected static final String USEFUL_TUPLES = "usefulTuples";
	protected static final String CURRENT_TIME = "currentTime";
	protected static final String PROCESSED_DOCUMENT_POSITION = "processed_document_position";
	protected static final String EXECUTED_QUERY_POSITION = "executed_query_position";
	protected static final String DATABASE_LIMIT = "E.`limit`";
	protected static final String USEFUL_TOTAL_TUPLES = "W.usefulTuples";
	protected static final String USEFUL_TOTAL_DOCUMENTS = "W.usefulDocuments";
	public static final String TOTAL_DOCUMENTS = "D.size";
	public static final String NO_NORMALIZATION = "W.noNormalization";
	public static final String QUERY_POSITION = "S.query_submitted_position";
	public static final String QUERY_ROUND = "S.query_round";
	public static final String QUERY_COUNT = "count(S.*) as NUM";
	public static final String MIN_SAM = "MIN_SAM";
	public static final String MAX_SAM = "MAX_SAM";
		
	public static final String DOCUMENT_POSITION_IN_SAMPLE = "S.document_position_in_sample";
	public static final String DOCUMENT_POSITION = "S.document_position";
	public static final String DOCUMENT_IN_QUERY_POSITION = "S.doc_in_query_position";
	public static final String QUERY_GENERATED = "S.query_generated_position";
	
	public static final String QUERY_MIN = "min("+DOCUMENT_POSITION_IN_SAMPLE+") as " + MIN_SAM;
	public static final String QUERY_MAX = "max("+DOCUMENT_POSITION_IN_SAMPLE+") as " + MAX_SAM;
	
	private static final int INTERVAL_NUMBERS = 10;
	public static final double NUMBER_OF_PROCESSED_DOCUMENTS_NORMALIZED = 0.001;
	public static final double NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER = 5;
	protected static final double NUMBER_OF_ADDED_DOCUMENTS = 5;
	protected static final double NUMBER_OF_PROCESSED_DOCUMENTS_BY_QUERY = 0.05;
	protected static final double NUMBER_OF_ISSUED_QUERIES = 2;
	protected static final double NUMBER_OF_ROUNDS = 2;
	protected static final double NUMBER_OF_SAMPLED_DOCUMENTS = 25;
	protected static final double NUMBER_OF_SAMPLED_DOCUMENTS_UT = 5;
	protected static final double NUMBER_OF_SECONDS_INTERVAL = 100;
	protected static final double K_PARAMETER = 5;

	

	private List<String> attributes;
	private List<String> orderBy;
	private List<String> groupBy;
	private String where;

	public Series generateSeries(T selector){
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Series ret = new Series(getName(selector));
		
		List<Integer> selected = selector.getSelected();
		
		for (Integer sel : selected) {
			
			List<Hashtable<String, Double>> list = getAnalyzableData(pW,sel, getAttributes(), getOrderBy());
			
			double xNorm = 1.0;
			
			if (!getNormalizedXAttribute().equals(NO_NORMALIZATION)){
				xNorm = getTotal(pW,getNormalizedXAttribute(), sel);
			}
			
			double yNorm = 1.0;
			
			if (!getNormalizedYAttribute().equals(NO_NORMALIZATION)){
				yNorm = getTotal(pW,getNormalizedYAttribute(), sel);
			}
			
			ret.addSeries(generateSeries(list,sel,xNorm,yNorm));
			
		}
		
		return invokeAverage(ret);

	}
	
	
	protected String getName(T selector) {
		return selector.getName();
	}


	protected abstract Series invokeAverage(Series ret);


	protected abstract double getTotal(persistentWriter pW, String normalizedXAttribute,
			Integer sel);


	protected abstract List<Hashtable<String, Double>> getAnalyzableData(
			persistentWriter pW, Integer sel, List<String> attributes,
			List<String> orderBy);

	protected abstract String getNormalizedYAttribute();
	
	protected abstract String getNormalizedXAttribute();

	protected List<String> getAttributes(){
		if (attributes == null){
			attributes = generateAttributes();
		}
		return attributes;
	}

	public abstract List<String> generateAttributes();

	public List<String> getOrderBy(){
		if (orderBy == null){
			orderBy = generateOrderBy();
		}
		return orderBy;
	}

	public abstract List<String> generateOrderBy();

	public List<String> getGroupBy(){
		if (groupBy == null){
			groupBy = generateGroupBy();
		}
		return groupBy;
	}
	
	protected abstract List<String> generateGroupBy();
	
	public String getWhere(){
		if (where == null){
			where = generateWhere();
		}
		return where;
	}
	
	protected abstract String generateWhere();
	
	protected abstract Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue);

	public abstract String getTitle();

	public abstract String getAxisXTitle();

	public abstract String getAxisYTitle();

	public String getExtendedTitle(String extension) {
		return getTitle() + " [" + extension + "]";
	}

	public boolean getPercentX() {
		return false;
	}

	public boolean getPercentY() {
		return false;
	}


	public List<SeriesGenerator<T>> generateByParameterSeries(){
		
		List<SeriesGenerator<T>> ret = new ArrayList<SeriesGenerator<T>>(1);
		
		ret.add(this);
		
		return ret;
		
	}
		
}
