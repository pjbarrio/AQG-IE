package online.maintenance;

import org.htmlparser.Node;
import org.htmlparser.Parser;

public abstract class HTMLValidator {

	public abstract String getName();

	public abstract boolean isValid(Node unifiedNode, Node unify);


}
