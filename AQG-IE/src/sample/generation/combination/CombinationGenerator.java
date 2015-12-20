package sample.generation.combination;

import java.util.List;

import exploration.model.Database;
import exploration.model.enumerations.ClusterFunctionEnum;
import utils.persistence.databaseWriter;

public abstract class CombinationGenerator {

	protected databaseWriter pW;

	public CombinationGenerator(databaseWriter pW) {
		this.pW = pW;
	}

	public abstract List<Database> getCombination(int i);

	public abstract int numberOfCombinations();

	public abstract String getDatabaseName(int i);

	public abstract String getSampleType(int i);

	public abstract int isGlobal();

	public abstract int isCluster();

	public abstract ClusterFunctionEnum getClusteredFunction();

}
