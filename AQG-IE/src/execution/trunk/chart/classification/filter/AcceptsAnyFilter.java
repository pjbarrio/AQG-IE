package execution.trunk.chart.classification.filter;

import java.util.Set;

import exploration.model.enumerations.ClassificationClusterEnum;

public abstract class AcceptsAnyFilter extends Filter {

	public AcceptsAnyFilter(ClassificationClusterEnum classification) {
		super(classification);
	}

	@Override
	public boolean accepts(Set<Double> values) {
		
		for (Double value : values) {
			if (accepts(value))
				return true;
		}
		
		return false;
	}

	public abstract boolean accepts(double value);

}
