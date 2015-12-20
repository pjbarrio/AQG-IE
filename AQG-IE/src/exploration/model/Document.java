package exploration.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import exploration.model.dummy.DummyDatabase;
import exploration.model.enumerations.ContentExtractionSystemEnum;
import exploration.model.enumerations.ContentLoaderEnum;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentRetriever.DocumentRetriever;
import online.tagcleaner.HTMLTagCleaner;
import sample.generation.model.SampleBuilderParameters;
import utils.id.Idhandler;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordTokenizer;

public class Document {

	private static final String EMPTY_FILE = "/home/pjbarrio/html/empty.html";
	private static final File EMPTY_FILE_FILE = new File(EMPTY_FILE);
	
	private static final String EMPTY_URL = "http://www.cs.columbia.edu/~pjbarrio/empty.html";
	
	public static long NOT_EXISTING_ID = -1;
	public static Document empty_doc = new Document(new DummyDatabase("empty_db"),EMPTY_FILE_FILE,EMPTY_URL,"");
	public static final Document dummy_doc = new Document(new DummyDatabase("dummy_db"),NOT_EXISTING_ID);

	private int experimentId = DocumentRetriever.experimentIndependentValue;
	private boolean success = true;
	private long id;
	private Database database;
	private String content;
	private File filePath;
	private URL url;
	private long time = 0;
	private boolean hasId = false;
	private String extractedContent;
	private ContentLoaderEnum loadedWith;
	private ContentExtractionSystemEnum extractedWith;
	private String[] words;
	
	
	public Document(Database database, long id) {
		this.id = id;
		this.hasId = true;
		this.database = database;
	}

	public Document(Database database, Reader reader,
			URL url, boolean success) {
		String content;
		this.database = database;
		this.success = success;
		try {
			content = IOUtils.toString(reader);
			this.content = content;
		} catch (IOException e) {
			content = "";
			this.success = false;
		}
		
		this.url = url;
	}
	
	public Document(Database database, Reader reader, String urlString, boolean success) {
		
		this.database = database;
		this.success = success;
		try {
			this.content = IOUtils.toString(reader);
		} catch (IOException e) {
			content = "";
			this.success = false;
		}
		
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}

	public Document(Database database, String content) {
		this.database = database;
		this.content = content;
	}

	public Document(Database database, File filePath) {
		this.database = database;
		this.filePath = filePath;
	}

	public Document(Database database, URL url, String content, int experimentId, long time) {
		this(database, url, content, experimentId, time, true);
	}

	public Document(Database database, URL url, String content, int experimentId, long time,  boolean success) {
		this.database = database;
		this.url = url;
		this.content = content;
		this.experimentId = experimentId;
		this.time = time;
		this.success = success;
	}

	public Document(Database database, long idDocument, File filePath, URL url,
			int experimentId, long downloadedTime, boolean success) {
		
		this.database = database;
		this.id = idDocument;
		this.hasId = true;
		this.filePath = filePath;
		this.url = url;
		this.experimentId = experimentId;
		this.time = downloadedTime;
		this.success = success;
		
	}

	public Document(Database database, String content, File filePath, boolean success) {
		this.database = database;
		this.content = content;
		this.filePath = filePath;
		this.success = success;
	}

	

	
	public Document(Database database, File filePath, String urlString, String content) {
		this.database = database;
		this.filePath = filePath;
		this.content = content;
		try {
			this.url = new URL (urlString);
		} catch (MalformedURLException e) {
			this.url = null;
			this.success = false;
		}
	}

	public Document(Database database, long id, String content) {
		this(database,id);
		this.content = content;
	}

	public Document(Database database, String filePath, long idDocument) {
		this(database,idDocument);
		this.filePath = new File(filePath);
	}

	@Override
	public boolean equals(Object obj) {
		
		Document other = (Document)obj;
		
		if (!other.database.equals(database))
			return false;
		
		if (hasId && other.hasId)
			return id == other.id;
		
		if (experimentId != other.experimentId)
			return false;
	
		if (url != null && (url.toString().equals(other.url.toString())))
			return true;
		
		if (filePath != null && (filePath.toString().equals(other.filePath.toString())))
			return true;
		
		return false;
	}
	
	public long getId() {
		
		return id;
		
	}

	public Database getDatabase() {
		return database;
	}

	public synchronized String getContent(persistentWriter pW) {
		
		if (content == null){
			try {
				
				content = FileUtils.readFileToString(getFilePath(pW));
				
				
				//XXX can be downloaded. if it does not exist.
					
			} catch (IOException e) {
				content = ""; //does not exist
				e.printStackTrace();
			}
		}
		return content;
	}

	public synchronized String getContent(ContentExtractor contentExtractor, persistentWriter pW) {
		
		if (extractedWith != null && extractedWith == contentExtractor.getEnum()){
			return extractedContent;
		}else{
			extractedWith = contentExtractor.getEnum();
			extractedContent = contentExtractor.extractContent(getContent(pW));
			return extractedContent;
		}

	}
	
	public String getContent(ContentLoader contentLoader, persistentWriter pW) {
		
		if (loadedWith != null && loadedWith == contentLoader.getLoaderEnum()){
			return content;
		}else{
			loadedWith = contentLoader.getLoaderEnum();
			content = contentLoader.loadContent(this,pW);
			return content;

		}
		
	}

	public void setFilePath(File file) {
		this.filePath = file;		
	}

	public File getFilePath(persistentWriter pW) {
		
		if (filePath == null){
			filePath = _getFilePath(pW);
		}
		return filePath;
	}

	private File _getFilePath(persistentWriter pW){
		
		if (hasId){
			return pW.getDocumentHandler(database,experimentId).getFilePath(id);
		}else if (url != null){
			return pW.getDocumentHandler(database,experimentId).getFilePath(url);
		}
		
		throw new UnsupportedOperationException("I'm missing Id and url!!!!");
		
	}

	public void cleanContent(HTMLTagCleaner htmlTagCleaner, persistentWriter pW) {
		content = htmlTagCleaner.clean(getContent(pW));
	}

	public void setURL(URL url) {
		this.url = url;
	}

	public int getExperimentId() {
		return experimentId;
	}

	public URL getURL(persistentWriter pW) {
		
		if (url == null){
			url = _getURL(pW);
		}
		return url;
	}

	private URL _getURL(persistentWriter pW) {
		
		if (hasId){
			return pW.getDocumentHandler(database,experimentId).getURL(id);
		}else if (filePath != null){
			return pW.getDocumentHandler(database,experimentId).getURL(filePath);
		}
		
		throw new UnsupportedOperationException("I'm missing Id and filePath!!!!");
	}

	public long getDownloadTime() {
		return time;
	}

	public boolean isSuccessful() {
		return success;
	}

	public void setId(long id) {
		if (hasId){
			throw new UnsupportedOperationException("ALREADY HAS ID!!!" + url.toString());
		}
		hasId = true;
		this.id = id;
	}

	public void setExperimentId(int experimentId) {
		this.experimentId = experimentId;
	}

	public boolean hasId() {
		return hasId;
	}
	
	@Override
	public int hashCode() {
		
		return 31*new Integer(database.getId()).hashCode() + new Long(id).hashCode();
	
	}

	public String[] getWords(WordTokenizer wt, ContentExtractor contentExtractor, persistentWriter pW) {
		
		if (words != null){
			return words;
		}else{
			words = wt.getWords(this.getContent(contentExtractor, pW));
			return words;
		}
		
	}

}
