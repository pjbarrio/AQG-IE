package online.resultHandler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.database.OnlineDatabase;

import online.documentHandler.resultpage.ResultPageDocument;
import online.documentRetriever.DocumentRetriever;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.id.Idhandler;
import utils.persistence.InteractionPersister;

public abstract class ResultDocumentHandler {

	private Database website;
	protected InteractionPersister persister;
	private DocumentRetriever documentRetriever;

	protected ResultDocumentHandler(Database website, InteractionPersister persister){
		this.website = website;
		this.persister = persister;
	}

	public ResultDocumentHandler() {
		// TODO Auto-generated constructor stub
	}


	public abstract ResultDocumentHandler createInstance(Database website, InteractionPersister persister);

	public List<Document> getDocumentResults(ResultPageDocument rs) {

		if (documentRetriever == null)
			documentRetriever = new DocumentRetriever(DocumentRetriever.experimentIndependentValue,website,((OnlineDatabase)website).getWebsite(),persister.getBasePersister(),true,persister);
		
		List<Node> results = extractRecords(rs);
		
		if (hasProcessed(rs,results.size())){
			
			return persister.getExtracted(website,rs,this.getName());
		
		}
		
		List<Document> documents = new ArrayList<Document>();
			
		int resultIndex = 0;
		
		for (Node node : results) {
						
			if (documentRetriever.hasRetrieved(node)){
				
				Document doc = documentRetriever.getStoredDocument(node);
				
				documents.add(doc);
				
				persister.saveExtractedResult(rs.getExperimentId(),rs.getExtractionTechnique(),rs.getNavigationTechnique(),this.getName(),rs.getQuery(),doc,rs.getResultPage(),resultIndex);
				
			} else {
			
				Document reader = documentRetriever.getDocument(node,new File(persister.getExtractedResultPath(website,rs.getExtractionTechnique(), rs.getNavigationTechnique(),this.getName(), rs.getExperimentId(), rs.getQuery(), rs.getResultPage(), resultIndex)));
				
				persister.saveExtractedResult(rs.getExtractionTechnique(),rs.getNavigationTechnique(),this.getName(), rs.getExperimentId(), rs.getQuery(), rs.getResultPage(), resultIndex, reader,false);
						
//				Document doc = persister.getExtractedResultPath(website,rs.getExtractionTechnique(), rs.getNavigationTechnique(),this.getName(), rs.getExperimentId(), rs.getQuery(), rs.getResultPage(), resultIndex);
				
				persister.saveExtractedResult(rs.getExperimentId(),rs.getExtractionTechnique(),rs.getNavigationTechnique(),this.getName(),rs.getQuery(),reader,rs.getResultPage(),resultIndex);
				
				documents.add(reader);

			}
			
			resultIndex++;
		}
		
//		getProcessed().put(rs,documents);
				
		return documents;
		
	}

	public abstract String getName();

	protected abstract List<Node> extractRecords(ResultPageDocument rs);

	private boolean hasProcessed(ResultPageDocument rs, int size) {
		
		return persister.hasExtractedFromPage(website,rs.getExtractionTechnique(),rs.getNavigationTechnique(),this.getName(),rs.getExperimentId(),rs.getQuery(),rs.getResultPage(),size);
		
	}

	public List<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>> getDocumentURLS(ResultPageDocument rs) {
		
		List<Node> results = extractRecords(rs);
		
		if (hasProcessed(rs,results.size())){
			return new ArrayList<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>>(0);
		}
		
		List<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>> documents = new ArrayList<Pair<ResultPageDocument,Pair<Integer,Pair<Node,File>>>>();
			
		int resultIndex = 0;
		
		for (Node node : results) {
						
			File f = new File(persister.getExtractedResultPath(website,rs.getExtractionTechnique(), rs.getNavigationTechnique(),this.getName(), rs.getExperimentId(), rs.getQuery(), rs.getResultPage(), resultIndex));
			
			documents.add(new Pair<ResultPageDocument, Pair<Integer,Pair<Node,File>>>(rs, new Pair<Integer, Pair<Node,File>>(resultIndex, new Pair<Node,File>(node,f))));
			
			resultIndex++;
			
		}
		
		return documents;
		
	}

//	private Map<ResultPageDocument,List<String>> getProcessed() {
//		
//		if (processed == null){
//			processed = new HashMap<ResultPageDocument,List<String>>();
//		}
//		return processed;
//	}

}
