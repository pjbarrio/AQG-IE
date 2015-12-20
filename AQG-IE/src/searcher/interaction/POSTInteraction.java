package searcher.interaction;

import java.beans.Encoder;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import online.documentRetriever.download.impl.DownloaderRunnable;

import exploration.model.Database;
import exploration.model.Document;

import searcher.interaction.formHandler.InteractionForm;
import searcher.interaction.formHandler.POSTInteractionForm;
import searcher.interaction.formHandler.TextQuery;
import utils.thread.TimedOutTask;
import utils.thread.UnderTimeOutRunnable;
import utils.thread.impl.QuerySubmitRunnable;


public class POSTInteraction extends Interaction {

	public POSTInteraction(Database website, String formFile, String encoding, List<String> inputNames) {
		super(website, formFile, encoding, inputNames);
	}

	@Override
	public Document submitQuery(TextQuery query) {

		UnderTimeOutRunnable<Document> dr = new QuerySubmitRunnable(this,query);
		
		TimedOutTask<Document> tot = new TimedOutTask<Document>(TIME_OUT,dr);
		
		return tot.execute();
		
	}

	@Override
	public Reader getNextResultsPage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected InteractionForm getInteractionFormInstance(String formFile, String encoding) {
		return new POSTInteractionForm(formFile,encoding);
	}

	@Override
	public synchronized Document _submitQuery(TextQuery query) {

		// Construct data
		String data = getInteractionForm().generateParameters(query,getInputNames());
		// Send data
		
		try {
			
			URL url;

			url = new URL(getPrefixWebsite());

			URLConnection conn = url.openConnection();

			conn.setConnectTimeout(Interaction.TIME_OUT);

			conn.setReadTimeout(Interaction.TIME_OUT);

			conn.setDoOutput(true);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

			wr.write(data);

			System.out.println(getPrefixWebsite() + data);

			wr.flush();

			return new Document(getWebsite(),new InputStreamReader(conn.getInputStream()),getPrefixWebsite() + data,true);

		} catch (MalformedURLException e) {
			return Document.empty_doc;
		} catch (IOException e) {
			return new Document(getWebsite(),new StringReader(""),getPrefixWebsite() + data,false);
		}

		


	}

}
