package utils.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntropyCalculator {

	public static <T> double calculateShannonEntropy(List<T> values) {
		Map<T, Integer> map = new HashMap<T, Integer>();
		// count the occurrences of each value
		for (T sequence : values) {
			if (!map.containsKey(sequence)) {
				map.put(sequence, 0);
			}
			map.put(sequence, map.get(sequence) + 1);
		}
		// calculate the entropy
		double result = 0.0;
		double size = values.size();
		double log = Math.log(2);
		for (T sequence : map.keySet()) {
			Double frequency = (double) map.get(sequence) / size;
			result -= frequency * (Math.log(frequency) / log);
		}
		return result;
	}

}
