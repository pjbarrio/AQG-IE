package online.documentRetriever;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import online.documentRetriever.download.Downloader;
import online.navigation.utils.NavigationUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Document;

public class DocumentRetriever {

	public static final int experimentIndependentValue = -1;
	private String website;
	private persistentWriter pW;
	private int wIndex;
	private int experimentId;
	private Map<String,Document> exploredRecently;
	private boolean createBatch;
	private Database database;

	public DocumentRetriever(int experimentId, Database website, String baseWebsite, persistentWriter pW, boolean experimentIndependent, InteractionPersister persister){
		this(experimentId,website,baseWebsite,pW,experimentIndependent,persister,false);
	}
	
	public DocumentRetriever(int experimentId, Database website, String baseWebsite, persistentWriter pW, boolean experimentIndependent, InteractionPersister persister, boolean createBatch) {
		
		if (experimentIndependent){
			this.experimentId = experimentIndependentValue;
		}
		else{
			this.experimentId = experimentId;
		}
		this.createBatch = createBatch;
		this.website = baseWebsite;
		this.wIndex = website.getId();
		this.database = website;
		this.pW = pW;
		
			
	}

	public Document getDocument(Node node, File toSave){
		
		Document toReturn = getDocument(node);
		
		toReturn.setFilePath(toSave);
		
////		StringWriter sw = new StringWriter();
//		
//		URL url = getStorableUrl(node);
//		
//		toReturn.setURL(url);
		
		try {
			
//			IOUtils.copy(toReturn, sw);
//			
//			String content = sw.toString();
			
			FileUtils.writeStringToFile(toSave, toReturn.getContent(pW));

			save(toReturn);
			
			if (createBatch){
				pW.prepareStoredDownloadedDocument(toReturn);
			}else
				pW.saveStoredDownloadedDocument(toReturn);			
			
			return toReturn;
			
		} catch (IOException e) {

			save(toReturn);
			
			if (createBatch){
				pW.prepareStoredDownloadedDocument(toReturn,false);
			}else
				pW.saveStoredDownloadedDocument(toReturn,false);
			
			
		}
		
		return toReturn;		
		
	}

	private synchronized void save(Document document) {
		pW.getDocumentHandler(database,experimentId).save(document);
	}

	public URL getStorableUrl(Node node) {
		
		TagNode tnode = (TagNode)node;
		
		if (NavigationUtils.isJavaScript(tnode)){
			
			return getStorableJavaScript(tnode);
			
		}
		
		String href = NavigationUtils.getAttribute(tnode, NavigationUtils.HREF_ATTRIBUTE);
		
		return generateURL(website,href);
		
	}

	private URL getStorableJavaScript(TagNode tnode) {
		
		return Document.empty_doc.getURL(pW);
	
	}

	private URL generateURL(String website, String href) {
		
		String toRetrieve = StringEscapeUtils.unescapeHtml3(href);
		
		if (toRetrieve.contains("\n"))
			toRetrieve = toRetrieve.replaceAll("\\n(\\s)*", "").replaceAll(" ", "+");
				
		try {
			
			return new URL(new URL(website),toRetrieve);
			
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		} 
		
		return Document.empty_doc.getURL(pW);
	}

	private Document followJavaScript(Database database, Node node) {
		return Document.empty_doc;
	}

	private Document followHrefLink(URL url) {

		String id = ClockEnum.DOWNLOAD_DOCUMENT + url.toString();
		
		Clock.startTime(id);
		
		String content = new Downloader().download(url);
		
		Clock.stopTime(id);
		
		long time = Clock.getWallTime(id);
		
		if (content == null){
			content = "";
			return new Document(database,url,content,experimentId,time,false);
		}
		
		return new Document(database,url,content,experimentId,time);
				
	}

	public boolean hasRetrieved(Node node) {
		
		return hasRetrieved(database,experimentId,getStorableUrl(node));
		
	}

	private synchronized boolean hasRetrieved(Database database, int experimentId,URL url) {
		
		return pW.getDocumentHandler(database, experimentId).hasRetrieved(url);
			
	}

	public Document getStoredDocument(Node node) {
		
		TagNode tnode = (TagNode)node;
		
		if (NavigationUtils.isJavaScript(tnode)){
			
			return getStoredDocumentJavascript(tnode);
			
		}
		
		String href = NavigationUtils.getAttribute(tnode,NavigationUtils.HREF_ATTRIBUTE);
		
		return getStoredDocumentHref(database,experimentId,generateURL(website,href));
		
	}

	private synchronized Document getStoredDocumentHref(Database database, int experimentId,URL url) {
		
		return pW.getDocumentHandler(database, experimentId).getDocument(url);
		
	}

	private Document getStoredDocumentJavascript(TagNode tnode) {
				
		return Document.empty_doc;
	
	}

	public Document getDocument(Node node) {
		
		TagNode tnode = (TagNode)node;
		
		Document toReturn = null;
		
		if (NavigationUtils.isJavaScript(tnode)){
			
			toReturn = followJavaScript(database,node);
			
		}else{
		
			String href = NavigationUtils.getAttribute(tnode,NavigationUtils.HREF_ATTRIBUTE);
			
			URL url = generateURL(website,href);
			
			toReturn = getExploredRecently().get(url.toString());
				
			if (toReturn == null){
				toReturn = followHrefLink(url);
				getExploredRecently().put(url.toString(),toReturn);
			}

		}
		
		return toReturn;
		
	}

	private Map<String,Document> getExploredRecently() {
		
		if (exploredRecently == null){
		
			exploredRecently = new HashMap<String,Document>();
		
		}
		
		return exploredRecently;
	}

	public static void main(String[] args) {
		
		try {
			new Downloader().download(new URL("http://www.carmeuse.com/page.asp?q=shift old&id=90&title=FGD Technical papers & reports"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
