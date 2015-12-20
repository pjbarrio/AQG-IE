package execution.trunk.test;

import java.io.File;
import java.io.IOException;
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
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.arff.myArffHandler;
import utils.document.DocumentHandler;
import utils.document.MixedDocumentHandler;
import utils.execution.MapBasedComparator;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractorAbs;

public class SampleStatistics {

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
	 * @throws IOException 
	 */
	public static void main(String[] args){

		//generates all the versions :) tasw , !tasw , Full - depends on the Workload and version ...

		int group = Integer.valueOf(args[0]);

		int numbOfSamples = Integer.valueOf(args[1]);
		
		int idWorkload = Integer.valueOf(args[2]);
		
		int idVersion = Integer.valueOf(args[3]);
		
		int idExtractor = Integer.valueOf(args[4]);

		int idRelationConfiguration = Integer.valueOf(args[5]);
		
		int numDatabases = Integer.valueOf(args[6]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();

		int[] sampleBuilderIds = generateArray(Arrays.copyOfRange(args, 7, args.length));

		SampleBuilderParameters[] sp = generateSampleBuilderParameters(pW,sampleBuilderIds);

		List<Database> databases = pW.getSamplableDatabases(group);

		Map<Integer,List<List<String>>> s = new HashMap<Integer, List<List<String>>>(databases.size());
		
		for (int db = 0; db < databases.size() && numDatabases > 0; db++) {
			
			Database database = databases.get(db);			
			
			numDatabases--;			
			
			List<List<String>> f = new ArrayList<List<String>>();
			
			for (int j = 0; j < sp.length; j++) {

				Map<Integer,Pair<Integer,Integer>> ulessSamples = pW.getUselessSamples(idWorkload,idVersion,idExtractor,idRelationConfiguration,sp[j].getUselessDocuments(), database.getId()); 

				Map<Integer,List<Integer>> ulessRPQ = generateByRQP(ulessSamples);
				
				Set<Integer> samples = pW.getDoneSamples(sp[j].getId(), database.getId(),idWorkload,idVersion,idExtractor,idRelationConfiguration);

//				load all the useless samples (they are all the same for all the positive versions). (or only the ones that contain a fixed number of useless documents and treat them as different versions of the same positive sample)
//				iterate through negative samples
				
				int numberOfSamples = numbOfSamples;
				
				for (Integer samp : samples) {
					
					if (numberOfSamples == 0)
						break;
					
					Sample sample = pW.getSample(samp);
									
					int res = pW.getSampleConfigution(sample.getSampleConfiguration().getId()).getResultsPerQuery();
					
					List<Integer> filteredUless = ulessRPQ.get(res);
					
					for (Integer uselessSample : filteredUless) {
						
						if (numberOfSamples ==0)
							break;
						
						try {
						
							SVMFeaturesLoader.loadFeatures(new File(pW.getSMOWekaOutput(sample,sp[j],uselessSample)),null);
							ArrayList<String> feats = SVMFeaturesLoader.getFeatures();
							f.add(feats);
							
							numberOfSamples--;
							System.err.println(database.getId() + " - " + feats.size());
						}catch (IOException e) {
						
							System.err.println("not done file");
						
						}
						
					}
					
				}

			}

			s.put(database.getId(), f);
			
		}

		Map<Integer,List<String>> ret = unify(s);
		
		for (Database database1 : databases) {
			
			for (Database database2 : databases) {
				
				System.out.println(database1.getId() + " - " + database2.getId() + " : " + getFeatOverlap(new ArrayList<String>(ret.get(database1.getId())), new ArrayList<String>(ret.get(database2.getId()))));
				
			}
			
		}
		
	}

	private static Map<Integer, List<String>> unify(
			Map<Integer, List<List<String>>> s) {
		
		Map<Integer, List<String>> ret = new HashMap<Integer, List<String>>(s.size());
		
		for (Entry<Integer, List<List<String>>> iterable_element : s.entrySet()) {
			
			ret.put(iterable_element.getKey(), unify(iterable_element.getValue()));
			
		}
		
		return ret;
		
	}

	private static List<String> unify(List<List<String>> value) {
		
		Map<String,Pair<Integer,Integer>> m = new HashMap<String, Pair<Integer,Integer>>();
		
		for (int i = 0; i < value.size(); i++) {
			
			for (int j = 0; j < value.get(i).size(); j++) {
				
				Pair<Integer,Integer> m1 = m.get(value.get(i).get(j));

				if (m1 == null){
					m1 = new Pair<Integer,Integer>(j,1);
				}else{
					m1 = new Pair<Integer,Integer>(j+m1.getFirst(),1+m1.getSecond());
				}
			
				m.put(value.get(i).get(j), m1);
			}
			
		}
		
		Map<String,Double> r = new HashMap<String, Double>();
		
		for (Entry<String,Pair<Integer,Integer>> ent : m.entrySet()) {
		
			r.put(ent.getKey(), (double)ent.getValue().first/(double)ent.getValue().second);
			
		}
		
		List<String> cc = new ArrayList<String>(r.keySet());
		
		Collections.sort(cc,new MapBasedComparator<String, Double>(r));
		
		return cc;
		
	}

	private static String getFeatOverlap(List<String> hashSet,
			List<String> hashSet2) {
		
		String s = "";
		
		int d = 100;
		
		for (int i = 0; i < hashSet.size()-d; i+=d) {
			
			List<String> r = hashSet.subList(i, i+d);
			r.retainAll(hashSet2);
			
			s += " - " + (double)r.size()/(double)(i+d);
			
		}
		
		return s;
		
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

			Thread t = new Thread(new SampleStatistics().new DocumentCacher(document,sp,pW,cl,ce));

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
