package online.navigation.rule;

import org.htmlparser.nodes.TagNode;



public interface NavigationRule {

	boolean isNavigable(TagNode elementAt, String qur,String encoding);

}
