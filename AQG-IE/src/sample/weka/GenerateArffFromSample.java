package sample.weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.FileHandlerUtils;
import utils.id.Idhandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;

public class GenerateArffFromSample {

	private static ArrayList<DocumentHandle> sample;
	private static ArrayList<DocumentHandle> useful;
	private static HashSet<Tuple> tuples;
	private static ArrayList<String> noFilteringFields;
	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] version = {"INDEPENDENT","DEPENDENT"}; 
		
		String[][] database = {/*{"Business","Bloomberg","randomSample"},*/{"Trip","TheCelebrityCafe","randomSample"}/*,{"Business","TheEconomist","randomSample"},{"General","UsNews","randomSample"},{"Trip","Variety","randomSample"}*/};
		
		pW = PersistenceImplementation.getWriter();
		
		int workload = 1;
		
		int sample_number = 1;

		WorkloadModel dummyWorkload = new WorkloadModel(workload,"/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/Workload.ds","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/Workload.wl","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/WorkloadQueries","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/WorkloadRelations");
		
		WordExtractorAbs usefulWE = new WordExtractor();
		
		WordExtractorAbs generalWE = new WordExtractor();
		
		for (int i = 0; i < database.length; i++) {
			
			for (int j = 0; j < version.length; j++) {
				
				args = new String[16];
				
				args[0] = pW.getDatabaseIds(database[i][1]);
				
				args[1] = pW.getUsefulFiles(database[i][1], version[j],dummyWorkload);

				Sample dummysample = null;
				
//				Sample dummysample = Sample.getSample(pW.getDatabaseByName(database[i][1]),new DummyVersion(version[j]),dummyWorkload,sample_number,new DummySampleConfiguration(1));
				
				args[2] = pW.getSampleFile(dummysample);
				
				args[3] = pW.getPrefix(database[i][0], database[i][1]);

				Idhandler idhandler = new Idhandler(dummysample.getDatabase(),pW,true);

				ArrayList<String> allUseful = FileHandlerUtils.getAllResourceNames(new File(args[1]));
				
				args[4] = pW.getArffRawModel(dummysample);
				
				args[5] = pW.getSampleTuples(dummysample);
				
				args[6] = dummyWorkload.getTuples();
				
				args[7] = dummyWorkload.getDescription();
				
				args[8] = "/home/pjbarrio/workspace/LuceneProject/stopWords.txt";
				
				args[9] = "NaturalDisaster";
				
				args[10] = pW.getTableFile(database[i][1]);
				
				args[11] = database[i][1] + "-randomSampling";
				
				args[12] = "false";
				
				args[13] = "false";
				
				args[14] = "effect";
				
				args[15] = pW.getUselessFiles(database[i][1], version[j],dummyWorkload);
				
				boolean frequency = Boolean.valueOf(args[12]);
				
				boolean stemmed = Boolean.valueOf(args[13]);
				
				ArrayList<String> allUseless = FileHandlerUtils.getAllResourceNames(new File(args[15]));
				
				loadNoFilteringFields(args[14]);
				
				loadFile(args[2],allUseful,allUseless,idhandler);
				
				loadTuples(args[5]);

				allUseful.clear();
				allUseless.clear();
				
				//WorkLoadCondition wc = new WorkLoadCondition(args[6], args[7]);
				
//				TuplesCondition wc = new TuplesCondition();
				
				SampleArffGenerator rbl = new SampleArffGenerator(args[8],usefulWE, generalWE);
				
//XXX class not used any more				rbl.learn(sample, useful, args[4], tuples, args[11], frequency, stemmed, noFilteringFields, null,false);
				
				
			}
			
		}
		
	}

	private static void loadNoFilteringFields(String string) {
		
		noFilteringFields = new ArrayList<String>();
		
		String[] fields = string.split(",");
		
		for (String string2 : fields) {
			noFilteringFields.add(string2);
		}
		
	}

	public static HashSet<Tuple> loadTuples(String tuplesFile) throws IOException {
		
		tuples = new HashSet<Tuple>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(tuplesFile)));
		
		String line = br.readLine();
		
		while (line!=null){
			
			Tuple t = TupleReader.generateTuple(line);
			
			if (!tuples.contains(t)){
			
				tuples.add(t);

			}
			line = br.readLine();
			
		}
		
		br.close();
		
		return tuples;
		
	}

	private static void loadFile(String sampleFile,ArrayList<String> allUseful,ArrayList<String> allUseless, Idhandler idhandler) throws IOException {
		
		sample = new ArrayList<DocumentHandle>();
		
		useful = new ArrayList<DocumentHandle>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(sampleFile)));
		
		String line = br.readLine();
		DocumentHandle aux;
		while (line!=null){
			
			aux = new DocumentHandle(idhandler.getDocument(Long.valueOf(line)));
						
			if (allUseful.contains(line)){
				useful.add(aux);
			}
		
			sample.add(aux);
			
			line = br.readLine();
			
		}
		
		br.close();
		
	}

}
