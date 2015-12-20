package domain.caching.coreference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gdata.util.common.base.Pair;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.id.Idhandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import domain.caching.candidatesentence.CachCandidateSentencesRelationship;
import domain.caching.candidatesentence.CandidateSentencesGeneratorRunnable;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import domain.caching.coreference.tools.CoreferenceResolutor;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPCoreference;
import exploration.model.Database;
import exploration.model.enumerations.ExperimentStatusEnum;

public class CachCoreferenceResolution {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		int group = Integer.valueOf(args[0]); //1 to 9
		
		int idEntityType = Integer.valueOf(args[1]); /*between 1 and 6? */

		int idInformationExtractionSystem = Integer.valueOf(args[2]); /* for the entity */

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int idExperiment = pW.getCoreferenceExperiment(idEntityType,idInformationExtractionSystem);
		
		int runningInstances = Integer.valueOf(args[3]);

		Map<Integer,ContentExtractor> contentExtractors = new HashMap<Integer, ContentExtractor>(runningInstances);

		Map<Integer,CoreferenceResolutor> coreferenceResolutors = new HashMap<Integer, CoreferenceResolutor>(runningInstances);

		Map<Integer,String> tags = reverse(pW.getEntityTypeTable());

		Set<String> asInput = new HashSet<String>();

		asInput.add(tags.get(idEntityType));

		for (int i = 0; i < runningInstances; i++) {

			coreferenceResolutors.put(i, new CoreferenceResolutor(asInput,pW, new StanfordNLPCoreference(asInput)));

			contentExtractors.put(i, new TikaContentExtractor());

		}

		List<Database> databases = pW.getSamplableDatabases(group);

		Collections.shuffle(databases);
		
		int[][] entities = new int[][]{new int[]{idInformationExtractionSystem,idEntityType}};

		for (int i = 0; i < databases.size(); i++) {

			if (!pW.isExperimentAvailable(idExperiment,databases.get(i).getId(),pW.getComputerName())){
				continue;
			}

			pW.reportExperimentStatus(idExperiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);

			Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> entitiesMap = CachCandidateSentencesRelationship.loadEntities(pW, databases.get(i), entities); 

			Idhandler idHandler = new Idhandler(databases.get(i), pW, true);

			File[] files = CachCandidateSentencesRelationship.loadFiles(entitiesMap.keySet(),idHandler);

			Map<Integer,File[]> splits = CachCandidateSentencesRelationship.createSplits(files,runningInstances);

			Map<Integer,Map<String,Long>> idMaps = CachCandidateSentencesRelationship.createIds(splits,idHandler);

			Map<Integer,Map<Long,Map<Integer,List<Pair<Long,Pair<Integer, Integer>>>>>> entitiesSplit = CachCandidateSentencesRelationship.generateEntitySplit(entitiesMap,idMaps);

			idHandler.clear();

			System.gc();

			List<Thread> ts = new ArrayList<Thread>(runningInstances);

			for (int j = 0; j < runningInstances; j++) {

				ts.add(new CachCoreferenceResolution().generateCoreferenceEntities(j,coreferenceResolutors.get(j),splits.get(j), databases.get(i), idMaps.remove(j),pW,contentExtractors.get(j), entitiesSplit.remove(j),tags,idInformationExtractionSystem,idEntityType));

			}

			for (int k = 0; k < runningInstances; k++) {

				if (ts.get(k) != null)
					ts.get(k).join();

			}

			pW.reportExperimentStatus(idExperiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);


		}

		pW.endAlgorithm();

		
	}

	private Thread generateCoreferenceEntities(
			int threadId,
			CoreferenceResolutor stanfordNLPCoreference,
			File[] files,
			Database database,
			Map<String, Long> idsTable,
			persistentWriter pW,
			ContentExtractor contentExtractor,
			Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> entitiesMap,
			Map<Integer, String> entitiesTable,
			int idInformationExtractionSystem,
			int idEntityType) {
		
		Thread t = new Thread(new CoreferenceResolutorRunnable(threadId,stanfordNLPCoreference,files,database,idsTable,pW,contentExtractor,entitiesMap, entitiesTable, idInformationExtractionSystem,idEntityType));
		
		t.start();
		
		return t;
		
	}

	private static <A, B> Map<A, B> reverse(
			Map<B, A> table) {

		Map<A,B> ret = new HashMap<A, B>();

		for (Entry<B, A> entry : table.entrySet()) {

			ret.put(entry.getValue(), entry.getKey());

		}

		return ret;

	}

}
