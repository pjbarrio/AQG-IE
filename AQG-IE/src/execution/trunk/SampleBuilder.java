package execution.trunk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.Set;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentHandler.contentLoader.impl.FileContentLoader;

import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Sample;
import exploration.model.enumerations.ExperimentStatusEnum;

import sample.generation.model.SampleBuilderParameters;
import sample.generation.utils.InstanceBasedSampleGenerationUtils;
import sample.generation.utils.SampleGenerationUtils;
import sample.weka.SampleArffGenerator;
import utils.arff.myArffHandler;
import utils.document.DocumentHandler;
import utils.document.MixedDocumentHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractorAbs;

public class SampleBuilder {

	class DocumentCacher implements Runnable {

		private Document document;
		private Semaphore sp;
		private persistentWriter pW;
		private ContentLoader cl;
		private ContentExtractor ce;

		public DocumentCacher(Document document, Semaphore sp, persistentWriter pW, ContentLoader cl, ContentExtractor ce) {
			this.document = document;
			this.sp = sp;
			this.pW = pW;
			this.cl = cl;
			this.ce = ce;
		}

		@Override
		public void run() {
			
			document.getContent(cl,pW);
			
			document.getContent(ce, pW);
			
			sp.release();
			
		}
		
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//generates all the versions :) tasw , !tasw , Full - depends on the Workload and version ...

		int group = Integer.valueOf(args[0]);

		int numbOfSamples = Integer.valueOf(args[1]);
		
		int idWorkload = Integer.valueOf(args[2]);
		
		int idVersion = Integer.valueOf(args[3]);
		
		int idExtractor = Integer.valueOf(args[4]);

		int idRelationConfiguration = Integer.valueOf(args[5]);
		
		int numDatabases = Integer.valueOf(args[6]);
		
		ContentLoader cl = new FileContentLoader();
		
		ContentExtractor ce = new TikaContentExtractor();

		persistentWriter pW = PersistenceImplementation.getWriter();

		//since it starts in 124, we have to add 111 + 13 
		int experimentId = 111 + pW.getExperiment(idRelationConfiguration, RelationConfiguration.getInformationExtractionBaseIdFromTuples(idExtractor)); 
		
		int[] sampleBuilderIds = generateArray(Arrays.copyOfRange(args, 7, args.length));

		String stopWords = pW.getStopWords();

		SampleBuilderParameters[] sp = generateSampleBuilderParameters(pW,sampleBuilderIds);

		System.err.println("Make sure only load all uniques or all not uniques! (1-16 or 17-32)");
		
		if (!sp[0].getUnique()){
			experimentId += 42;
		}
		
		List<Database> databases = pW.getSamplableDatabases(group);

		Collections.shuffle(databases);
		
		for (int db = 0; db < databases.size() && numDatabases > 0; db++) {
			
			Database database = databases.get(db);			
			
			if (!pW.isExperimentAvailable(experimentId, database.getId(),pW.getComputerName())){ //someone is running the same or it finished;
				continue;
			}

			numDatabases--;			
			
			pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);

			DocumentHandler dh;
			
			if (database.isCluster() || database.isGlobal())
				dh = new MixedDocumentHandler(database,-1,pW);
			else
				dh = new DocumentHandler(database, -1, pW,true,null);
			
//			List<Document> allDocuments = new ArrayList<Document>(dh.getDocuments());
			
