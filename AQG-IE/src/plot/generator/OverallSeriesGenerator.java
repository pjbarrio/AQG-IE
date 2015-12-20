package plot.generator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.selector.ExecutionSelector;
import plot.selector.Selector;
import plot.selector.impl.OverallByParameterSelector;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public abstract class OverallSeriesGenerator extends SeriesGenerator<OverallByParameterSelector> {

	private List<Double> parameters;

	protected OverallSeriesGenerator(List<Double> parameters){
		
		this.parameters = parameters;
	
	}
	
	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		return null;
	}

	@Override
	public Series generateSeries(OverallByParameterSelector selector){

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Series ret = new Series(selector.getName());

		for (double parameter : parameters) {

			ArrayList<Double> aux = new ArrayList<Double>();
			
			List<Integer> executions = selector.getExecutionSelector(parameter).getSelected();
			
			for (Integer execution : executions) {
				
				List<Hashtable<String, Double>> list = pW.getAnalyzableData(execution, getAttributes(), getOrderBy());
				
				aux.add(getOverallValue(list,execution,pW.getTotal(getNormalizedYAttribute(),execution)));
				
			}
			
			ret.addPair(Pair.getPair(parameter,calculateAverage(aux)));
			
		}		
		
		return ret;
	}

	protected abstract Double getOverallValue(List<Hashtable<String, Double>> list,
			Integer execution, double total);

	private double calculateAverage(ArrayList<Double> values) {
		
		double sum = 0;
		
		for (Double value : values) {
			sum += value;
		}
		
		return sum / values.size();
	}

	@Override
	protected final double getTotal(persistentWriter pW, String normalizedXAttribute,
			Integer sel) {
		// XXX Might need to re-design
		return 0;
	}

	@Override
	protected final List<Hashtable<String, Double>> getAnalyzableData(
			persistentWriter pW, Integer sel, List<String> attributes,
			List<String> orderBy) {
		// XXX might need to re-design
		return null;
	}
	
	@Override
	protected Series invokeAverage(Series ret) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> generateGroupBy(){
		return new ArrayList<String>();
	}
	
	@Override
	public String generateWhere(){
		return "";
	}
}
