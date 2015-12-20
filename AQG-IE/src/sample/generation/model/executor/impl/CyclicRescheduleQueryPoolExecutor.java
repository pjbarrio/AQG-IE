package sample.generation.model.executor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sample.generation.model.performanceChecker.QueryPerformanceChecker;
import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;
import sample.generation.model.queryPool.QueryPool;

public class CyclicRescheduleQueryPoolExecutor<T> extends
		ReScheduleQueryPoolExecutor<T> {

	public CyclicRescheduleQueryPoolExecutor(QueryPool<T> queryPool,
			QueryPoolPerformanceChecker queryPoolPerformanceChecker,
			QueryPerformanceChecker queryPerformanceChecker, int memory) {
		super(queryPool, queryPoolPerformanceChecker, queryPerformanceChecker, memory);
	}


	@Override
	protected List<T> regenerateQueryPool(List<Integer> queriesTokeep,
			Map<Integer, T> queries, Map<Integer, List<Boolean>> documents) {
		
		List<T> ret = new ArrayList<T>(queriesTokeep.size());
		
		for (int i = 0; i < queriesTokeep.size(); i++) {
			
			ret.add(queries.get(queriesTokeep.get(i)));
			
		}
		
		return ret;
	
	}

}
