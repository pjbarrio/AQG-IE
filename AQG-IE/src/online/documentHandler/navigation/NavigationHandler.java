package online.documentHandler.navigation;

import java.io.Reader;

import online.tagcleaner.HTMLTagCleaner;

import exploration.model.Database;
import exploration.model.Document;

import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;

public abstract class NavigationHandler {

	private Database website;
	private InteractionPersister persister;
	private TextQuery query;
	private int experimentId;
	private HTMLTagCleaner cleaner;

	protected NavigationHandler(int experimentId,Database website, InteractionPersister persister, TextQuery query, HTMLTagCleaner cleaner){
		this.experimentId = experimentId;
		this.website = website;
		this.persister = persister;
		this.query = query;
		this.cleaner = cleaner;
	}
	
	protected HTMLTagCleaner getCleaner() {
		return cleaner;
	}

	
	public NavigationHandler(int experimentId) {
		this.experimentId = experimentId;
	}

	public abstract boolean hasNext();

	public abstract void initialize(String cleanContent);

	public abstract Document getNext();

	public abstract NavigationHandler createInstance(int experimentId, Database website, InteractionPersister persister, TextQuery query, HTMLTagCleaner cleaner);

	public abstract String getName();

	protected TextQuery getQuery(){
		return query;
	}

	public int getExperimentId() {
		return experimentId;
	}

	public abstract void delete();
	
	protected Database getDatabase(){
		return website;
	}

	public abstract boolean nextIsNew();
	
}
