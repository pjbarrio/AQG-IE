package plot.selector.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import plot.selector.ExecutionSelector;
import plot.selector.Selector;

public class IntersectionSelector implements Selector {

	private Selector ex1;
	private Selector ex2;
	private String name;

	public IntersectionSelector(String name, Selector ex1, Selector ex2){
		this.name = name;
		this.ex1 = ex1;
		this.ex2 = ex2;
	}
	
	@Override
	public List<Integer> getSelected() {
		
		List<Integer> ex1List = ex1.getSelected();
		List<Integer> ex2List = ex2.getSelected();
		
		Set<Integer> s = new TreeSet<Integer>(ex1List);
		
		s.retainAll(ex2List);
		
		ArrayList<Integer> ret = new ArrayList<Integer>(s);
		
		return ret;
	}

	@Override
	public String getName() {
		return name;
	}

}
