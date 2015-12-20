package online.nodefilter;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.nodes.TagNode;

public class ContainsInAttribute implements NodeFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String attribute;
	private String[] contained;

	public ContainsInAttribute(String attribute, String ...contained) {
		this.attribute = attribute;
		this.contained = new String[contained.length];
		for (int i = 0; i < contained.length; i++) {
			this.contained[i] = contained[i].toLowerCase();
		}

	}

	@Override
	public boolean accept(Node node) {
		
		if (node instanceof TagNode){
			TagNode tn = (TagNode)node;
			
			String val = tn.getAttribute(attribute);
			
			if (val == null)
				return false;
			
			for (String cont : contained) {
				if (val.toLowerCase().contains(cont))
					return true;
			}
			
			return false;
			
		}
		return false;
	}

}
