package online.documentHandler.navigation.impl;

import java.io.Reader;
import java.io.StringReader;

import exploration.model.Database;
import exploration.model.Document;

import online.documentHandler.navigation.NavigationHandler;
import online.tagcleaner.HTMLTagCleaner;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;

public class DummyNavigationHandler extends NavigationHandler {

	protected DummyNavigationHandler(int experimentId,Database website,
			InteractionPersister persister, TextQuery query, HTMLTagCleaner cleaner) {
		super(experimentId,website, persister,query, cleaner);
	}

	public DummyNavigationHandler(int experimentId) {
		super(experimentId);
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public void initialize(String cleanContent) {
		;
	}

	@Override
	public Document getNext() {
		return Document.dummy_doc;
	}

	@Override
	public NavigationHandler createInstance(int experimentId,Database website,
			InteractionPersister persister, TextQuery query, HTMLTagCleaner cleaner) {
		return new DummyNavigationHandler(experimentId, website, persister, query, cleaner);
	}

	@Override
	public String getName() {
		return "DummyNav";
	}

	@Override
	public void delete() {
		;
	}

	@Override
	public boolean nextIsNew() {
		return false;
	}

}
