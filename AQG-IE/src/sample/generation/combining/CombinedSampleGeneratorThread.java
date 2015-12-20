package sample.generation.combining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import sample.generation.model.impl.DummySampleConfiguration;
import sample.generation.utils.SampleGenerationUtils;
import sample.weka.GenerateArffFromSample;
import sample.weka.SampleArffGenerator;
import utils.FileHandlerUtils;
import utils.persistence.databaseWriter;
import utils.word.extraction.WordExtractorAbs;
import execution.workload.tuple.Tuple;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;

public class CombinedSampleGeneratorThread implements Runnable {

	private ArrayList<String> noFilteringFields;
	private HashSet<Tuple> tuples;
	private ArrayList<DocumentHandle> sampleReadyDocs;
	private ArrayList<DocumentHandle> usefulReadyDocs;
	private int workload;
	private String version;
	private int sample_number;
	private String databaseId;
	private String[] database;
	private databaseWriter pW;
	private int id;
	private String relation;
	private boolean frequency;
	private boolean stemmed;
	private String noFilteringFieldsString;
	private WordExtractorAbs usefulWE;
	private WordExtractorAbs generalWE;
	
	public CombinedSampleGeneratorThread(int workload, String version,
			int sample_number, String databaseId, String[] database,
			databaseWriter pW, int id, String relation, boolean frequency, boolean stemmed, String noFilteringFieldsString, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) {
		
		this.workload = workload;
		this.version = version;
		this.sample_number = sample_number;
		this.databaseId = databaseId;
		this.database = database;
		this.pW = pW;
		this.id = id;
		this.relation = relation;
		this.frequency = frequency;
		this.stemmed = stemmed;
		this.noFilteringFieldsString = noFilteringFieldsString;
		this.usefulWE = usefulWE;
		this.generalWE = generalWE;
		
	}

