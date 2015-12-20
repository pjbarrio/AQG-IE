package sample.generation.model.performanceChecker.impl;

import java.util.List;
import java.util.Map;

import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;

public class UnderPrecisionInKQueries implements QueryPoolPerformanceChecker {

	private int K;
	private double precision;

	public UnderPrecisionInKQueries(int K, double precision) {
		this.K = K;
		this.precision = precision;
	}

	@Override
	public boolean isStillProcessable(List<Integer> orderedList,
			Map<Integer, List<Boolean>> documents) {
		
		if (orderedList.size() < K){
			
			return true;
			
		}else{
			
			double count = 0.0;
			
			for (int i = orderedList.size()-K; i < orderedList.size(); i++) {
				
				if (hasUseful(documents.get(orderedList.get(i)))){
					
					count++;
					
				}
				
			}
			
			return (count/K)>=precision;
			
		}
		
	}

	private boolean hasUseful(List<Boolean> list) {
		
		for (int i = 0; i < list.size(); i++) {
			
			if (list.get(i))
				return true;
			
		}
		
		return false;
		
	}
	
}
