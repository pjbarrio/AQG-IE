package utils.algorithms;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BooleanCountBasedComparator implements Comparator<Integer> {

	private Map<Integer, List<Boolean>> documents;

	public BooleanCountBasedComparator(Map<Integer, List<Boolean>> documents) {
		this.documents = documents;
	}

	@Override
	public int compare(Integer val0, Integer val1) {
		
		return Double.compare(count(documents.get(val0)), count(documents.get(val1)));
		
	}

	private double count(List<Boolean> list) {
		
		double count = 0.0;
		
		for (int i = 0; i < list.size(); i++) {
			
			if (list.get(i))
				count++;
			
		}
		
		return count;
	}

}
