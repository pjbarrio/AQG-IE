package plot.selector;

import java.util.List;

public abstract class SampleGenerationSelector implements Selector{

	private int ommitedValidValue;

	public SampleGenerationSelector(int ommitedValidValue) {
		this.ommitedValidValue = ommitedValidValue;
	}

	public abstract String getName();

	protected abstract List<Integer> getSampleGeneration(int valid);

	@Override
	public List<Integer> getSelected() {
		return getSampleGeneration(ommitedValidValue);
	}

}
