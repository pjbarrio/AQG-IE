package execution.trunk.chart.classification.filter;

import java.util.Set;

import exploration.model.enumerations.ClassificationClusterEnum;

public abstract class Filter {

	private ClassificationClusterEnum classification;

	public Filter(ClassificationClusterEnum classification) {
		this.classification = classification;
	}

	public ClassificationClusterEnum getClassification() {
		return classification;
	}

	public abstract boolean accepts(Set<Double> values);

	public abstract boolean accepts(double value);
	
}
