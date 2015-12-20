package plot.generator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Series;
import plot.selector.ExecutionSelector;
import utils.persistence.persistentWriter;

public abstract class ExecutionSeriesGenerator extends SeriesGenerator<ExecutionSelector>{

	protected static final String USEFUL_TUPLES = "usefulTuples";
	protected static final String CURRENT_TIME = "currentTime";
	protected static final String PROCESSED_DOCUMENT_POSITION = "processed_document_position";
	protected static final String EXECUTED_QUERY_POSITION = "executed_query_position";
	protected static final String DATABASE_LIMIT = "E.`limit`";
	protected static final String USEFUL_TOTAL_TUPLES = "W.usefulTuples";
	protected static final String USEFUL_TOTAL_DOCUMENTS = "W.usefulDocuments";
	protected static final String TOTAL_DOCUMENTS = "D.size";
	protected static final String NO_NORMALIZATION = "W.noNormalization";
	
	@Override
	protected final double getTotal(persistentWriter pW, String normalizedXAttribute,
			Integer sel) {
		return pW.getTotal(getNormalizedXAttribute(), sel);
	}

	@Override
	protected final List<Hashtable<String, Double>> getAnalyzableData(
			persistentWriter pW, Integer sel, List<String> attributes,
			List<String> orderBy) {
		return pW.getAnalyzableData(sel, getAttributes(), getOrderBy());	
	}

	@Override
	protected abstract String getNormalizedYAttribute();

	@Override
	protected abstract String getNormalizedXAttribute();

	@Override
	public abstract List<String> generateAttributes();

	@Override
	public abstract List<String> generateOrderBy();

	@Override
	public List<String> generateGroupBy(){
		return new ArrayList<String>();
	}
	
	@Override
	public String generateWhere(){
		return "";
	}
	
	@Override
	protected abstract Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue);

	@Override
	public abstract String getTitle();

	@Override
	public abstract String getAxisXTitle();

	@Override
	public abstract String getAxisYTitle();

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}
	
}