	@Override
	public void run() {
		
		try {
		
			WorkloadModel dummyWorkload = pW.getWorkloadModel(workload);
			
			boolean created = generateSample(version,sample_number,dummyWorkload,databaseId,database,pW);

			if (!created)
				return;
			
			String[] args = new String[16];
			
			Sample dummysample = Sample.getSample(pW.getDatabaseByName(databaseId),new DummyVersion(version),dummyWorkload,sample_number,new DummySampleConfiguration(1));
			
			args[2] = pW.getSampleFilteredFile(dummysample);
			
			args[3] = ""; //Prefix
			
			args[4] = pW.getArffFullModel(dummysample);
		
			args[8] = pW.getStopWords();
			
			args[9] = relation;

			args[11] = databaseId + id + "-mixingSampling";
			
			args[12] = Boolean.valueOf(frequency).toString();
			
			args[13] = Boolean.valueOf(stemmed).toString();
			
			args[14] = noFilteringFieldsString;
	
			generateCrossableSample(args,dummysample,usefulWE,generalWE);
	
			generateRawSample(databaseId, dummysample, pW,usefulWE,generalWE);
			
			args = new String[4];
			
			args[2] = "0.003"; //%min
			
			args[3] = "0.9"; //%max
			
//XXX class not used any more			new SampleGenerationUtils().tailorAttributes(dummysample,args,pW);
			
			args = new String[2];
			
//			new SampleGenerationUtils().runSVM(dummysample,args,pW);
			
			args = new String[4];
			
			args[1] = "700";
			
//			new SampleGenerationUtils().removeAttributesBasedOnSVM(dummysample,args,pW,id);
			
			new SampleGenerationUtils(null).generateTrueModel(dummysample, pW);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	private void generateRawSample(String databaseName, Sample sample, databaseWriter pW, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) throws IOException {

		String stopWords = pW.getStopWords();

		ArrayList<String> noFilteringFields = new ArrayList<String>();

		noFilteringFields.add(noFilteringFieldsString);

		String relation = this.relation;

		String output = pW.getArffRawFilteredModel(sample);

		String tuplesFile = pW.getSampleTuples(sample);

		tuples = GenerateArffFromSample.loadTuples(tuplesFile);

		String idFiles = pW.getSampleFilteredFile(sample);

		sampleReadyDocs = createDocumentHandles(idFiles);

		String usefulFiles = pW.getSampleUsefulDocuments(sample);
		
		usefulReadyDocs = createDocumentHandles(usefulFiles);
		
		SampleArffGenerator rbl = new SampleArffGenerator(stopWords, usefulWE, generalWE);

//		rbl.learn(sampleReadyDocs, usefulReadyDocs, output, tuples, id + "-mixingSample", frequency,
//				stemmed, noFilteringFields, null,false);

	}

	private ArrayList<DocumentHandle> createDocumentHandles(
			String idFiles) throws IOException {
		
		ArrayList<DocumentHandle> ret = new ArrayList<DocumentHandle>();
	
		LineIterator it = FileUtils.lineIterator(new File(idFiles));
		
		while (it.hasNext()){
			ret.add(new DocumentHandle(it.next()));
		}
		
		it.close();
		
		return ret;
		
	}
	
	private void generateCrossableSample(String[] args, Sample sample, WordExtractorAbs usefulWE, WordExtractorAbs generalWE)
			throws IOException {

		boolean frequency = Boolean.valueOf(args[12]);

		boolean stemmed = Boolean.valueOf(args[13]);

		loadNoFilteringFields(args[14]);

		SampleArffGenerator rbl = new SampleArffGenerator(args[8], usefulWE, generalWE);

		String idFiles = pW.getSampleFilteredFile(sample);

		sampleReadyDocs = createDocumentHandles(idFiles);
		
		String usefulFiles = pW.getSampleUsefulDocuments(sample);
		
		usefulReadyDocs =createDocumentHandles(usefulFiles);
		
//		rbl.learn(sampleReadyDocs, usefulReadyDocs, args[4],
//				new HashSet<Tuple>(), args[11], frequency, stemmed,
//				noFilteringFields, null,false);

	}

	private void loadNoFilteringFields(String string) {

		noFilteringFields = new ArrayList<String>();

		String[] fields = string.split(",");

		for (String string2 : fields) {
			noFilteringFields.add(string2);
		}

	}

	private boolean generateSample(String version, int sample_number,
			WorkloadModel dummyWorkload, String databaseName,
			String[] database, databaseWriter pW) throws IOException {

		HashSet<String> tuples = new HashSet<String>();

		ArrayList<String> docs = new ArrayList<String>();

		ArrayList<String> usefulDocs = new ArrayList<String>();
		
		boolean hasValue = false;
		
		for (int i = 0; i < database.length; i++) {

			Sample sample = Sample.getSample(pW.getDatabaseByName(database[i]),
					new DummyVersion(version), dummyWorkload, sample_number,new DummySampleConfiguration(1));

			//Check that the information is available
			
			if (!new File(pW.getArffBooleanModel(sample)).exists())
				continue;
			
			hasValue = true;
			// reads All the tuples (to clean the sample)

			String tuplesFile = pW.getSampleTuples(sample);

			// update the information for the learner.

			tuples.addAll(readTuples(tuplesFile));

			docs.addAll(readDocuments(pW.getSampleFilteredFile(sample)));

			usefulDocs.addAll(readDocuments(pW.getSampleUsefulDocuments(sample)));
			
		}

		if (!hasValue)
			return false;
		
		Sample combinedSample = Sample.getSample(
				pW.getDatabaseByName(databaseId), new DummyVersion(version),
				dummyWorkload, sample_number,new DummySampleConfiguration(1));


		String fileIds = pW.getSampleFilteredFile(combinedSample);

		writeSampleFiles(fileIds,docs);
		
		String usefulfileIds = pW.getSampleUsefulDocuments(combinedSample);
		
		writeSampleFiles(usefulfileIds, usefulDocs);		
		
		// Save the extracted tuples

		String tuplesFile = pW.getSampleTuples(combinedSample);

		writeValues(tuplesFile, tuples);

		return true;
		
	}

	private void writeSampleFiles(String fileIds, List<String> docs) {
		
		try {
			FileUtils.writeLines(new File(fileIds), docs);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private Collection<? extends String> readDocuments(String sampleFile) {
		try {
			return FileUtils.readLines(new File(sampleFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	private void writeValues(String file, Collection<String> lines)
			throws IOException {

		int i = 1;

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));

		for (String string : lines) {

			bw.write(string + "," + i + "\n");

			i++;

		}

		bw.close();

	}

	private Collection<? extends String> readTuples(String tuplesFile) {

		return FileHandlerUtils.getAllResourceNames(new File(tuplesFile));

	}
	
}
