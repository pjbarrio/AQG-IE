package utils.thread.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import exploration.model.Document;

import searcher.interaction.GETInteraction;
import searcher.interaction.Interaction;
import searcher.interaction.formHandler.InteractionForm;
import searcher.interaction.formHandler.TextQuery;
import utils.thread.UnderTimeOutRunnable;

public class QuerySubmitRunnable extends UnderTimeOutRunnable<Document> {

	private Interaction Interaction;
	private TextQuery query;

	public QuerySubmitRunnable(Interaction getInteraction,
			TextQuery query) {
		
		this.Interaction = getInteraction;
		this.query = query;
		
	}

	@Override
	protected Document execute() {
		
		return Interaction._submitQuery(query);
		
	}
	
}
