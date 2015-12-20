package utils.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;

import org.apache.commons.io.FileUtils;

import com.google.gdata.util.common.base.Pair;

import sample.generation.model.SampleBuilderParameters;
import sample.generation.model.SampleConfiguration;
import searcher.interaction.formHandler.TextQuery;
import utils.document.DocumentHandler;
import utils.id.Idhandler;
import utils.results.ResultHandler;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import edu.columbia.cs.ref.model.CandidateSentence;
import execution.model.Generation;
import execution.model.Source;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.trunk.SampleBuilder;
import execution.workload.tuple.Tuple;
import exploration.model.Combination;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Execution;
import exploration.model.ExecutionAlternative;
import exploration.model.Query;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.clusterfunction.ClusterFunction;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;
import exploration.model.enumerations.QueryStatusEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.source.similarity.SimilarityFunction;

public abstract class persistentWriter {
	
	public static int QUERY_GENERATED_POSITION = 0;
	public static int QUERY_SUBMITTED_POSITION = 1;
	public static int QUERY_ROUND = 2;
	public static int DOCUMENT_POSITION = 3;
	public static int DOCUMENT_IN_QUERY_POSITION = 4;
	public static int DOCUMENT_POSITION_IN_SAMPLE = 5;
	public static int USEFUL_TUPLES = 6;
	public static int DOCUMENT_DATABASE = 7;
	public static int DOCUMENT_ID = 8;
	
	private static final String TASTE_SUFFIX = "_Taste";
	private static final ExperimentEnum[] CONSISTENSIES = {null,ExperimentEnum.CONSISTENSY_1,ExperimentEnum.CONSISTENSY_2,ExperimentEnum.CONSISTENSY_3,ExperimentEnum.CONSISTENSY_4,ExperimentEnum.CONSISTENSY_5,ExperimentEnum.CONSISTENSY_6};
	private static final ExperimentEnum[] NEGATIVE_CONSISTENSIES = {null,ExperimentEnum.NEGATIVE_CONSISTENSY_1,ExperimentEnum.NEGATIVE_CONSISTENSY_2,ExperimentEnum.NEGATIVE_CONSISTENSY_3,ExperimentEnum.NEGATIVE_CONSISTENSY_4,ExperimentEnum.NEGATIVE_CONSISTENSY_5};

	private String directoryToMake;

	protected String prefix;
	protected BufferedWriter bwo;

	private String outputPrefix;

	private int nextEvaluationid;

	private String retString;
	private HashMap<Integer, Idhandler> idHandlerTable;
	private Map<String, List<String>> initialRandomQueriesTable;
	private String computername;
	
	protected persistentWriter (String prefix){
		this.prefix = prefix;
	}
	
	public abstract int setAlgorithm(String algorithm, int database, String version, int workload, int version_pos_seed, int version_neg_seed, int sample_configuration, int sample_parameters, int w_parameter_ID) throws IOException;

	public abstract void writeQueries(int combination_id, List<Pair<TextQuery,Long>> querytime);

	public abstract void endAlgorithm() throws IOException;

//	public abstract void setIncrementalAdditionalParameters(int w_parameterId, double performance_threshold, double fp_weight,
//			double beta_Efficiency);
//
//	public abstract void setMSCAdditionalParameters(int w_parameterId, double minPrecision,
//			double minimumSupportSVM, int k, double pow);
//
//	public abstract void setOptimistic(int w_parameterId, double threshold_performance, double minWeight, double minPrecision,
//			double minimumSupportSVM);
//
//	public abstract void setQProberAdditionalParameters(int w_parameterId, double minimumSupportSVM, double minWeight, double minPrecision);
//
//	public abstract void setTupleAdditionalParameters(int w_parameterId, long hitsPerPage,
//			double querySubmissionPerUnitTime, long queryTimeConsumed,
//			double ieSubmissionPerUnitTime, long ieTimeConsumed);
//	
//	public abstract void setRipperAdditionalParameters(int w_parameterId, int fold, double minNo,
//			int optimizationRuns, long seedValue, boolean pruning,
//			boolean checkErrorRate);
	
	public abstract int setCombinedAlgorithm(Combination simpleConfig, Sample crossableDatabase, int configuration, int w_combParameter) throws IOException;

	public abstract void insertEvaluation(Evaluation currentEvaluation, Execution w_execution);

	public abstract void writeProcessedDocuments(Execution w_execution,Evaluation evaluation,
			int evaluation_position, Query query, long query_position, Document document, long document_position, long currentTime, int usefulTuples);
	public abstract void writeDetectedTuples(Execution execution, ArrayList<String> tuples);

	public abstract Execution insertExecution(ExecutionAlternative w_executionAlternative);


	public abstract int getConfiguration(double weightCoverage, double weightSpecificity);

	public abstract void updateCurrentAlgorithmTime(int w_combinationID, long measuredTime);

	public abstract List<Database> getDatabases();

	public abstract List<Database> getCrossableDatabases();

	public abstract List<Database> getSearchableDatabases(String computername);
	
	public List<Database> getSearchableDatabases(){
		return getSearchableDatabases(null);
	}
	
	public abstract List<Execution> getExecutions();

	public abstract List<Combination> getCombinations(Version version, int workloadId);

	public abstract List<ExecutionAlternative> getActiveExecutionAlternatives();

	public abstract List<Query> getQueries(Combination combination);
	
	public abstract void cleanForExperiments();

	public abstract List<Hashtable<String, Double>> getAnalyzableData(
			int execution, List<String> attributes, List<String> orderBy);

	public abstract List<Integer> getExecutionsId(int executionAlternative);

	public abstract List<Integer> getCombinations(String algorithmName);

	public abstract Collection<? extends Integer> getExecutions(Integer combinationId);

	public abstract double getTotal(String attribute, Integer execution);

	public abstract boolean existsEvaluation(Evaluation ev, int executionAlternativeId);
	
	public abstract void saveSimilarity (Database basedOnDatabase,
			Database comparedDatabase,
			SimilarityFunctionEnum similarityFunction, double distance, Version version,
			int sample_number, WorkloadModel wlmodel);
	
	public String getOutputFile(Execution execution, int evaluation) {
		
		return getOutputFilePrefix(execution) + evaluation + ".txt";
	
	}
	
	public synchronized String getArffBooleanModel(Sample sample, SampleBuilderParameters sp, int uselessSample) {
	
		retString = prefix + "arff/Ready/" + getArffBoolean(sample,sp,uselessSample) + ".arff";
		
		return createDirectory(retString);
		
	}

	private String getArffBoolean(Sample sample, SampleBuilderParameters sp,
			int uselessSample) {
		return getSVMed(sample, sp, uselessSample);
	}

	private String getFileName(Sample sample) {
		
		return sample.getDatabase().getName() + "/" + sample.getWorkload().getId() + "/" 
		+ sample.getVersion().getName() + "/" + sample.getSampleConfiguration().getId() + "/" + sample.getId() + "_" + sample.getVersionSeedPos()+"_"+sample.getVersionSeedNeg();
		
	}

	private String getFileName(Sample sample,SampleBuilderParameters sampleBuilderParameters) {
		
		return getFileName(sample) + "_" + getInformation(sampleBuilderParameters);
		
	}
	
	private String getInformation(
			SampleBuilderParameters sampleBuilderParameters) {
		
		return "LC_" + sampleBuilderParameters.getLowerCase() + "_ST_" + sampleBuilderParameters.getStemmed() + "_UQ_" + sampleBuilderParameters.getUnique() + "_TA_" + sampleBuilderParameters.getTuplesAsStopWords();
		
	}

	public synchronized String getSMOWekaOutput(Sample sample,SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		
		retString =  prefix + "WekaOutput/SMO/" + getSMO(sample, sampleBuilderParameters, uselessSample);
		
		return createDirectory(retString);
		
	}

	private String getSMO(Sample sample, SampleBuilderParameters sp,
			int uselessSample) {
		return getArffTailored(sample, sp, uselessSample);
	}
	
