package online.documentHandler.navigation.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.navigation.NavigationHandler;
import online.documentHandler.navigation.impl.pile.NavigablePile;
import online.documentRetriever.DocumentRetriever;
import online.maintenance.HTMLValidator;
import online.maintenance.impl.HTMLTreeStructureValidator;
import online.navigation.rule.impl.ClusteredLinkBasedNavigationRule;
import online.navigation.textTransformer.NumberReplaceMent;
import online.navigation.textTransformer.TextTransformer;
import online.navigation.utils.NavigationUtils;
import online.tagcleaner.HTMLTagCleaner;
import searcher.interaction.factory.InteractionFactory;
import searcher.interaction.formHandler.TextQuery;
import utils.execution.MapBasedComparator;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;

public class ClusterHeuristicNavigationHandler extends NavigationHandler {

	private static final String ROOT_TAG = "root";
	private static Map<String, Object> tableTable;
	private static Map<String, Integer> intTable;

	private Map<String, List<String>> textTable;
	private Map<String, List<String>> hypTable;
	private Map<String, Boolean> featuresTable;
	private Integer number;
	private ClusteredLinkBasedNavigationRule nr;
	private NavigablePile navigablePile;
	private Parser parser;
	private DocumentRetriever documentRetriever;
	private HTMLValidator structureValidator;
	private Node unifiedNode;
	private String encoding;
	private boolean lastIsValid;
	private int currentIndex;
	private boolean isNew;
	private persistentWriter pW;

	public ClusterHeuristicNavigationHandler(int experimentId) {
		super(experimentId);
	}
	
