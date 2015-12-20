package sample.generation.model.performanceChecker.impl;

import java.util.List;
import java.util.Map;

import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;

public class NoUsefulDocumentsInKQueries implements
		QueryPoolPerformanceChecker {

	private int numberOfQueries;

	public NoUsefulDocumentsInKQueries(int numberOfQueries){
		this.numberOfQueries = numberOfQueries;
	}
	
	@Override
	public boolean isStillProcessable(List<Integer> orderedList,
			Map<Integer, List<Boolean>> documents) {
		
		if (orderedList.size() < numberOfQueries){
			
			return true;
			
		}else{
			
			for (int i = orderedList.size()-numberOfQueries; i < orderedList.size(); i++) {
				
				if (hasUseful(documents.get(orderedList.get(i)))){
					
					return true;
					
				}
				
			}
			
			return false;
			
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
