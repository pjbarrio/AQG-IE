package domain.caching.operablestructure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gdata.util.common.base.Pair;

import opennlp.tools.util.InvalidFormatException;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.counter.Counter;
import utils.counter.command.impl.PersistOperableStructureCommand;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import domain.caching.entity.CreateEntitySplits;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceReader;
import exploration.model.Database;
import exploration.model.enumerations.ExperimentStatusEnum;

public class CachOperableStructure {

	class CandidSentReadRunnable implements Runnable{

		private Long doc;
		private String file;
		private Map<Long, Set<CandidateSentence>> ret;

		public CandidSentReadRunnable(Long doc, String file,
				Map<Long, Set<CandidateSentence>> ret) {
			
			this.doc = doc;
			this.file = file;
			this.ret = ret;
			
		}

		@Override
		public void run() {
			
			Set<CandidateSentence> aux;
			
			try {
				
				long length = new File(file).length();
				
				System.out.println("CS File size: " + length);
				
				if (length > 10485760){ //1MB
					System.out.println("ommited - " + length);
					return;
				}
				
				aux = new CandidatesSentenceReader().readCandidateSentencesInstance(file);
				
				synchronized (ret) {
					ret.put(doc, aux);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			
			
		}
		
	}
	
	private static boolean thereWereMore;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 * @throws ClassNotFoundException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InvalidFormatException, IOException, ClassNotFoundException, InterruptedException {
		
		int group = Integer.valueOf(args[0]); //1 to 9
		
		int relation = Integer.valueOf(args[1]); /*between 7 and 12 */
		
		int infEsys = Integer.valueOf(args[2]); /* 1 to 5  for the relation extractionsystem*/

		int runningInstances = Integer.valueOf(args[3]);
		
		int numberOfDatabases = Integer.valueOf(args[4]);
		
		boolean fromPool = Boolean.valueOf(args[5]);

		int storeAfter = Integer.valueOf(args[6]);
		
		int numberOfDocs = -1;
		
		if (!fromPool)
			numberOfDocs = Integer.valueOf(args[7]);
		
		String where = args[8];
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Counter counter = new Counter(storeAfter,new PersistOperableStructureCommand(pW));
		
		int informationExtractionId = RelationConfiguration.getInformationExtractionSystemId(infEsys);
		
		ContentExtractor ce = new SgmlContentExtraction();//new TikaContentExtractor();
		
		int relationConf = RelationConfiguration.getRelationConf(relation);
		
		int experiment = pW.getExperiment(relationConf,informationExtractionId);
		
		List<Integer> requiredExperiments = pW.getRequiredExperiments(experiment);
		
		Set<Integer> completedDatabases = pW.getDatabasesByStatus(requiredExperiments.get(0),ExperimentStatusEnum.FINISHED); //at least one
		
		for (int i = 1; i < requiredExperiments.size(); i++) {
			
			completedDatabases.retainAll(pW.getDatabasesByStatus(requiredExperiments.get(i),ExperimentStatusEnum.FINISHED));
			
		}		
		
		Map<Integer,StructureConfiguration> structureConfigurations = new HashMap<Integer, StructureConfiguration>(runningInstances);
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		Collections.shuffle(databases);
		
		for (int i = 0; i < databases.size() && numberOfDatabases > 0; i++) {
			
			if (!fromPool && !completedDatabases.contains(databases.get(i).getId()))
				continue;
			
			if (!pW.isExperimentAvailable(experiment,databases.get(i).getId(),pW.getComputerName())){
				continue;
			}
			
			if (fromPool)
				pW.makeExperimentAvailable(experiment, databases.get(i).getId());
			
//			pW.reportExperimentStatus(experiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);
			
			Map<Long,Set<CandidateSentence>> candidateSentences = loadCandidateSentences(pW,databases.get(i), relationConf,ce,informationExtractionId,experiment,fromPool,numberOfDocs, where);
			
			if (candidateSentences.isEmpty()){
				pW.insertExperimentStatus(experiment, databases.get(i).getId(), pW.getComputerName(), ExperimentStatusEnum.FINISHED);
				continue;
			}
			
			numberOfDatabases--;
			
			if (structureConfigurations.isEmpty()){
				for (int pp = 0; pp < runningInstances; pp++) {
					
					structureConfigurations.put(pp, RelationConfiguration.generateStructureConfiguration(infEsys));
					
				}
			}
			
			Map<Integer, Map<Long,Set<CandidateSentence>>> splits = generateSplits(candidateSentences, runningInstances);
			
			System.gc();
			
			List<Thread> ts = new ArrayList<Thread>(runningInstances);
			
			for (int j = 0; j < runningInstances; j++) {
				
				ts.add(new CachOperableStructure().generateOperableStructures(structureConfigurations.get(j),databases.get(i),splits.remove(j),relationConf,ce, informationExtractionId, pW,counter));
				
			}
		
			for (int k = 0; k < runningInstances; k++) {
				
				if (ts.get(k) != null)
					ts.get(k).join();
				
			}
			
			if (thereWereMore && !fromPool)
				pW.makeExperimentAvailable(experiment, databases.get(i).getId());
			else if (!thereWereMore && !fromPool)
				pW.reportExperimentStatus(experiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
			
			pW.persistOperableStructure();
			
			System.gc();
			
		}
		
	}

	public static Map<Long,Set<CandidateSentence>> loadCandidateSentences(
			persistentWriter pW, Database database, int relationConf,
			ContentExtractor ce, int informationExtractionSystem, int idExperiment, boolean fromPool, int numDocs, String where) throws IOException, ClassNotFoundException {
		
		Set<Long> candidateSentences;
		
		if (fromPool){
			candidateSentences = CreateEntitySplits.loadFromSplit(pW, database, idExperiment);
		}else{
			int cachedIeSys = RelationConfiguration.getCachedInformationExtractionSystemId(informationExtractionSystem);
			candidateSentences = CreateOperableStructureSplit.loadNonProcessedDocsCandidateSentences(pW, database, relationConf, ce, informationExtractionSystem,cachedIeSys,where,true,0,numDocs);
		}
		
		thereWereMore = false;
		
		if (candidateSentences.isEmpty())
			return new HashMap<Long, Set<CandidateSentence>>();
		
		Map<Long, Pair<Integer, String>> candidateSentencesMap = pW.getCandidateSentencesMap(database,relationConf,ce,candidateSentences);
		
		Map<Long,Set<CandidateSentence>> ret = new HashMap<Long, Set<CandidateSentence>>();
		
		int loaded = 0;
		
		int toLoad = candidateSentences.size();
		
		List<Thread> ts = new ArrayList<Thread>();
		
		for (Long idDoc : candidateSentences) {
			
			if (loaded % 50 == 0)
				System.out.println(loaded + " out of " + toLoad);
			
			loaded++;
			
			if (!fromPool && loaded>numDocs){
				thereWereMore = true;
				break;
			}
			
			Pair<Integer,String> val = candidateSentencesMap.get(idDoc);
			
			if (val.first == 0){
				ret.put(idDoc, new HashSet<CandidateSentence>(0));
			}else{
			
				Thread t = new Thread(new CachOperableStructure().new CandidSentReadRunnable(idDoc,val.second,ret));
				
				ts.add(t);
				
				t.start();

			}
				
		}
		
		for (Thread thread : ts) {
			
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		return ret;
	}

		
	public static <T, O> Map<Integer, Map<T, Set<O>>> generateSplits(
			Map<T, Set<O>> map, int splitsN) {
		
		Map<Integer, Map<T, Set<O>>> ret = new HashMap<Integer, Map<T, Set<O>>>();
		
		Map<Integer, Set<T>> splits = generateSplits(map.keySet(), splitsN);
		
		for (Entry<Integer,Set<T>> entry : splits.entrySet()) {
			
			ret.put(entry.getKey(), new HashMap<T, Set<O>>());
			
			for (T id : entry.getValue()) {
				
				ret.get(entry.getKey()).put(id, map.remove(id));
				
			}
			
		}
		
		return ret;
		
	}

	private static <O> Map<Integer, Set<O>> generateSplits(Set<O> set, int splits) {

		Map<Integer, Set<O>> ret = new HashMap<Integer, Set<O>>();
		
		int size = (int)Math.ceil((double)set.size() / (double)splits);
		
		List<O> list = new ArrayList<O>(set);
		
		int offset = 0;
		
		for (int i = 0; i < splits; i++) {
			
			ret.put(i, new HashSet<O>(list.subList(Math.min(offset, list.size()), Math.min(offset+size, list.size()))));
			
			offset += size;
			
		}
		
		return ret;
		
	}

	private Thread generateOperableStructures(StructureConfiguration structureConfiguration, Database database,
			Map<Long, Set<CandidateSentence>> candSents, int relationConf,
			ContentExtractor ce, int informationExtractionId, persistentWriter pW, Counter counter) {
		
		Thread t = new Thread(new GenerateOperableStructuresRunnable(pW,structureConfiguration,database,candSents,relationConf,ce,informationExtractionId,counter));

		t.start();
		
		return t;
	}

	
}
