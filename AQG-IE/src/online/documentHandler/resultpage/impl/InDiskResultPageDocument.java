package online.documentHandler.resultpage.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.htmlparser.util.NodeList;

import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.resultpage.ResultPageDocument;
import online.queryResultPageHandler.QueryResultPageHandler;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;

public class InDiskResultPageDocument extends ResultPageDocument {

	private InteractionPersister persister;
	private Database website;
	private boolean loaded;
	private Document content;

	public InDiskResultPageDocument(int expId, TextQuery texQuery, int index,
			String navigationTechnique, String extractionTechnique, InteractionPersister persister, Database website) {
		
		super(expId, website, texQuery, navigationTechnique, index, extractionTechnique);
		
		this.persister = persister;
		this.website = website;
		
		loaded = false;
		
	}

	@Override
	public Document getContent() {
		
		loadDocument();
		
		return content;
		
	}

	private void loadDocument() {
		if (!loaded){
			loaded = true;
			
			content = persister.getExtractedPage(getExperimentId(),website,getNavigationTechnique(),getExtractionTechnique(),getQuery(),getResultPage());
			
		}
	}

}
