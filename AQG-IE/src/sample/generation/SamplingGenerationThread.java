package sample.generation;

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
import sample.generation.model.impl.DummySampleConfiguration;
import sample.generation.utils.SampleGenerationUtils;
import sample.weka.SampleArffGenerator;
import utils.FileHandlerUtils;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.arff.myArffHandler;
import utils.id.Idhandler;
import utils.id.TuplesLoader;
import utils.persistence.persistentWriter;
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

public class SamplingGenerationThread implements Runnable {

	private ArrayList<DocumentHandle> sampleCrossable;

	private ArrayList<DocumentHandle> usefulCrossable;
	
	private ArrayList<DocumentHandle> sampleDocs;
	private ArrayList<DocumentHandle> usefulDocs;
	private HashSet<Integer> alreadyChosen;
	private Hashtable<Long, ArrayList<String>> docTuplesTable;
	private ArrayList<String> noFilteringFields;
	private HashSet<Tuple> tuples;
	private int workload;
	private String databaseName;
	private String version;
	private int sample_number;
	private String useless;
	private String useful;
	private String type;
	private persistentWriter pW;
	private int id;
	private WordExtractorAbs generalWE;
	private WordExtractorAbs usefulWE;
	
	public SamplingGenerationThread(int workload, String databaseName,
			String version, int sample_number, String useless, String useful,
			String type, persistentWriter pW, int id, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) {
		
		this.workload = workload;
		this.databaseName = databaseName;
		this.version = version;
		this.sample_number = sample_number;
		this.useless = useless;
		this.useful = useful;
		this.type = type;
		this.pW = pW;
		this.id = id;
		this.usefulWE = usefulWE;
		this.generalWE = generalWE;
		
	}

