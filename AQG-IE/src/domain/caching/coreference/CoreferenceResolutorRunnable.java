package domain.caching.coreference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.CachCandidateSentenceRunnable;
import domain.caching.coreference.tools.CoreferenceResolutor;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPCoreference;
import edu.columbia.cs.utils.Pair;
import exploration.model.Database;

public class CoreferenceResolutorRunnable implements Runnable {

	private int threadId;
	private CoreferenceResolutor stanfordNLPCoreference;
	private File[] files;
	private Database database;
	private Map<String, Long> idsTable;
	private persistentWriter pW;
	private ContentExtractor contentExtractor;
	private Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> entitiesMap;
	private Map<Integer, String> entitiesTable;
	private int informationExractionSystem;
	private int entityType;

	public CoreferenceResolutorRunnable(
			int threadId,
			CoreferenceResolutor stanfordNLPCoreference,
			File[] files,
			Database database,
			Map<String, Long> idsTable,
			persistentWriter pW,
			ContentExtractor contentExtractor,
			Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> entitiesMap,
			Map<Integer, String> entitiesTable, int idInformationExtractionSystem, int idEntityType) {
		this.threadId = threadId;
		this.stanfordNLPCoreference = stanfordNLPCoreference;
		this.files = files;
		this.database = database;
		this.idsTable = idsTable;
		this.pW = pW;
		this.contentExtractor = contentExtractor;
		this.entitiesMap = entitiesMap;
		this.entitiesTable = entitiesTable;
		this.informationExractionSystem = idInformationExtractionSystem;
		this.entityType = idEntityType;
	}

	@Override
	public void run() {
		
		for (int j = 0; j < files.length; j++) {
			
			long idDocument = idsTable.get(files[j].getAbsolutePath());
			
			System.out.println(threadId + " - " + database.getId()+ " - " + idDocument + " : Processing: " + j + " out of " + files.length);
			
			if (!pW.hasDoneCoreferenceResolution(database.getId(),idDocument,contentExtractor,informationExractionSystem,entityType)){
				
				try {
					
					Thread t = ResolveCoreferences(database,stanfordNLPCoreference,idDocument,contentExtractor.extractContent(FileUtils.readFileToString(files[j])),pW,contentExtractor, entitiesMap.remove(idDocument),informationExractionSystem,entityType);
					
					t.join();
					
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
				
		}
		

	}

	private Thread ResolveCoreferences(Database database,
			CoreferenceResolutor stanfordNLPCoreference, long idDocument,
			String content, persistentWriter pW,
			ContentExtractor contentExtractor,
			Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> entities, int informationExractionSystem, int entityType) {
		
		Thread t = new Thread(new ResolveCoreferencesRunnable(database,stanfordNLPCoreference, idDocument,content,pW,contentExtractor, entities,entitiesTable,informationExractionSystem,entityType));
		
		t.start();
		
		return t;
		
	}

}
