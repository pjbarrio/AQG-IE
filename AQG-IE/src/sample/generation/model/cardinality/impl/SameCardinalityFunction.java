package sample.generation.model.cardinality.impl;

import java.util.ArrayList;
import java.util.List;

import exploration.model.Database;
import exploration.model.enumerations.CardinalityFunctionLimitEnum;
import sample.generation.model.SampleConfiguration;
import sample.generation.model.cardinality.CardinalityFunction;

public class SameCardinalityFunction implements CardinalityFunction {

	private List<Integer> usefuls;
	private List<Integer> uselesses;
	private CardinalityFunctionLimitEnum limit;

	public SameCardinalityFunction(CardinalityFunctionLimitEnum limit) {
		this.limit = limit;
	}

	@Override
	public void calculateRequests(List<Database> databases,
			SampleConfiguration sampleConfiguration) {

		int size = -1;
		switch (limit) {
		case UNLIMITED:
			size = 1;
			break;

		case SHARP:
			size = databases.size();
			break;
		default:
			throw new UnsupportedOperationException("Forgot to put the cardinality function limit!");

		}

		usefuls = generateArray(sampleConfiguration.getUsefulNumber(), size, databases.size());

		uselesses = generateArray(sampleConfiguration.getUselessNumber(), size, databases.size());

	}

	private List<Integer> generateArray(int number, int size, int numberToStore) {

		int num = (int)Math.round((double)number / (double)size);

		int missing = (int)Math.round((double)number % (double)size);
		
		List<Integer> numbers = new ArrayList<Integer>(numberToStore);

		for (int i = 0; i < numberToStore-1; i++) {

			numbers.add(num);

		}

		numbers.add(num + missing);
		
		return numbers;
	}

	@Override
	public int getUsefulDocuments(Database database) {
		return usefuls.remove(0);
	}

	@Override
	public int getUselessDocuments(Database database) {
		return uselesses.remove(0);
	}

}
