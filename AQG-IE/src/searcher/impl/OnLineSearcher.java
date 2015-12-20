package searcher.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.htmlparser.Node;

import com.google.gdata.util.common.base.Pair;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.navigation.NavigationHandler;
import online.documentHandler.resultpage.ResultPageDocument;
import online.documentRetriever.DocumentRetriever;
import online.documentRetriever.download.impl.DownloaderRunnable;
import online.queryResultPageHandler.QueryResultPageHandler;
import online.resultHandler.ResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;

import edu.oswego.cs.dl.util.concurrent.Semaphore;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.database.OnlineDatabase;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;
import searcher.Searcher;
import searcher.interaction.Interaction;
import searcher.interaction.factory.InteractionFactory;
import searcher.interaction.formHandler.TextQuery;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.id.Idhandler;
import utils.id.InputNamesHandler;
import utils.persistence.InteractionPersister;

public class OnLineSearcher extends Searcher {

	class ConcurrentDownloader implements Runnable{

		private DocumentRetriever documentRetriever;
		private int resultIndex;
		private ResultPageDocument rs;
		private Node node;
		private File toSave;
		private String rdhName;
		private double randomWait;
		private Semaphore sp;
		private long activeNow;

		public ConcurrentDownloader(DocumentRetriever documentRetriever,
				Pair<ResultPageDocument, Pair<Integer, Pair<Node, File>>> info, String rdhName, double randomWait, Semaphore sp) {
			
			this.rs = info.getFirst();
			this.resultIndex = info.getSecond().getFirst();
			this.node = info.getSecond().getSecond().getFirst();
			this.toSave = info.getSecond().getSecond().getSecond();
			this.rdhName = rdhName;
			
			this.documentRetriever = documentRetriever;
			this.randomWait = randomWait;
			this.sp = sp;
			this.activeNow = 60 - sp.permits();
		}

		@Override
		public void run() {
			
				if (documentRetriever.hasRetrieved(node)){
					
					Document doc = documentRetriever.getStoredDocument(node);
					
					persister.prepareExtractedResult(rs.getExperimentId(),rs.getExtractionTechnique(),rs.getNavigationTechnique(),rdhName,doc,rs.getQuery(),rs.getResultPage(),resultIndex);
					
				} else {
				
					try {
						Thread.sleep((long) (Math.max(1, Math.min(activeNow * 1000, randomWait) * Math.random())));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					Document reader = documentRetriever.getDocument(node,toSave);
					
					persister.saveExtractedResult(rs.getExtractionTechnique(),rs.getNavigationTechnique(),rdhName, rs.getExperimentId(), rs.getQuery(), rs.getResultPage(), resultIndex, reader,true);
							
//					String doc = persister.getExtractedResultPath(website,rs.getExtractionTechnique(), rs.getNavigationTechnique(),rdhName, rs.getExperimentId(), rs.getQuery(), rs.getResultPage(), resultIndex);
//					
//					long idDoc = idHandler.get(doc);
					
					persister.prepareExtractedResult(rs.getExperimentId(),rs.getExtractionTechnique(),rs.getNavigationTechnique(),rdhName,reader,rs.getQuery(),rs.getResultPage(),resultIndex);
					
				}

				sp.release();
				
		}
		
	}

	
	
	private String encoding;
	private String formFile;
	private List<String> inputNames;
	private long maxAllowedNavigation;
	private Database website;
	private int attempt_number;
	private OnlineDocumentHandler onlineDocumentHandler;
	private int expId;
	private HTMLTagCleaner htmlTagCleaner;
	private Map<Long, TextQuery> processedQueries;
	private Map<String, Integer> nextQueryTable;
	private Map<Integer, Set<TextQuery>> experimentQueries;
	private InteractionPersister persister;
	private double randomWait;

	private HashSet<Long> donotstore;

	public OnLineSearcher(long timebetweensearch,String encoding, Database website,
			String formFile, String inputNameFiles, long maxAllowedNavigations, int attempt_number, 
			OnlineDocumentHandler onlineDocumentHandler,int expId,HTMLTagCleaner htmlTagCleaner, InteractionPersister persister){
		this(timebetweensearch, encoding, website, formFile, inputNameFiles, maxAllowedNavigations, attempt_number, onlineDocumentHandler, expId, htmlTagCleaner, persister, false,0.0);
	}
	
