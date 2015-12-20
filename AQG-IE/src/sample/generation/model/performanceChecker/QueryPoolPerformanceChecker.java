package sample.generation.model.performanceChecker;

import java.util.List;
import java.util.Map;

public interface QueryPoolPerformanceChecker {

	boolean isStillProcessable(List<Integer> orderedList, Map<Integer, List<Boolean>> documents);

}
