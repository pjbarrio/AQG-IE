package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;
import plot.selector.SampleGenerationSelector;
import utils.persistence.persistentWriter;

public class PrecisionAtKinDifferentStartingPositions extends
		SampleGenerationSeriesGenerator {

	private List<Integer> startingPositions;
	private int startingPoint;

	public PrecisionAtKinDifferentStartingPositions(
			List<Integer> startingPositions) {
		this.startingPositions = startingPositions;
	}

	private PrecisionAtKinDifferentStartingPositions(int startingPoint) {
		this.startingPoint = startingPoint;
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
		return super.getName(selector) + "_" + startingPoint;
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
		
		double processing = 0;
		
		double lastProcessed = K_PARAMETER;
		
		double count = 0;
		
		boolean added = false;
		
		for (int i = startingPoint-1; i < array.length; i++) {
			
			added = true;
			
			processing++;
			
			if (array[i])
				count++;
			
			if (processing == lastProcessed){
				ret.addPair(Pair.getPair(lastProcessed,count/lastProcessed));
				lastProcessed+=K_PARAMETER;
				added = false;
			}
				
		}		
		
		if (added)
			ret.addPair(Pair.getPair(lastProcessed, count/lastProcessed));
		
		return ret;
		
	}

	@Override
	public String getTitle() {
		return "Precision at Different Starting Points";
	}

	@Override
	public String getAxisXTitle() {
		return "K -Queries processed-";
	}

	@Override
	public String getAxisYTitle() {
		return "Precision at K";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.independentAverage();
	}

	@Override
	public List<SeriesGenerator<SampleGenerationSelector>> generateByParameterSeries() {
		
		List<SeriesGenerator<SampleGenerationSelector>> ret = new ArrayList<SeriesGenerator<SampleGenerationSelector>>();
		
		for (int i = 0; i < startingPositions.size(); i++) {
			
			ret.add(new PrecisionAtKinDifferentStartingPositions(startingPositions.get(i)));
			
		}
		
		return ret;
	}
	
}
