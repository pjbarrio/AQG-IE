package online.documentHandler.resultpage.impl;


import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.resultpage.ResultPageDocument;
import searcher.interaction.formHandler.TextQuery;

public class InMemoryResultDocument extends ResultPageDocument {

	private Document content;
	
	public InMemoryResultDocument(int expId, Database db, TextQuery query, int resultPage, String navigationTechnique, String extractionTechnique,Document content) {
		super(expId,db, query,navigationTechnique,resultPage,extractionTechnique);
		this.content = content;
	}

	@Override
	public Document getContent() {
		return content;
	}


}
