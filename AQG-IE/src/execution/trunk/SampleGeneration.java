package execution.trunk;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;


import sample.generation.model.SampleConfiguration;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.enumerations.ExperimentStatusEnum;

public class SampleGeneration {

	class SampleGenerationRunnable implements Runnable{

		private SampleConfiguration sc;
		private persistentWriter pW;
		private Database database;	
		private int positiveVersions;
		private int negativeVersions;
		private int firstPositiveVersion;
		private int firstNegativeVersion;
		private Semaphore semaphore;

		public SampleGenerationRunnable(Semaphore s, SampleConfiguration sc,
				persistentWriter pW, Database database, int firstPositiveVersion, int positiveVersions, int firstNegativeVersion, int negativeVersions) {
			this.semaphore = s;
			this.sc = sc;
			this.pW = pW;
			this.database = database;
			this.firstPositiveVersion = firstPositiveVersion;
			this.positiveVersions = positiveVersions;
			this.firstNegativeVersion = firstNegativeVersion;
			this.negativeVersions = negativeVersions;
		}

		@Override
		public void run() {
			
			if (sc.getSampleExecutor().samples(database)){

				persistentWriter pWInt = pW.createNewInstance(true);
				
				for (int pos = firstPositiveVersion; pos <= positiveVersions; pos++) {
					
					for (int neg = firstNegativeVersion; neg <= negativeVersions; neg++){
						
//						Sample sample = Sample.getSample(database, sc.getVersion(), sc.getWorkloadModel(), pW.getNextSampleNumber(sc.getId(),database,sc.getVersion(),sc.getWorkloadModel()),sc);

						System.err.println("Version: " + pos + " - " + neg);
						
						
						Sample sample = Sample.getSample(database, sc.getVersion(), sc.getWorkloadModel(), pos,neg,sc);
						
						int idSample = pWInt.writeSample(sample);
						
						if (idSample == -1){ //exists
							continue;
						}
						
						System.gc();
						
						sample.setId(idSample);
						
						boolean finished = sc.getSampleExecutor().generateSample(pWInt,sample, sc, pos,neg);
											
						System.out.println("DONE: " + sc.getId() + " - " + database.getId());

						saveSample(sample,pWInt);
						
						pWInt.makeAvailable(sample,finished);
						
					}
						
				}

				pW.releaseInstance(pWInt);
				
			}
			
			semaphore.release();
			
		}
		
		private void saveSample(Sample sample, persistentWriter pW) {

			pW.finishSampleGeneration(sample.getId());
			
			if (!sample.getUseful().isEmpty())
				pW.saveSampleDocuments(sample.getId(),sample.getUseful(),true);
			if (!sample.getUseless().isEmpty())
				pW.saveSampleDocuments(sample.getId(),sample.getUseless(),false);
			
			if (!sample.getTuplesTable().isEmpty())
				pW.saveSampleTuples(sample.getId(),sample.getTuplesTable());
			
		}
		
	}
	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		System.setOut(new PrintStream(new FileOutputStream("/dev/null")));
//		
		int group = Integer.valueOf(args[0]); //from 1 to 9
		
		int numberOfDbs = Integer.valueOf(args[1]);
		
		int firstpositiveVersion = Integer.valueOf(args[2]);
		
		int positiveVersions = Integer.valueOf(args[3]);

		int firstNegativeVersion = Integer.valueOf(args[4]);
		
		int negativeVersions = Integer.valueOf(args[5]);
		
		int numberOfConcurrentSamples = Integer.valueOf(args[6]);
		
		int activeValue = Integer.valueOf(args[7]);
		
		boolean avoidQueries = false;
		
		if (args.length > 8)
			avoidQueries = Boolean.valueOf(args[8]);
		
		int experimentId = activeValue;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
	
		System.err.println("Make sure everything is cached");
		
		Long qAux = null;
		
		List<SampleConfiguration> list = null;
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		Collections.shuffle(databases);
		
		for (int ind = 0; ind < databases.size() && numberOfDbs > 0; ind++) {
			
			Database database = databases.get(ind);
			
			if (numberOfDbs == 0)
				break;
			
//			int last_sample_configuration = pW.getLastSampleConfiguration(database.getId());
//			
//			pW.clean(database.getId(),last_sample_configuration);
			
			if (!pW.isExperimentAvailable(experimentId, database.getId(),pW.getComputerName())){ //someone is running the same or it finished;
				continue;
			}
			
			//load the text queries that I'll share with everyone later on (All the stuff has been cached)
			
			if (qAux == null && !avoidQueries){
				qAux = pW.getTextQuery(new TextQuery("damage"));
			}else
				System.err.println("Avoiding Query Caching...");
			numberOfDbs--;
			
			pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);
			
			Semaphore s = new Semaphore(numberOfConcurrentSamples);
			
			int proc = 0;
			
			List<Thread> ts = new ArrayList<Thread>();
			
			if (list == null){
				list = pW.getActiveSampleConfigurations(activeValue);
			}
			
			for (SampleConfiguration sc : list) {
				
				proc++;
				
				try {
					s.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				System.err.println("Generating: " + sc.getId() + " - " + proc + " - Out of " + list.size());
				
				Thread t = new Thread(new SampleGeneration().new SampleGenerationRunnable(s,sc,pW,database,firstpositiveVersion,positiveVersions,firstNegativeVersion,negativeVersions));
				
				ts.add(t);
				
				t.start();
				
			}
			
			for (int i = 0; i < ts.size(); i++) {
				try {
					ts.get(i).join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
			
			System.gc();
			
		}
		
	}
	
}