	public synchronized String getArffBooleanTrueModel(Sample sample, SampleBuilderParameters sp,
			int uselessSample) {
	
		retString =  prefix + "arff/True/" + getBooleanTrue(sample,sp,uselessSample) + ".arff";
	
		return createDirectory(retString);
		
	}

	private String getBooleanTrue(Sample sample, SampleBuilderParameters sp,
			int uselessSample) {
		return getSVMed(sample, sp, uselessSample);
	}
	
	public synchronized String getDatabaseIds(String database) {
		
		retString =  prefix + "id/" + database + ".id";
		
		return createDirectory(retString);
		
	}
	
	public void endWritingOutput() throws IOException {
		
		bwo.close();
		
	}

	public void writeOutput(long measuredTime, String query,
			ArrayList<Long> tp, ArrayList<Long> fp, ArrayList<Long> resIds) throws IOException {
		
		String tpString = ResultHandler.generateSequence(tp);
		
		String fpString = ResultHandler.generateSequence(fp);
		
		String resIdsString = ResultHandler.generateSequence(resIds);
		
		bwo.write(ResultHandler.generateTriple(measuredTime, query, tpString, fpString, resIdsString));
		
	}

	public synchronized String getTasteFile(Combination config,
			Sample crossableSample) {
		
		retString =  getScoresFile(config,crossableSample) + TASTE_SUFFIX;
		
		return createDirectory(retString);
		
	}

	public synchronized String getScoresFile(Combination config,
			Sample crossableSample) {

		retString =  prefix + "evaluation/final/Scores/" + getFileName(crossableSample) + "_" +  config.getId();
	
		return createDirectory(retString);
	}
	
	private synchronized String createDirectory(String file) {
		
		directoryToMake = file.substring(0,file.lastIndexOf("/") + 1);
		
		File f = new File(directoryToMake);
		
		if (!f.exists())
			(new File(directoryToMake)).mkdirs();
		
		return file;
	}

	public abstract List<String> getSampleFilteredDocuments(Sample sample,SampleBuilderParameters sampleBuilderParameters);/* {
		
		retString =  prefix + "sample/Filtered/"+ getFileName(sample,sampleBuilderParameters) +".txt";
		
		return createDirectory(retString);
		
	}*/

	public synchronized String getMatchingTuplesWithSourcesFile(Sample sample) {
		
		retString =  prefix + "sample/MatchingTuplesWithSources/" + getFileName(sample) + ".tuples";
		
		return createDirectory(retString);
		
	}

	public synchronized String getMatchingTuplesWithSourcesFile(String database, String version, WorkloadModel workload) {
		
		retString =  prefix + "MatchingTuplesWithSources/" + workload.getId() + "/" + database + "_" + version + ".tuples";
		
		return createDirectory(retString);
		
	}
	
	public synchronized File getInitialMatchingTuplesWithSourcesFile(Database database,
			Version version, WorkloadModel workload, String resName) {
		
		retString =  prefix + "MatchingTuplesWithSources/Initial/" + workload.getId() + "/" + database.getId() + "_" + version.getName() + "_" + resName + ".tuples";
		
		return new File(createDirectory(retString));
		
	}
	
	public synchronized String getPrefix(String modelClass, String database) {
		
		retString =  "/proj/db/NoBackup/pjbarrio/sites/" + modelClass + "/" + database + "/";
	
		return createDirectory(retString);
		
	}
	
	public synchronized String getUselessFiles(String database, String version, WorkloadModel workload) {
		
		retString =  prefix + "Useless/" + workload.getId() + "/" + database + "_" + version;
		
		return createDirectory(retString);
		
	}

	public abstract List<Document> getSampleDocuments(Sample sample, int numberofUsefulDocuments,int numberofUselessDocuments, DocumentHandler dh);
	
	public abstract List<Document> getSampleDocuments(Sample sample, int numberofUsefulDocuments,int numberofUselessDocuments);/* {
		
		retString =  prefix + "sample/Raw/" + getFileName(sample,sampleBuilderParameters) + ".txt";
		
		return createDirectory(retString);
	}*/

	/*public abstract String getSampleTuples(Sample sample); {
		
		retString = prefix + "sample/Tuples/"+ getFileName(sample) + ".tuples";
		
		return createDirectory(retString);
		
	}*/

	public synchronized String getUsefulFiles(String database, String version, WorkloadModel workload) {
		
		retString = prefix + "Useful/" + workload.getId() + "/" + database + "_" + version;
				
		return createDirectory(retString);
	}
	
	public synchronized String getSampleUsefulDocuments(Sample sample) {
		
		retString = prefix + "sample/Useful/" + getFileName(sample) + ".useful";
		
		return createDirectory(retString);
	}

	public synchronized String getArffRawModel(Sample sample, SampleBuilderParameters sp, int uselessSample){
		
		retString = prefix + "arff/Raw/" + getArffRaw(sample,sp,uselessSample)  + ".arff";
		
		return createDirectory(retString);
	}

	private String getArffRaw(Sample sample, SampleBuilderParameters sp, int uselessSample) {
		return getFileName(sample,sp) + "_UF_" + sp.getUsefulDocuments() + "_UL_" + sp.getUselessDocuments() + "_US_" + uselessSample;
	}

	public synchronized String getTableFile(String database) {
		
		retString = "/proj/db/NoBackup/pjbarrio/OCOutput/" + database + ".table";
	
		return createDirectory(retString);
		
	}

	public synchronized String getArffFilteredRawModel(Sample sample,SampleBuilderParameters sampleBuilderParameters, int uselessSample) {

		retString = prefix + "arff/Filtered/" + getArffFiltered(sample,sampleBuilderParameters,uselessSample) + ".arff";
		
		return createDirectory(retString);
	}

	private String getArffFiltered(Sample sample, SampleBuilderParameters sampleBuilderParameters,
			int uselessSample) {
		return getArffRaw(sample,sampleBuilderParameters,uselessSample) + "_Filtered";
	}

	public synchronized String getArffTailoredModel(Sample sample,SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		
		retString = prefix + "arff/Final/" + getArffTailored(sample,sampleBuilderParameters,uselessSample) + ".arff";
		
		return createDirectory(retString);
	}

	private String getArffTailored(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		
		return getArffFiltered(sample, sampleBuilderParameters, uselessSample)  + "_MinF_" + sampleBuilderParameters.getMinFrequency() + "_MaxF" + sampleBuilderParameters.getMaxFrequency();
		
	}

	public String getSVMedModel(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		
		
		retString = prefix + "arff/Final/" + getSVMed(sample,sampleBuilderParameters,uselessSample) + ".arff";
		
		return createDirectory(retString);
		
	}
	
	private String getSVMed(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) {
		
		return getArffTailored(sample, sampleBuilderParameters, uselessSample) + "_Feat_" + sampleBuilderParameters.getFeatures();
	
	}

	public synchronized String getArffFullModel(Sample sample,SampleBuilderParameters sampleBuilderParameters) {
		
		//FIXME should return the raw arff NO tuples as stop words
		
		System.err.println("Implement well!");
		
		retString = prefix + "arff/ReadyFull/" + getFullFileName(sample,sampleBuilderParameters) + ".arff";
	
		return createDirectory(retString);
		
	}
	
	private String getFullFileName(Sample sample,
			SampleBuilderParameters sampleBuilderParameters) {
		return getFileName(sample) + "_" + getFullInformation(sampleBuilderParameters);
	}

	private String getFullInformation(
			SampleBuilderParameters sbp) {
		return sbp.getParameter()+"_"+(sbp.getUnique()? "uq" : "fq")+"_"+(sbp.getLowerCase()? "lc":"cs")+"_"+(sbp.getStemmed()? "st":"fw") + "_" + (sbp.getTuplesAsStopWords()? "tasw":"aw");
	}

	public synchronized String getTempFile(Sample sample, String aux) {
		
		retString = prefix + "TEMP/" + aux + "/" + getFileName(sample) + ".txt";
		
		return createDirectory(retString);
		
	}
	
