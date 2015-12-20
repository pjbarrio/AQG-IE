package online.sample.wordsDistribution;

import java.util.Comparator;
import java.util.Map;

public class HashBasedComparator<T> implements Comparator<T> {

	private Map<T, Double> map;

	public HashBasedComparator(Map<T, Double> map) {
		this.map = map;
	}

	@Override
	public int compare(T word1, T word2) {
		return Double.compare(map.get(word2),map.get(word1));
	}

}
