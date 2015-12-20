package utils.execution;

import online.navigation.textTransformer.DummyTextTransformer;
import online.navigation.textTransformer.TextTransformer;
import online.navigation.utils.NavigationUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.Node;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;

public class TextRetriever {

public static String recoverText(Node node, boolean imageIsText, TextTransformer tt) {
		
		if (node instanceof TextNode){
			return tt.transformText(/*StringEscapeUtils.unescapeHtml3(*/node.getText()/*)*/);
		}else if (node instanceof ImageTag){
			
			String ret = NavigationUtils.getAttribute((ImageTag)node,"alt");
			
			if (ret != null)
				ret = tt.transformImageText(/*StringEscapeUtils.unescapeHtml3(*/ret/*)*/);
			else
				ret = "";
			
			if (imageIsText)
				return ret + " " + tt.transformImageFile(/*StringEscapeUtils.unescapeHtml3(*/((ImageTag) node).getImageURL()/*)*/);
			else
				return ret;
				
		}else{
			
			String ret = "";
			
			NodeList child = ChildFinder.getChildren(node);
			
			for (int i = 0; i < child.size(); i++) {
				
				ret += recoverText(child.elementAt(i),imageIsText,tt);
				
			}
			
			return ret;
		}
		
	}

	public static String recoverText(Node node){
		return recoverText(node, false, new DummyTextTransformer());
	}
	
	public static String recoverText(Node node, boolean b) {
		return recoverText(node, b, new DummyTextTransformer());
	}

}
