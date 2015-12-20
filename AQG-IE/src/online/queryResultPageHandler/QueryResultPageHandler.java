package online.queryResultPageHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Parser;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.navigation.NavigationHandler;
import online.documentHandler.resultpage.ResultPageDocument;
import online.documentHandler.resultpage.impl.InDiskResultPageDocument;
import online.resultHandler.ResultDocumentHandler;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;

public abstract class QueryResultPageHandler {

	private Map<Integer, Map<TextQuery, Map<Integer, ResultPageDocument>>> processed;
	private Database website;
	protected InteractionPersister persister;

	protected QueryResultPageHandler(Database website, InteractionPersister persister) {
	
		this.website = website;
	
		this.persister = persister;
	
	}

	public QueryResultPageHandler() {
		// TODO Auto-generated constructor stub
	}

	public ResultPageDocument getResultDocument(TextQuery texQuery,int index, int expId, Database webs, NavigationHandler nh, boolean cached){
		
		if (hasProcessed(expId,texQuery,index,nh.getName())){
			
			return new InDiskResultPageDocument(expId,texQuery,index,nh.getName(),this.getName(),persister,website);
		}
		
		Document content = getRawContent(expId,webs,nh,texQuery,index);
		
		ResultPageDocument rd = process(content,expId,texQuery,nh.getName(), this.getName(), index);
		
		persister.saveExtractedResultPage(website,this.getName(),nh.getName(),rd.getExperimentId(),rd.getQuery(),rd.getResultPage(),rd.getContent(), cached);
		
		return rd;
		
	}

	private Document getRawContent(int expId, Database website,
			NavigationHandler nh, TextQuery query, int resultPage) {

		return persister.getRawResultPage(expId,website,nh.getName(),query,resultPage);
		
	}
	
	public abstract String getName();

	protected abstract ResultPageDocument process(Document content,int expId, TextQuery query, String navigationTechnique, String extractionTechnique, int resultPage);

	private synchronized boolean hasProcessed(int expId, TextQuery texQuery, int index, String navigationTechnique) {
//		return getProcessed(expId,texQuery).containsKey(index) || persister.hasProcessedPage(website,this.getName(),navigationTechnique,expId,texQuery,index);
		
		return persister.hasProcessedPage(website,this.getName(),navigationTechnique,expId,texQuery,index);
	
	}

//	private Map<Integer,ResultPageDocument> getProcessed(int expId, TextQuery texQuery){
//		
//		Map<Integer,ResultPageDocument> ret = getProcessed(expId).get(texQuery);
//		
//		if (ret == null){
//			ret = new HashMap<Integer, ResultPageDocument>();
//			getProcessed(expId).put(texQuery,ret);
//		}
//		
//		return ret;
//	}

//	private Map<TextQuery, Map<Integer,ResultPageDocument>> getProcessed(int expId) {
//		
//		Map<TextQuery,Map<Integer,ResultPageDocument>> ret = getProcessed().get(expId);
//		
//		if (ret == null){
//			
//			ret = new HashMap<TextQuery, Map<Integer,ResultPageDocument>>();
//			
//			getProcessed().put(expId,ret);
//			
//		}
//		
//		return ret;
//		
//	}

//	private Map<Integer, Map<TextQuery,Map<Integer,ResultPageDocument>>> getProcessed() {
//		
//		if (processed == null){
//			
//			processed = new HashMap<Integer, Map<TextQuery,Map<Integer,ResultPageDocument>>>();
//			
//		}
//		
//		return processed;
//	}

	public abstract QueryResultPageHandler createInstance(Database website, InteractionPersister persister);

}
