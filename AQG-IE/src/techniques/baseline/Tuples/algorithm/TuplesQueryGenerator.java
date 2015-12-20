package techniques.baseline.Tuples.algorithm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;

import org.apache.commons.io.FileUtils;

import sample.generation.factory.InferredTypeFactory;
import sample.generation.factory.OmittedAttributeFactory;
import sample.generation.model.impl.DummySampleConfiguration;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import techniques.algorithms.Tuples;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyVersion;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;

public class TuplesQueryGenerator {

	class TuplesTGRunnable implements Runnable{

		private String[] omitAttribute;
		private String[] version;
		private persistentWriter pW;
		private Database database;
		private WorkloadModel workloadModel;
		private RelationExtractionSystem relExtSys;
		private int idRelationConfiguration;
		private int experiment;
		private Semaphore semaphore;
		private int[] seedTupleNumber;
		private Searcher ls;
		private RelationExtractionSystem tr;
		private String[] relations;
		private int idInformationExtractionSystem;

		public TuplesTGRunnable(String[] omitAttribute, String[] version, persistentWriter pW, Database database, 
				WorkloadModel workloadModel, int idRelationConfiguration, int experiment, 
				Semaphore semaphore, int[] seedTupleNumber, int idInformationExtraction, String[] relations){
			
			this.omitAttribute = omitAttribute;
			this.version = version;
			this.pW = pW;
			this.database = database;
			this.workloadModel = workloadModel;
			this.idRelationConfiguration = idRelationConfiguration;
			this.experiment = experiment;
			this.semaphore = semaphore;
			this.seedTupleNumber = seedTupleNumber;
			this.relations = relations;
			this.idInformationExtractionSystem = idInformationExtraction;
			
		}
		
		@Override
		public void run() {
			
			System.err.println("Processing: " + database.getId());
			
			RelationExtractionSystem tr = new TupleRelationExtractionSystem(pW, idRelationConfiguration, idInformationExtractionSystem,false,true);

			ContentExtractor ce = new TikaContentExtractor();
			
			Searcher ls = new OnLineSearcher(10000,"UTF-8",database,
					"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+database.getId()+".html",
					"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+database.getId()+".txt",20,3,getOnlineDocumentHandler(),getSearchRoundId(),getHtmlTagCleaner(),getInteractionPersister(pW));

			RelationExtractionSystem relExtSys = tr.createInstance(database,getInteractionPersister(pW),ce,relations);//Id, relation, interactionPersister.getExtractionTable(website,Id), interactionPersister.getExtractionFolder(website,Id));
			
			try{
			
				for (int oa = 0; oa < omitAttribute.length; oa++) {
					
					TupleQueryGenerator tqg = new TupleQueryGenerator(InferredTypeFactory.getInferredTypes(RelationConfiguration.getType(idRelationConfiguration),pW), 
							true, true, false, OmittedAttributeFactory.generateList(pW,RelationConfiguration.getType(idRelationConfiguration),omitAttribute[oa]));
	
					for (int j = 0; j < version.length; j++) {
	
						Version v = new DummyVersion(version[j]);
						
						int numbOfTups = FileUtils.readLines(pW.getInitialMatchingTuplesWithSourcesFile(database, v, workloadModel, relExtSys.getName())).size();
						
						if (numbOfTups == 0)
							continue;
						
						for (int nit = 0; nit < NUMBER_OF_INITIAL_TUPLES.length; nit++) {
							
							numbOfTups = Math.min(numbOfTups, NUMBER_OF_INITIAL_TUPLES[nit]);
														
							for (int hits_per_page = 0; hits_per_page < HITS_PER_PAGE.length; hits_per_page++) {
								
								for (int s = 0; s < seedTupleNumber.length; s++) {
			
									Sample sample = Sample.getSample(database,v,workloadModel,seedTupleNumber[s], -1,new DummySampleConfiguration(-1));
			
									new Tuples(sample, -1, -1, -1,HITS_PER_PAGE[hits_per_page],QUERY_SUBMISSION_PER_UNIT_TIME, QUERY_TIME_CONSUMED,IE_SUBMISSION_PER_UNIT_TIME, IE_TIME_CONSUMED,RET_T,seedTupleNumber[s],relExtSys,ls,numbOfTups, MAX_NUMBER_OF_QUERIES,tqg).execute(null,pW,null);
									
									Runtime.getRuntime().gc();
			
								}
			
							}
							
						}
							
					}
				}
				
				pW.reportExperimentStatus(experiment,database.getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);

				System.out.println("DONE! - " + database.getId());
				
			} catch (Exception e){
				
				e.printStackTrace();
				
				pW.reportExperimentStatus(experiment,database.getId(),pW.getComputerName(),ExperimentStatusEnum.ERROR);
				
				System.out.println("DONE WITH ERRORS - " + database.getId());
				
			}
			
			pW.makeExperimentAvailable(ExperimentEnum.QUERYING, database.getId());
			
			semaphore.release();
			
			ls.cleanSearcher();
			
		}
		
		
	}
	
