package sample.weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.id.Idhandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import execution.workload.tuple.Tuple;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;

public class GenerateArffFromFile {

	private static HashSet<Tuple> tuples;
	private static ArrayList<DocumentHandle> sample;
	private static ArrayList<DocumentHandle> useful;
	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] version = {"INDEPENDENT","DEPENDENT"};
		
		String[] combined = {"GlobalSample","Mixed"};
		
		pW = PersistenceImplementation.getWriter();
		
		int workload = 1;
		
		int[] version_seed_pos = {2,3,4,5};
		
		int[] version_seed_neg = {1,2,3,4,5};
		
		WordExtractorAbs usefulWE = new WordExtractor();
		
		WordExtractorAbs generalWE = new WordExtractor();
		
		for (int k = 0; k < version_seed_pos.length; k++) {
			
			for (int l = 0; l < version_seed_neg.length; l++) {
				
				for (int i = 0; i < version.length; i++) {
					
					String stopWords = pW.getStopWords();
					
					boolean frequency = false;
					
					boolean stemmed = false;
					
					ArrayList<String> noFilteringFields = new ArrayList<String>();
					
					noFilteringFields.add("effect");
					
					String relation = "NaturalDisaster";
					
					Sample dummysample = Sample.getSample(pW.getDatabaseByName(combined[0]),new DummyVersion(version[i]),new DummyWorkload(workload),version_seed_pos[k],version_seed_neg[l],new DummySampleConfiguration(1));
					
					String output = pW.getArffBooleanModel(dummysample);
					
					String tuplesFile = pW.getSampleTuples(dummysample);
					
					tuples = GenerateArffFromSample.loadTuples(tuplesFile);
					
					String idFiles = pW.getSampleFilteredFile(dummysample);
					
					String idFile = pW.getDatabaseIds(combined[0]) + "." + version[i] + "." + version_seed_pos[k] + "." + version_seed_neg[l];
					
					Idhandler idHandler = new Idhandler(dummysample.getDatabase(),pW,true);
					
					sample = createDocumentHandles(idFiles,idHandler);
					
					String usefulFile = pW.getSampleUsefulDocuments(dummysample);
									
					useful = createDocumentHandles(usefulFile,idHandler);
					
					SampleArffGenerator rbl = new SampleArffGenerator(stopWords,usefulWE, generalWE);
					
	//XXX class not used anymore				rbl.learn(sample, useful, output, tuples, "mixingSample", frequency, stemmed, noFilteringFields, null,false);
	
				}

		}
				
		}
		
		
		

	}

	private static ArrayList<DocumentHandle> createDocumentHandles(
			String idFiles, Idhandler idHandler) throws IOException {
		
		ArrayList<DocumentHandle> ret = new ArrayList<DocumentHandle>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(idFiles)));
		
		String line = br.readLine();
		
		while (line != null){
			
			ret.add(new DocumentHandle(idHandler.getDocument(Long.valueOf(line)+1))); //Remove +1
			
			line = br.readLine();
		}
		
		br.close();
		
		return ret;
		
	}
	
}
