package sample.generation.model.executor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import sample.generation.model.performanceChecker.QueryPerformanceChecker;
import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;
import sample.generation.model.queryPool.QueryPool;
import utils.algorithms.BooleanCountBasedComparator;

public class OpportunityQueryPoolExecutor<T> extends
		ReScheduleQueryPoolExecutor<T> {

	public OpportunityQueryPoolExecutor(QueryPool<T> queryPool,
			QueryPoolPerformanceChecker queryPoolPerformanceChecker,
			QueryPerformanceChecker queryPerformanceChecker, int memory) {
		super(queryPool, queryPoolPerformanceChecker, queryPerformanceChecker, memory);
	}

	@Override
	protected List<T> regenerateQueryPool(List<Integer> queriesTokeep,
			Map<Integer, T> queries, Map<Integer, List<Boolean>> documents) {
		
		Collections.sort(queriesTokeep, Collections.reverseOrder(new BooleanCountBasedComparator(documents)));
		
		List<T> ret = new ArrayList<T>(queriesTokeep.size());
		
		for (int i = 0; i < queriesTokeep.size(); i++) {
			
			ret.add(queries.get(queriesTokeep.get(i)));
			
		}
		
		return ret;
		
	}

}
