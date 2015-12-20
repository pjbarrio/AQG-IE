package online.documentHandler.resultpage;

import org.htmlparser.Node;
import org.htmlparser.util.NodeList;

import exploration.model.Database;
import exploration.model.Document;

import searcher.interaction.formHandler.TextQuery;

public abstract class ResultPageDocument {

	private int expId;
	private TextQuery query;
	private int resultPage;
	private String extractionTechnique;
	private String navigationTechnique;
	private Database database;

	public ResultPageDocument(int expId, Database database, TextQuery query, String navigationTechnique,int resultPage, String extractionTechnique){
		this.expId = expId;
		this.database = database;
		this.query = query;
		this.resultPage = resultPage;
		this.navigationTechnique = navigationTechnique;
		this.extractionTechnique = extractionTechnique;
	}
	
	public abstract Document getContent();

	protected Database getDatabase(){
		return database;
	}
	
	public int getExperimentId() {
		return expId;
	}

	public TextQuery getQuery() {
		return query;
	}

	public int getResultPage() {
		return resultPage;
	}

	public String getExtractionTechnique() {
		return extractionTechnique;
	}

	public String getNavigationTechnique() {
		return navigationTechnique;
	}

}
