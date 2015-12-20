package utils.dispatcher;

import java.util.Comparator;
import java.util.Hashtable;

public class MyComparator<T,E extends Comparable<E>> implements Comparator<T> {

	private Hashtable<T, E> table;

	public MyComparator(Hashtable<T, E> times){
		this.table = times;
	}
	
	@Override
	public int compare(T o1, T o2) {
		
		return table.get(o1).compareTo(table.get(o2));
		
	}

}
