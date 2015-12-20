package domain.caching.tuple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import sample.generation.relation.GenerateSplit;
import utils.counter.Counter;
import utils.counter.command.impl.PersistOperableStructureCommand;
import utils.counter.command.impl.PersistTupleCommand;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import domain.caching.entity.CreateEntitySplits;
import domain.caching.operablestructure.CachOperableStructure;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.re.Model;
import edu.columbia.cs.ref.tool.io.CoreReader;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.enumerations.ExperimentStatusEnum;

public class CachTuple {

	class CoreReaderRunnable implements Runnable{

		private Document doc;
		private String file;
		private Map<Document, Set<OperableStructure>> ret;

		public CoreReaderRunnable(Document doc, String file,
				Map<Document, Set<OperableStructure>> ret) {
			this.doc = doc;
			this.file = file;
			this.ret = ret;
		}

		@Override
		public void run() {
			
			Set<OperableStructure> os;
			
			try {
				
				long length = new File(file).length();
				
				System.out.println("OS File size: " + length);
				
				if (length > 10485760){ //1MB
					System.out.println("ommited - " + length);
					return;
				}
				os = new CoreReader().readOperableStructuresInstance(file);
				
				synchronized (ret) {
					
					ret.put(doc,os);
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			
			
		}
		
	}
	
	private static boolean thereAreMore;

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
		
		int group = Integer.valueOf(args[0]); //1 to 9
		
		int relation = Integer.valueOf(args[1]); /*between 7 and 12 */
		
		int infEsys = Integer.valueOf(args[2]); /* 1 to 5  for the relation extractionsystem*/

		int runningInstances = Integer.valueOf(args[3]);
		
		int numDbs = Integer.valueOf(args[4]);

		int numDocs = Integer.valueOf(args[5]);
		
		int storeAfter = Integer.valueOf(args[6]);
		
		boolean askforCompletion = Boolean.valueOf(args[7]);
		
		boolean fromPool = Boolean.valueOf(args[8]);
		
		int docsToLoad = Integer.valueOf(args[9]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
				
		Counter counter = new Counter(storeAfter,new PersistTupleCommand(pW));
		
		int informationExtractionId = RelationConfiguration.getInformationExtractionSystemId(infEsys);
		
		int cachedInformationExtractionId = RelationConfiguration.getCachedInformationExtractionSystemId(infEsys);
		
		ContentExtractor ce = new SgmlContentExtraction();//new TikaContentExtractor();
		
		int relationConf = RelationConfiguration.getRelationConf(relation);
		
		String relName = RelationConfiguration.getRelationName(relation);

		int requiredExperiment = pW.getExperiment(relationConf,informationExtractionId);

		int experiment = requiredExperiment + 45; //it's tuples, not operable structure
		
		Set<Integer> completedDatabases = pW.getDatabasesByStatus(requiredExperiment,ExperimentStatusEnum.FINISHED); //at least one
		
		Map<Integer,Model> relationExtractionSystem = new HashMap<Integer, Model>(runningInstances);
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		Collections.shuffle(databases);

		for (int i = 0; i < databases.size() && numDbs > 0; i++) {
			
			if (!fromPool && askforCompletion && !completedDatabases.contains(databases.get(i).getId()))
				continue;
			
			if (!pW.isExperimentAvailable(experiment,databases.get(i).getId(),pW.getComputerName())){
				continue;
			}

			if (fromPool)
				pW.makeExperimentAvailable(experiment, databases.get(i).getId());
			
			
			Map<Document,String> opString = loadOperableSources(pW,databases.get(i), relationConf,ce,informationExtractionId, cachedInformationExtractionId,numDocs,fromPool,experiment);
			
			if (opString.isEmpty()){
				pW.insertExperimentStatus(experiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
				continue;
			}
			
			numDbs--;
			
			List<Map<Document,String>> list = split(opString, docsToLoad, fromPool, numDocs);
			
			for (int ll = 0; ll < list.size(); ll++) {
			
				Map<Document,Set<OperableStructure>> operableStructure = loadOperableStructures(list.get(ll));
				
				if (relationExtractionSystem.isEmpty()){
				
					for (int pp = 0; pp < runningInstances; pp++) {
						
						System.out.println("Generating Instance...");
						
						relationExtractionSystem.put(pp, RelationConfiguration.generateRelationExtractionSystem(pW,informationExtractionId,relationConf));
						
					}

				}
					
				pW.reportExperimentStatus(experiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);
				
				Map<Integer, Map<Document,Set<OperableStructure>>> splits = CachOperableStructure.generateSplits(operableStructure, runningInstances);
				
				List<Thread> ts = new ArrayList<Thread>(runningInstances);
				
				for (int j = 0; j < runningInstances; j++) {
					
					ts.add(new CachTuple().obtainTuples(relationExtractionSystem.get(j),databases.get(i),splits.remove(j),relationConf,ce, informationExtractionId, pW,relName,cachedInformationExtractionId, counter));
					
				}
			
				for (int k = 0; k < runningInstances; k++) {
					
					if (ts.get(k) != null)
						ts.get(k).join();
					
				}

				list.get(ll).clear();
				
				System.out.println("Garbage Collection...");
				
				System.gc();
				
			}
			
			if (thereAreMore && !fromPool)
				pW.makeExperimentAvailable(experiment,databases.get(i).getId());
			else if (!thereAreMore && !fromPool)
				pW.reportExperimentStatus(experiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
			
			pW.persistTuple();
			
		}
		

	}

	private static List<Map<Document, String>> split(
			Map<Document, String> opString, int docsToLoad, boolean fromPool,
			int numDocs) {
		
		if (fromPool){
			numDocs = opString.size();
		}
		
		int loaded = 0;
		
		List<Map<Document,String>> ret = new ArrayList<Map<Document,String>>();
		
		Map<Document,String> auxMap = null;
		
		for (Entry<Document,String> entry : opString.entrySet()) {
			
			if ((loaded % docsToLoad) == 0){
				if (auxMap!=null)
					ret.add(auxMap);
				auxMap = new HashMap<Document, String>(docsToLoad);
			}
			
			loaded++;
			
			if (loaded > numDocs){
				if (auxMap != null && !auxMap.isEmpty())
					ret.add(auxMap);
				break;
			}
			
			auxMap.put(entry.getKey(), entry.getValue());
			
		}
		
		if (auxMap != null && !auxMap.isEmpty())
			ret.add(auxMap);
		
		return ret;
		
	}

	private static Map<Document, String> loadOperableSources(persistentWriter pW, Database database, int relationConf,
			ContentExtractor ce, int informationExtractionId, int cachedInformationExtractionId,int numDocs, boolean fromPool, int idExperiment) {
		
		Map<Document,String> opStructMap = null;
		
		thereAreMore = true;
		
		if (fromPool){
			
			Set<Long> docs = CreateEntitySplits.loadFromSplit(pW, database, idExperiment);
		
			if (docs.isEmpty()){
				opStructMap = new HashMap<Document,String>(0);
				thereAreMore = false;
			}
			else{	
			opStructMap = pW.getExtractionTable(database.getId(), pW.getRelationExtractionSystemId(relationConf, informationExtractionId),ce,new ArrayList<Long>(docs));
			}
		}else{
			
			opStructMap = pW.getExtractionTable(database.getId(), pW.getRelationExtractionSystemId(relationConf, informationExtractionId),ce);
			
			Map<Document,String> already = pW.getExtractionTable(database.getId(), pW.getRelationExtractionSystemId(relationConf, cachedInformationExtractionId),ce);
			
			opStructMap.keySet().removeAll(already.keySet());
			
			if (opStructMap.size() <= numDocs)
				thereAreMore = false;
			
			already.clear();
			
		}

		return opStructMap;
		
	}

	private Thread obtainTuples(
			Model model,
			Database database, Map<Document, Set<OperableStructure>> table,
			int relationConf, ContentExtractor ce, int informationExtractionId,
			persistentWriter pW, String relation, int cachedInformationExtractionId, Counter counter) {
		
		Thread t = new Thread(new ObtainTuplesRunnable(model,database,table,ce,pW,pW.getRelationExtractionSystemId(relationConf,cachedInformationExtractionId),relation,counter));

		t.start();
		
		return t;
		
	}

	public static Map<Document, Set<OperableStructure>> loadOperableStructures(Map<Document,String> opStructMap) throws IOException, ClassNotFoundException {

		Map<Document,Set<OperableStructure>> ret = new HashMap<Document,  Set<OperableStructure>>(opStructMap.size());
		
		List<Thread> ts = new ArrayList<Thread>();
		
		int loaded = 0;
		
		int toLoad = opStructMap.size();
		
		for (Entry<Document,String> entry : opStructMap.entrySet()) {
			
			if (loaded % 20 == 0)
				System.out.println(loaded + " out of " + toLoad);
		
			loaded++;
	
			Thread t = new Thread(new CachTuple().new CoreReaderRunnable(entry.getKey(),entry.getValue(),ret));
			
			t.start();
			
			ts.add(t);
						
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

	
}
