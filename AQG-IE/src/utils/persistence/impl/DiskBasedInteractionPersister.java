package utils.persistence.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.documentHandler.resultpage.ResultPageDocument;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Database;
import exploration.model.Document;

import searcher.Searcher;
import searcher.impl.CachedSearcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.document.DocumentHandler;
import utils.id.DatabaseIndexHandler;
import utils.id.Idhandler;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;

public class DiskBasedInteractionPersister implements InteractionPersister {

	class SaverRunnable implements Runnable{

		class Saver implements Runnable{

			private Document document;
			private Semaphore sem;
			private persistentWriter pW;

			public Saver(Document document, Semaphore sem, persistentWriter pW) {
				this.document = document;
				this.sem = sem;
				this.pW = pW;
			}

			@Override
			public void run() {
				
				try {
					
					FileUtils.writeStringToFile(document.getFilePath(pW), document.getContent(pW));
					
				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				
				sem.release();
				
			}
			
		}
		
		private Collection<Document> list;
		private persistentWriter pW;
		
		public SaverRunnable(Collection<Document> map, persistentWriter pW) {
			
			this.list = map;
			this.pW= pW;
			
		}

		@Override
		public void run() {
			
			Semaphore sem = new Semaphore(150);
			
			Set<String> already = new HashSet<String>();
			
			for (Document doc : list) {
				
				try {
				
					sem.acquire();
					
					File folder = doc.getFilePath(pW).getParentFile();
					
					if (!already.contains(folder.getAbsolutePath())){
						already.add(folder.getAbsolutePath());
						folder.mkdirs();
					}
					
					new Thread(new Saver(doc,sem,pW)).start();
					
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				
				}
								
			}
			
			list.clear();
			
		}
		
	}
	
	private static final String EMPTY_QUERY = "EMPTY_QUERY";
	private static final String QUERY_FOLDER = "QUERY";
	private static final String PAGE_EXTRACTION = "PAGE_EXTRACTION";
	private static final String RESULT_EXTRACTION = "RESULTS";
	private static final String RELATION_EXTRACTION = "relationExtraction";
	private String prefix;
	private persistentWriter basePersister;
	private Map<String, Map<String,Document>> rawResultsMap = new HashMap<String, Map<String,Document>>();
	private Map<String, Document> extractedResultPage = new HashMap<String, Document>();
	private Map<String, List<Document>> extractedResultMap = new HashMap<String, List<Document>>();
	private Map<Integer, Map<Integer, Set<Long>>> processedCachedTable = new HashMap<Integer, Map<Integer,Set<Long>>>();
	private Map<Integer, Map<Integer, Map<Long, List<Document>>>> queryResultsTable = new HashMap<Integer, Map<Integer,Map<Long,List<Document>>>>();
	private static Map<Integer,Searcher> searcherTable = new HashMap<Integer, Searcher>();

	public DiskBasedInteractionPersister(String prefix, String fileIndex, persistentWriter pW) {
		this.prefix = prefix;
		this.basePersister = pW;
	}

