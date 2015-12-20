package plot.selector.impl;

import java.util.ArrayList;
import java.util.List;

import plot.selector.SampleGenerationSelector;
import utils.persistence.persistentWriter;

public class FixedSampleGenerationSelector extends SampleGenerationSelector {

	private List<Integer> configuration;
	private String name;
	private persistentWriter pW;
	private List<Integer> databases;

	public FixedSampleGenerationSelector(List<Integer> configuration, String name, persistentWriter pW,int ommitedValidValue) {
		this(configuration,name,pW,ommitedValidValue,new ArrayList<Integer>(0));
	}
	
	public FixedSampleGenerationSelector(List<Integer> configuration, String name, persistentWriter pW,int ommitedValidValue, List<Integer> databases) {
		super(ommitedValidValue);
		this.configuration = configuration;
		this.name = name;
		this.pW = pW;
		this.databases = databases;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected List<Integer> getSampleGeneration(int ommitedValidValue) {
		if (databases.isEmpty())
			return pW.getIdSamplesForConfiguration(configuration, ommitedValidValue);
		else
			return pW.getIdSamplesForConfigurationOnDatabases(configuration,ommitedValidValue,databases);
	}

}
