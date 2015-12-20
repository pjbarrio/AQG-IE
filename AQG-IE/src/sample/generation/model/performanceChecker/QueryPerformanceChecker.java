package sample.generation.model.performanceChecker;

import java.util.List;

public interface QueryPerformanceChecker {

	boolean isStillProcessable(List<Boolean> documents);

}
