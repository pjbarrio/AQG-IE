package utils.document;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import utils.persistence.persistentWriter;

import exploration.model.Database;
import exploration.model.Document;

public class DocumentHandler {

	protected Database database;
	protected int experimentId;
	protected persistentWriter pW;
	private Hashtable<String, Document> URLtable;
	private Hashtable<Long, Document> Idtable;
	private Hashtable<String, Document> filePathtable;
	private long nextId;

	public DocumentHandler(Database database, int experimentId,
			persistentWriter pW, boolean readOnly, List<Long> documents) {
		this.database = database;
		this.experimentId = experimentId;
		this.pW = pW;
		if (readOnly)
			initializeReadOnly(database.getId(),experimentId,documents);
		else	
			initialize();
	}

	
	private void initializeReadOnly(int id, int experimentId, List<Long> documents) {
		Idtable = pW.getDownloadedDocumentsIdTable(database.getId(), experimentId,documents);
	}


	public DocumentHandler(Database database, int experimentId,
			persistentWriter pW) {
		this(database,experimentId,pW,false,null);
	}

	private void initialize() {
		
		URLtable = pW.getDownloadedDocumentsTable(database.getId(), experimentId);
		nextId = URLtable.size() + 1;
	}

	public boolean hasRetrieved(URL url) {
		return URLtable.containsKey(url.toString());
	}

	public Document getDocument(URL url) {
		return URLtable.get(url.toString());
	}

	public void save(Document document) {

		if (!document.hasId()){
		
			long id = createId(document);
			document.setId(id);
			URLtable.put(document.getURL(pW).toString(), document);
			getIdtable().put(id, document);
			getFilePathtable().put(document.getFilePath(pW).getAbsolutePath(), document);
		
		}
	}

	public Document getDocument(Database db, long id){
		return getDocument(id);
	}
	
	private Document getDocument(long id){
		Document d =  getIdtable().get(id);
		
		if (d == null){
			
			d = new Document(database,id,"");
			
			System.out.println("Empty document created...");
			
			getIdtable().put(id, d);
			
		}
		
		return d;
	}
	
	private synchronized long createId(Document document) {
		
		long ret = nextId;
		
		nextId++;
		
		return ret;
		
	}

	public synchronized void clear() {
		if (URLtable !=null)
			URLtable.clear();
		if (Idtable != null)
			Idtable.clear();
		if (filePathtable != null)
			filePathtable.clear();
	}

	public File getFilePath(long id) {
		return getIdtable().get(id).getFilePath(pW);
	}

	private synchronized Hashtable<Long, Document> getIdtable() {
		
		if (Idtable == null){
			
			Idtable = new Hashtable<Long, Document>();
			
			for (Document document : URLtable.values()) {
				
				Idtable.put(document.getId(), document);
				
			}
			
		}
		
		return Idtable;
	}

	public File getFilePath(URL url) {
		return URLtable.get(url.toString()).getFilePath(pW);
	}

	public URL getURL(long id) {
		return getIdtable().get(id).getURL(pW);
	}

	public URL getURL(File filePath) {
		return getFilePathtable().get(filePath.getAbsolutePath()).getURL(pW);
	}

	private synchronized Hashtable<String, Document> getFilePathtable() {
		
		if (filePathtable == null){
			filePathtable = new Hashtable<String, Document>();
			for (Document document : URLtable.values()) {
				
				filePathtable.put(document.getFilePath(pW).getAbsolutePath(), document);
				
			}
		}
		return filePathtable;
	}
	
	public Collection<Document> getDocuments(){
		if (Idtable != null){
			return Idtable.values();
		}
		return URLtable.values();
	}


	public Document getDocument(String filePath) {
		return getFilePathtable().get(filePath);
	}
	
}
