package sample.generation.model.performanceChecker.impl;

import java.util.List;

import sample.generation.model.performanceChecker.QueryPerformanceChecker;

public class PrecisionPerformanceChecker implements QueryPerformanceChecker {

	private double minPrecision;

	public PrecisionPerformanceChecker(double minPrecision) {
		this.minPrecision = minPrecision;
	}
	
	@Override
	public boolean isStillProcessable(List<Boolean> documents) {
		
		double useful = 0;
		
		for (int i = 0; i < documents.size(); i++) {
			
			if (documents.get(i))
				useful++;
			
		}
		
		return (useful/(documents.size()) >= minPrecision);
					
	}

}