	@Override
	public void run() {
		
		try {
		
			WorkloadModel dummyWorkload = new DummyWorkload(workload);
			
			String[] args = new String[8];
			
			args[0] = pW.getUsefulFiles(databaseName, version , dummyWorkload); 
	
			args[1] = pW.getUselessFiles(databaseName, version , dummyWorkload);
	
			Sample sample = null;
			
//			Sample sample = Sample.getSample(pW.getDatabaseByName(databaseName), new DummyVersion(version),dummyWorkload,sample_number,new DummySampleConfiguration(1));
			
			args[2] = pW.getSampleFile(sample);
			
			args[3] = pW.getSampleTuples(sample);
			
			args[4] = useless; //useless
			
			args[5] = useful; //useful
			
//XXX class not used anymore			args[6] = pW.getMatchingTuplesWithSourcesFile(databaseName, version,dummyWorkload);
			
			args[7] = pW.getSampleUsefulDocuments(sample);
			
			generateRawSample(args,databaseName);
					
			args = new String[17];
			
			args[0] = pW.getDatabaseIds(databaseName);
			
			args[1] = pW.getUsefulFiles(databaseName, version,dummyWorkload);
	
			args[2] = pW.getSampleFile(sample);
			
			args[3] = pW.getPrefix(type, databaseName);
	
			args[4] = pW.getArffRawModel(sample);
			
			args[5] = pW.getSampleTuples(sample);
			
			args[6] = dummyWorkload.getTuples();
			
			args[7] = dummyWorkload.getDescription();
			
			args[8] = pW.getStopWords();
			
			args[9] = "NaturalDisaster";
			
			args[10] = pW.getTableFile(databaseName);
			
			args[11] = databaseName + "-randomSampling";
			
			args[12] = "false";
			
			args[13] = "false";
			
			args[14] = "effect";
			
			args[15] = pW.getUselessFiles(databaseName, version,dummyWorkload);
			
			args[16] = pW.getArffFullModel(sample);
			
			generateArffRawModel(sample, args,usefulWE,generalWE);
			
			args = new String[2];
			
			args[0] = pW.getDatabaseIds(databaseName);
			
			args[1] = pW.getPrefix(type, databaseName);
			
//			new SampleGenerationUtils().removeDuplicates(sample,pW,id);
			
			args = new String[16];
			
			args[0] = pW.getDatabaseIds(databaseName);
			
			args[1] = pW.getUsefulFiles(databaseName, version,dummyWorkload);
	
			args[2] = pW.getSampleFilteredFile(sample);
			
			args[3] = pW.getPrefix(type,databaseName);
	
			args[4] = pW.getArffFullModel(sample);
		
			args[8] = pW.getStopWords();
			
			args[9] = "NaturalDisaster";
			
			args[11] = databaseName + "-randomSampling";
			
			args[12] = "false";
			
			args[13] = "false";
			
			args[14] = "effect";
			
			generateFullArffSample(sample, args,usefulWE,generalWE);
			
			args = new String[4];
			
			args[2] = "0.003"; //%min
			
			args[3] = "0.9"; //%max
			
//			new SampleGenerationUtils().tailorAttributes(sample,args,pW);
			
			args = new String[2];
			
//			new SampleGenerationUtils().runSVM(sample,args,pW);
			
			args = new String[4];
			
			args[1] = "700";
			
//			new SampleGenerationUtils().removeAttributesBasedOnSVM(sample,args,pW,id);
			
			new SampleGenerationUtils(null).generateTrueModel(sample,pW);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void generateFullArffSample(Sample sample, String[] args, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) throws IOException {
		
		Idhandler idhandler = new Idhandler(sample.getDatabase(),pW,true);

		ArrayList<String> allUseful = FileHandlerUtils.getAllResourceNames(new File(args[1]));

		boolean frequency = Boolean.valueOf(args[12]);
		
		boolean stemmed = Boolean.valueOf(args[13]);
		
		loadNoFilteringFields(args[14]);
		
		loadFile(args[2],allUseful,idhandler);
	
		allUseful.clear();
				
		SampleArffGenerator rbl = new SampleArffGenerator(args[8],usefulWE, generalWE);
		
//		rbl.learn(sampleCrossable, usefulCrossable, args[4], new HashSet<Tuple>(), args[11], frequency, stemmed, noFilteringFields, null,false);
			
	}

	private void loadFile(String sampleFile,ArrayList<String> allUseful,Idhandler idhandler) throws IOException {
		
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
	
	





	private void generateArffRawModel(Sample sample, String[] args, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) throws IOException {
		
		Idhandler idhandler = new Idhandler(sample.getDatabase(),pW,true);

		ArrayList<String> allUseful = FileHandlerUtils.getAllResourceNames(new File(args[1]));
		
		boolean frequency = Boolean.valueOf(args[12]);
		
		boolean stemmed = Boolean.valueOf(args[13]);
		
		ArrayList<String> allUseless = FileHandlerUtils.getAllResourceNames(new File(args[15]));
		
		loadNoFilteringFields(args[14]);
		
		loadFile(args[2],allUseful,allUseless,idhandler);
		
		loadTuples(args[5]);

		allUseful.clear();
		allUseless.clear();
		
		SampleArffGenerator rbl = new SampleArffGenerator(args[8],usefulWE, generalWE);
		
//		rbl.learn(sampleDocs, usefulDocs, args[4], tuples, args[11], frequency, stemmed, noFilteringFields, null,false);
		
	}

	private void loadFile(String sampleFile,ArrayList<String> allUseful,ArrayList<String> allUseless, Idhandler idhandler) throws IOException {
		
		sampleDocs = new ArrayList<DocumentHandle>();
		
		usefulDocs = new ArrayList<DocumentHandle>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(sampleFile)));
		
		String line = br.readLine();
		DocumentHandle aux;
		while (line!=null){
			
			aux = new DocumentHandle(idhandler.getDocument(Long.valueOf(line)));
						
			if (allUseful.contains(line)){
				usefulDocs.add(aux);
			}
		
			sampleDocs.add(aux);
			
			line = br.readLine();
			
		}
		
		br.close();
		
	}
	
	public HashSet<Tuple> loadTuples(String tuplesFile) throws IOException {
		
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
	
	private void loadNoFilteringFields(String string) {
		
		noFilteringFields = new ArrayList<String>();
		
		String[] fields = string.split(",");
		
		for (String string2 : fields) {
			noFilteringFields.add(string2);
		}
		
	}
	
	

	private void generateRawSample(String[] args, String database) throws IOException {
		
		docTuplesTable = TuplesLoader.loadIdtuplesTable(args[6]);
		
		alreadyChosen = new HashSet<Integer>();
		
		ArrayList<String> usefulFiles = FileHandlerUtils.getAllResourceNames(new File(args[0]));
		
		System.out.println(id + "--->USEFUL: " + args[0]);
		
		ArrayList<String> uselessFiles = FileHandlerUtils.getAllResourceNames(new File(args[1]));
		
		System.out.println(id + "--->USELESS: " + args[1]);
		
		long useless = Long.valueOf(args[4]);
		
		long useful = Math.min(Long.valueOf(args[5]), usefulFiles.size());
		
		ArrayList<String> UselessSample = generateSample(useless,uselessFiles);

		alreadyChosen.clear();
		
		ArrayList<String> UsefulSample = generateSample(useful,usefulFiles);
		
		writeTuplesFile(UsefulSample,args[3],database);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(args[2]));
		
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(args[7]));
		
		for (String string : UsefulSample) {
			
			bw.write(string + "\n");
			
			bw2.write(string + "\n");
			
		}
		
		for (String string : UselessSample) {
			
			bw.write(string + "\n");
			
		}
		
		bw2.close();
		
		bw.close();
		
	}

	private void writeTuplesFile(ArrayList<String> usefulSample, String output, String database) throws IOException {
			
		ArrayList<String> print = getMatchingTuples(usefulSample,database);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		for (String tuple : print) {
			
			bw.write(tuple + "\n");
			
		}
		
		bw.close();
		
	}

	private ArrayList<String> getMatchingTuples(ArrayList<String> useful, String database) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		long i = 1;
		
		for (String usefulDoc : useful) {
			
			System.out.println(id + "--->" + i++ + " Processing: " + usefulDoc + " Outof: " + useful.size());
			
			ret.addAll(getMatchingTuples(usefulDoc,database));
			
		}
		
		return ret;
	}

	private Collection<? extends String> getMatchingTuples(
			String usefulDoc, String database) {
		
		ArrayList<String> ret = docTuplesTable.get(Long.valueOf(usefulDoc));
		
		if (ret != null)
			return ret;
		
		return new ArrayList<String>();
		
	}
	
	private ArrayList<String> generateSample(long need,
			ArrayList<String> retrieveFrom) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		long cant = 0;
		
		while (cant < need){
			
			System.out.println(id + "--->" + cant + " out of " + need + " -- " + retrieveFrom.size());
			
			long newIndex = generateNewValue(retrieveFrom.size()-1);
			
			ret.add(retrieveFrom.get((int)newIndex));
			
			cant++;
		}
		
		return ret;
	}

	private long generateNewValue(long maxValue) {
		
		int value = (int) Math.round(Math.random()*maxValue);
		
		while (alreadyChosen.contains(value)){
			value = (int) Math.round(Math.random()*maxValue);
		}
		
		alreadyChosen.add(value);
		
		return value;
	}
	
}
