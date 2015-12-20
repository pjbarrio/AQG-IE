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

public class GenerateCrossableArffFromSample {

	private static ArrayList<DocumentHandle> sampleCrossable;
	private static ArrayList<DocumentHandle> usefulCrossable;
	private static HashSet<Tuple> tuples;
	private static ArrayList<String> noFilteringFields;
	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		String[] version = {"INDEPENDENT","DEPENDENT"}; 
		
//		String[][] database = {{"Business","Bloomberg","randomSample"},{"Trip","TheCelebrityCafe","randomSample"},{"Business","TheEconomist","randomSample"},{"General","UsNews","randomSample"},{"Trip","Variety","randomSample"}};

		//NO PREFIX args[3]
		
//		String[] version = {"INDEPENDENT"};
		
//		String[][] database = {{"","GlobalSample","Mixed"}};
		
		String[] version = {"DEPENDENT"};
		
		String[][] database = {{"","GlobalSample","Mixed"}};
		
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
				
				args[2] = pW.getSampleFilteredFile(dummysample);
				
//				args[3] = pW.getPrefix(database[i][0], database[i][1]);

				args[3] = "";
				
				Idhandler idhandler = new Idhandler(dummysample.getDatabase(),pW,true);

				ArrayList<String> allUseful = FileHandlerUtils.getAllResourceNames(new File(args[1]));
				
				args[4] = pW.getArffFullModel(dummysample);
			
				args[8] = pW.getStopWords();
				
				args[9] = "NaturalDisaster";
				
				args[11] = database[i][1] + "-randomSampling";
				
				args[12] = "false";
				
				args[13] = "false";
				
				args[14] = "effect";
				
				
				boolean frequency = Boolean.valueOf(args[12]);
				
				boolean stemmed = Boolean.valueOf(args[13]);
				
				loadNoFilteringFields(args[14]);
				
				loadFile(args[2],allUseful,idhandler);
			
				allUseful.clear();
				
				
				SampleArffGenerator rbl = new SampleArffGenerator(args[8],usefulWE,generalWE);
				
//XXX class not used anymore				rbl.learn(sampleCrossable, usefulCrossable, args[4], new HashSet<Tuple>(), args[11], frequency, stemmed, noFilteringFields, null,false);
				
				
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


	private static void loadFile(String sampleFile,ArrayList<String> allUseful,Idhandler idhandler) throws IOException {
		
		sampleCrossable = new ArrayList<DocumentHandle>();
		
		usefulCrossable = new ArrayList<DocumentHandle>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(sampleFile)));
		
		String line = br.readLine();
		DocumentHandle aux;
		while (line!=null){
			
			aux = new DocumentHandle(idhandler.getDocument(Long.valueOf(line)));
						
			if (allUseful.contains(line)){
				usefulCrossable.add(aux);
			}
		
			sampleCrossable.add(aux);
			
			line = br.readLine();
			
		}
		
		br.close();
		
	}

}
