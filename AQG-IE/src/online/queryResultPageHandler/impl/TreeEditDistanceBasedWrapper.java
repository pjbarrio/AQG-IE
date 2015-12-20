package online.queryResultPageHandler.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.axis.encoding.Base64;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.resultpage.ResultPageDocument;
import online.documentHandler.resultpage.impl.InMemoryResultDocument;
import online.navigation.utils.NavigationUtils;
import online.nodefilter.MatchesWrap;
import online.queryResultPageHandler.QueryResultPageHandler;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;

public class TreeEditDistanceBasedWrapper extends QueryResultPageHandler {

	private static final String NO_MATCHING_TAG = "DIV";

	private static final String ID_NO_MATCHING = "id";

	private static final String ID_NO_MATCHING_VALUE = "no_matching_tag";
	
	private TagNode node;
	private String path;
	private Integer level;

	private NodeFilter wrapFilter;
	
	private TreeEditDistanceBasedWrapper(Database website, InteractionPersister persister) {
		
		super(website,persister);
		
		try {
			readWrapper(persister.getWrapperFile(website));
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		wrapFilter = new MatchesWrap(level,path,node);
		
	}

	public TreeEditDistanceBasedWrapper() {
		super();
	}

	@Override
	protected synchronized ResultPageDocument process(Document content,int expId, TextQuery query, String navigationTechnique, String extractionTechnique, int resultPage) {
		
		Parser parser = new Parser(new Lexer(new Page(content.getContent(persister.getBasePersister()), "UTF-8")));
		
		NodeList n = new NodeList();
			
		NodeList updated = new NodeList();
				
		try {
			n = parser.parse(wrapFilter);
						
			for (int i = 0; i < n.size(); i++) {
				
				if (!NavigationUtils.hasChildren(n.elementAt(i))){
					updated.add(update(n.elementAt(i)));
				} else {
					updated.add(n.elementAt(i));
				}
				
			}
			
		} catch (ParserException e) {
			e.printStackTrace();
		}
	
		return new InMemoryResultDocument(expId,content.getDatabase(), query, resultPage, navigationTechnique,extractionTechnique, new Document(content.getDatabase(),updated.toHtml()));

	}

	private Node update(Node tobeUpdated) {
		return tobeUpdated.getParent();
	}
	
	@Override
	public QueryResultPageHandler createInstance(Database website,
			InteractionPersister persister) {
		return new TreeEditDistanceBasedWrapper(website, persister);
	}
	
	private void readWrapper(File wrap) throws IOException, ParserException {
		
		System.out.println(wrap);
		
		BufferedReader br = new BufferedReader(new FileReader(wrap));
		
		level = Integer.valueOf(br.readLine().replace("LEVEL: ", ""));
		
		String nodeString = br.readLine().replace("NODE: ", "");
		
		int ind = nodeString.indexOf('<');

		if (ind < 0){
			
			node = new TagNode();
		
			node.setTagName(NO_MATCHING_TAG);
		
			node.setAttribute(ID_NO_MATCHING, ID_NO_MATCHING_VALUE);
			
		}else{

			
			path = nodeString.substring(0,ind);
			
			String nodeStr = nodeString.substring(ind);

			Parser parser = new Parser(new Lexer(new Page(nodeStr, "UTF-8")));
			
			NodeList nl = parser.parse(null);
			
			if (nl.elementAt(0) instanceof TagNode){
				node = (TagNode) nl.elementAt(0);

			}else{
				
				node = new TagNode();
				node.setTagName(NO_MATCHING_TAG);
			}
			
		}
		
		br.close();
		
	}

	@Override
	public String getName() {
		return "TEDW";
	}

	
}
