package utils.execution;

import java.util.Comparator;
import java.util.Map;

import org.htmlparser.nodes.TagNode;

public class MapBasedComparator<T,D extends Comparable<D>> implements Comparator<T> {

	private Map<T, D> map;

	public MapBasedComparator(Map<T, D> map) {
		this.map = map;
	}

	@Override
	public int compare(T o1, T o2) {
		return map.get(o2).compareTo(map.get(o1));
//		return Double.compare(map.get(o2), map.get(o1));
	}

}
