package online.documentHandler.navigation.impl.pile;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeSet;

import online.navigation.utils.NavigationUtils;

import org.htmlparser.nodes.TagNode;

import searcher.interaction.formHandler.TextQuery;

public class NumberNavigablePile extends NavigablePile {

	private TreeSet<Integer> sorted;
	private Hashtable<Integer, List<TagNode>> numbers;
	
	public NumberNavigablePile(TextQuery query) {
		
		super(query);

		sorted = new TreeSet<Integer>();
		
		numbers = new Hashtable<Integer, List<TagNode>>();
	}

	@Override
	protected boolean addNumberNode(Integer key, TagNode value, TextQuery textQuery, int resultPage) {
		
		sorted.add(key);
		
		List<TagNode> list = getList(numbers,key);
		
		if (!contains(list, value, textQuery, resultPage)){
			list.add(value);
			return true;
		}
		
		return false;
		
	}

	private List<TagNode> getList(
			Hashtable<Integer, List<TagNode>> numbers, Integer key) {
		
		List<TagNode> ret = numbers.get(key);
		
		if (ret == null){
			ret = new ArrayList<TagNode>();
			numbers.put(key, ret);
		}
		
		return ret;
	}

	@Override
	protected boolean pileNode(TagNode tagNode, TextQuery textQuery, int resultPage) {
		return false;
	}

	@Override
	public boolean hasNext() {
		
		return !numbers.isEmpty();
		
	}

	@Override
	protected TagNode generateNext() {
		
		Integer r = sorted.first();
		
		TagNode ret = numbers.get(r).remove(0);
		
		if (numbers.get(r).isEmpty()){
			sorted.remove(r);
			numbers.remove(r);
		}
		
		return ret;
			
	}

}
