package online.resultHandler.impl;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.resultpage.ResultPageDocument;
import online.documentRetriever.DocumentRetriever;
import online.navigation.utils.NavigationUtils;
import online.resultHandler.ResultDocumentHandler;
import utils.id.Idhandler;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;

public class AllHrefResultDocumentHandler extends ResultDocumentHandler {

//	private DocumentRetriever documentRetriever;

	private AllHrefResultDocumentHandler(Database website,
			InteractionPersister persister) {
		super(website, persister);
//		documentRetriever = new DocumentRetriever(website);
	}


	public AllHrefResultDocumentHandler() {
		super();
	}


	@Override
	public ResultDocumentHandler createInstance(Database website,
			InteractionPersister persister) {
		return new AllHrefResultDocumentHandler(website,persister);
	}

	@Override
	public String getName() {
		return "ALLLINKS";
	}

	@Override
	protected List<Node> extractRecords(ResultPageDocument rs){
		
		NodeList links = getLinks(rs.getContent(),persister.getBasePersister());
		
		List<Node> readers = new ArrayList<Node>();
		
		for (int i = 0; i < links.size(); i++) {
			
//			readers.add(documentRetriever.getReader(links.elementAt(i)));
			
			readers.add(links.elementAt(i));
			
		}
		
		return readers;
	}


	private NodeList getLinks(Document content,persistentWriter pW) {
		
		Parser parser = new Parser(new Lexer(new Page(content.getContent(pW), "UTF-8")));
		
		NodeList links = new NodeList();
		
		try {
			links = parser.parse(NavigationUtils.linkFilter);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		
		return links;
	}

}