	@Override
	public void saveRawResultPage(int expId, Database website,TextQuery texQuery, String navigationTechnique, int page,
			Document cleanContent, boolean createBatch, boolean isNew) {
		
		File folder = getRawResultPageFolder(expId,website,texQuery, navigationTechnique);
		
		File file = new File(folder,getRawResultPageFile(page));
			
		cleanContent.setFilePath(file);

		getBasePersister().getDocumentHandler(website,expId).save(cleanContent);
		
		if (createBatch){	
			
			Map<String,Document> list;
			
			synchronized (rawResultsMap) {
		
				list = rawResultsMap.get(folder.getAbsolutePath());
				
				if (list == null){
					list = new HashMap<String, Document>();
					rawResultsMap.put(folder.getAbsolutePath(), list);
				}
				
			}
			
			list.put(file.getAbsolutePath(),cleanContent);
			
			if (isNew)
				getBasePersister().prepareStoredDownloadedDocument(cleanContent);
			
			getBasePersister().prepareRawResultPage(expId,website.getId(),texQuery,navigationTechnique, page);
		
		}else{
			
			folder.mkdirs();
			
			try {
				FileUtils.writeStringToFile(file, cleanContent.getContent(basePersister));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			getBasePersister().saveRawResultPage(expId,website.getId(),texQuery,navigationTechnique, page);
			
			getBasePersister().saveStoredDownloadedDocument(cleanContent);	
			
		}
	}

	private String getRawResultPageFile(int page) {
		return page + ".html";
	}

	private File getRawResultPageFolder(int expId, Database website,
			TextQuery texQuery, String navigationTechnique) {
		return new File(prefix + "/" + expId + "/" + getIndex(website) + "/" + QUERY_FOLDER + "/" + navigationTechnique + "/" + asFolder(texQuery) + "/");
	}

	private Integer getIndex(Database website) {
		return website.getId();
	}

	private String asFolder(TextQuery texQuery) {
		
		String ret = texQuery.getText().replaceAll("[\\W]", "_");
		
		if (ret.isEmpty()){
			ret = EMPTY_QUERY;
		}
		return ret;
	}

	@Override
	public File getWrapperFile(Database website) {
		
		int index = getIndex(website);
		
		return new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Wrappers/BreakDetector/27/"+index+"/"+index+".total.break");
		
	}
	private String getExtractedResultPageFile(int resultPage) {
		return resultPage + ".html";
	}

	private File getExtractedResultPageFolder(int experimentId, Database website,
			String navigationTechnique, String extractionTechnique, TextQuery query) {
		
		return new File(prefix + "/" + experimentId + "/" + getIndex(website) + "/" + PAGE_EXTRACTION + "/" + navigationTechnique + "/" + extractionTechnique + "/" + asFolder(query) + "/");
		
	}

	private String getExtractedResultFile(int resultIndex) {
		return resultIndex + ".html";
	}

	private File getExtractedResultFolder(int experimentId, Database website, String navigationTechnique,
			String extractionTechnique, String resultExtraction, TextQuery query, int resultPage) {
		return new File(prefix + "/" + experimentId + "/" + getIndex(website) + "/" + RESULT_EXTRACTION + "/" + navigationTechnique + "/" +  extractionTechnique + "/" + resultExtraction + "/" + asFolder(query) + "/" + resultPage + "/");
	}

	@Override
	public boolean hasProcessedQuery(int expId, Database website,
			TextQuery texQuery, String navigationTechnique) {
		
		File folder = getRawResultPageFolder(expId,website,texQuery, navigationTechnique);
		
		Map<String,Document> aux;
		
		synchronized (rawResultsMap) {
			
			aux = rawResultsMap.get(folder.getAbsolutePath()); //if it has one, it was processed.

		}
		
		
		if (aux == null){
			
			return getBasePersister().hasProcessedQuery(expId,website.getId(),texQuery,navigationTechnique);

		}
		
		return true;
		
		
//		return new File(getRawResultPageFolder(expId, website, texQuery, navigationTechnique),getRawResultPageFile(0)).exists();
	}

	@Override
	public void saveExtractedResultPage(Database website, String extractionTechnique,
			String navigationTechnique, int experimentId, TextQuery query,
			int resultPage, Document content, boolean cached){
		
		File folder = getExtractedResultPageFolder(experimentId,website,navigationTechnique,extractionTechnique,query);
		
		File file = new File(folder,getExtractedResultPageFile(resultPage));
		
		content.setFilePath(file);
		
		if (cached){
			
			synchronized (extractedResultPage) {
				extractedResultPage.put(file.getAbsolutePath(),content);
			}
			
			getBasePersister().prepareExtractedResultPage(experimentId,website.getId(),query,extractionTechnique, navigationTechnique, resultPage);
			
		}else{
			
			folder.mkdirs();
			
			try {
				FileUtils.writeStringToFile(file, content.getContent(basePersister));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			getBasePersister().saveExtractedResultPage(experimentId,website.getId(),query,extractionTechnique, navigationTechnique, resultPage);
			
		}
		
	}

	@Override
	public void saveExtractedResult(String extractionTechnique,
			String navigationTechnique, String resultExtraction,
			int experimentId, TextQuery query, int resultPage, int resultIndex,
			Document content, boolean cached) {
		
		File folder = getExtractedResultFolder(experimentId,content.getDatabase(),navigationTechnique,extractionTechnique,resultExtraction,query,resultPage);
		
		File file = new File(folder,getExtractedResultFile(resultIndex));
		
		content.setFilePath(file);
		
		if (cached){
			
			List<Document> list;
			
			synchronized (extractedResultMap) {
				
				list = extractedResultMap.get(folder.getAbsolutePath());
				
				if (list == null){
					list = new ArrayList<Document>();
					extractedResultMap.put(folder.getAbsolutePath(), list);
					
				}

				
			}
			
			list.add(content);
			
		}else{
		
			folder.mkdirs();
			
			try {
				FileUtils.writeStringToFile(file, content.getContent(basePersister));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	public String getExtractedResultPath(Database website,
			String extractionTechnique, String navigationTechnique,
			String resultExtraction, int experimentId, TextQuery query,
			int resultPage, int resultIndex) {
		return new File(getExtractedResultFolder(experimentId, website, navigationTechnique, extractionTechnique, resultExtraction, query, resultPage),getExtractedResultFile(resultIndex)).getAbsolutePath();

	}

	@Override
	public boolean hasProcessedPage(Database website, String extractionTechnique,
			String navigationTechnique, int expId, TextQuery texQuery, int index) {
		
		File folder = getExtractedResultPageFolder(expId,website,navigationTechnique,extractionTechnique,texQuery);
		
		File file = new File(folder,getExtractedResultPageFile(index));
		
		Document aux;
		
		synchronized (extractedResultPage) {
			aux = extractedResultPage.get(file.getAbsolutePath());
		}
		
		if (aux == null)
			return getBasePersister().hasExtractedPage(expId,website.getId(),texQuery,extractionTechnique, navigationTechnique, index);
		
		return true;
//		return getExtractedPage(expId, website, navigationTechnique, extractionTechnique, texQuery, index).exists();
	}

	@Override
	public boolean hasExtractedFromPage(Database website,
			String extractionTechnique, String navigationTechnique,
			String resultExtraction, int experimentId, TextQuery query, int resultPage, int size) {
		
		File folder = getExtractedResultFolder(experimentId,website,navigationTechnique,extractionTechnique,resultExtraction,query,resultPage);
		
		List<Document> list;
		
		synchronized (extractedResultMap) {
			
			list = extractedResultMap.get(folder);

		}
		
		if (list == null){
		
			int processed = getBasePersister().getExtractedResults(experimentId,website.getId(),query,extractionTechnique, navigationTechnique, resultExtraction, resultPage);
			
			return processed == size;

		} else {
			
			return list.size() == size;
			
		}
			
//		File f = getExtractedResultFolder(experimentId,website,navigationTechnique,extractionTechnique,resultExtraction,query,resultPage);
//		
//		if (f.exists()){
//			if (f.list().length == size)
//				return true;
//		}
//		
//		return false;
		
	}

	@Override
	public List<Document> getExtracted(Database website, ResultPageDocument rs, String resultTechnique) {
		
//		might need to bring it from the database (all the required information in the rs)
		
		File folder = getExtractedResultFolder(rs.getExperimentId(),website,rs.getNavigationTechnique(),rs.getExtractionTechnique(),resultTechnique,rs.getQuery(),rs.getResultPage());
		
		
		List<Document> list;
		
		synchronized (extractedResultMap) {
			
			list = extractedResultMap.get(folder);
			
		}
		
		if (list == null){
		
			List<Document> docs = getBasePersister().getQueryResults(rs.getExperimentId(),website.getId(),rs.getQuery(),rs.getExtractionTechnique(),rs.getNavigationTechnique(),resultTechnique,rs.getResultPage());
			
			return docs;
		
		}
		
		return list;
			
//		File folder = getExtractedResultFolder(rs.getExperimentId(),website,rs.getNavigationTechnique(),rs.getExtractionTechnique(),resultTechnique,rs.getQuery(),rs.getResultPage());
//		
//		File[] files = folder.listFiles();
//		
//		Arrays.sort(files,new NameFileComparator());
//		
//		List<String> ret = new ArrayList<String>(files.length);
//		
//		for (int i = 0; i < files.length; i++) {
//			ret.add(files[i].getAbsolutePath());
//		}
//		
//		return ret;
	}

	@Override
	public Integer getNumberOfProcessedPages(int experimentId, Database website,
			TextQuery query, String navigationHandler) {
		
		File folder = getRawResultPageFolder(experimentId,website,query, navigationHandler);
		
		Map<String, Document> aux;
		
		synchronized (rawResultsMap) {
			
			aux = rawResultsMap.get(folder.getAbsolutePath());
			
		}
		
		if (aux == null)
			return getBasePersister().getProcessedPages(experimentId,website.getId(),query,navigationHandler);
		
		return aux.size();
		
//		File folder = getRawResultPageFolder(experimentId, website, query, navigationHandler);
//		
//		File[] files = folder.listFiles();
//		
//		if (files == null || files.length == 0){
//			return 0;
//		}
//		
//		return files.length;
	}

	@Override
	public File getNavigationHandlerFolder(String name) {
		return new File(prefix + "/navigationHandler/" + name + "/");
	}

	@Override
	public File getMaintenanceFolder(String name) {
		return new File(prefix + "/maintenance/" + name + "/");
	}

	@Override
	public Document getExtractedPage(int experimentId, Database website,
			String navigationTechnique, String extractionTechnique,
			TextQuery query, int resultPage) {
		
		File folder = getExtractedResultPageFolder(experimentId,website,navigationTechnique,extractionTechnique,query);
		
		File file = new File(folder,getExtractedResultPageFile(resultPage));
		
		Document aux;
		
		synchronized (extractedResultPage) {
			
			aux = extractedResultPage.get(file.getAbsolutePath());
			
		}
		
		if (aux == null){
			//it's on disk. Has been extracted. That's why we are here.
			return new Document(website, file);
		}
		return aux;
		
	}

	@Override
	public String getWebsiteEncoding(String website) {
		return "UTF-8";
	}

	@Override
	public Document getRawResultPage(int expId, Database website,
			String navigationHandler, TextQuery query, int resultPage) {
		
		File folder = getRawResultPageFolder(expId,website,query, navigationHandler);
		
		File file = new File(folder,getRawResultPageFile(resultPage));
		
		Map<String,Document> map;
		
		synchronized (rawResultsMap) {
			
			map = rawResultsMap.get(folder.getAbsolutePath());
			
		}

		if (map!=null){
			
			Document d = map.get(file.getAbsolutePath());
			
			if (d!=null)
				return d;
			
		}
		
		
		return new Document(website, file);
			
		//XXX see why returns null
	}

	@Override
	public persistentWriter getBasePersister() {
		return basePersister;
	}

	@Override
	public File getExtractionFolder(Database website,
			String relationExtractionSystem) {
		
		File f = new File(prefix,RELATION_EXTRACTION + "/" + relationExtractionSystem + "/" + getIndex(website) + "/");
		
		f.mkdirs();
		
		return f;
	}

	@Override
	public Map<Document,String> getExtractionTable(Database website, int relationExtractionSystem, ContentExtractor ce){
		
		return getBasePersister().getExtractionTable(website.getId(),relationExtractionSystem,ce);
		
	}

	@Override
	public String getName() {
		
		return "DiskBasedIP";
	
	}

	@Override
	public void saveExtractedResult(int experimentId, String extractionTechnique,
			String navigationTechnique, String resultExtraction, TextQuery query, Document document, int resultPage, int resultIndex) {
		getBasePersister().saveExtractedResult(experimentId,document, extractionTechnique,navigationTechnique,resultExtraction,query,resultPage,resultIndex);
	}

	@Override
	public void prepareExtractedResult(int experimentId,
			String extractionTechnique, String navigationTechnique,
			String rdhName, Document document, TextQuery query,
			int resultPage, int resultIndex) {
		
		getBasePersister().prepareExtractedResult(experimentId,extractionTechnique,navigationTechnique,rdhName,query,document,resultPage,resultIndex);
		
	}

	@Override
	public void saveQueryTime(int expId, int idDatabase, TextQuery texQuery, int page,
			long time) {
		
		getBasePersister().saveQueryTime(expId,idDatabase,texQuery,page,time);
		
	}

	@Override
	public void prepareQueryTime(int expId, int idDatabase, TextQuery texQuery,
			int page, long time) {
		getBasePersister().prepareQueryTime(expId,idDatabase,texQuery,page,time);		
	}
	
	@Override
	public void finishBatchDownloader(int idDatabase) {
		
		saveFiles(idDatabase);
		
		getBasePersister().finishBatchDownloader(idDatabase);
		
	}

	private void saveFiles(int idDatabase) {
		
		Collection<Map<String,Document>> pag;
		
		synchronized (rawResultsMap) {
			
			pag = rawResultsMap.values();
			
		}
		
		List<Document> pages = new ArrayList<Document>();
		
		for (Map<String,Document> list : pag) {
			pages.addAll(list.values());
		}
		
		Thread t = new Thread(new SaverRunnable(pages,basePersister));
		
		t.start();
		
		try {
			t.join();
			
			synchronized (rawResultsMap) {
				
				rawResultsMap.clear();
				
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		Thread t2;
		
		synchronized (extractedResultPage) {
			t2 = new Thread(new SaverRunnable(extractedResultPage.values(),basePersister));
		}
		
		t2.start();
		
		try {
			t2.join();
			synchronized (extractedResultPage) {
				extractedResultPage.clear();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Collection<List<Document>> aux;
		
		synchronized (extractedResultMap) {
			aux = extractedResultMap.values();
		}
				
		List<Document> d = new ArrayList<Document>();
		
		for (List<Document> list : aux) {
			d.addAll(list);
		}

		
		Thread t3 = new Thread(new SaverRunnable(d,basePersister));
		
		t3.start();
		
		try {
			t3.join();
			
			synchronized (extractedResultMap) {
				extractedResultMap.clear();
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void clearDocuments(Database database) {
		Collection<DocumentHandler> list = getBasePersister().getDocumentHandler(database);
		for (DocumentHandler documentHandler : list) {
			documentHandler.clear();
		}
	}

	@Override
	public void finishNegativeBatchDownloader(int idDatabase) {
		
		saveFiles(idDatabase);
		
		getBasePersister().finishNegativeBatchDownloader(idDatabase);
		
	}

	@Override
	public Set<Long> getProcessedTable(int expId, Database website,
			String navigationTechnique) {
		
		Set<Long> table = getProcessedTable(expId,navigationTechnique).get(website.getId());
		
		if (table == null){
			table = getBasePersister().getProcessedQueries(expId,website.getId(),navigationTechnique);
			getProcessedTable(expId,navigationTechnique).put(website.getId(),table);
		}
		
		return table;

	}

	private Map<Integer,Set<Long>> getProcessedTable(int expId, String navigationTechnique) {
		
		Map<Integer,Set<Long>> ret;
		
		synchronized (processedCachedTable) {
			ret = processedCachedTable.get(generateId(expId,navigationTechnique));
			
			if (ret == null){
				ret = new HashMap<Integer, Set<Long>>();
				processedCachedTable.put(generateId(expId,navigationTechnique),ret);
			}

			
		}
		
		return ret;
	}

	private Integer generateId(int expId, String navigationTechnique) {
		return expId; //XXX change if I change navigationTechnique;
	}

	@Override
	public Map<Long, List<Document>> getQueryResultsTable(int expId,
			Database website, String navHandler, String extractionTechnique, String resultTechnique) {
 
		Map<Long, List<Document>> table = getQueryResultsTable(expId,navHandler,extractionTechnique,resultTechnique).get(website.getId());
		
		if (table == null){
			table = getBasePersister().getQueryResultsTable(expId,website.getId(),navHandler, extractionTechnique, resultTechnique);
			getQueryResultsTable(expId,navHandler,extractionTechnique,resultTechnique).put(website.getId(),table);
		}
		
		return table;
		
	}

	private Map<Integer,Map<Long, List<Document>>> getQueryResultsTable(int expId, String navHandler, String extractionTechnique, String resultTechnique) {
		
		Map<Integer,Map<Long, List<Document>>> ret;
		
		synchronized (queryResultsTable) {
			
			ret = queryResultsTable.get(generateId(expId,navHandler,extractionTechnique,resultTechnique));
			
			if (ret == null){
				ret = new HashMap<Integer,Map<Long, List<Document>>>();
				queryResultsTable.put(generateId(expId,navHandler,extractionTechnique,resultTechnique),ret);
			}
			
		}
		
		return ret;
		
	}

	private Integer generateId(int expId, String navHandler,
			String extractionTechnique, String resultTechnique) {
		return expId; //XXX change if they change.
	}

	@Override
	public Searcher getSearcher(Database database) {
		
		synchronized (searcherTable) {
			
			Searcher searcher = searcherTable.get(database.getId());

			if (searcher == null){

				searcher = new CachedSearcher(database,getSearchRoundId(),new ClusterHeuristicNavigationHandler(getSearchRoundId()),new TreeEditDistanceBasedWrapper(), new AllHrefResultDocumentHandler(),this);

				searcherTable.put(database.getId(),searcher);

			}
				
			return searcher;
			
		}
		
		
	}

	private static int getSearchRoundId() {
		return 3;
	}
	
}
