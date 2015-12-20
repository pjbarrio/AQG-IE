package plot.generator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Series;
import plot.selector.SampleGenerationSelector;
import utils.persistence.persistentWriter;

public abstract class SampleGenerationSeriesGenerator extends SeriesGenerator<SampleGenerationSelector> {
	
	@Override
	protected final double getTotal(persistentWriter pW, String normalizedXAttribute,
			Integer sel) {
		return pW.getSampleGenerationTotal(normalizedXAttribute, sel);
	}

	@Override
	protected final List<Hashtable<String, Double>> getAnalyzableData(
			persistentWriter pW, Integer sel, List<String> attributes,
			List<String> orderBy) {
		return pW.getAnalyzableSampleGenerationData(sel, getAttributes(), getOrderBy(), getWhere(), getGroupBy());
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

}
