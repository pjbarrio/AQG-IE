package utils.persistence;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.resultpage.ResultPageDocument;

import searcher.Searcher;
import searcher.interaction.formHandler.TextQuery;
import utils.id.Idhandler;

public interface InteractionPersister {

	public void saveRawResultPage(int expId, Database website, TextQuery texQuery, String navigationTechnique, int page,
			Document cleanContent, boolean createBatch, boolean isNew);

	public File getWrapperFile(Database website);

	public void saveExtractedResultPage(Database website, String extractionTechnique, String navigationTechnique, int experimentId,
			TextQuery query, int resultPage, Document content, boolean cached);

	public void saveExtractedResult(String extractionTechnique, String navigationTechnique, String resultExtraction, int experimentId,
			TextQuery query, int resultPage, int resultIndex, Document document, boolean cached);

	public String getExtractedResultPath(Database website,String extractionTechnique, String navigationTechnique, String resultExtraction, int experimentId,
			TextQuery query, int resultPage, int resultIndex);

	public boolean hasProcessedQuery(int expId, Database website,
			TextQuery texQuery, String navigationTechnique);

	public boolean hasProcessedPage(Database website, String extractionTechnique, String navigationTechnique, int expId,
			TextQuery texQuery, int index);

	public boolean hasExtractedFromPage(Database website,
			String extractionTechnique, String navigationTechnique,
			String resultExtraction, int experimentId, TextQuery query, int resultPage, int size);

	public List<Document> getExtracted(Database website, ResultPageDocument rs, String resultExtraction);

	public Integer getNumberOfProcessedPages(int experimentId, Database website,
			TextQuery query, String navigationHandler);

	public File getNavigationHandlerFolder(String name);

	public File getMaintenanceFolder(String name);

	public Document getExtractedPage(int experimentId, Database website,
			String navigationTechnique, String extractionTechnique,
			TextQuery query, int resultPage);

	public String getWebsiteEncoding(String website);

	public Document getRawResultPage(int expId, Database website, String navigationHandler,
			TextQuery query, int resultPage);

	public persistentWriter getBasePersister();

	public File getExtractionFolder(Database website, String relationExtractionSystem);

	public Map<Document,String> getExtractionTable(Database website, int relationExtractionSystem, ContentExtractor ce);

	public String getName();

	public void saveExtractedResult(int experimentId, String extractionTechnique, String navigationTechnique, String resultExtraction, TextQuery query,
			Document doc, int resultPage, int resultIndex);

	public void saveQueryTime(int expId, int id, TextQuery texQuery, int page, long time);

	public void prepareExtractedResult(int experimentId,
			String extractionTechnique, String navigationTechnique,
			String rdhName, Document document, TextQuery query,
			int resultPage, int resultIndex);

	public void prepareQueryTime(int expId, int idDatabase, TextQuery texQuery, int page,
			long wallTime);

	public void finishBatchDownloader(int idDatabase);

	public void clearDocuments(Database website);

	public void finishNegativeBatchDownloader(int id);

	public Set<Long> getProcessedTable(int expId, Database website, String name);

	public Map<Long,List<Document>> getQueryResultsTable(int expId, Database website,
			String navHandler, String extractionTechnique, String resultTechnique);

	public Searcher getSearcher(Database database);
	
}
