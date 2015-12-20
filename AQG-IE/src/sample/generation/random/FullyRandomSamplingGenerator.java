package sample.generation.random;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import sample.AttributeTailoring.In_FrequentRemovalFilter;
import sample.generation.SamplingGenerationThread;
import sample.weka.SampleArffGenerator;
import utils.FileHandlerUtils;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.arff.myArffHandler;
import utils.id.Idhandler;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.SubsetByExpression;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;

public class FullyRandomSamplingGenerator {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String[][] database = {{"Bloomberg","randomSample","600","215","Business"},{"TheCelebrityCafe","randomSample","500","100","Trip"},{"TheEconomist","randomSample","650","240","Business"},{"UsNews","randomSample","600","215","General"},{"Variety","randomSample","585","208","Trip"}};
		
		String[] version = {/*"INDEPENDENT",*/"DEPENDENT"};
		
		int[] sample_number = {1/*,2,3,4,5*/};
		
		int[] workload = {1/*,2,3,4,5*/};
		
		args = new String[8];
		
		int thread = 0;
		
		WordExtractorAbs usefulWE = new WordExtractor();
		
		WordExtractorAbs generalWE = new WordExtractor();
		
		for (int w = 0; w < workload.length; w++) {
			
			for (int i = 0; i < database.length; i++) {
				
				for (int j = 0; j < version.length; j++) {
					
					for (int k = 0; k < sample_number.length; k++) {
						
						startSampleGeneration(workload[w],database[i][0],version[j],sample_number[k],database[i][2],database[i][3],database[i][4], new databaseWriter("/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/"), thread,usefulWE,generalWE);
						
						thread++;
					}
					
				}
				
			}
			
		}
		
	}

	private static void startSampleGeneration(int workload, String databaseName, String version, int sample_number, String useless, String useful, String type, persistentWriter pW, int thread, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) throws Exception {
		
		new Thread(new SamplingGenerationThread(workload,databaseName,version,sample_number,useless,useful,type,pW, thread,usefulWE,generalWE)).start();
		
	}

	

}
