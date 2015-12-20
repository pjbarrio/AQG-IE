package sample.generation.model.executor.impl;

import java.util.List;
import java.util.Map;

import sample.generation.model.performanceChecker.QueryPerformanceChecker;
import sample.generation.model.performanceChecker.QueryPoolPerformanceChecker;
import sample.generation.model.queryPool.QueryPool;

public class StratifiedQueryPoolExecutor<T> extends ReScheduleQueryPoolExecutor<T> {

	public StratifiedQueryPoolExecutor(QueryPool<T> queryPool,
			QueryPoolPerformanceChecker queryPoolPerformanceChecker,
			QueryPerformanceChecker queryPerformanceChecker, int memory) {
		super(queryPool, queryPoolPerformanceChecker, queryPerformanceChecker, memory);
	}

	@Override
	protected List<T> regenerateQueryPool(List<Integer> queriesTokeep,
			Map<Integer, T> queries, Map<Integer, List<Boolean>> documents){
		
		System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		return null;
	}


}
