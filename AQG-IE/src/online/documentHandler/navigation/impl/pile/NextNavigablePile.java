package online.documentHandler.navigation.impl.pile;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import online.navigation.utils.NavigationUtils;

import org.htmlparser.nodes.TagNode;

import searcher.interaction.formHandler.TextQuery;

public class NextNavigablePile extends NavigablePile {

	private List<TagNode> pile;
	
	

	public NextNavigablePile(TextQuery query) {
		super(query);
		pile = new ArrayList<TagNode>();
				
	}

	@Override
	protected boolean addNumberNode(Integer key, TagNode value, TextQuery textQuery, int resultPage) {
		return false;		
	}

	@Override
	protected boolean pileNode(TagNode tagNode, TextQuery textQuery, int resultPage) {
		
		if (!contains(pile,tagNode,textQuery,resultPage)){
			pile.add(tagNode);
			return true;
		}
		
		return false;
	}

	@Override
	public boolean hasNext() {
		return !pile.isEmpty();
	}

	@Override
	protected TagNode generateNext() {
		return pile.remove(0);	
	}
}
