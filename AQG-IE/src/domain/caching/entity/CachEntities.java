package domain.caching.entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentRetriever.DocumentRetriever;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import etxt2db.api.ClassificationExecutor;
import etxt2db.api.ClassificationModel;
import etxt2db.serialization.ClassificationModelSerializer;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.enumerations.ExperimentStatusEnum;
import exploration.model.enumerations.QueryStatusEnum;
import extraction.net.extractors.ClassificationBasedExtractor;
import extraction.net.extractors.EntityExtractor;
import extraction.net.extractors.StanfordNLPBasedExtractor;
import utils.counter.Counter;
import utils.counter.command.impl.PersistEntityCommand;
import utils.document.DocumentHandler;
import utils.id.Idhandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class CachEntities {
	
	private static boolean thereAreMore;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ClassCastException, ClassNotFoundException {
		
		int group = Integer.valueOf(args[0]); //1 to 9
		
		int entity = Integer.valueOf(args[1]);
		
		int	runningInstances = Integer.valueOf(args[2]);
		
		int numberofDatabases = Integer.valueOf(args[3]);

		boolean fromPool = Boolean.valueOf(args[4]);

		int storeAfter = Integer.valueOf(args[5]);
		
		int numberofDocuments = -1;
		int firstRes = -1;
		
		if (!fromPool){
			numberofDocuments = Integer.valueOf(args[6]);
			firstRes = Integer.valueOf(args[7]);
		}
		
		String where = "";
		
		if (args.length > 8)
			where= args[8];
		
		System.out.println("Running Instances: " + runningInstances);
		
		Map<Integer,ContentExtractor> contentExtractors = new HashMap<Integer, ContentExtractor>(runningInstances);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Counter counter = new Counter(storeAfter,new PersistEntityCommand(pW));
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		Collections.shuffle(databases);
		
		Map<Integer, EntityExtractor> extractors = new HashMap<Integer, EntityExtractor>(runningInstances);
		
		int[] entities = RelationConfiguration.getEntitiesForExp(entity);
		
		int extractor = RelationConfiguration.getExtractor(entity);
		
		for (int i = 0; i < databases.size() && numberofDatabases > 0; i++) {

			if (!pW.isExperimentAvailable(entity,databases.get(i).getId(),pW.getComputerName())){
				continue;
			}

			//so that other processes can read them
			pW.makeExperimentAvailable(entity,databases.get(i).getId());
			
//			pW.reportExperimentStatus(entity,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);
			
			System.out.println("Running");
			
			DocumentHandler dh = new DocumentHandler(databases.get(i), DocumentRetriever.experimentIndependentValue, pW, true);
			
			System.out.println("Loaded DH");
			
			Document[] files = loadDocuments(pW,databases.get(i),dh, entities, extractor, new TikaContentExtractor(),firstRes,firstRes+numberofDocuments,fromPool,entity, where);
			
			if (files.length == 0){
				pW.insertExperimentStatus(entity,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
				continue;
			}
			
			if (extractors.isEmpty()){

				for (int pp = 0; pp < runningInstances; pp++) {
					
					extractors.put(pp, RelationConfiguration.createEntityExtractor(pW,entity));
					
					contentExtractors.put(pp, new TikaContentExtractor());
					
				}
			}
			
			System.out.println("Database Processing ...");
			
			numberofDatabases--;
			
			System.out.println("Generated Docs");
			
			Map<Integer,Document[]> splits = createSplits(files,runningInstances);
			
			System.out.println("Splits done");
			
			dh.clear();

			System.gc();
			
			List<Thread> ts = new ArrayList<Thread>(runningInstances);
			
			for (int j = 0; j < runningInstances; j++) {
				
				ts.add(new CachEntities().extract(j,extractors.get(j),splits.get(j), databases.get(i), pW,contentExtractors.get(j),counter));
				
			}
		
			for (int k = 0; k < runningInstances; k++) {
				
				if (ts.get(k) != null)
					ts.get(k).join();
				
			}
			
			if (thereAreMore)
				pW.makeExperimentAvailable(entity,databases.get(i).getId());
			else
				pW.insertExperimentStatus(entity,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
			
			pW.persistEntities();
			
		}

	}

	private Thread extract(int threadId, EntityExtractor entityExtractor, Document[] files, Database database, persistentWriter pW, ContentExtractor ce, Counter counter) throws InterruptedException, IOException {
		
		Thread t = new Thread(new FullExtractRunnable(threadId,entityExtractor,files,database,pW,ce,counter));
		
		t.start();
		
		return t;
		
	}

	private static Map<Integer, Document[]> createSplits(Document[] files,
			int runningInstances) {
		
		Map<Integer, Document[]> ret = new HashMap<Integer, Document[]>();
		
		int size = (int)Math.ceil((double)files.length / (double)runningInstances);
		
		int offset = 0;
		
		for (int i = 0; i < runningInstances; i++) {
			
			ret.put(i, Arrays.copyOfRange(files, Math.min(offset, files.length), Math.min(offset+size, files.length)));
			
			offset += size;
			
		}
		
		return ret;
	}

	

	

	
	
	private static Document[] loadDocuments(persistentWriter pW, Database database, DocumentHandler dh, int[] entities, int extractor, ContentExtractor contentExtractor, int firstRes, int lastRes, boolean fromPool,int entity, String where) {
		
		Set<Long> ids;
		int numberOfDocuments;
		
		if (!fromPool){
			ids = CreateEntitySplits.loadNonProcessedDocuments(pW,database,entities,extractor,contentExtractor,firstRes,lastRes, where);
			numberOfDocuments = lastRes-firstRes;
		}else{
			ids = CreateEntitySplits.loadFromSplit(pW,database,entity);
			numberOfDocuments = ids.size();
		}
		
		thereAreMore = false;
		
		if (ids.isEmpty())
			return new Document[0];
		
		Document[] ret = new Document[Math.min(numberOfDocuments, ids.size())];

		int i = 0;
		
		for (Long id : ids) {
			
			if (i >= numberOfDocuments){
				thereAreMore = true;
				break;
			}
							
			ret[i] = dh.getDocument(id);
			
			i++;
			
		}
		
		if (fromPool)
			thereAreMore = true;
		
		return ret;
		
	}


}