	private String getOutputFilePrefix(Execution execution) {
		
		createDirectory(prefix + "evaluation/final/output/");
		
		return prefix + "evaluation/final/output/" + execution.getId() + "_";
	
	}

	public synchronized String getSeedTuples(String database, String version, WorkloadModel workload, int seedTuplesGroup, String resName) {
		
		retString = prefix + "seed/" + workload.getId() + "/" + database + "_" + version + "_" + resName + "_" + seedTuplesGroup;
			
		return createDirectory(retString);
	}

	public void setOutputExecution(Execution execution) {
		
		outputPrefix = getOutputFilePrefix(execution);
	
		nextEvaluationid = 1;
		
	}

	public ArrayList<String> getOutputFiles(Execution execution) {
		
		int experiments = execution.getEvaluations().size();
		
		ArrayList<String> arr = new ArrayList<String>();
		
		for (int evaluation = 1; evaluation <= experiments; evaluation++) {
			
			arr.add(getOutputFile(execution, evaluation));
			
		}
	
		return arr;
		
	}
	
	public void setOutputEvaluation(Evaluation evaluation) throws IOException {
		
		if (bwo != null){
		
			bwo.close();
		
		}
				
		bwo = new BufferedWriter(new FileWriter(new File(outputPrefix + nextEvaluationid + ".txt")));
		
		bwo.write("\n");
		
		nextEvaluationid++;
	}

	public synchronized String getDatabaseExtractionTable(Database database) {
		
		retString = "/proj/db/NoBackup/pjbarrio/OCOutput/"+ database.getName() +".table";
		
		return createDirectory(retString);
	}

	public String getStopWords() {
		
		return "src/searcher/lucene/stopWords.txt";
	
	}

	public synchronized String getRelationTuples(Database db, String relation) {
		return prefix + "tuples/" + relation + "/" + db.getName() + "/tuples";
	}
	
	public synchronized String getRelationUseful(Database db, String relation) {
		return prefix + "tuples/" + relation + "/" + db.getName() + "/useful";
	}

	public synchronized String getRelationUseless(Database db, String relation) {
		return prefix + "tuples/" + relation + "/" + db.getName() + "/useless";
	}
	
	public String getContentSummaryFile(Database db) {
		return "/local/pjbarrio/Files/Research-Dataset/LuceneSites/"+ db.getType() +"-" + db.getName() + "-cs.txt";
	}

	public synchronized String getBasedOnContentSummary(Sample sample, Database db) {
		
		retString = prefix + "basedOn/" + db.getName() + "/" + getFileName(sample) + "-cs.txt";
		
		return createDirectory(retString);
	
	}

	public void saveStoredDownloadedDocument(Document document){
		saveStoredDownloadedDocument(document,true);
	}

	public abstract void saveStoredDownloadedDocument(Document document, boolean success);

	public abstract void saveExtractionTime(Document file, String extractionSystem, long time);

	public abstract Database getDatabaseByName(String name);

	public synchronized String getContentSummaryFile(Sample sample) {
		
		retString = prefix + "sample/ContentSummary/" + getFileName(sample) + ".arff";
		
		return createDirectory(retString);
	}

	public String getQueries(Sample sample) {
		return prefix + "queries/Initial/" + sample.getDatabase().getId() + "/" + sample.getDatabase().getId() + ".txt";
	}

	public String getBasePrefix() {
		return prefix;
	}

	public abstract Hashtable<String, Long> getDocumentsTable(Database database);

	public abstract void insertDocument(int idDatabase, long idDocument, String path);

	public abstract Hashtable<String, Document> getDownloadedDocumentsTable(int databaseId, int experimentId);

	public abstract Hashtable<Long, Document> getDownloadedDocumentsIdTable(int databaseId, int experimentId, List<Long> documents);
	
	public int getStorableId(URL url){
		return url.toString().hashCode();
	}

	public abstract WorkloadModel getWorkloadModel(int workloadModelId);

	public abstract List<Integer> getExecutions(List<Integer> executionAlternatives, String algorithmName);

	public abstract double getHTMLValidatorThreshold(Database website, String htmlValidator);

	public abstract void saveHTMLValidatorThreshold(Database db, String name,
			double threshold);

	public abstract int getNextSampleNumber(int id, Database database, Version version,
			WorkloadModel workload);

	public abstract int writeSample(Sample sample);

	public abstract void WriteSentQuery(int queriedDB, int generatedQueries, Sample sample, TextQuery query);

	public abstract void WriteSentQuery(int queriedDB, int generatedQueries, Sample sample, long idTextQuery);
	
	public abstract void addProcessedDocument(int idSample, int query_generated_position, int query_submitted_position, int queryRound, int documentPosition,
			int document_position_in_query, int document_position_in_sample, int useful_tuples, Document document);

	public abstract List<SampleConfiguration> getActiveSampleConfigurations(int activeValue);

	public abstract List<String> getRelationKeywords(int idInformationExtractionSystem, int relationConf, String collection, WorkloadModel idWorkload, Version idVersion, boolean tuplesAsStopWords, int version_seed, ASEvaluation eval, int docsInTraining);
	
//	public List<String> getRelationKeywords(String relation, boolean tuplesAsStopWords, int version_seed, ASEvaluation eval){
//		
//		File f = getRelationKeywordsFile(relation,tuplesAsStopWords,version_seed, eval);
//		
//		
//		try {
//			return FileUtils.readLines(f);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return new ArrayList<String>(0);
//	}

	public File getRelationKeywordsFile(String collection, String relation,
			boolean tuplesAsStopWords, int version_seed, ASEvaluation eval,String relationExtractor) {
		
		File f;
		
		if (tuplesAsStopWords)
			f = new File(prefix + "keywords/" + collection + "_" + relationExtractor + "_" + relation + "_" + eval.getClass().getSimpleName() + "_" + version_seed + "-TASW.keywords");
		else
			f = new File(prefix + "keywords/" + collection + "_" + relationExtractor + "_" + relation + "_" + eval.getClass().getSimpleName() + "_" + version_seed + ".keywords");
		
		return f;
		
	}

	public abstract File getRelationRulesFile(int idInformationExtractionSystem,String collection, int relationConf,WorkloadModel workload, Version version, int docsInTraining, int version_seed, boolean tuplesAsStopWords);
//	{
//	
//	
//	
//	if (tuplesAsStopWords)
//		return new File(prefix + "rules/" + workload.getId() + "_" + version.getId() + "_" + version_seed + "-TASW.rules");
//	
//	return new File(prefix + "rules/" + workload.getId() + "_" + version.getId() + "_" + version_seed + ".rules");
//}

	
//	public abstract File getRelationRulesFile(String collection, WorkloadModel workload,
//			Version version, int docsInTraining, int version_seed, boolean tuplesAsStopWords);


	
	protected abstract String getRelationName(int idWorkload);

	public File getSourceWords(String source, int version_seed){
		
		return new File(prefix + "querySources/" + source + "_" + version_seed + ".wf");
		
	}

	public File getSourceWords(String source){
		
		return new File(prefix + "querySources/" + source + ".wf");
		
	}
	
