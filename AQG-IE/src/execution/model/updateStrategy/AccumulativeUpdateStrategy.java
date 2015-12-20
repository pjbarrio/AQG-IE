package execution.model.updateStrategy;

import execution.model.documentCollector.DocumentCollector;

public class AccumulativeUpdateStrategy implements UpdateStrategy {

	@Override
	public void update(DocumentCollector documentCollector) {
		
		documentCollector.saveIteration();
		
	}
	
	
}