			for (int j = 0; j < sp.length; j++) {

				Map<Integer,Pair<Integer,Integer>> ulessSamples = pW.getUselessSamples(idWorkload,idVersion,idExtractor,idRelationConfiguration,sp[j].getUselessDocuments(), database.getId()); 

				Map<Integer,List<Integer>> ulessRPQ = generateByRQP(ulessSamples);
				
				Map<Integer,List<Document>> uselessSamples = new HashMap<Integer, List<Document>>();
				
				Map<Integer, Sample> ulessSamp = loadSamples(ulessSamples.keySet(),pW);
				
				List<Integer> samples = pW.getNotDoneSampleIds(idWorkload,idVersion,idExtractor,idRelationConfiguration, sp[j],database.getId());

//				load all the useless samples (they are all the same for all the positive versions). (or only the ones that contain a fixed number of useless documents and treat them as different versions of the same positive sample)
//				iterate through negative samples
				
				int numberOfSamples = numbOfSamples;
				
				for (int i = 0; i < samples.size() && numberOfSamples != 0 ; i++) {

					numberOfSamples--;

					Sample sample = pW.getSample(samples.get(i));
									
					int res = pW.getSampleConfigution(sample.getSampleConfiguration().getId()).getResultsPerQuery();
					
					List<Integer> filteredUless = ulessRPQ.get(res);
					
					for (Integer uselessSample : filteredUless) {
						
						System.out.println("Cleaning...");
						
						System.gc();
						
						try {
							
							SampleGenerationUtils sgu = null;
							
							if (sp[j].getUnique() && Trued(pW,sample,sp[j],uselessSample)) //currently need special treatment for !unique
								continue;
							
							if (!learned(pW,sample,sp[j],uselessSample)){

								List<Document> uselessDocuments = uselessSamples.get(uselessSample);
								
								if (uselessDocuments == null){
									
									uselessDocuments = loadUselessSamples(ulessSamp.get(uselessSample),pW,dh,sp[j].getUselessDocuments()); //have to load the same document instance
																		 
									uselessSamples.put(uselessSample, uselessDocuments); 
								
								}
																
								//has to load the documents without loading duplicates ;)
								List<Document> usefulDocuments = loadDocuments(pW,sample,sp[j].getUsefulDocuments(),dh);//pW.getUsefulDocuments(sample, sp[j].getUsefulDocuments());

								Set<Tuple> tuples = new HashSet<Tuple>(0);
								
								if (sp[j].getTuplesAsStopWords())
									
									tuples = pW.getTuples(sample, usefulDocuments);

								
								List<Document> documents = new ArrayList<Document>(usefulDocuments);
								
								documents.addAll(uselessDocuments);
								
								cachContent(documents,pW,cl,ce);
								
								SampleArffGenerator sarffG = new SampleArffGenerator(stopWords,sp[j].getUsefulWordExtractor(),sp[j].getGeneralWordExtractor());

								sarffG.learn(ce,cl,pW, documents, usefulDocuments,pW.getArffRawModel(sample,sp[j],uselessSample),tuples,!sp[j].getUnique(),sp[j].getStemmed(),sp[j].getLowerCase(),true);

								sgu = new InstanceBasedSampleGenerationUtils(sp[j],sarffG.getInstances());
								
								sgu.removeDuplicates(sample,pW,dh,uselessSample,documents);
								
								sgu.tailorAttributes(sample,pW,uselessSample);
								
								sgu.runSVM(sample,pW,uselessSample);

								sgu.removeAttributesBasedOnSVM(sample,pW,sample.getDatabase().getId(),uselessSample);

								pW.saveBooleanModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(), pW.getArffBooleanModel(sample, sp[j], uselessSample));

								sgu.generateTrueModel(sample,pW,uselessSample);

								pW.saveTrueModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(),pW.getArffBooleanTrueModel(sample, sp[j], uselessSample));

								
							} else if (!removedDuplicates(pW,sample,sp[j],uselessSample)){
								
								List<Document> uselessDocuments = uselessSamples.get(uselessSample);
								
								if (uselessDocuments == null){
									
									uselessDocuments = loadUselessSamples(ulessSamp.get(uselessSample),pW,dh,sp[j].getUselessDocuments()); //have to load the same document instance
																		 
									uselessSamples.put(uselessSample, uselessDocuments); 
								
								}
																
								//has to load the documents without loading duplicates ;)
								List<Document> usefulDocuments = loadDocuments(pW,sample,sp[j].getUsefulDocuments(),dh);//pW.getUsefulDocuments(sample, sp[j].getUsefulDocuments());

								List<Document> documents = new ArrayList<Document>(usefulDocuments);
								
								documents.addAll(uselessDocuments);
								
								//cachContent(documents,pW,cl,ce); No ... already loaded.
								
								sgu = new InstanceBasedSampleGenerationUtils(sp[j],myArffHandler.loadInstances(pW.getArffRawModel(sample,sp[j],uselessSample)));
								
								sgu.removeDuplicates(sample,pW,dh,uselessSample, documents);
								
								sgu.tailorAttributes(sample,pW,uselessSample);
								
								sgu.runSVM(sample,pW,uselessSample);
								
								sgu.removeAttributesBasedOnSVM(sample,pW,sample.getDatabase().getId(),uselessSample);

								pW.saveBooleanModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(), pW.getArffBooleanModel(sample, sp[j], uselessSample));

								sgu.generateTrueModel(sample,pW,uselessSample);

								pW.saveTrueModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(),pW.getArffBooleanTrueModel(sample, sp[j], uselessSample));

								
							} else if (!tailored(pW,sample,sp[j],uselessSample) || !sp[j].getUnique() /*special treatment for unique*/){
								
								sgu = new InstanceBasedSampleGenerationUtils(sp[j],myArffHandler.loadInstances(pW.getArffFilteredRawModel(sample,sp[j],uselessSample)));
								
								sgu.tailorAttributes(sample,pW,uselessSample);
								
								sgu.runSVM(sample,pW,uselessSample);
								
								sgu.removeAttributesBasedOnSVM(sample,pW,sample.getDatabase().getId(),uselessSample);

								pW.saveBooleanModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(), pW.getArffBooleanModel(sample, sp[j], uselessSample));

								sgu.generateTrueModel(sample,pW,uselessSample);

