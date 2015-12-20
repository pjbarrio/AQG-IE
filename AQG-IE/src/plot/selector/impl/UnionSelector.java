package plot.selector.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import plot.selector.Selector;

public class UnionSelector implements Selector {

	private String name;
	private Selector[] es;

	public UnionSelector (String name, Selector...executionSelectors){
		this.es = executionSelectors;
		this.name = name;
	}
	
	@Override
	public List<Integer> getSelected() {
		
		Set<Integer> s = new TreeSet<Integer>();
		
		for (int i = 0; i < es.length; i++) {
			
			s.addAll(es[i].getSelected());
			
		}
		
		ArrayList<Integer> ret = new ArrayList<Integer>(s);
		
		return ret;
		
	}

	@Override
	public String getName() {
		return name;
	}

}
