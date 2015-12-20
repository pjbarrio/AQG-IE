package utils.algorithms;

import java.util.Comparator;
import java.util.List;

public class ListBasedPositionComparator<T> implements Comparator<T> {

	private List<T> positions;

	public ListBasedPositionComparator(List<T> positions) {
		this.positions = positions;
	}

	@Override
	public int compare(T val0, T val1) {
		
		return Double.compare(positions.indexOf(val0), positions.indexOf(val1));
		
	}

}
