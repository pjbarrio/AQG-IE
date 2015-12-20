package execution.model.updateStrategy;

import execution.model.documentCollector.DocumentCollector;

public class RegenerativeUpdateStrategy implements UpdateStrategy {

	@Override
	public void update(DocumentCollector documentCollector) {
		documentCollector.clear();
	}


}
