package plot.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;
import plot.selector.SampleGenerationSelector;

public class PrecisionsAtK extends SampleGenerationSeriesGenerator {

	private List<Integer> kforPrecision;
	private double K;

	public PrecisionsAtK(List<Integer> kforPrecision) {
		this.kforPrecision = kforPrecision;
	}

	private PrecisionsAtK(int K) {
		
		kforPrecision = null;
		this.K = K;
		
	}

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
		
		List<String> ret = new ArrayList<String>();
		
		ret.add(QUERY_POSITION);
		
		ret.add(QUERY_MAX);
		
		return ret;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>();
		
		ret.add(QUERY_POSITION);
		
		return ret;
		
	}

	@Override
	public List<String> generateGroupBy(){
		
		List<String> ret = new ArrayList<String>();
		
		ret.add(QUERY_POSITION);
		
		return ret;
		
	}

	@Override
	protected String getName(SampleGenerationSelector selector) {
		return super.getName(selector) + "_" + K;
	}
	
	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		Series ret = new Series(execution.toString());
		
		if (list.isEmpty())
			return ret;
		
		double lastQ = list.get(list.size()-1).get(QUERY_POSITION);
		
		int currentIndex = 0;
		
		boolean[] array = new boolean[(int)lastQ];
		
		for (double i = 1.0; i <= lastQ; i++) {
			
			if (i == list.get(currentIndex).get(QUERY_POSITION)){
				
				int max = (int)Double.valueOf(list.get(currentIndex).get(QUERY_MAX)).doubleValue();
				
				if (max > 0){
					
					array[(int)i-1]=true;
					
				}
				
				currentIndex++;
				
			}
			
		}
		
		for (double i = NUMBER_OF_ISSUED_QUERIES; i <= array.length; i+=NUMBER_OF_ISSUED_QUERIES) {
			
			int start = (int)i - 1;
			
			if (start + K >= array.length)
				break;
			
			double cant = 0;
			
			for (int j = start; j < start + K; j++) {
				
				if (array[j])
					cant++;
				
			}
			
			ret.addPair(Pair.getPair(i, cant));
			
		}
		
				
		return ret;
		
		
	}

	@Override
	public String getTitle() {
		return "Precision At K";
	}

	@Override
	public String getAxisXTitle() {
		return "Query Position";
	}

	@Override
	public String getAxisYTitle() {
		return "Number of Samples";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}

	@Override
	public List<SeriesGenerator<SampleGenerationSelector>> generateByParameterSeries() {
		
		List<SeriesGenerator<SampleGenerationSelector>> ret = new ArrayList<SeriesGenerator<SampleGenerationSelector>>();
		
		for (int i = 0; i < kforPrecision.size(); i++) {
			
			ret.add(new PrecisionsAtK(kforPrecision.get(i)));
			
		}
		
		return ret;
	}

}
