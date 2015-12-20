package online.nodefilter;

import java.util.regex.Matcher;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.nodes.TagNode;

import com.hp.hpl.jena.graph.query.Pattern;

public class IsOfExtensionAttribute implements NodeFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String attribute;
	private String ending;
	private java.util.regex.Pattern p;

	public IsOfExtensionAttribute(String attribute, String ending) {
		this.attribute = attribute;
		this.ending = ending;
		p = java.util.regex.Pattern.compile(ending);
	}

	@Override
	public boolean accept(Node node) {
		
		if (node instanceof TagNode){
			TagNode tn = (TagNode)node;
			
			String val = tn.getAttribute(attribute);
			
			Matcher m = p.matcher(val);
			
			return m.find();
			
		}

		return false;

	}

}
