package sample.generation.model.cardinality;

import java.util.List;

import sample.generation.model.SampleConfiguration;
import sample.generation.model.SampleGenerator;

import execution.model.parameters.Parametrizable;
import exploration.model.Database;

public interface CardinalityFunction {

	public void calculateRequests(List<Database> databases,
			SampleConfiguration sampleConfiguration);

	public int getUsefulDocuments(Database database);

	public int getUselessDocuments(Database database);

}
