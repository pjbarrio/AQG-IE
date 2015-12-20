package searcher.interaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import online.documentRetriever.download.Downloader;

import searcher.interaction.formHandler.GETInteractionForm;
import searcher.interaction.formHandler.InteractionForm;
import searcher.interaction.formHandler.TextQuery;
import utils.thread.TimedOutTask;
import utils.thread.UnderTimeOutRunnable;
import utils.thread.impl.QuerySubmitRunnable;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Query;

public class GETInteraction extends Interaction {


	public GETInteraction (Database website, String formFile,String encoding, List<String> inputNames){
		super(website,formFile, encoding, inputNames);
	}
	
	public Document submitQuery(TextQuery query){
		
		// Create query string
			
		UnderTimeOutRunnable<Document> dr = new QuerySubmitRunnable(this,query);
		
		TimedOutTask<Document> tot = new TimedOutTask<Document>(TIME_OUT,dr);
		
		return tot.execute();
		
	}
	
	public Reader getNextResultsPage(){
		return null;
	}

	@Override
	protected InteractionForm getInteractionFormInstance(String formFile,String encoding) {
		return new GETInteractionForm(formFile,encoding);
	}

	@Override
	public synchronized Document _submitQuery(TextQuery query) {
		
		URL url = null;
		
		try {
			
			String queryString = getInteractionForm().generateParameters(query,getInputNames());
			
			String prefix = getPrefixWebsite();

			String append = "?";
			
			if (prefix.contains("?"))
				append = "";
			
			String urlString = prefix + append + queryString;
			
			System.out.println(urlString);
			
			url = new URL(urlString);

			URLConnection urlConnection = url.openConnection();
			
			urlConnection.setConnectTimeout(Interaction.TIME_OUT);
			
			urlConnection.setReadTimeout(Interaction.TIME_OUT);
			
			// Read the response
			
			return new Document(getWebsite(),new InputStreamReader(urlConnection.getInputStream()),url,true);

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return Document.empty_doc;
		} catch (IOException e) {
			return new Document(getWebsite(),new StringReader(""),url,false);
		}
		
	}
	
}