	public List<String> getInferredTypes(String relation){
		
		File f = new File(prefix + "inferredTypes/" + relation + ".itypes");
		try {
			return FileUtils.readLines(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>(0);
		
	}

	public List<String> getNoFilteringFields(String relation) {
		
		File f = new File(prefix + "NoFilteringFields/" + relation + ".nffields");
		try {
			return FileUtils.readLines(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>(0);
		
	}
	
	public String getDocumentListForCollection(String collection) {
		return prefix + "collectionList/" + collection + "/documents.extraction";
	}

	public String getMatchingTuplesWithSources(String collection,
			String relation, String relationExtractor) {
		
		String ret = prefix + "collectionList/" + collection + "/" + relation  + "/"+relationExtractor+"-MatchingSourceWithTuples.tuples";
		
		return createDirectory(ret);
	}

	
	public String getUsefulDocumentsForCollection(String collection,
			String relation, String relationExtractor) {
		String ret = prefix + "collectionList/" + collection + "/" + relation + "/"+relationExtractor+"-useful.extraction";
		return createDirectory(ret);
	}

	public String getSelectedUsefulDocumentsForCollection(String collection, String relation, int selectedNumber) {
		return prefix + "collectionList/" + collection + "/" + relation + "/useful-"+selectedNumber+".extraction";
	}
	
	public String getUselessDocumentsForCollection(String collection,
			String relation, String relationExtractor) {
		String ret =  prefix + "collectionList/" + collection  + "/" + relation + "/"+relationExtractor+"-useless.extraction";
		return createDirectory(ret);
	}
	
	public String getUsefulDocumentExtractionForRelation(String collection,
			String relation, int size, int split, String relationExtractor){
	
		return prefix + "collectionList/" + collection  + "/" + relation + "/"+relationExtractor+"-useful_" + split + "_" + size + ".extraction";

	}

	public String getUselessDocumentExtractionForRelation(String collection,
			String relation, int size, int split, String relationExtractor){
	
		return prefix + "collectionList/" + collection + "/" + relation  + "/"+relationExtractor+"-useless_" + split + "_" + size + ".extraction";

	}

	
	public List<Document> getUsefulDocumentListForRelation(String collection,
			String relation, int size, int split) {

		throw new UnsupportedOperationException("Implement");

	}

	public List<Document> getUselessDocumentListForRelation(String collection,
			String relation, int size, int split) {
		throw new UnsupportedOperationException("Implement");

	}
	
	public String getArffModelForSplit(String collection, String relation,
			int size, int split, boolean tuplesAsStopWords, String relationExtractor) {
		
		String ret = prefix + "arffModel/" + collection + "/" + relation + "/" + tuplesAsStopWords + "/" + relationExtractor + "-model_" + split + "_" + size + ".arff";
		
		return createDirectory(ret);
	}
	
	public String getReducedArffModelForSplit(String collection,
			String relation, int size, int split, boolean tuplesAsStopWords, String relationExtraction) {
		
		return prefix + "arffModel/" + collection + "/" + relation + "/" + tuplesAsStopWords + "/"+relationExtraction+"-reduced_model_" + split + "_" + size + ".arff";
	}

	public String getRelationWordsFromSplitModel(String collection,
			String relation, int size, int split, ASEvaluation eval, ASSearch search, boolean tuplesAsStopWords, String relationExtractor) {
		return createDirectory(prefix + "arffModel/" + collection + "/" + relation + "/" + tuplesAsStopWords + "/SelectedAttributes/" + relationExtractor + "-relationWords_" + search.getClass().getSimpleName() + "_" + eval.getClass().getSimpleName() + "_" + split + "_" + size + ".words");
	}

	public String getAggregatedRanking(String collection, String relation,
			ASEvaluation eval, ASSearch search, boolean tuplesAsStopWords, String relationExtractor) {
		
		return prefix + "arffModel/" + collection + "/" + relation + "/" + tuplesAsStopWords + "/AggregatedRanking/"+relationExtractor+"-aggregatedRanking_" + search.getClass().getSimpleName() + "_" + eval.getClass().getSimpleName() + ".rank";
	}
	
	public String getRulesFromSplitModel(String collection, String relation,
			int size, int split, Classifier classifier, boolean tuplesAsStopWords, String relationExtractor) {
		
		String ret = prefix + "arffModel/" + collection + "/" + relation + "/" + tuplesAsStopWords + "/Rules/"+relationExtractor+"-rules_" + classifier.getClass().getSimpleName() + "_" + split + "_" + size + ".rules";
	
		return createDirectory(ret);
	}
	
	public File getRelationsFile(String relation) {
		return new File(prefix + "Relations/" + relation);
	}
	
	public File getTfIdfWordsFileForWebsite(Database database) {
		return new File(getTfIdfFolder(), database.getId() + ".txt");
	}
	
	public String getTfIdfFolder() {
		return prefix + "/tfIdfWords/";
	}

	public String getComputerName() {

		if (computername == null){
		
			try{
				computername = InetAddress.getLocalHost().getHostName();
				System.out.println(computername);
			}catch (Exception e){
				System.out.println("Exception caught ="+e.getMessage());
			}
		}
		return computername;
	}


	
	public abstract void makeAvailable(Sample sample, boolean finished);

	public abstract List<Hashtable<String, Double>> getAnalyzableSampleGenerationData(
			Integer sampleGeneration, List<String> attributes,
			List<String> orderBy, String where, List<String> groupBy);

	public abstract double getSampleGenerationTotal(String normalizedXAttribute,
			Integer sampleGeneration);

	public abstract List<Integer> getIdSamplesForConfiguration(List<Integer> sampleConfigurationconfiguration,int valid);

	public abstract List<Database> getSamplableDatabases(String computerName);

	public abstract List<Database> getSamplableDatabases(int group);
	
	public abstract List<Database> getDatabasesInGroup(int id,
			ClusterFunction clusterFunction, Version version,
			WorkloadModel workload);

	public abstract List<Integer> getIdSamplesForConfigurationOnDatabases(
			List<Integer> configuration, int ommitedValidValue,
			List<Integer> databases);

	public abstract Sample getSample(int sampleGeneration);

	public abstract SampleConfiguration getSampleConfigution(int sampleConfigurationId);

	public abstract int getSampleId(int idSampleConfiguration, int idDatabase, int version_seed_pos, int version_seed_neg);

	public abstract List<int[]> getSampleGeneration(
			int sampleNumber, int documents);

	public abstract List<Pair<int[], Long>> getSampleGenerationQueries(int sampleNumber, int lastQuery);

	public abstract void clean(int idDatabase, int idSampleConfiguration);

	public synchronized File getForTrainingSourcesFile(Database database, Version version,
			WorkloadModel workload, ASEvaluation eval) {
		
		retString =  prefix + "RELTraining/" + workload.getId() + "/" + database.getId() + "_" + version.getName() + "_" + eval.getClass().getSimpleName() + ".sources";
		
		return new File(createDirectory(retString));
		
	}

	public synchronized File getForTrainingMatchingTuplesWithSourcesFile(Database database,
			Version version, WorkloadModel workload, ASEvaluation eval) {
		
		retString =  prefix + "RELTraining/" + workload.getId() + "/" + database.getId() + "_" + version.getName() + "_" + eval.getClass().getSimpleName() + ".tuples";
		
		return new File(createDirectory(retString));
		
	}

	public File getTuplesFile(String collection, String relation) {
		
		return new File(prefix + "collectionList/" + collection + "/" + relation  + "/tuples.tuples");
		
	}

	public List<String> getOmittedFields(String relation) {
		
		File f = new File(prefix + "OmittedFields/" + relation + ".ofields");
		
		try {
			return FileUtils.readLines(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<String>(0);
		
	}

	public File getSortedTuplesFile(String collection, String relation) {
		
		return new File(prefix + "collectionList/" + collection + "/" + relation  + "/tuplesSorted.tuples");
		
	}

	public String getSavedOutputForRelationExtractionTraining(
			String collection, String relation, int selectedNumber) {
		return prefix + "RELTraining/" + relation + "/" + collection + "_surviving-" + selectedNumber + ".tuples";
	}

	public String getSavedOutputForRelationExtractionTrainingAll(
			String collection, String relation, int selectedNumber) {
		return prefix + "RELTraining/" + relation + "/" + collection + "_all-"+selectedNumber+".tuples";
	}
	
	public File getAttributesToKeep(String collection, String relation) {
		return new File(prefix + "RELTraining/" + relation + "/attributes.list");
	}

	public abstract void insertExtraction(int idExtractionSystem, Document document,
			String internalExtraction, ContentExtractor ce);

	public abstract Map<Document, String> getExtractionTable(int id,
			int idExtractionSystem, ContentExtractor ce);

	public abstract Map<Document, String> getExtractionTable(int id,
			int idExtractionSystem, ContentExtractor ce,
			List<Long> docs);
	
	public abstract long writeTextQuery(TextQuery query);

	public abstract void saveRawResultPage(int expId, int idDatabase, TextQuery texQuery, String navigationTechnique, int page);

	public abstract boolean hasProcessedQuery(int expId, int idDatabase, TextQuery texQuery, String navigationTechnique);

	public abstract void saveExtractedResultPage(int experimentId, int id,
			TextQuery query, String extractionTechnique, String navigationTechnique, int resultPage);

	public abstract boolean hasExtractedPage(int expId, int idDatabase, TextQuery texQuery,
			String extractionTechnique, String navigationTechnique, int index);

	public abstract int getExtractedResults(int experimentId, int idDatabase, TextQuery query,
			String extractionTechnique, String navigationTechnique, String resultExtraction, int resultPage);

	public abstract List<Document> getQueryResults(int experimentId, int idDatabase,
			TextQuery query, String extractionTechnique, String navigationTechnique, String resultTechnique, int resultPage);

	public abstract Integer getProcessedPages(int experimentId, int id, TextQuery query, String navigationHandler);

	public abstract void saveExtractedResult(int experimentId, Document document,
			String extractionTechnique, String navigationTechnique, String resultExtraction, TextQuery query, int resultPage, int resultIndex);

	public abstract void writeHostDatabase(int databaseId, String computer);

	public abstract void cleanHostDatabases();

	public abstract List<List<String>> loadNonProcessedQueriesforSample(int idExperiment, Database database, String navigationTechnique, int relationConf);

	public abstract void saveQueryTime(int expId, int idDatabase, TextQuery texQuery,
			int page, long time);

	public abstract void reportQueryingStatus(String computerName, int idDatabase, QueryStatusEnum status);

	public abstract void reportQueryingStatus(String computerName, int idDatabase, int status);
	
	public abstract boolean isQueryAvailable(String computerName, int idDatabase);

	public abstract boolean isExperimentAvailable(int idExperiment, int idDatabase, String computerName);

	public abstract void reportExperimentStatus(int idExperiment, int idDatabase, String computerName,
			ExperimentStatusEnum status);
	
	public abstract void reportExperimentStatus(int idExperiment, int idDatabase, String computerName,
			int status);
	

	public abstract boolean hasExtractedEntities(int idDatabase, long idDocument, int idEntityType,
			int idInformationExtractionSystem, ContentExtractor ce);

	public abstract void saveEntity(int idDatabase, long idDocument, ContentExtractor ce,
			int informationExtractionSystem, int entityType, int start,
			int end, long time);

	public Set<Long> getDocumentsInQueryResults(int idExperiment, Database database, int[] entities, int firstRes, int lastRes, boolean queries, boolean negative, boolean tuples){

		Set<Long> ret = getDocumentsInQueryResults(database, idExperiment, entities[0], firstRes, lastRes,queries,negative,tuples);
		
		for (int i = 1; i < entities.length; i++) {
			
			ret.addAll(getDocumentsInQueryResults(database, idExperiment, entities[i],firstRes, lastRes,queries,negative,tuples));
			
		}
		
		return ret;
	
	}

	protected abstract Set<Long> getDocumentsInQueryResults(Database database, int idExperiment,  int entity, int firstRes, int lastRes, boolean queries, boolean negative, boolean tuples);

	public boolean hasExtractedEntities(int idDatabase, long idDocument, int[] tagIds,
			int idInformationExtractionSystem, ContentExtractor ce) {
		
		for (int i = 0; i < tagIds.length; i++) {
			
			if (hasExtractedEntities(idDatabase, idDocument, tagIds[i], idInformationExtractionSystem, ce))
				return true;
			
		}
		
		return false;
	}

//	public void saveEntity(int idDatabase, long idDocument, ContentExtractor ce,
//			int informationExtractionSystem, int[] tagIds, int start, int end, long time) {
//		for (int i = 0; i < tagIds.length; i++) {
//			saveEntity(idDatabase, idDocument, ce, informationExtractionSystem, tagIds[i], start, end, time);
//		}
//		
//	}

	public abstract Map<String, Integer> getEntityTypeTable();

	public abstract void writeTextQueryEntity(long queryId, int idEntityType);

	public abstract Map<String, Integer> getRelationshipTable();

	public abstract void writeTextQueryRelation(long qId, int idRelationshipType);

	public void saveExtractedDocument(int idDatabase, long idDocument,
			ContentExtractor ce, int idInformationExtractor, int[] entityIds, long time) {
		
		for (int i = 0; i < entityIds.length; i++) {
			
			saveExtractedDocument(idDatabase, idDocument, ce, idInformationExtractor, entityIds[i],time);
			
		}
		
	}

	protected abstract void saveExtractedDocument(int idDatabase, long idDocument,
			ContentExtractor ce, int idInformationExtractor, int idEntity, long time);

	public abstract void insertExperimentOnDatabase(int idDatabase, int idExperiment);

	public abstract boolean hasGeneratedCandidateSentence(int idDatabase, long idDocument,
			int relationConf, ContentExtractor ce);

	public abstract void saveGeneratedCandidateSentence(int idDatabase, long idDocument,
			int relationId, ContentExtractor ce);

	public abstract void saveCandidateSentenceGeneration(int idDatabase, long idDocument,
			ContentExtractor ce, int relationId, String file, long time);

	public abstract Map<Long, Map<Integer, List<long[]>>> getEntitiesMap(
			Database database, int[][] entities, List<Long> processedDocuments, ContentExtractor ce, boolean in);

	public synchronized String getCandidateSentencesFile(int idDatabase, long idDocument,
			ContentExtractor ce, int relationConfigurationId) {
		
		retString =  prefix + "CandidateSentences/" + relationConfigurationId + "/" + idDatabase + "/" + idDocument + "_" + ce.getName() + ".cs";
		
		return createDirectory(retString);

	}

	public abstract void cleanExperiments();

	public Set<Long> getExtractedDocuments(Database database, int[] entities,
			int extractor, ContentExtractor contentExtractor){
		
		Set<Long> docs = getExtractedDocuments(database, entities[0], extractor, contentExtractor);
		
		for (int i = 1; i < entities.length; i++) {
			
			docs.retainAll(getExtractedDocuments(database, entities[i], extractor, contentExtractor));
			
		}
		
		return docs;
	}

	protected abstract Set<Long> getExtractedDocuments(Database database, int entityType,
			int extractor, ContentExtractor contentExtractor);

	public abstract Map<Long, Pair<Integer,String>> getCandidateSentencesMap(int idExperiment, Database database,
			int relationConf, ContentExtractor ce, int informationExtractionSystem, boolean queries,
			boolean negative, boolean tuples, boolean all, int firstRes, int lastRes);

	public abstract Set<Long> getProcessedCandidateSentences(Database database,
			int relationConf, ContentExtractor ce,
			int informationExtractionSystem);

	public abstract void saveGeneratedOperableStructure(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId);

	public synchronized String getOperableStructureFile(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId) {
		
		retString =  prefix + "OperableStructures/" + relationConf + "/" + document.getDatabase().getId() + "/" + informationExtractionId + "/" +  document.getId() + "_" + ce.getName() + ".os";
		
		return createDirectory(retString);
	
	}

	public abstract void saveOperableStructureGeneration(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId,
			String file, long time);

	public abstract int getExperiment(int relationConf, int infEsys);

	public abstract void insertRelationExperiment(int relationConfiguration, int informationExtractionSystemId);

	public abstract List<Integer> getRequiredExperiments(int idExperiment);

	public abstract Set<Integer> getDatabasesByStatus(int idExperiment,
			ExperimentStatusEnum status);

	public abstract void writeExtractionPerformance(String relation, String name, int spl, int tp,
			int fp, int fn, double precision, double recall, double fmeas, String type);

	public abstract void saveCorefEntity(int idDatabase, long idDocument,
			ContentExtractor contenExtractor, int informationExtractionSystem,
			int entityType, long idRootEntity, int start, int end, long time);

	public abstract boolean hasDoneCoreferenceResolution(int idDatabase, long idDocument,
			ContentExtractor contentExtractor, int informationExractionSystem,
			int entityType);

	public abstract void saveCoreferenceResolution(int idDatabase, long idDocument,
			ContentExtractor contentExtractor, int informationExractionSystem,
			int entityType, long time);

	public synchronized List<String> loadInitialRandomQueries(String collection, int split){
		
		File f = new File(prefix + "querySources/Random_" + collection + "_" + split + ".wf");
		
		List<String> lines = getInitialRandomQueriesTable().get(f.getAbsolutePath());
		
		if (lines == null){
			
			try {

				lines = FileUtils.readLines(f);
				getInitialRandomQueriesTable().put(f.getAbsolutePath(),lines);

			} catch (IOException e) {
			
				e.printStackTrace();
			
			}
			
		}
		
		return lines;
	}

	private Map<String,List<String>> getInitialRandomQueriesTable() {
		
		if (initialRandomQueriesTable == null){
			initialRandomQueriesTable = new HashMap<String, List<String>>();
		}
		return initialRandomQueriesTable;
	}

	public void saveInitialRandomQueries(String collection, int split,
			List<String> words) {
			
		
		File f = new File(prefix + "querySources/Random_" + collection + "_" + split + ".wf");
		
		try {
			FileUtils.writeLines(f, words);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public abstract boolean existsQuery(TextQuery t);

	public abstract void prepareNegativeSampleEntry(int idExperiment, Database database,
			int split, int processedDocs, int position, TextQuery query, String navHandler, String extTechnique, String resExtTechnique);

	public abstract void prepareNegativeSampleEntry(int idExperiment, Database database,
			int split, int processedDocs, int position, long queryId, String navHandler, String extTechnique, String resExtTechnique);

	public abstract Database getDatabaseById(int idDatabase);

	public abstract List<String> getQueriesForNegativeSample(int experimentId, int processedDocs, Database database,
			int version_seed);

	public abstract String getInformationExtractionSystemName(int idRelationExtractionSystem);

	public abstract int getRelationExtractionSystemId(int relationConf,
			int informationExtractionId);

	public abstract boolean isExperimentInStatus(Database database, int idExperiment,
			ExperimentStatusEnum... state);

	public abstract void cleanExperimentStatusInStatus(int idExperiment, ExperimentStatusEnum ... state);

	public abstract void cleanExperimentStatusNotInStatus(int idExperiment,
			ExperimentStatusEnum... state);

	public abstract List<Integer> getActiveSampleConfigurationIds(int activeValue);

	public abstract List<Integer> getWorkloadIds();

	public abstract boolean hasGeneratedQueries(int idSample, int algorithm, SampleBuilderParameters spb);

	public abstract void writeGeneratedQueries(int idSample, SampleBuilderParameters spb, int algorithm, ExperimentStatusEnum state);

	public abstract void insertQueryForTuplesGeneration(int experimentId,
			Database database, long qId, int position);

	public abstract List<List<String>> loadQueriesForTuple(int experimentId, Database database);

	public abstract void insertQueryForTuplesGeneration(int experimentId,
			Database database, TextQuery tq, int position);

	public abstract int getCoreferenceExperiment(int idEntityType,
			int idInformationExtractionSystem);

	protected String getWorkloadQueries(int idWorkload) {
		return prefix + "/workload/" + idWorkload + "/WorkloadQueries";
	}

	protected abstract String[] getWorkloadRelations(int id);

	protected String getWorkloadDescription(int idWorkload) {
		return prefix + "/workload/" + idWorkload + "/Workload.ds";
	}

	protected String getWorkloadTuples(int idWorkload) {
		return prefix + "/workload/" + idWorkload + "/Workload.dl";
	}

	public abstract int getRESInformationExtractionSystem(int idRelationExtractionSystem);

	public abstract String getRESFileModel(int idInformationExtractionSystem, int idRelationshipType);

	public abstract int getRESRelationConfiguration(int idRelationExtractionSystem);

	public String getRelationExtractionModelsPrefix() {
		
		return prefix + "Models/";
		
	}

	public abstract int getRelationshipType(int idRelationConfiguration);

	public abstract int getQueriesBatchLastExecutedQuery(int searchRoundId,
			Database database, String navigationTechnique, ExperimentEnum experiment);

	public abstract List<Integer> getQueriesBatchNegativeLastExecutedQuery(int searchRoundId,
			Database database, int split, int processedDoc, String extractionTechnique, String navigationTechnique, String resultExtractionTechnique);

	public abstract int getQueriesBatchTuplesLastExecutedQuery(int searchRoundId,
			Database database, String extTechnique, String navHandler, String resExtTechnique);

	public abstract void reportQueryForTupleSent(int searchRoundId, Database database,
			int position, TextQuery tq, String extTechnique, String navHandler,
			String resExtTechnique);

	public abstract boolean isExperimentAvailable(ExperimentEnum exp, int idDatabase, String computerName);

	public abstract void makeExperimentAvailable(ExperimentEnum exp, int idDatabase);

	public abstract void makeExperimentAvailable(int exp, int idDatabase);
	
	public abstract void removeNegativeSampleEntries(int searchRoundId,
			Database database, int split, int allowed_size, String navHandler,
			String extTechnique, String resExtTechnique);

	public abstract void removeQueriesForTupleSent(int searchRoundId, Database database,
			String extTechnique, String navHandler, String resExtTechnique);

	public abstract void writeRelationKeyword(int idInformationExtractionSystem,int relationConfiguration, String collection, int idWorkload, int idVersion, int split, boolean tasw, ASEvaluation eval, int position,
			TextQuery query, int docsInTraining);

	public abstract void saveSampleDocuments(int id, List<Document> docs, boolean isUseful);

	public abstract void saveSampleTuples(int id, Hashtable<Document, List<Tuple>> tuples);

	public abstract SampleBuilderParameters getSampleBuilderParameters(int sampleBuilderId);

	public abstract List<Integer> getNotDoneSampleIds(int idWorkload, int idVersion, int idExtractor, int idRelationConfiguration, SampleBuilderParameters sampleBuilder, int idDatabase);

	public abstract Collection<Document> getUsefulDocuments(Sample sample, int numberOfUsefulDocuments);

	public abstract Set<Tuple> getTuples(Sample sample, Collection<Document> usefulDocuments);

	public abstract boolean hasGeneratedFullSample(
			SampleBuilderParameters sampleBuilderParameters, Sample sample);

	public abstract boolean hasGeneratedSample(
			SampleBuilderParameters sampleBuilderParameters, Sample sample);

	public abstract void writeFilteredDocuments(Sample sample,
			SampleBuilderParameters sp, int uselessSample, List<Document> documents);

	public abstract Map<Document, List<Tuple>> getSampleTuples(Sample sample);

	public String getSampleFilteredFile(Sample sample) {
		throw new UnsupportedOperationException("Should Implement the new version");
	}

	public String getArffFullModel(Sample dummysample) {
		throw new UnsupportedOperationException("Should Implement the new version");
	}

	public String getSampleFile(Sample sample) {
		throw new UnsupportedOperationException("Should Implement the new version");
	}
	
	public String getArffBooleanModel(Sample sample) {
		throw new UnsupportedOperationException("Should Implement the new version");
	}

	public String getArffRawFilteredModel(Sample sample) {
		throw new UnsupportedOperationException("Should Implement the new version");
	}

	public String getArffRawModel(Sample sample) {
		throw new UnsupportedOperationException("Should Implement the new version");
	}

	public String getArffTailoredModel(Sample sample) {
		throw new UnsupportedOperationException("Should Implement the new version");
	}

	public String getRelation(WorkloadModel workloadModel) {
		if (workloadModel.getRelations().length == 1)
			return workloadModel.getRelations()[0];
		else
			throw new UnsupportedOperationException("Should Implement the new version");
	}

	public abstract String getAttributeWithLargeDomain(String relation);
	
	public abstract String getAttributeWithSmallDomain(String relation);

	public abstract void writeSampleConfigurations(List<int[]> sc);

	public abstract String writeInternalTuples(int idDatabase, int idExtractionSystem, String content, long time);

	public abstract Pair<String, String> getInformationExtractionDescription(int extractionSystemId, int relationConfiguration);

	public abstract Map<String,String> loadDatabaseExtractions(Database db, int idExtractionSystem);

	public abstract Map<Integer, List<String>> getInternalTupleMap();

	public abstract void finishBatchDownloader(int idDatabase);

	public abstract void prepareRawResultPage(int expId, int id, TextQuery texQuery,
			String navigationTechnique, int page);

	public abstract void prepareQueryTime(int expId, int idDatabase, TextQuery texQuery,
			int page, long time);

	public abstract void prepareExtractedResult(int experimentId, 
			String extractionTechnique, String navigationTechnique,
			String rdhName, TextQuery query, Document document, int resultPage,
			int resultIndex);

	public void prepareStoredDownloadedDocument(Document document){
		prepareStoredDownloadedDocument(document, true);
	}

	public abstract void prepareStoredDownloadedDocument(Document document, boolean success);
	
	public abstract void prepareExtractedResultPage(int experimentId, int idDatabase,
			TextQuery query, String extractionTechnique,
			String navigationTechnique, int resultPage);

	public abstract void prepareES(int idDatabase, int idExperiment, int status, int host);

	public abstract void batch();

	public abstract DocumentHandler getDocumentHandler(Database database,
			int experimentId);

	public abstract Collection<DocumentHandler> getDocumentHandler(Database database);

	public abstract void finishNegativeBatchDownloader(int idDatabase);

	public abstract int getValue(ExperimentStatusEnum status);

	public abstract Long getTextQuery(TextQuery query);

	public abstract void InitializeExperimentStatus(ExperimentEnum experiment, int idDatabase,
			String computerName);

	public abstract void reportExperimentStatus(ExperimentEnum experiment, int idDatabase,
			String computerName, int status);

	public abstract void removeInformAsNotProcessed(int searchRoundId,
			List<Integer> toRemoveQueries, Database database, String resultPageHandler,
			String NavigationHandler, String ResultHandler);

	public abstract int getExperimentConsistensyId(
			ExperimentEnum experimentConsistensyId, int split);

	public abstract void insertInteractionError(int idDatabase, int idExperiment);

	public abstract void writeExperimentSplit(int split, int idDatabase);

	public abstract void updateExperimentSplit(int split, int idDatabase);

	public abstract  void reportInteractionError(int idDatabase, int idExperiment);

	public ExperimentEnum getConsistensy(int relation) {
		//XXX need search round id navTech
		return CONSISTENSIES[relation];

	}

	public ExperimentEnum getNegativeConsistensy(int version) {
		//XXX need search round id, processedDocs, navTech, extracTech, resExtTech.
		return NEGATIVE_CONSISTENSIES[version];
		
	}

	public abstract List<Integer> loadQueriesforSampleRelation(int searchRoundId,
			Database database, String navigationTechnique, int relation, int minQuery, int lastSentQuery);

	public abstract void reportQueryConsistency(int idDatabase, int idQuery, ExperimentEnum consistensy);

	public abstract void reportNegativeQueryConsistency(int idDatabase, int idQuery,
			ExperimentEnum consistensy);

	public abstract int getLastSentQuery(int searchRoundId, Database database,
			String navigationTechnique, int relation);

	public abstract List<Integer> loadIdQueriesForTuple(int searchRoundId,
			Database database, int lastStoredQuery);

	public ExperimentEnum getTupleConsistensy(){
		return ExperimentEnum.TUPLE_CONSISTENCY;
	}

	public abstract List<Long> getProcessedDocumentsForCandidateSentences(
			Database database, int relationConf, ContentExtractor ce);

	public abstract void persistEntities();

	public abstract void prepareEntity(int idDatabase, long idDocument, ContentExtractor ce,
			int informationExtractionSystem, int entityType, int start,
			int end);

	public void prepareExtractedDocument(int idDatabase, long idDocument,
			ContentExtractor ce, int idInformationExtractor, int[] entityIds, long time) {
		
		for (int i = 0; i < entityIds.length; i++) {
			
			prepareExtractedDocument(idDatabase, idDocument, ce, idInformationExtractor, entityIds[i],time);
			
		}
		
	}

	protected abstract void prepareExtractedDocument(int idDatabase, long idDocument,
			ContentExtractor ce, int idInformationExtractor, int entityId, long time);

	public abstract void persistCandidateSentences();

	public abstract void prepareCandidateSentenceGeneration(int idDatabase, long idDocument,
			ContentExtractor ce, int relationConfigurationId, String file,
			long time, int sentences);

	public abstract void prepareGeneratedCandidateSentence(int idDatabase, long idDocument,
			int relationConfigurationId, ContentExtractor ce);

	public abstract void persistOperableStructure();

	public abstract void prepareGeneratedOperableStructure(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId);

	public abstract void prepareOperableStructureGeneration(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId,
			String file, long time);

	public abstract void persistTuple();

	public abstract void prepareExtraction(int relationExtractionSystemId,
			Document document, String file, ContentExtractor ce);

	public abstract void prepareInternalExtraction(int tuplesRelationExtractionId, Document doc, List<Tuple> t,
			long time, ContentExtractor ce);

	public abstract void finishSampleGeneration(int idSample);

	public abstract List<Pair<Long, Pair<Integer, Integer>>> getExtractedEntities(int idDatabase, long idDocument, int idEntityType,
			int idInformationExtractionSystem, ContentExtractor ce);

	public abstract Set<CandidateSentence> getGeneratedCandidateSentences(int idDatabase, long idDocument,
			int relationConf, ContentExtractor ce);

	public abstract void finishTupleExtractionFull();

	public abstract void writeRulesFromSplitModel(String collection, int relationConf, int idWorkload, int idVersion,
			int realsize, int split, boolean tuplesAsStopWords, String filePath, int idInformationExtractionSystem);

	public abstract Set<Long> getProcessedQueries(int experimentId, int idDatabase,
			String navigationTechnique);

	public abstract Map<Long, List<Document>> getQueryResultsTable(int experimentId,
			int idDatabase, String navHandler,String extractionTechnique, String resultTechnique);

	public abstract void clearExperimentSplit(Database database, int idExperiment);

	public abstract void prepareExperimentSplit(Database database, int idExperiment,
			long idDoc, int split);

	public abstract void executeExperimentSplit();

	public abstract boolean isAvailable(Database database, int idExperiment, int split);

	public abstract Set<Long> getDocumentsInSplit(Database database, int idExperiment,
			int split);

	public abstract void insertExperimentStatus(int idExperiment, int idDatabase, String computerName,
			ExperimentStatusEnum status);

	public synchronized String getFileForCandidateSentenceMap(Database database,
			int idExperiment, long idDocument) {
		
		retString =  prefix + "csMap/"+database.getId()+"/" + idExperiment + "/" + idDocument + ".csmap";
		
		return createDirectory(retString);
		
	}

	public abstract Set<Long> getDocumentsCandidateSentenceForSplits(Database database,
			int[][] entities, List<Long> processedDocuments, ContentExtractor ce, int firstRes, int lastRes, boolean queries, boolean negative, boolean tuples);

	public abstract int getIncrementalParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double threshold, Double wfp, double betaE);

	public abstract int getMSCParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double min_precision,
			double minimum_support_SVM, int k, double pow);

	public abstract int getOptimisticParameters(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double threshold, double min_weight,
			Double min_precision, double supp);

	public abstract int getQProberParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double minSVMSupport, double epsilon,
			double min_precision);

	public abstract int getRipperParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int fold, double minNo,
			int optimizationRuns, long seedValue, boolean pruning,
			boolean checkErrorRate);

	public abstract int getTupleParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, long hits_per_page,
			double querySubmissionPerUnitTime, long queryTimeConsumed,
			double ieSubmissionPerUnitTime, long ieTimeConsumed);

	public abstract int getCombinedCollaborativeFilteringParameters(boolean preserveOrder, int neighbors, String userSimilarity,
			String neightborhood, String recommender);

	public abstract int getQXtractParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int fold, double minNo,
			int optimizationRuns, long seedValue, boolean pruning,
			boolean checkErrorRate);

	public abstract List<TextQuery> getQueriesForTuplesGeneration(int searchRoundId,
			int idDatabase);

	public void saveSentencesForRelation(String collection, String relation,
			int realsize, int split, Map<String,Integer> map, String relationExtractor) {
		
        try {
        	
            ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(getFileSentencesForRelation(collection,relation,realsize,split,relationExtractor)));
            objOut.writeObject(map);
        	
			objOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private File getFileSentencesForRelation(String collection,
			String relation, int size, int split, String relationExtractor) {
		return new File(prefix + "collectionList/" + collection  + "/" + relation + "/selfsupkeygen_" + split + "_" + size + "_"+relationExtractor+".map");
	}
	
	public Map<String,Integer> getSentencesForRelation(String collection, String relation,
			int realsize, int split,String relationExtractor) {
		
		try {
			InputStream file = new FileInputStream(getFileSentencesForRelation(collection, relation, realsize, split,relationExtractor));
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput inputObj = new ObjectInputStream ( buffer );
			Map<String,Integer> map = (Map<String,Integer>)inputObj.readObject();
			inputObj.close();
			return map;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveChunksForRelations(String collection, String relation,
			int realsize, int split, Map<String, Integer> chunkMap, String relationExtractor) {
		
		try {
        	
            ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(getFileChunksForRelation(collection,relation,realsize,split,relationExtractor)));
            objOut.writeObject(chunkMap);
        	
			objOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private File getFileChunksForRelation(String collection, String relation,
			int size, int split,String relationExtractor) {
		return new File(prefix + "collectionList/" + collection  + "/" + relation + "/selfsupkeygen_" + split + "_" + size + "_" +relationExtractor+ ".chunk");
	}

	public Map<String, Integer> getChunksForRelations(String collection,
			String relation, int size, int split,String relationExtractor) {
		
		try {
			InputStream file = new FileInputStream(getFileChunksForRelation(collection,relation,size,split,relationExtractor));
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput inputObj = new ObjectInputStream ( buffer );
			Map<String,Integer> map = (Map<String,Integer>)inputObj.readObject();
			inputObj.close();
			return map;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	public void saveDiscriminativeRules(String collection, String relation,
			int split, int size, List<String> rules, Map<String, Double> score, int minSupport) {
		// They would never have a punctuation
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(getDiscriminativeRulesFile(collection,relation,split,size,minSupport)));
			
			for (int i = 0; i < rules.size(); i++) {
				
				bw.write(rules.get(i) + "," + score.get(rules.get(i)));
				
				if (i < rules.size() - 1)
					bw.newLine();
				
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private String getDiscriminativeRulesFile(String collection,
			String relation, int split, int size, int minSupport) {
		return prefix + "collectionList/" + collection  + "/" + relation + "/selfsupkeygen_" + split + "_" + size + "_" + minSupport + ".discrimRules";
	}

	public abstract int getSampleConfigurationBaseParameter(int configuration);

	public abstract String getSmallAttributeName(int idSample);

	public abstract String getLargeAttributeName(int idSample);

	public abstract List<String> getCandidateSentencesFileList();

	public abstract List<String> getOperableStructureFileList(int extractor);

	public abstract Set<Long> getDocumentsInExtractedEntities(Database database, List<Integer> entities);

	public abstract void writeTextQueryCollection(long qId, String collection);

	public abstract void writeTextQueryExtractor(long qId, int informationExtractionSystem, int relationConfiguration);

	public abstract List<Long> getDocumentsWithTuples(int informationExtractionSystem, int relationConfiguration, int idDatabase);

	public String getSignificantPhrasesFromSplitModel(String collection,
			String relation, int size, int split, boolean tasw, String extractor) {
		return createDirectory(prefix + "arffModel/" + collection + "/" + relation + "/" + tasw + "/SignificantPhrases/" + extractor + "-significantPhrases_" + "_" + split + "_" + size + ".phrases");
	}

	public abstract void writeSignificantPhrase(int ieSystem, int relConf,
			String collection, int workload, int version, int split, boolean tupleAsStopWord, int position,
			TextQuery qq);

	public abstract Map<Long, Pair<Integer, String>> getCandidateSentencesMap(
			Database database, int relationConf, ContentExtractor ce,
			Set<Long> documents);

	public abstract List<List<String>> loadNotProcessedHurry(Database database,
			int relationConf);

	public abstract List<Long> getDocumentsBelowSplit(int db, int idExperiment, int split);

	public abstract List<Long> getNotContainingCandidateSentence(int db, int relationConf);

	public abstract List<Pair<Long, String>> getCandidateSentences(int idDatabase, int idContentExtractor, int idRelationConfiguration);

	public abstract persistentWriter createNewInstance(boolean copyTextQueries);

	public abstract Map<Integer,Set<Double>> getDatabaseClusterValues(int clusterFunctionId,
			int similarityFunctionId, int idRelationshipType);

	public abstract Map<Integer, Double> getClusterValues(	int similarityFunctionId, int idRelationShipType);

	public abstract List<Database> getClusterDatbases(
			ClusterFunctionEnum clusterFunction, int idRelationshipType);

	public abstract void releaseInstance(persistentWriter pW);

	public abstract int getNextPossibleSplit(Database database, int idExperiment);

	public abstract List<String> getSignificantPhrases(int informationExtractionSystem,
			int relationConf, String collection, WorkloadModel workload,
			Version version, boolean tuplesAsStopwords, int version_seed,
			int docsInTraining);

	public abstract Map<Document, List<Tuple>> getSampleTuples(int sampleNumber);

	public abstract Map<Integer, Pair<Integer, Integer>> getUselessSamples(int idWorkload, int idVersion,
			int idExtractor, int idRelationConfiguration, int uselessDocuments,
			int idDatabase);

	public abstract void saveDoneSample(int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration,
			SampleBuilderParameters sampleBuilderParameters, int idDatabase, int idSample);

	public abstract void saveBooleanModel(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample, int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration, int idDatabase,
			String arffBooleanModel);

	public abstract void saveTrueModel(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample, int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration,int idDatabase,
			String arffBooleanModel);

	public abstract List<String> getBooleanModelFiles();

	public abstract int getSignificantPhrases(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int ngrams, ContentExtractor ce,
			int generatedQueries);

	public abstract Document getDocument(Database databse, int experimentId, long idDocument);

	public abstract int getSVMWordParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate);

	public abstract Set<Integer> getDoneSamples(int idSampleBuilder, int idDatabase,
			int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration);

	public abstract TextQuery getTextQueryFromId(long query);

	public abstract List<Long> getQueriesUsedToGenerateNegativeSample(int idExperiment, Database database, int split, String navHandler, String extTechnique, String resExtTechnique);

	public abstract List<Integer> getSampleConfiguration(int sampleAlgorithm,
			int workload, int relationConfiguration, boolean countAll,
			int sampleGenerator, boolean useAll, int version,
			int queryPoolExecutor, int documentPerQuery, int maxQueries,
			int uselessCount, int idInformationExtraction);

//	public abstract List<Integer> getSampleConfiguration(int sampleAlgorithm, int workload,
//			int relationConfiguration, boolean countAll, int sampleGenerator, boolean useAll,
//			int version, int queryPoolExecutor, int documentPerQuery,
//			int uselessCount, int informationExtractionSystem);

	
}
