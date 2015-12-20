package online.cluster.features.selection.impl;

import online.cluster.features.selection.ForClusterFeatureSelector;

public class FrequencyBasedFeatureSelector extends ForClusterFeatureSelector {

	private double low;
	private double high;

	public FrequencyBasedFeatureSelector(double low, double high) {
		
		this.low = low;
		this.high= high;
		
	}

}
