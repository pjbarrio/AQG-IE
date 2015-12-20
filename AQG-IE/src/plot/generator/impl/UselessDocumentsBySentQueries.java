package plot.generator.impl;

import java.util.Hashtable;
import java.util.List;

import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;

public class UselessDocumentsBySentQueries extends SampleGenerationSeriesGenerator {

	@Override
	protected String getNormalizedYAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getNormalizedXAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> generateAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> generateOrderBy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAxisXTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAxisYTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}

}
