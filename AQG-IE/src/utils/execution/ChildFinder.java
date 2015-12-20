package utils.execution;

import org.htmlparser.Node;
import org.htmlparser.util.NodeList;

public class ChildFinder {

	public static NodeList getChildren(Node n) {
		
		NodeList nl = n.getChildren();
		
		if (nl == null){
			nl = new NodeList();
			n.setChildren(nl);
		}
		
		return nl;
	}

	public static boolean hasChildren(Node node) {
		
		if (node.getChildren()==null || node.getChildren().size()==0)
			return false;
		
		return true;
	}

}
