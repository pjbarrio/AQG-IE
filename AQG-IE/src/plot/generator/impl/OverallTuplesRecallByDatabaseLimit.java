package plot.generator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import plot.data.Series;
import plot.generator.OverallSeriesGenerator;
import plot.generator.SeriesGenerator;

public class OverallTuplesRecallByDatabaseLimit extends OverallSeriesGenerator {

	public OverallTuplesRecallByDatabaseLimit(List<Double> limits) {
		
		super(limits);
		
	}

	@Override
	public List<String> generateAttributes() {
		
		List<String> r = new ArrayList<String>();
		
		r.add("SUM(" + USEFUL_TUPLES + ") as S");
		
		return r;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> r = new ArrayList<String>();
		
//		r.add("S");
		
		return r;

	}

	@Override
	public String getTitle() {
		return "Overall Recall by Maximum Retrieved Documents By Query";
	}

	@Override
	public String getAxisXTitle() {
		return "Retrieved Documents By Query";
	}

	@Override
	public String getAxisYTitle() {
		return "Recall";
	}

	@Override
	protected Double getOverallValue(List<Hashtable<String, Double>> list,
			Integer execution, double total) {
		return list.get(0).get("SUM(" + USEFUL_TUPLES + ") as S") / total;
	}

	@Override
	protected String getNormalizedYAttribute() {
		return USEFUL_TOTAL_TUPLES;

	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}

}
