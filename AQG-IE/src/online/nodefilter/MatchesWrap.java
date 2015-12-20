package online.nodefilter;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

import online.navigation.utils.NavigationUtils;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;

public class MatchesWrap implements NodeFilter {

	private static final int LEVEL_THRESHOLD = 2;
	private static final int POSITION_THRESHOLD = 2;
	private Integer level;
	private String path;
	private TagNode node;
	private HashMap<String, String> attTable;

	public MatchesWrap(Integer level, String path, TagNode node) {
		this.level = level;
		this.path = path;
		this.node = node;
		initialize(node);
	}

	private void initialize(TagNode local) {
		
		attTable = new HashMap<String, String>();
		
		Vector atts = local.getAttributesEx();
		
		for (Object attribute : atts) {
			
			Attribute attr = (Attribute)attribute;
			
			if (attr.getName() == null || attr.getValue() == null)
				continue;
						
			attTable.put(attr.getName(), attr.getValue());
			
		}
	}

	@Override
	public boolean accept(Node node) {
		
//		System.out.println("NODE: " + node.toHtml());
		
		if (node instanceof TagNode){
			
			TagNode tt = (TagNode)node;
			
			if (tt.isEndTag())
				return false;
			
			if (match(tt,this.node)){
				
				//need to check which level it is on ...
				
				int level = getLevel(node);
				
				//need to check which children it is.
				
//				int position = getChildPosition(node);
				
				if (acceptsLevel(level)/* && acceptsPosition(position)*/)
					return true;
			
			}
			
		}
		
		return false;
	}

	private boolean match(TagNode visitNode, TagNode localNode) {
		
		if (!visitNode.getTagName().equals(localNode.getTagName()))
			return false;
		
		for (Entry<String, String> entry : attTable.entrySet()) {
			
			String visitNodeAttr = NavigationUtils.getAttribute(visitNode, entry.getKey());
			
			if (visitNodeAttr == null){
				return false;
			}
			if (!visitNodeAttr.equals(entry.getValue())){
				return false;
			}
		
		}
		
		return true;

	}

	private boolean acceptsLevel(int calcLevel) {
		if (Math.abs(calcLevel-level) <= LEVEL_THRESHOLD){
			System.out.println("OK: Level - " + calcLevel + " - " + level);
			return true;
		}
		System.out.println("ERROR: Different Level - " + calcLevel + " - " + level);
		
		return false;
	}

	private boolean acceptsPosition(int position) {
		
		int calcposition = getChildPosition(path);
		
		if (Math.abs(position-calcposition)<= POSITION_THRESHOLD)
			return true;
		System.out.println("ERROR: Different Position - " + calcposition + " - " + position);
		return false;
	
	}

	private int getChildPosition(String path) {
		
		int val = path.lastIndexOf("-") + 1;
		
		if (val <=1)
			return 0;
		
		return Integer.valueOf(path.substring(val));
		
	}

	private int getLevel(Node node) {
		
		if (node.getParent() == null)
			return 0;
		return getLevel(node.getParent()) + 1;
	}

	private int getChildPosition(Node check) {

		Node parentNode = check.getParent();

		NodeList children = parentNode.getChildren();

		for (int i = 0; i < children.size(); i++) {
			
			if (children.elementAt(i) == check)
				return i;
			
		}
		
		return -1; //should never get here.
	}

}