								pW.saveTrueModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(),pW.getArffBooleanTrueModel(sample, sp[j], uselessSample));

								
							} else if (!SVMed(pW,sample,sp[j],uselessSample)){
								
								sgu = new InstanceBasedSampleGenerationUtils(sp[j],myArffHandler.loadInstances(pW.getArffTailoredModel(sample,sp[j],uselessSample)));
								
								sgu.runSVM(sample,pW,uselessSample);
								
								sgu.removeAttributesBasedOnSVM(sample,pW,sample.getDatabase().getId(),uselessSample);

								pW.saveBooleanModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(), pW.getArffBooleanModel(sample, sp[j], uselessSample));

								sgu.generateTrueModel(sample,pW,uselessSample);

								pW.saveTrueModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(),pW.getArffBooleanTrueModel(sample, sp[j], uselessSample));
								
							} else if (!Trued(pW,sample,sp[j],uselessSample)){
								
								sgu = new InstanceBasedSampleGenerationUtils(sp[j], myArffHandler.loadInstances(pW.getSVMedModel(sample, sp[j],uselessSample)));
								
								sgu.generateTrueModel(sample,pW,uselessSample);

								pW.saveTrueModel(sample,sp[j],uselessSample, idWorkload, idVersion, idExtractor, idRelationConfiguration, database.getId(),pW.getArffBooleanTrueModel(sample, sp[j], uselessSample));

								
							}
										
							
	//						}
	
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					
					pW.saveDoneSample(idWorkload,idVersion,idExtractor,idRelationConfiguration, sp[j],database.getId(),sample.getId());
					
				}

			}

			pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);	

		}

	}

	public static Map<Integer, List<Integer>> generateByRQP(
			Map<Integer, Pair<Integer, Integer>> ulessSamples) {
		
		Map<Integer,List<Integer>> ret = new HashMap<Integer, List<Integer>>();
		
		for (Entry<Integer,Pair<Integer,Integer>> entry : ulessSamples.entrySet()) {
			
			List<Integer> aux = ret.get(entry.getValue().getFirst());
			
			if (aux == null){
				aux = new ArrayList<Integer>();
				ret.put(entry.getValue().getFirst(), aux);
			}
			
			aux.add(entry.getKey());
			
		}
		
		return ret;
		
	}

	private static Map<Integer, Sample> loadSamples(Set<Integer> ulessSamples, persistentWriter pW) {
		
		Map<Integer,Sample> map = new HashMap<Integer, Sample>();
		
		for (Integer uselessSamp : ulessSamples) {
			
			map.put(uselessSamp, pW.getSample(uselessSamp));
			
		}
		
		return map;
		
	}

	private static void cachContent(Collection<Document> sample, persistentWriter pW, ContentLoader cl, ContentExtractor ce) {

		System.out.println("Caching content...");

		Semaphore sp = new Semaphore(50);

		List<Thread> ts = new ArrayList<Thread>(sample.size());

		for (Document document : sample) {

			try {
				sp.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if ((ts.size() % 50)==0)
				System.out.println("Status ... " + ts.size());

			Thread t = new Thread(new SampleBuilder().new DocumentCacher(document,sp,pW,cl,ce));

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

	}
	
	private static boolean Trued(persistentWriter pW, Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		return new File(pW.getArffBooleanTrueModel(sample, sampleBuilderParameters, uselessSample)).exists();
	}

	private static boolean SVMed(persistentWriter pW, Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		return new File(pW.getSMOWekaOutput(sample,sampleBuilderParameters,uselessSample)).exists();
	}

	private static boolean tailored(persistentWriter pW, Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		return new File(pW.getArffTailoredModel(sample, sampleBuilderParameters, uselessSample)).exists();
	}

	private static boolean removedDuplicates(persistentWriter pW, Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		return new File(pW.getArffFilteredRawModel(sample, sampleBuilderParameters, uselessSample)).exists();
	}

	private static boolean learned(persistentWriter pW, Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		return new File(pW.getArffRawModel(sample, sampleBuilderParameters, uselessSample)).exists();
	}

	private static List<Document> loadDocuments(persistentWriter pW,Sample sample,
			int usefulDocuments, DocumentHandler dh) {
		return pW.getSampleDocuments(sample, usefulDocuments,0,dh);
	}

	private static List<Document> loadUselessSamples(
			Sample sample, persistentWriter pW, DocumentHandler dh, int uselessDocuments) {
		
		return pW.getSampleDocuments(sample, 0,uselessDocuments,dh);
		
	}

	private static SampleBuilderParameters[] generateSampleBuilderParameters(
			persistentWriter pW, int[] sampleBuilderIds) {

		SampleBuilderParameters[] ret = new SampleBuilderParameters[sampleBuilderIds.length];

		for (int i = 0; i < sampleBuilderIds.length; i++) {

			ret[i] = pW.getSampleBuilderParameters(sampleBuilderIds[i]);

		}

		return ret;
	}

	public static int[] generateArray(String[] originalArr) {

		List<Integer> original = new ArrayList<Integer>();
		
		for (int i = 0; i < originalArr.length; i++) {
			
			if (originalArr[i].contains("-")){
				
				String[] spl = originalArr[i].split("-");
				int arr1 = Integer.valueOf(spl[0]);
				int arr2 = Integer.valueOf(spl[1]);
				
				for (int j = arr1; j <= arr2; j++) {
					original.add(j);
				}
				
			}else{
				original.add(Integer.valueOf(originalArr[i]));
			}
			
		}
		
		int[] ret = new int[original.size()];

		for (int i = 0; i < original.size(); i++) {
			ret[i] = Integer.valueOf(original.get(i));
		}

		return ret;
	}

}
