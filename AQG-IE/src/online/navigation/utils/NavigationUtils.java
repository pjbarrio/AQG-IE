package online.navigation.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import online.nodefilter.ContainsInAttribute;
import online.nodefilter.IsOfExtensionAttribute;
import online.tagcleaner.HTMLTagCleaner;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import exploration.model.Document;

import searcher.interaction.formHandler.TextQuery;

public class NavigationUtils {

	public static final String HREF_ATTRIBUTE = "href";
	public static final String ONCLICK_ATTRIBUTE = "onclick";
	public static final String JAVASCRIPT_PREFIX = "javascript:";
	public static final String HTTPS = "https";
	public static final String MAILTO = "mailto:";
	private static final String FILE = "file";
	private static final String FTP = "ftp";
	
	public static final String NEXT = "next";
	public static final String MORE = "more";
	public static final String GT = ">";
	public static final String PREVIOUS = "previous";
	public static final String OLDER = "older";
	public static final String LT = "<";
	public static final String ALL_NUMBERS = "ALL_NUMBERS";
	public static final String CONTAINS_EQUALS = "CONTAIN_EQUALS";
	private static final String EXCLUDED_EXTENSIONS_NUTCH = "\\.(avi|AVI|gif|GIF|jpg|JPG|png|PNG|ico|ICO|css|CSS|sit|SIT|eps|EPS|wmf|WMF|zip|ZIP|ppt|PPT|mpg|MPG|xls|XLS|gz|GZ|rpm|RPM|tgz|TGZ|mov|MOV|exe|EXE|jpeg|JPEG|bmp|BMP|js|JS|pdf|PDF|flv|FLV)$";

	
	public static final NodeFilter linkFilter = new OrFilter(new AndFilter(new HasAttributeFilter(NavigationUtils.HREF_ATTRIBUTE),new AndFilter(new NotFilter(new IsOfExtensionAttribute(NavigationUtils.HREF_ATTRIBUTE, NavigationUtils.EXCLUDED_EXTENSIONS_NUTCH)),new NotFilter(new ContainsInAttribute(NavigationUtils.HREF_ATTRIBUTE,NavigationUtils.HTTPS, NavigationUtils.MAILTO, NavigationUtils.FILE, NavigationUtils.FTP)))) ,new HasAttributeFilter(NavigationUtils.ONCLICK_ATTRIBUTE));
	
	public synchronized static Map<String, Map<String, Integer>> generateTextFreqStructure(
			Map<String, List<String>> textMap) {
		Map<String, Map<String,Integer>> ret = new HashMap<String, Map<String,Integer>>();
		
		for (Entry<String,List<String>> entry : textMap.entrySet()) {
			
			Map<String,Integer> map = generateFreqMap(entry.getValue());
			
			ret.put(entry.getKey(), map);
			
		}
		
		return ret;
	}

	public synchronized static boolean isAllNumbers(char[] cs) {
		if (cs.length == 0){
			return false;
		}
		for (int i = 0; i < cs.length; i++) {
			if (!Character.isDigit(cs[i]))
				return false;
		}
		return true;
	}

	
	private synchronized static Map<String, Integer> generateFreqMap(List<String> value) {
		
		Collections.sort(value);
				
		Map<String,Integer> freqMap = new HashMap<String, Integer>();

		int lastIndex = 0;
		int freq = 1;
		for (int i = 1; i < value.size(); i++) {
			
			if (value.get(i).equals(value.get(lastIndex)))
				freq++;
			else{
				freqMap.put(value.get(lastIndex), freq);
				lastIndex = i;
				freq=1;
			
			}
		}
		
		freqMap.put(value.get(lastIndex), freq);
		
		return freqMap;
	}
	
	public synchronized static boolean isInAll(Map<String, Integer> map, int length) {
		
		return map.keySet().size() == length;
		
	}
	
