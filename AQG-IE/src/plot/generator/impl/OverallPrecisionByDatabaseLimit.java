package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Series;
import plot.generator.OverallSeriesGenerator;
import plot.generator.SeriesGenerator;

public class OverallPrecisionByDatabaseLimit extends OverallSeriesGenerator {

	public OverallPrecisionByDatabaseLimit(List<Double> parameters) {
		super(parameters);
	}

	@Override
	public List<String> generateAttributes() {
		List<String> r = new ArrayList<String>();
		
		r.add(USEFUL_TUPLES);
		
		return r;	
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> r = new ArrayList<String>();
		
		r.add(USEFUL_TUPLES);
		
		return r;
	
	}

	@Override
	public String getTitle() {
		return "Overall Precision by Database limit";
	}

	@Override
	public String getAxisXTitle() {
		return "Database Limit";
	}

	@Override
	public String getAxisYTitle() {
		return "Precision";
	}

	@Override
	protected Double getOverallValue(List<Hashtable<String, Double>> list,
			Integer execution, double total) {
		double useful = 0;
		
		if (list.size() == 0){
			return 0.0;
		}
		
		for (Hashtable<String, Double> hashtable : list) {
			
			if (hashtable.get(USEFUL_TUPLES) > 0)
				useful++;
		
		}
				
		
		return useful/(double)list.size();
	}

	@Override
	protected String getNormalizedYAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}




}