	public OnLineSearcher(long timebetweensearch,String encoding, Database website,
			String formFile, String inputNameFiles, long maxAllowedNavigations, int attempt_number, 
			OnlineDocumentHandler onlineDocumentHandler,int expId,HTMLTagCleaner htmlTagCleaner, InteractionPersister persister, boolean cachIds,double randomWait){
		super(timebetweensearch,website);
		initializeStructures();
		experimentQueries = null;
		this.encoding = encoding;
		this.website = website;
		this.formFile = formFile;
		this.inputNames = InputNamesHandler.loadInputNames(inputNameFiles);
		this.maxAllowedNavigation = maxAllowedNavigations;
		this.attempt_number = attempt_number;
		this.onlineDocumentHandler = onlineDocumentHandler;
		this.expId = expId;
		this.htmlTagCleaner = htmlTagCleaner;
		this.persister = persister;
		this.randomWait = randomWait;
		this.donotstore = new HashSet<Long>();
	}
	
	private void initializeStructures() {
		
		processedQueries = null;
		nextQueryTable = null;
//		totalStoredTable = null;
		
	}

	@Override
	protected synchronized boolean executeSearch(long id, List<String> must_words,
			List<String> must_not_words, boolean createBatch, long waitbetweeensearch) {
				
		TextQuery texQuery = new TextQuery(must_words);
		
		//should be the same searcher, not the queryResultPageIndexer ...
		
		NavigationHandler nh = onlineDocumentHandler.getNavigationHandler(website,persister,texQuery);

		if (hasProcessed(id,texQuery,expId,nh)){
			
			System.out.println("has Processed the query..." + texQuery.getText());
			
			donotstore.add(id);
			
			return true;
		
		}
		
		try {
			Thread.sleep((long) ((double)waitbetweeensearch * Math.random() + 1000.0));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("has to process the query: " + texQuery.getText());
		
		getExperimentQueries(expId).add(texQuery);
		
		getProcessedQueries().put(id,texQuery);
		
		Interaction interaction = InteractionFactory.generateInstance(website, formFile, encoding, inputNames);
		
		System.out.println("Interaction Generated");
		
		Document rd = null;
		
		int attempts = 0;
		
		while (rd == null && attempts<attempt_number){
		
			String idIQ = ClockEnum.ISSUED_QUERY.name() + Thread.currentThread().getId();
			
			Clock.startTime(idIQ);
			
			rd = interaction.submitQuery(texQuery);
		
			Clock.stopTime(idIQ);

			if (rd != null){
				
				rd.setExperimentId(expId);
				
				if (createBatch)
					persister.prepareQueryTime(expId,website.getId(),texQuery,0,Clock.getWallTime(idIQ));
				else
					persister.saveQueryTime(expId,website.getId(),texQuery,0,Clock.getWallTime(idIQ));
			}
			System.out.println("query submitted");
			
			attempts++;
			
		}
		
		if (rd != null){
//		
//			StringWriter sw = new StringWriter();
//			
//			try {
//				IOUtils.copy(rd, sw);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
//			String cleanContent = htmlTagCleaner.clean(sw.toString());
			
			rd.cleanContent(htmlTagCleaner,persister.getBasePersister());
			
			save(texQuery,nh.getName(),0,rd,expId,createBatch,true);
			
			nh.initialize(rd.getContent(persister.getBasePersister()));
			
			System.out.println("Navigation Initialized");
			
			int index = 0;
			
			while (nh.hasNext() && index <= maxAllowedNavigation){
				
				index++;
				
				String idClock = ClockEnum.NEXT_REQUIRED.name() + Thread.currentThread().getId();
				
				Clock.startTime(idClock);
				
				Document next = nh.getNext();
				
				boolean isNew = nh.nextIsNew();
				
				Clock.stopTime(idClock);
				
				if (createBatch)
					persister.prepareQueryTime(expId,website.getId(),texQuery,index,Clock.getWallTime(idClock));
				else
					persister.saveQueryTime(expId,website.getId(),texQuery,index,Clock.getWallTime(idClock));
				
				System.out.println("next retrieved");
				
//				sw = new StringWriter();
//				
//				try {
//					IOUtils.copy(next, sw);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
////				cleanContent = htmlTagCleaner.clean(sw.toString()); is already clean
				
//				cleanContent = sw.toString();
				
				save(texQuery,nh.getName(),index,next,expId,createBatch,isNew);
				
				System.out.println("Next Saved");
				
			}
			
//			getTotalStored().put(generateId(expId, id),index+1);
			
			return true;
			
		} else {
			
			return false;
		
		}
	}
		
//	private Map<String, Integer> getTotalStored() {
//		
//		if (totalStoredTable == null){
//			totalStoredTable = new HashMap<String, Integer>();
//		}
//		
//		return totalStoredTable;
//		
//	}

	private void save(TextQuery texQuery, String navigationTechnique, int page, Document cleanContent, int expId, boolean createBatch, boolean isNew) {
		
		persist(texQuery,navigationTechnique,page,cleanContent,expId,createBatch,isNew);
		
//		getRawContent(expId,texQuery).put(page,cleanContent);
		
	}

//	private Map<Integer, String> getRawContent(int expId, TextQuery texQuery) {
//		
//		Map<Integer,String> ret = getRawContent(expId).get(texQuery);
//		
//		if (ret == null){
//			ret = new HashMap<Integer, String>();
//			getRawContent(expId).put(texQuery,ret);
//		}
//		
//		return ret;
//	}

//	private Map<TextQuery, Map<Integer,String>> getRawContent(int expId) {
//		
//		Map<TextQuery,Map<Integer,String>> ret = getRawContent().get(expId);
//		
//		if (ret == null){
//			ret = new HashMap<TextQuery, Map<Integer,String>>();
//			getRawContent().put(expId,ret);
//		}
//		
//		return ret;
//	}

//	private Map<Integer,Map<TextQuery,Map<Integer,String>>> getRawContent() {
//		
//		if (rawPageContentTable == null){
//			rawPageContentTable = new HashMap<Integer, Map<TextQuery,Map<Integer,String>>>();
//		}
//		return rawPageContentTable;
//	}

	private void persist(TextQuery texQuery, String navigationTechnique, int page, Document cleanContent,
			int expId, boolean createBatch, boolean isNew) {
		
		persister.saveRawResultPage(expId,website,texQuery,navigationTechnique,page,cleanContent,createBatch,isNew);
		
	}

	private synchronized boolean hasProcessed(long queryId, TextQuery texQuery, int expId, NavigationHandler nh) {
		
//		if (getExperimentQueries(expId).contains(texQuery)){
//			
//			System.out.println("AHAAAAAAAAAA!");
//			
//			return true;
//		}
		if (getExperimentQueries(expId).contains(texQuery) || persister.hasProcessedQuery(expId,website,texQuery,nh.getName())){
			
			//need to save the id
			
			if (getProcessedQueries().get(queryId) == null){
				getProcessedQueries().put(queryId,texQuery);
			}
			return true;
			
		}
		
		return false;
		
		
		
	}

	private synchronized  Set<TextQuery> getExperimentQueries(int expId) {
		
		Set<TextQuery> s = getExperimentQueries().get(expId);
		
		if (s == null){
			s = new HashSet<TextQuery>();
			getExperimentQueries().put(expId, s);
		}
		
		return s;
	}

	private Map<Integer, Set<TextQuery>> getExperimentQueries() {
		if (experimentQueries == null){
			experimentQueries = new HashMap<Integer, Set<TextQuery>>();
		}
		return experimentQueries;
	}

	private synchronized Map<Long,TextQuery> getProcessedQueries() {
		
		if (processedQueries == null){
			processedQueries = new HashMap<Long, TextQuery>();
		}
		return processedQueries;
	}

	@Override
	public synchronized  List<Document> retrieveNextDocuments(long queryId) {
		
		int index = nextQueryPage(queryId,expId);
		
		QueryResultPageHandler qrph = onlineDocumentHandler.getQueryResultPageHandler(website,persister);
		
		TextQuery query = getQuery(queryId);
		
		NavigationHandler nh = onlineDocumentHandler.getNavigationHandler(website,persister,query);
		
//		ResultPageDocument rs = qrph.getResultDocument(query, index, expId,nh.getName(),getRawContent(expId, query).get(index));
		
		ResultPageDocument rs = qrph.getResultDocument(query, index, expId, website,nh,false);
		
		ResultDocumentHandler resultDocumentHandler = onlineDocumentHandler.getResultDocumentsHandler(website,persister);
		
		Set<Document> ret = new HashSet<Document>(resultDocumentHandler.getDocumentResults(rs));
		
		return new ArrayList<Document>(ret);

	}

	private int nextQueryPage(long queryId, int expId) {
		
		Integer i = getNextQueryTable().get(generateId(expId,queryId));
		
		if (i == null){
			i=-1;
		}
		
		getNextQueryTable().put(generateId(expId, queryId), i+1);
		
		return i+1;
	}

	private Map<String,Integer> getNextQueryTable() {
		
		if (nextQueryTable == null){
			nextQueryTable = new HashMap<String, Integer>();
		}
		
		return nextQueryTable;
	}

	private String generateId(int expId, long queryId) {
		return expId + "-" +queryId;
	}

	private synchronized TextQuery getQuery(long queryId) {
		return getProcessedQueries().get(queryId);
	}

	@Override
	public synchronized  List<Document> retrieveAllowedDocuments(long queryId){
		
		TextQuery query = getQuery(queryId);
		
		NavigationHandler nh = onlineDocumentHandler.getNavigationHandler(website,persister,query);

		QueryResultPageHandler qrph = onlineDocumentHandler.getQueryResultPageHandler(website,persister);

		ResultDocumentHandler resultDocumentHandler = onlineDocumentHandler.getResultDocumentsHandler(website,persister);
		
		int total = getNumberOfProcessedPages(expId,queryId,nh);
		
		Set<Document> documents = new HashSet<Document>();
	
		for (int index = 0; index < total; index++) {
			
//			ResultPageDocument rs = qrph.getResultDocument(query, index, expId, nh.getName(), getRawContent(expId, query).get(index));
			
			ResultPageDocument rs = qrph.getResultDocument(query, index, expId, website,nh,false);
			
			documents.addAll(resultDocumentHandler.getDocumentResults(rs));
			
		}

		return new ArrayList<Document>(documents);

	}

	private synchronized int getNumberOfProcessedPages(int experimentId, long queryId, NavigationHandler nh) {
		
		return persister.getNumberOfProcessedPages(experimentId,website,getQuery(queryId),nh.getName());
	
	}

	@Override
	public synchronized  void cleanQueryInternal(long queryId) {

		TextQuery texQuery;
		
		texQuery = getProcessedQueries().remove(queryId);
		donotstore.remove(queryId);
		
		getNextQueryTable().remove(generateId(expId, queryId));
		
		//should be the same searcher, not the queryResultPageIndexer ...
		
		onlineDocumentHandler.deleteNavigationHandler(website,persister,texQuery);
		
		
		
	}

	@Override
	protected synchronized  void cleanSearcherInternal() {
		getProcessedQueries().clear();
		getNextQueryTable().clear();
		getExperimentQueries().clear();
		persister.clearDocuments(website);
		donotstore.clear();
	}

	@Override
	protected synchronized  void storeAllowedDocuments(long id) {
		
		if (donotstore.contains(id))
			return;
		
		QueryResultPageHandler qrph = onlineDocumentHandler.getQueryResultPageHandler(website,persister);
		
		TextQuery query = getQuery(id);
		
		NavigationHandler nh = onlineDocumentHandler.getNavigationHandler(website,persister,query);
		
		int total = getNumberOfProcessedPages(expId,id,nh);
		
		ResultDocumentHandler resultDocumentHandler = onlineDocumentHandler.getResultDocumentsHandler(website,persister);

		List<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>> documents = new ArrayList<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>>();
	
		for (int index = 0; index < total; index++) {
			
			ResultPageDocument rs = qrph.getResultDocument(query, index, expId, website,nh,true);
			
			documents.addAll(resultDocumentHandler.getDocumentURLS(rs));
			
		}
		
		store(documents,resultDocumentHandler.getName());
		
	}

	private void store(List<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>> documents,String rdhName) {
				
		DocumentRetriever prep = new DocumentRetriever(-1,website,((OnlineDatabase)website).getWebsite(),persister.getBasePersister(),true,persister,true);
		
		Set<String> processed = new HashSet<String>();
		
		List<Thread> ts = new ArrayList<Thread>(documents.size());
		
		List<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>> later = new ArrayList<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>>();
		
		Semaphore sp = new Semaphore(60);
		
		while (documents.size() > 0){
			
			Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>> info = documents.remove(0);
			
			URL url = prep.getStorableUrl(info.getSecond().getSecond().getFirst());
			
			if (!processed.contains(url.toString())){
				
				try {
					sp.acquire();
					
					Thread.sleep(250 + (long)(Math.random()*250.0));
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				processed.add(url.toString());
				
				Thread t = new Thread(new ConcurrentDownloader(new DocumentRetriever(-1,website,((OnlineDatabase)website).getWebsite(),persister.getBasePersister(),true,persister,true),info,rdhName,randomWait,sp));
				
				t.start();
				
				ts.add(t);
			
			}else{
				later.add(info);
			}
			
		}
		
		for (int i = 0; i < ts.size(); i++) {
			
			try {
				ts.get(i).join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		while (later.size() > 0){
			
			Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>> info = later.remove(0);
			
			Thread t = new Thread(new ConcurrentDownloader(prep,info,rdhName,randomWait,sp));
			
			t.start();
			
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	@Override
	public synchronized  void finishBatchDownloader(ExperimentEnum experimentConsistensyId, int databaseId, String computerName, int goodState) {

		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				persister.finishBatchDownloader(website.getId());
				
			}
		});
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		persister.getBasePersister().reportExperimentStatus(experimentConsistensyId, databaseId, computerName, goodState);
		
	}

	@Override
	public synchronized  void finishNegativeBatchDownloader(ExperimentEnum experimentConsistensyId, int idDatabase, String computerName, int goodState, int split) {
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				persister.finishNegativeBatchDownloader(website.getId());
				
			}
		});
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		persister.getBasePersister().reportExperimentStatus(persister.getBasePersister().getExperimentConsistensyId(experimentConsistensyId,split), idDatabase, computerName, goodState);
		
	}

}
