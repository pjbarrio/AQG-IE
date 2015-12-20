package online.maintenance.ctm;

import online.navigation.utils.NavigationUtils;

import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;

public class ClusteredTreeMatching {

	public double clusteredTreeMatching(Node n1, Node n2,int siblings1, int siblings2){
		
		if (sameLabel(n1,n2)){
			
			NodeList nc1 = NavigationUtils.getChildren(n1); 
			NodeList nc2 = NavigationUtils.getChildren(n2);
			
			int m = nc1.size();
			int n = nc2.size();
			
			double[][] M = new double[m+1][n+1];
			
			for (int i = 0; i < M.length; i++) {
				M[i][0] = 0;
			}
			
			for (int j = 0; j < M[0].length; j++) {
				M[0][j] = 0;
			}
			
			for (int i = 1; i <= m; i++) {
				for (int j = 1; j <= n; j++) {
					M[i][j] = Math.max(M[i][j-1], Math.max(M[i-1][j], M[i-1][j-1] + clusteredTreeMatching(nc1.elementAt(i-1), nc2.elementAt(j-1),m,n)));
				}
			}
			
			double div = Math.max(siblings1, siblings2);
			
			if (m>0 && n>0){
				
				return M[m][n]*1.0/div;
				
			}else{
				
				return M[m][n]+1.0/div;
			}
			
		}
		
		return 0.0;
	}

	private boolean sameLabel(Node n1, Node n2) {
		
		if (n1 instanceof TagNode && n2 instanceof TagNode){
			
			String tag1 = ((TagNode)n1).getTagName();
			String tag2 = ((TagNode)n2).getTagName();
			
			if (tag1.equalsIgnoreCase(tag2)){
				return true;
			}
			return false;
		}else if (n1 instanceof TextNode && n2 instanceof TextNode){
			return true;
		}
		
		return false;
	}
	
}
