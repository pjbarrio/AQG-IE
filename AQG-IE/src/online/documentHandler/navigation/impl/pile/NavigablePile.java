package online.documentHandler.navigation.impl.pile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import online.navigation.utils.NavigationUtils;

import org.htmlparser.nodes.TagNode;

import searcher.interaction.formHandler.TextQuery;

public abstract class NavigablePile {

	protected List<TagNode> visited;
	private TextQuery query;
	private Hashtable<TagNode,Integer> page;
	
	public NavigablePile(TextQuery tq) {
		
		query = tq;
		
		visited = new ArrayList<TagNode>();
		
		page = new Hashtable<TagNode, Integer>();
		
	}
	
	public void addNumber(Integer key, TagNode value, TextQuery textQuery, int resultPage){
		
		if (!contains(visited,value,textQuery,resultPage))
			if(addNumberNode(key, value, textQuery, resultPage)){
				page.put(value,resultPage);
			}
		
		
		
	}

	protected abstract boolean addNumberNode(Integer key, TagNode value,
			TextQuery textQuery, int resultPage);

	public void pile(TagNode tagNode, TextQuery textQuery, int resultPage){
		
		if (!contains(visited,tagNode,textQuery,resultPage))
			if (pileNode(tagNode, textQuery, resultPage)){
				page.put(tagNode,resultPage);
			}

	}

	protected abstract boolean pileNode(TagNode tagNode, TextQuery textQuery,
			int resultPage);

	public abstract boolean hasNext();

	public TagNode getNext() {
		
		TagNode toreturn = generateNext();
		
		updateVisited(toreturn);
		
		return toreturn;		
		
	}

	protected boolean contains(Collection<TagNode> stored, TagNode tagNode, TextQuery textQuery, int resultPage) {
		
		for (TagNode st : stored) {
			if (NavigationUtils.referenceSamePage(st,page.get(st), tagNode, resultPage))
				return true;
		}
		
		return false;
	}
	
	private void updateVisited(TagNode toreturn) {
		
		visited.add(toreturn);
		
	}

	protected abstract TagNode generateNext();

	public static NavigablePile createInstance(TextQuery query,
			boolean allNumbers) {
		if (allNumbers)
			return new NumberNavigablePile(query);
		return new NextNavigablePile(query);
		
	}

	public void delete() {
		visited.clear();
		page.clear();
		
	}

}