	public synchronized static int getFrequency(List<String> list) {
		
		if (list == null)
			return 0;
		
		Set<String> set = new HashSet<String>();
		
		for (String string : list) {
			set.add(string.trim());
		}

		return set.size();
		
	}
	
	public synchronized static String combine(String hyperLink, String onclick) {
		
		if (onclick == null)
			return hyperLink;
		if (hyperLink == null)
			return onclick;
		
		return hyperLink + " - " + onclick;
	}
	
	public synchronized static List<String> generateWords(String text) {
		
		String[] spl = text.split("[^+=\\w]");
		
		return Arrays.asList(spl);
		
	}
	
	public synchronized static boolean isAlwaysBelowFrequency(Map<String, Integer> map, int freq) {
		
		if (map == null){
			return false;
		}
		
		for (Entry<String,Integer> entry : map.entrySet()) {
			
			if (entry.getValue() > freq)
				return false;
			
		}
		
		return true;
		
	}

	public synchronized static boolean sameFrequencyInAll(Map<String, Integer> map) {
		
		if (map == null)
			return false;
		
		int prev = -1;			
		
		for (Entry<String,Integer> entry : map.entrySet()) {
			
			if (prev==-1){
				prev = entry.getValue();
			}else{
				if (prev != entry.getValue())
					return false;
				else
					prev = entry.getValue();
			}
			
		}
		
		return true;
	}
	
	public synchronized static Node unify(Parser parser, String tag){
		
		TagNode n = new TagNode();
		
		n.setTagName(tag);
		
		NodeList child = new NodeList();
		
		try {
			
			for (NodeIterator ni = parser.elements();ni.hasMoreNodes();){
				
				Node kid = ni.nextNode();
				
				kid.setParent(n);
				
				child.add(kid);
				
			}
		
		} catch (ParserException e) {
			e.printStackTrace();
		}
		
		n.setChildren(child);

		return n;
		
	}
	
	public synchronized static NodeList getChildren(Node n) {
		
		NodeList nl = n.getChildren();
		
		if (nl == null){
			nl = new NodeList();
			n.setChildren(nl);
		}
		
		return nl;
	}

	public synchronized static boolean hasChildren(Node node) {
		
		if (node.getChildren()==null || node.getChildren().size()==0)
			return false;
		
		return true;
	}

	public synchronized static boolean referenceSamePage(TagNode toreturn, Integer page, TagNode tagNode, int newPage) {
		
		if (NavigationUtils.isJavaScript(toreturn)){
			
			if (NavigationUtils.isJavaScript(tagNode)){
			
				if (!page.equals(newPage)){
					return false;
				}
		
				if (NavigationUtils.getLink(toreturn).equals(NavigationUtils.getLink(tagNode))){
					return true;
				}
				
			} else {
				return false;
			}
		} else {
			if (!NavigationUtils.isJavaScript(tagNode)){
				
				if (NavigationUtils.getLink(toreturn).equals(NavigationUtils.getLink(tagNode))){
					return true;
				}
				
			}else{
				return false;
			}
		}
		
		return false;	
		
		
	}

	public synchronized static String getLink(TagNode node) {
		
		String ret = NavigationUtils.getAttribute(node,HREF_ATTRIBUTE);
		
		if (ret != null && !ret.equals("#"))
			return ret.trim();
		
		return getAttribute(node, ONCLICK_ATTRIBUTE);
		
	}

	public synchronized static boolean isJavaScript(TagNode node) {
		
		if (getAttribute(node,ONCLICK_ATTRIBUTE) != null || NavigationUtils.getAttribute(node, HREF_ATTRIBUTE).toLowerCase().startsWith(JAVASCRIPT_PREFIX)){
			return true;
		}
		return false;
	}

	public synchronized static String getAttribute(TagNode tnode, String attribute) {
		
		String ret = tnode.getAttribute(attribute);
		
		if (ret!=null)
			ret = ret.trim();
		
		return ret;
	}
		
}
