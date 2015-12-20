package online.documentHandler;

import java.util.HashMap;
import java.util.Map;

import exploration.model.Database;

import online.documentHandler.navigation.NavigationHandler;
import online.queryResultPageHandler.QueryResultPageHandler;
import online.resultHandler.ResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import searcher.interaction.formHandler.TextQuery;
import utils.id.Idhandler;
import utils.persistence.InteractionPersister;

public class OnlineDocumentHandler {

	private QueryResultPageHandler rpw;
	private NavigationHandler nh;
	private ResultDocumentHandler rdh;
	private Map<Database,QueryResultPageHandler> queryResultPageHandlerTable;
	private Map<Database, Map<TextQuery,NavigationHandler>> navigationHandlerTable;
	private Map<Database, ResultDocumentHandler> resultDocumentHandlerTable;
	private HTMLTagCleaner cleaner;

	public OnlineDocumentHandler(QueryResultPageHandler rpw, NavigationHandler nh, ResultDocumentHandler rdh, HTMLTagCleaner cleaner){
		
		initializeStructures();
		
		this.rpw = rpw;
		this.nh = nh;
		this.rdh = rdh;
		this.cleaner = cleaner;
	}

	private void initializeStructures() {
		
		queryResultPageHandlerTable = new HashMap<Database, QueryResultPageHandler>();
		navigationHandlerTable = new HashMap<Database, Map<TextQuery,NavigationHandler>>();
		resultDocumentHandlerTable = new HashMap<Database, ResultDocumentHandler>();
		
	}

	public synchronized QueryResultPageHandler getQueryResultPageHandler(Database website, InteractionPersister persister) {
		
		QueryResultPageHandler ret = queryResultPageHandlerTable.get(website);
		
		if (ret == null){
			ret = rpw.createInstance(website,persister);
			queryResultPageHandlerTable.put(website, ret);
		}
		
		return ret;
		
	}

	public synchronized NavigationHandler getNavigationHandler(Database website, InteractionPersister persister, TextQuery texQuery) {
		
		NavigationHandler navH = getNavigationHandler(website).get(texQuery);
		
		if (navH == null){
			navH = nh.createInstance(nh.getExperimentId(),website, persister,texQuery,cleaner.createInstance());
			getNavigationHandler(website).put(texQuery, navH);
		}
		
		return navH;
	}

	private synchronized Map<TextQuery,NavigationHandler> getNavigationHandler(Database website){
		
		Map<TextQuery,NavigationHandler> ret = navigationHandlerTable.get(website);
		
		if (ret == null){
			ret = new HashMap<TextQuery, NavigationHandler>();
			navigationHandlerTable.put(website, ret);
		}
		
		return ret;
		
	}
	
	public synchronized void deleteNavigationHandler(Database website,
			InteractionPersister persister, TextQuery texQuery) {
		NavigationHandler nh = getNavigationHandler(website).remove(texQuery);
		if (nh != null)
			nh.delete();
		//TODO see why might be null
	}
	
	public synchronized ResultDocumentHandler getResultDocumentsHandler(Database website, InteractionPersister persister) {
		
		ResultDocumentHandler ret = resultDocumentHandlerTable.get(website);
		
		if (ret == null){
			ret = rdh.createInstance(website,persister);
			resultDocumentHandlerTable.put(website, ret);
		}
		
		return ret;
		
	}

}