	private ClusterHeuristicNavigationHandler(int experimentId,Database website,
			InteractionPersister persister, TextQuery query, HTMLTagCleaner cleaner) {
		
		super(experimentId,website,persister,query,cleaner);
		
		File navigationHandlerFolder = persister.getNavigationHandlerFolder(this.getName());
		
		int web = website.getId();
		
		textTable = (Map<String, List<String>>)loadTable(new File(navigationHandlerFolder,web+".text.ser"));
		
		hypTable = (Map<String, List<String>>)loadTable(new File(navigationHandlerFolder,web+".links.ser"));
		
		featuresTable = (Map<String,Boolean>)loadTable(new File(navigationHandlerFolder,web+".features.ser"));
		
		try {
			number = loadIntTable(new File(navigationHandlerFolder,web+".number.ser"));
					
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		TextTransformer tt = new NumberReplaceMent();
		
		nr = new ClusteredLinkBasedNavigationRule(textTable,hypTable,featuresTable.containsKey(NavigationUtils.CONTAINS_EQUALS),tt, number);
		
		navigablePile = NavigablePile.createInstance(query, featuresTable.containsKey(NavigationUtils.ALL_NUMBERS));
		
		documentRetriever = new DocumentRetriever(experimentId,website,website.getIndex(),persister.getBasePersister(),false,persister);
		
		structureValidator = new HTMLTreeStructureValidator(website,persister.getBasePersister());
		
		encoding = persister.getWebsiteEncoding(website.getName());
		
		pW = persister.getBasePersister();
		
	}

	private Integer loadIntTable(File file) {
		
		Integer ret = getIntTable().get(file.getAbsolutePath());
		
		if (ret == null){
			
			try {
				ret = Integer.valueOf(FileUtils.readFileToString(file));
				getIntTable().put(file.getAbsolutePath(),ret);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return ret;
	}

	private synchronized Map<String, Integer> getIntTable() {
		
		if (intTable == null){
			intTable = new HashMap<String,Integer>();
		}
		return intTable;
	}

	private synchronized static Object loadTable(File file) {
		
		Object tab = getTabletable().get(file.getAbsolutePath());
		
		if (tab == null){
		
		    try {
		    	FileInputStream fis = new FileInputStream(file);
			    ObjectInputStream ois = new ObjectInputStream(fis);
			    Object ret = ois.readObject();
				ois.close();
				getTabletable().put(file.getAbsolutePath(),ret);
				return ret;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
		}
		
	    return tab;
	    
	}

	private static Map<String, Object> getTabletable() {
		
		if (tableTable == null){
			
			tableTable = new HashMap<String, Object>();
			
		}
		return tableTable;
	}

	@Override
	public boolean hasNext() {
		return navigablePile.hasNext();
	}

	@Override
	public void initialize(String cleanContent) {
		
		currentIndex = 0;
		
		initWithPage(cleanContent,currentIndex);

	}

	private void initWithPage(String cleanContent, int resultPage) {
		
		parser = new Parser(new Lexer(new Page(cleanContent, "UTF-8")));
		
		try {
			
			NodeList n = parser.parse(NavigationUtils.linkFilter);
		
			unifiedNode = NavigationUtils.unify(parser, ROOT_TAG);
			
			Map<Integer,TagNode> numbers = new HashMap<Integer,TagNode>(); 
			
			Map<TagNode,Integer> nexts = new HashMap<TagNode, Integer>();
			
			Map<TagNode,Integer> previous = new HashMap<TagNode, Integer>();
			
			Map<TagNode, Integer> remaining = new HashMap<TagNode, Integer>();
			
			for (int i = 0; i < n.size(); i++) {
				
				TagNode nd = (TagNode)n.elementAt(i);
				
				boolean added = false;
				
				if (nr.isNavigable(nd, InteractionFactory.encode(getQuery().getText(), encoding),encoding) || (nr.isAllNumbers)){ //the second condition depends on the update of the first one
					
					if (nr.isAllNumbers && featuresTable.containsKey(NavigationUtils.ALL_NUMBERS)){
						
						numbers.put(nr.number, nd);
						
						added = true;
					}
					
					if (!featuresTable.containsKey(NavigationUtils.ALL_NUMBERS) && !added && ((nr.contains(NavigationUtils.NEXT)&&featuresTable.containsKey(NavigationUtils.NEXT)) || 
							(nr.contains(NavigationUtils.MORE) && featuresTable.containsKey(NavigationUtils.MORE)) || 
							(nr.contains(NavigationUtils.GT) && featuresTable.containsKey(NavigationUtils.GT)))){
						
						nexts.put(nd, nr.getFrequency(nr.getText()));
						
						added = true;
						
					}
					
					if (!featuresTable.containsKey(NavigationUtils.ALL_NUMBERS) && !added && ((nr.contains(NavigationUtils.OLDER)&&featuresTable.containsKey(NavigationUtils.OLDER)) 
							|| (nr.contains(NavigationUtils.PREVIOUS) && featuresTable.containsKey(NavigationUtils.PREVIOUS)) 
							|| (nr.contains(NavigationUtils.LT)&&featuresTable.containsKey(NavigationUtils.LT)))){
						
						previous.put(nd, nr.getFrequency(nr.getText()));
						
						added = true;
					}
					
					if (!featuresTable.containsKey(NavigationUtils.ALL_NUMBERS) && !nr.isAllNumbers && !added){
						
						remaining.put(nd, nr.getFrequency(nr.getText()));
						
					}
				}
				
			}
			
			//PILE ALL THE NUMBERS FIRST IN ORDER
			
			for (Entry<Integer,TagNode> entry : numbers.entrySet()) {
				
				if (entry.getKey() != 0 && entry.getKey() != 1) // avoid store 0 or 1
					navigablePile.addNumber(entry.getKey(),entry.getValue(),getQuery(),resultPage);
				
			}
									
			if (!nexts.isEmpty()){
				
				//PILE THE NEXT IN ORDER OF FREQUENCY
				
				pile(nexts,getQuery(),resultPage);
			}else if (!previous.isEmpty()){
				
				//PILE THE PREVIOUS IN ORDER OF FREQUENCY
				
				pile(previous,getQuery(),resultPage);
			}else{
				
				//PILE THE REMAINING IN ORDER OF FREQUENCY
				
				pile(remaining,getQuery(),resultPage);
			}
						
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void pile(Map<TagNode, Integer> map, TextQuery textQuery, int resultPage) {
		
		Comparator<TagNode> comp = new MapBasedComparator<TagNode,Integer>(map);
		
		List<TagNode> lkey = new ArrayList<TagNode>(map.keySet());

		Collections.sort(lkey,comp);
		
		for (TagNode tagNode : lkey) {
			
			navigablePile.pile(tagNode,textQuery,resultPage);
			
		}
		
	}

	@Override
	public Document getNext() {

		isNew = true;
		
		Document r;
		
		do {

			TagNode tn = navigablePile.getNext();
						
			if (documentRetriever.hasRetrieved(tn)){
				isNew = false;
				return documentRetriever.getStoredDocument(tn);
			}
			
			r = documentRetriever.getDocument(tn);

			r.cleanContent(getCleaner(),pW);
			
		} while (!isValid(r.getContent(pW)) && navigablePile.hasNext());		

		if (lastIsValid){
			currentIndex++;
			initWithPage(r.getContent(pW),currentIndex);
			return r;
		}

		return null;	

	}

	private boolean isValid(String content) {
		
		lastIsValid = false;
		
		Parser newPage = new Parser(new Lexer(new Page(content, "UTF-8")));
		
		lastIsValid = structureValidator.isValid(unifiedNode,NavigationUtils.unify(newPage,ROOT_TAG));
	
		return lastIsValid;
		
	}

	@Override
	public NavigationHandler createInstance(int experimentId, Database website,
			InteractionPersister persister, TextQuery query, HTMLTagCleaner cleaner) {
		return new ClusterHeuristicNavigationHandler(experimentId,website,persister, query,cleaner);
	}

	@Override
	public String getName() {
		
		return "CHNH";
	}

	@Override
	public void delete() {
		
		textTable.clear();
		hypTable.clear();
		featuresTable.clear();
		navigablePile.delete();
		documentRetriever=null;
		structureValidator = null;
		
		
	}

	@Override
	public boolean nextIsNew() {
		return isNew;
	}

}
