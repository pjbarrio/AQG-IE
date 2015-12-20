package domain.caching.entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import online.documentHandler.contentExtractor.ContentExtractor;
import etxt2db.api.ClassificationExecutor.ClassifiedSpan;
import exploration.model.Document;
import utils.counter.Counter;
import utils.persistence.persistentWriter;
import exploration.model.Database;
import extraction.net.extractors.EntityExtractor;

public class FullExtractRunnable implements Runnable {

	class ContentExtractorRunnable implements Runnable{

		private Map<Long,String> contents;
		private long idDoc;
		private ContentExtractor contentExtractor;
		private Document document;

		public ContentExtractorRunnable(Map<Long,String> contents, 
				ContentExtractor contentExtractor, Document document) {
			this.contents = contents;
			this.contentExtractor = contentExtractor;
			this.document = document;
		}

		@Override
		public void run() {
			
			if (document == null){
				System.out.println("null");
			}
			
			contents.put(document.getId(),contentExtractor.extractContent(document.getContent(pW)));			
		}
		
	}
	
	private static final int SIZE = 50;
	
	private EntityExtractor entityExtractor;
	private Document[] files;
	private Database database;
	private persistentWriter pW;
	private ContentExtractor ce;
	private int threadId;
	private List<ContentExtractor> ceList;

	private Counter counter;

	public FullExtractRunnable(int threadId, EntityExtractor entityExtractor,
			Document[] files, Database database,
			persistentWriter pW, ContentExtractor ce, Counter counter) {
		this.entityExtractor = entityExtractor;
		this.files = files;
		this.database = database;
		this.pW = pW;
		this.ce = ce;
		this.threadId = threadId;
		this.counter = counter;
	}

	@Override
	public void run() {
		
		Map<Long,String> contents = new HashMap<Long,String>(SIZE);
		
		int explored = 0;
		
		for (int j = 0; j < files.length; j++) {
			
			if (explored % SIZE == 0){
				
				try {
					
					System.out.println("Loading contents");
					
					loadContents(contents,files,j);
					
					System.out.println("Done with Contents");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			explored++;
			
			if (files[j] != null){
			
				long idDocument = files[j].getId();
				
				System.out.println(threadId + " - " + database.getId()+ " - " + idDocument + " : Processing: " + j + " out of " + files.length);
				
				if (entityExtractor.getTagIds().length == 1 || !pW.hasExtractedEntities(database.getId(),idDocument,entityExtractor.getTagIds(),entityExtractor.getId(),ce)){
					//we only process the docs that have not been processed yet. So, if we have only one entity, then we don't care.
					try {
						
						System.out.println("Extracting...");
						
						Thread t = extract(database,entityExtractor,idDocument,contents.remove(idDocument),pW,ce);
						
						System.out.println("Done Extracting.");
						
						t.join();
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					counter.inform();
					
				}
				
			}
		}


	}

	private void loadContents(Map<Long,String> contents, Document[] files, int initial) throws IOException {
		
		List<Thread> ts = new ArrayList<Thread>();
		
		if (ceList == null){
			ceList = new ArrayList<ContentExtractor>(SIZE);
			for (int i = 0 ; i < SIZE ; i++){
				ceList.add(ce.newInstance());
			}
		}
		
		for (int i = initial; i < files.length & i < initial + SIZE; i++) {
			
			if (files[i] != null){
			
				Thread t = new Thread(new ContentExtractorRunnable(contents,ceList.get(i-initial),files[i]));
				
				t.start();
				
				ts.add(t);

			}
			
		}
		
		int i = 0;
		
		for (Thread t : ts){
			
			if ( i % 25 == 0)
				System.out.println("Loading... " + i);
			
			i++;
			
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private Thread extract(Database database,EntityExtractor extractor, long idDocument, String content,
			persistentWriter pW, ContentExtractor ce) {
				
		Thread t = new Thread(new CachExtractRunnable(database,extractor, idDocument,content,pW,ce,new HashMap<String,List<ClassifiedSpan>>(0)));
		
		t.start();
		
		return t;
		
	}

}