	private static final long[] HITS_PER_PAGE = {1000/*,500,450,400,350,300,250,200,150,100,50,2500,5000,10000,25000,50000,100000,700000*/};
	private static final int[] NUMBER_OF_INITIAL_TUPLES = {50,40,30,20,10};
	private static final double QUERY_SUBMISSION_PER_UNIT_TIME = 4;
	private static final long QUERY_TIME_CONSUMED = 200;
	private static final double IE_SUBMISSION_PER_UNIT_TIME = 4;
	private static final long IE_TIME_CONSUMED = 5000;
	private static final long RET_T = 20;
	private static final int MAX_NUMBER_OF_QUERIES = 500;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		int group = Integer.valueOf(args[0]);

		int workload = Integer.valueOf(args[1]);
		
		int idInformationExtractionSystem = Integer.valueOf(args[2]); //20, for instance.

		int idRelationConfiguration = Integer.valueOf(args[3]); //1 for instance

		int numDbs = Integer.valueOf(args[4]);
		
		int concDbs = Integer.valueOf(args[5]);
		
		Semaphore semaphore = new Semaphore(concDbs);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int experiment = 81 + pW.getExperiment(idRelationConfiguration, RelationConfiguration.getInformationExtractionBaseIdFromTuples(idInformationExtractionSystem));
		
		String[] omitAttribute = {"SMALL_DOMAIN","LARGE_DOMAIN","NONE"};
		
		String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/}; 

		int[] seedTupleNumber = {1,2,3,4,5};

		List<Database> databases = pW.getSamplableDatabases(group);

		Collections.shuffle(databases);
		
		WorkloadModel workloadModel = pW.getWorkloadModel(workload);

		String relation = pW.getRelation(workloadModel);
		
		String[] relations = new String[]{relation};

		List<Thread> ts = new ArrayList<Thread>();
		
		for (int i = 0; i < databases.size() && numDbs > 0; i++) {
		
			Database database = databases.get(i);
			
			semaphore.acquire();
			
			System.out.println("ACQUIRED: " + semaphore.availablePermits());
			
			if (!pW.isExperimentAvailable(ExperimentEnum.QUERYING, databases.get(i).getId(),pW.getComputerName())){ //someone is querying
				semaphore.release();
				System.out.println("RELEASED: " + semaphore.availablePermits());
				continue;
			}
			
			if (!pW.isExperimentAvailable(experiment, databases.get(i).getId(),pW.getComputerName())){ //it finished (-3) or has errors (-2,-1)
				
				if (pW.isExperimentInStatus(databases.get(i), experiment, ExperimentStatusEnum.FINISHED)){ //it finished;
					pW.makeExperimentAvailable(ExperimentEnum.QUERYING, databases.get(i).getId()); //if it got here is because it was available.
					semaphore.release();
					System.out.println("RELEASED: " + semaphore.availablePermits());
					continue;
				}
				
				//OTHERWISE it's error or running (but not actually running)
				
			}
			
			numDbs--;
			
			pW.reportExperimentStatus(experiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);
						
			Thread t = new Thread(new TuplesQueryGenerator().new TuplesTGRunnable(omitAttribute,version,PersistenceImplementation.getNewWriter(),database,workloadModel,idRelationConfiguration,experiment,semaphore,seedTupleNumber,idInformationExtractionSystem,relations));
			
			ts.add(t);
			
			t.start();
			
		}

	}

	private static InteractionPersister getInteractionPersister(persistentWriter pW) {

		return new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);

	}

	private static HTMLTagCleaner getHtmlTagCleaner() {

		return new HTMLCleanerBasedCleaner();

	}

	private static OnlineDocumentHandler getOnlineDocumentHandler() {

		return new OnlineDocumentHandler(new TreeEditDistanceBasedWrapper(), new ClusterHeuristicNavigationHandler(getSearchRoundId()), new AllHrefResultDocumentHandler(),getHtmlTagCleaner());

	}

	private static int getSearchRoundId() {
		return 3;
	}



}
