package execution.trunk.chart.classification.filter;

import java.util.Set;

import exploration.model.enumerations.ClassificationClusterEnum;
import exploration.model.enumerations.IntervalFilterEnum;

public class IntervalFilter extends AcceptsAnyFilter{

	private double min;
	private double max;
	private IntervalFilterEnum compMin;
	private IntervalFilterEnum compMax;

	public IntervalFilter(ClassificationClusterEnum classification, double min, double max,
			IntervalFilterEnum compMin, IntervalFilterEnum compMax) {
		super(classification);
		this.min = min;
		this.max = max;
		this.compMin = compMin;
		this.compMax = compMax;
		
	}

	@Override
	public boolean accepts(double value) {
		
		if (in(compMin,min, value) && in(compMax,max,value))
			return true;
		
		return false;
		
	}

	private boolean in(IntervalFilterEnum comp, double extreme, double value) {
		
		switch (comp) {
		case E:
			
			return extreme == value;

		case G:
			
			return value > extreme;
			
		case GE:
			
			return value >= extreme;
			
		case L:
		
			return value < extreme;
			
		case LE:
			
			return value <= extreme;
			
		default:
			
			return false;
		}
		
	}

	

}
