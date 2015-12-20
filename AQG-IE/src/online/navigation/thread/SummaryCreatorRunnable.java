package online.navigation.thread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import online.navigation.rule.NavigationRule;
import online.navigation.rule.impl.ClusteredLinkBasedNavigationRule;
import online.navigation.textTransformer.TextTransformer;
import online.navigation.utils.NavigationUtils;
import utils.execution.TextRetriever;

public class SummaryCreatorRunnable implements Runnable {


	private File[] follows;
	private List<String> queries;
	private File outputLinks;
	private TextTransformer tt;
	private File outputText;
	private File outputFeatures;
	private File outputNumber;
	private String encoding;

	public SummaryCreatorRunnable(File[] follows, List<String> tfidfs,
			TextTransformer tt, File outputLinks, File outputText,
			File outputFeatures, File outputNumber, String encoding) {

		this.follows = follows;
		this.queries = tfidfs;
		this.tt = tt;
		this.outputLinks = outputLinks;
		this.outputText = outputText;
		this.outputFeatures = outputFeatures;
		this.outputNumber = outputNumber;
		this.encoding = encoding;

	}

	@Override
	public void run() {

		Map<String,List<String>> hypMap = new HashMap<String, List<String>>();

		Map<String, List<String>> textMap = new HashMap<String,List<String>>();

		Map<String, NodeList> nodesMap = new HashMap<String, NodeList>();
		
		Map<String,Boolean> featuresMap = new HashMap<String, Boolean>();
		
		boolean containsEquals = false;

		//Load The Structures

		for (int i = 0; i < follows.length; i++) {

			try {

				int index = Integer.valueOf(follows[i].getName().replaceAll(".html", ""));

				String qur = "";

				if (index>=0)
					qur = queries.get(index).toLowerCase();

				String htmlContent = FileUtils.readFileToString(follows[i]);

				Parser parser = new Parser(new Lexer(new Page(htmlContent, "UTF-8")));

				NodeList n = parser.parse(NavigationUtils.linkFilter);

				for (int j = 0; j < n.size(); j++) {

					TagNode node = (TagNode)n.elementAt(j);

					String text = TextRetriever.recoverText(node,true,tt.personalize(qur));

					updateMap(text,textMap,follows[i].getName());

					String hyperLink = NavigationUtils.getAttribute(node, NavigationUtils.HREF_ATTRIBUTE);

					if (hyperLink != null && hyperLink.toLowerCase().contains("=" + qur)){
						containsEquals = true;
						featuresMap.put(NavigationUtils.CONTAINS_EQUALS, true);
					}

					String onclick = NavigationUtils.getAttribute(node,NavigationUtils.ONCLICK_ATTRIBUTE);

					String combined = NavigationUtils.combine(hyperLink,onclick);

					updateMap(combined,hypMap,follows[i].getName());

				}
				
				nodesMap.put(follows[i].getName(), n);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}			

		writeMap(textMap,outputText);
		
		writeMap(hypMap,outputLinks);
		
		ClusteredLinkBasedNavigationRule nr = new ClusteredLinkBasedNavigationRule(textMap,hypMap,containsEquals,tt, follows.length);
				
		try {

			List<TagNode> acceptedNodes = new ArrayList<TagNode>();
			
			for (int i = 0; i < follows.length; i++) {
				
				NodeList nl = nodesMap.get(follows[i].getName());
				
				int index = Integer.valueOf(follows[i].getName().replaceAll(".html", ""));

				String qur = "";

				if (index>=0)
					qur = queries.get(index).toLowerCase();

				for (int j = 0; j < nl.size(); j++) {

					TagNode node = (TagNode)nl.elementAt(j);
					
					if (nr.isNavigable(node,qur,encoding)){
					
						acceptedNodes.add(node);
						
						if (nr.isAllNumbers){
							featuresMap.put(NavigationUtils.ALL_NUMBERS, true);
						}else{
							if (nr.contains(NavigationUtils.NEXT))
								featuresMap.put(NavigationUtils.NEXT, true);
							if (nr.contains(NavigationUtils.MORE))
								featuresMap.put(NavigationUtils.MORE, true);
							if (nr.contains(NavigationUtils.GT))
								featuresMap.put(NavigationUtils.GT, true);;
							if (nr.contains(NavigationUtils.PREVIOUS))
								featuresMap.put(NavigationUtils.PREVIOUS, true);;
							if (nr.contains(NavigationUtils.OLDER))
								featuresMap.put(NavigationUtils.OLDER, true);;
							if (nr.contains(NavigationUtils.LT))
								featuresMap.put(NavigationUtils.LT, true);;
						}
						
					}

				}

			}

		} catch (Exception e){
			//
		}

		writeMap(featuresMap, outputFeatures);
		
		try {
			
			FileUtils.writeStringToFile(outputNumber, Integer.toString(follows.length));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	private void writeMap(Object map, File file) {
				
	    try {
	    	FileOutputStream fos = new FileOutputStream(file);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
		    oos.writeObject(map);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void updateMap(String text, Map<String, List<String>> textMap,
			String name) {
		
		List<String> files = textMap.get(text);
		
		if (files == null){
			files = new ArrayList<String>();
			textMap.put(text, files);
		}
		
		files.add(name);
		
	}
}