package domain.caching.candidatesentence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.CandidateSentenceGenerator;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.counter.Counter;
import utils.id.Idhandler;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.model.CandidateSentence;
import exploration.model.Database;
import exploration.model.Document;

public class CandidateSentencesGeneratorRunnable implements Runnable {

	private Document[] files;
	private Database database;
	private persistentWriter pW;
	private ContentExtractor ce;
	private int threadId;
	private CandidateSentenceGenerator candidatesSentenceGenerator;
	private Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> entities;
	private Map<Integer, String> entitiesTable;
	private Counter counter;

	public CandidateSentencesGeneratorRunnable(int threadId, CandidateSentenceGenerator candidatesSentenceGenerator,
			Document[] files, Database database,
			persistentWriter pW, ContentExtractor ce,Map<Long,Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>> entities, Map<Integer,String> entitiesTable, Counter counter) {
		this.candidatesSentenceGenerator = candidatesSentenceGenerator;
		this.files = files;
		this.database = database;
		this.pW = pW;
		this.ce = ce;
		this.threadId = threadId;
		this.entities = entities;
		this.entitiesTable = entitiesTable;
		this.counter = counter;
	}

	@Override
	public void run() {
		
		for (int j = 0; j < files.length; j++) {
			
			long idDocument = files[j].getId();
			
			System.out.println(threadId + " - " + database.getId()+ " - " + idDocument + " : Processing: " + j + " out of " + files.length);
			
			try {
				
				Thread t = generateCandidateSentence(database,candidatesSentenceGenerator,idDocument,ce.extractContent(files[j].getContent(pW)),pW,ce, entities.remove(idDocument));
				
				t.join();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			counter.inform();
			
		}


	}

	private Thread generateCandidateSentence(Database database,CandidateSentenceGenerator generator, long idDocument, String content,
			persistentWriter pW, ContentExtractor ce, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>> entitiesMap) {
				
		Thread t = new Thread(new CachCandidateSentenceRunnable(database,generator, idDocument,content,pW,ce, entitiesMap,entitiesTable, new HashSet<CandidateSentence>(0)));
		
		t.start();
		
		return t;
		
	}

}
