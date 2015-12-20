package utils.persistence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;

import org.apache.axis.encoding.Base64;

import plot.generator.SeriesGenerator;

import sample.generation.factory.QueryPoolExecutorFactory;
import sample.generation.factory.QueryPoolFactory;
import sample.generation.factory.SampleExecutorFactory;
import sample.generation.factory.SampleGeneratorFactory;
import sample.generation.factory.WordExtractorFactory;
import sample.generation.model.SampleBuilderParameters;
import sample.generation.model.SampleConfiguration;
import sample.generation.model.SampleGenerator;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.impl.DummySampleConfiguration;
import sample.generation.model.queryPool.QueryPool;
import sample.generation.model.queryPool.impl.DummyQueryPool;
import searcher.interaction.formHandler.TextQuery;
import utils.document.DocumentHandler;
import weka.attributeSelection.ASEvaluation;

import com.google.gdata.util.common.base.Pair;

import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceReader;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceWriter;
import edu.columbia.cs.ref.tool.io.CoreWriter;
import execution.model.factory.AdaptiveStrategyFactory;
import execution.model.factory.AlgorithmSelectionFactory;
import execution.model.factory.ContentExtractorFactory;
import execution.model.factory.ContentLoaderFactory;
import execution.model.factory.DatabaseSelectionFactory;
import execution.model.factory.FinishingStrategyFactory;
import execution.model.factory.GenerationFactory;
import execution.model.factory.InformationExtractionFactory;
import execution.model.factory.InteractionPersisterFactory;
import execution.model.factory.SourceFactory;
import execution.model.factory.StatisticsForSampleSelectorFactory;
import execution.model.factory.UpdateStrategyFactory;
import execution.model.parameters.Parametrizable;
import execution.model.parameters.StringParameters;
import execution.model.parameters.TableParameters;
import execution.model.policy.ExecutionPolicy;
import execution.model.policy.LimitedNumberPolicy;
import execution.model.scheduler.Scheduler;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.Algorithm;
import exploration.model.Combination;
import exploration.model.Configuration;
import exploration.model.Database;
import exploration.model.DatabasesModel;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Execution;
import exploration.model.ExecutionAlternative;
import exploration.model.Query;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.clusterfunction.ClusterFunction;
import exploration.model.database.ClusterDatabase;
import exploration.model.database.GlobalDatabase;
import exploration.model.database.LocalDatabase;
import exploration.model.database.OnlineDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;
import exploration.model.enumerations.AlgorithmEnum;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;
import exploration.model.enumerations.QueryStatusEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;
import extraction.relationExtraction.RelationExtractionSystem;

public class databaseWriter extends persistentWriter {

	public static final String DATABASE_USER = "user";
	public static final String DATABASE_PASSWORD = "password";
	public static final String MYSQL_AUTO_RECONNECT = "autoReconnect";
	public static final String MYSQL_MAX_RECONNECTS = "maxReconnects";

	private static final String getDatabaseString = "SELECT D.Name, D.size, D.Class, D.ModelType, D.isGlobal, D.isCluster, D.isLocal, D.`Index`" +
			" FROM `Database` D WHERE D.idDatabase = ?";
	private static final String getCombinationString = "SELECT C.*  FROM Combination C WHERE C.idCombination = ?";
	private static final String versionDescriptionString = "SELECT V.idVersion, V.Description FROM Version V WHERE V.idVersion = ?";

	private static final String insertDocumentString = ("INSERT INTO `Document` (`idDatabase`,`idDocument`, `path`) VALUES (?,?,?)");
	private static final String insertExtractionTimeString = ("INSERT INTO `ExtractionTime` (`databaseId`,`docId`, extractionSystem, `time`) VALUES (?,?,?,?)");

	private static String insertExtractionString = ("INSERT INTO `AutomaticQueryGeneration`.`Extraction` (`idExtractionSystem`, `idDatabase`, `docId`, `fileAuxiliar`, `contentExtractor`) VALUES ( ?, ?, ?, ?, ?); ");

	private static final String insertRetrievedURLString = ("INSERT INTO `AutomaticQueryGeneration`.`WebDocument` (`idDatabase`, `idExperiment`, `idDocument`, `filePath`, `URL`, `downloadTime`, `success`) VALUES ( ?, ?, ?, ?, ?, ?, ?)");

	private static final String insertSampleGeneration = ("INSERT INTO `AutomaticQueryGeneration`.`SampleGeneration` (`idSample`, " +
			"`query_generated_position`, `query_submitted_position`, `query_round`, `document_position`,`doc_in_query_position`, `document_position_in_sample`, `usefulTuples`, `documentDatabase`, `documentId`) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

	private static final String insertSampleGenerationQuery = ("INSERT INTO `AutomaticQueryGeneration`.`SampleGenerationQueries` (`idSample`, `queried_db`, `query_generated`,`query`) VALUES ( ?, ?, ?, ?) ");

	private static final String insertSampleString = ("INSERT INTO `AutomaticQueryGeneration`.`Sample` (`idDatabase`, `idVersion`, `idWorkload`, `version_seed_pos`,`version_seed_neg`, " +
			"`idSampleConfiguration`) VALUES (?, ?, ?, ?, ?, ?); ");

	private static final String resDescriptionString = "SELECT IE.enumerationName,IE.name FROM RelationExtractionSystem IE WHERE IE.idInformationExtractionSystem = ? and IE.idRelationConfiguration = ?";


	private static final String sampleDetails = "SELECT idDatabase, idVersion, idWorkload, version_seed_pos,version_seed_neg, idSampleConfiguration FROM Sample where idSample = ?";

	private static final String ParameterIdString = "SELECT idParameter FROM QueryPoolExecutor where idQueryPoolExecutor = ?";
	private static final String ParameterSGIdString = "SELECT idParameter FROM SampleGenerator where idSampleGenerator = ?";

	private String getQueriesBatchLastExecutedQueryString = "SELECT status from `AutomaticQueryGeneration`.`ExperimentStatus` where idExperiment = ? and idDatabase = ?";
	private String getQueriesBatchNegativeLastExecutedQueryString = "select queryId FROM `AutomaticQueryGeneration`.`NegativeSampling` where idExperimentId = ? and idDatabase = ? and split = ? and processedDocs = ? and navigationTechnique = ? and extractionTechnique = ? and resultExtractionTechnique = ? order by position";
	private String reportQueryForTupleSentString = "INSERT INTO `AutomaticQueryGeneration`.`QueryForTupleLog` (`idExperiment`, `idDatabase`, `position`, `extractionTechnique`, `navigationTechnique`, `resultExtractionTechnique`, `idQuery`) VALUES ( ?, ?, ?, ?, ?, ?, ?)";


	private static final int ERROR_VALUE = -1;
	private static final int RUNNING_VALUE = -2;
	private static final int FINISHED_VALUE = -3;
	private static final int PARTIALLY_FINISHED = -4;
	private static final int INCONSISTENT = -5;

	//idExperiment
	private static final int CONSISTENSY_VALUE_1 = -1;
	private static final int CONSISTENSY_VALUE_2 = -2;
	private static final int CONSISTENSY_VALUE_3 = -3;
	private static final int CONSISTENSY_VALUE_4 = -4;
	private static final int CONSISTENSY_VALUE_5 = -5;
	private static final int CONSISTENSY_VALUE_6 = -6;

	//idExperiment
	private static final int QUERYING_VALUE = -7; 

	//idExperiment
	private static final int TUPLE_CONSISTENCY_VALUE = -8;

	//idExperiment
	private static final int NEGATIVE_CONSISTENSY_VALUE_1 = -11;
	private static final int NEGATIVE_CONSISTENSY_VALUE_2 = -12;
	private static final int NEGATIVE_CONSISTENSY_VALUE_3 = -13;
	private static final int NEGATIVE_CONSISTENSY_VALUE_4 = -14;
	private static final int NEGATIVE_CONSISTENSY_VALUE_5 = -15;


	private String getNextSampleNumberString = ("SELECT MAX(sample_number) FROM `AutomaticQueryGeneration`.`Sample` where idSampleConfiguration = ? and idDatabase = ? and idVersion = ? and idWorkload = ?");

	private String sampleId = "SELECT idSample FROM Sample where idSampleConfiguration = ? and idDatabase = ? and version_seed_pos = ? and version_seed_neg = ?";

	private String sampleGenerationConstrained = "SELECT S.query_generated_position, S.query_submitted_position, S.query_round, S.document_position,S.doc_in_query_position,S.document_position_in_sample,S.usefulTuples,S.documentDatabase,S.documentId from SampleGeneration S where idSample = ? and ( S.document_position < ? or (S.document_position = ? and S.document_position_in_sample = ?)) order by S.query_submitted_position,S.document_position";

	private String sampleGenerationNotConstrained = "SELECT S.query_generated_position, S.query_submitted_position, S.query_round, S.document_position,S.doc_in_query_position,S.document_position_in_sample,S.usefulTuples,S.documentDatabase,S.documentId from SampleGeneration S where idSample = ? order by S.query_submitted_position,S.document_position";

	private String sampleGenerationQueries = "SELECT S.query_generated, S.query FROM SampleGenerationQueries S where S.idSample = ? and S.query_generated <= ? order by query_generated";

	private String getDocumentPositionString = "SELECT S.document_position from SampleGeneration S where idSample = ? and S.document_position_in_sample = ?";

	private String insertwriteTextQuery = "insert into TextQuery(`Text`) values(?)";

	private String saveExtractedResultString = "INSERT INTO `AutomaticQueryGeneration`.`QueryResults`(`idExperiment`,`idDatabase`,`idQuery`,`idDocument`,`page`,`position`,`extractionTechnique`,`navigationTechnique`,`resultExtractionTechnique`)VALUES(?,?,?,?,?,?,?,?,?)";

	private String getProcessedPagesString = "SELECT count(*) from RawResultPage where idExperiment = ? and idDatabase = ? and idQuery = ? and navigationTechnique = ?";

	private String getExtractedResultsString = "SELECT COUNT(*) from QueryResults Q where Q.idExperiment = ? and Q.idDatabase = ? and Q.idQuery = ? and Q.position >= 0 and Q.page = ? and Q.extractionTechnique = ? and Q.navigationTechnique = ? and Q.resultExtractionTechnique = ?";

	private String getQueryResultsString = "SELECT Q.idDocument from QueryResults Q where Q.idExperiment = ? and Q.idDatabase = ? and Q.idQuery = ? and Q.position >= 0 and Q.page = ? and Q.extractionTechnique = ? and Q.navigationTechnique = ? and Q.resultExtractionTechnique = ? order by Q.position";

	private String insertQueryTimeString = "INSERT INTO `AutomaticQueryGeneration`.`QueryTime`(`idExperiment`,`idDatabase`,`idQuery`,`idPage`,`time`)VALUES(?,?,?,?,?)";

	private String reportStatus = "UPDATE `AutomaticQueryGeneration`.`HostDatabase` SET `queryStatus` = ? WHERE `idDatabase` = ? and `host` = ?";

	private String reportExperimentStatus = "UPDATE `AutomaticQueryGeneration`.`ExperimentStatus` SET `status` = ?, computerName = ? WHERE `idDatabase` = ? and `idExperiment` = ?";

	private String hasExtractedEntitiesString = "SELECT * From `ExtractedEntity` where idDatabase = ? and idDocument = ? and idContentExtractionSystem = ? and idInformationExtractionSystem = ? and idEntityType = ?";

	private String getExtractedEntitiesString = "SELECT idEntity,start,end From `Entity` where idDatabase = ? and idDocument = ? and idContentExtractionSystem = ? and idInformationExtractionSystem = ? and idEntityType = ?";

	private String hasGeneratedCandidateSentenceString = "SELECT * From `GeneratedCandidateSentence` where idDatabase = ? and idDocument = ? and idContentExtractionSystem = ? and idRelationConfiguration = ? ";

	private String getGeneratedCandidateSentenceString = "SELECT file From `CandidateSentence` where idDatabase = ? and idDocument = ? and idContentExtractionSystem = ? and idRelationConfiguration = ? ";

	private String saveExtractedEntityString = "INSERT INTO `AutomaticQueryGeneration`.`ExtractedEntity` (`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idInformationExtractionSystem`,`idEntityType`,`time`) VALUES (?,?,?,?,?,?)";

	private String saveGeneratedCandidateSentenceString = "INSERT INTO `AutomaticQueryGeneration`.`GeneratedCandidateSentence`(`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idRelationConfiguration`) VALUES (?,?,?,?)";

	private String saveEntityString = "INSERT INTO `AutomaticQueryGeneration`.`Entity`(`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idInformationExtractionSystem`,`idEntityType`,`start`,`end`)VALUES(?,?,?,?,?,?,?)";

	private String saveCandidateSentenceGenerationString = "INSERT INTO `AutomaticQueryGeneration`.`CandidateSentence`(`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idRelationConfiguration`,`file`,`time`,`size`)VALUES(?,?,?,?,?,?,?)";

	private String writeTextQueryEntityString = "INSERT INTO `AutomaticQueryGeneration`.`TextQueryEntityType`(`idTextQuery`,`idEntityType`) VALUES (?,?)";

	private String writeTextQueryRelationConfString = "INSERT INTO `AutomaticQueryGeneration`.`TextQueryRelationConfiguration`(`idTextQuery`,`idRelationConfiguration`) VALUES (?,?)";

	private String insertExperimentOnDatabaseString = "INSERT INTO `AutomaticQueryGeneration`.`ExperimentStatus`(`idDatabase`,`idExperiment`) VALUES (?,?)";

	private String getAllCandidateSentencesMapString = "SELECT C.idDocument,C.file,C.size from CandidateSentence C where C.idDatabase = ? " +
			"and C.idRelationConfiguration = ? and C.idContentExtractionSystem = ?";

	private String getCandidateSentencesMapString = "SELECT C.idDocument,C.file,C.size from CandidateSentence C where C.idDatabase = ? " +
			"and C.idRelationConfiguration = ? and C.idContentExtractionSystem = ? and C.idDocument IN " +
			"(select Q.idDocument from QueryResults Q join (select R.idQuery from RelationshipExtractorQueries R " +
			"where R.idInformationExtractionSystem = ? and R.idRelationConfiguration = ?) as REQ on (REQ.idQuery = Q.idQuery) " +
			"where Q.idExperiment = ? and Q.idDatabase = C.idDatabase and Q.position >= 0 order by Q.position);";

	private String getQueriesIESystem = "select R.idQuery from RelationshipExtractorQueries R " +
			"where R.idInformationExtractionSystem = ? and R.idRelationConfiguration = ?";

	private String getNegativeQueriesIESystem = "select N.queryId from NegativeSampling N where N.idDatabase = ?";

	private String getForTupleQueriesIESystem = "select T.idQuery from QueryForTuples T where T.idDatabase = ?";

	private String getCandidateSentencesNegativeMapString = "SELECT C.idDocument,C.file,C.size from CandidateSentence C where C.idDatabase = ? " +
			"and C.idRelationConfiguration = ? and C.idContentExtractionSystem = ? and C.idDocument IN " +
			"(select Q.idDocument from QueryResults Q join (select N.queryId from NegativeSampling N " +
			"where N.idDatabase = ? ) as REQ on (REQ.queryId = Q.idQuery) " +
			"where Q.idExperiment = ? and Q.idDatabase = C.idDatabase and Q.position >= 0 order by Q.position);";

	private String getCandidateSentencesTuplesMapString = "SELECT C.idDocument,C.file,C.size from CandidateSentence C where C.idDatabase = ? " +
			"and C.idRelationConfiguration = ? and C.idContentExtractionSystem = ? and C.idDocument IN " +
			"(select Q.idDocument from QueryResults Q where Q.idQuery IN (select T.idQuery from QueryForTuples T " +
			"where T.idDatabase = ?) and Q.idExperiment = ? and Q.idDatabase = C.idDatabase and Q.position >= 0 order by Q.position);";

	private String getProcessedCandidateSentencesString = "SELECT idDocument from GeneratedOperableStructure where idDatabase = ? and idRelationConfiguration = ? and idContentExtractionSystem = ? and idInformationExtractionSystem = ?";

	private String saveGeneratedOperableStructureString = "INSERT INTO `AutomaticQueryGeneration`.`GeneratedOperableStructure`(`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idRelationConfiguration`,`idInformationExtractionSystem`)VALUES(?,?,?,?,?)";

	private String saveOperableStructureGenerationString = "INSERT INTO `AutomaticQueryGeneration`.`OperableStructure`(`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idRelationConfiguration`,`idInformationExtractionSystem`,`file`,`time`)VALUES(?,?,?,?,?,?,?)";

	private String insertRelationExperimentString = "INSERT INTO `AutomaticQueryGeneration`.`RelationExperiment`(`idRelationConfiguration`,`idInformationExtractionSystem`)VALUES(?,?)";

	private String isExperimentAvailableString = "INSERT INTO `AutomaticQueryGeneration`.`ExperimentStatus`(`idDatabase`,`idExperiment`,`computerName`)VALUES(?,?,?);";

	private String hasGeneratedQueriesString = "INSERT INTO AlgorithmOnSample (idAlgorithm,idSample,idSampleParameters) VALUES (?,?,?)";

	private String writeExtractionPerformanceString = "INSERT INTO `AutomaticQueryGeneration`.`NETPerformance`(`relation`,`technique`,`split`,`truePositive`,`falsePositive`,`falseNegative`,`precision`,`recall`,`fmeas`,`type`)VALUES(?,?,?,?,?,?,?,?,?,?)";

	private String saveCorefEntityString = "INSERT INTO `AutomaticQueryGeneration`.`CorefEntity`(`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idInformationExtractionSystem`,`idEntityType`,`idRootEntity`,`start`,`end`,`time`)VALUES(?,?,?,?,?,?,?,?,?)";

	private String writeNegativeSampleEntryString = "INSERT INTO `AutomaticQueryGeneration`.`NegativeSampling`(`idExperimentId`,`idDatabase`,`split`,`processedDocs`,`position`,`queryId`,`navigationTechnique`,`extractionTechnique`,`resultExtractionTechnique`)VALUES(?,?,?,?,?,?,?,?,?)";

	private String saveCoreferenceResolutionString = "INSERT INTO `AutomaticQueryGeneration`.`CoreferenceResolution`(`idDatabase`,`idDocument`,`idContentExtractionSystem`,`idInformationExtractionSystem`,`idEntityType`,`time`)VALUES(?,?,?,?,?,?)";

	private String hasDoneCoreferenceResolutionString = "SELECT * FROM `AutomaticQueryGeneration`.`CoreferenceResolution` where idDatabase = ? " +
			"and idDocument = ? and idContentExtractionSystem = ? and idInformationExtractionSystem = ? and idEntityType = ?";

	private String getQueriesForNegativeSampleString = "Select text from `AutomaticQueryGeneration`.`NegativeSampling` join TextQuery  on (queryId = idQuery) where idExperimentId = ? and processedDocs = ? and idDatabase = ? and split = ? order by position";

	private String getRelationExtractionSystemIdString = "Select idRelationExtractionSystem from RelationExtractionSystem where idRelationConfiguration = ? and idInformationExtractionSystem = ?";

	private String writeGeneratedQueriesStatus = "UPDATE `AutomaticQueryGeneration`.`AlgorithmOnSample` SET  `status` = ? WHERE `idAlgorithm` = ? and `idSample` = ? and `idSampleParameters` = ? ";

	private String insertQueryForTuplesGenerationString = "INSERT INTO `AutomaticQueryGeneration`.`QueryForTuples`  (`idExperiment`,  `idDatabase`,  `position`,  `idQuery`)  VALUES  (  ?,  ?,  ?,  ?  )";

	private String loadQueriesForTupleString = "SELECT T.Text FROM QueryForTuples Q join TextQuery T on (Q.idQuery = T.idQuery) where idExperiment = ? and idDatabase = ? order by position";

	private String loadIdQueriesForTupleString = "select idQuery from QueryForTuples where idExperiment = ? and idDatabase = ? and position > (select position from QueryForTuples where idExperiment = ? and idDatabase = ? and idQuery = ?) order by position";

	private String saveRawResultPageString = "INSERT INTO `AutomaticQueryGeneration`.`RawResultPage` (`idExperiment`, `idDatabase`, `idQuery`, `navigationTechnique`, `page`) VALUES (?, ?, ?, ?, ?)";

	private String saveExtractedResultPageString = "INSERT INTO `AutomaticQueryGeneration`.`ExtractedResultPage` (`idExperiment`, `idDatabase`, `idQuery`, `idExtractionTechnique`, `idNavigationTechnique`, `resultPage`) VALUES ( ?, ?, ?, ?, ?, ?)";

	private String hasReachedPageString = "select idExperiment from `AutomaticQueryGeneration`.`RawResultPage` where idExperiment = ? and idDatabase = ? and idQuery = ? and navigationTechnique = ? and page = ?";

	private String hasExtractedPageString = "select idExperiment from `AutomaticQueryGeneration`.`ExtractedResultPage` where idExperiment = ? and idDatabase = ? and idQuery = ? and idExtractionTechnique = ? and idNavigationTechnique = ? and resultPage = ?";

	private String getQueriesBatchTuplesLastExecutedQueryString = "select idQuery FROM `AutomaticQueryGeneration`.`QueryForTupleLog` where idExperiment = ? and idDatabase = ? and extractionTechnique = ? and navigationTechnique = ? and resultExtractionTechnique = ? order by position DESC LIMIT 0,1";

	private String saveSampleDocumentsString = "INSERT INTO `AutomaticQueryGeneration`.`SampleDocument` (`idSample`, `isUseful`, `position`, `idDatabase`, `idDocument`) VALUES (?,?,?,?,?);";

	private String writeRelationKeywordString = "INSERT INTO `AutomaticQueryGeneration`.`RelationKeywords` (`idInformationExtractionSystem`,`idRelationConfiguration`,`collection`,`idWorkload`, `idVersion`, `split`, `tuplesAsStopWords`, `ASEvaluation`, `position`, `docsInTraining`, `idQuery`) VALUES (?,?,?,?,?,?,?,?,?,?,?);";

	private String saveSampleTuplesString = "INSERT INTO `AutomaticQueryGeneration`.`SampleTuples` (`idSample`, `tupleNumber`, `idDatabase`,`idDocument`,`tupleString`) VALUES (?,?,?,?,?);";

	private String getRelationKeywordsString = "SELECT Text FROM TextQuery T join RelationKeywords R on (T.idQuery = R.idQuery) where idInformationExtractionSystem = ? and idRelationConfiguration = ? and collection = ? and idWorkload = ? and idVersion = ? and split = ? and tuplesAsStopWords = ? and ASEvaluation = ? and docsInTraining = ? order by position";

	private String getSampleDocumentsString = "SELECT idDatabase, idDocument FROM AutomaticQueryGeneration.SampleDocument where idSample = ? and ((isUseful = 1 AND position <= ?) OR (isUseful = 0 AND position <= ?)) order by isUseful DESC, position";

	private String getTuplesString = "SELECT idDatabase,idDocument,tupleString FROM AutomaticQueryGeneration.SampleTuples where idSample = ?";

	private String getSampleUsefulDocumentsString = "SELECT idDatabase, idDocument FROM AutomaticQueryGeneration.SampleDocument where idSample = ? and isUseful = 1 and position <= ? order by position";

	private String getSampleFilteredDocumentsString = "SELECT document FROM AutomaticQueryGeneration.SampleFilteredDocuments where idSample = ? and idSampleParameter = ? order by position";

	private String getSampleBuilderParametersString = "SELECT * FROM AutomaticQueryGeneration.SampleParameters where idSampleParameters = ?";

	private String writeFilteredDocumentsString = "INSERT INTO `AutomaticQueryGeneration`.`SampleFilteredDocuments` (`idSample`, `lowercase`, `stemmed`, `unique`, `tuplesAsStopWords`, `uselessSample`, `usefulDocuments`, `uselessDocuments`, `position`, `idDatabase`,`idDocument` ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?);";

	private String hasGeneratedSampleString = "INSERT INTO `AutomaticQueryGeneration`.`GeneratedSample` (`idSample`, `idSampleBuilderParameters`, `idDatabase`) VALUES ( ?, ?, ? )";

	private String hasGeneratedFullSampleString = "INSERT INTO `AutomaticQueryGeneration`.`GeneratedFullSample` (`idSample`, `unique`, `lowercase`, `stemmed`) VALUES ( ?, ?, ?, ?)";

	private String getAllSamplesString = "select S.idSample from Sample S where idDatabase = ? and idVersion = ? and idWorkload = ? and usefulDocuments >= ? and idSampleConfiguration IN (select idSampleConfiguration from SampleConfiguration where idVersion = ? and idWorkload = ? and idRelationConfiguration = ? and idExtractionSystem = ? and usefulNumber >= ? and uselessNumber = 0)";

	//private String getUselessSamplesString = "select S.idSample from Sample S where idDatabase = ? and idVersion = ? and idWorkload = ? and uselessDocuments >= ? and idSampleConfiguration IN (select idSampleConfiguration from SampleConfiguration where idVersion = ? and idWorkload = ? and idRelationConfiguration = ? and idExtractionSystem = ? and usefulNumber = 0 and uselessNumber >= ?)";
	
	private String getUselessSamplesString = "select SC.resultsPerQuery, S.idSample, S.version_seed_neg from Sample S join SampleConfiguration SC on (S.idVersion = SC.idVersion and S.idWorkload = SC.idWorkload and S.idSampleConfiguration = SC.idSampleConfiguration) where S.idDatabase = ? and S.idVersion = ? and S.idWorkload = ? and S.uselessDocuments >= ? and SC.idRelationConfiguration = ? and SC.idExtractionSystem = ? and SC.usefulNumber = 0 and SC.uselessNumber >= ? order by SC.resultsPerQuery, S.version_seed_neg";
	
	private String getDoneSamplesString = "SELECT idSample FROM AutomaticQueryGeneration.GeneratedSample where idDatabase = ? and idSampleBuilderParameters = ? and idWorkload = ? and idRelationConfiguration = ? and idVersion = ? and idInformationExtractionSystem = ?";

	private String writeSampleConfigurationsString = "INSERT INTO `AutomaticQueryGeneration`.`SampleConfiguration` (`idParameter`, `idVersion`, `idWorkload`,`idRelationConfiguration`, `idExtractionSystem`, `idQueryPoolExecutor`, `idSampleGenerator`, `use_all`, `active`, `resultsPerQuery`, `usefulNumber`, `uselessNumber`, `maxQueries`, `maxDocuments`, `CountsAll`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?);";

	private String writeInternalTuplesString = "INSERT INTO `AutomaticQueryGeneration`.`InternalTupleExtraction` (`idDatabase`, `idExtractionSystem`, `time`, `content`) VALUES ( ?, ?, ?, ? )";

	private String getAttributeWithLargeDomainString = "select attributeTupleLargeDomain from RelationAttributeDomains where idRelationshipType = ?";

	private String getAttributeWithSmallDomainString = "select attributeTupleSmallDomain from RelationAttributeDomains where idRelationshipType = ?";

	private String selectQueryForTuplesGenerationString = "SELECT text from `AutomaticQueryGeneration`.`QueryForTuples` natural join `AutomaticQueryGeneration`.`TextQuery`  where `idExperiment` = ? and `idDatabase` = ? order by `position`";

	private String reportInteractionErrorString = "UPDATE `AutomaticQueryGeneration`.`InteractionError` SET `numberOfErrors` = `numberOfErrors` + 1 WHERE `idExperiment` = ? and `idDatabase` = ?";

	private String reportQueryConsistencyString = "INSERT INTO `AutomaticQueryGeneration`.`QueryConsistency` (`idDatabase`, `idQuery`, `idConsistency`) VALUES (?, ?, ?)";

	private String reportNegativeQueryConsistencyString = "INSERT INTO `AutomaticQueryGeneration`.`NegativeConsistency` (`idDatabase`, `idQuery`, `idConsistency`) VALUES (?, ?, ?)";

	private String testTableString = "INSERT INTO `AutomaticQueryGeneration`.`TestTable` (`idCol`,`col1`, `col2`, `col3`) VALUES (?,?, ?, ?);";

	private String getProcessedDocumentsForCandidateSentencesString = "SELECT idDocument From `GeneratedCandidateSentence` where idDatabase = ? and idContentExtractionSystem = ? and idRelationConfiguration = ? ";

	private String getQueryResultsTableString = "SELECT Q.idQuery,Q.idDocument from QueryResults Q where Q.idExperiment = ? and Q.idDatabase = ? and Q.position >= 0 and Q.extractionTechnique = ? and Q.navigationTechnique = ? and Q.resultExtractionTechnique = ? order by Q.page,Q.position";

	private String getProcessedQueriesString = "select idQuery from `AutomaticQueryGeneration`.`RawResultPage` where idExperiment = ? and idDatabase = ? and navigationTechnique = ? and page = 0";


	private Connection conn;
	//	private int stored_query;

	private Hashtable<Integer,DatabasesModel> model = null;
	private Hashtable<Integer, Database> databasesTable = new Hashtable<Integer, Database>();
	private Hashtable<Integer, Algorithm> algorithmsTable = null;
	private Hashtable<Integer, Configuration> configurationTable = null;
	private Hashtable<Integer, Combination> combinationsTable = null;
	private Hashtable<Integer, WorkloadModel> workloadModelTable = null;
	private Hashtable<Integer, List<Query>> queriesTable = null;
	private Hashtable<Integer, String> versionDescriptionTable;

	private ResultSet RSgetSampleConfigurationId;
	//	private PreparedStatement PStmtinsertSampleGeneration;
	//	private PreparedStatement PStmtinsertSampleGenerationQuery;

	private Map<ClusterFunctionEnum, Integer> cachedClusterFunction = new HashMap<ClusterFunctionEnum, Integer>();
	private PreparedStatement PStmtinsertExtraction;
	private Map<TextQuery, Long> textquerytable = new HashMap<TextQuery, Long>();

	private PreparedStatement PStmtsaveEntity;
	private Statement StmtgetRelationTypeTable;
	private ResultSet RSgetRelationTypeTable;
	private PreparedStatement PStmtsaveExtractedEntity;
	private PreparedStatement PStmtsaveGeneratedCandidateSentence;
	private PreparedStatement PStmtsaveCandidateSentenceGeneration;

	private PreparedStatement PStmtsaveGeneratedOperableStructure;
	private PreparedStatement PStmtsaveOperableStructureGeneration;

	private ResultSet RSgetRESInformationExtractionSystem;
	private Map<String, Integer> resultExtractionTechniqueTable = new HashMap<String, Integer>();
	private Map<String, Integer> navigatonTechniqueTable = new HashMap<String, Integer>();
	private Map<String, Integer> extractionTechniqueTable = new HashMap<String, Integer>();

	private Map<String, Integer> relationshipTable = new HashMap<String, Integer>();

	private ResultSet RSgetSampleUsefulDocuments;
	private Map<Integer, SampleBuilderParameters> sampleBuilders = new HashMap<Integer, SampleBuilderParameters>();

	private String getInternalTupleMapString = "select idDatabase,content from InternalTupleExtraction";
	private PreparedStatement PStmtprepareES;
	private String getTextQueryTableString = "Select idQuery, Text from TextQuery where idQuery > ?";
	private Map<Integer, Map<Integer, DocumentHandler>> dhMap = new HashMap<Integer, Map<Integer,DocumentHandler>>();
	private Map<Integer, PreparedStatement> prepareExtractedResultPageTable = new HashMap<Integer, PreparedStatement>();
	private Map<Integer, PreparedStatement> prepareStoredDownloadedDocumentTable = new HashMap<Integer, PreparedStatement>();
	private Map<Integer, PreparedStatement> prepareExtractedResultTable = new HashMap<Integer, PreparedStatement>();
	private Map<Integer, PreparedStatement> prepareQueryTimeTable = new HashMap<Integer, PreparedStatement>();
	private Map<Integer, PreparedStatement> prepareRawResultPage = new HashMap<Integer, PreparedStatement>();
	private Map<String, Integer> contentExtractionTable = new HashMap<String, Integer>();
	private Map<Integer, PreparedStatement> prepareNegativeSampleEntryTable = new HashMap<Integer, PreparedStatement>();

	private List<Object[]> internalData = new ArrayList<Object[]>();
	private PreparedStatement PStmtwriteInternalTuplesIds;
	private Map<Integer, Double> cachedThreshold = new HashMap<Integer, Double>();
	private Map<Integer, PreparedStatement> isgqTable = new HashMap<Integer, PreparedStatement>();
	private Map<Integer, PreparedStatement> tpdTable = new HashMap<Integer, PreparedStatement>();
	private String prepareExperimentSplitString = "INSERT INTO `AutomaticQueryGeneration`.`SplitDocsToProcess` " +
			"(`idDatabase`, `idExperiment`, `split`, `idDocument`) VALUES ( ?, ?, ?, ? ); ";
	private PreparedStatement PStmtprepareExperimentSplit;
	private String isAvailableString = "INSERT INTO `AutomaticQueryGeneration`.`SplitsToProcess` (`idDatabase`, `idExperiment`, `split`) VALUES ( ?, ?, ? );";
	private String getDocumentsInSplitString = "SELECT idDocument from SplitDocsToProcess where idDatabase = ? and idExperiment = ? and split = ?";
	private String insertExperimentStatusString = "INSERT INTO `AutomaticQueryGeneration`.`ExperimentStatus` (`idDatabase`, `idExperiment`, `computerName`, `status`) VALUES ( ?, ?, ?, ? );";
	private long maxIdQuery = -1;
	private String writeQueriesString = "INSERT INTO Query (idCombination, Position, query, time) VALUES (?,?,?,?);";
	private Map<Long, TextQuery> idtextquerytable = new HashMap<Long, TextQuery>();
	
	private String getIncrementalParameterString = "SELECT `Incremental`.`idIncremental` FROM `AutomaticQueryGeneration`.`Incremental` WHERE `Incremental`.`maxQuerySize` = ? and  `Incremental`.`minSupport` = ? and  `Incremental`.`minSuppAfterUpdate` = ? and  `Incremental`.`threshold` = ? and  `Incremental`.`wfp` = ? and  `Incremental`.`betaE`  = ?";
	private String getMSCParameterString = "SELECT `MSC`.`idMSC` FROM `AutomaticQueryGeneration`.`MSC` WHERE `MSC`.`maxQuerySize`= ? and  `MSC`.`minSupport`= ? and  `MSC`.`minSuppAfterUpdate`= ? and  `MSC`.`min_precision`= ? and  `MSC`.`min_support_SVM`= ? and  `MSC`.`k`= ? and  `MSC`.`pow` = ?";
	private String getOptimisticParametersString = "SELECT `Optimistic`.`idOptimistic` FROM `AutomaticQueryGeneration`.`Optimistic` WHERE `Optimistic`.`maxQuerySize` = ? and `Optimistic`.`minSupport` = ? and `Optimistic`.`minSuppAfterUpdate` = ? and `Optimistic`.`threshold` = ? and `Optimistic`.`min_weight` = ? and `Optimistic`.`min_precision` = ? and `Optimistic`.`supp` = ?";
	private String getQProberParameterString = "SELECT `QProber`.`idQProber` FROM `AutomaticQueryGeneration`.`QProber` where `QProber`.`maxQuerySize` = ? and `QProber`.`minSupport` = ? and `QProber`.`minSuppAfterUpdate` = ? and `QProber`.`minSVMSupport` = ? and `QProber`.`epsilon` = ? and `QProber`.`min_precision` = ?";
	private String getSVMWordParameterString = "SELECT `SVMWord`.`idSVMWord` FROM `AutomaticQueryGeneration`.`SVMWord` where `SVMWord`.`maxQuerySize` = ? and `SVMWord`.`minSupport` = ? and `SVMWord`.`minSuppAfterUpdate` = ?";
	private String getRipperParameterString = "SELECT `Ripper`.`idRipper` FROM `AutomaticQueryGeneration`.`Ripper` WHERE `Ripper`.`maxQuerySize`= ? and `Ripper`.`minSupport`= ? and `Ripper`.`minSuppAfterUpdate`= ? and `Ripper`.`fold`= ? and `Ripper`.`minNo`= ? and `Ripper`.`optimizationRuns`= ? and `Ripper`.`seedValue`= ? and `Ripper`.`pruning`= ? and `Ripper`.`checkErrorRate`= ?";
	private String getQXtractParameterString = "SELECT `QXtract`.`idQXtract` FROM `AutomaticQueryGeneration`.`Ripper` WHERE `Ripper`.`maxQuerySize`= ? and `Ripper`.`minSupport`= ? and `Ripper`.`minSuppAfterUpdate`= ? and `Ripper`.`fold`= ? and `Ripper`.`minNo`= ? and `Ripper`.`optimizationRuns`= ? and `Ripper`.`seedValue`= ? and `Ripper`.`pruning`= ? and `Ripper`.`checkErrorRate`= ?";
	private String getTupleParameterString;
	private String getSignificanPhraseParameterString = "SELECT `SignificantPhrasesParameter`.`idSignificantPhrasesParameter` FROM `AutomaticQueryGeneration`.`SignificantPhrasesParameter` WHERE  `SignificantPhrasesParameter`.`maxQuerySize` = ? and `SignificantPhrasesParameter`.`minSupport` = ? and `SignificantPhrasesParameter`.`minSupportAfterUpdate` = ? and `SignificantPhrasesParameter`.`ngrams` = ? and `SignificantPhrasesParameter`.`contentExtractorId` = ? and `SignificantPhrasesParameter`.`generatedQueries` = ?";
	
	
	private String getCombinedCollaborativeFilteringParametersString;
	private String insertCombinationString = "INSERT INTO Combination (idAlgorithm, idDatabase_g, idParameter_g, idVersion, version_pos_seed_g, version_neg_seed_g idSampleConfiguration_g, idSampleParameterBuilder_g, idWorkload) VALUES (?,?,?,?,?,?,?,?,?)";
	private String insertCombinationCrossedString = "INSERT INTO Combination (idAlgorithm, idDatabase_g, idParameter_g, idVersion, version_pos_seed_g,version_neg_seed_g, idSampleConfiguration_g, idWorkload, idDatabase_c, idParameter_c, version_pos_seed_c, version_neg_seed_c, idSampleConfiguration_c, idCombConf) VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private String insertExecutionString = "INSERT INTO Execution (idExecutionAlternative) VALUES (?)";
	private String insertIncrementalString = "INSERT INTO `AutomaticQueryGeneration`.`Incremental` (`maxQuerySize`, `minSupport`, `minSuppAfterUpdate`, `threshold`, `wfp`, `betaE`) VALUES (?, ?, ?, ?, ?, ?);";
	private String insertMSCString = " INSERT INTO `AutomaticQueryGeneration`.`MSC` (`maxQuerySize`, `minSupport`, `minSuppAfterUpdate`, `min_precision`, `min_support_SVM`, `k`, `pow`) VALUES (?, ?, ?, ?, ?, ?, ? );";
	private String insertOptimisticString = "INSERT INTO `AutomaticQueryGeneration`.`Optimistic` (`maxQuerySize`, `minSupport`, `minSuppAfterUpdate`, `threshold`, `min_weight`, `min_precision`, `supp`) VALUES ( ?, ?, ?, ?, ?, ?, ? )";
	private String insertQProberString = "INSERT INTO `AutomaticQueryGeneration`.`QProber` (`maxQuerySize`, `minSupport`, `minSuppAfterUpdate`, `minSVMSupport`, `epsilon`, `min_precision`) VALUES ( ?, ?, ?, ?, ?, ?)";
	private String insertSVMWordString = "INSERT INTO `AutomaticQueryGeneration`.`SVMWord` (`maxQuerySize`, `minSupport`, `minSuppAfterUpdate`) VALUES ( ?, ?, ?)";
	private String insertRipperString = "INSERT INTO `AutomaticQueryGeneration`.`Ripper` (`maxQuerySize`, `minSupport`, `minSuppAfterUpdate`, `fold`, `minNo`, `optimizationRuns`, `seedValue`, `pruning`, `checkErrorRate`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )";
	private String insertQXtractString = "INSERT INTO `AutomaticQueryGeneration`.`QXtract` (`maxQuerySize`, `minSupport`, `minSuppAfterUpdate`, `fold`, `minNo`, `optimizationRuns`, `seedValue`, `pruning`, `checkErrorRate`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )";
	private String insertSignificanPhraseParameterString = "INSERT INTO `AutomaticQueryGeneration`.`SignificantPhrasesParameter` (`maxQuerySize`, `minSupport`, `minSupportAfterUpdate`, `ngrams`, `contentExtractorId`, `generatedQueries`) VALUES ( ?, ?, ?, ?, ?, ?);";
	
	private String getSampleConfigurationBaseParameterString = "SELECT idParameter FROM AutomaticQueryGeneration.SampleConfiguration where idSampleConfiguration = ?";

	private String insertTupleAlgorithmString = "INSERT INTO `AutomaticQueryGeneration`.`TupleAlgorithm` (`maxQuerySize`, `minSupport`, `minSupportAfterUpdate`, `hits_per_page`, `querySubmissionPerUnitTime`, `queryTimeConsumed`, `ieSubmissionPerUnitTime`, `ieTimeConsumed`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
	private String insertCombinedCollaborativeFilteringParametersString = "INSERT INTO `AutomaticQueryGeneration`.`CollaborativeFiltering` (`preserveOrder`, `neighbors`, `userSimilarity`, `neighborhood`, `recommender`) VALUES ( ?, ?, ?, ?, ?);";
	private String writePRocessedDocumentsString = "INSERT INTO ProcessedDocuments (idExecution, idCombination, idDatabase, `limit`, position_e, position_q, executed_query_position, processed_document_position, " +
			"currentTime, usefulTuples, idDocument) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	String getSampleConfigurationString = "SELECT `SampleConfiguration`.`IdSampleConfiguration` from SampleConfiguration where (`SampleConfiguration`.`idParameter` = ? or -1 = ?) and (`SampleConfiguration`.`idVersion` = ? or -1 = ?) and (`SampleConfiguration`.`idWorkload` = ? or -1 = ?) and (`SampleConfiguration`.`idRelationConfiguration` = ?  or -1 = ?) and " +
			"(`SampleConfiguration`.`idExtractionSystem` = ? or -1 = ?) and (`SampleConfiguration`.`idQueryPoolExecutor` = ? or -1 = ?) and (`SampleConfiguration`.`idSampleGenerator` = ? or -1 = ?) and (`SampleConfiguration`.`use_all` = ? or -1 = ?) and (`SampleConfiguration`.`resultsPerQuery` = ?  or -1 = ?) and " +
			" (`SampleConfiguration`.`uselessNumber` = ? or -1 = ?) and (`SampleConfiguration`.`CountsAll` = ?  or -1 = ?) and (`SampleConfiguration`.`maxQueries` = ? or -1 = ?)";

//	String getSampleConfigurationStringM = "SELECT `SampleConfiguration`.`IdSampleConfiguration` from SampleConfiguration where (`SampleConfiguration`.`idParameter` = ? or -1 = ?) and (`SampleConfiguration`.`idVersion` = ? or -1 = ?) and (`SampleConfiguration`.`idWorkload` = ? or -1 = ?) and (`SampleConfiguration`.`idRelationConfiguration` = ?  or -1 = ?) and " +
//			"(`SampleConfiguration`.`idExtractionSystem` = ? or -1 = ?) and (`SampleConfiguration`.`idQueryPoolExecutor` = ? or -1 = ?) and (`SampleConfiguration`.`idSampleGenerator` = ? or -1 = ?) and (`SampleConfiguration`.`use_all` = ? or -1 = ?) and (`SampleConfiguration`.`resultsPerQuery` = ?  or -1 = ?) and " +
//			" (`SampleConfiguration`.`uselessNumber` = ? or -1 = ?) and (`SampleConfiguration`.`CountsAll` = ?  or -1 = ?) and (`SampleConfiguration`.`maxQueries` = ? or -1 = ?)";
	
	private Semaphore accessInternalTuples = new Semaphore(1);
	private Semaphore accessEntity = new Semaphore(1);
	private Semaphore accessExtractedEntity = new Semaphore(1);
	private Semaphore accessGeneratedCandidateSentence = new Semaphore(1);
	private Semaphore accessCandidateSentence = new Semaphore(1);
	private Semaphore accessOperableStructure = new Semaphore(1);
	private Semaphore accessGeneratedOperableStructure = new Semaphore(1);
	private Semaphore accessExtraction = new Semaphore(1);
	private Semaphore accessCandidateSentences = new Semaphore(1);
	private Semaphore accessExperimentSplit = new Semaphore(1);
	private String getSampleTuplesString = "SELECT idDatabase,idDocument,tupleString FROM AutomaticQueryGeneration.SampleTuples where idSample = ? order by idDatabase,idDocument";
	private String getSmallAttributeNameString = "select attributeTupleSmallDomain from RelationAttributeDomains natural join WorkloadRelation natural join (select SC.idWorkload from Sample S join SampleConfiguration SC on (S.idSampleConfiguration = SC.idSampleConfiguration) where idSample = ?) as A";
	private String getLargeAttributeNameString = "select attributeTupleLargeDomain from RelationAttributeDomains natural join WorkloadRelation natural join (select SC.idWorkload from Sample S join SampleConfiguration SC on (S.idSampleConfiguration = SC.idSampleConfiguration) where idSample = ?) as A";
	private Map<Integer, Map<Document, List<Tuple>>> cachedTuples;
	private String resgetDocumentsWithTuplesString = "select docId from Extraction E join InternalTupleExtraction I on (E.idExtractionSystem = I.idExtractionSystem and " +
			"E.idDatabase = I.idDatabase and E.fileAuxiliar = I.idInternalTupleExtraction) where E.idExtractionSystem = ? and " +
			"E.idDatabase = ? and I.content != 'rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAAdwQAAAAAeA=='";
	private String writeSignificantPhraseString = "INSERT INTO `AutomaticQueryGeneration`.`SignificantPhrases` (`idInformationExtractionSystem`, `idRelationConfiguration`, `collection`, `idWorkload`, `idVersion`, `split`, `tuplesAsStopWords`, `position`, `idQuery`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private List<persistentWriter> poolInstance;
	private Map<String, Map<Long, List<Document>>> cachedQueryResultsTable;
	private String getSignificantPhrasesString = "SELECT Text FROM TextQuery T join SignificantPhrases R on (T.idQuery = R.idQuery) where idInformationExtractionSystem = ? and idRelationConfiguration = ? and collection = ? and idWorkload = ? and idVersion = ? and split = ? and tuplesAsStopWords = ? and docsInTraining = ? order by position";
	private String existsSample = "SELECT EXISTS(SELECT 1 FROM Sample where idDatabase = ? and idVersion = ? and idWorkload = ? and version_seed_pos = ? and version_seed_neg = ? and idSampleConfiguration = ?)";
	private String saveDoneSample = "INSERT INTO `AutomaticQueryGeneration`.`GeneratedSample` (" +
			"`idSample`, `idSampleBuilderParameters`, `idDatabase`, `idWorkload`, `idVersion`, `idInformationExtractionSystem`, `idRelationConfiguration`) VALUES ( ?, ?, ?, ?, ?, ?, ?); ";
	private String insertBooleanMoodel = "INSERT INTO `AutomaticQueryGeneration`.`BooleanModels` (`idSample`, `idSampleBuilderParameters`, `uselessSample`, `idWorkload`, `idVersion`, `idInformationExtractionSystem`, `idRelationConfiguration`, `idDatabase`, `file`) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";
	private String insertTrueMoodel = "INSERT INTO `AutomaticQueryGeneration`.`TrueModels` (`idSample`, `idSampleBuilderParameters`, `uselessSample`, `idWorkload`, `idVersion`, `idInformationExtractionSystem`, `idRelationConfiguration`, `idDatabase`, `file`) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , ? )";
	private String getDocumentString = "select filePath from WebDocument where idDatabase = ? and idExperiment = ? and idDocument = ?";
	private String queriesUsedToGenerateNegativeSampleString = "select queryId from NegativeSampling where idExperimentId = ? and idDatabase = ? and split = ? and navigationTechnique = ? and extractionTechnique = ? and resultExtractionTechnique = ?;";

	public databaseWriter(String prefix, Map<TextQuery, Long> textqueries, long maxIdQuery) {

		super(prefix);

		if (textqueries != null){
			textquerytable = textqueries; //XXX I don't think I shall copy ...
			this.maxIdQuery = maxIdQuery;
		}

		conn = null;

	}

	public synchronized void closeConnection() {
		try {
			getConnection().close();
			System.out.println("Disconnected from database");
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	private synchronized Connection getConnection() {
		if (conn == null){
			openConnection();
		}

		return conn;
	}


	public synchronized  void openConnection() {

		conn = null;
		String url = "jdbc:mysql://db-files.cs.columbia.edu:3306/";
		String dbName = "AutomaticQueryGeneration";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "pjbarrio"; 
		String password = "test456";
		try {

			Class.forName(driver).newInstance();

			java.util.Properties connProperties = new java.util.Properties();

			connProperties.put(DATABASE_USER, userName);

			connProperties.put(DATABASE_PASSWORD, password);

			connProperties.put(MYSQL_AUTO_RECONNECT, "true");

			connProperties.put(MYSQL_MAX_RECONNECTS, "500");

			conn = DriverManager.getConnection(url+dbName,connProperties);

			System.out.println("Connected to the database");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void performTransaction(String todo){
		try {
			Statement statementToDo = getConnection().createStatement();
			statementToDo.executeUpdate (todo);
			statementToDo.close();
		}
		catch (Exception e) {
			System.out.println("CA: " + todo + " - CA." );
			e.printStackTrace();
		}     

	}

	@Override
	public void endAlgorithm() throws IOException {

		closeConnection();

	}

	//	@Override
	//	public void finishProcessingQueries() throws IOException {
	//
	//		actualQueries.clear();
	//		actualTimes.clear();
	//		actualNumberOfProcessedDocs.clear();
	//		actualQueries = null;
	//		actualTimes = null;
	//		actualNumberOfProcessedDocs = null;
	//	}
	//
	//	@Override
	//	public long getNextGenerationTime() throws NumberFormatException,
	//	IOException {
	//
	//		retrieveQueries();
	//
	//		actualTimeIndex++;
	//
	//		return actualTimes.get(actualTimeIndex-1);
	//
	//	}
	//
	//	@Override
	//	public String getNextQuery() throws IOException {
	//
	//		retrieveQueries();
	//
	//		actualQueryIndex++;
	//
	//		actualNumberOfProcessedDocsIndex++;
	//
	//		return actualQueries.get(actualQueryIndex-1);
	//
	//	}
	//
	//
	//	@Override
	//	public int getNumberOfDocumentsToProcess() {
	//		return actualNumberOfProcessedDocs.get(actualNumberOfProcessedDocsIndex-1);
	//	}
	//
	//	private void retrieveQueries() {
	//
	//		if (actualQueries != null)
	//			return;
	//
	//		actualQueryIndex = 0;
	//		actualTimeIndex = 0;
	//		actualNumberOfProcessedDocsIndex = 0;
	//
	//		actualQueries = new ArrayList<String>();
	//		actualNumberOfProcessedDocs = new ArrayList<Integer>();
	//		actualTimes = new ArrayList<Long>();
	//
	//		int idExecution = r_execution.getId();
	//		int idCombination = r_evaluation.getCombination().getId();
	//		int idEvaluatedOnDatabase = r_evaluation.getEvaluableDatabase().getId();
	//		int limit = r_evaluation.getDatabaseLimit();
	//
	//
	//		try {
	//
	//			StmtretrieveQueries = getConnection().createStatement();
	//
	//			RSretrieveQueries = StmtretrieveQueries.executeQuery
	//					("SELECT Q.query, D.numberOfDocuments, Q.time FROM Query Q, DocumentsToProcess D " +
	//							"WHERE D.idExecution = " + idExecution + " AND D.idCombination = " + idCombination + " AND " +
	//							"D.idDatabase = " + idEvaluatedOnDatabase + " AND D.limit = " + limit + " ORDER BY D.executed_position");
	//
	//			while (RSretrieveQueries.next()) {
	//
	//				actualQueries.add(RSretrieveQueries.getString(1));
	//				actualNumberOfProcessedDocs.add(RSretrieveQueries.getInt(2));
	//				actualTimes.add(RSretrieveQueries.getLong(3));
	//
	//			}
	//
	//			RSretrieveQueries.close();
	//
	//			StmtretrieveQueries.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
	//
	//	@Override
	//	public boolean hasMoreQueries() {
	//
	//		retrieveQueries();
	//
	//		return (actualTimeIndex<actualTimes.size() && actualQueryIndex<actualQueries.size());
	//
	//	}

	@Override
	public int setAlgorithm(String algorithm, int database, String version, int workload, int version_pos_seed, int version_neg_seed, int sample_configuration, int sample_parameters, int w_parameterId)
			throws IOException {

		//		stored_query = 0;


		int w_algorithId = getAlgorithmId(algorithm);
		int w_generatorId = database;
		int w_versionId = getVersion(version);
		//		w_workload = workload;
		//		w_sample_number_g = sample_number;

		//		int maxQuerySize_Id = getAttributeId(AttributeEnum.MAX_QUERY_SIZE);
		//		int minSupport_Id = getAttributeId(AttributeEnum.MIN_SUPPORT);
		//		int minSupportAfterUpdate_Id = getAttributeId(AttributeEnum.MIN_SUPPORT_AFTER_UPDATE);

		//		insertParameter(0,new String("Algorithm Configuration for " + algorithm + " over " + database + " " + version + " " + sample_configuration + " " + sample_parameters + " number: " + version_pos_seed + "-" + version_neg_seed));

		//		int w_parameterId = getNewParameterId();
		//
		//		insertParameterAssociation(w_parameterId,maxQuerySize_Id,"" + maxQuerySize);
		//		insertParameterAssociation(w_parameterId,minSupport_Id,"" + minSupport);
		//		insertParameterAssociation(w_parameterId,minSupportAfterUpdate_Id,"" + minSuppAfterUpdate);

		insertGeneration(w_algorithId,w_generatorId,w_parameterId,w_versionId,workload,version_pos_seed,version_neg_seed,sample_configuration,sample_parameters);

		int w_combinationId = insertCombination(w_algorithId,w_generatorId,w_parameterId,w_versionId,version_pos_seed,version_neg_seed,workload,sample_configuration, sample_parameters);

		//		int[] ret = new int[2];
		//
		//		ret[0] = w_combinationId;
		//		ret[1] = w_parameterId;

		return w_combinationId;

	}

	private int insertCombination(int algorithm, int generatorDB, int parameter, int version, int version_pos_seed, int version_neg_seed,
			int workload, int sample_configuration, int sample_parameters) {

		int ret = -1;

		try {

			//			if (PStmtinsertCombination == null){
			//				PStmtinsertCombination = getConnection().prepareStatement(insertCombinationString,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertCombination.clearParameters();

			PreparedStatement PStmtinsertCombination = getConnection().prepareStatement(insertCombinationString,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertCombination.setInt(1, algorithm);
			PStmtinsertCombination.setInt(2, generatorDB);
			PStmtinsertCombination.setInt(3, parameter);
			PStmtinsertCombination.setInt(4, version);
			PStmtinsertCombination.setInt(5, version_pos_seed);
			PStmtinsertCombination.setInt(6, version_neg_seed);
			PStmtinsertCombination.setInt(7, workload);
			PStmtinsertCombination.setInt(8, sample_configuration);
			PStmtinsertCombination.setInt(9, sample_parameters);

			PStmtinsertCombination.execute();

			ResultSet rs = PStmtinsertCombination.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertCombination.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private void insertGeneration(int algorithm, int generatorDB,
			int parameter, int version, int workload, int version_pos_seed, int version_neg_seed, int sample_configuration, int sample_parameters) {

		String todoinsertGeneration = ("INSERT INTO Generation (idAlgorithm, idDatabase, idParameter, idVersion, idWorkload, version_pos_seed, version_neg_seed, idSampleConfiguration, idSampleParameterBuilder) VALUES " +
				"(" + algorithm + "," + generatorDB + "," + parameter + "," + version + "," + workload +"," + version_pos_seed +"," + version_neg_seed + "," + sample_configuration + "," + sample_parameters + ")");

		performTransaction(todoinsertGeneration);

	}

	//	private synchronized int getNewCombinationId() {
	//
	//		int res = 0;
	//
	//		try {
	//			
	//			StmtnewCombinationId = getConnection().createStatement();
	//
	//			RSnewCombinationId = StmtnewCombinationId.executeQuery
	//					("SELECT LAST_INSERT_ID()");
	//
	//			while (RSnewCombinationId.next()) {
	//
	//				res = RSnewCombinationId.getInt(1);
	//
	//			}
	//			RSnewCombinationId.close();
	//			
	//			StmtnewCombinationId.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//		return res;
	//
	//	}


	//	private synchronized int getNewParameterId() {
	//
	//		int res = 0;
	//
	//		try {
	//
	//			StmtnewParameterId = getConnection().createStatement();
	//
	//			RSnewParameterId = StmtnewParameterId.executeQuery
	//					("SELECT LAST_INSERT_ID()");
	//
	//			while (RSnewParameterId.next()) {
	//
	//				res = RSnewParameterId.getInt(1);
	//
	//			}
	//			RSnewParameterId.close();
	//			StmtnewParameterId.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//		return res;
	//
	//	}

	private  int getVersion(String version) {

		int res = 0;

		try {

			Statement StmtgetVersion = getConnection().createStatement();

			ResultSet RSVersion = StmtgetVersion.executeQuery
					("SELECT V.idVersion " +
							" FROM Version V WHERE V.Description='" + format(version)+"'");

			while (RSVersion.next()) {
				res = RSVersion.getInt(1);
			}
			RSVersion.close();
			StmtgetVersion.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;

	}

	private String format(String stringValue) {

		if (stringValue.contains("'"))
			return stringValue.replace("'", "\\'");

		return stringValue;

	}

	private int getDatabaseId(String database) {

		int res = 0;

		try {
			Statement StmtDatabaseId = getConnection().createStatement();

			ResultSet RSDatabaseId = StmtDatabaseId.executeQuery
					("SELECT D.idDatabase " +
							" FROM `Database` D WHERE D.Name='" + format(database)+"'");

			while (RSDatabaseId.next()) {
				res = RSDatabaseId.getInt(1);
			}
			RSDatabaseId.close();
			StmtDatabaseId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return res;

	}

	private int getAlgorithmId(String algorithm) {

		int res = 0;

		try {
			Statement StmtAlgorithmId = getConnection().createStatement();

			ResultSet RSAlgorithmId = StmtAlgorithmId.executeQuery
					("SELECT A.idAlgorithm " +
							" FROM Algorithm A WHERE A.Name='" + format(algorithm) + "'");

			while (RSAlgorithmId.next()) {
				res = RSAlgorithmId.getInt(1);
			}
			RSAlgorithmId.close();
			StmtAlgorithmId.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;

	}

	@Override
	public int setCombinedAlgorithm(Combination simpleConfig, Sample crossableSample, int configuration, int w_parameterCombId)
			throws IOException {

		//		stored_query = 0;


		int w_algorithId = simpleConfig.getAlgorithm().getId();
		int w_generatorId = simpleConfig.getGeneratorSample().getDatabase().getId();
		int w_versionId = simpleConfig.getVersion().getId();
		//		w_sample_number_g = simpleConfig.getGeneratorSample().getSample_number();
		//		w_workload = simpleConfig.getGeneratorSample().getWorkload().getId();


		int w_evaluatorId = crossableSample.getDatabase().getId();
		int w_version_pos_seed_c = crossableSample.getVersionSeedPos();
		int w_version_neg_seed_c = crossableSample.getVersionSeedNeg();
		int w_sample_configuration_c = crossableSample.getSampleConfiguration().getId();

		int w_configurationId = configuration;

		//		int preserveOrder_Id = getAttributeId(AttributeEnum.PRESERVE_ORDER);
		//		int neighbors_Id = getAttributeId(AttributeEnum.NEIGHBORS);
		//		int userSimilarity_Id = getAttributeId(AttributeEnum.USER_SIMILARITY);
		//		int userNeighborhood_Id = getAttributeId(AttributeEnum.USER_NEIGHBORHOOD);
		//		int recommender_Id = getAttributeId(AttributeEnum.RECOMMENDER);

		int w_parameterId = simpleConfig.getParameterG();
		//
		//		insertParameter(w_parameterCombId,new String("Combining Algorithm Configuration for " + simpleConfig.getId() + " On " + crossableSample.getDatabase().getName()));
		//		
		//		int w_parameterCombId = getNewParameterId();
		//
		//		insertParameterAssociation(w_parameterCombId,preserveOrder_Id,"" + preserveOrder);
		//		insertParameterAssociation(w_parameterCombId,neighbors_Id,"" + neighbors);
		//		insertParameterAssociation(w_parameterCombId,userSimilarity_Id,userSimilarity);
		//		insertParameterAssociation(w_parameterCombId, userNeighborhood_Id, userNeighborhood);
		//		insertParameterAssociation(w_parameterCombId, recommender_Id, recommender);

		int w_combinationId = insertCombination(w_algorithId, w_generatorId, w_parameterId, w_versionId,simpleConfig.getGeneratorSample().getVersionSeedPos(),simpleConfig.getGeneratorSample().getVersionSeedNeg(),simpleConfig.getGeneratorSample().getSampleConfiguration().getId(),simpleConfig.getWorkload().getId(), w_evaluatorId,w_parameterCombId,w_version_pos_seed_c,w_version_neg_seed_c, w_sample_configuration_c,w_configurationId);

		//		int[] ret = new int[2];
		//		ret[0] = w_combinationId;
		//		ret[1] = w_parameterCombId;

		return w_combinationId;

	}

	private int insertCombination(int algorithm,
			int generatorDB, int parameter, int version, int version_pos_seed_G, int version_neg_seed_G, int sample_configuration_g, int workload, 
			int evaluatorDB, int parameterComb, int version_pos_seed_C, int version_neg_seed_C, int sample_configuration_c, int configuration) {

		int ret = -1;

		try {

			//			if (PStmtinsertCombinationCrossed == null){
			//				PStmtinsertCombinationCrossed = getConnection().prepareStatement(insertCombinationCrossedString,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertCombinationCrossed.clearParameters();

			PreparedStatement PStmtinsertCombinationCrossed = getConnection().prepareStatement(insertCombinationCrossedString,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertCombinationCrossed.setInt(1, algorithm);
			PStmtinsertCombinationCrossed.setInt(2, generatorDB);
			PStmtinsertCombinationCrossed.setInt(3, parameter);
			PStmtinsertCombinationCrossed.setInt(4, version);
			PStmtinsertCombinationCrossed.setInt(5, version_pos_seed_G);
			PStmtinsertCombinationCrossed.setInt(6, version_neg_seed_G);
			PStmtinsertCombinationCrossed.setInt(7, sample_configuration_g);
			PStmtinsertCombinationCrossed.setInt(8, workload);
			PStmtinsertCombinationCrossed.setInt(9, evaluatorDB);
			PStmtinsertCombinationCrossed.setInt(10, parameterComb);
			PStmtinsertCombinationCrossed.setInt(11, version_pos_seed_C);
			PStmtinsertCombinationCrossed.setInt(12, version_neg_seed_C);
			PStmtinsertCombinationCrossed.setInt(13, sample_configuration_c);
			PStmtinsertCombinationCrossed.setInt(14, configuration);


			PStmtinsertCombinationCrossed.execute();

			ResultSet rs = PStmtinsertCombinationCrossed.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertCombinationCrossed.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	//	private synchronized  int getParameterIdFromGeneration(int algorithm, int generatorDB,
	//			int version, int workload, int sample_number) {
	//		
	//		int ret = 0;
	//		
	//		try {
	//			
	//			StmtgetParameterIdFromGeneration = getConnection().createStatement();
	//			
	//			RSgetParameterIdFromGeneration = StmtgetParameterIdFromGeneration.executeQuery
	//	        ("SELECT G.idParameter " +
	//	         " FROM Generation G WHERE G.idAlgorithm=" + algorithm + " AND G.idDatabase = " + generatorDB + " AND " +
	//	         		"G.idVersion=" + version + " AND G.idWorkload = " + workload + " AND G.sample_number = " + sample_number);
	//
	//	        while (RSgetParameterIdFromGeneration.next()) {
	//	        
	//	        	ret = RSgetParameterIdFromGeneration.getInt(1); //keep the lastOne.
	//	        
	//	        }
	//	       
	//	        RSgetParameterIdFromGeneration.close();
	//	        StmtgetParameterIdFromGeneration.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//        return ret;
	//		
	//	}

	//	@Override
	//	public void setIncrementalAdditionalParameters(int w_parameterId, double performanceThreshold,
	//			double fpWeight, double betaEfficiency) {
	//
	//		int performanceThreshold_Id = getAttributeId(AttributeEnum.PERFORMANCE_THRESHOLD);
	//		int fpWeight_Id = getAttributeId(AttributeEnum.FP_WEIGHT);
	//		int betaEfficiency_Id = getAttributeId(AttributeEnum.BETA_EFFICIENCY);
	//
	//		insertParameterAssociation(w_parameterId, performanceThreshold_Id, Double.toString(performanceThreshold));
	//		insertParameterAssociation(w_parameterId, fpWeight_Id, Double.toString(fpWeight));
	//		insertParameterAssociation(w_parameterId, betaEfficiency_Id, Double.toString(betaEfficiency));
	//
	//	}
	//
	//	@Override
	//	public void setMSCAdditionalParameters(int w_parameterId, double minPrecision,
	//			double minimumSupportSVM, int maxNumberOfQueries, double pow) {
	//
	//		int minPrecision_Id = getAttributeId(AttributeEnum.MIN_PRECISION);
	//		int minimumSupportSVM_Id = getAttributeId(AttributeEnum.MIN_SUPPORT_SVM);
	//		int maxNumberOfQueries_Id = getAttributeId(AttributeEnum.MAX_NUMBER_OF_QUERIES);
	//		int power_Id = getAttributeId(AttributeEnum.POWER);
	//
	//		insertParameterAssociation(w_parameterId,minPrecision_Id,Double.toString(minPrecision));
	//		insertParameterAssociation(w_parameterId,minimumSupportSVM_Id, Double.toString(minimumSupportSVM));
	//		insertParameterAssociation(w_parameterId,maxNumberOfQueries_Id, Integer.toString(maxNumberOfQueries));
	//		insertParameterAssociation(w_parameterId,power_Id, Double.toString(pow));
	//
	//
	//	}
	//
	//	@Override
	//	public void setOptimistic(int w_parameterId, double thresholdPerformance, double minWeight,
	//			double minPrecision, double minimumSupportSVM) {
	//
	//		int thresholdPerformance_id = getAttributeId(AttributeEnum.PERFORMANCE_THRESHOLD);
	//		int minWeightSVM_Id = getAttributeId(AttributeEnum.MIN_WEIGHT);
	//		int minPrecision_Id = getAttributeId(AttributeEnum.MIN_PRECISION);
	//		int minimumSupportSVM_Id = getAttributeId(AttributeEnum.MIN_SUPPORT_SVM);
	//
	//		insertParameterAssociation(w_parameterId,thresholdPerformance_id,Double.toString(thresholdPerformance));
	//		insertParameterAssociation(w_parameterId,minWeightSVM_Id,Double.toString(minWeight));
	//		insertParameterAssociation(w_parameterId,minPrecision_Id,Double.toString(minPrecision));
	//		insertParameterAssociation(w_parameterId,minimumSupportSVM_Id,Double.toString(minimumSupportSVM));
	//
	//	}
	//
	//	@Override
	//	public void setQProberAdditionalParameters(int w_parameterId, double minimumSupportSVM,
	//			double minWeight, double minPrecision) {
	//
	//		int minimumSupportSVM_Id = getAttributeId(AttributeEnum.MIN_SUPPORT_SVM);
	//		int minWeightSVM_Id = getAttributeId(AttributeEnum.MIN_WEIGHT);
	//		int minPrecision_Id = getAttributeId(AttributeEnum.MIN_PRECISION);
	//
	//		insertParameterAssociation(w_parameterId,minimumSupportSVM_Id,Double.toString(minimumSupportSVM));
	//		insertParameterAssociation(w_parameterId,minWeightSVM_Id,Double.toString(minWeight));
	//		insertParameterAssociation(w_parameterId,minPrecision_Id,Double.toString(minPrecision));
	//
	//	}

	@Override
	public void writeQueries(int w_combinationId, List<Pair<TextQuery,Long>> querytime){

		try {

			//			if (PStmtwriteQueries == null){
			//				PStmtwriteQueries = getConnection().prepareStatement(writeQueriesString );
			//			}else
			//				PStmtwriteQueries.clearParameters();

			PreparedStatement PStmtwriteQueries = getConnection().prepareStatement(writeQueriesString );

			for (int i = 0; i < querytime.size(); i++) {

				long qId = getTextQuery(querytime.get(i).getFirst());

				PStmtwriteQueries.setInt(1, w_combinationId);
				PStmtwriteQueries.setInt(2, i+1);
				PStmtwriteQueries.setLong(3, qId);
				PStmtwriteQueries.setLong(4, querytime.get(i).getSecond());
				PStmtwriteQueries.addBatch();

			}

			PStmtwriteQueries.executeBatch();

			PStmtwriteQueries.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		//		kkkkkkkkkkk

		//		int stored_query = 1;
		//
		//		StringBuilder sb = new StringBuilder();
		//		
		//		for (int i = 0; i < querytime.size(); i++){
		//		
		//		 sb.append(insertQuery(w_combinationId,stored_query,querytime.get(i).getFirst(),querytime.get(i).getSecond()));
		//
		//		}
		//		
		//		performTransaction(sb.toString());

	}

	//	private synchronized String insertQuery(int combination, int position, String query,
	//			long time) {
	//
	//		kkkkkkkkkkkkkkkkkkkkk
	//		
	//		String todoinsertQuery = ("INSERT INTO Query (idCombination, Position, query, time) VALUES " +
	//				"(" + combination + "," + position + ",'" + format(query) + "'," + time + ");");
	//
	//		return todoinsertQuery;
	//	}

	//	@Override
	//	public void setTupleAdditionalParameters(int w_parameterId, long hitsPerPage,
	//			double querySubmissionPerUnitTime, long queryTimeConsumed,
	//			double ieSubmissionPerUnitTime, long ieTimeConsumed) {
	//
	//		int hitsPerPage_Id = getAttributeId(AttributeEnum.HITS_PER_PAGE);
	//		int querySubmissionPerUnitTime_Id = getAttributeId(AttributeEnum.QUERY_SUBMISSION_PER_UNIT_TIME);
	//		int queryTimeConsumed_Id = getAttributeId(AttributeEnum.QUERY_TIME_CONSUMED);
	//		int ieSubmissionPerUnitTime_Id = getAttributeId(AttributeEnum.IE_SUBMISSION_PER_TIME);
	//		int ieTimeConsumed_Id = getAttributeId(AttributeEnum.IE_TIME_CONSUMED);
	//
	//		insertParameterAssociation(w_parameterId, hitsPerPage_Id, Double.toString(hitsPerPage));
	//		insertParameterAssociation(w_parameterId, querySubmissionPerUnitTime_Id, Double.toString(querySubmissionPerUnitTime));
	//		insertParameterAssociation(w_parameterId, queryTimeConsumed_Id, Double.toString(queryTimeConsumed));
	//		insertParameterAssociation(w_parameterId, ieSubmissionPerUnitTime_Id , Double.toString(ieSubmissionPerUnitTime));
	//		insertParameterAssociation(w_parameterId, ieTimeConsumed_Id, Double.toString(ieTimeConsumed));
	//
	//	}

	@Override
	public void insertEvaluation(Evaluation evaluation, Execution w_execution) {

		insertEvaluation(evaluation.getCombination().getId(), evaluation.getEvaluableDatabase().getId(), evaluation.getDatabaseLimit());

		insertExecutionCombination(w_execution.getId(),evaluation.getCombination().getId(), evaluation.getEvaluableDatabase().getId(), evaluation.getDatabaseLimit(),"No Description");

	}


	@Override
	public void writeDetectedTuples(Execution execution,
			ArrayList<String> tuples) {
		//TODO See if we want to save the tuples... Maybe we could write them to a file

	}

	private int insertExecution(int executionAlternativeId) {

		int ret = -1;

		try {

			//			if (PStmtinsertExecution == null){
			//				PStmtinsertExecution = getConnection().prepareStatement(insertExecutionString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertExecution.clearParameters();

			PreparedStatement PStmtinsertExecution = getConnection().prepareStatement(insertExecutionString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertExecution.setInt(1, executionAlternativeId);

			PStmtinsertExecution.execute();

			ResultSet rs = PStmtinsertExecution.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertExecution.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private void insertEvaluation(int combination,
			int searchableDatabase, int limit) {

		String todoinsertEvaluation = ("INSERT INTO Evaluation (idCombination, idDatabase, `limit`) VALUES " +
				"(" + combination + "," + searchableDatabase + "," + limit + ")");

		performTransaction(todoinsertEvaluation);

	}

	private void insertExecutionCombination(int Executionid, int CombinationId, int DatabaseId,
			int limit, String description) {

		String todoinsertExecutionCombination = ("INSERT INTO ExecutionCombination (idExecution, idCombination, idDatabase, `limit`, Description) VALUES " +

				"(" + Executionid + "," + CombinationId + "," + DatabaseId + "," + limit + ",'" + format(description) + "')");

		performTransaction(todoinsertExecutionCombination);

	}

	//	@Override
	//	public void setCurrentExecutionAlternative(
	//			ExecutionAlternative executionAlternative) {
	//
	//		w_executionAlternative = executionAlternative;
	//
	//	}

	@Override
	public synchronized Execution insertExecution(ExecutionAlternative w_executionAlternative) {

		int id = insertExecution(w_executionAlternative.getId());

		return new Execution(id,w_executionAlternative,this);
	}

	//	private synchronized int getNextExecutionId() {
	//
	//		int res = 0;
	//
	//		try {
	//			StmtnextExecutionId = getConnection().createStatement();
	//
	//			RSnextExecutionId = StmtnextExecutionId.executeQuery
	//					("SELECT LAST_INSERT_ID()");
	//
	//			while (RSnextExecutionId.next()) {
	//
	//				res = RSnextExecutionId.getInt(1);
	//
	//			}
	//			RSnextExecutionId.close();
	//			StmtnextExecutionId.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//		return res;
	//
	//
	//
	//	}

	//	@Override
	//	public void setRipperAdditionalParameters(int w_parameterId, int fold, double minNo,
	//			int optimizationRuns, long seedValue, boolean pruning,
	//			boolean checkErrorRate) {
	//
	//		int foldId = getAttributeId(AttributeEnum.FOLDS);
	//		int minNoId = getAttributeId(AttributeEnum.MINIMUM_INSTANCE_WEIGHT);
	//		int optimizationRunsId = getAttributeId(AttributeEnum.OPTIMIZATION_RUNS);
	//		int seedValueId = getAttributeId(AttributeEnum.SEED_VALUE);
	//		int pruningId = getAttributeId(AttributeEnum.USE_PRUNING);
	//		int checkErrorRateId = getAttributeId(AttributeEnum.CHECK_ERROR_RATE);
	//
	//		insertParameterAssociation(w_parameterId,foldId,Integer.toString(fold));
	//		insertParameterAssociation(w_parameterId,minNoId,Double.toString(minNo));
	//		insertParameterAssociation(w_parameterId,optimizationRunsId,Integer.toString(optimizationRuns));
	//		insertParameterAssociation(w_parameterId,seedValueId,Long.toString(seedValue));
	//		insertParameterAssociation(w_parameterId,pruningId,Boolean.toString(pruning));
	//		insertParameterAssociation(w_parameterId,checkErrorRateId,Boolean.toString(checkErrorRate));
	//
	//	}

	@Override
	public int getConfiguration(double weightCoverage, double weightSpecificity) {

		int res = 0;

		try {
			Statement StmtConfiguration = getConnection().createStatement();

			ResultSet RSConfiguration = StmtConfiguration.executeQuery
					("SELECT C.idCombConf FROM CombConf C WHERE" +
							" C.weightCoverage = " + weightCoverage + " AND C.weightSpecificity = " + weightSpecificity);

			while (RSConfiguration.next()) {

				res = RSConfiguration.getInt(1);

			}

			RSConfiguration.close();
			StmtConfiguration.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;

	}

	@Override
	public void updateCurrentAlgorithmTime(int w_combinationId,long measuredTime) {

		String todoupdateCurrentAlgorithmTime = ("UPDATE Combination SET generationTime = " + measuredTime + " WHERE idCombination = " + w_combinationId);

		performTransaction(todoupdateCurrentAlgorithmTime);

	}

	@Override
	public void writeProcessedDocuments(Execution w_execution, Evaluation evaluation,
			int evaluation_position, Query query, long query_position,
			Document document, long document_position, long currentTime,
			int usefulTuples) {


		try {

			//			if (PStmtwriteProcessedDocuments == null){
			//				PStmtwriteProcessedDocuments = getConnection().prepareStatement(writePRocessedDocumentsString);
			//			}else
			//				PStmtwriteProcessedDocuments.clearParameters();

			PreparedStatement PStmtwriteProcessedDocuments = getConnection().prepareStatement(writePRocessedDocumentsString);

			PStmtwriteProcessedDocuments.setInt(1,  w_execution.getId());
			PStmtwriteProcessedDocuments.setInt(2,  evaluation.getCombination().getId());
			PStmtwriteProcessedDocuments.setInt(3, evaluation.getEvaluableDatabase().getId());
			PStmtwriteProcessedDocuments.setLong(4, evaluation.getDatabaseLimit());
			PStmtwriteProcessedDocuments.setDouble(5, evaluation_position);
			PStmtwriteProcessedDocuments.setLong(6,  query.getPosition());
			PStmtwriteProcessedDocuments.setDouble(7, query_position);
			PStmtwriteProcessedDocuments.setLong(8, document_position);
			PStmtwriteProcessedDocuments.setLong(9,  currentTime);
			PStmtwriteProcessedDocuments.setDouble(10, usefulTuples);
			PStmtwriteProcessedDocuments.setLong(11, document.getId());

			PStmtwriteProcessedDocuments.execute();

			PStmtwriteProcessedDocuments.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	//	@Override
	//	public boolean hasMoreCombinations() {
	//
	//		return combinations.size()>0;
	//
	//	}
	//
	//	@Override
	//	public void initializeSimpleExplorator() {
	//
	//		combinations = new ArrayList<Integer>();
	//
	//		try {
	//
	//			StmtinitializeSimpleExplorator = getConnection().createStatement();
	//
	//			RSinitializeSimpleExplorator = StmtinitializeSimpleExplorator.executeQuery
	//					("SELECT idCombination " +
	//							" FROM SimpleCombinationsToCombine");
	//
	//			while (RSinitializeSimpleExplorator.next()) {
	//
	//				combinations.add(RSinitializeSimpleExplorator.getInt(1)); //keep the lastOne.
	//
	//			}
	//
	//			RSinitializeSimpleExplorator.close();
	//			StmtinitializeSimpleExplorator.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//	}
	//
	//	@Override
	//	public Combination nextCombination() {
	//
	//		Combination ret = getCombination(combinations.remove(0));
	//
	//		return ret;
	//	}

	private Combination getCombination(Integer id) {

		Combination comb = getCachedCombination(id);

		if (comb != null)
			return comb;

		Combination ret = null;

		try {

			//			StmtgetCombination = getConnection().createStatement();

			//			if (PStmtgetCombination == null){
			//				PStmtgetCombination = getConnection().prepareStatement(getCombinationString);
			//			}else
			//				PStmtgetCombination.clearParameters();

			PreparedStatement PStmtgetCombination = getConnection().prepareStatement(getCombinationString);

			PStmtgetCombination.setInt(1, id);

			ResultSet RSgetCombination = PStmtgetCombination.executeQuery();

			//			RSgetCombination = StmtgetCombination.executeQuery
			//	        ("SELECT C.*  FROM Combination C WHERE C.idCombination = " + id);

			while (RSgetCombination.next()) {

				ret = getCombination(RSgetCombination);

			}

			RSgetCombination.close();
			PStmtgetCombination.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		getCombinationsTable().put(id,ret);

		return ret;

	}

	private Combination getCachedCombination(Integer id) {

		return getCombinationsTable().get(id);

	}

	private synchronized Hashtable<Integer,Combination> getCombinationsTable() {

		if (combinationsTable == null)
			combinationsTable  = new Hashtable<Integer, Combination>();
		return combinationsTable;
	}

	private Combination getCombination(ResultSet RS) throws SQLException {

		Combination ret;

		int combination_Id = RS.getInt(1);

		int algorithm_Id = RS.getInt(2);

		Algorithm al = getAlgorithm(algorithm_Id);

		int gDatabase_id = RS.getInt(3);

		Database gDatabase = getDatabase(gDatabase_id);

		int parameter_g = RS.getInt(4);

		int version_Id = RS.getInt(5);

		int version_pos_seed_g = RS.getInt(6);

		int version_neg_seed_g = RS.getInt(7);

		int sample_configuration_g = RS.getInt(8);

		int idWorkload = RS.getInt(9);

		WorkloadModel workload = getWorkloadModel(idWorkload);

		Version ve = getVersion(version_Id,workload);

		Sample sample_G = Sample.getSample(gDatabase, ve, workload, version_pos_seed_g,version_neg_seed_g,new DummySampleConfiguration(sample_configuration_g));

		int cDatabase_Id = RS.getInt(10);

		long time = RS.getLong(16);

		if (cDatabase_Id != 0){

			int parameter_c = RS.getInt(11);

			Database cDatabase = getDatabase(cDatabase_Id);

			int version_pos_seed_c = RS.getInt(12);

			int version_neg_seed_c = RS.getInt(13);

			int sample_configuration_c = RS.getInt(14);

			Sample sample_C = Sample.getSample(cDatabase,ve,workload,version_pos_seed_c, version_neg_seed_c,new DummySampleConfiguration(sample_configuration_c));

			int combined_Id = RS.getInt(15);

			Configuration con = getConfiguration(combined_Id);

			ret = new Combination(combination_Id, al, sample_G,sample_C,con,time,parameter_g,parameter_c);

		} else {

			ret = new Combination(combination_Id,al,sample_G,time,parameter_g);

		}

		return ret;

	}

	private Configuration getConfiguration(int combinationId) {

		Configuration conf = getCachedConfiguration(combinationId);

		if (conf != null)
			return conf;


		Configuration ret = null;

		try {

			Statement StmtgetConfiguration = getConnection().createStatement();

			ResultSet RSgetConfiguration = StmtgetConfiguration.executeQuery
					("SELECT C.idCombConf, C.Description, C.weightCoverage, C.weightSpecificity " +
							" FROM CombConf C WHERE C.idCombConf = " + combinationId);

			while (RSgetConfiguration.next()) {

				ret = new Configuration(combinationId,RSgetConfiguration.getString(2), RSgetConfiguration.getDouble(3),RSgetConfiguration.getDouble(4));

			}

			RSgetConfiguration.close();
			StmtgetConfiguration.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		getConfigurationsTable().put(combinationId,ret);

		return ret;

	}

	private Configuration getCachedConfiguration(int combinationId) {

		return getConfigurationsTable().get(combinationId);

	}

	private synchronized  Hashtable<Integer,Configuration> getConfigurationsTable() {

		if (configurationTable == null){
			configurationTable  = new Hashtable<Integer, Configuration>();
		}
		return configurationTable;
	}

	private Version getVersion(int versionId, WorkloadModel wm) {

		return Version.generateInstance(getVersionDescription(versionId), wm);

	}

	private String getVersionDescription(int versionId) {

		String ret = getCachedVersionDescription(versionId);

		if (ret!=null)
			return ret;

		try {

			//			StmtversionDescription = getConnection().createStatement();

			//			if (PStmtversionDescription == null){
			//				PStmtversionDescription = getConnection().prepareStatement(versionDescriptionString);
			//			}else
			//				PStmtversionDescription.clearParameters();

			PreparedStatement PStmtversionDescription = getConnection().prepareStatement(versionDescriptionString);

			PStmtversionDescription.setInt(1, versionId);

			//			RSgetVersionDescription = StmtversionDescription.executeQuery
			//	        ("SELECT V.idVersion, V.Description " +
			//	         " FROM Version V WHERE V.idVersion = " + versionId);

			ResultSet RSgetVersionDescription = PStmtversionDescription.executeQuery();

			while (RSgetVersionDescription.next()) {

				ret = RSgetVersionDescription.getString(2);

			}

			RSgetVersionDescription.close();
			PStmtversionDescription.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		saveCachedVersionDescription(versionId,ret);

		return ret;

	}

	private void saveCachedVersionDescription(int versionId, String description) {

		getVersionDescriptionTable().put(versionId, description);

	}

	private String getCachedVersionDescription(int versionId) {

		return getVersionDescriptionTable().get(versionId);

	}

	private synchronized Hashtable<Integer,String> getVersionDescriptionTable() {

		if (versionDescriptionTable == null){
			versionDescriptionTable = new Hashtable<Integer, String>();
		}

		return versionDescriptionTable;
	}

	public Database getDatabase(int id) {

		synchronized (databasesTable) {

			Database db = databasesTable.get(id);

			if (db != null)
				return db;

			Database ret = null;

			try {

				//			StmtgetDatabase = getConnection().createStatement();

				//				if (PStmtgetDatabase == null){
				//					PStmtgetDatabase = getConnection().prepareStatement(getDatabaseString);
				//				}else
				//					PStmtgetDatabase.clearParameters();

				PreparedStatement PStmtgetDatabase = getConnection().prepareStatement(getDatabaseString);

				PStmtgetDatabase.setInt(1, id);

				//			RSgetDatabase = StmtgetDatabase.executeQuery
				//	        ("SELECT D.Name, D.size, D.Class, D.ModelType, D.isGlobal, D.isCluster, D.isLocal, D.`Index`" +
				//	         " FROM `Database` D WHERE D.idDatabase = " + id);

				ResultSet RSgetDatabase = PStmtgetDatabase.executeQuery();

				while (RSgetDatabase.next()) {

					String name = RSgetDatabase.getString(1);
					int size = RSgetDatabase.getInt(2);
					String Type = RSgetDatabase.getString(3);
					String ModelType = RSgetDatabase.getString(4);
					boolean isGlobal = RSgetDatabase.getBoolean(5);
					boolean isCluster = RSgetDatabase.getBoolean(6);
					boolean isLocal = RSgetDatabase.getBoolean(7);
					String index = RSgetDatabase.getString(8);

					if (isGlobal)
						ret = new GlobalDatabase(id,name);
					else if (isCluster)
						ret = new ClusterDatabase(id, name);
					else if (isLocal)
						ret = new LocalDatabase(id,name,size,Type,ModelType,index);
					else
						ret = new OnlineDatabase(id,name,ModelType,index);

				}

				RSgetDatabase.close();
				PStmtgetDatabase.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}

			databasesTable.put(ret.getId(), ret);

			return ret;

		}



	}

	private Algorithm getAlgorithm(int id) {

		Algorithm algorithm = getCachedAlgorithm(id);

		if (algorithm != null)
			return algorithm;


		Algorithm ret = null;

		try {

			Statement StmtgetAlgorithm = getConnection().createStatement();

			ResultSet RSgetAlgorithm = StmtgetAlgorithm.executeQuery
					("SELECT A.Name FROM Algorithm A WHERE A.idAlgorithm = " + id);

			while (RSgetAlgorithm.next()) {

				String Name = RSgetAlgorithm.getString(1);

				ret = new Algorithm(id,AlgorithmEnum.valueOf(Name));

			}

			RSgetAlgorithm.close();
			StmtgetAlgorithm.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		getAlgorithmsTable().put(id, ret);

		return ret;

	}

	private Algorithm getCachedAlgorithm(int id) {

		return getAlgorithmsTable().get(id);

	}

	private synchronized  Hashtable<Integer,Algorithm> getAlgorithmsTable() {

		if (algorithmsTable == null){
			algorithmsTable  = new Hashtable<Integer, Algorithm>();
		}
		return algorithmsTable;
	}

	@Override
	public List<Database> getCrossableDatabases() {

		ArrayList<Database> ret = new ArrayList<Database>();

		try {

			Statement StmtgetCrossableDatabases = getConnection().createStatement();

			ResultSet RSgetCrossableDatabases = StmtgetCrossableDatabases.executeQuery
					("SELECT D.idDatabase" +
							" FROM `Database` D WHERE D.Crossable=1");

			while (RSgetCrossableDatabases.next()) {

				int id = RSgetCrossableDatabases.getInt(1);

				ret.add(getDatabase(id));

			}

			RSgetCrossableDatabases.close();
			StmtgetCrossableDatabases.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<Execution> getExecutions() {

		ArrayList<Execution> ret = new ArrayList<Execution>();

		try {

			Statement StmtgetExecutions = getConnection().createStatement();

			ResultSet RSgetExecutions = StmtgetExecutions.executeQuery
					("SELECT E.idExecution FROM Execution E");

			while (RSgetExecutions.next()) {

				int executionId = RSgetExecutions.getInt(1);

				ret.add(new Execution(executionId, getEvaluations(executionId)));

			}

			RSgetExecutions.close();
			StmtgetExecutions.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;


	}

	private List<Evaluation> getEvaluations(int executionId) {

		ArrayList<Evaluation> ret = new ArrayList<Evaluation>();


		try {

			Statement StmtgetEvaluations = getConnection().createStatement();

			ResultSet RSgetEvaluations = StmtgetEvaluations.executeQuery
					("SELECT E.idCombination, E.idDatabase, limit, Description FROM ExecutionCombination E " +
							"WHERE E.idExecution = " + executionId + " ORDER BY position");

			while (RSgetEvaluations.next()) {

				int combinationID = RSgetEvaluations.getInt(1);
				int databaseID = RSgetEvaluations.getInt(2);
				int limit = RSgetEvaluations.getInt(3);

				ret.add(Evaluation.getEvaluation(getCombination(combinationID),getDatabase(databaseID),limit));

			}

			RSgetEvaluations.close();
			StmtgetEvaluations.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<Combination> getCombinations(Version version, int workloadId) {

		ArrayList<Combination> ret = new ArrayList<Combination>();

		try {

			Statement StmtgetCombinations = getConnection().createStatement();

			ResultSet RSgetCombinations = StmtgetCombinations.executeQuery
					("SELECT C.*  FROM Combination C WHERE" +
							" C.idVersion = " + version.getId() + " AND C.idWorkload = " +  workloadId);

			while (RSgetCombinations.next()) {

				ret.add(getCombination(RSgetCombinations));

			}

			RSgetCombinations.close();

			StmtgetCombinations.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<Database> getSearchableDatabases(String computername) {

		ArrayList<Database> ret = new ArrayList<Database>();


		try {

			Statement StmtgetSearchableDatabases = getConnection().createStatement();

			ResultSet RSgetSearchableDatabases;

			if (computername != null)
				RSgetSearchableDatabases = StmtgetSearchableDatabases.executeQuery
				("SELECT D.idDatabase" +
						" FROM `Database` D join HostDatabase H on D.idDatabase= H.idDatabase WHERE D.Searchable=1 and H.host = '" + format(computername) + "'");
			else {

				RSgetSearchableDatabases = StmtgetSearchableDatabases.executeQuery
						("SELECT D.idDatabase FROM `Database` D WHERE D.Searchable=1");

			}

			while (RSgetSearchableDatabases.next()) {

				int id = RSgetSearchableDatabases.getInt(1);

				ret.add(getDatabase(id));

			}

			RSgetSearchableDatabases.close();
			StmtgetSearchableDatabases.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<ExecutionAlternative> getActiveExecutionAlternatives(){

		ArrayList<ExecutionAlternative> ret = getActiveExecutionAlternativesId();

		for (ExecutionAlternative exAlt : ret){

			Integer parametersId = exAlt.getParameterId();

			Parametrizable parameters = getExecutionParameters(parametersId);

			WorkloadModel wm = getWorkloadModel(exAlt.getWorkloadModelId());

			Version version = getVersion(exAlt.getVersionId(), wm);

			DatabasesModel dm = loadDatabasesModel(version);

			exAlt.setSource(SourceFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.SOURCE).getString(),parameters.loadParameter(ExecutionAlternativeEnum.GENERATION_SOURCE),dm,Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.EXECUTION_ORDER).getString())));

			exAlt.setVersion(version);

			exAlt.setGeneration(GenerationFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.GENERATION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.GENERATION_PARAMETERS),dm));

			exAlt.setFinishingStrategy(FinishingStrategyFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.FINISHING_STRATEGY).getString(), Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS_FINISHING).getString()),parameters.loadParameter(ExecutionAlternativeEnum.FINISHING_STRATEGY_PARAMETERS)));

			exAlt.setAdaptiveStrategy(AdaptiveStrategyFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.ADAPTIVE_STRATEGY).getString(),Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS).getString()), parameters.loadParameter(ExecutionAlternativeEnum.ADAPTIVE_STRATEGY_PARAMETERS),this,dm));

			ExecutionPolicy ep = ExecutionPolicy.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.EXECUTION_POLICY_PARAMETERS));

			exAlt.setExecutionPolicy(ep);

			exAlt.setUpdateStrategy(UpdateStrategyFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.COLLECTING_DOCUMENT_STRATEGY).getString(), parameters.loadParameter(ExecutionAlternativeEnum.COLLECTING_DOCUMENT_STRATEGY_PARAMETERS)));

			exAlt.setDatabaseSelection(DatabaseSelectionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.DATABASE_SELECTION).getString()));

			exAlt.setSelector(StatisticsForSampleSelectorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.STATISTICS_FOR_SAMPLE_SELECTOR).getString()));

			exAlt.setScheduler(new Scheduler<Evaluation,Query,LimitedNumberPolicy>(Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.STOP_WHILE_UPDATE).getString()),new LimitedNumberPolicy(ep.getDatabasesToContact())).getInstance(parameters.loadParameter(ExecutionAlternativeEnum.SCHEDULER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.SCHEDULER_PARAMETERS)));

			exAlt.setQueryScheduler(new Scheduler<Query,Query,LimitedNumberPolicy>(false,null).getInstance(parameters.loadParameter(ExecutionAlternativeEnum.QUERY_SCHEDULER).getString(),parameters.loadParameter(ExecutionAlternativeEnum.QUERY_SCHEDULER_PARAMETERS)));

			exAlt.setAlgorithmSelection(AlgorithmSelectionFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.ALGORITHM_SELECTION).getString(),parameters.loadParameter(ExecutionAlternativeEnum.ALGORITHM_SELECTION_PARAMETERS)));

			RelationExtractionSystem res = getRelationExtractionSystem(exAlt.getExtractionSystemId(),wm,exAlt.getRelationConfiguration());

			exAlt.setInformationExtractionSystem(res);

			exAlt.setContentExtractor(ContentExtractorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CONTENT_EXTRACTION).getString()));
			//TODO Something with evaluations concurrently...??


		}

		return ret;

	}

	private RelationExtractionSystem getRelationExtractionSystem(
			int extractionSystemId, WorkloadModel workload, int relationConfiguration) {

		return InformationExtractionFactory.generateInstance(extractionSystemId,workload,this,relationConfiguration);

	}

	@Override
	public Pair<String,String> getInformationExtractionDescription(int extractionSystemId, int idRelationConfiguration) {

		Pair<String,String> ret = null;

		try {

			//			if (PStmtresDescription == null){
			//				PStmtresDescription = getConnection().prepareStatement(resDescriptionString);
			//			}else
			//				PStmtresDescription.clearParameters();

			PreparedStatement PStmtresDescription = getConnection().prepareStatement(resDescriptionString);

			PStmtresDescription.setInt(1, extractionSystemId);
			PStmtresDescription.setInt(2, idRelationConfiguration);


			ResultSet RSgetresDescription = PStmtresDescription.executeQuery();

			while (RSgetresDescription.next()) {

				ret = new Pair<String,String>(RSgetresDescription.getString(1),RSgetresDescription.getString(2));

			}

			RSgetresDescription.close();
			PStmtresDescription.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private DatabasesModel loadDatabasesModel(Version version) {

		DatabasesModel databasesModel = getModelTable().get(version.getId());

		if (databasesModel == null){

			databasesModel = new DatabasesModel(getDatabases());

			loadSimilarities(databasesModel, version);

			loadGroups(databasesModel, version);

			getModelTable().put(version.getId(),databasesModel);

		}

		return databasesModel;
	}

	private synchronized Hashtable<Integer, DatabasesModel> getModelTable() {

		if (model == null){
			model = new Hashtable<Integer, DatabasesModel>();
		}
		return model;
	}

	private void loadGroups(DatabasesModel databasesModel, Version version) {


		try {

			Statement StmtloadGroups = getConnection().createStatement();

			ResultSet RSloadGroups = StmtloadGroups.executeQuery
					("SELECT D.idCluster,D.idDatabase,C.description,D.version_pos_seed, D.version_neg_seed,D.idWorkload FROM " +
							"DatabaseCluster D, ClusterFunction C " +
							"WHERE D.idClusterFunction = C.idClusterFunction AND D.idVersion = " + version.getId());

			while (RSloadGroups.next()) {

				databasesModel.addBelongsToGroup(RSloadGroups.getInt(1),RSloadGroups.getInt(2),ClusterFunctionEnum.valueOf(RSloadGroups.getString(3)),RSloadGroups.getInt(4),RSloadGroups.getInt(5),RSloadGroups.getInt(6));

			}

			RSloadGroups.close();

			StmtloadGroups.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}


	}

	private void loadSimilarities(DatabasesModel databasesModel, Version version) {


		try {

			Statement StmtloadSimilarities = getConnection().createStatement();

			ResultSet RSloadSimilarities = StmtloadSimilarities.executeQuery
					("SELECT S.idDatabase_1,S.idDatabase_2,M.Description,S.similarityValue,S.version_pos_seed, S.version_neg_seed,S.idWorkload FROM " +
							"SimilarityDatabase S, SimilarityMeasure M " +
							"WHERE S.SimilarityMeasure = M.idSimilarityMeasure AND S.idVersion = " + version.getId());

			while (RSloadSimilarities.next()) {

				databasesModel.addSimilarity(RSloadSimilarities.getInt(1),RSloadSimilarities.getInt(2),SimilarityFunctionEnum.valueOf(RSloadSimilarities.getString(3)),RSloadSimilarities.getDouble(4),RSloadSimilarities.getInt(5),RSloadSimilarities.getInt(6),RSloadSimilarities.getInt(7));

			}

			RSloadSimilarities.close();

			StmtloadSimilarities.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

	}

	@Override
	public WorkloadModel getWorkloadModel(int workloadModelId) {

		WorkloadModel wm = getCachedWorkloadModel(workloadModelId);

		if (wm != null)
			return wm;


		try {

			Statement StmtgetWorkloadModel = getConnection().createStatement();

			ResultSet RSgetWorkloadModel = StmtgetWorkloadModel.executeQuery("SELECT W.* FROM Workload W WHERE W.idWorkload = " + workloadModelId);

			while (RSgetWorkloadModel.next()) {

				int id = RSgetWorkloadModel.getInt(1);

				wm = new WorkloadModel(id, getWorkloadDescription(id), getWorkloadTuples(id), getWorkloadQueries(id),getWorkloadRelations(id));

				getWorkloadModelsTable().put(wm.getId(),wm);

				return wm;

			}

			RSgetWorkloadModel.close();

			StmtgetWorkloadModel.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	protected String[] getWorkloadRelations(int idWorkload) {

		List<String> ret = new ArrayList<String>();

		try {

			Statement StmtgetWorkloadRelations = getConnection().createStatement();

			ResultSet RSgetWorkloadRelations = StmtgetWorkloadRelations.executeQuery
					("select description from `AutomaticQueryGeneration`.`RelationshipType` R join WorkloadRelation W on (W.idRelationshipType = R.idRelationshipType) where idWorkload = " + idWorkload);

			while (RSgetWorkloadRelations.next()) {

				ret.add(RSgetWorkloadRelations.getString(1));

			}

			RSgetWorkloadRelations.close();

			StmtgetWorkloadRelations.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret.toArray(new String[ret.size()]);

	}

	private WorkloadModel getCachedWorkloadModel(int workloadModelId) {

		return getWorkloadModelsTable().get(workloadModelId);
	}

	private synchronized  Hashtable<Integer,WorkloadModel> getWorkloadModelsTable() {

		if (workloadModelTable == null){
			workloadModelTable  = new Hashtable<Integer,WorkloadModel>();
		}
		return workloadModelTable;
	}

	private ArrayList<ExecutionAlternative> getActiveExecutionAlternativesId() {

		ArrayList<ExecutionAlternative> ret = new ArrayList<ExecutionAlternative>();


		try {

			Statement StmtgetExecutionAlternativesId = getConnection().createStatement();

			ResultSet RSgetExecutionAlternativesId = StmtgetExecutionAlternativesId.executeQuery
					("SELECT E.idExecutionAlternative,E.idParameter,E.idVersion,E.idWorkload,E.idRelationConfiguration,E.idInformationExtractionSystem FROM ExecutionAlternative E WHERE E.active = 1 " +
							"ORDER BY E.idExecutionAlternative");

			while (RSgetExecutionAlternativesId.next()) {

				ret.add(new ExecutionAlternative(RSgetExecutionAlternativesId.getInt(1), RSgetExecutionAlternativesId.getInt(2),RSgetExecutionAlternativesId.getInt(3),RSgetExecutionAlternativesId.getInt(4),RSgetExecutionAlternativesId.getInt(5),RSgetExecutionAlternativesId.getInt(6)));

			}

			RSgetExecutionAlternativesId.close();

			StmtgetExecutionAlternativesId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	private Parametrizable getExecutionParameters(Integer parametersId) {

		Hashtable<String, String> ret = readParameters(parametersId);

		return generateParametrizable(ret);

	}

	private Hashtable<String, String> readParameters(Integer parametersId) {
		Hashtable<String, String> ret = new Hashtable<String, String>();

		try {

			Statement StmtgetParameters = getConnection().createStatement();

			ResultSet RSgetParameters = StmtgetParameters.executeQuery
					("SELECT A.Name,P.value FROM ParameterAssociation P, Attribute A " +
							"WHERE P.idParameter = " + parametersId + " AND P.idAttribute = A.idAttribute");

			while (RSgetParameters.next()) {

				ret.put(RSgetParameters.getString(1), RSgetParameters.getString(2));

			}

			RSgetParameters.close();

			StmtgetParameters.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}
		return ret;
	}

	private Parametrizable generateParametrizable(
			Hashtable<String, String> parameters) {

		TableParameters ret = new TableParameters();

		//SOURCE

		Parametrizable sourceText = new StringParameters(ExecutionAlternativeEnum.SOURCE,parameters.get(ExecutionAlternativeEnum.SOURCE.toString()));

		ret.addParameter(ExecutionAlternativeEnum.SOURCE, sourceText);

		Parametrizable generationSource = loadGenerationSource(parameters);

		ret.addParameter(ExecutionAlternativeEnum.GENERATION_SOURCE, generationSource);

		Parametrizable executionOrder = new StringParameters(ExecutionAlternativeEnum.EXECUTION_ORDER,parameters.get(ExecutionAlternativeEnum.EXECUTION_ORDER.toString()));

		ret.addParameter(ExecutionAlternativeEnum.EXECUTION_ORDER, executionOrder);

		//GENERATION

		Parametrizable generationText = new StringParameters(ExecutionAlternativeEnum.GENERATION,parameters.get(ExecutionAlternativeEnum.GENERATION.toString()));

		ret.addParameter(ExecutionAlternativeEnum.GENERATION, generationText);

		Parametrizable generation = loadGeneration(parameters);

		ret.addParameter(ExecutionAlternativeEnum.GENERATION_PARAMETERS, generation);

		//FINISHING STRATEGY

		Parametrizable finishingStrategyText = new StringParameters(ExecutionAlternativeEnum.FINISHING_STRATEGY,parameters.get(ExecutionAlternativeEnum.FINISHING_STRATEGY.toString()));

		ret.addParameter(ExecutionAlternativeEnum.FINISHING_STRATEGY, finishingStrategyText);

		Parametrizable afterNDocumentsText = new StringParameters(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS_FINISHING,parameters.get(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS_FINISHING.toString()));

		ret.addParameter(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS_FINISHING, afterNDocumentsText);

		Parametrizable finishingStrategy = loadFinishingStrategy(parameters);

		ret.addParameter(ExecutionAlternativeEnum.FINISHING_STRATEGY_PARAMETERS, finishingStrategy);

		//ADAPTIVE STRATEGY

		Parametrizable adaptiveStrategyText = new StringParameters(ExecutionAlternativeEnum.ADAPTIVE_STRATEGY,parameters.get(ExecutionAlternativeEnum.ADAPTIVE_STRATEGY.toString()));

		ret.addParameter(ExecutionAlternativeEnum.ADAPTIVE_STRATEGY, adaptiveStrategyText);

		Parametrizable afterNDocsText = new StringParameters(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS,parameters.get(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS.toString()));

		ret.addParameter(ExecutionAlternativeEnum.MEASURE_AFTER_N_DOCUMENTS, afterNDocsText);

		Parametrizable adaptiveStrategy = loadAdaptiveStrategy(parameters);

		ret.addParameter(ExecutionAlternativeEnum.ADAPTATION_STRATEGY_PARAMETERS, adaptiveStrategy);

		//EXECUTION POLICY

		Parametrizable executionPolicy = loadExecutionPolicy(parameters);

		ret.addParameter(ExecutionAlternativeEnum.EXECUTION_POLICY_PARAMETERS, executionPolicy);

		//UPDATE STRATEGY

		Parametrizable updateStrategyText = new StringParameters(ExecutionAlternativeEnum.COLLECTING_DOCUMENT_STRATEGY,parameters.get(ExecutionAlternativeEnum.COLLECTING_DOCUMENT_STRATEGY.toString()));

		ret.addParameter(ExecutionAlternativeEnum.COLLECTING_DOCUMENT_STRATEGY, updateStrategyText);

		Parametrizable updateStrategy = loadUpdateStrategy(parameters);

		ret.addParameter(ExecutionAlternativeEnum.COLLECTING_DOCUMENT_STRATEGY_PARAMETERS, updateStrategy);

		//DATABASE SELECTION

		Parametrizable databaseSelectionText = new StringParameters(ExecutionAlternativeEnum.DATABASE_SELECTION,parameters.get(ExecutionAlternativeEnum.DATABASE_SELECTION.toString()));

		ret.addParameter(ExecutionAlternativeEnum.DATABASE_SELECTION, databaseSelectionText);

		//STATISTICS FOR SAMPLE SELECTOR

		Parametrizable statisticsForSampleSelectorText = new StringParameters(ExecutionAlternativeEnum.STATISTICS_FOR_SAMPLE_SELECTOR,parameters.get(ExecutionAlternativeEnum.STATISTICS_FOR_SAMPLE_SELECTOR.toString()));

		ret.addParameter(ExecutionAlternativeEnum.STATISTICS_FOR_SAMPLE_SELECTOR, statisticsForSampleSelectorText);

		//SCHEDULER

		Parametrizable stopWhileUpdateText = new StringParameters(ExecutionAlternativeEnum.STOP_WHILE_UPDATE,parameters.get(ExecutionAlternativeEnum.STOP_WHILE_UPDATE.toString()));

		ret.addParameter(ExecutionAlternativeEnum.STOP_WHILE_UPDATE, stopWhileUpdateText);

		Parametrizable schedulerText = new StringParameters(ExecutionAlternativeEnum.SCHEDULER,parameters.get(ExecutionAlternativeEnum.SCHEDULER.toString()));

		ret.addParameter(ExecutionAlternativeEnum.SCHEDULER, schedulerText);

		Parametrizable scheduler = loadScheduler(parameters);

		ret.addParameter(ExecutionAlternativeEnum.SCHEDULER_PARAMETERS, scheduler);

		//QUERY SCHEDULER

		Parametrizable querySchedulerText = new StringParameters(ExecutionAlternativeEnum.QUERY_SCHEDULER,parameters.get(ExecutionAlternativeEnum.QUERY_SCHEDULER.toString()));

		ret.addParameter(ExecutionAlternativeEnum.QUERY_SCHEDULER, querySchedulerText);

		Parametrizable queryScheduler = loadQueryScheduler(parameters);

		ret.addParameter(ExecutionAlternativeEnum.QUERY_SCHEDULER_PARAMETERS, queryScheduler);

		//ALGORITHM SELECTION

		Parametrizable algorithmSelectionText = new StringParameters(ExecutionAlternativeEnum.ALGORITHM_SELECTION,parameters.get(ExecutionAlternativeEnum.ALGORITHM_SELECTION.toString()));

		ret.addParameter(ExecutionAlternativeEnum.ALGORITHM_SELECTION, algorithmSelectionText);

		Parametrizable algorithmSelection = loadAlgorithmSelection(parameters);

		ret.addParameter(ExecutionAlternativeEnum.ALGORITHM_SELECTION_PARAMETERS, algorithmSelection);

		//		//SEARCHER
		//		
		//		Parametrizable resultDocumentHandlerText = new StringParameters(ExecutionAlternativeEnum.RESULT_DOCUMENT_HANDLER, parameters.get(ExecutionAlternativeEnum.RESULT_DOCUMENT_HANDLER.toString()));
		//		
		//		ret.addParameter(ExecutionAlternativeEnum.RESULT_DOCUMENT_HANDLER, resultDocumentHandlerText);
		//		
		//		Parametrizable navigationHandlerText = new StringParameters(ExecutionAlternativeEnum.NAVIGATION_HANDLER, parameters.get(ExecutionAlternativeEnum.NAVIGATION_HANDLER.toString()));
		//		
		//		ret.addParameter(ExecutionAlternativeEnum.NAVIGATION_HANDLER, navigationHandlerText);
		//		
		//		Parametrizable navigationHandler = loadNavigationHandler(parameters);
		//		
		//		ret.addParameter(ExecutionAlternativeEnum.NAVIGATION_HANDLER_PARAMETERS, navigationHandler);
		//		
		//		Parametrizable QueryResultPageHandlerText = new StringParameters(ExecutionAlternativeEnum.QUERY_RESULT_PAGE_HANDLER, parameters.get(ExecutionAlternativeEnum.QUERY_RESULT_PAGE_HANDLER.toString()));
		//		
		//		ret.addParameter(ExecutionAlternativeEnum.QUERY_RESULT_PAGE_HANDLER, QueryResultPageHandlerText);
		//		
		//		Parametrizable htmlTagCleanerText = new StringParameters(ExecutionAlternativeEnum.HTML_TAG_CLEANER, parameters.get(ExecutionAlternativeEnum.HTML_TAG_CLEANER.toString()));
		//		
		//		ret.addParameter(ExecutionAlternativeEnum.HTML_TAG_CLEANER, htmlTagCleanerText);

		//CONTENT_EXTRACTOR

		Parametrizable contentExtractionText = new StringParameters(ExecutionAlternativeEnum.CONTENT_EXTRACTION,parameters.get(ExecutionAlternativeEnum.CONTENT_EXTRACTION.toString()));

		ret.addParameter(ExecutionAlternativeEnum.CONTENT_EXTRACTION, contentExtractionText);

		//END

		return ret;

	}

	//	private Parametrizable loadNavigationHandler(
	//			Hashtable<String, String> parameters) {
	//		
	//		TableParameters tp = new TableParameters();
	//		
	//		Parametrizable nhText =  new StringParameters(ExecutionAlternativeEnum.SEARCH_ROUND_ID,parameters.get(ExecutionAlternativeEnum.SEARCH_ROUND_ID.toString()));
	//		
	//		tp.addParameter(ExecutionAlternativeEnum.SEARCH_ROUND_ID, nhText);
	//		
	//		return tp;
	//		
	//	}

	private Parametrizable loadAlgorithmSelection(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		AlgorithmEnum[] values = AlgorithmEnum.values();

		for (int i = 0; i < values.length; i++) {

			ExecutionAlternativeEnum enumActual = ExecutionAlternativeEnum.valueOf(values[i].toString());

			if (parameters.containsKey(values[i].toString())){

				Parametrizable text =  new StringParameters(enumActual,parameters.get(values[i].toString()));

				tp.addParameter(enumActual, text);

			}
		}

		return tp;

	}

	private Parametrizable loadQueryScheduler(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable roundRobinQuantumText =  new StringParameters(ExecutionAlternativeEnum.ROUND_ROBIN_QUANTUM,parameters.get(ExecutionAlternativeEnum.QUERY_ROUND_ROBIN_QUANTUM.toString()));

		tp.addParameter(ExecutionAlternativeEnum.ROUND_ROBIN_QUANTUM, roundRobinQuantumText);

		return tp;

	}

	private Parametrizable loadScheduler(Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable roundRobinQuantumText =  new StringParameters(ExecutionAlternativeEnum.ROUND_ROBIN_QUANTUM,parameters.get(ExecutionAlternativeEnum.ROUND_ROBIN_QUANTUM.toString()));

		tp.addParameter(ExecutionAlternativeEnum.ROUND_ROBIN_QUANTUM, roundRobinQuantumText);

		return tp;

	}

	private Parametrizable loadUpdateStrategy(
			Hashtable<String, String> parameters) {

		return new TableParameters();

	}

	private Parametrizable loadExecutionPolicy(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable databasesToContactText =  new StringParameters(ExecutionAlternativeEnum.DATABASES_TO_CONTACT,parameters.get(ExecutionAlternativeEnum.DATABASES_TO_CONTACT.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DATABASES_TO_CONTACT, databasesToContactText);

		Parametrizable queriesPerDatabaseText =  new StringParameters(ExecutionAlternativeEnum.QUERIES_PER_DATABASE,parameters.get(ExecutionAlternativeEnum.QUERIES_PER_DATABASE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERIES_PER_DATABASE, queriesPerDatabaseText);

		Parametrizable queriesASecondText =  new StringParameters(ExecutionAlternativeEnum.QUERIES_A_SECOND,parameters.get(ExecutionAlternativeEnum.QUERIES_A_SECOND.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERIES_A_SECOND, queriesASecondText);

		Parametrizable queryProcessingTimeText =  new StringParameters(ExecutionAlternativeEnum.QUERY_PROCESSING_TIME,parameters.get(ExecutionAlternativeEnum.QUERY_PROCESSING_TIME.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERY_PROCESSING_TIME, queryProcessingTimeText);

		Parametrizable totalDocumentsToRetrieveText =  new StringParameters(ExecutionAlternativeEnum.TOTAL_DOCUMENTS_TO_RETRIEVE,parameters.get(ExecutionAlternativeEnum.TOTAL_DOCUMENTS_TO_RETRIEVE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.TOTAL_DOCUMENTS_TO_RETRIEVE, totalDocumentsToRetrieveText);

		Parametrizable DocumentsPerQueryText =  new StringParameters(ExecutionAlternativeEnum.DATABASE_LIMIT,parameters.get(ExecutionAlternativeEnum.DATABASE_LIMIT.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DATABASE_LIMIT, DocumentsPerQueryText);

		Parametrizable extractionASecondText =  new StringParameters(ExecutionAlternativeEnum.DOCUMENTS_TO_EXTRACT_A_SECOND,parameters.get(ExecutionAlternativeEnum.DOCUMENTS_TO_EXTRACT_A_SECOND.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DOCUMENTS_TO_EXTRACT_A_SECOND, extractionASecondText);

		Parametrizable ExtractionTimeText =  new StringParameters(ExecutionAlternativeEnum.EXTRACTION_TIME,parameters.get(ExecutionAlternativeEnum.EXTRACTION_TIME.toString()));

		tp.addParameter(ExecutionAlternativeEnum.EXTRACTION_TIME, ExtractionTimeText);

		Parametrizable retrievalText =  new StringParameters(ExecutionAlternativeEnum.RETRIEVAL_TIME,parameters.get(ExecutionAlternativeEnum.RETRIEVAL_TIME.toString()));

		tp.addParameter(ExecutionAlternativeEnum.RETRIEVAL_TIME, retrievalText);

		Parametrizable informationExtractionInstancesText =  new StringParameters(ExecutionAlternativeEnum.IE_INSTANCES,parameters.get(ExecutionAlternativeEnum.IE_INSTANCES.toString()));

		tp.addParameter(ExecutionAlternativeEnum.IE_INSTANCES, informationExtractionInstancesText);

		Parametrizable isIESequentialText =  new StringParameters(ExecutionAlternativeEnum.IE_SEQUENTIAL,parameters.get(ExecutionAlternativeEnum.IE_SEQUENTIAL.toString()));

		tp.addParameter(ExecutionAlternativeEnum.IE_SEQUENTIAL, isIESequentialText);

		Parametrizable isQSequentialText =  new StringParameters(ExecutionAlternativeEnum.Q_SEQUENTIAL,parameters.get(ExecutionAlternativeEnum.Q_SEQUENTIAL.toString()));

		tp.addParameter(ExecutionAlternativeEnum.Q_SEQUENTIAL, isQSequentialText);

		return tp;
	}

	private Parametrizable loadAdaptiveStrategy(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable adaptationConditionText =  new StringParameters(ExecutionAlternativeEnum.ADAPTATION_CONDITION,parameters.get(ExecutionAlternativeEnum.ADAPTATION_CONDITION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.ADAPTATION_CONDITION, adaptationConditionText);

		Parametrizable adaptationCondition = loadAdaptationCondition(parameters);

		tp.addParameter(ExecutionAlternativeEnum.ADAPTATION_CONDITION_PARAMETERS, adaptationCondition);

		Parametrizable adaptationStrategyText =  new StringParameters(ExecutionAlternativeEnum.ADAPTATION_STRATEGY,parameters.get(ExecutionAlternativeEnum.ADAPTATION_STRATEGY.toString()));

		tp.addParameter(ExecutionAlternativeEnum.ADAPTATION_STRATEGY, adaptationStrategyText);

		Parametrizable adaptationStrategy = loadAdaptationStrategy(parameters);

		tp.addParameter(ExecutionAlternativeEnum.ADAPTATION_STRATEGY_PARAMETERS, adaptationStrategy);

		return tp;

	}

	private Parametrizable loadAdaptationStrategy(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable nextGenerationAlgorithmText =  new StringParameters(ExecutionAlternativeEnum.NEXT_GENERATION_ALGORITHM,parameters.get(ExecutionAlternativeEnum.NEXT_GENERATION_ALGORITHM.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NEXT_GENERATION_ALGORITHM, nextGenerationAlgorithmText);

		Parametrizable nextGenerationText =  new StringParameters(ExecutionAlternativeEnum.NEXT_GENERATION,parameters.get(ExecutionAlternativeEnum.NEXT_GENERATION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NEXT_GENERATION, nextGenerationText);

		Parametrizable generation = loadNextGeneration(parameters);

		tp.addParameter(ExecutionAlternativeEnum.NEXT_GENERATION_PARAMETERS, generation);

		Parametrizable newDatabaseText =  new StringParameters(ExecutionAlternativeEnum.NEW_DATABASE,parameters.get(ExecutionAlternativeEnum.NEW_DATABASE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NEW_DATABASE, newDatabaseText);

		Parametrizable newAlgorithmText =  new StringParameters(ExecutionAlternativeEnum.NEW_ALGORITHM,parameters.get(ExecutionAlternativeEnum.NEW_ALGORITHM.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NEW_ALGORITHM, newAlgorithmText);

		Parametrizable newAlgorithm = loadNewAlgorithm(parameters);

		tp.addParameter(ExecutionAlternativeEnum.NEW_ALGORITHM_PARAMETERS, newAlgorithm);

		return tp;

	}

	private Parametrizable loadNewAlgorithm(Hashtable<String, String> parameters) {
		// TODO COMPLETE THIS!
		return new TableParameters();
	}

	private Parametrizable loadNextGeneration(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable crossableSourceText =  new StringParameters(ExecutionAlternativeEnum.CROSSABLE_SOURCE,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_SOURCE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CROSSABLE_SOURCE, crossableSourceText);

		Parametrizable crossableSource = loadNextCrossableSource(parameters);

		tp.addParameter(ExecutionAlternativeEnum.CROSSABLE_SOURCE_PARAMETERS, crossableSource);

		Parametrizable configurationText = new StringParameters(ExecutionAlternativeEnum.CROSSABLE_CONFIGURATION,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_CONFIGURATION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CROSSABLE_CONFIGURATION, configurationText);

		return tp;

	}

	private Parametrizable loadNextCrossableSource(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable databaseSimilarityText = new StringParameters(ExecutionAlternativeEnum.DATABASE_SIMILARITY,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_DATABASE_SIMILARITY.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DATABASE_SIMILARITY, databaseSimilarityText);

		Parametrizable clusterFunctionText = new StringParameters(ExecutionAlternativeEnum.CLUSTER_FUNCTION,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_CLUSTER_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION, clusterFunctionText);

		Parametrizable clusterFunction = loadNextCrossableClusterFunction(parameters);

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS, clusterFunction);

		return tp;

	}

	private Parametrizable loadNextCrossableClusterFunction(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable kMeansValueText = new StringParameters(ExecutionAlternativeEnum.K_MEANS_K_VALUE,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_K_MEANS_K_VALUE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.K_MEANS_K_VALUE, kMeansValueText);

		Parametrizable kMeansSimiliarityFunctionText = new StringParameters(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_K_MEANS_SIMILARITY_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION, kMeansSimiliarityFunctionText);

		Parametrizable FuzzyCMeansKValueText = new StringParameters(ExecutionAlternativeEnum.FUZZY_C_K_VALUE,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_FUZZY_C_K_VALUE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FUZZY_C_K_VALUE, FuzzyCMeansKValueText);

		Parametrizable FuzzySimilarityFunctionText = new StringParameters(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION,parameters.get(ExecutionAlternativeEnum.NEXT_CROSSABLE_FUZZY_C_SIMILARITY_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION, FuzzySimilarityFunctionText);

		return tp;

	}

	private Parametrizable loadAdaptationCondition(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable performanceCheckerText =  new StringParameters(ExecutionAlternativeEnum.PERFORMANCE_CHECKER,parameters.get(ExecutionAlternativeEnum.PERFORMANCE_CHECKER.toString()));

		tp.addParameter(ExecutionAlternativeEnum.PERFORMANCE_CHECKER, performanceCheckerText);

		Parametrizable performanceChecker = loadPerformanceChecker(parameters);

		tp.addParameter(ExecutionAlternativeEnum.PERFORMANCE_CHECKER_PARAMETERS, performanceChecker);

		Parametrizable performanceThresholdText =  new StringParameters(ExecutionAlternativeEnum.PERFORMANCE_THRESHOLD,parameters.get(ExecutionAlternativeEnum.PERFORMANCE_THRESHOLD.toString()));

		tp.addParameter(ExecutionAlternativeEnum.PERFORMANCE_THRESHOLD, performanceThresholdText);

		Parametrizable sampleEvaluatorText =  new StringParameters(ExecutionAlternativeEnum.SAMPLE_EVALUATOR,parameters.get(ExecutionAlternativeEnum.SAMPLE_EVALUATOR.toString()));

		tp.addParameter(ExecutionAlternativeEnum.SAMPLE_EVALUATOR, sampleEvaluatorText);

		Parametrizable sampleEvaluator = loadSampleEvaluator(parameters);

		tp.addParameter(ExecutionAlternativeEnum.SAMPLE_EVALUATOR_PARAMETERS, sampleEvaluator);

		Parametrizable NTokensText =  new StringParameters(ExecutionAlternativeEnum.TOKEN_COMPARATOR,parameters.get(ExecutionAlternativeEnum.TOKEN_COMPARATOR.toString()));

		tp.addParameter(ExecutionAlternativeEnum.TOKEN_COMPARATOR, NTokensText);

		Parametrizable NumberOfTokensText =  new StringParameters(ExecutionAlternativeEnum.NUMBER_OF_TOKENS,parameters.get(ExecutionAlternativeEnum.NUMBER_OF_TOKENS.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NUMBER_OF_TOKENS, NumberOfTokensText);

		return tp;

	}

	private Parametrizable loadSampleEvaluator(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable NumberOfDocuments =  new StringParameters(ExecutionAlternativeEnum.NUMBER_OF_DOCUMENTS,parameters.get(ExecutionAlternativeEnum.NUMBER_OF_DOCUMENTS.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NUMBER_OF_DOCUMENTS, NumberOfDocuments);

		Parametrizable NumberOfWordsText =  new StringParameters(ExecutionAlternativeEnum.NUMBER_OF_WORDS,parameters.get(ExecutionAlternativeEnum.NUMBER_OF_WORDS.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NUMBER_OF_WORDS, NumberOfWordsText);

		Parametrizable DistributionOfWordsText =  new StringParameters(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION,parameters.get(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION, DistributionOfWordsText);

		Parametrizable DistributionOfWords = loadDistributionOfWords(parameters);

		tp.addParameter(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION_PARAMETERS, DistributionOfWords);

		Parametrizable DistributionOfWordsSimilarityThresholdText =  new StringParameters(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION_SIMILARITY_THRESHOLD,parameters.get(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION_SIMILARITY_THRESHOLD.toString()));

		tp.addParameter(ExecutionAlternativeEnum.PROBABILISTIC_DISTRIBUTION_SIMILARITY_THRESHOLD, DistributionOfWordsSimilarityThresholdText);

		return tp;

	}

	private Parametrizable loadDistributionOfWords(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable powerLawAlphaText =  new StringParameters(ExecutionAlternativeEnum.POWER_LAW_ALPHA,parameters.get(ExecutionAlternativeEnum.POWER_LAW_ALPHA.toString()));

		tp.addParameter(ExecutionAlternativeEnum.POWER_LAW_ALPHA, powerLawAlphaText);

		Parametrizable powerLawBeta =  new StringParameters(ExecutionAlternativeEnum.POWER_LAW_BETA,parameters.get(ExecutionAlternativeEnum.POWER_LAW_BETA.toString()));

		tp.addParameter(ExecutionAlternativeEnum.POWER_LAW_BETA, powerLawBeta);

		Parametrizable powerLawEpsilon =  new StringParameters(ExecutionAlternativeEnum.POWER_LAW_EPSILON,parameters.get(ExecutionAlternativeEnum.POWER_LAW_EPSILON.toString()));

		tp.addParameter(ExecutionAlternativeEnum.POWER_LAW_EPSILON, powerLawEpsilon);

		Parametrizable weibullNu =  new StringParameters(ExecutionAlternativeEnum.WEIBULL_NU,parameters.get(ExecutionAlternativeEnum.WEIBULL_NU.toString()));

		tp.addParameter(ExecutionAlternativeEnum.WEIBULL_NU, weibullNu);

		Parametrizable weibullBeta =  new StringParameters(ExecutionAlternativeEnum.WEIBULL_BETA,parameters.get(ExecutionAlternativeEnum.WEIBULL_BETA.toString()));

		tp.addParameter(ExecutionAlternativeEnum.WEIBULL_BETA, weibullBeta);

		return tp;

	}

	private Parametrizable loadPerformanceChecker(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable uselessQueryConditionText =  new StringParameters(ExecutionAlternativeEnum.USELESS_QUERY_CONDITION,parameters.get(ExecutionAlternativeEnum.USELESS_QUERY_CONDITION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.USELESS_QUERY_CONDITION, uselessQueryConditionText);

		Parametrizable uselessQueryThresholdText =  new StringParameters(ExecutionAlternativeEnum.USELESS_QUERY_THRESHOLD,parameters.get(ExecutionAlternativeEnum.USELESS_QUERY_THRESHOLD.toString()));

		tp.addParameter(ExecutionAlternativeEnum.USELESS_QUERY_THRESHOLD, uselessQueryThresholdText);

		return tp;

	}

	private Parametrizable loadFinishingStrategy(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable performanceCheckerText =  new StringParameters(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_COMPARATOR,parameters.get(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_COMPARATOR.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_COMPARATOR, performanceCheckerText);

		Parametrizable documentPerformance = loadDocumentPerformanceFinishing(parameters);

		tp.addParameter(ExecutionAlternativeEnum.DOCUMENT_PERFORMANCE_COMPARATOR_PARAMETERS, documentPerformance);

		Parametrizable documentPerformanceThresholdText =  new StringParameters(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_THRESHOLD,parameters.get(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_THRESHOLD.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DOCUMENTS_PERFORMANCE_THRESHOLD, documentPerformanceThresholdText);

		Parametrizable queryPerformanceText =  new StringParameters(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING,parameters.get(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING, queryPerformanceText);

		Parametrizable queryPerformance = loadQueryPerformanceFinishing(parameters);

		tp.addParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_FINISHING_PARAMETERS, queryPerformance);

		Parametrizable queryPerformanceThresholdText =  new StringParameters(ExecutionAlternativeEnum.QUERIES_PERFORMANCE_THRESHOLD,parameters.get(ExecutionAlternativeEnum.QUERIES_PERFORMANCE_THRESHOLD.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERIES_PERFORMANCE_THRESHOLD, queryPerformanceThresholdText);

		return tp;

	}

	private Parametrizable loadQueryPerformanceFinishing(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable uselessQueryConditionText =  new StringParameters(ExecutionAlternativeEnum.USELESS_QUERY_CONDITION,parameters.get(ExecutionAlternativeEnum.FINISHING_USELESS_QUERY_CONDITION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.USELESS_QUERY_CONDITION, uselessQueryConditionText);

		Parametrizable uselessQueryThresholdText =  new StringParameters(ExecutionAlternativeEnum.USELESS_QUERY_THRESHOLD,parameters.get(ExecutionAlternativeEnum.FINISHING_USELESS_QUERY_THRESHOLD.toString()));

		tp.addParameter(ExecutionAlternativeEnum.USELESS_QUERY_THRESHOLD, uselessQueryThresholdText);

		return tp;

	}

	private Parametrizable loadDocumentPerformanceFinishing(
			Hashtable<String, String> parameters) {
		//They don't need parameters
		return new TableParameters();
	}

	private Parametrizable loadGeneration(Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable crossableSourceText =  new StringParameters(ExecutionAlternativeEnum.CROSSABLE_SOURCE,parameters.get(ExecutionAlternativeEnum.CROSSABLE_SOURCE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CROSSABLE_SOURCE, crossableSourceText);

		Parametrizable crossableSource = loadCrossableSource(parameters);

		tp.addParameter(ExecutionAlternativeEnum.CROSSABLE_SOURCE_PARAMETERS, crossableSource);

		Parametrizable configurationText = new StringParameters(ExecutionAlternativeEnum.CROSSABLE_CONFIGURATION,parameters.get(ExecutionAlternativeEnum.CROSSABLE_CONFIGURATION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CROSSABLE_CONFIGURATION, configurationText);

		return tp;

	}

	private Parametrizable loadCrossableSource(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable databaseSimilarityText = new StringParameters(ExecutionAlternativeEnum.DATABASE_SIMILARITY,parameters.get(ExecutionAlternativeEnum.CROSSABLE_DATABASE_SIMILARITY.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DATABASE_SIMILARITY, databaseSimilarityText);

		Parametrizable clusterFunctionText = new StringParameters(ExecutionAlternativeEnum.CLUSTER_FUNCTION,parameters.get(ExecutionAlternativeEnum.CROSSABLE_CLUSTER_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION, clusterFunctionText);

		Parametrizable clusterFunction = loadCrossableClusterFunction(parameters);

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS, clusterFunction);

		return tp;


	}

	private Parametrizable loadCrossableClusterFunction(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable kMeansValueText = new StringParameters(ExecutionAlternativeEnum.K_MEANS_K_VALUE,parameters.get(ExecutionAlternativeEnum.CROSSABLE_K_MEANS_K_VALUE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.K_MEANS_K_VALUE, kMeansValueText);

		Parametrizable kMeansSimiliarityFunctionText = new StringParameters(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION,parameters.get(ExecutionAlternativeEnum.CROSSABLE_K_MEANS_SIMILARITY_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION, kMeansSimiliarityFunctionText);

		Parametrizable FuzzyCMeansKValueText = new StringParameters(ExecutionAlternativeEnum.FUZZY_C_K_VALUE,parameters.get(ExecutionAlternativeEnum.CROSSABLE_FUZZY_C_K_VALUE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FUZZY_C_K_VALUE, FuzzyCMeansKValueText);

		Parametrizable FuzzySimilarityFunctionText = new StringParameters(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION,parameters.get(ExecutionAlternativeEnum.CROSSABLE_FUZZY_C_SIMILARITY_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION, FuzzySimilarityFunctionText);

		return tp;

	}

	private Parametrizable loadGenerationSource(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable databaseSimilarityText = new StringParameters(ExecutionAlternativeEnum.DATABASE_SIMILARITY,parameters.get(ExecutionAlternativeEnum.DATABASE_SIMILARITY.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DATABASE_SIMILARITY, databaseSimilarityText);

		Parametrizable clusterFunctionText = new StringParameters(ExecutionAlternativeEnum.CLUSTER_FUNCTION,parameters.get(ExecutionAlternativeEnum.CLUSTER_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION, clusterFunctionText);

		Parametrizable clusterFunction = loadGenerationClusterFunction(parameters);

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS, clusterFunction);

		return tp;

	}

	private Parametrizable loadGenerationClusterFunction(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable kMeansValueText = new StringParameters(ExecutionAlternativeEnum.K_MEANS_K_VALUE,parameters.get(ExecutionAlternativeEnum.K_MEANS_K_VALUE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.K_MEANS_K_VALUE, kMeansValueText);

		Parametrizable kMeansSimiliarityFunctionText = new StringParameters(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION,parameters.get(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.K_MEANS_SIMILARITY_FUNCTION, kMeansSimiliarityFunctionText);

		Parametrizable FuzzyCMeansKValueText = new StringParameters(ExecutionAlternativeEnum.FUZZY_C_K_VALUE,parameters.get(ExecutionAlternativeEnum.FUZZY_C_K_VALUE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FUZZY_C_K_VALUE, FuzzyCMeansKValueText);

		Parametrizable FuzzySimilarityFunctionText = new StringParameters(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION,parameters.get(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FUZZY_C_SIMILARITY_FUNCTION, FuzzySimilarityFunctionText);

		return tp;

	}

	public List<Database> getDatabases() {

		ArrayList<Database> ret = new ArrayList<Database>();

		try {

			Statement StmtgetDatabases = getConnection().createStatement();

			ResultSet RSgetDatabases = StmtgetDatabases.executeQuery
					("SELECT D.idDatabase" +
							" FROM `Database` D");

			while (RSgetDatabases.next()) {

				int id = RSgetDatabases.getInt(1);

				ret.add(getDatabase(id));

			}

			RSgetDatabases.close();
			StmtgetDatabases.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<Query> getQueries(Combination combination) {

		List<Query> queries = getCachedQueries(combination);

		if (queries != null)
			return queries;

		ArrayList<Query> ret = new ArrayList<Query>();

		try {

			Statement StmtgetQueries = getConnection().createStatement();

			ResultSet RSgetQueries = StmtgetQueries.executeQuery
					("SELECT Q.query,Q.time,Q.Position FROM Query Q WHERE Q.idCombination = " + combination.getId() + " ORDER BY time,Position");

			while (RSgetQueries.next()) {

				ret.add(new Query(combination,getTextQueryFromId(RSgetQueries.getLong(1)),RSgetQueries.getLong(2),RSgetQueries.getInt(3)));

			}

			getQueriesTable().put(combination.getId(), ret);

			RSgetQueries.close();

			StmtgetQueries.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}


	public TextQuery getTextQueryFromId(long qId) {

		return getIdTextQueryTable().get(qId);

	}

	private synchronized Map<Long,TextQuery> getIdTextQueryTable() {

		if (idtextquerytable == null || idtextquerytable.isEmpty()){

			if (idtextquerytable == null)
				idtextquerytable = new ConcurrentHashMap<Long,TextQuery>();

			try {

				//				if (PStmtgetIdTextQueryTable == null){
				//					PStmtgetIdTextQueryTable = getConnection().prepareStatement(getTextQueryTableString);
				//				}else
				//					PStmtgetIdTextQueryTable.clearParameters();

				PreparedStatement PStmtgetIdTextQueryTable = getConnection().prepareStatement(getTextQueryTableString);

				PStmtgetIdTextQueryTable.setLong(1, -1);

				ResultSet RSgetIdTextQueryTable = PStmtgetIdTextQueryTable.executeQuery();

				while (RSgetIdTextQueryTable.next()) {

					long qId = RSgetIdTextQueryTable.getLong(1);

					String query = RSgetIdTextQueryTable.getString(2);

					TextQuery q = new TextQuery(query);

					idtextquerytable.put(qId,q);

				}

				PStmtgetIdTextQueryTable.close();
				RSgetIdTextQueryTable.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		return idtextquerytable;

	}

	private List<Query> getCachedQueries(Combination combination) {

		return getQueriesTable().get(combination.getId());

	}

	private synchronized Hashtable<Integer,List<Query>> getQueriesTable() {

		if (queriesTable == null){
			queriesTable  = new Hashtable<Integer, List<Query>>();
		}

		return queriesTable;
	}

	@Override
	public void cleanForExperiments() {

		String todo = "TRUNCATE TABLE ProcessedDocuments";

		performTransaction(todo);

		todo = "TRUNCATE TABLE ExecutionCombination";

		performTransaction(todo);

		todo = "TRUNCATE TABLE Execution"; 

		performTransaction(todo);

		todo = "TRUNCATE TABLE Evaluation";

		performTransaction(todo);

	}

	@Override
	public List<Hashtable<String, Double>> getAnalyzableData(int execution,
			List<String> attributes, List<String> orderBy) {

		List<Hashtable<String, Double>> ret = new ArrayList<Hashtable<String,Double>>();

		try {

			Statement StmtgetAnalyzableData = getConnection().createStatement();

			String head = headerOrderBy(orderBy);

			String query = "SELECT " + header(attributes) + " FROM ProcessedDocuments " +
					"WHERE idExecution = " + execution;

			if (!head.isEmpty())
				query += " Order by " + head;

			//			System.out.println(query);

			ResultSet RSgetAnalyzableData = StmtgetAnalyzableData.executeQuery(query);

			while (RSgetAnalyzableData.next()) {

				Hashtable<String, Double> aux = new Hashtable<String, Double>(attributes.size());

				int i = 1;

				for (String attribute : attributes) {

					aux.put(attribute, RSgetAnalyzableData.getDouble(i));

					i++;

				}

				ret.add(aux);

			}

			RSgetAnalyzableData.close();

			StmtgetAnalyzableData.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;
	}

	private String headerOrderBy(List<String> attributes) {

		if (attributes.isEmpty())
			return "";

		String ret = "";

		for (String string : attributes) {

			ret += ", " + string + " + 0";

		}

		return ret.substring(2);
	}

	private String headerGroupBy(List<String> attributes) {

		if (attributes.isEmpty())
			return "";

		String ret = "";

		for (String string : attributes) {

			ret += ", " + string;

		}

		return ret.substring(2);
	}

	private String header(List<String> attributes) {

		String ret = "";

		for (String string : attributes) {

			ret += ", " + string;

		}

		return ret.substring(2);
	}

	@Override
	public List<Integer> getExecutionsId(int executionAlternative) {

		ArrayList<Integer> ret = new ArrayList<Integer>();


		try {

			Statement StmtgetExecutionsId = getConnection().createStatement();

			ResultSet RSgetExecutionsId = StmtgetExecutionsId.executeQuery
					("SELECT E.idExecution" +
							" FROM Execution E WHERE idExecutionAlternative = " + executionAlternative);

			while (RSgetExecutionsId.next()) {

				int id = RSgetExecutionsId.getInt(1);

				ret.add(id);

			}

			RSgetExecutionsId.close();
			StmtgetExecutionsId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<Integer> getCombinations(String algorithmName) {
		int algorithmId = getAlgorithmId(algorithmName);
		return getCombinationIds(algorithmId);
	}

	private List<Integer> getCombinationIds(int algorithmId) {

		ArrayList<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetCombinationsId = getConnection().createStatement();

			ResultSet RSgetCombinationsId = StmtgetCombinationsId.executeQuery
					("SELECT C.idCombination" +
							" FROM Combination C WHERE idAlgorithm = " + algorithmId);

			while (RSgetCombinationsId.next()) {

				int id = RSgetCombinationsId.getInt(1);

				ret.add(id);

			}

			RSgetCombinationsId.close();
			StmtgetCombinationsId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;
	}

	@Override
	public Collection<? extends Integer> getExecutions(Integer combinationId) {

		ArrayList<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetExecs = getConnection().createStatement();

			ResultSet RSgetExecs = StmtgetExecs.executeQuery
					("SELECT C.idExecution" +
							" FROM ExecutionCombination C WHERE idCombination = " + combinationId);

			while (RSgetExecs.next()) {

				int id = RSgetExecs.getInt(1);

				ret.add(id);

			}

			RSgetExecs.close();
			StmtgetExecs.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public double getTotal(String attribute, Integer execution) {

		double ret = 0.0;

		try {

			Statement StmtgetTotal = getConnection().createStatement();

			String query = null;

			if (attribute.startsWith("D."))
				query = ("SELECT " + attribute + " FROM `Database` D join WorkloadDatabaseStatistics W on (D.idDatabase = W.idDatabase) join ExecutionCombination E on (W.idDatabase=E.idDatabase) join Combination C " +
						"on (E.idCombination = C.idCombination and W.idVersion = C.idVersion and W.idWorkload = C.idWorkload) WHERE E.idExecution = " + execution);

			else{

				query = ("SELECT " + attribute + " FROM WorkloadDatabaseStatistics W join ExecutionCombination E on (W.idDatabase=E.idDatabase) join Combination C " +
						"on (E.idCombination = C.idCombination and W.idVersion = C.idVersion and W.idWorkload = C.idWorkload) WHERE E.idExecution = " + execution);

			}

			ResultSet RSgetTotal = StmtgetTotal.executeQuery(query);


			while (RSgetTotal.next()) {

				ret = RSgetTotal.getDouble(1);

			}

			RSgetTotal.close();
			StmtgetTotal.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public boolean existsEvaluation(Evaluation ev, int executionAlternativeId) {

		boolean ret = false;


		try {

			Statement StmtexistsEvaluation = getConnection().createStatement();

			ResultSet RSexistsEvaluation = StmtexistsEvaluation.executeQuery
					("SELECT E.*" +
							" FROM Evaluation E, ExecutionCombination C, Execution X WHERE E.idCombination = " + ev.getCombination().getId() + " AND E.idDatabase = " + ev.getEvaluableDatabase().getId() + 
							" AND E.`limit`= " + ev.getDatabaseLimit() + " AND X.idExecutionAlternative = " + executionAlternativeId + " AND E.idDatabase = C.idDatabase AND E.idCombination = C.idCombination AND " +
							"E.`limit` = C.`limit`");

			while (RSexistsEvaluation.next()) {

				ret = true;

			}

			RSexistsEvaluation.close();
			StmtexistsEvaluation.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	public void saveSimilarity(Database basedOnDatabase,
			Database comparedDatabase,
			SimilarityFunctionEnum similarityFunction, double distance, Version version,
			int sample_number, WorkloadModel wlmodel){

		insertSimilarity(basedOnDatabase.getId(),comparedDatabase.getId(),getSimilarityFunctionId(similarityFunction),distance,version.getId(),sample_number,wlmodel.getId());



	}

	private void insertSimilarity(int idDatabase1, int idDatabase2, int similarityMeasure,
			double similarityValue, int idVersion, int sample_number, int idWorkload) {

		String todoinsertSimilarity = ("INSERT INTO SimilarityDatabase (idDatabase_1,idDatabase_2,SimilarityMeasure,similarityValue,idVersion,sample_number,idWorkload) VALUES " +

				"(" + idDatabase1 + "," + idDatabase2 + "," + similarityMeasure + "," + similarityValue + "," + idVersion + "," + sample_number + "," + idWorkload + ")");

		performTransaction(todoinsertSimilarity);


	}

	private int getSimilarityFunctionId(SimilarityFunctionEnum similarityFunction) {

		int res = 0;

		try {
			Statement StmtsimilarityFunctionIdId = getConnection().createStatement();

			ResultSet RSsimilarityFunctionId = StmtsimilarityFunctionIdId.executeQuery
					("SELECT S.idSimilarityMeasure " +
							" FROM SimilarityMeasure S WHERE S.Description='" + format(similarityFunction.name())+"'");

			while (RSsimilarityFunctionId.next()) {
				res = RSsimilarityFunctionId.getInt(1);
			}
			RSsimilarityFunctionId.close();
			StmtsimilarityFunctionIdId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return res;
	}


	@Override
	public synchronized void saveStoredDownloadedDocument(Document document, boolean success) {
		try {

			//			if (PStmtinsertRetrievedURL == null){
			//				PStmtinsertRetrievedURL = getConnection().prepareStatement(insertRetrievedURLString);
			//			}else
			//				PStmtinsertRetrievedURL.clearParameters();

			PreparedStatement PStmtinsertRetrievedURL = getConnection().prepareStatement(insertRetrievedURLString);

			PStmtinsertRetrievedURL.setInt(1, document.getDatabase().getId());
			PStmtinsertRetrievedURL.setInt(2, document.getExperimentId());
			PStmtinsertRetrievedURL.setLong(3, document.getId());
			PStmtinsertRetrievedURL.setString(4, document.getFilePath(this).getAbsolutePath());
			PStmtinsertRetrievedURL.setString(5, document.getURL(this).toString());
			PStmtinsertRetrievedURL.setLong(6, document.getDownloadTime());
			PStmtinsertRetrievedURL.setBoolean(7, document.isSuccessful());

			PStmtinsertRetrievedURL.execute();
			PStmtinsertRetrievedURL.close();

		} catch (SQLException e) {
			//XXX It's OK.
		}


	}

	public void insertDatabase(int idDatabase, String Name, int size, String Class,
			String ModelType, int Crossable, int Searchable, String Index, int isGlobal, int isCluster, int isLocal){

		String todoinsertDatabase = ("INSERT INTO `Database` (`idDatabase`, Name, size, Class, ModelType, Crossable," +
				" Searchable, `Index`, isGlobal, isCluster, isLocal) VALUES " +

				"(" + idDatabase + ",'" + format(Name) + "'," + size + ",'" + format(Class) + "','" + format(ModelType) + "'," + Crossable + "," + Searchable + 
				",'" + format(Index) + "'," + isGlobal + "," + isCluster+ "," + isLocal + ")");

		performTransaction(todoinsertDatabase);


	}

	public void updateIndexOfWebsite(int databaseId, String Index) {

		String todoupdateDatabase = ("UPDATE `Database` SET `Index`= '" + format(Index) + "' WHERE `idDatabase`= " + databaseId);

		performTransaction(todoupdateDatabase);

	}
	@Override
	public void saveExtractionTime(Document file, String extractionSystem,
			long time) {

		try {

			//			if (PStmtinsertExtractionTime == null){
			//				PStmtinsertExtractionTime = getConnection().prepareStatement(insertExtractionTimeString);
			//			}else
			//				PStmtinsertExtractionTime.clearParameters();

			PreparedStatement PStmtinsertExtractionTime = getConnection().prepareStatement(insertExtractionTimeString);

			PStmtinsertExtractionTime.setLong(1, file.getDatabase().getId());
			PStmtinsertExtractionTime.setLong(2, file.getId());
			PStmtinsertExtractionTime.setString(3, format(extractionSystem));
			PStmtinsertExtractionTime.setLong(4, time);

			System.out.println(PStmtinsertExtractionTime.toString());

			PStmtinsertExtractionTime.execute();
			PStmtinsertExtractionTime.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}


		//		todoinsertExtractionTime = ("INSERT INTO `ExtractionTime` (`databaseId`,`filePath`, extractionSystem, `time`) VALUES " +
		//				
		//				"(" + databaseId + ",'" +  format(file) + "','" + format(extractionSystem) + "'," + time + ")");
		//		
		//		performTransaction(todoinsertExtractionTime);


	}

	public void saveGlobalSimilarity(Database d1, Database d2,
			SimilarityFunctionEnum similarityFunction, double distance) {

		insertSimilarity(d1.getId(),d2.getId(),getSimilarityFunctionId(similarityFunction),distance,-1,-1,-1);

	}

	public void insertDocument(int idDatabase, long idDocument, String path) {

		try {

			//			if (PStmtinsertDocument == null){
			//				PStmtinsertDocument = getConnection().prepareStatement(insertDocumentString);
			//			}else
			//				PStmtinsertDocument.clearParameters();

			PreparedStatement PStmtinsertDocument = getConnection().prepareStatement(insertDocumentString);

			PStmtinsertDocument.setInt(1, idDatabase);
			PStmtinsertDocument.setLong(2, idDocument);
			PStmtinsertDocument.setString(3, format(path));

			PStmtinsertDocument.execute();
			PStmtinsertDocument.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		//		todoinsertDocument = ("INSERT INTO `Document` (`idDatabase`,`idDocument`, `path`) VALUES " +
		//				
		//				"(" + idDatabase + "," +  idDocument + "," + format(path) + ")");
		//		
		//		performTransaction(todoinsertDocument);

	}

	@Override
	public Database getDatabaseByName(String name) {
		return getDatabase(this.getDatabaseId(name));
	}

	public void insertClusteredDatabase(int clusterId, int databaseId,
			ClusterFunctionEnum clusterFunction, int version, int sample, int relation, int workload, int relationConf, int informationExtractionSystem) {

		String todoinsertCluster = ("INSERT INTO `DatabaseCluster` (`idCluster`,`idDatabase`, idClusterFunction, `idVersion`,`sample_number`,`idRelationshipType`,`idWorkload`,`idRelationConfiguration`,`idInformationExtractionSytem`) VALUES " +

				"(" + clusterId + "," +  databaseId + "," + getClusterFunctionId(clusterFunction) + "," + version + "," + sample + "," + relation + "," + workload + "," + relationConf + "," + informationExtractionSystem + ")");

		performTransaction(todoinsertCluster);


	}

	private int getClusterFunctionId(ClusterFunctionEnum clusterFunction) {

		synchronized (cachedClusterFunction) {

			Integer id = cachedClusterFunction.get(clusterFunction);

			if (id == null){

				try {

					Statement StmtGetClusterFunctionId = getConnection().createStatement();

					ResultSet RSGetClusterFunctionId = StmtGetClusterFunctionId.executeQuery
							("SELECT idClusterFunction" +
									" FROM ClusterFunction WHERE description ='" + format(clusterFunction.name()) + "'");

					while (RSGetClusterFunctionId.next()) {

						id = RSGetClusterFunctionId.getInt(1);

					}

					RSGetClusterFunctionId.close();

					StmtGetClusterFunctionId.close();

					cachedClusterFunction.put(clusterFunction,id);


				} catch (SQLException e) {

					e.printStackTrace();

				}

			}

			return id;

		}



	}

	@Override
	public Hashtable<String, Long> getDocumentsTable(Database database) {

		Hashtable<String, Long> res = new Hashtable<String, Long>();

		try {

			Statement StmtRetrievedDocuments = getConnection().createStatement();

			ResultSet RSRetrievedDocuments = StmtRetrievedDocuments.executeQuery
					("SELECT idDocument, path" +
							" FROM Document WHERE idDatabase =" + database.getId());

			while (RSRetrievedDocuments.next()) {

				res.put(RSRetrievedDocuments.getString(2), RSRetrievedDocuments.getLong(1));

			}

			RSRetrievedDocuments.close();

			StmtRetrievedDocuments.close();


		} catch (SQLException e) {

			e.printStackTrace();

		}

		return res;

	}

	@Override
	public Hashtable<String, Document> getDownloadedDocumentsTable(int databaseId,
			int experimentId) {


		Hashtable<String, Document> res = new Hashtable<String, Document>();

		Database db = getDatabase(databaseId);

		try {

			Statement StmtRetrievedDocTable = getConnection().createStatement();

			ResultSet RSRetrievedDocTable = StmtRetrievedDocTable.executeQuery
					("SELECT idDocument,filePath, URL, downloadTime, success FROM WebDocument where  idDatabase = " + databaseId + " and idExperiment = " + experimentId);

			while (RSRetrievedDocTable.next()) {

				String url = RSRetrievedDocTable.getString(3);

				try {
					res.put(url, new Document(db,RSRetrievedDocTable.getLong(1),new File(RSRetrievedDocTable.getString(2)),new URL(url),experimentId,RSRetrievedDocTable.getLong(4),RSRetrievedDocTable.getBoolean(5)));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

			RSRetrievedDocTable.close();
			StmtRetrievedDocTable.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return res;

	}

	@Override
	public Hashtable<Long, Document> getDownloadedDocumentsIdTable(int databaseId,
			int experimentId, List<Long> documents) {


		Hashtable<Long, Document> res = new Hashtable<Long, Document>();

		Database db = getDatabase(databaseId);

		String IN = "";

		if (documents != null && !documents.isEmpty()){
			IN = " and idDocument IN " + generateIn(documents);
		}	

		try {

			Statement StmtRetrievedDocTable = getConnection().createStatement();

			ResultSet RSRetrievedDocTable = StmtRetrievedDocTable.executeQuery
					("SELECT idDocument,filePath, URL, downloadTime, success FROM WebDocument where  idDatabase = " + databaseId + " and idExperiment = " + experimentId + IN);

			while (RSRetrievedDocTable.next()) {

				Long id = RSRetrievedDocTable.getLong(1);
				try {
					res.put(id, new Document(db,id,new File(RSRetrievedDocTable.getString(2)),new URL(RSRetrievedDocTable.getString(3)),experimentId,RSRetrievedDocTable.getLong(4),RSRetrievedDocTable.getBoolean(5)));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

			RSRetrievedDocTable.close();
			StmtRetrievedDocTable.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return res;

	}


	@Override
	public List<Integer> getExecutions(List<Integer> executionAlternatives,
			String algorithmName) {

		int algorithmId = getAlgorithmId(algorithmName);

		List<Integer> res = new ArrayList<Integer>();

		try {

			Statement StmtgetFilteredExecutions = getConnection().createStatement();

			ResultSet RSgetFilteredExecutions;

			if (executionAlternatives.size() == 1){
				RSgetFilteredExecutions = StmtgetFilteredExecutions.executeQuery
						("select EC.idExecution from Combination C join ExecutionCombination EC on (C.idCombination = EC.idCombination) join Execution E on (E.idExecution = EC.idExecution) where idExecutionAlternative = " + executionAlternatives.get(0) + " and idAlgorithm = " + algorithmId);
			}else{
				RSgetFilteredExecutions = StmtgetFilteredExecutions.executeQuery
						("select EC.idExecution from Combination C join ExecutionCombination EC on (C.idCombination = EC.idCombination) join Execution E on (E.idExecution = EC.idExecution) where idExecutionAlternative IN " + generateIn(executionAlternatives) + " and idAlgorithm = " + algorithmId);
			}
			while (RSgetFilteredExecutions.next()) {
				res.add(RSgetFilteredExecutions.getInt(1));
			}

			RSgetFilteredExecutions.close();
			StmtgetFilteredExecutions.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return res;
	}

	private <T> String generateIn(List<T> elements) {

		String ret = "(" + elements.get(0);

		for (int i = 1; i < elements.size(); i++) {

			ret += "," + elements.get(i);

		}

		return ret + ")";
	}

	@Override
	public double getHTMLValidatorThreshold(Database website,
			String htmlValidator) {

		Double thres = getCachedThresHold().get(website.getId()); //XXX change if the htmlValidator changes

		if (thres == null){

			try {

				Statement StmtRetrievedThreshold = getConnection().createStatement();

				ResultSet RSRetrievedThreshold = StmtRetrievedThreshold.executeQuery
						("SELECT threshold FROM HTMLValidatorThreshold WHERE idDatabase=" + website.getId() + " AND HTMLValidatorThresholdId = '" + htmlValidator +"'");

				while (RSRetrievedThreshold.next()) {
					thres = RSRetrievedThreshold.getDouble(1);
				}

				RSRetrievedThreshold.close();
				StmtRetrievedThreshold.close();

			} catch (SQLException e) {

				e.printStackTrace();

			}

			getCachedThresHold().put(website.getId(),thres);

		}

		return thres;
	}

	private synchronized Map<Integer, Double> getCachedThresHold() {

		if (cachedThreshold == null){
			cachedThreshold = new HashMap<Integer, Double>();
		}
		return cachedThreshold;
	}

	@Override
	public void saveHTMLValidatorThreshold(Database db, String name,
			double threshold) {

		String todoinsertThreshold = ("INSERT INTO `AutomaticQueryGeneration`.`HTMLValidatorThreshold` (`idDatabase`, `HTMLValidatorThresholdId`,`threshold`) VALUES " +

				"(" + db.getId() + ",'" +  format(name) + "'," + threshold + ")");

		performTransaction(todoinsertThreshold);

	}

	@Override
	public int getNextSampleNumber(int id, Database database, Version version,
			WorkloadModel workload) {

		int max = 1;

		try {

			//			if (PStmtgetNextSampleNumber == null){
			//				PStmtgetNextSampleNumber = getConnection().prepareStatement(getNextSampleNumberString);
			//			}else
			//				PStmtgetNextSampleNumber.clearParameters();

			PreparedStatement PStmtgetNextSampleNumber = getConnection().prepareStatement(getNextSampleNumberString);

			PStmtgetNextSampleNumber.setInt(1, id);
			PStmtgetNextSampleNumber.setInt(2, database.getId());
			PStmtgetNextSampleNumber.setInt(3, version.getId());
			PStmtgetNextSampleNumber.setInt(4, workload.getId());

			ResultSet RSgetNextSampleNumber = PStmtgetNextSampleNumber.executeQuery();

			while (RSgetNextSampleNumber.next()) {

				max = RSgetNextSampleNumber.getInt(1);

			}

			RSgetNextSampleNumber.close();
			PStmtgetNextSampleNumber.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return max + 1;

	}

	@Override
	public synchronized int writeSample(Sample sample) {

		int key = -1;

		if (!existsSample(sample)){

			try {


				//			if (PStmtinsertSample == null){
				//				PStmtinsertSample = getConnection().prepareStatement(insertSampleString, Statement.RETURN_GENERATED_KEYS);
				//			}else
				//				PStmtinsertSample.clearParameters();

				PreparedStatement PStmtinsertSample = getConnection().prepareStatement(insertSampleString, Statement.RETURN_GENERATED_KEYS);

				PStmtinsertSample.setInt(1, sample.getDatabase().getId());
				PStmtinsertSample.setInt(2, sample.getVersion().getId());
				PStmtinsertSample.setInt(3, sample.getWorkload().getId());
				PStmtinsertSample.setInt(4, sample.getVersionSeedPos());
				PStmtinsertSample.setInt(5, sample.getVersionSeedNeg());
				PStmtinsertSample.setInt(6, sample.getSampleConfiguration().getId());

				PStmtinsertSample.execute();

				ResultSet rs = PStmtinsertSample.getGeneratedKeys();
				if (rs != null && rs.next()) {
					key = rs.getInt(1);
				}

				PStmtinsertSample.close();
				rs.close();

				return key;

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return key;

		}

		return key; //duplicated

	}

	//	private synchronized int getNewSampleId() {
	//
	//		int res = 0;
	//
	//		try {
	//
	//			StmtnewSampleId = getConnection().createStatement();
	//
	//			RSnewSampleId = StmtnewSampleId.executeQuery
	//					("SELECT LAST_INSERT_ID()");
	//
	//			while (RSnewSampleId.next()) {
	//
	//				res = RSnewSampleId.getInt(1);
	//
	//			}
	//			RSnewSampleId.close();
	//			StmtnewSampleId.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//		return res;
	//
	//	}

	private boolean existsSample(Sample sample) {

		int val = 0;

		try {

			PreparedStatement PStmtexistsSample = getConnection().prepareStatement(existsSample);

			PStmtexistsSample.setInt(1, sample.getDatabase().getId());
			PStmtexistsSample.setInt(2, sample.getVersion().getId());
			PStmtexistsSample.setInt(3, sample.getWorkload().getId());
			PStmtexistsSample.setInt(4, sample.getVersionSeedPos());
			PStmtexistsSample.setInt(5, sample.getVersionSeedNeg());
			PStmtexistsSample.setInt(6, sample.getSampleConfiguration().getId());


			ResultSet RSexistsSample = PStmtexistsSample.executeQuery();

			while (RSexistsSample.next()) {

				val = RSexistsSample.getInt(1);
			}

			RSexistsSample.close();
			PStmtexistsSample.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

		return val == 1;

	}

	@Override
	public void WriteSentQuery(int queriedDb, int generatedQueries,Sample sample, TextQuery query){

		long id = getTextQuery(query);

		WriteSentQuery(queriedDb,generatedQueries,sample,id);

	}

	@Override
	public void WriteSentQuery(int queriedDb, int generatedQueries,Sample sample, long query){

		try {

			PreparedStatement PStmtinsertSampleGenerationQuery;

			synchronized (isgqTable) {
				PStmtinsertSampleGenerationQuery = isgqTable.get(sample.getId());

				if (PStmtinsertSampleGenerationQuery == null){
					PStmtinsertSampleGenerationQuery = getConnection().prepareStatement(insertSampleGenerationQuery);
					isgqTable.put(sample.getId(),PStmtinsertSampleGenerationQuery);
				}

			}

			PStmtinsertSampleGenerationQuery.setInt(1, sample.getId());
			PStmtinsertSampleGenerationQuery.setInt(2, queriedDb);
			PStmtinsertSampleGenerationQuery.setInt(3, generatedQueries);
			PStmtinsertSampleGenerationQuery.setLong(4, query);

			PStmtinsertSampleGenerationQuery.addBatch();

			//			PStmtinsertSampleGenerationQuery.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void addProcessedDocument(int idSample, int query_generated_position, int query_submitted_position, int queryRound, int documentPosition,
			int document_position_in_query, int document_position_in_sample, int useful_tuples, Document document){

		try {

			PreparedStatement PStmtinsertSampleGeneration;

			synchronized (tpdTable) {

				PStmtinsertSampleGeneration = tpdTable.get(idSample);

				if (PStmtinsertSampleGeneration == null){
					PStmtinsertSampleGeneration = getConnection().prepareStatement(insertSampleGeneration);
					tpdTable.put(idSample,PStmtinsertSampleGeneration);
				}

			}


			PStmtinsertSampleGeneration.setInt(1, idSample);
			PStmtinsertSampleGeneration.setInt(2, query_generated_position);
			PStmtinsertSampleGeneration.setInt(3, query_submitted_position);
			PStmtinsertSampleGeneration.setInt(4, queryRound);
			PStmtinsertSampleGeneration.setInt(5, documentPosition);
			PStmtinsertSampleGeneration.setInt(6, document_position_in_query);
			PStmtinsertSampleGeneration.setInt(7, document_position_in_sample);
			PStmtinsertSampleGeneration.setInt(8, useful_tuples);
			PStmtinsertSampleGeneration.setInt(9, document.getDatabase().getId());
			PStmtinsertSampleGeneration.setLong(10, document.getId());

			PStmtinsertSampleGeneration.addBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<SampleConfiguration> getActiveSampleConfigurations(int activeValue){

		List<SampleConfiguration> ret = getActiveSampleConfigutionsId(activeValue);

		for (SampleConfiguration sc : ret){

			Integer parametersId = sc.getParameterId();

			Parametrizable parameters = getSampleConfigurationParameters(parametersId);

			WorkloadModel wm = getWorkloadModel(sc.getWorkloadModelId());

			Version version = getVersion(sc.getVersionId(), wm);

			sc.setWorkloadModel(wm);

			sc.setVersion(version);

			RelationExtractionSystem res = getRelationExtractionSystem(sc.getExtractionSystemId(),sc.getWorkloadModel(),sc.getRelationConfiguration());

			sc.setInformationExtractionSystem(res);

			//			sc.setNo_Filtering_Fields(InferredTypeFactory.getNoFilteringFields(parameters.loadParameter(ExecutionAlternativeEnum.NO_FILTERING_FIELDS).getString(),this));

			ContentExtractor contentExtractor = ContentExtractorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CONTENT_EXTRACTION).getString());

			sc.setContentExtractor(contentExtractor);

			ContentLoader contentLoader = ContentLoaderFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.CONTENT_LOADER).getString());

			sc.setContentLoader(contentLoader);

			InteractionPersister interactionPersister = InteractionPersisterFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.INTERACTION_PERSISTER).getString(),
					parameters.loadParameter(ExecutionAlternativeEnum.INTERACTION_PERSISTER_PARAMETERS),this);

			sc.setInteractionPersister(interactionPersister);

			QueryPool qp = new DummyQueryPool(null,true);

			if (sc.getUsefulNumber() > 0)
				qp = QueryPoolFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL).getString(),parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_PARAMETERS),sc,this,interactionPersister,true);

			QueryPool qn = new DummyQueryPool(null,false);

			if (sc.getUselessNumber() > 0)		
				qn = QueryPoolFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL).getString(),parameters.loadParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_PARAMETERS),sc,this,interactionPersister,false);

			Parametrizable parametersQPE = getQueryPoolExecutor(sc.getQueryPoolExecutor());

			QueryPoolExecutor qpep = QueryPoolExecutorFactory.generateInstance(parametersQPE.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_EXECUTOR).getString(),parametersQPE.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_EXECUTOR_PARAMETERS),qp,sc.getResultsPerQuery());

			QueryPoolExecutor qpen = QueryPoolExecutorFactory.generateInstance(parametersQPE.loadParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_EXECUTOR).getString(),parametersQPE.loadParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_EXECUTOR_PARAMETERS),qn,sc.getResultsPerQuery());

			Integer sgParameter = getParameterSGId(sc.getSampleGenerator()); //Gets actually the executor

			Parametrizable parametersSG = getSampleExecutor(sgParameter);

			//			String sample_generator = retrieveSimpleSampleGenerator(sgParameter);

			SampleGenerator impl = SampleGeneratorFactory.generateInstance(sc.useAll(),
					qpep,qpen, 
					Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_FIRST).getString()),interactionPersister,wm,contentExtractor,res); 

			//It's the executor and it's built with the samplegenerator.

			sc.setSampleExecutor(SampleExecutorFactory.generateInstance(res,parametersSG.loadParameter(ExecutionAlternativeEnum.SAMPLE_EXECUTOR).getString(),impl, 
					parametersSG.loadParameter(ExecutionAlternativeEnum.SAMPLE_EXECUTOR_PARAMETERS),this,getSimpleSampleConfiguration(sc)));

			//			sc.setUsefulWordExtractor(new WordExtractor(contentExtractor, contentLoader));
			//
			//			sc.setGeneralWordExtractor(new WordExtractor(contentExtractor, contentLoader));

		}

		return ret;

	}


	//	private String retrieveSimpleSampleGenerator(Integer param) {
	//		
	//		if (sampleConfig == -1){ //does not depend on any
	//			
	//			Hashtable<String,String> paramTab = readParameters(param);
	//			
	//			Parametrizable tab = loadSampleGenerator(paramTab);
	//			
	//			return tab.loadParameter(ExecutionAlternativeEnum.SAMPLE_GENERATOR).getString();
	//			
	//		} else {
	//			
	//			Pair<Integer,Integer> pair = getParameterSGId(getSampleGenerator(sampleConfig));
	//			//gets the params and the sampleConfiguration
	//			
	//			return retrieveSimpleSampleGenerator(pair.getFirst(), pair.getSecond());
	//		}
	//		
	//	}

	private int getSimpleSampleConfiguration(SampleConfiguration sc) {


		int ret = -1;

		try {

			Statement StmtgetSampleGenerator = getConnection().createStatement();

			int uaint = sc.useAll()? 1:0;
			int countAllint = sc.countsAll()? 1:0;

			ResultSet RSgetSampleGenerator = StmtgetSampleGenerator.executeQuery
					("SELECT IdSampleConfiguration from SampleConfiguration where `SampleConfiguration`.`idParameter` = " +sc.getParameterId() + " and " +
							"`SampleConfiguration`.`idVersion` = " + sc.getVersionId() + " and `SampleConfiguration`.`idWorkload` = " + sc.getWorkloadModelId() + " and " +
							"`SampleConfiguration`.`idRelationConfiguration` = " + sc.getRelationConfiguration() + " and `SampleConfiguration`.`idExtractionSystem` = " + sc.getExtractionSystemId() + " and " +
							"`SampleConfiguration`.`idQueryPoolExecutor` = " + sc.getQueryPoolExecutor() + " and `SampleConfiguration`.`idSampleGenerator` = 1 and `SampleConfiguration`.`use_all` = " + uaint + " and " +
							"`SampleConfiguration`.`resultsPerQuery` = " + sc.getResultsPerQuery() + " and `SampleConfiguration`.`usefulNumber` = " + sc.getUsefulNumber() + " and " + "" +
							"`SampleConfiguration`.`uselessNumber` = " + sc.getUselessNumber() + " and `SampleConfiguration`.`maxQueries` = " + sc.getAllowedNumberOfQueries() + " and " +
							"`SampleConfiguration`.`maxDocuments` = " + sc.getAllowedNumberOfDocuments() + " and `SampleConfiguration`.`CountsAll` = " + countAllint + " and " +
							"`SampleConfiguration`.`baseCollection` = '" + sc.getBaseCollection() + "' and `SampleConfiguration`.`docsInTraining` = " + sc.getDocsInTraining() + ";");


			while (RSgetSampleGenerator.next()) {

				ret = RSgetSampleGenerator.getInt(1);

			}

			RSgetSampleGenerator.close();
			StmtgetSampleGenerator.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;


	}

	private int getSampleGenerator(Integer sampleConfig) {

		int ret = -1;

		try {

			Statement StmtgetSampleGenerator = getConnection().createStatement();

			ResultSet RSgetSampleGenerator = StmtgetSampleGenerator.executeQuery
					("SELECT idSampleGenerator FROM AutomaticQueryGeneration.SampleConfiguration where idSampleConfiguration = " + sampleConfig); 

			while (RSgetSampleGenerator.next()) {

				ret = RSgetSampleGenerator.getInt(1);

			}

			RSgetSampleGenerator.close();
			StmtgetSampleGenerator.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private Parametrizable loadSampleGenerator(
			Hashtable<String, String> parameters) {
		//SAMPLE_GENERATOR

		TableParameters ret = new TableParameters();

		Parametrizable sampleGeneratorText = new StringParameters(ExecutionAlternativeEnum.SAMPLE_GENERATOR,parameters.get(ExecutionAlternativeEnum.SAMPLE_GENERATOR.toString()));

		ret.addParameter(ExecutionAlternativeEnum.SAMPLE_GENERATOR, sampleGeneratorText);

		return ret;
	}

	private Parametrizable getSampleExecutor(int parameterId) {

		Hashtable<String, String> ret = readParameters(parameterId);

		return generateSampleExecutor(ret);

	}

	private Parametrizable generateSampleExecutor(Hashtable<String, String> parameters) {

		//SAMPLE EXECUTOR

		TableParameters ret = new TableParameters();

		Parametrizable sampleExecutorText = new StringParameters(ExecutionAlternativeEnum.SAMPLE_EXECUTOR,parameters.get(ExecutionAlternativeEnum.SAMPLE_EXECUTOR.toString()));

		ret.addParameter(ExecutionAlternativeEnum.SAMPLE_EXECUTOR,sampleExecutorText);

		Parametrizable sampleExecutor = loadSampleExecutor(parameters);

		ret.addParameter(ExecutionAlternativeEnum.SAMPLE_EXECUTOR_PARAMETERS, sampleExecutor);

		return ret;

	}

	private Integer getParameterSGId(int sampleGenerator) {

		Integer ret = null;

		try {

			//			if (PStmtParameterSGId == null){
			//				PStmtParameterSGId = getConnection().prepareStatement(ParameterSGIdString);
			//			}else
			//				PStmtParameterSGId.clearParameters();

			PreparedStatement PStmtParameterSGId = getConnection().prepareStatement(ParameterSGIdString);

			PStmtParameterSGId.setInt(1, sampleGenerator);

			ResultSet RSgetParameterSGId = PStmtParameterSGId.executeQuery();

			while (RSgetParameterSGId.next()) {

				ret = RSgetParameterSGId.getInt(1) ;

			}

			RSgetParameterSGId.close();
			PStmtParameterSGId.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private Parametrizable getQueryPoolExecutor(int queryPoolExecutor) {

		int parameterId = getParameterId(queryPoolExecutor);

		Hashtable<String, String> ret = readParameters(parameterId);

		return generateQueryPoolParametrizable(ret);

	}

	private Parametrizable generateQueryPoolParametrizable(
			Hashtable<String, String> parameters) {

		TableParameters ret = new TableParameters();

		//POSITIVE_QUERY_POOL_EXECUTOR

		Parametrizable positiveQueryExecutorText = new StringParameters(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_EXECUTOR,parameters.get(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_EXECUTOR.toString()));

		ret.addParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_EXECUTOR, positiveQueryExecutorText);

		Parametrizable positiveQueryExecutorPool = loadPositiveQueryExecutorParameters(parameters);

		ret.addParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_EXECUTOR_PARAMETERS, positiveQueryExecutorPool);	

		//NEGATIVE_QUERY_POOL_EXECUTOR

		Parametrizable negativeQueryExecutorText = new StringParameters(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_EXECUTOR,parameters.get(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_EXECUTOR.toString()));

		ret.addParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_EXECUTOR, negativeQueryExecutorText);

		Parametrizable negativeQueryExecutorPool = loadNegativeQueryExecutorParameters(parameters);

		ret.addParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_EXECUTOR_PARAMETERS, negativeQueryExecutorPool);	

		return ret;

	}

	private int getParameterId(int queryPoolExecutor) {

		int ret = 0;

		try {

			//			if (PStmtParameterId == null){
			//				PStmtParameterId = getConnection().prepareStatement(ParameterIdString);
			//			}else
			//				PStmtParameterId.clearParameters();

			PreparedStatement PStmtParameterId = getConnection().prepareStatement(ParameterIdString);

			PStmtParameterId.setInt(1, queryPoolExecutor);

			ResultSet RSgetParameterId = PStmtParameterId.executeQuery();

			while (RSgetParameterId.next()) {

				ret = RSgetParameterId.getInt(1);

			}

			PStmtParameterId.close();
			RSgetParameterId.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private Parametrizable getSampleConfigurationParameters(Integer parametersId) {
		Hashtable<String, String> ret = readParameters(parametersId);

		return generateSampleParametrizable(ret);

	}

	private Parametrizable generateSampleParametrizable(
			Hashtable<String, String> parameters) {

		TableParameters ret = new TableParameters();

		Parametrizable positiveFirst = new StringParameters(ExecutionAlternativeEnum.POSITIVE_FIRST,parameters.get(ExecutionAlternativeEnum.POSITIVE_FIRST.toString()));

		ret.addParameter(ExecutionAlternativeEnum.POSITIVE_FIRST,positiveFirst);

		Parametrizable noFilteringFields = new StringParameters(ExecutionAlternativeEnum.NO_FILTERING_FIELDS,parameters.get(ExecutionAlternativeEnum.NO_FILTERING_FIELDS.toString()));

		ret.addParameter(ExecutionAlternativeEnum.NO_FILTERING_FIELDS,noFilteringFields);

		//CONTENT_EXTRACTOR

		Parametrizable contentExtractionText = new StringParameters(ExecutionAlternativeEnum.CONTENT_EXTRACTION,parameters.get(ExecutionAlternativeEnum.CONTENT_EXTRACTION.toString()));

		ret.addParameter(ExecutionAlternativeEnum.CONTENT_EXTRACTION, contentExtractionText);

		//POSITIVE_QUERY_POOL

		Parametrizable positiveQueryText = new StringParameters(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL,parameters.get(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL.toString()));

		ret.addParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL, positiveQueryText);

		Parametrizable positiveQueryPool = loadPositiveQueryParameters(parameters);

		ret.addParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_POOL_PARAMETERS, positiveQueryPool);

		//NEGATIVE_QUERY_POOL

		Parametrizable negativeQueryText = new StringParameters(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL,parameters.get(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL.toString()));

		ret.addParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL, negativeQueryText);

		Parametrizable negativeQueryPool = loadNegativeQueryParameters(parameters);

		ret.addParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_POOL_PARAMETERS, negativeQueryPool);

		//CONTENT_LOADER

		Parametrizable contentLoaderText = new StringParameters(ExecutionAlternativeEnum.CONTENT_LOADER,parameters.get(ExecutionAlternativeEnum.CONTENT_LOADER.toString()));

		ret.addParameter(ExecutionAlternativeEnum.CONTENT_LOADER, contentLoaderText);

		//INTERACTION PERSISTER

		Parametrizable interactionPersisterText = new StringParameters(ExecutionAlternativeEnum.INTERACTION_PERSISTER,parameters.get(ExecutionAlternativeEnum.INTERACTION_PERSISTER.toString()));

		ret.addParameter(ExecutionAlternativeEnum.INTERACTION_PERSISTER,interactionPersisterText);

		Parametrizable interactionPersister = loadInteractionPersister(parameters);

		ret.addParameter(ExecutionAlternativeEnum.INTERACTION_PERSISTER_PARAMETERS, interactionPersister);

		return ret;
	}

	private Parametrizable loadNegativeQueryExecutorParameters(
			Hashtable<String, String> parameters) {
		return new TableParameters();
	}

	private Parametrizable loadPositiveQueryExecutorParameters(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable queryPoolText = new StringParameters(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER,parameters.get(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER, queryPoolText);

		Parametrizable queryPool = loadQueryPoolPerformance(parameters);

		tp.addParameter(ExecutionAlternativeEnum.QUERY_POOL_PERFORMANCE_CHECKER_PARAMETERS, queryPool);


		Parametrizable queryText = new StringParameters(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER,parameters.get(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER, queryText);

		Parametrizable query = loadQueryPerformance(parameters);

		tp.addParameter(ExecutionAlternativeEnum.QUERY_PERFORMANCE_CHECKER_PARAMETERS, query);


		Parametrizable memory = new StringParameters(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES,parameters.get(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES.toString()));

		tp.addParameter(ExecutionAlternativeEnum.MEMORY_FOR_QUERIES, memory);

		Parametrizable dpr = new StringParameters(ExecutionAlternativeEnum.MAX_DOCS_PER_QUERY,parameters.get(ExecutionAlternativeEnum.MAX_DOCS_PER_QUERY.toString()));

		tp.addParameter(ExecutionAlternativeEnum.MAX_DOCS_PER_QUERY, dpr);
		
		
		//For quality driven methods
		
		if (parameters.containsKey(ExecutionAlternativeEnum.REVERSE_QUERY_POOL.name())){

			Parametrizable reverse = new StringParameters(ExecutionAlternativeEnum.REVERSE_QUERY_POOL,parameters.get(ExecutionAlternativeEnum.REVERSE_QUERY_POOL.toString()));

			tp.addParameter(ExecutionAlternativeEnum.REVERSE_QUERY_POOL,reverse);

			Parametrizable numquespool = new StringParameters(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL,parameters.get(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL.toString()));

			tp.addParameter(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL,numquespool);

		}
				
		
		return tp;

	}

	private Parametrizable loadQueryPerformance(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable minPrecText = new StringParameters(ExecutionAlternativeEnum.MINIMUM_PRECISION_IN_RESULTS,parameters.get(ExecutionAlternativeEnum.MINIMUM_PRECISION_IN_RESULTS.toString()));

		tp.addParameter(ExecutionAlternativeEnum.MINIMUM_PRECISION_IN_RESULTS, minPrecText);

		return tp;		

	}

	private Parametrizable loadQueryPoolPerformance(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable nouqText = new StringParameters(ExecutionAlternativeEnum.NUMBER_OF_USELESS_QUERIES,parameters.get(ExecutionAlternativeEnum.NUMBER_OF_USELESS_QUERIES.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NUMBER_OF_USELESS_QUERIES, nouqText);

		Parametrizable noqueText = new StringParameters(ExecutionAlternativeEnum.NUMBER_OF_QUERIES,parameters.get(ExecutionAlternativeEnum.NUMBER_OF_QUERIES.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NUMBER_OF_QUERIES, noqueText);

		Parametrizable precisionText = new StringParameters(ExecutionAlternativeEnum.PRECISION_OF_QUERY_POOL,parameters.get(ExecutionAlternativeEnum.PRECISION_OF_QUERY_POOL.toString()));

		tp.addParameter(ExecutionAlternativeEnum.PRECISION_OF_QUERY_POOL, precisionText);

		return tp;

	}

	private Parametrizable loadSampleExecutor(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable clusterFunctionText = new StringParameters(ExecutionAlternativeEnum.CLUSTER_FUNCTION,parameters.get(ExecutionAlternativeEnum.CLUSTER_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION, clusterFunctionText);

		Parametrizable clusterFunction = loadGenerationClusterFunction(parameters);

		tp.addParameter(ExecutionAlternativeEnum.CLUSTER_FUNCTION_PARAMETERS, clusterFunction);

		Parametrizable cardinalityText = new StringParameters(ExecutionAlternativeEnum.CARDINALITY_FUNCTION,parameters.get(ExecutionAlternativeEnum.CARDINALITY_FUNCTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION, cardinalityText);

		Parametrizable cardinalityFunction = loadCardinalityFunction(parameters);

		tp.addParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_PARAMETERS, cardinalityFunction);

		Parametrizable AleatorizeText = new StringParameters(ExecutionAlternativeEnum.ALEATORIZE_DBS,parameters.get(ExecutionAlternativeEnum.ALEATORIZE_DBS.toString()));

		tp.addParameter(ExecutionAlternativeEnum.ALEATORIZE_DBS, AleatorizeText);

		return tp;

	}

	private Parametrizable loadCardinalityFunction(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable Limits = new StringParameters(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_LIMIT,parameters.get(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_LIMIT.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CARDINALITY_FUNCTION_LIMIT, Limits);

		return tp;
	}

	private Parametrizable loadNegativeQueryParameters(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable queryGeneratorText = new StringParameters(ExecutionAlternativeEnum.NEGATIVE_QUERY_GENERATOR,parameters.get(ExecutionAlternativeEnum.NEGATIVE_QUERY_GENERATOR.toString()));

		tp.addParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_GENERATOR, queryGeneratorText);

		Parametrizable wordLoader = new StringParameters(ExecutionAlternativeEnum.WORD_LOADER,parameters.get(ExecutionAlternativeEnum.WORD_LOADER.toString()));

		tp.addParameter(ExecutionAlternativeEnum.WORD_LOADER, wordLoader);

		Parametrizable wl = loadWordLoader(parameters);

		tp.addParameter(ExecutionAlternativeEnum.WORD_LOADER_PARAMETERS, wl);

		Parametrizable wordSelectionStrategy = new StringParameters(ExecutionAlternativeEnum.WORD_SELECTION_STRATEGY,parameters.get(ExecutionAlternativeEnum.WORD_SELECTION_STRATEGY.toString()));

		tp.addParameter(ExecutionAlternativeEnum.WORD_SELECTION_STRATEGY, wordSelectionStrategy);

		Parametrizable eId = new StringParameters(ExecutionAlternativeEnum.EXPERIMENT_ID,parameters.get(ExecutionAlternativeEnum.EXPERIMENT_ID.toString()));

		tp.addParameter(ExecutionAlternativeEnum.EXPERIMENT_ID, eId);

		Parametrizable pD = new StringParameters(ExecutionAlternativeEnum.PROCESSED_DOCUMENTS,parameters.get(ExecutionAlternativeEnum.PROCESSED_DOCUMENTS.toString()));

		tp.addParameter(ExecutionAlternativeEnum.PROCESSED_DOCUMENTS, pD);



		return tp;		

	}

	private Parametrizable loadWordLoader(Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable queryOtherSource = new StringParameters(ExecutionAlternativeEnum.QUERY_OTHER_SOURCE,parameters.get(ExecutionAlternativeEnum.QUERY_OTHER_SOURCE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.QUERY_OTHER_SOURCE, queryOtherSource);

		return tp;

	}

	private Parametrizable loadPositiveQueryParameters(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable queryGeneratorText = new StringParameters(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR,parameters.get(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR.toString()));

		tp.addParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR, queryGeneratorText);

		Parametrizable queryGenerator = loadQueryGenerator(parameters);

		tp.addParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR_PARAMETERS, queryGenerator);

		Parametrizable tuplesAsStopWords = new StringParameters(ExecutionAlternativeEnum.TUPLES_AS_STOP_WORDS,parameters.get(ExecutionAlternativeEnum.TUPLES_AS_STOP_WORDS.toString()));

		tp.addParameter(ExecutionAlternativeEnum.TUPLES_AS_STOP_WORDS,tuplesAsStopWords);

		//Should be it's own parameter

		Parametrizable featureWeighter = new StringParameters(ExecutionAlternativeEnum.FEATURE_WEIGHTER,parameters.get(ExecutionAlternativeEnum.FEATURE_WEIGHTER.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FEATURE_WEIGHTER,featureWeighter);

		Parametrizable initialTuple = new StringParameters(ExecutionAlternativeEnum.INITIAL_TUPLES,parameters.get(ExecutionAlternativeEnum.INITIAL_TUPLES.toString()));

		tp.addParameter(ExecutionAlternativeEnum.INITIAL_TUPLES,initialTuple);

		//For quality driven methods
		
		if (parameters.contains(ExecutionAlternativeEnum.REVERSE_QUERY_POOL.toString())){

			Parametrizable reverse = new StringParameters(ExecutionAlternativeEnum.REVERSE_QUERY_POOL,parameters.get(ExecutionAlternativeEnum.REVERSE_QUERY_POOL.toString()));

			tp.addParameter(ExecutionAlternativeEnum.REVERSE_QUERY_POOL,reverse);

			Parametrizable numquespool = new StringParameters(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL,parameters.get(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL.toString()));

			tp.addParameter(ExecutionAlternativeEnum.NUMBER_OF_QUERIES_FROM_POOL,numquespool);

		}
		
		
		return tp;

		
	}

	private Parametrizable loadInteractionPersister(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable diskPrefix = new StringParameters(ExecutionAlternativeEnum.DISK_PREFIX,parameters.get(ExecutionAlternativeEnum.DISK_PREFIX.toString()));

		tp.addParameter(ExecutionAlternativeEnum.DISK_PREFIX,diskPrefix);

		Parametrizable fileIndex = new StringParameters(ExecutionAlternativeEnum.FILE_INDEX,parameters.get(ExecutionAlternativeEnum.FILE_INDEX.toString()));

		tp.addParameter(ExecutionAlternativeEnum.FILE_INDEX,fileIndex);

		return tp;

	}

	private Parametrizable loadQueryGenerator(
			Hashtable<String, String> parameters) {

		TableParameters tp = new TableParameters();

		Parametrizable inferredTypes = new StringParameters(ExecutionAlternativeEnum.INFERRED_TYPES,parameters.get(ExecutionAlternativeEnum.INFERRED_TYPES.toString()));

		tp.addParameter(ExecutionAlternativeEnum.INFERRED_TYPES,inferredTypes);

		Parametrizable unique = new StringParameters(ExecutionAlternativeEnum.UNIQUE,parameters.get(ExecutionAlternativeEnum.UNIQUE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.UNIQUE,unique);

		Parametrizable lowercase = new StringParameters(ExecutionAlternativeEnum.LOWERCASE,parameters.get(ExecutionAlternativeEnum.LOWERCASE.toString()));

		tp.addParameter(ExecutionAlternativeEnum.LOWERCASE,lowercase);

		Parametrizable stemmed = new StringParameters(ExecutionAlternativeEnum.STEMMED,parameters.get(ExecutionAlternativeEnum.STEMMED.toString()));

		tp.addParameter(ExecutionAlternativeEnum.STEMMED,stemmed);

		Parametrizable ommited_attributes = new StringParameters(ExecutionAlternativeEnum.OMMITED_ATTRIBUTES,parameters.get(ExecutionAlternativeEnum.OMMITED_ATTRIBUTES.toString()));

		tp.addParameter(ExecutionAlternativeEnum.OMMITED_ATTRIBUTES,ommited_attributes);

		return tp;

	}

	private List<SampleConfiguration> getActiveSampleConfigutionsId(int activeValue) {

		List<SampleConfiguration> ret = new ArrayList<SampleConfiguration>();

		try {

			Statement StmtgetSampleConfigurationId = getConnection().createStatement();

			ResultSet RSgetSampleConfigurationId = StmtgetSampleConfigurationId.executeQuery
					("SELECT S.idSampleConfiguration,S.idParameter,S.idVersion,S.idWorkload,S.idRelationConfiguration,S.idExtractionSystem,S.idQueryPoolExecutor,S.idSampleGenerator,S.use_all,S.resultsPerQuery,S.usefulNumber,S.uselessNumber,S.maxQueries,S.maxDocuments, S.CountsAll, S.baseCollection, S.docsInTraining FROM SampleConfiguration S WHERE S.active =  " + activeValue + " " +
							"ORDER BY S.idSampleConfiguration");

			while (RSgetSampleConfigurationId.next()) {

				ret.add(new SampleConfiguration(RSgetSampleConfigurationId.getInt(1), RSgetSampleConfigurationId.getInt(2),RSgetSampleConfigurationId.getInt(3),RSgetSampleConfigurationId.getInt(4),
						RSgetSampleConfigurationId.getInt(5),RSgetSampleConfigurationId.getInt(6),RSgetSampleConfigurationId.getInt(7),RSgetSampleConfigurationId.getInt(8),RSgetSampleConfigurationId.getBoolean(9),RSgetSampleConfigurationId.getInt(10),RSgetSampleConfigurationId.getInt(11),RSgetSampleConfigurationId.getInt(12),
						RSgetSampleConfigurationId.getInt(13),RSgetSampleConfigurationId.getInt(14),RSgetSampleConfigurationId.getBoolean(15),RSgetSampleConfigurationId.getString(16), RSgetSampleConfigurationId.getInt(17)));

			}

			RSgetSampleConfigurationId.close();

			StmtgetSampleConfigurationId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;


	}

	public SampleConfiguration getSampleConfigution(int sampleConfigurationId) {

		SampleConfiguration ret = null;

		try {

			Statement StmtgetSampleConfigurationId = getConnection().createStatement();

			ResultSet RSgetSampleConfigurationId = StmtgetSampleConfigurationId.executeQuery
					("SELECT S.idSampleConfiguration,S.idParameter,S.idVersion,S.idWorkload,S.idRelationConfiguration,S.idExtractionSystem,S.idQueryPoolExecutor,S.idSampleGenerator,S.use_all,S.resultsPerQuery,S.usefulNumber,S.uselessNumber,S.maxQueries,S.maxDocuments, S.CountsAll, S.baseCollection, S.docsInTraining FROM SampleConfiguration S WHERE " +
							"S.idSampleConfiguration = " + sampleConfigurationId);

			while (RSgetSampleConfigurationId.next()) {

				ret = new SampleConfiguration(RSgetSampleConfigurationId.getInt(1), RSgetSampleConfigurationId.getInt(2),RSgetSampleConfigurationId.getInt(3),RSgetSampleConfigurationId.getInt(4),
						RSgetSampleConfigurationId.getInt(5),RSgetSampleConfigurationId.getInt(6),RSgetSampleConfigurationId.getInt(7),RSgetSampleConfigurationId.getInt(8),RSgetSampleConfigurationId.getBoolean(9),RSgetSampleConfigurationId.getInt(10),RSgetSampleConfigurationId.getInt(11),RSgetSampleConfigurationId.getInt(12),
						RSgetSampleConfigurationId.getInt(13),RSgetSampleConfigurationId.getInt(14), RSgetSampleConfigurationId.getBoolean(15),RSgetSampleConfigurationId.getString(16), RSgetSampleConfigurationId.getInt(17));

				WorkloadModel wm = getWorkloadModel(ret.getWorkloadModelId());

				Version version = getVersion(ret.getVersionId(), wm);

				ret.setWorkloadModel(wm);

				ret.setVersion(version);

			}

			RSgetSampleConfigurationId.close();

			StmtgetSampleConfigurationId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;


	}

	@Override
	public void makeAvailable(Sample sample, boolean finished) {

		//		todoupdateSample = ("update `AutomaticQueryGeneration`.`Sample` set `valid`='1' where `idDatabase`= " + sample.getDatabase().getId() + " and `sample_number`="+
		//		
		//				sample.getSample_number()+" and `idVersion`= "+ sample.getVersion().getId() + " and `idWorkload`=" + sample.getWorkload().getId() + " and `idSampleConfiguration`= " + sample.getSampleConfiguration().getId());
		//		

		String valid = "0";

		if (finished)
			valid = "1";

		String todoupdateSample = ("update `AutomaticQueryGeneration`.`Sample` set `valid`='"+valid+"', usefulDocuments = "+sample.getUseful().size()+" where `idSample`= " + sample.getId() );

		performTransaction(todoupdateSample);

	}

	@Override
	public List<Hashtable<String, Double>> getAnalyzableSampleGenerationData(
			Integer sampleGeneration, List<String> attributes,
			List<String> orderBy, String where, List<String> groupBy) {

		List<Hashtable<String, Double>> ret = new ArrayList<Hashtable<String,Double>>();

		try {

			Statement StmtgetAnalyzableData = getConnection().createStatement();

			String head = headerOrderBy(orderBy);

			String groupby = headerGroupBy(groupBy);

			String query = "SELECT " + header(attributes) + " FROM SampleGeneration S where " +
					"idSample = " + sampleGeneration;

			if (!where.isEmpty()){
				query += " and " + where;
			}

			if (!groupBy.isEmpty())
				query += " Group by " + groupby;

			if (!head.isEmpty())
				query += " Order by " + head;


			//			System.out.println(query);

			ResultSet RSgetAnalyzableData = StmtgetAnalyzableData.executeQuery(query);

			while (RSgetAnalyzableData.next()) {

				Hashtable<String, Double> aux = new Hashtable<String, Double>(attributes.size());

				int i = 1;

				for (String attribute : attributes) {

					aux.put(attribute, RSgetAnalyzableData.getDouble(i));

					i++;

				}

				ret.add(aux);

			}

			RSgetAnalyzableData.close();

			StmtgetAnalyzableData.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;


	}

	@Override
	public double getSampleGenerationTotal(String normalizedXAttribute,
			Integer sampleGeneration) {
		// XXX so far only for database.

		Double ret = null;

		try {

			Statement StmtgetSampleGenerationTotal = getConnection().createStatement();

			ResultSet RSgetSampleGenerationTotal = StmtgetSampleGenerationTotal.executeQuery
					("select " + normalizedXAttribute + " from `Database` D where D.idDatabase = (select idDatabase from Sample where idSample = "+sampleGeneration+")");

			while (RSgetSampleGenerationTotal.next()) {

				ret = RSgetSampleGenerationTotal.getDouble(1);

			}

			RSgetSampleGenerationTotal.close();

			StmtgetSampleGenerationTotal.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<Integer> getIdSamplesForConfiguration(
			List<Integer> sampleConfigurationid, int ommitedValidValue) {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetSampleId = getConnection().createStatement();

			ResultSet RSgetSampleId = StmtgetSampleId.executeQuery
					("select S.idSampleConfiguration, S.idSample, S.idDatabase, S.version_seed_pos from Sample S where S.valid != "+ ommitedValidValue +" and S.idSampleConfiguration IN " + generateIn(sampleConfigurationid) + " order by S.idSampleConfiguration, S.idDatabase, idSample desc;");// and S.idSample NOT IN (376267,376274,376276,376293,376315,376322,403179,403182,403184,407437,407449,407451,409108,409111,409118,409436,409444,409448,411165,411166,411175,412506,412728,413458,413574,413576,413729,414372,414378,414388,415040,415043,415046,415584,415593,415598,421003,421007,421264,423518,423630,423632,425266,425269,425279,427184,427297,427381,438937,439235,441042,441078,442592,442663,443561,443983,444043,444148,444156,444157,444553,444557,444596,444847,444854,444888,445085,445086,445099,445177,445183,445200,445205,445433,445454,445470,445530,445576,445660,445672,445703,445845,445860,445886,446027,446040,446069,446238,446261,446295,446411,446413,446414,446654,448134,449290,453856,453930,453953,454078,454109,454135,454264,454309,454370,454578,454610,454658,454966,455029,455093,455368,455426,455436,455568,455589,455598,455657,455683,455696,455803,455827,455838,455884,455926,455950,455962,455986,464171,464211,464220,464233,464237,464263,464270,464281,464290,464294,464312,464314,648401,648402,648404,675562,675582,675617,682925,682926,682959,685269,685272,685282,685675,685677,685682,686033,686036,686048,686267,686269,686274,686449,686455,686462,686536,686542,686548,686659,686661,686667,745022,745023,745025,764254,764270,764301,765233,765364,767913,769235,769278,769337,771164,771206,771234,772775,772806,772817,774115,774116,774117,774273,774307,774937,775129,775158,775196,775756,775798,775855,776318,776336,776369,776857,776873,776888,779210,779227,779235,779894,779897,779923,780212,780220,780225,780446,780456,780464,780859,780934,780979,781095,781130,781186,781278,781285,781320,781466,781484,781527,781723,781774,781843,782355,782365,782388,792259,792297,792377,792971,792972,792973,793119,793125,793131,798410,798415,798444,798931,798936,798945,798960,798966,798987,799312,799320,799344,799657,799660,799685,799918,799985,800031,800116,800131,800167,800260,800276,800307,800393,800401,800440,800509,800525,800644,801068,801069,801081,801724,801731,801738,802216,802225,802235,802671,802675,802683,802701,802705,802743,803034,803098,803301,803322,803324,803343,803517,803523,803531,803764,803770,803797,804031,804034,804039,806755,809106,809133,809677,809771,809851,810258,810273,810391,810490,810993,811109,811132,811587,811702,811780,812347,812354,813268,813279,813339,813658,814274,814466,814624,814774,815005,815207,815443,815595,815601,815965,815974,815982,816004,816011,816343,816785,817068,817517,817519,817523,818139,818153,818187,819090,819095,819110,819333,819337,819358,819744,819751,819792,820047,820054,820074,820334,820347,820368,820931,821043,821145,821172,821175,821270,821331,821359,821391,821447,821547,821582,821627,821851,822063,822371,822418,822515,823747,823868,823916,824712,824841,824882,825563,825634,825668,826245,826251,826252,826258,826384,826424,826953,827095,827155,827344,827346,827358,827460,827522,827553,827569,827570,827586,827684,827685,827691,827807,827813,827814,827820,827858,827997,828007,828089,828115,828131,828133,828142,828165,828168,828173,828268,828269,828272,828299,828300,828309,828342,828510,828523,828672,828700,828773,829231,829232,829233,829260,829261,829266,829290,829293,829294,829307,829309,829314,829335,829340,829342,829343,829348,829353,829371,829373,829375,829381,829383,829384,829401,829404,829406,829409,829412,829416,829508,829537,829551,829634,829655,829682,829696,829699,829702,829711,829713,829727,829735,829737,829740,829744,829747,829758,829764,829770,829773,829774,829777,829786,829796,829801,829808,829814,829816,829828,901495,901497,901500,927648,927688,927754,931369,931390,931469,933559,933598,933685,935596,935614,935686,937624,937660,937950,938478,938493,938569,939248,939258,939288,939575,939586,939624,939836,939843,939881,996044,996046,996049,999796,999810,999841,1000780,1000804,1000817,1001257,1001269,1001283,1001639,1001658,1001669,1002298,1002350,1002589,1002673,1002694,1002715,1002921,1002945,1002962,1003115,1003149,1003157,1003341,1003356,1003373,1012921,1012923,1012929,1013056,1013108,1013137,1014440,1014475,1014527,1014858,1014864,1014903,1015089,1015099,1015147,1015302,1015315,1015347,1016048,1016079,1016233,1016237,1016242,1016265,1016269,1016277,1016287,1016290,1016296,1016309,1016314,1016317,1016450,1018328,1018329,1018331,1018352,1018355,1018358,1018365,1018368,1018370,1018384,1018387,1018388,1018401,1018405,1018406,1018489,1018501,1018507,1018521,1018525,1018532,1018538,1018541,1018549,1018566,1018571,1018577,1018601,1018607,1018614,1019614,1019617,1019620,1019657,1019662,1019664,1019707,1019709,1019712,1019780,1019794,1019798,1019809,1019811,1019813,1019818,1019819,1019821,1019834,1019838,1019841,1019851,1019854,1019859,1019860,1019863,1019865,1019881,1019885,1019888,1020009,1020028,1020040,1020055,1020062,1020078,1020085,1020091,1020100,1020119,1020127,1020132,1020137,1020142,1020146,1020150,1020154,1020157,1020176,1020181,1020182,1020186,1020188,1020192,1020286,1020288,1020300,1020327,1020340,1020349,1028707,1028712,1029586,1029588,1029589,1032396,1032409,1032430,1032771,1032781,1032792,1032974,1032982,1032996,1033178,1033191,1033200,1033363,1033365,1033366,1033473,1033536,1033550,1033585,1033596,1033626,1033738,1033752,1033769,1033851,1033857,1033887,1033974,1033987,1034007,1034572,1034878,1034984,1035097,1035100,1035102,1035232,1035263,1035264,1035270,1035340,1035344,1035347,1035577,1035580,1035621,1035668,1035670,1035705,1035706,1035716,1035721,1035750,1035756,1035757,1035790,1035792,1035837,1035926,1036011,1036168,1036926,1037281,1037903,1038216,1038522,1038768,1038798,1038833,1038859,1038886,1120146,1120148,1120149,1124872,1124878,1124882,1125551,1125553,1125566,1125861,1125863,1125865,1126302,1126316,1126323,1126577,1126608,1126722,1126774,1126783,1126794,1126878,1126879,1126892,1127053,1127064,1127068,1127207,1127222,1127234,1144781,1144783,1144784,1154079,1154081,1154094,1155247,1155251,1155259,1155800,1155803,1155811,1156194,1156196,1156210,1156382,1156469,1156481,1156496,1156499,1156505,1156649,1156653,1156658,1156774,1156777,1156783,1156888,1156890,1156900,1159155,1159156,1159162,1159438,1159448,1159544,1160143,1160149,1160150,1160556,1160561,1160563,1160789,1160795,1160797,1161131,1161146,1161148,1161344,1161359,1161375,1161541,1161556,1161558,1161609,1161621,1161624,1161735,1161741,1161742,1161757,1161761,1161771,1168125,1168686,1168938,1170335,1170341,1170360,1171696,1171702,1171716,1172595,1172597,1172600,1173422,1173435,1173444,1174399,1174400,1174415,1174759,1174764,1174770,1175056,1175058,1175076,1175281,1175302,1175305,1175315,1175346,1175477,1175481,1175491,1175493,1175813,1175824,1175911,1175938,1175941,1175956,1176059,1176062,1176084,1176143,1176152,1176160,1176200,1176205,1176217,1176497,1176518,1176530,1177253,1177256,1177274,1177957,1177961,1177979,1178407,1178414,1178435,1180018,1180021,1180046,1182603,1182605,1182623,1182659,1182717,1182725,1182734,1182735,1182740,1182811,1182815,1182818,1182839,1182842,1182848,1182857,1182858,1182862,1183009,1183023,1183046,1183054,1183057,1183060,1183108,1183111,1183117,1183151,1183154,1183161,1183182,1183189,1183194,1183481,1183482,1183483,1183491,1183493,1183495,1183505,1183507,1183509,1183518,1183524,1183528,1183538,1183539,1183543,1183642,1183659,1183664,1183717,1183719,1183720,1183742,1183744,1183746,1183761,1183764,1183766,1183772,1183774,1183776,1184223,1184225,1184229,1184272,1184273,1184276,1184292,1184294,1184306,1184322,1184323,1184327,1184417,1184418,1184422,1184537,1184543,1184547,1184562,1184574,1184588,1184605,1184624,1184681,1184699,1184707,1184710,1184727,1184729,1184731,1186072,1186074,1186075,1186144,1186151,1186153,1187381,1187388,1187390,1187670,1187671,1187673,1187903,1187906,1187910,1188107,1188109,1188117,1189462,1189466,1189846,1189848,1189854,1189856,1189859,1189867,1189871,1189872,1189885,1189887,1189890,1189917,1189918,1189922,1189950,1189951,1189955,1189960,1189961,1190085,1190088,1190100,1190103,1190116,1190118,1190130,1190133,1190134,1190145,1190146,1190152,1190154,1190159,1190190,1190273,1190304,1190310,1190349,1190368,1190371,1190416,1190583,1190702,1234965,1234968,1234977,1241158,1241169,1241184,1244835,1245814,1248699,1248705,1249029,1249600,1249617,1249871,1249878,1250037,1250232,1250241,1250273,1250297,1250315,1250743,1250827,1250853,1250860,1251229,1251235,1251245,1251286,1251745,1251871,1252091,1253269,1254948,1257775,1259663,1259665,1259668,1260110,1260298,1261551,1263219,1263222,1263322,1263324,1264219,1264225,1265244,1265330,1265333,1265338,1265864,1266413,1266553,1266613,1266991,1267466,1267491,1267585,1267667,1267697,1268557,1268700,1270465,1271469,1271520,1272036,1272111,1272222,1272436,1272634,1272670,1272711,1272873,1272881,1272916,1272945,1272967,1273017,1273024,1273059,1273091,1273100,1273154,1273225,1273230,1273242,1273416,1273424,1273433,1273447,1273467,1273473,1273497,1273575,1273604,1273627,1273840,1273918,1273920,1273923,1274066,1274087,1274121,1274136,1274143,1274258,1274283,1274317,1274344,1274353,1274365,1274374,1274376,1274417,1274437,1274443,1274448,1274450,1274456,1274463,1274469,1274476,1274488,1274497,1274501,1274512,1274519,1274527,1274532,1274543,1274552,1274557,1274563,1274672,1274688,1274689,1274715,1274740,1274755,1274782,1274791,1274916,1274927,1274935,1274940,1274977,1275147,1275452,1276189,1276192,1276199,1276512,1276531,1276573,1276615,1276620,1276631,1276633,1276636,1276658,1276664,1276666,1276668,1276672,1276688,1276689,1276691,1276696,1276702,1276709,1276720,1276722,1276727,1276736,1276741,1276744,1276747,1276839,1276847,1276912,1276977,1277038,1277064,1277125,1277129,1277146,1277152,1277291,1277295,1277335,1277348,1277350,1277373,1277374,1277388)"); //XXX these samples could be removed but I dont want to break anything 

			int maxiddb = 3002;
			int currentsc = -1;
			int sc,db,vers;
			
			System.err.println("If dbs have more than 4000 ids, then review this code");
			
			byte[] stat = new byte[maxiddb+1];
			
			while (RSgetSampleId.next()) {

				sc = RSgetSampleId.getInt(1);
				db = RSgetSampleId.getInt(3);
//				vers = RSgetSampleId.getInt(4);

				if (currentsc == -1){ //first time
					currentsc = sc;
				} else if (currentsc != sc){
					//switch sample configuration
					for (int i = 0; i < maxiddb + 1; i++) {
						stat[i] = 0;
					}
					
					currentsc = RSgetSampleId.getInt(1);
				
				}
	
				//I could do this based on bits [00110], one for each experiment.
				
				stat[db]++;
				
				if (stat[db] <= 5)
					ret.add(RSgetSampleId.getInt(2));

			}

			RSgetSampleId.close();

			StmtgetSampleId.close();

		} catch (Exception e) { //SQL and IndexOutofBounds

			System.out.println(e.getMessage());

		}

		return ret;

	}

	@Override
	public List<Database> getSamplableDatabases(String computerName) {

		List<Database> ret = new ArrayList<Database>();

		try {

			Statement StmtgetSamplableDatabases = getConnection().createStatement();
			ResultSet RSgetSamplableDatabases;

			if (computerName != null)
				RSgetSamplableDatabases = StmtgetSamplableDatabases.executeQuery
				("SELECT D.idDatabase" +
						" FROM `Database` D join HostDatabase H on D.idDatabase= H.idDatabase WHERE H.host = '" + format(computerName) + "'");
			else {

				RSgetSamplableDatabases = StmtgetSamplableDatabases.executeQuery
						("SELECT D.idDatabase FROM `Database` D where searchable = 1");

			}

			while (RSgetSamplableDatabases.next()) {

				int id = RSgetSamplableDatabases.getInt(1);

				ret.add(getDatabase(id));

			}

			RSgetSamplableDatabases.close();
			StmtgetSamplableDatabases.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<Database> getDatabasesInGroup(int id,
			ClusterFunction clusterFunction, Version version,
			WorkloadModel workload) {

		List<Database> ret = new ArrayList<Database>();

		try {

			Statement StmtgetInGroupDatabases = getConnection().createStatement();

			String query = "Select idDatabase from `AutomaticQueryGeneration`.`DatabaseCluster` where idCluster = "+id+" and idClusterFunction = "+getClusterFunctionId(clusterFunction.getEnum())+" and (idVersion = "+version.getId()+" or idVersion = -1) and (idWorkload = " + workload.getId() + " or idWorkload = -1)";

			ResultSet RSgetInGroupDatabases = StmtgetInGroupDatabases.executeQuery
					(query);

			while (RSgetInGroupDatabases.next()) {

				int did = RSgetInGroupDatabases.getInt(1);

				ret.add(getDatabase(did));

			}

			RSgetInGroupDatabases.close();
			StmtgetInGroupDatabases.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<Integer> getIdSamplesForConfigurationOnDatabases(
			List<Integer> configuration, int ommitedValidValue,
			List<Integer> databases) {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetSampleId = getConnection().createStatement();

			ResultSet RSgetSampleId = StmtgetSampleId.executeQuery
					("select S.idSampleConfiguration, S.idSample, S.idDatabase, S.version_seed_pos from Sample S where S.valid != "+ ommitedValidValue +" and idSampleConfiguration IN " + generateIn(configuration) + " and idDatabase IN " + generateIn(databases) + " order by S.idSampleConfiguration, S.idDatabase, idSample desc;");// and S.idSample NOT IN (376267,376274,376276,376293,376315,376322,403179,403182,403184,407437,407449,407451,409108,409111,409118,409436,409444,409448,411165,411166,411175,412506,412728,413458,413574,413576,413729,414372,414378,414388,415040,415043,415046,415584,415593,415598,421003,421007,421264,423518,423630,423632,425266,425269,425279,427184,427297,427381,438937,439235,441042,441078,442592,442663,443561,443983,444043,444148,444156,444157,444553,444557,444596,444847,444854,444888,445085,445086,445099,445177,445183,445200,445205,445433,445454,445470,445530,445576,445660,445672,445703,445845,445860,445886,446027,446040,446069,446238,446261,446295,446411,446413,446414,446654,448134,449290,453856,453930,453953,454078,454109,454135,454264,454309,454370,454578,454610,454658,454966,455029,455093,455368,455426,455436,455568,455589,455598,455657,455683,455696,455803,455827,455838,455884,455926,455950,455962,455986,464171,464211,464220,464233,464237,464263,464270,464281,464290,464294,464312,464314,648401,648402,648404,675562,675582,675617,682925,682926,682959,685269,685272,685282,685675,685677,685682,686033,686036,686048,686267,686269,686274,686449,686455,686462,686536,686542,686548,686659,686661,686667,745022,745023,745025,764254,764270,764301,765233,765364,767913,769235,769278,769337,771164,771206,771234,772775,772806,772817,774115,774116,774117,774273,774307,774937,775129,775158,775196,775756,775798,775855,776318,776336,776369,776857,776873,776888,779210,779227,779235,779894,779897,779923,780212,780220,780225,780446,780456,780464,780859,780934,780979,781095,781130,781186,781278,781285,781320,781466,781484,781527,781723,781774,781843,782355,782365,782388,792259,792297,792377,792971,792972,792973,793119,793125,793131,798410,798415,798444,798931,798936,798945,798960,798966,798987,799312,799320,799344,799657,799660,799685,799918,799985,800031,800116,800131,800167,800260,800276,800307,800393,800401,800440,800509,800525,800644,801068,801069,801081,801724,801731,801738,802216,802225,802235,802671,802675,802683,802701,802705,802743,803034,803098,803301,803322,803324,803343,803517,803523,803531,803764,803770,803797,804031,804034,804039,806755,809106,809133,809677,809771,809851,810258,810273,810391,810490,810993,811109,811132,811587,811702,811780,812347,812354,813268,813279,813339,813658,814274,814466,814624,814774,815005,815207,815443,815595,815601,815965,815974,815982,816004,816011,816343,816785,817068,817517,817519,817523,818139,818153,818187,819090,819095,819110,819333,819337,819358,819744,819751,819792,820047,820054,820074,820334,820347,820368,820931,821043,821145,821172,821175,821270,821331,821359,821391,821447,821547,821582,821627,821851,822063,822371,822418,822515,823747,823868,823916,824712,824841,824882,825563,825634,825668,826245,826251,826252,826258,826384,826424,826953,827095,827155,827344,827346,827358,827460,827522,827553,827569,827570,827586,827684,827685,827691,827807,827813,827814,827820,827858,827997,828007,828089,828115,828131,828133,828142,828165,828168,828173,828268,828269,828272,828299,828300,828309,828342,828510,828523,828672,828700,828773,829231,829232,829233,829260,829261,829266,829290,829293,829294,829307,829309,829314,829335,829340,829342,829343,829348,829353,829371,829373,829375,829381,829383,829384,829401,829404,829406,829409,829412,829416,829508,829537,829551,829634,829655,829682,829696,829699,829702,829711,829713,829727,829735,829737,829740,829744,829747,829758,829764,829770,829773,829774,829777,829786,829796,829801,829808,829814,829816,829828,901495,901497,901500,927648,927688,927754,931369,931390,931469,933559,933598,933685,935596,935614,935686,937624,937660,937950,938478,938493,938569,939248,939258,939288,939575,939586,939624,939836,939843,939881,996044,996046,996049,999796,999810,999841,1000780,1000804,1000817,1001257,1001269,1001283,1001639,1001658,1001669,1002298,1002350,1002589,1002673,1002694,1002715,1002921,1002945,1002962,1003115,1003149,1003157,1003341,1003356,1003373,1012921,1012923,1012929,1013056,1013108,1013137,1014440,1014475,1014527,1014858,1014864,1014903,1015089,1015099,1015147,1015302,1015315,1015347,1016048,1016079,1016233,1016237,1016242,1016265,1016269,1016277,1016287,1016290,1016296,1016309,1016314,1016317,1016450,1018328,1018329,1018331,1018352,1018355,1018358,1018365,1018368,1018370,1018384,1018387,1018388,1018401,1018405,1018406,1018489,1018501,1018507,1018521,1018525,1018532,1018538,1018541,1018549,1018566,1018571,1018577,1018601,1018607,1018614,1019614,1019617,1019620,1019657,1019662,1019664,1019707,1019709,1019712,1019780,1019794,1019798,1019809,1019811,1019813,1019818,1019819,1019821,1019834,1019838,1019841,1019851,1019854,1019859,1019860,1019863,1019865,1019881,1019885,1019888,1020009,1020028,1020040,1020055,1020062,1020078,1020085,1020091,1020100,1020119,1020127,1020132,1020137,1020142,1020146,1020150,1020154,1020157,1020176,1020181,1020182,1020186,1020188,1020192,1020286,1020288,1020300,1020327,1020340,1020349,1028707,1028712,1029586,1029588,1029589,1032396,1032409,1032430,1032771,1032781,1032792,1032974,1032982,1032996,1033178,1033191,1033200,1033363,1033365,1033366,1033473,1033536,1033550,1033585,1033596,1033626,1033738,1033752,1033769,1033851,1033857,1033887,1033974,1033987,1034007,1034572,1034878,1034984,1035097,1035100,1035102,1035232,1035263,1035264,1035270,1035340,1035344,1035347,1035577,1035580,1035621,1035668,1035670,1035705,1035706,1035716,1035721,1035750,1035756,1035757,1035790,1035792,1035837,1035926,1036011,1036168,1036926,1037281,1037903,1038216,1038522,1038768,1038798,1038833,1038859,1038886,1120146,1120148,1120149,1124872,1124878,1124882,1125551,1125553,1125566,1125861,1125863,1125865,1126302,1126316,1126323,1126577,1126608,1126722,1126774,1126783,1126794,1126878,1126879,1126892,1127053,1127064,1127068,1127207,1127222,1127234,1144781,1144783,1144784,1154079,1154081,1154094,1155247,1155251,1155259,1155800,1155803,1155811,1156194,1156196,1156210,1156382,1156469,1156481,1156496,1156499,1156505,1156649,1156653,1156658,1156774,1156777,1156783,1156888,1156890,1156900,1159155,1159156,1159162,1159438,1159448,1159544,1160143,1160149,1160150,1160556,1160561,1160563,1160789,1160795,1160797,1161131,1161146,1161148,1161344,1161359,1161375,1161541,1161556,1161558,1161609,1161621,1161624,1161735,1161741,1161742,1161757,1161761,1161771,1168125,1168686,1168938,1170335,1170341,1170360,1171696,1171702,1171716,1172595,1172597,1172600,1173422,1173435,1173444,1174399,1174400,1174415,1174759,1174764,1174770,1175056,1175058,1175076,1175281,1175302,1175305,1175315,1175346,1175477,1175481,1175491,1175493,1175813,1175824,1175911,1175938,1175941,1175956,1176059,1176062,1176084,1176143,1176152,1176160,1176200,1176205,1176217,1176497,1176518,1176530,1177253,1177256,1177274,1177957,1177961,1177979,1178407,1178414,1178435,1180018,1180021,1180046,1182603,1182605,1182623,1182659,1182717,1182725,1182734,1182735,1182740,1182811,1182815,1182818,1182839,1182842,1182848,1182857,1182858,1182862,1183009,1183023,1183046,1183054,1183057,1183060,1183108,1183111,1183117,1183151,1183154,1183161,1183182,1183189,1183194,1183481,1183482,1183483,1183491,1183493,1183495,1183505,1183507,1183509,1183518,1183524,1183528,1183538,1183539,1183543,1183642,1183659,1183664,1183717,1183719,1183720,1183742,1183744,1183746,1183761,1183764,1183766,1183772,1183774,1183776,1184223,1184225,1184229,1184272,1184273,1184276,1184292,1184294,1184306,1184322,1184323,1184327,1184417,1184418,1184422,1184537,1184543,1184547,1184562,1184574,1184588,1184605,1184624,1184681,1184699,1184707,1184710,1184727,1184729,1184731,1186072,1186074,1186075,1186144,1186151,1186153,1187381,1187388,1187390,1187670,1187671,1187673,1187903,1187906,1187910,1188107,1188109,1188117,1189462,1189466,1189846,1189848,1189854,1189856,1189859,1189867,1189871,1189872,1189885,1189887,1189890,1189917,1189918,1189922,1189950,1189951,1189955,1189960,1189961,1190085,1190088,1190100,1190103,1190116,1190118,1190130,1190133,1190134,1190145,1190146,1190152,1190154,1190159,1190190,1190273,1190304,1190310,1190349,1190368,1190371,1190416,1190583,1190702,1234965,1234968,1234977,1241158,1241169,1241184,1244835,1245814,1248699,1248705,1249029,1249600,1249617,1249871,1249878,1250037,1250232,1250241,1250273,1250297,1250315,1250743,1250827,1250853,1250860,1251229,1251235,1251245,1251286,1251745,1251871,1252091,1253269,1254948,1257775,1259663,1259665,1259668,1260110,1260298,1261551,1263219,1263222,1263322,1263324,1264219,1264225,1265244,1265330,1265333,1265338,1265864,1266413,1266553,1266613,1266991,1267466,1267491,1267585,1267667,1267697,1268557,1268700,1270465,1271469,1271520,1272036,1272111,1272222,1272436,1272634,1272670,1272711,1272873,1272881,1272916,1272945,1272967,1273017,1273024,1273059,1273091,1273100,1273154,1273225,1273230,1273242,1273416,1273424,1273433,1273447,1273467,1273473,1273497,1273575,1273604,1273627,1273840,1273918,1273920,1273923,1274066,1274087,1274121,1274136,1274143,1274258,1274283,1274317,1274344,1274353,1274365,1274374,1274376,1274417,1274437,1274443,1274448,1274450,1274456,1274463,1274469,1274476,1274488,1274497,1274501,1274512,1274519,1274527,1274532,1274543,1274552,1274557,1274563,1274672,1274688,1274689,1274715,1274740,1274755,1274782,1274791,1274916,1274927,1274935,1274940,1274977,1275147,1275452,1276189,1276192,1276199,1276512,1276531,1276573,1276615,1276620,1276631,1276633,1276636,1276658,1276664,1276666,1276668,1276672,1276688,1276689,1276691,1276696,1276702,1276709,1276720,1276722,1276727,1276736,1276741,1276744,1276747,1276839,1276847,1276912,1276977,1277038,1277064,1277125,1277129,1277146,1277152,1277291,1277295,1277335,1277348,1277350,1277373,1277374,1277388)"); //XXX these samples could be removed but I dont want to break anything 

			int maxiddb = 3002;
			int currentsc = -1;
			int sc,db,vers;
			
			System.err.println("If dbs have more than 4000 ids, then review this code");
			
			byte[] stat = new byte[maxiddb+1];
			
			while (RSgetSampleId.next()) {

				sc = RSgetSampleId.getInt(1);
				db = RSgetSampleId.getInt(3);
//				vers = RSgetSampleId.getInt(4);

				if (currentsc == -1){ //first time
					currentsc = sc;
				} else if (currentsc != sc){
					//switch sample configuration
					for (int i = 0; i < maxiddb + 1; i++) {
						stat[i] = 0;
					}
					
					currentsc = RSgetSampleId.getInt(1);
				
				}
	
				//I could do this based on bits [00110], one for each experiment.
				
				stat[db]++;
				
				if (stat[db] <= 5)
					ret.add(RSgetSampleId.getInt(2));

			}

			RSgetSampleId.close();

			StmtgetSampleId.close();

		} catch (Exception e) { //SQLException and IndexOutOfBounds

			System.out.println(e.getMessage());

		}

		return ret;

	}

	public Sample getSample(int sampleGeneration){

		try {

			//			if (PStmtsampleDetails == null){
			//				PStmtsampleDetails = getConnection().prepareStatement(sampleDetails);
			//			}else
			//				PStmtsampleDetails.clearParameters();

			PreparedStatement PStmtsampleDetails = getConnection().prepareStatement(sampleDetails);

			PStmtsampleDetails.setInt(1, sampleGeneration);

			ResultSet RSgetsampleDetails = PStmtsampleDetails.executeQuery();

			int idDatabase = -1;
			int idVersion = -1; 
			int idWorkload = -1;
			int version_seed_pos = -1;
			int version_seed_neg = -1;
			int idSampleConfiguration = -1;

			while (RSgetsampleDetails.next()) {

				idDatabase = RSgetsampleDetails.getInt(1);

				idVersion = RSgetsampleDetails.getInt(2);

				idWorkload = RSgetsampleDetails.getInt(3);

				version_seed_pos = RSgetsampleDetails.getInt(4);

				version_seed_neg = RSgetsampleDetails.getInt(5);

				idSampleConfiguration = RSgetsampleDetails.getInt(6);

			}

			RSgetsampleDetails.close();
			PStmtsampleDetails.close();

			WorkloadModel wm = getWorkloadModel(idWorkload);

			Sample sample = Sample.getSample(getDatabase(idDatabase), getVersion(idVersion, wm), wm, version_seed_pos,version_seed_neg, new DummySampleConfiguration(idSampleConfiguration));
			
			sample.setId(sampleGeneration);
			
			return sample;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public int getSampleId(int idSampleConfiguration, int idDatabase, int version_seed_pos, int version_seed_neg){

		int idSample = -1;

		try {

			//			if (PStmtsampleId == null){
			//				PStmtsampleId = getConnection().prepareStatement(sampleId);
			//			}else
			//				PStmtsampleId.clearParameters();

			PreparedStatement PStmtsampleId = getConnection().prepareStatement(sampleId);

			PStmtsampleId.setInt(1, idSampleConfiguration);
			PStmtsampleId.setInt(2, idDatabase);
			PStmtsampleId.setInt(3, version_seed_pos);
			PStmtsampleId.setInt(4, version_seed_neg);

			ResultSet RSgetsampleId = PStmtsampleId.executeQuery();

			while (RSgetsampleId.next()) {

				idSample = RSgetsampleId.getInt(1);


			}

			RSgetsampleId.close();
			PStmtsampleId.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return idSample;

	}

	@Override
	public List<int[]> getSampleGeneration(int sampleNumber, int documents) {

		try {

			PreparedStatement PStmtsampleGeneration,PStmtsampleGenerationConstrained,PStmtsampleGenerationNotConstrained;

			int docPos = getDocumentPosition(sampleNumber,documents);

			List<int[]> ret = new ArrayList<int[]>();

			if (docPos > 0){

				//				if (PStmtsampleGenerationConstrained == null){
				//					PStmtsampleGenerationConstrained = getConnection().prepareStatement(sampleGenerationConstrained);
				//				}else
				//					PStmtsampleGenerationConstrained.clearParameters();

				PStmtsampleGenerationConstrained = getConnection().prepareStatement(sampleGenerationConstrained);

				PStmtsampleGenerationConstrained.setInt(1, sampleNumber);
				PStmtsampleGenerationConstrained.setInt(2, docPos);
				PStmtsampleGenerationConstrained.setInt(3, docPos);
				PStmtsampleGenerationConstrained.setInt(4, documents);

				PStmtsampleGeneration = PStmtsampleGenerationConstrained;

			} else {

				//				if (PStmtsampleGenerationNotConstrained == null){
				//					PStmtsampleGenerationNotConstrained = getConnection().prepareStatement(sampleGenerationNotConstrained);
				//				}else
				//					PStmtsampleGenerationNotConstrained.clearParameters();

				PStmtsampleGenerationNotConstrained = getConnection().prepareStatement(sampleGenerationNotConstrained);

				PStmtsampleGenerationNotConstrained.setInt(1, sampleNumber);

				PStmtsampleGeneration = PStmtsampleGenerationNotConstrained;

			}

			ResultSet RSgetsampleGeneration = PStmtsampleGeneration.executeQuery();

			int query_generated_position = -1;
			int query_submitted_position = -1;
			int query_round = -1;
			int documentPosition = -1; 
			int docInQueryPosition = -1;
			int docPosInSample = -1;
			int usefulTuples = -1;
			int documentDatabase = -1;
			int documentId = -1;

			while (RSgetsampleGeneration.next()) {

				query_generated_position = RSgetsampleGeneration.getInt(1);

				query_submitted_position = RSgetsampleGeneration.getInt(2);

				query_round = RSgetsampleGeneration.getInt(3);

				documentPosition = RSgetsampleGeneration.getInt(4);

				docInQueryPosition = RSgetsampleGeneration.getInt(5);

				docPosInSample = RSgetsampleGeneration.getInt(6);

				usefulTuples = RSgetsampleGeneration.getInt(7);

				documentDatabase = RSgetsampleGeneration.getInt(8);

				documentId = RSgetsampleGeneration.getInt(9);

				ret.add(new int[]{query_generated_position,query_submitted_position,query_round,documentPosition,docInQueryPosition,docPosInSample,usefulTuples,documentDatabase,documentId});

			}

			RSgetsampleGeneration.close();
			PStmtsampleGeneration.close();

			return ret;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	private int getDocumentPosition(int sampleNumber, int documents) {

		try {

			int ret = -1;

			//			if (PStmtgetDocumentPosition == null){
			//				PStmtgetDocumentPosition = getConnection().prepareStatement(getDocumentPositionString);
			//			}else
			//				PStmtgetDocumentPosition.clearParameters();

			PreparedStatement PStmtgetDocumentPosition = getConnection().prepareStatement(getDocumentPositionString);

			PStmtgetDocumentPosition.setInt(1, sampleNumber);
			PStmtgetDocumentPosition.setInt(2, documents);

			ResultSet RSgetDocumentPosition = PStmtgetDocumentPosition.executeQuery();

			while (RSgetDocumentPosition.next()) {

				ret = RSgetDocumentPosition.getInt(1);

			}

			RSgetDocumentPosition.close();
			PStmtgetDocumentPosition.close();

			return ret;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;

	}

	public List<Pair<int[], Long>> getSampleGenerationQueries(int sampleNumber, int lastQuery){

		try {

			List<Pair<int[],Long>> ret = new ArrayList<Pair<int[],Long>>();

			//			if (PStmtsampleGenerationQueries == null){
			//				PStmtsampleGenerationQueries = getConnection().prepareStatement(sampleGenerationQueries);
			//			}else
			//				PStmtsampleGenerationQueries.clearParameters();

			PreparedStatement PStmtsampleGenerationQueries = getConnection().prepareStatement(sampleGenerationQueries);

			PStmtsampleGenerationQueries.setInt(1, sampleNumber);
			PStmtsampleGenerationQueries.setInt(2, lastQuery);

			ResultSet RSgetsampleGenerationQueries = PStmtsampleGenerationQueries.executeQuery();

			int generated_query = -1; 
			Long query = null;

			while (RSgetsampleGenerationQueries.next()) {

				generated_query = RSgetsampleGenerationQueries.getInt(1);

				query = RSgetsampleGenerationQueries.getLong(2);

				ret.add(new Pair<int[], Long>(new int[]{generated_query}, query));

			}

			RSgetsampleGenerationQueries.close();
			PStmtsampleGenerationQueries.close();

			return ret;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}		

	//	public int getLastSampleConfiguration(int idDatabase){
	//
	//		int ret = -1;
	//
	//		try {
	//
	//			Statement StmtgetLastSampleConfiguration = getConnection().createStatement();
	//
	//			ResultSet RSgetLastSampleConfiguration = StmtgetLastSampleConfiguration.executeQuery
	//					("select max(S.idSampleConfiguration) from Sample S where idDatabase = " + idDatabase);
	//
	//			while (RSgetLastSampleConfiguration.next()) {
	//
	//				ret = RSgetLastSampleConfiguration.getInt(1);
	//
	//			}
	//
	//			RSgetLastSampleConfiguration.close();
	//
	//			StmtgetLastSampleConfiguration.close();
	//
	//		} catch (SQLException e) {
	//
	//			e.printStackTrace();
	//
	//		}
	//
	//		return ret;
	//
	//	}

	public void clean(int idDatabase, int idSampleConfiguration){

		String todo = "delete FROM `AutomaticQueryGeneration`.`SampleGeneration` where idSample IN (select idSample from Sample where idDatabase = "+idDatabase+" and idSampleConfiguration >= "+idSampleConfiguration+")";

		performTransaction(todo);

		todo = "delete FROM `AutomaticQueryGeneration`.`SampleGenerationQueries` where idSample IN (select idSample from Sample where idDatabase = "+idDatabase+" and idSampleConfiguration >= "+idSampleConfiguration+")";

		performTransaction(todo);

		todo = "delete FROM `AutomaticQueryGeneration`.`Sample` where idDatabase = "+idDatabase+"  and idSampleConfiguration >= " + idSampleConfiguration; 

		performTransaction(todo);

	}

	public File getTuplesInDatabaseFile(String dbName, String relation) {

		File t = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tuples/" + dbName + "/");

		if (!t.exists())
			t.mkdirs();

		return new File(t,relation + ".tuples");

	}

	@Override
	public void insertExtraction(int idExtractionSystem, Document document, String internalExtraction, ContentExtractor ce) {

		try {

			//			if (PStmtinsertExtraction == null){
			//				PStmtinsertExtraction = getConnection().prepareStatement(insertExtractionString);
			//			}else
			//				PStmtinsertExtraction.clearParameters();

			PreparedStatement PStmtinsertExtraction = getConnection().prepareStatement(insertExtractionString);

			PStmtinsertExtraction.setInt(1, idExtractionSystem);
			PStmtinsertExtraction.setLong(2, document.getDatabase().getId());
			PStmtinsertExtraction.setLong(3, document.getId());
			PStmtinsertExtraction.setString(4, format(internalExtraction));
			PStmtinsertExtraction.setInt(5, getContentExtractorId(ce));

			PStmtinsertExtraction.execute();

			PStmtinsertExtraction.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private int getContentExtractorId(ContentExtractor ce) {

		synchronized (contentExtractionTable) {

			if (contentExtractionTable.isEmpty()){

				try {

					Statement StmtgetContentExtractionTable = getConnection().createStatement();

					ResultSet RSgetContentExtractionTable = StmtgetContentExtractionTable.executeQuery
							("select idContentExtractionSystem,Description from ContentExtractionSystem S"); 

					while (RSgetContentExtractionTable.next()) {

						contentExtractionTable.put(RSgetContentExtractionTable.getString(2),RSgetContentExtractionTable.getInt(1));

					}

					RSgetContentExtractionTable.close();

					StmtgetContentExtractionTable.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

			return contentExtractionTable.get(ce.getName());
		}

	}

	@Override
	public Map<Document, String> getExtractionTable(int idDatabase, int idExtractionSystem, ContentExtractor ce) {

		return getExtractionTable(idDatabase, idExtractionSystem, ce, new ArrayList<Long>(0));

	}

	@Override
	public Map<Document, String> getExtractionTable(int idDatabase, int idExtractionSystem, ContentExtractor ce, List<Long> docs) {

		Map<Document,String> ret = new HashMap<Document, String>();

		String restr = "";

		if (!docs.isEmpty()){
			restr = " and docId IN " + generateIn(docs);
		}

		try {

			Statement StmtgetExtractionTable = getConnection().createStatement();

			System.out.println(idExtractionSystem);
			
			ResultSet RSgetExtractionTable = StmtgetExtractionTable.executeQuery
					("select docId,fileAuxiliar from Extraction S where idDatabase = " + idDatabase + " and idExtractionSystem = " + idExtractionSystem + " and contentExtractor = " + getContentExtractorId(ce) + restr); 

			while (RSgetExtractionTable.next()) {

				ret.put(new Document(getDatabase(idDatabase),RSgetExtractionTable.getLong(1)),RSgetExtractionTable.getString(2));

			}

			RSgetExtractionTable.close();

			StmtgetExtractionTable.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}
	@Override
	public long writeTextQuery(TextQuery query) {

		long ret = -1L;

		try {

			//			if (PStmtwriteTextQuery == null){
			//				PStmtwriteTextQuery = getConnection().prepareStatement(insertwriteTextQuery, Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtwriteTextQuery.clearParameters();

			PreparedStatement PStmtwriteTextQuery = getConnection().prepareStatement(insertwriteTextQuery, Statement.RETURN_GENERATED_KEYS);

			PStmtwriteTextQuery.setString(1, format(query.getText()));

			PStmtwriteTextQuery.execute();

			//			ret =  createdQueryId();
			//
			//			trick("TextQuery");

			ResultSet rs = PStmtwriteTextQuery.getGeneratedKeys();

			if (rs != null && rs.next()) {
				ret = rs.getLong(1);
			}

			putTextQueryTable(query,ret);

			PStmtwriteTextQuery.close();
			rs.close();

		} catch (SQLException e) {

			e.printStackTrace();

			return getTextQueryTable(true,query); //was added while doing it.

		}

		return ret;

	}


	private void putTextQueryTable(TextQuery query, long ret) {

		getTextQueryTable(false, query);

		synchronized (textquerytable) {

			textquerytable.put(query, ret);

		}

	}

	//	private synchronized long createdQueryId() {
	//
	//		long res = 0;
	//
	//		try {
	//			StmtcreatedQueryId = getConnection().createStatement();
	//
	//			RScreatedQueryId = StmtcreatedQueryId.executeQuery
	//					("SELECT LAST_INSERT_ID()");
	//
	//			while (RScreatedQueryId.next()) {
	//
	//				res = RScreatedQueryId.getLong(1);
	//
	//			}
	//			RScreatedQueryId.close();
	//			StmtcreatedQueryId.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//		return res;
	//
	//	}

	@Override
	public void saveRawResultPage(int expId, int idDatabase,
			TextQuery texQuery, String navigationTechnique, int page) {

		long qId = getTextQuery(texQuery);

		try {

			//			if (PStmtsaveRawResultPage == null){
			//				PStmtsaveRawResultPage = getConnection().prepareStatement(saveRawResultPageString);
			//			}else
			//				PStmtsaveRawResultPage.clearParameters();

			PreparedStatement PStmtsaveRawResultPage = getConnection().prepareStatement(saveRawResultPageString);

			PStmtsaveRawResultPage.setInt(1, expId);
			PStmtsaveRawResultPage.setInt(2, idDatabase);
			PStmtsaveRawResultPage.setLong(3, qId);
			PStmtsaveRawResultPage.setInt(4, getNavigationTechniqueId(navigationTechnique));
			PStmtsaveRawResultPage.setInt(5, page);

			PStmtsaveRawResultPage.execute();

			PStmtsaveRawResultPage.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean hasProcessedQuery(int expId, int idDatabase, TextQuery texQuery, String navigationTechnique) {

		return hasReachedPage(expId, idDatabase, texQuery, navigationTechnique, 0);

	};

	private boolean hasReachedPage(int expId, int idDatabase,
			TextQuery texQuery, String navigationTechnique, int page) {

		boolean hep = false;

		long qId = getTextQuery(texQuery);

		try {

			//			if (PStmthasReachedPage == null){
			//				PStmthasReachedPage = getConnection().prepareStatement(hasReachedPageString);
			//			}else
			//				PStmthasReachedPage.clearParameters();

			PreparedStatement PStmthasReachedPage = getConnection().prepareStatement(hasReachedPageString);

			PStmthasReachedPage.setInt(1, expId);
			PStmthasReachedPage.setInt(2, idDatabase);
			PStmthasReachedPage.setLong(3, qId);
			PStmthasReachedPage.setInt(4, getNavigationTechniqueId(navigationTechnique));
			PStmthasReachedPage.setInt(5, page);


			ResultSet RShasReachedPage = PStmthasReachedPage.executeQuery();

			while (RShasReachedPage.next()) {

				hep = true;

			}

			RShasReachedPage.close();
			PStmthasReachedPage.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return hep;



	}

	@Override
	public void saveExtractedResultPage(int experimentId, int idDatabase,
			TextQuery query, String extractionTechnique,
			String navigationTechnique, int resultPage){

		long qId = getTextQuery(query);

		try {

			//			if (PStmtsaveExtractedResultPage == null){
			//				PStmtsaveExtractedResultPage = getConnection().prepareStatement(saveExtractedResultPageString);
			//			}else
			//				PStmtsaveExtractedResultPage.clearParameters();

			PreparedStatement PStmtsaveExtractedResultPage = getConnection().prepareStatement(saveExtractedResultPageString);

			PStmtsaveExtractedResultPage.setInt(1, experimentId);
			PStmtsaveExtractedResultPage.setInt(2, idDatabase);
			PStmtsaveExtractedResultPage.setLong(3, qId);
			PStmtsaveExtractedResultPage.setInt(4, getExtractionTechniqueId(extractionTechnique));
			PStmtsaveExtractedResultPage.setInt(5, getNavigationTechniqueId(navigationTechnique));
			PStmtsaveExtractedResultPage.setInt(6, resultPage);

			PStmtsaveExtractedResultPage.execute();

			PStmtsaveExtractedResultPage.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean hasExtractedPage(int expId, int idDatabase,
			TextQuery texQuery, String extractionTechnique,
			String navigationTechnique, int index) {

		boolean hep = false;

		long qId = getTextQuery(texQuery);

		try {

			//			if (PStmthasExtractedPage == null){
			//				PStmthasExtractedPage = getConnection().prepareStatement(hasExtractedPageString);
			//			}else
			//				PStmthasExtractedPage.clearParameters();

			PreparedStatement PStmthasExtractedPage = getConnection().prepareStatement(hasExtractedPageString);

			PStmthasExtractedPage.setInt(1, expId);
			PStmthasExtractedPage.setInt(2, idDatabase);
			PStmthasExtractedPage.setLong(3, qId);
			PStmthasExtractedPage.setInt(4, getExtractionTechniqueId(extractionTechnique));
			PStmthasExtractedPage.setInt(5, getNavigationTechniqueId(navigationTechnique));
			PStmthasExtractedPage.setInt(6, index);


			ResultSet RShasExtractedPage = PStmthasExtractedPage.executeQuery();

			while (RShasExtractedPage.next()) {

				hep = true;

			}

			RShasExtractedPage.close();

			PStmthasExtractedPage.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return hep;


	}

	@Override
	public int getExtractedResults(int experimentId, int idDatabase,
			TextQuery query, String extractionTechnique,
			String navigationTechnique, String resultExtraction, int resultPage) {

		int res = -1;

		long qId = getTextQuery(query);

		try {

			//			if (PStmtgetExtractedResults == null){
			//				PStmtgetExtractedResults = getConnection().prepareStatement(getExtractedResultsString);
			//			}else
			//				PStmtgetExtractedResults.clearParameters();

			PreparedStatement PStmtgetExtractedResults = getConnection().prepareStatement(getExtractedResultsString);

			PStmtgetExtractedResults.setInt(1, experimentId);
			PStmtgetExtractedResults.setInt(2, idDatabase);
			PStmtgetExtractedResults.setLong(3, qId);
			PStmtgetExtractedResults.setLong(4, resultPage);
			PStmtgetExtractedResults.setInt(5, getExtractionTechniqueId(extractionTechnique));
			PStmtgetExtractedResults.setInt(6, getNavigationTechniqueId(navigationTechnique));
			PStmtgetExtractedResults.setInt(7, getResultExtractionTechniqueId(resultExtraction));


			ResultSet RSgetExtractedResults = PStmtgetExtractedResults.executeQuery();

			while (RSgetExtractedResults.next()) {

				res = RSgetExtractedResults.getInt(1);

			}

			RSgetExtractedResults.close();
			PStmtgetExtractedResults.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;

	}

	@Override
	public List<Document> getQueryResults(int experimentId, int idDatabase,
			TextQuery query, String extractionTechnique,
			String navigationTechnique, String resultTechnique, int resultPage) {

		List<Document> res = new ArrayList<Document>();

		long qId = getTextQuery(query);

		try {

			//			if (PStmtgetQueryResults == null){
			//				PStmtgetQueryResults = getConnection().prepareStatement(getQueryResultsString);
			//			}else
			//				PStmtgetQueryResults.clearParameters();

			PreparedStatement PStmtgetQueryResults = getConnection().prepareStatement(getQueryResultsString);

			PStmtgetQueryResults.setInt(1, experimentId);
			PStmtgetQueryResults.setInt(2, idDatabase);
			PStmtgetQueryResults.setLong(3, qId);
			PStmtgetQueryResults.setLong(4, resultPage);
			PStmtgetQueryResults.setInt(5, getExtractionTechniqueId(extractionTechnique));
			PStmtgetQueryResults.setInt(6, getNavigationTechniqueId(navigationTechnique));
			PStmtgetQueryResults.setInt(7, getResultExtractionTechniqueId(resultTechnique));


			ResultSet RSgetQueryResults = PStmtgetQueryResults.executeQuery();

			while (RSgetQueryResults.next()) {

				res.add(new Document(getDatabase(idDatabase),RSgetQueryResults.getLong(1)));

			}

			RSgetQueryResults.close();
			PStmtgetQueryResults.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;


	}

	@Override
	public Integer getProcessedPages(int experimentId, int idDatabase, TextQuery query,
			String navigationHandler) {

		int prop = 0;

		long qId = getTextQuery(query);

		try {

			//			if (PStmtgetProcessedPages == null){
			//				PStmtgetProcessedPages = getConnection().prepareStatement(getProcessedPagesString);
			//			}else
			//				PStmtgetProcessedPages.clearParameters();

			PreparedStatement PStmtgetProcessedPages = getConnection().prepareStatement(getProcessedPagesString);

			PStmtgetProcessedPages.setInt(1, experimentId);
			PStmtgetProcessedPages.setInt(2, idDatabase);
			PStmtgetProcessedPages.setLong(3, qId);
			PStmtgetProcessedPages.setInt(4, getNavigationTechniqueId(navigationHandler));


			ResultSet RSgetProcessedPages = PStmtgetProcessedPages.executeQuery();

			while (RSgetProcessedPages.next()) {

				prop = RSgetProcessedPages.getInt(1);


			}

			RSgetProcessedPages.close();

			PStmtgetProcessedPages.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return prop;

	}

	@Override
	public void saveExtractedResult(int experimentId, Document document,
			String extractionTechnique, String navigationTechnique,
			String resultExtraction, TextQuery query, 
			int resultPage, int resultIndex) {

		Long qId = getTextQuery(query);

		try {

			//			if (PStmtsaveExtractedResult == null){
			//				PStmtsaveExtractedResult = getConnection().prepareStatement(saveExtractedResultString);
			//			}else
			//				PStmtsaveExtractedResult.clearParameters();

			PreparedStatement PStmtsaveExtractedResult = getConnection().prepareStatement(saveExtractedResultString);

			PStmtsaveExtractedResult.setInt(1, experimentId);
			PStmtsaveExtractedResult.setInt(2, document.getDatabase().getId());
			PStmtsaveExtractedResult.setLong(3, qId);
			PStmtsaveExtractedResult.setLong(4, document.getId());
			PStmtsaveExtractedResult.setInt(5, resultPage);
			PStmtsaveExtractedResult.setInt(6, resultIndex);
			PStmtsaveExtractedResult.setInt(7, getExtractionTechniqueId(extractionTechnique));
			PStmtsaveExtractedResult.setInt(8, getNavigationTechniqueId(navigationTechnique));
			PStmtsaveExtractedResult.setInt(9, getResultExtractionTechniqueId(resultExtraction));

			PStmtsaveExtractedResult.execute();

			PStmtsaveExtractedResult.close();

		} catch (SQLException e) {
			//XXX should not happen...
		}

	}

	private int getResultExtractionTechniqueId(String resultExtraction) {

		return getResultExtractionTechniqueTable().get(resultExtraction);

	}

	private Map<String,Integer> getResultExtractionTechniqueTable() {

		synchronized (resultExtractionTechniqueTable){

			if (resultExtractionTechniqueTable.isEmpty()){

				try {

					Statement StmtgetResultExtractionTechniqueTable = getConnection().createStatement();

					ResultSet RSgetResultExtractionTechniqueTable = StmtgetResultExtractionTechniqueTable.executeQuery(
							"select idResultHandler, name from `AutomaticQueryGeneration`.`ResultHandler`");

					while (RSgetResultExtractionTechniqueTable.next()) {

						resultExtractionTechniqueTable.put(RSgetResultExtractionTechniqueTable.getString(2), RSgetResultExtractionTechniqueTable.getInt(1));

					}

					RSgetResultExtractionTechniqueTable.close();

					StmtgetResultExtractionTechniqueTable.close();

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		}

		return resultExtractionTechniqueTable;		

	}

	private int getNavigationTechniqueId(String navigationTechnique) {
		return getNavigationTechniqueTable().get(navigationTechnique);
	}

	private Map<String,Integer> getNavigationTechniqueTable() {

		synchronized (navigatonTechniqueTable){

			if (navigatonTechniqueTable.isEmpty()){

				try {

					Statement StmtgetNavigationTechniqueTable = getConnection().createStatement();

					ResultSet RSgetNavigationTechniqueTable = StmtgetNavigationTechniqueTable.executeQuery(
							"select idNavigationHandler,name from `AutomaticQueryGeneration`.`NavigationHandler`");

					while (RSgetNavigationTechniqueTable.next()) {

						navigatonTechniqueTable.put(RSgetNavigationTechniqueTable.getString(2), RSgetNavigationTechniqueTable.getInt(1));

					}

					RSgetNavigationTechniqueTable.close();

					StmtgetNavigationTechniqueTable.close();

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		}

		return navigatonTechniqueTable;

	}

	private int getExtractionTechniqueId(String extractionTechnique) {

		return getExtractionTechniqueTable().get(extractionTechnique);

	}

	private Map<String,Integer> getExtractionTechniqueTable() {

		synchronized (extractionTechniqueTable){

			if (extractionTechniqueTable.isEmpty()){

				try {

					Statement StmtgetExtractionTechniqueTable = getConnection().createStatement();

					ResultSet RSgetExtractionTechniqueTable = StmtgetExtractionTechniqueTable.executeQuery(
							"select idQueryResultPageHandler,name from `AutomaticQueryGeneration`.`QueryResultPageHandler`");

					while (RSgetExtractionTechniqueTable.next()) {

						extractionTechniqueTable.put(RSgetExtractionTechniqueTable.getString(2), RSgetExtractionTechniqueTable.getInt(1));

					}

					RSgetExtractionTechniqueTable.close();

					StmtgetExtractionTechniqueTable.close();

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		}

		return extractionTechniqueTable;

	}

	@Override
	public Long getTextQuery(TextQuery query) {

		Long qId = getTextQueryTable(false,query);

		if (qId == null){

			return writeTextQuery(query);

		}

		return qId;

	}

	private Long getTextQueryTable(boolean reload, TextQuery querying) {

		synchronized (textquerytable) {

			if (textquerytable.isEmpty() || reload){

				try {

					//					if (PStmtgetTextQueryTable == null){
					//						PStmtgetTextQueryTable = getConnection().prepareStatement(getTextQueryTableString);
					//					}else
					//						PStmtgetTextQueryTable.clearParameters();

					PreparedStatement PStmtgetTextQueryTable = getConnection().prepareStatement(getTextQueryTableString);

					PStmtgetTextQueryTable.setLong(1, maxIdQuery);

					ResultSet RSgetTextQueryTable = PStmtgetTextQueryTable.executeQuery();

					while (RSgetTextQueryTable.next()) {

						long qId = RSgetTextQueryTable.getLong(1);

						if (maxIdQuery  < qId)
							maxIdQuery = qId;

						String query = RSgetTextQueryTable.getString(2);

						TextQuery q = new TextQuery(query);

						textquerytable.put(q, qId);

					}

					RSgetTextQueryTable.close();

					PStmtgetTextQueryTable.close();

				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		}


		Long qId = textquerytable.get(querying);

		return qId;

	}

	@Override
	public void writeHostDatabase(int databaseId, String computer) {

		String todowriteHostDatabase = ("INSERT INTO `AutomaticQueryGeneration`.`HostDatabase` (`idDatabase`,`host`) VALUES (" + databaseId + ",'"+format(computer)+"');");

		performTransaction(todowriteHostDatabase);

	}

	@Override
	public void cleanHostDatabases() {

		performTransaction("delete from `HostDatabase`");

	}

	@Override
	public List<List<String>> loadNonProcessedQueriesforSample(int idExperiment, Database database, String NavigationTechnique, int relationConf){

		Map<Integer,List<String>> res = new HashMap<Integer, List<String>>();

		try {

			Statement StmtloadQueries = getConnection().createStatement();

			//Loads only the ones that are for the sample.

			ResultSet RSloadQueries = StmtloadQueries.executeQuery
					("SELECT idQuery,Text from `TextQuery` TQ join `TextQueryRelationConfiguration` TR on (TQ.idQuery = TR.idTextQuery) where idRelationConfiguration = "+relationConf+" order by idQuery");

			while (RSloadQueries.next()) {

				res.put(RSloadQueries.getInt(1),Arrays.asList(RSloadQueries.getString(2).split(" ")));

			}
			RSloadQueries.close();
			StmtloadQueries.close();

			//Want to remove tha ones that were already processed

			Set<Integer> processed = new HashSet<Integer>();

			StmtloadQueries = getConnection().createStatement();

			RSloadQueries = StmtloadQueries.executeQuery
					("SELECT idQuery from RawResultPage where idQuery IN (select idTextQuery from TextQueryRelationConfiguration) and idExperiment = " + idExperiment + " and idDatabase" +
							" = " + database.getId() + " and navigationTechnique = " + getNavigationTechniqueId(NavigationTechnique) + " and page = 0");

			while (RSloadQueries.next()) {

				processed.add(RSloadQueries.getInt(1));

			}

			RSloadQueries.close();
			StmtloadQueries.close();

			res.keySet().removeAll(processed);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<Integer> keys =new ArrayList<Integer>(res.keySet());

		Collections.sort(keys);

		List<List<String>> ret = new ArrayList<List<String>>(keys.size());

		for (int i = 0; i < keys.size(); i++) {

			ret.add(res.get(keys.get(i)));

		}

		return ret;

	}

	@Override
	public void saveQueryTime(int expId, int idDatabase, TextQuery texQuery,
			int page, long time) {

		long qId = getTextQuery(texQuery);

		try {

			//			if (PStmtinsertQueryTime == null){
			//				PStmtinsertQueryTime = getConnection().prepareStatement(insertQueryTimeString);
			//			}else
			//				PStmtinsertQueryTime.clearParameters();

			PreparedStatement PStmtinsertQueryTime = getConnection().prepareStatement(insertQueryTimeString);

			PStmtinsertQueryTime.setInt(1, expId);
			PStmtinsertQueryTime.setInt(2, idDatabase);
			PStmtinsertQueryTime.setLong(3, qId);
			PStmtinsertQueryTime.setInt(4, page);
			PStmtinsertQueryTime.setLong(5, time);

			PStmtinsertQueryTime.execute();

			PStmtinsertQueryTime.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void reportQueryingStatus(String computerName, int idDatabase,
			QueryStatusEnum status) {

		switch (status) {
		case ERROR:
			reportQueryingStatus(computerName, idDatabase, ERROR_VALUE);
			break;
		case RUNNING:
			reportQueryingStatus(computerName, idDatabase, RUNNING_VALUE);
			break;

		default:
			break;
		}

	}

	@Override
	public void reportQueryingStatus(String computerName, int idDatabase,
			int status) {

		try {

			//			if (PStmtreportQueryingStatus == null){
			//				PStmtreportQueryingStatus = getConnection().prepareStatement(reportStatus);
			//			}else
			//				PStmtreportQueryingStatus.clearParameters();

			PreparedStatement PStmtreportQueryingStatus = getConnection().prepareStatement(reportStatus);

			PStmtreportQueryingStatus.setInt(1, status);
			PStmtreportQueryingStatus.setInt(2, idDatabase);
			PStmtreportQueryingStatus.setString(3, computerName);

			PStmtreportQueryingStatus.execute();
			PStmtreportQueryingStatus.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isQueryAvailable(String computerName, int idDatabase) {

		boolean res = false;

		try {

			Statement StmtisQueryAvailable = getConnection().createStatement();

			ResultSet RSisQueryAvailable = StmtisQueryAvailable.executeQuery
					("SELECT * from `HostDatabase` where host = '" + format(computerName) + "' and idDatabase = " + idDatabase + " and queryStatus = 0");

			while (RSisQueryAvailable.next()) {

				res = true;

			}
			RSisQueryAvailable.close();
			StmtisQueryAvailable.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;

	}

	@Override
	public boolean isExperimentAvailable(int idExperiment, int idDatabase, String computerName) {

		try {

			//			if (PStmtisExperimentAvailable == null){
			//				PStmtisExperimentAvailable = getConnection().prepareStatement(isExperimentAvailableString);
			//			}else
			//				PStmtisExperimentAvailable.clearParameters();

			PreparedStatement PStmtisExperimentAvailable = getConnection().prepareStatement(isExperimentAvailableString);

			PStmtisExperimentAvailable.setInt(1, idDatabase);
			PStmtisExperimentAvailable.setInt(2, idExperiment);
			PStmtisExperimentAvailable.setString(3, computerName);

			PStmtisExperimentAvailable.execute();

			PStmtisExperimentAvailable.close();

		} catch (SQLException e) {
			return false; //duplicated key, someone has already written it.
		}

		return true;

	}

	@Override
	public void reportExperimentStatus(int idExperiment, int idDatabase, String computerName,
			ExperimentStatusEnum status) {

		reportExperimentStatus(idExperiment,idDatabase,computerName, getValue(status));

	}

	@Override
	public int getValue(ExperimentStatusEnum status) {

		switch (status) {
		case ERROR:
			return ERROR_VALUE;
		case RUNNING:
			return RUNNING_VALUE;
		case FINISHED:
			return FINISHED_VALUE;
		case PARTIALLY_FINISHED:
			return PARTIALLY_FINISHED;
		case INCONSISTENT:
			return INCONSISTENT;
		default:
			break;
		}

		return 0;

	}

	@Override
	public void reportExperimentStatus(int idExperiment, int idDatabase,String computerName,
			int status) {

		try {

			//			if (PStmtreportExperimentStatus == null){
			//				PStmtreportExperimentStatus = getConnection().prepareStatement(reportExperimentStatus);
			//			}else
			//				PStmtreportExperimentStatus.clearParameters();

			PreparedStatement PStmtreportExperimentStatus = getConnection().prepareStatement(reportExperimentStatus);

			PStmtreportExperimentStatus.setInt(1, status);
			PStmtreportExperimentStatus.setString(2,computerName);
			PStmtreportExperimentStatus.setInt(3, idDatabase);
			PStmtreportExperimentStatus.setInt(4, idExperiment);

			PStmtreportExperimentStatus.execute();

			PStmtreportExperimentStatus.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean hasExtractedEntities(int idDatabase, long idDocument, int idEntityType,
			int idInformationExtractionSystem, ContentExtractor ce) {

		boolean ret = false;

		try {

			//			if (PStmthasExtractedEntities == null){
			//				PStmthasExtractedEntities = getConnection().prepareStatement(hasExtractedEntitiesString);
			//			}else
			//				PStmthasExtractedEntities.clearParameters();

			PreparedStatement PStmthasExtractedEntities = getConnection().prepareStatement(hasExtractedEntitiesString);

			PStmthasExtractedEntities.setInt(1, idDatabase);
			PStmthasExtractedEntities.setLong(2, idDocument);
			PStmthasExtractedEntities.setInt(3, getContentExtractorId(ce));
			PStmthasExtractedEntities.setInt(4, idInformationExtractionSystem);
			PStmthasExtractedEntities.setInt(5, idEntityType); 


			ResultSet RShasExtractedEntities = PStmthasExtractedEntities.executeQuery();

			while (RShasExtractedEntities.next()) {

				ret = true;

			}

			RShasExtractedEntities.close();

			PStmthasExtractedEntities.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void saveEntity(int idDatabase, long idDocument, ContentExtractor ce,
			int informationExtractionSystem, int entityType, int start,
			int end, long time) {

		try {

			//			if (PStmtsaveEntity == null){
			//				PStmtsaveEntity = getConnection().prepareStatement(saveEntityString);
			//			}else
			//				PStmtsaveEntity.clearParameters();

			PreparedStatement PStmtsaveEntity = getConnection().prepareStatement(saveEntityString);

			PStmtsaveEntity.setInt(1, idDatabase);
			PStmtsaveEntity.setLong(2, idDocument);
			PStmtsaveEntity.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveEntity.setInt(4, informationExtractionSystem);
			PStmtsaveEntity.setInt(5, entityType);
			PStmtsaveEntity.setInt(6, start);
			PStmtsaveEntity.setInt(7, end);
			PStmtsaveEntity.setLong(8, time);

			PStmtsaveEntity.execute();

			PStmtsaveEntity.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<Long> getDocumentsInQueryResults(Database database, int idExperiment, int entity, int firstRes, int lastRes, boolean queries, boolean negative, boolean tuples) {

		Set<Long> res = new HashSet<Long>();



		List<Long> qIds = new ArrayList<Long>();

		if (queries){

			qIds.addAll(getQueriesInPositiveSampling(entity,idExperiment));

		}

		//plus all the ones that were added for the negative sampling and the seed tuples... (the tuple ones were already processed accordingly)

		if (negative){
			qIds.addAll(getQueriesInNegativeSampling(database,idExperiment));
		}

		if (tuples){
			qIds.addAll(getQueriesInTuples(database,idExperiment));
		}

		if (!qIds.isEmpty()){

			res.addAll(getDocumentsInQueries(idExperiment, qIds,database,firstRes,lastRes));

		}

		return res;

	}

	private Set<Long> getDocumentsInQueries(int idExperiment, List<Long> qIds, Database database,int firstRes, int lastRes) {

		Set<Long> res = new HashSet<Long>();

		try{

			Statement StmtgetDocumentsInQueryResults = getConnection().createStatement();

			ResultSet RSgetDocumentsInQueryResults = null;

			String in = generateIn(qIds);

			StmtgetDocumentsInQueryResults = getConnection().createStatement();
			RSgetDocumentsInQueryResults = StmtgetDocumentsInQueryResults.executeQuery
					("select distinct idDocument from `AutomaticQueryGeneration`.`QueryResults` where idExperiment = "+idExperiment+" and idDatabase = " + database.getId() + " and idDocument > -1 and " +
							"idQuery IN " + in + " and position >= " + firstRes + " and position < " + lastRes);

			while (RSgetDocumentsInQueryResults.next()) {

				res.add(RSgetDocumentsInQueryResults.getLong(1));

			}

			RSgetDocumentsInQueryResults.close();
			StmtgetDocumentsInQueryResults.close();

		} catch (SQLException e){
			e.printStackTrace();
		}

		return res;
	}

	private List<Long> getQueriesInPositiveSampling(int entity, int idExperiment) {

		List<Long> res = new ArrayList<Long>();

		try {



			Statement StmtgetQueriesInPositiveSampling = getConnection().createStatement();

			ResultSet RSgetQueriesInPositiveSampling = StmtgetQueriesInPositiveSampling.executeQuery
					("select distinct idTextQuery from TextQueryEntityType where " +
							" idEntityType = " + entity + " and idExperiment = " + idExperiment);

			while (RSgetQueriesInPositiveSampling.next()) {

				res.add(RSgetQueriesInPositiveSampling.getLong(1));

			}

			StmtgetQueriesInPositiveSampling.close();

			RSgetQueriesInPositiveSampling.close();
		} catch (SQLException e) {

			e.printStackTrace();

		}

		return res;

	}

	private List<Long> getQueriesInTuples(Database database, int idExperiment) {

		List<Long> ret = new ArrayList<Long>();

		try {

			Statement StmtgetQueriesInTuples = getConnection().createStatement();

			ResultSet RSgetQueriesInTuples = StmtgetQueriesInTuples.executeQuery
					("select idQuery from `AutomaticQueryGeneration`.`QueryForTuples` where idDatabase = " + database.getId() + " and idExperiment = " + idExperiment);

			while (RSgetQueriesInTuples.next()) {

				ret.add(RSgetQueriesInTuples.getLong(1));

			}

			RSgetQueriesInTuples.close();
			StmtgetQueriesInTuples.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private List<Long> getQueriesInNegativeSampling(Database database, int idExperiment) {

		List<Long> ret = new ArrayList<Long>();

		try {

			Statement StmtgetQueriesInNegativeSampling = getConnection().createStatement();

			ResultSet RSgetQueriesInNegativeSampling = StmtgetQueriesInNegativeSampling.executeQuery
					("select queryId from `AutomaticQueryGeneration`.`NegativeSampling` where idDatabase = " + database.getId() + " and idExperimentId = " + idExperiment);

			while (RSgetQueriesInNegativeSampling.next()) {

				ret.add(RSgetQueriesInNegativeSampling.getLong(1));

			}

			RSgetQueriesInNegativeSampling.close();
			StmtgetQueriesInNegativeSampling.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public Map<String, Integer> getEntityTypeTable() {

		Map<String, Integer> res = new HashMap<String, Integer>();

		try {

			Statement StmtgetEntityTypeTable = getConnection().createStatement();

			ResultSet RSgetEntityTypeTable = StmtgetEntityTypeTable.executeQuery
					("select idEntityType, description from `AutomaticQueryGeneration`.`EntityType`");

			while (RSgetEntityTypeTable.next()) {

				res.put(RSgetEntityTypeTable.getString(2).toUpperCase(),RSgetEntityTypeTable.getInt(1));

			}

			RSgetEntityTypeTable.close();
			StmtgetEntityTypeTable.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;

	}

	@Override
	public void writeTextQueryEntity(long query, int idEntityType) {

		try {

			//			if (PStmtwriteTextQueryEntity == null){
			//				PStmtwriteTextQueryEntity = getConnection().prepareStatement(writeTextQueryEntityString);
			//			}else
			//				PStmtwriteTextQueryEntity.clearParameters();

			PreparedStatement PStmtwriteTextQueryEntity = getConnection().prepareStatement(writeTextQueryEntityString);

			PStmtwriteTextQueryEntity.setLong(1, query);
			PStmtwriteTextQueryEntity.setInt(2, idEntityType);

			PStmtwriteTextQueryEntity.execute();

			PStmtwriteTextQueryEntity.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Map<String, Integer> getRelationshipTable() {

		synchronized (relationshipTable){

			if (relationshipTable.isEmpty()){

				try {

					StmtgetRelationTypeTable = getConnection().createStatement();

					RSgetRelationTypeTable = StmtgetRelationTypeTable.executeQuery
							("select idRelationshipType, description from `AutomaticQueryGeneration`.`RelationshipType`");

					while (RSgetRelationTypeTable.next()) {

						relationshipTable.put(RSgetRelationTypeTable.getString(2),RSgetRelationTypeTable.getInt(1));

					}

					RSgetRelationTypeTable.close();
					StmtgetRelationTypeTable.close();

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return relationshipTable;

	}

	@Override
	public void writeTextQueryRelation(long query, int idRelationshipType) {

		try {

			//			if (PStmtwriteTextQueryRelation == null){
			//				PStmtwriteTextQueryRelation = getConnection().prepareStatement(writeTextQueryRelationString);
			//			}else
			//				PStmtwriteTextQueryRelation.clearParameters();

			PreparedStatement PStmtwriteTextQueryRelation = getConnection().prepareStatement(writeTextQueryRelationConfString);

			PStmtwriteTextQueryRelation.setLong(1, query);
			PStmtwriteTextQueryRelation.setInt(2, idRelationshipType);

			PStmtwriteTextQueryRelation.execute();
			PStmtwriteTextQueryRelation.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void saveExtractedDocument(int idDatabase, long idDocument,
			ContentExtractor ce, int idInformationExtractor, int idEntity, long time) {

		try {

			//			if (PStmtsaveExtractedEntity == null){
			//				PStmtsaveExtractedEntity = getConnection().prepareStatement(saveExtractedEntityString);
			//			}else
			//				PStmtsaveExtractedEntity.clearParameters();

			PreparedStatement PStmtsaveExtractedEntity = getConnection().prepareStatement(saveExtractedEntityString);

			PStmtsaveExtractedEntity.setInt(1, idDatabase);
			PStmtsaveExtractedEntity.setLong(2, idDocument);
			PStmtsaveExtractedEntity.setInt(3, getContentExtractorId(ce));
			PStmtsaveExtractedEntity.setInt(4, idInformationExtractor);
			PStmtsaveExtractedEntity.setInt(5, idEntity);
			PStmtsaveExtractedEntity.setLong(6, time);

			PStmtsaveExtractedEntity.execute();

			PStmtsaveExtractedEntity.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void insertExperimentOnDatabase(int idDatabase, int idExperiment) {

		try {

			//			if (PStmtinsertExperimentOnDatabase == null){
			//				PStmtinsertExperimentOnDatabase = getConnection().prepareStatement(insertExperimentOnDatabaseString);
			//			}else
			//				PStmtinsertExperimentOnDatabase.clearParameters();

			PreparedStatement PStmtinsertExperimentOnDatabase = getConnection().prepareStatement(insertExperimentOnDatabaseString);

			PStmtinsertExperimentOnDatabase.setInt(1, idDatabase);
			PStmtinsertExperimentOnDatabase.setInt(2, idExperiment);

			PStmtinsertExperimentOnDatabase.execute();

			PStmtinsertExperimentOnDatabase.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public boolean hasGeneratedCandidateSentence(int idDatabase,
			long idDocument, int relationConf, ContentExtractor ce) {

		boolean ret = false;

		try {

			//			if (PStmthasGeneratedCandidateSentence == null){
			//				PStmthasGeneratedCandidateSentence = getConnection().prepareStatement(hasGeneratedCandidateSentenceString);
			//			}else
			//				PStmthasGeneratedCandidateSentence.clearParameters();

			PreparedStatement PStmthasGeneratedCandidateSentence = getConnection().prepareStatement(hasGeneratedCandidateSentenceString);

			PStmthasGeneratedCandidateSentence.setInt(1, idDatabase);
			PStmthasGeneratedCandidateSentence.setLong(2, idDocument);
			PStmthasGeneratedCandidateSentence.setInt(3, getContentExtractorId(ce));
			PStmthasGeneratedCandidateSentence.setInt(4, relationConf);


			ResultSet RShasGeneratedCandidateSentence = PStmthasGeneratedCandidateSentence.executeQuery();

			while (RShasGeneratedCandidateSentence.next()) {

				ret = true;

			}

			RShasGeneratedCandidateSentence.close();

			PStmthasGeneratedCandidateSentence.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public void saveGeneratedCandidateSentence(int idDatabase, long idDocument,
			int relationConfigurationId, ContentExtractor ce) {

		try {

			//			if (PStmtsaveGeneratedCandidateSentence == null){
			//				PStmtsaveGeneratedCandidateSentence = getConnection().prepareStatement(saveGeneratedCandidateSentenceString);
			//			}else
			//				PStmtsaveGeneratedCandidateSentence.clearParameters();

			PreparedStatement PStmtsaveGeneratedCandidateSentence = getConnection().prepareStatement(saveGeneratedCandidateSentenceString);

			PStmtsaveGeneratedCandidateSentence.setInt(1, idDatabase);
			PStmtsaveGeneratedCandidateSentence.setLong(2, idDocument);
			PStmtsaveGeneratedCandidateSentence.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveGeneratedCandidateSentence.setInt(4, relationConfigurationId);

			PStmtsaveGeneratedCandidateSentence.execute();
			PStmtsaveGeneratedCandidateSentence.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public synchronized void saveCandidateSentenceGeneration(int idDatabase,
			long idDocument, ContentExtractor ce, int relationConfigurationId, String file, long time) {

		try {

			//			if (PStmtsaveCandidateSentenceGeneration == null){
			//				PStmtsaveCandidateSentenceGeneration = getConnection().prepareStatement(saveCandidateSentenceGenerationString);
			//			}else
			//				PStmtsaveCandidateSentenceGeneration.clearParameters();

			PreparedStatement PStmtsaveCandidateSentenceGeneration = getConnection().prepareStatement(saveCandidateSentenceGenerationString);

			PStmtsaveCandidateSentenceGeneration.setInt(1, idDatabase);
			PStmtsaveCandidateSentenceGeneration.setLong(2, idDocument);
			PStmtsaveCandidateSentenceGeneration.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveCandidateSentenceGeneration.setInt(4, relationConfigurationId);
			PStmtsaveCandidateSentenceGeneration.setString(5, format(file));
			PStmtsaveCandidateSentenceGeneration.setLong(6, time);

			PStmtsaveCandidateSentenceGeneration.execute();

			PStmtsaveCandidateSentenceGeneration.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Set<Long> getDocumentsCandidateSentenceForSplits(
			Database database, int[][] entities, List<Long> processedDocuments, ContentExtractor ce, int firstRes, int lastRes, boolean queries, boolean negative, boolean tuples) {

		int ceId = getContentExtractorId(ce);

		System.out.println("First Cand ...");

		Set<Long> docs = loadDocsCandSent(database,entities[entities.length-1],processedDocuments, ceId,false);

		for (int i = entities.length-2; i >= 0; i--) {

			System.out.println("Second Cand...");

			docs = loadDocsCandSent(database,entities[i],new ArrayList<Long>(docs), ceId,true);

			System.out.println("Done...");

		}

		//Then, the ones I'm looking for: 

		//		int[] ents = new int[entities.length];
		//				
		//		for (int i = 0; i < ents.length; i++) {
		//			ents[i] = entities[i][1]; //The firstone is the extractor
		//		}
		//		
		//		docs.retainAll(getDocumentsInQueryResults(database, ents, firstRes, lastRes, queries, negative, tuples));
		//	
		return docs;
	}


	private Set<Long> loadDocsCandSent(
			Database database, int[] entityInfo, List<Long> docs, int ceId, boolean in) {

		Set<Long> ret = new HashSet<Long>();

		String inEnt = generateIn(entityInfo,ceId);

		String docRestr = "";

		if (!docs.isEmpty()){
			if (in)
				docRestr = " and idDocument IN "+generateIn(docs);
			else
				docRestr = " and idDocument NOT IN "+generateIn(docs);
		}else{
			if (in) //I'm trying to find documents in the empty set ...
				return ret;
		}

		String query = "select idDocument from Entity where idDatabase = "+database.getId()+" and (idInformationExtractionSystem , idContentExtractionSystem , idEntityType ) IN (" + inEnt +")"+ docRestr;

		try {

			Statement StmtgetEntitiesMap = getConnection().createStatement();

			ResultSet RSgetEntitiesMap = StmtgetEntitiesMap.executeQuery
					(query);

			while (RSgetEntitiesMap.next()) {

				long idDocument = RSgetEntitiesMap.getLong(1);

				ret.add(idDocument);

			}

			RSgetEntitiesMap.close();
			StmtgetEntitiesMap.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public Map<Long, Map<Integer, List<long[]>>> getEntitiesMap(
			Database database, int[][] entities, List<Long> processedDocuments, ContentExtractor ce, boolean in) {

		int ceId = getContentExtractorId(ce);

		if (in){

			return loadEntitiesMap(database,entities,processedDocuments, ceId,true);

		}

		Map<Long, Map<Integer, List<long[]>>> cs = loadEntitiesMap(database,new int[][]{entities[entities.length-1]},processedDocuments, ceId,false);

		for (int i = entities.length-2; i >= 0; i--) {

			Map<Long, Map<Integer, List<long[]>>> aux = loadEntitiesMap(database,new int[][]{entities[i]},new ArrayList<Long>(cs.keySet()), ceId,true);

			cs.keySet().retainAll(aux.keySet());

			for (Entry<Long,Map<Integer, List<long[]>>> idDocument : aux.entrySet()) {

				for (Entry<Integer, List<long[]>> idEntityType : idDocument.getValue().entrySet()) {

					getEntities(cs,idDocument.getKey(),idEntityType.getKey()).addAll(idEntityType.getValue());

				}

			}


		}

		return cs;

		//			String query = "select B.idDocument, B.idEntityType, B.idEntity, B.start, B.end from Entity B where B.idDatabase = "
		//					+database.getId()+" and B.idDocument IN (select A.idDocument from (select idDocument, idEntityType from Entity where "+docRestr+" idDatabase = " 
		//					+ database.getId() + " and (idInformationExtractionSystem,idEntityType) IN "+inEnt+" group by idDocument,idEntityType) as A group by " +
		//					"A.idDocument having count(A.idDocument) = " + entities.length + ") and (B.idInformationExtractionSystem, B.idEntityType) IN " + inEnt + " order by B.idDocument";

		//			String query = "select idDocument, idEntityType, idEntity, start, end from Entity where idDatabase = "+database.getId()+" and " +
		//					"(idDatabase,idDocument) IN (select A.idDatabase,A.idDocument from (select distinct idDatabase,idDocument,idEntityType " +
		//					"from Entity where idDatabase = "+database.getId()+" and (idInformationExtractionSystem,idContentExtractionSystem,idEntityType) " +
		//							"IN "+inEnt+") as A group by A.idDatabase,A.idDocument having count(*) = "+entities.length+") and " + docRestr + 
		//									"(idInformationExtractionSystem,idContentExtractionSystem,idEntityType) IN " + inEnt;




	}



	private Map<Long, Map<Integer, List<long[]>>> loadEntitiesMap(
			Database database, int[][] entityInfo, List<Long> docs, int ceId, boolean in) {

		Map<Long, Map<Integer, List<long[]>>> ret = new HashMap<Long, Map<Integer,List<long[]>>>();

		String inEnt = generateIn(entityInfo,ceId);

		String docRestr = "";

		if (!docs.isEmpty()){
			if (in)
				docRestr = " and idDocument IN "+generateIn(docs);
			else
				docRestr = " and idDocument NOT IN "+generateIn(docs);
		}

		String query = "select idDocument, idEntityType, idEntity, start, end from Entity where idDatabase = "+database.getId()+" and (idInformationExtractionSystem , idContentExtractionSystem , idEntityType ) IN " + inEnt + docRestr;

		try {

			Statement StmtgetEntitiesMap = getConnection().createStatement();

			ResultSet RSgetEntitiesMap = StmtgetEntitiesMap.executeQuery
					(query);

			while (RSgetEntitiesMap.next()) {

				long idDocument = RSgetEntitiesMap.getLong(1);
				int idEntityType = RSgetEntitiesMap.getInt(2);
				long idEntity = RSgetEntitiesMap.getLong(3);
				int start = RSgetEntitiesMap.getInt(4);
				int end = RSgetEntitiesMap.getInt(5);

				getEntities(ret,idDocument,idEntityType).add(new long[]{idEntity,start,end});

			}

			RSgetEntitiesMap.close();
			StmtgetEntitiesMap.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private List<long[]> getEntities(
			Map<Long, Map<Integer, List<long[]>>> map, long idDocument,
			int idEntityType) {

		List<long[]> ret = getMap(map,idDocument).get(idEntityType);

		if (ret == null){
			ret = new ArrayList<long[]>();
			getMap(map,idDocument).put(idEntityType,ret);
		}

		return ret;

	}

	private Map<Integer, List<long[]>> getMap(
			Map<Long, Map<Integer, List<long[]>>> map, long idDocument) {
		Map<Integer,List<long[]>> ret = map.get(idDocument);

		if (ret == null){

			ret = new HashMap<Integer, List<long[]>>();

			map.put(idDocument, ret);

		}

		return ret;
	}

	private String generateIn(int[][] values, int ceId) {

		String ret = "(" + generateIn(values[0],ceId);

		for (int i = 1; i < values.length; i++) {

			ret += "," + generateIn(values[i],ceId);

		}

		return ret + ")";

	}

	private String generateIn(int[] values, int ceId) {

		String ret = "(" + values[0] + "," + ceId;

		for (int i = 1; i < values.length; i++) {

			ret += "," + values[i];

		}

		return ret + ")";

	}

	@Override
	public void cleanExperiments() {

		String todo = "Delete from ExperimentStatus";

		performTransaction(todo);

	}

	@Override
	public Set<Long> getExtractedDocuments(Database database, int entityType,
			int extractor, ContentExtractor contentExtractor) {

		Set<Long> ret = new HashSet<Long>();

		try {

			Statement StmtgetExtractedDocuments = getConnection().createStatement();

			ResultSet RSgetExtractedDocuments = StmtgetExtractedDocuments.executeQuery
					("select distinct idDocument from ExtractedEntity where idDatabase = "
							+ database.getId()+ " and idEntityType = "+ entityType + " and idInformationExtractionSystem = " + extractor + " and " +
							"idContentExtractionSystem = " + getContentExtractorId(contentExtractor)); 

			while (RSgetExtractedDocuments.next()) {

				ret.add(RSgetExtractedDocuments.getLong(1));

			}

			RSgetExtractedDocuments.close();
			StmtgetExtractedDocuments.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public Map<Long, Pair<Integer,String>> getCandidateSentencesMap(int idExperiment, Database database,
			int relationConf, ContentExtractor ce, int informationExtractionSystem, boolean queries,
			boolean negative, boolean tuples, boolean all, int firstRes, int lastRes) {

		Map<Long, Pair<Integer,String>> ret = new HashMap<Long, Pair<Integer,String>>();

		try {

			PreparedStatement PStmtgetCandidateSentencesMap = getConnection().prepareStatement(getAllCandidateSentencesMapString);

			PStmtgetCandidateSentencesMap.setInt(1, database.getId());
			PStmtgetCandidateSentencesMap.setLong(2, relationConf);
			PStmtgetCandidateSentencesMap.setInt(3, getContentExtractorId(ce));

			ResultSet RSgetCandidateSentencesMap = PStmtgetCandidateSentencesMap.executeQuery();

			while (RSgetCandidateSentencesMap.next()) {

				ret.put(RSgetCandidateSentencesMap.getLong(1), new Pair<Integer,String>(RSgetCandidateSentencesMap.getInt(3),RSgetCandidateSentencesMap.getString(2)));

			}

			RSgetCandidateSentencesMap.close();
			PStmtgetCandidateSentencesMap.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (!all){

			Set<Long> docs = getDocumentsInQueryResultsForInformationExtractionSystem(idExperiment, database, relationConf, informationExtractionSystem, firstRes, lastRes, queries, negative, tuples);

			ret.keySet().retainAll(docs);


		}

		return ret;


	}

	private Set<Long> getDocumentsInQueryResultsForInformationExtractionSystem(
			int idExperiment, Database database, int relationConf, int informationExtractionSystem, int firstRes,
			int lastRes, boolean queries, boolean negative, boolean tuples) {

		List<Long> qIds = new ArrayList<Long>();

		Set<Long> res = new HashSet<Long>();

		if (queries){

			qIds.addAll(getQueriesInPositiveSamplingForIESystem(relationConf, informationExtractionSystem));

		}

		//plus all the ones that were added for the negative sampling and the seed tuples... (the tuple ones were already processed accordingly)

		if (negative){
			qIds.addAll(getQueriesInNegativeSamplingForIESystem(database));
		}

		if (tuples){
			qIds.addAll(getQueriesInTuplesForIESystem(database));
		}

		if (!qIds.isEmpty()){

			res.addAll(getDocumentsInQueries(idExperiment, qIds,database,firstRes,lastRes));

		}

		return res;

		//		if (queries){
		//			
		//			try {
		//	
		//				PreparedStatement PStmtgetCandidateSentencesMap = getConnection().prepareStatement(getCandidateSentencesMapString);
		//	
		//				PStmtgetCandidateSentencesMap.setInt(1, database.getId());
		//				PStmtgetCandidateSentencesMap.setLong(2, relationConf);
		//				PStmtgetCandidateSentencesMap.setInt(3, getContentExtractorId(ce));
		//				PStmtgetCandidateSentencesMap.setInt(4, informationExtractionSystem);
		//				PStmtgetCandidateSentencesMap.setLong(5, relationConf); //Have to send it again to make the query more efficient
		//	
		//				ResultSet RSgetCandidateSentencesMap = PStmtgetCandidateSentencesMap.executeQuery();
		//	
		//				while (RSgetCandidateSentencesMap.next()) {
		//	
		//					ret.put(RSgetCandidateSentencesMap.getLong(1), new Pair<Integer,String>(RSgetCandidateSentencesMap.getInt(3),RSgetCandidateSentencesMap.getString(2)));
		//	
		//				}
		//	
		//				RSgetCandidateSentencesMap.close();
		//	
		//			} catch (SQLException e) {
		//				e.printStackTrace();
		//			}
		//
		//		}
		//		
		//		if (negative){
		//			
		//			try {
		//				
		//				PreparedStatement PStmtgetCandidateSentencesNegativeMap = getConnection().prepareStatement(getCandidateSentencesNegativeMapString);
		//	
		//				PStmtgetCandidateSentencesNegativeMap.setInt(1, database.getId());
		//				PStmtgetCandidateSentencesNegativeMap.setLong(2, relationConf);
		//				PStmtgetCandidateSentencesNegativeMap.setInt(3, getContentExtractorId(ce));
		//				PStmtgetCandidateSentencesNegativeMap.setInt(4, database.getId()); //Have to send it again to make the query more efficient
		//	
		//				ResultSet RSgetCandidateSentencesNegativeMap = PStmtgetCandidateSentencesNegativeMap.executeQuery();
		//	
		//				while (RSgetCandidateSentencesNegativeMap.next()) {
		//	
		//					ret.put(RSgetCandidateSentencesNegativeMap.getLong(1), new Pair<Integer,String>(RSgetCandidateSentencesNegativeMap.getInt(3),RSgetCandidateSentencesNegativeMap.getString(2)));
		//	
		//				}
		//	
		//				RSgetCandidateSentencesNegativeMap.close();
		//	
		//			} catch (SQLException e) {
		//				e.printStackTrace();
		//			}
		//			
		//		}
		//		
		//		if (tuples){
		//			
		//			try {
		//				
		//				PreparedStatement PStmtgetCandidateSentencesTuplesMap = getConnection().prepareStatement(getCandidateSentencesTuplesMapString);
		//	
		//				PStmtgetCandidateSentencesTuplesMap.setInt(1, database.getId());
		//				PStmtgetCandidateSentencesTuplesMap.setLong(2, relationConf);
		//				PStmtgetCandidateSentencesTuplesMap.setInt(3, getContentExtractorId(ce));
		//				PStmtgetCandidateSentencesTuplesMap.setInt(4, database.getId()); //Have to send it again to make the query more efficient
		//	
		//				ResultSet RSgetCandidateSentencesTuplesMap = PStmtgetCandidateSentencesTuplesMap.executeQuery();
		//	
		//				while (RSgetCandidateSentencesTuplesMap.next()) {
		//	
		//					ret.put(RSgetCandidateSentencesTuplesMap.getLong(1), new Pair<Integer,String>(RSgetCandidateSentencesTuplesMap.getInt(3),RSgetCandidateSentencesTuplesMap.getString(2)));
		//	
		//				}
		//	
		//				RSgetCandidateSentencesTuplesMap.close();
		//	
		//			} catch (SQLException e) {
		//				e.printStackTrace();
		//			}
		//			
		//		}


	}

	private List<Long> getQueriesInTuplesForIESystem(
			Database database) {

		List<Long> ret = new ArrayList<Long>();

		try {

			PreparedStatement PStmtgetQueriesInTuplesForIEystem = getConnection().prepareStatement(getForTupleQueriesIESystem);

			PStmtgetQueriesInTuplesForIEystem.setInt(1, database.getId());

			ResultSet RSgetQueriesInTuplesForIESystem = PStmtgetQueriesInTuplesForIEystem.executeQuery();

			while (RSgetQueriesInTuplesForIESystem.next()) {

				ret.add(RSgetQueriesInTuplesForIESystem.getLong(1));

			}

			RSgetQueriesInTuplesForIESystem.close();
			PStmtgetQueriesInTuplesForIEystem.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private Collection<? extends Long> getQueriesInNegativeSamplingForIESystem(
			Database database) {

		List<Long> ret = new ArrayList<Long>();

		try {

			PreparedStatement PStmtgetNegativeSamplingForIESystem = getConnection().prepareStatement(getNegativeQueriesIESystem);

			PStmtgetNegativeSamplingForIESystem.setInt(1, database.getId());

			ResultSet RSgetNegativeSampling = PStmtgetNegativeSamplingForIESystem.executeQuery();

			while (RSgetNegativeSampling.next()) {

				ret.add(RSgetNegativeSampling.getLong(1));

			}

			RSgetNegativeSampling.close();

			PStmtgetNegativeSamplingForIESystem.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private Collection<? extends Long> getQueriesInPositiveSamplingForIESystem(
			int relationConf, int informationExtractionSystem) {

		List<Long> ret = new ArrayList<Long>();

		try {

			PreparedStatement PStmtgetQueriesInTuplesForIESystem = getConnection().prepareStatement(getQueriesIESystem);

			PStmtgetQueriesInTuplesForIESystem.setInt(1, informationExtractionSystem);
			PStmtgetQueriesInTuplesForIESystem.setInt(2, relationConf);

			ResultSet RSgetQueriesInTuplesForIESystem = PStmtgetQueriesInTuplesForIESystem.executeQuery();

			while (RSgetQueriesInTuplesForIESystem.next()) {

				ret.add(RSgetQueriesInTuplesForIESystem.getLong(1));

			}

			RSgetQueriesInTuplesForIESystem.close();
			PStmtgetQueriesInTuplesForIESystem.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public Set<Long> getProcessedCandidateSentences(Database database,
			int relationConf, ContentExtractor ce,
			int informationExtractionSystem) {

		Set<Long> ret = new HashSet<Long>();

		try {

			//			if (PStmtgetProcessedCandidateSentences == null){
			//				PStmtgetProcessedCandidateSentences = getConnection().prepareStatement(getProcessedCandidateSentencesString);
			//			}else
			//				PStmtgetProcessedCandidateSentences.clearParameters();

			PreparedStatement PStmtgetProcessedCandidateSentences = getConnection().prepareStatement(getProcessedCandidateSentencesString);

			PStmtgetProcessedCandidateSentences.setInt(1, database.getId());
			PStmtgetProcessedCandidateSentences.setLong(2, relationConf);
			PStmtgetProcessedCandidateSentences.setInt(3, getContentExtractorId(ce));
			PStmtgetProcessedCandidateSentences.setInt(4, informationExtractionSystem);

			ResultSet RSgetProcessedCandidateSentences = PStmtgetProcessedCandidateSentences.executeQuery();

			while (RSgetProcessedCandidateSentences.next()) {

				ret.add(RSgetProcessedCandidateSentences.getLong(1));

			}

			RSgetProcessedCandidateSentences.close();
			PStmtgetProcessedCandidateSentences.close();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void saveGeneratedOperableStructure(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId) {

		try {

			//			if (PStmtsaveGeneratedOperableStructure == null){
			//				PStmtsaveGeneratedOperableStructure = getConnection().prepareStatement(saveGeneratedOperableStructureString);
			//			}else
			//				PStmtsaveGeneratedOperableStructure.clearParameters();

			PreparedStatement PStmtsaveGeneratedOperableStructure = getConnection().prepareStatement(saveGeneratedOperableStructureString);

			PStmtsaveGeneratedOperableStructure.setInt(1, document.getDatabase().getId());
			PStmtsaveGeneratedOperableStructure.setLong(2, document.getId());
			PStmtsaveGeneratedOperableStructure.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveGeneratedOperableStructure.setInt(4, relationConf);
			PStmtsaveGeneratedOperableStructure.setInt(5, informationExtractionId);

			PStmtsaveGeneratedOperableStructure.execute();
			PStmtsaveGeneratedOperableStructure.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveOperableStructureGeneration(Document document, ContentExtractor ce, int relationConf,
			int informationExtractionId, String file, long time) {

		try {

			//			if (PStmtsaveOperableStructureGeneration == null){
			//				PStmtsaveOperableStructureGeneration = getConnection().prepareStatement(saveOperableStructureGenerationString);
			//			}else
			//				PStmtsaveOperableStructureGeneration.clearParameters();

			PreparedStatement PStmtsaveOperableStructureGeneration = getConnection().prepareStatement(saveOperableStructureGenerationString);

			PStmtsaveOperableStructureGeneration.setInt(1, document.getDatabase().getId());
			PStmtsaveOperableStructureGeneration.setLong(2, document.getId());
			PStmtsaveOperableStructureGeneration.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveOperableStructureGeneration.setInt(4, relationConf);
			PStmtsaveOperableStructureGeneration.setInt(5, informationExtractionId);
			PStmtsaveOperableStructureGeneration.setString(6, file);
			PStmtsaveOperableStructureGeneration.setLong(7, time);


			PStmtsaveOperableStructureGeneration.execute();
			PStmtsaveOperableStructureGeneration.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public int getExperiment(int relationConf, int infEsys) {

		int ret = -1;

		try {

			Statement StmtgetExperiment = getConnection().createStatement();

			ResultSet RSgetExperiment = StmtgetExperiment.executeQuery
					("select idExperiment from RelationExperiment where idRelationConfiguration = " + relationConf + " and idInformationExtractionSystem = " + infEsys);

			while (RSgetExperiment.next()) {

				ret = RSgetExperiment.getInt(1);

			}

			RSgetExperiment.close();
			StmtgetExperiment.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void insertRelationExperiment(int relationConfiguration,
			int informationExtractionSystemId) {

		try {

			//			if (PStmtinsertRelationExperiment== null){
			//				PStmtinsertRelationExperiment = getConnection().prepareStatement(insertRelationExperimentString);
			//			}else
			//				PStmtinsertRelationExperiment.clearParameters();

			PreparedStatement PStmtinsertRelationExperiment = getConnection().prepareStatement(insertRelationExperimentString);

			PStmtinsertRelationExperiment.setInt(1, relationConfiguration);
			PStmtinsertRelationExperiment.setInt(2, informationExtractionSystemId);

			PStmtinsertRelationExperiment.execute();

			PStmtinsertRelationExperiment.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<Integer> getRequiredExperiments(int idExperiment) {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetRequiredExperiments = getConnection().createStatement();

			ResultSet RSgetRequiredExperiments = StmtgetRequiredExperiments.executeQuery
					("select idExperimentDep from ExperimentDependency where idExperiment = " + idExperiment);

			while (RSgetRequiredExperiments.next()) {

				ret.add(RSgetRequiredExperiments.getInt(1));

			}

			RSgetRequiredExperiments.close();
			StmtgetRequiredExperiments.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public Set<Integer> getDatabasesByStatus(int idExperiment,
			ExperimentStatusEnum status) {

		return getDatabasesByStatus(idExperiment,getValue(status));

	}

	private Set<Integer> getDatabasesByStatus(int idExperiment, int value) {

		Set<Integer> ret = new HashSet<Integer>();

		try {

			Statement StmtgetDatabasesByStatus = getConnection().createStatement();

			ResultSet RSgetDatabasesByStatus = StmtgetDatabasesByStatus.executeQuery
					("select idDatabase from ExperimentStatus where idExperiment = " + idExperiment + " and status = " + value);

			while (RSgetDatabasesByStatus.next()) {

				ret.add(RSgetDatabasesByStatus.getInt(1));

			}

			RSgetDatabasesByStatus.close();
			StmtgetDatabasesByStatus.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void writeExtractionPerformance(String relation, String name, int spl, int tp,
			int fp, int fn, double precision, double recall, double fmeas, String type) {

		try {

			//			if (PStmtwriteExtractionPerformance== null){
			//				PStmtwriteExtractionPerformance = getConnection().prepareStatement(writeExtractionPerformanceString);
			//			}else
			//				PStmtwriteExtractionPerformance.clearParameters();

			PreparedStatement PStmtwriteExtractionPerformance = getConnection().prepareStatement(writeExtractionPerformanceString);

			PStmtwriteExtractionPerformance.setString(1, relation);
			PStmtwriteExtractionPerformance.setString(2, name);
			PStmtwriteExtractionPerformance.setInt(3, spl);
			PStmtwriteExtractionPerformance.setInt(4, tp);
			PStmtwriteExtractionPerformance.setInt(5, fp);
			PStmtwriteExtractionPerformance.setInt(6, fn);
			PStmtwriteExtractionPerformance.setDouble(7, precision);
			PStmtwriteExtractionPerformance.setDouble(8, recall);
			PStmtwriteExtractionPerformance.setDouble(9, fmeas);
			PStmtwriteExtractionPerformance.setString(10, type);


			PStmtwriteExtractionPerformance.execute();

			PStmtwriteExtractionPerformance.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveCorefEntity(int idDatabase, long idDocument,
			ContentExtractor contenExtractor, int informationExtractionSystem,
			int entityType, long idRootEntity, int start, int end, long time) {

		try {

			//			if (PStmtsaveCorefEntity == null){
			//				PStmtsaveCorefEntity = getConnection().prepareStatement(saveCorefEntityString);
			//			}else
			//				PStmtsaveCorefEntity.clearParameters();

			PreparedStatement PStmtsaveCorefEntity = getConnection().prepareStatement(saveCorefEntityString);

			PStmtsaveCorefEntity.setInt(1, idDatabase);
			PStmtsaveCorefEntity.setLong(2, idDocument);
			PStmtsaveCorefEntity.setInt(3, getContentExtractorId(contenExtractor)); 
			PStmtsaveCorefEntity.setInt(4, informationExtractionSystem);
			PStmtsaveCorefEntity.setInt(5, entityType);
			PStmtsaveCorefEntity.setLong(6, idRootEntity);
			PStmtsaveCorefEntity.setInt(7, start);
			PStmtsaveCorefEntity.setInt(8, end);
			PStmtsaveCorefEntity.setLong(9, time);

			PStmtsaveCorefEntity.execute();
			PStmtsaveCorefEntity.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public boolean hasDoneCoreferenceResolution(int idDatabase,
			long idDocument, ContentExtractor contentExtractor,
			int informationExractionSystem, int entityType) {

		boolean ret = false;

		try {

			//			if (PStmthasDoneCoreferenceResolution == null){
			//				PStmthasDoneCoreferenceResolution = getConnection().prepareStatement(hasDoneCoreferenceResolutionString);
			//			}else
			//				PStmthasDoneCoreferenceResolution.clearParameters();

			PreparedStatement PStmthasDoneCoreferenceResolution = getConnection().prepareStatement(hasDoneCoreferenceResolutionString);

			PStmthasDoneCoreferenceResolution.setInt(1, idDatabase);
			PStmthasDoneCoreferenceResolution.setLong(2, idDocument);
			PStmthasDoneCoreferenceResolution.setInt(3, getContentExtractorId(contentExtractor));
			PStmthasDoneCoreferenceResolution.setInt(4, informationExractionSystem);
			PStmthasDoneCoreferenceResolution.setInt(5, entityType);

			ResultSet RShasDoneCoreferenceResolution = PStmthasDoneCoreferenceResolution.executeQuery();

			while (RShasDoneCoreferenceResolution.next()) {

				ret = true;

			}

			RShasDoneCoreferenceResolution.close();

			PStmthasDoneCoreferenceResolution.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void saveCoreferenceResolution(int idDatabase, long idDocument,
			ContentExtractor contentExtractor, int informationExractionSystem,
			int entityType, long time) {

		try {

			//			if (PStmtsaveCoreferenceResolution == null){
			//				PStmtsaveCoreferenceResolution = getConnection().prepareStatement(saveCoreferenceResolutionString);
			//			}else
			//				PStmtsaveCoreferenceResolution.clearParameters();

			PreparedStatement PStmtsaveCoreferenceResolution = getConnection().prepareStatement(saveCoreferenceResolutionString);

			PStmtsaveCoreferenceResolution.setInt(1, idDatabase);
			PStmtsaveCoreferenceResolution.setLong(2, idDocument);
			PStmtsaveCoreferenceResolution.setInt(3, getContentExtractorId(contentExtractor)); 
			PStmtsaveCoreferenceResolution.setInt(4, informationExractionSystem);
			PStmtsaveCoreferenceResolution.setInt(5, entityType);
			PStmtsaveCoreferenceResolution.setLong(6, time);

			PStmtsaveCoreferenceResolution.execute();
			PStmtsaveCoreferenceResolution.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean existsQuery(TextQuery t) {

		Long qId = getTextQueryTable(false,t);

		if (qId == null){

			return false;

		}

		return true;

	}

	@Override
	public void prepareNegativeSampleEntry(int idExperiment, Database database,
			int split, int processedDocs, int position, TextQuery query, String navHandler, String extTechnique, String resExtTechnique) {

		Long qId = getTextQuery(query);

		prepareNegativeSampleEntry(idExperiment, database, split, processedDocs, position, qId, navHandler, extTechnique, resExtTechnique);

	}

	@Override
	public void prepareNegativeSampleEntry(int idExperiment, Database database,
			int split, int processedDocs, int position, long queryId, String navHandler, String extTechnique, String resExtTechnique) {

		try {

			PreparedStatement ps;

			synchronized (prepareNegativeSampleEntryTable) {

				ps = prepareNegativeSampleEntryTable.get(database.getId());

				if (ps == null){
					ps = getConnection().prepareStatement(writeNegativeSampleEntryString);
					prepareNegativeSampleEntryTable.put(database.getId(), ps);
				}

			}

			ps.setInt(1, idExperiment);
			ps.setInt(2, database.getId());
			ps.setInt(3, split);
			ps.setInt(4, processedDocs);
			ps.setInt(5, position);
			ps.setLong(6, queryId);
			ps.setInt(7, getNavigationTechniqueId(navHandler));
			ps.setInt(8, getExtractionTechniqueId(extTechnique));
			ps.setInt(9, getResultExtractionTechniqueId(resExtTechnique));

			ps.addBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public Database getDatabaseById(int idDatabase) {
		return getDatabase(idDatabase);
	}

	@Override
	public List<String> getQueriesForNegativeSample(int experimentId, int processedDocs,
			Database database, int version_seed) {

		List<String> ret = new ArrayList<String>();

		try {

			//			if (PStmtgetQueriesForNegativeSample == null){
			//				PStmtgetQueriesForNegativeSample = getConnection().prepareStatement(getQueriesForNegativeSampleString);
			//			}else
			//				PStmtgetQueriesForNegativeSample.clearParameters();

			PreparedStatement PStmtgetQueriesForNegativeSample = getConnection().prepareStatement(getQueriesForNegativeSampleString);

			PStmtgetQueriesForNegativeSample.setInt(1, experimentId);
			PStmtgetQueriesForNegativeSample.setInt(2, processedDocs);
			PStmtgetQueriesForNegativeSample.setInt(3, database.getId());
			PStmtgetQueriesForNegativeSample.setInt(4, version_seed);

			ResultSet RSgetQueriesForNegativeSample = PStmtgetQueriesForNegativeSample.executeQuery();

			while (RSgetQueriesForNegativeSample.next()) {

				ret.add(RSgetQueriesForNegativeSample.getString(1));

			}

			RSgetQueriesForNegativeSample.close();
			PStmtgetQueriesForNegativeSample.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public String getInformationExtractionSystemName(
			int idRelationExtractionSystem) {

		String ret = "";

		try {

			Statement StmtgetInformationExtractionSystemName = getConnection().createStatement();

			ResultSet RSgetInformationExtractionSystemName = StmtgetInformationExtractionSystemName.executeQuery
					("select name from RelationExtractionSystem where idRelationExtractionSystem = " + idRelationExtractionSystem);

			while (RSgetInformationExtractionSystemName.next()) {

				ret = RSgetInformationExtractionSystemName.getString(1);

			}

			RSgetInformationExtractionSystemName.close();
			StmtgetInformationExtractionSystemName.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getRelationExtractionSystemId(int relationConf,
			int informationExtractionId) {

		int ret = -1;

		try {

			//			if (PStmtgetRelationExtractionSystemId == null){
			//				PStmtgetRelationExtractionSystemId = getConnection().prepareStatement(getRelationExtractionSystemIdString);
			//			}else
			//				PStmtgetRelationExtractionSystemId.clearParameters();

			PreparedStatement PStmtgetRelationExtractionSystemId = getConnection().prepareStatement(getRelationExtractionSystemIdString);

			PStmtgetRelationExtractionSystemId.setInt(1, relationConf);
			PStmtgetRelationExtractionSystemId.setInt(2, informationExtractionId);

			ResultSet RSgetRelationExtractionSystemId = PStmtgetRelationExtractionSystemId.executeQuery();

			while (RSgetRelationExtractionSystemId.next()) {

				ret= RSgetRelationExtractionSystemId.getInt(1);

			}

			RSgetRelationExtractionSystemId.close();
			PStmtgetRelationExtractionSystemId.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public boolean isExperimentInStatus(Database database, int idExperiment,
			ExperimentStatusEnum... state) {

		boolean ret = false;

		try {

			Statement StmtisExperimentInStatus = getConnection().createStatement();

			ResultSet RSisExperimentInStatus = StmtisExperimentInStatus.executeQuery
					("select * from ExperimentStatus where idDatabase = " + database.getId() + " and idExperiment = " + idExperiment + " and status IN " + generateIN(state));

			while (RSisExperimentInStatus.next()) {

				ret = true;

			}

			RSisExperimentInStatus.close();
			StmtisExperimentInStatus.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private String generateIN(ExperimentStatusEnum[] state) {

		String ret = "(" + getValue(state[0]);

		for (int i = 1; i < state.length; i++) {

			ret += "," + getValue(state[i]);

		}

		return ret + ")";

	}

	@Override
	public void cleanExperimentStatusInStatus(int idExperiment,
			ExperimentStatusEnum... state) {

		String cleanExperimentStatusInStatus = "DELETE FROM ExperimentStatus where idExperiment = " + idExperiment + " and status IN " + generateIN(state);

		performTransaction(cleanExperimentStatusInStatus);

	}

	@Override
	public void cleanExperimentStatusNotInStatus(int idExperiment,
			ExperimentStatusEnum... state) {

		String cleanExperimentStatusNotInStatus = "DELETE FROM ExperimentStatus where idExperiment = " + idExperiment + " and status NOT IN " + generateIN(state);

		performTransaction(cleanExperimentStatusNotInStatus);

	}

	@Override
	public List<Integer> getActiveSampleConfigurationIds() {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetActiveSampleConfigurationIds = getConnection().createStatement();

			ResultSet RSgetActiveSampleConfigurationIds = StmtgetActiveSampleConfigurationIds.executeQuery
					("SELECT S.idSampleConfiguration FROM SampleConfiguration S WHERE S.active = 1 ORDER BY S.idSampleConfiguration");

			while (RSgetActiveSampleConfigurationIds.next()) {

				ret.add(RSgetSampleConfigurationId.getInt(1));

			}

			RSgetActiveSampleConfigurationIds.close();

			StmtgetActiveSampleConfigurationIds.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<Integer> getWorkloadIds() {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetWorkloadIds = getConnection().createStatement();

			ResultSet RSgetWorkloadIds = StmtgetWorkloadIds.executeQuery
					("SELECT idWorkload FROM Workload ORDER BY idWorkload");

			while (RSgetWorkloadIds.next()) {

				ret.add(RSgetWorkloadIds.getInt(1));

			}

			RSgetWorkloadIds.close();

			StmtgetWorkloadIds.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;


	}

	@Override
	public boolean hasGeneratedQueries(int idSample, int algorithm, SampleBuilderParameters spb) {

		should cache ...
		
		try {

			//			if (PStmthasGeneratedQueries == null){
			//				PStmthasGeneratedQueries = getConnection().prepareStatement(hasGeneratedQueriesString);
			//			}else
			//				PStmthasGeneratedQueries.clearParameters();

			PreparedStatement PStmthasGeneratedQueries = getConnection().prepareStatement(hasGeneratedQueriesString);

			PStmthasGeneratedQueries.setInt(1, idSample);
			PStmthasGeneratedQueries.setInt(2, algorithm);
			PStmthasGeneratedQueries.setInt(3, spb.getId());

			PStmthasGeneratedQueries.execute();

			PStmthasGeneratedQueries.close();

		} catch (SQLException e) {
			return true; //duplicated key, someone has already written it.
		}

		return false;
	}

	@Override
	public  void writeGeneratedQueries(int idSample, SampleBuilderParameters spb, int algorithm,
			ExperimentStatusEnum state) {

		try {
			//			if (PStmtwriteGeneratedQueries == null){
			//				PStmtwriteGeneratedQueries = getConnection().prepareStatement(writeGeneratedQueriesStatus);
			//			}else
			//				PStmtwriteGeneratedQueries.clearParameters();

			PreparedStatement PStmtwriteGeneratedQueries = getConnection().prepareStatement(writeGeneratedQueriesStatus);

			PStmtwriteGeneratedQueries.setInt(1, getValue(state));
			PStmtwriteGeneratedQueries.setInt(2, algorithm);
			PStmtwriteGeneratedQueries.setInt(3, idSample);
			PStmtwriteGeneratedQueries.setInt(4, spb.getId());


			PStmtwriteGeneratedQueries.execute();

			PStmtwriteGeneratedQueries.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertQueryForTuplesGeneration(int experimentId,
			Database database, long queryId, int position) {

		try {

			//			if (PStmtinsertQueryForTuplesGeneration == null){
			//				PStmtinsertQueryForTuplesGeneration = getConnection().prepareStatement(insertQueryForTuplesGenerationString);
			//			}else
			//				PStmtinsertQueryForTuplesGeneration.clearParameters();

			PreparedStatement PStmtinsertQueryForTuplesGeneration = getConnection().prepareStatement(insertQueryForTuplesGenerationString);

			PStmtinsertQueryForTuplesGeneration.setInt(1, experimentId);
			PStmtinsertQueryForTuplesGeneration.setInt(2, database.getId());
			PStmtinsertQueryForTuplesGeneration.setInt(3, position);
			PStmtinsertQueryForTuplesGeneration.setLong(4, queryId);

			PStmtinsertQueryForTuplesGeneration.execute();

			PStmtinsertQueryForTuplesGeneration.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public List<List<String>> loadQueriesForTuple(int experimentId,
			Database database) {

		List<List<String>> ret = new ArrayList<List<String>>();

		try {

			//			if (PStmtloadQueriesForTuple == null){
			//				PStmtloadQueriesForTuple = getConnection().prepareStatement(loadQueriesForTupleString);
			//			}else
			//				PStmtloadQueriesForTuple.clearParameters();

			PreparedStatement PStmtloadQueriesForTuple = getConnection().prepareStatement(loadQueriesForTupleString);

			PStmtloadQueriesForTuple.setInt(1, experimentId);
			PStmtloadQueriesForTuple.setInt(2, database.getId());

			ResultSet RSloadQueriesForTuple = PStmtloadQueriesForTuple.executeQuery();

			while (RSloadQueriesForTuple.next()) {

				ret.add(Arrays.asList(RSloadQueriesForTuple.getString(1)));

			}

			RSloadQueriesForTuple.close();

			PStmtloadQueriesForTuple.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void insertQueryForTuplesGeneration(int experimentId,
			Database database, TextQuery tq, int position) {

		//I know that it exists already.

		Long queryId = getTextQuery(tq);

		insertQueryForTuplesGeneration(experimentId, database, queryId, position);

	}

	@Override
	public int getCoreferenceExperiment(int idEntityType,
			int idInformationExtractionSystem) {

		int ret = -1;

		try {

			Statement StmtgetCoreferenceExperiment = getConnection().createStatement();

			ResultSet RSgetCoreferenceExperiment = StmtgetCoreferenceExperiment.executeQuery
					("SELECT idExperiment FROM CoreferenceExperiment where idEntityType = " + idEntityType + " and idInformationExtractionSystem = " + idInformationExtractionSystem);

			while (RSgetCoreferenceExperiment.next()) {

				ret = RSgetCoreferenceExperiment.getInt(1);

			}

			RSgetCoreferenceExperiment.close();

			StmtgetCoreferenceExperiment.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public int getRESInformationExtractionSystem(int idRelationExtractionSystem) {

		int ret = -1;

		try {

			Statement StmtgetRESInformationExtractionSystem = getConnection().createStatement();

			ResultSet RSgetRESInformationExtractionSystem = StmtgetRESInformationExtractionSystem.executeQuery
					("select idInformationExtractionSystem from `AutomaticQueryGeneration`.`RelationExtractionSystem` where idRelationExtractionSystem = " + idRelationExtractionSystem);

			while (RSgetRESInformationExtractionSystem.next()) {

				ret = RSgetRESInformationExtractionSystem.getInt(1);

			}

			RSgetRESInformationExtractionSystem.close();

			StmtgetRESInformationExtractionSystem.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public String getRESFileModel(int idInformationExtractionSystem, int idRelationshipType) {

		String ret = "";

		try {

			Statement StmtgetRESFileModel = getConnection().createStatement();

			ResultSet RSgetRESFileModel = StmtgetRESFileModel.executeQuery
					("select fileModel from `AutomaticQueryGeneration`.`RelationExtractionModel` where idInformationExtractionSystem = " + idInformationExtractionSystem + " and idRelationshipType = " + idRelationshipType);

			while (RSgetRESFileModel.next()) {

				ret = RSgetRESFileModel.getString(1);

			}

			RSgetRESFileModel.close();

			StmtgetRESFileModel.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public int getRESRelationConfiguration(int idRelationExtractionSystem) {

		int ret = -1;

		try {

			Statement StmtgetRESRelationConfiguration = getConnection().createStatement();

			ResultSet RSgetRESRelationConfiguration = StmtgetRESRelationConfiguration.executeQuery
					("select idRelationConfiguration from `AutomaticQueryGeneration`.`RelationExtractionSystem` where idRelationExtractionSystem = " + idRelationExtractionSystem);

			while (RSgetRESRelationConfiguration.next()) {

				ret = RSgetRESInformationExtractionSystem.getInt(1);

			}

			RSgetRESRelationConfiguration.close();

			StmtgetRESRelationConfiguration.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public int getRelationshipType(int idRelationConfiguration) {

		int ret = -1;

		try {

			Statement StmtgetRelationshipType = getConnection().createStatement();

			ResultSet RSgetRelationshipType = StmtgetRelationshipType.executeQuery
					("select idRelationshipType from `AutomaticQueryGeneration`.`RelationConfiguration` where idRelationConfiguration  = " + idRelationConfiguration);

			while (RSgetRelationshipType.next()) {

				ret = RSgetRelationshipType.getInt(1);

			}

			RSgetRelationshipType.close();

			StmtgetRelationshipType.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public int getQueriesBatchLastExecutedQuery(int searchRoundId,
			Database database, String navigationTechnique, ExperimentEnum experiment) {

		return getQueriesBatchLastExecutedQuery(searchRoundId,database,navigationTechnique,getExperiment(experiment));

	}

	public int getQueriesBatchLastExecutedQuery(int searchRoundId,
			Database database, String navigationTechnique, int experiment) {

		int ret = 0;

		try {

			//			if (PStmtgetQueriesBatchLastExecutedQuery == null){
			//				PStmtgetQueriesBatchLastExecutedQuery = getConnection().prepareStatement(getQueriesBatchLastExecutedQueryString);
			//			}else
			//				PStmtgetQueriesBatchLastExecutedQuery.clearParameters();

			PreparedStatement PStmtgetQueriesBatchLastExecutedQuery = getConnection().prepareStatement(getQueriesBatchLastExecutedQueryString);

			PStmtgetQueriesBatchLastExecutedQuery.setInt(1, experiment);
			PStmtgetQueriesBatchLastExecutedQuery.setInt(2, database.getId());

			ResultSet RSgetQueriesBatchLastExecutedQuery = PStmtgetQueriesBatchLastExecutedQuery.executeQuery();

			//it's only one

			while (RSgetQueriesBatchLastExecutedQuery.next()) {

				ret = RSgetQueriesBatchLastExecutedQuery.getInt(1);

			}

			RSgetQueriesBatchLastExecutedQuery.close();

			PStmtgetQueriesBatchLastExecutedQuery.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;


	}

	@Override
	public List<Integer> getQueriesBatchNegativeLastExecutedQuery(int searchRoundId,
			Database database, int split, int processedDocs, String extractionTechnique, String navigationTechnique, String resultExtractionTechnique) {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			int lastSavedQuery = getQueriesBatchLastExecutedQuery(searchRoundId, database, navigationTechnique, getNegativeConsistensy(split));

			boolean store = false;

			if (lastSavedQuery == 0)
				store = true;

			//			if (PStmtgetQueriesBatchNegativeLastExecutedQuery == null){
			//				PStmtgetQueriesBatchNegativeLastExecutedQuery = getConnection().prepareStatement(getQueriesBatchNegativeLastExecutedQueryString);
			//			}else
			//				PStmtgetQueriesBatchNegativeLastExecutedQuery.clearParameters();

			PreparedStatement PStmtgetQueriesBatchNegativeLastExecutedQuery = getConnection().prepareStatement(getQueriesBatchNegativeLastExecutedQueryString);

			PStmtgetQueriesBatchNegativeLastExecutedQuery.setInt(1, searchRoundId);
			PStmtgetQueriesBatchNegativeLastExecutedQuery.setInt(2, database.getId());
			PStmtgetQueriesBatchNegativeLastExecutedQuery.setInt(3, split);
			PStmtgetQueriesBatchNegativeLastExecutedQuery.setInt(4, processedDocs);
			PStmtgetQueriesBatchNegativeLastExecutedQuery.setInt(5, getNavigationTechniqueId(navigationTechnique));
			PStmtgetQueriesBatchNegativeLastExecutedQuery.setInt(6, getExtractionTechniqueId(extractionTechnique));
			PStmtgetQueriesBatchNegativeLastExecutedQuery.setInt(7, getResultExtractionTechniqueId(resultExtractionTechnique));

			ResultSet RSgetQueriesBatchNegativeLastExecutedQuery = PStmtgetQueriesBatchNegativeLastExecutedQuery.executeQuery();

			while (RSgetQueriesBatchNegativeLastExecutedQuery.next()) {

				int idQuery = RSgetQueriesBatchNegativeLastExecutedQuery.getInt(1);

				if (store || idQuery == lastSavedQuery){
					store = true;

					ret.add(idQuery);

				}

			}

			RSgetQueriesBatchNegativeLastExecutedQuery.close();

			PStmtgetQueriesBatchNegativeLastExecutedQuery.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public int getQueriesBatchTuplesLastExecutedQuery(int searchRoundId,
			Database database, String extTechnique, String navHandler,
			String resExtTechnique) {

		int ret = -1;

		try {

			//			if (PStmtgetQueriesBatchTuplesLastExecutedQuery == null){
			//				PStmtgetQueriesBatchTuplesLastExecutedQuery = getConnection().prepareStatement(getQueriesBatchTuplesLastExecutedQueryString);
			//			}else
			//				PStmtgetQueriesBatchTuplesLastExecutedQuery.clearParameters();

			PreparedStatement PStmtgetQueriesBatchTuplesLastExecutedQuery = getConnection().prepareStatement(getQueriesBatchTuplesLastExecutedQueryString);

			PStmtgetQueriesBatchTuplesLastExecutedQuery.setInt(1, searchRoundId);
			PStmtgetQueriesBatchTuplesLastExecutedQuery.setInt(2, database.getId());
			PStmtgetQueriesBatchTuplesLastExecutedQuery.setInt(3, getExtractionTechniqueId(extTechnique));
			PStmtgetQueriesBatchTuplesLastExecutedQuery.setInt(4, getNavigationTechniqueId(navHandler));
			PStmtgetQueriesBatchTuplesLastExecutedQuery.setInt(5, getResultExtractionTechniqueId(resExtTechnique));


			ResultSet RSgetQueriesBatchTuplesLastExecutedQuery = PStmtgetQueriesBatchTuplesLastExecutedQuery.executeQuery();

			while (RSgetQueriesBatchTuplesLastExecutedQuery.next()) {

				ret = RSgetQueriesBatchTuplesLastExecutedQuery.getInt(1);

			}

			RSgetQueriesBatchTuplesLastExecutedQuery.close();

			PStmtgetQueriesBatchTuplesLastExecutedQuery.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void reportQueryForTupleSent(int searchRoundId, Database database,
			int position, TextQuery tq, String extTechnique, String navHandler,
			String resExtTechnique) {

		long qId = getTextQuery(tq);

		try {

			//			if (PStmtreportQueryForTupleSent == null){
			//				PStmtreportQueryForTupleSent = getConnection().prepareStatement(reportQueryForTupleSentString);
			//			}else
			//				PStmtreportQueryForTupleSent.clearParameters();

			PreparedStatement PStmtreportQueryForTupleSent = getConnection().prepareStatement(reportQueryForTupleSentString);

			PStmtreportQueryForTupleSent.setInt(1, searchRoundId);
			PStmtreportQueryForTupleSent.setInt(2, database.getId());
			PStmtreportQueryForTupleSent.setInt(3, position);
			PStmtreportQueryForTupleSent.setInt(4, getExtractionTechniqueId(extTechnique));
			PStmtreportQueryForTupleSent.setInt(5, getNavigationTechniqueId(navHandler));
			PStmtreportQueryForTupleSent.setInt(6, getResultExtractionTechniqueId(resExtTechnique));
			PStmtreportQueryForTupleSent.setLong(7, qId);

			PStmtreportQueryForTupleSent.execute();
			PStmtreportQueryForTupleSent.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public List<Database> getSamplableDatabases(int group) {

		List<Database> ret = new ArrayList<Database>();

		try {

			Statement StmtgetSamplableDatabases = getConnection().createStatement();

			ResultSet RSgetSamplableDatabases = StmtgetSamplableDatabases.executeQuery
					("select idDatabase from `AutomaticQueryGeneration`.`DatabaseExperimentSet` where experimentSplit  = " + group);

			while (RSgetSamplableDatabases.next()) {

				ret.add(getDatabase(RSgetSamplableDatabases.getInt(1)));

			}

			RSgetSamplableDatabases.close();

			StmtgetSamplableDatabases.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public boolean isExperimentAvailable(ExperimentEnum exp,
			int idDatabase, String computerName) {
		return isExperimentAvailable(getExperiment(exp), idDatabase, computerName);
	}

	@Override
	public void makeExperimentAvailable(ExperimentEnum exp,
			int idDatabase) {

		makeExperimentAvailable(getExperiment(exp), idDatabase);

	}

	private int getExperiment(ExperimentEnum exp) {

		switch (exp) {
		case QUERYING:
			return QUERYING_VALUE;
		case CONSISTENSY_1:
			return CONSISTENSY_VALUE_1;
		case CONSISTENSY_2:
			return CONSISTENSY_VALUE_2;
		case CONSISTENSY_3:
			return CONSISTENSY_VALUE_3;
		case CONSISTENSY_4:
			return CONSISTENSY_VALUE_4;
		case CONSISTENSY_5:
			return CONSISTENSY_VALUE_5;
		case CONSISTENSY_6:
			return CONSISTENSY_VALUE_6;
		case NEGATIVE_CONSISTENSY_1:
			return NEGATIVE_CONSISTENSY_VALUE_1;
		case NEGATIVE_CONSISTENSY_2:
			return NEGATIVE_CONSISTENSY_VALUE_2;
		case NEGATIVE_CONSISTENSY_3:
			return NEGATIVE_CONSISTENSY_VALUE_3;
		case NEGATIVE_CONSISTENSY_4:
			return NEGATIVE_CONSISTENSY_VALUE_4;
		case NEGATIVE_CONSISTENSY_5:
			return NEGATIVE_CONSISTENSY_VALUE_5;
		case TUPLE_CONSISTENCY:
			return TUPLE_CONSISTENCY_VALUE;
		default:
			break;
		}

		return -1;
	}

	@Override
	public void removeNegativeSampleEntries(int searchRoundId,
			Database database, int split, int allowed_size, String navHandler,
			String extTechnique, String resExtTechnique) {

		performTransaction("delete from NegativeSampling where idExperimentId = "+searchRoundId+" and idDatabase = " + database.getId() + " and split = " + split + " and processedDocs = " + allowed_size + " and navigationTechnique = " + getNavigationTechniqueId(navHandler) + " and extractionTechnique = " + getExtractionTechniqueId(extTechnique) + " and resultExtractionTechnique = " + getResultExtractionTechniqueId(resExtTechnique));

	}

	@Override
	public void removeQueriesForTupleSent(int searchRoundId, Database database,
			String extTechnique, String navHandler, String resExtTechnique) {

		performTransaction("delete from QueryForTupleLog where idExperiment = " + searchRoundId + " and idDatabase = " + database.getId() + " and extractionTechnique = " + getExtractionTechniqueId(extTechnique) + " and navigationTechnique = " + getNavigationTechniqueId(navHandler) + " and resultExtractionTechnique = " + getResultExtractionTechniqueId(resExtTechnique));

	}

	@Override
	public void writeRelationKeyword(int idInformationExtractionSystem, int relationConf, String collection, int idWorkload, int idVersion, int split, boolean tasw, ASEvaluation eval,
			int position, TextQuery query, int docsInTraining) {

		long idQuery = getTextQuery(query);

		try {

			//			if (PStmtwriteRelationKeyword == null){
			//				PStmtwriteRelationKeyword = getConnection().prepareStatement(writeRelationKeywordString);
			//			}else
			//				PStmtwriteRelationKeyword.clearParameters();

			PreparedStatement PStmtwriteRelationKeyword = getConnection().prepareStatement(writeRelationKeywordString);

			PStmtwriteRelationKeyword.setInt(1, idInformationExtractionSystem);
			PStmtwriteRelationKeyword.setInt(2, relationConf);
			PStmtwriteRelationKeyword.setString(3, collection);
			PStmtwriteRelationKeyword.setInt(4, idWorkload);
			PStmtwriteRelationKeyword.setInt(5, idVersion);
			PStmtwriteRelationKeyword.setInt(6, split);
			PStmtwriteRelationKeyword.setBoolean(7, tasw);
			PStmtwriteRelationKeyword.setString(8, eval.getClass().getSimpleName());
			PStmtwriteRelationKeyword.setInt(9, position);
			PStmtwriteRelationKeyword.setInt(10, docsInTraining);
			PStmtwriteRelationKeyword.setLong(11, idQuery);

			PStmtwriteRelationKeyword.execute();
			PStmtwriteRelationKeyword.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<String> getRelationKeywords(int idInformationExtractionSystem, int relationConf, String collection, WorkloadModel workload, Version version,
			boolean tuplesAsStopWords, int version_seed, ASEvaluation eval, int docsInTraining) {

		List<String> relationKeywords = new ArrayList<String>();

		try {

			//			if (PStmtgetRelationKeywords == null){
			//				PStmtgetRelationKeywords = getConnection().prepareStatement(getRelationKeywordsString);
			//			}else
			//				PStmtgetRelationKeywords.clearParameters();

			PreparedStatement PStmtgetRelationKeywords = getConnection().prepareStatement(getRelationKeywordsString);

			PStmtgetRelationKeywords.setInt(1, idInformationExtractionSystem);
			PStmtgetRelationKeywords.setInt(2, relationConf);
			PStmtgetRelationKeywords.setString(3, collection);
			PStmtgetRelationKeywords.setInt(4, workload.getId());
			PStmtgetRelationKeywords.setInt(5, version.getId());
			PStmtgetRelationKeywords.setInt(6, version_seed);
			PStmtgetRelationKeywords.setBoolean(7, tuplesAsStopWords);
			PStmtgetRelationKeywords.setString(8, eval.getClass().getSimpleName());
			PStmtgetRelationKeywords.setInt(9, docsInTraining);

			ResultSet RSgetRelationKeywords = PStmtgetRelationKeywords.executeQuery();

			while (RSgetRelationKeywords.next()){

				relationKeywords.add(RSgetRelationKeywords.getString(1));

			}

			RSgetRelationKeywords.close();
			PStmtgetRelationKeywords.close();

		} catch (SQLException e) {
			return null;
		}

		return relationKeywords;

	}

	@Override
	public void saveSampleDocuments(int id, List<Document> docs,
			boolean isUseful) {

		try {

			//			if (PStmtsaveSampleDocuments == null){
			//				PStmtsaveSampleDocuments = getConnection().prepareStatement(saveSampleDocumentsString);
			//			}else
			//				PStmtsaveSampleDocuments.clearParameters();

			PreparedStatement PStmtsaveSampleDocuments = getConnection().prepareStatement(saveSampleDocumentsString);

			for (int i = 0; i < docs.size(); i++) {

				PStmtsaveSampleDocuments.setInt(1, id);
				PStmtsaveSampleDocuments.setBoolean(2, isUseful);
				PStmtsaveSampleDocuments.setInt(3, i + 1);
				PStmtsaveSampleDocuments.setLong(4, docs.get(i).getDatabase().getId());
				PStmtsaveSampleDocuments.setLong(5, docs.get(i).getId());
				PStmtsaveSampleDocuments.addBatch();

			}

			PStmtsaveSampleDocuments.executeBatch();
			PStmtsaveSampleDocuments.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveSampleTuples(int id, Hashtable<Document, List<Tuple>> tuples) {

		try {

			//			if (PStmtsaveSampleTuples == null){
			//				PStmtsaveSampleTuples = getConnection().prepareStatement(saveSampleTuplesString);
			//			}else
			//				PStmtsaveSampleTuples.clearParameters();

			PreparedStatement PStmtsaveSampleTuples = getConnection().prepareStatement(saveSampleTuplesString);

			int i = 0;

			for (Entry<Document,List<Tuple>> entry : tuples.entrySet()) {

				for (Tuple tuple : entry.getValue()) {

					PStmtsaveSampleTuples.setInt(1, id);

					PStmtsaveSampleTuples.setInt(2, i+1);

					PStmtsaveSampleTuples.setInt(3, entry.getKey().getDatabase().getId());

					PStmtsaveSampleTuples.setLong(4, entry.getKey().getId());

					PStmtsaveSampleTuples.setString(5, tuple.toString());

					PStmtsaveSampleTuples.addBatch();

					i++;

				}

			}

			PStmtsaveSampleTuples.executeBatch();

			PStmtsaveSampleTuples.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<Document> getSampleDocuments(Sample sample, int numberofUsefulDocuments,int numberofUselessDocuments){
		return getSampleDocuments(sample, numberofUsefulDocuments, numberofUselessDocuments, null);
	}
	
	@Override
	public List<Document> getSampleDocuments(Sample sample, int usefulDocuments, int uselessDocuments, DocumentHandler dh) {

		List<Document> documents = new ArrayList<Document>();

		try {

			//			if (PStmtgetSampleDocuments == null){
			//				PStmtgetSampleDocuments = getConnection().prepareStatement(getSampleDocumentsString);
			//			}else
			//				PStmtgetSampleDocuments.clearParameters();

			PreparedStatement PStmtgetSampleDocuments = getConnection().prepareStatement(getSampleDocumentsString);

			PStmtgetSampleDocuments.setInt(1, sample.getId());
			PStmtgetSampleDocuments.setInt(2, usefulDocuments);
			PStmtgetSampleDocuments.setInt(3, uselessDocuments);

			ResultSet RSgetSampleDocuments = PStmtgetSampleDocuments.executeQuery();

			if (dh == null){

			
				while (RSgetSampleDocuments.next()){
	
					documents.add(new Document(getDatabase(RSgetSampleDocuments.getInt(1)),RSgetSampleDocuments.getLong(2)));
	
				}

			} else {
				
				while (RSgetSampleDocuments.next()){
					
					documents.add(dh.getDocument(getDatabaseById(RSgetSampleDocuments.getInt(1)),RSgetSampleDocuments.getLong(2)));

				}
				
			}
			RSgetSampleDocuments.close();
			PStmtgetSampleDocuments.close();

		} catch (SQLException e) {
			return null;
		}

		return documents;

	}

	@Override
	public SampleBuilderParameters getSampleBuilderParameters(
			int sampleBuilderId) {

		SampleBuilderParameters sp = sampleBuilders.get(sampleBuilderId);

		if (sp != null)
			return sp;

		try {

			//			if (PStmtgetSampleBuilderParameters == null){
			//				PStmtgetSampleBuilderParameters = getConnection().prepareStatement(getSampleBuilderParametersString );
			//			}else
			//				PStmtgetSampleBuilderParameters.clearParameters();

			PreparedStatement PStmtgetSampleBuilderParameters = getConnection().prepareStatement(getSampleBuilderParametersString );

			PStmtgetSampleBuilderParameters.setInt(1, sampleBuilderId);

			ResultSet RSgetSampleBuilderParameters = PStmtgetSampleBuilderParameters.executeQuery();

			int idParameter = -1;
			int NoF = -1; 
			double MinF = -1;
			double MaxF = -1;
			boolean unique = false;
			boolean lowercase = false;
			boolean stemmed = false;
			boolean tasw = false;
			int usefulDocuments = -1;
			int uselessDocuments = -1;

			while (RSgetSampleBuilderParameters.next()) {

				idParameter = RSgetSampleBuilderParameters.getInt(2);

				NoF = RSgetSampleBuilderParameters.getInt(3);

				MinF = RSgetSampleBuilderParameters.getDouble(4);

				MaxF = RSgetSampleBuilderParameters.getDouble(5);

				unique = RSgetSampleBuilderParameters.getBoolean(6);

				lowercase = RSgetSampleBuilderParameters.getBoolean(7);

				stemmed = RSgetSampleBuilderParameters.getBoolean(8);

				tasw = RSgetSampleBuilderParameters.getBoolean(9);

				usefulDocuments = RSgetSampleBuilderParameters.getInt(10);

				uselessDocuments = RSgetSampleBuilderParameters.getInt(11);

			}

			RSgetSampleBuilderParameters.close();

			PStmtgetSampleBuilderParameters.close();

			Hashtable<String, String> p = readParameters(idParameter);

			Parametrizable param = loadWordExtractors(p);

			ContentLoader cl = ContentLoaderFactory.generateInstance(param.loadParameter(ExecutionAlternativeEnum.CONTENT_LOADER).getString());

			ContentExtractor ce = ContentExtractorFactory.generateInstance(param.loadParameter(ExecutionAlternativeEnum.CONTENT_EXTRACTION).getString());

			sp = new SampleBuilderParameters(sampleBuilderId,NoF,MinF,MaxF,unique,lowercase,stemmed,tasw,WordExtractorFactory.generateInstance(param.loadParameter(ExecutionAlternativeEnum.GENERAL_WORD_EXTRACTOR).getString(),cl,ce),WordExtractorFactory.generateInstance(param.loadParameter(ExecutionAlternativeEnum.USEFUL_DOCUMENTS_WORD_EXTRACTOR).getString(),cl,ce), idParameter,usefulDocuments,uselessDocuments);

			synchronized (sampleBuilders) {

				sampleBuilders.put(sampleBuilderId,sp);

			}

			return sp;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	private Parametrizable loadWordExtractors(Hashtable<String, String> p) {

		TableParameters tp = new TableParameters();

		Parametrizable contentLoaderText = new StringParameters(ExecutionAlternativeEnum.CONTENT_LOADER,p.get(ExecutionAlternativeEnum.CONTENT_LOADER.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CONTENT_LOADER, contentLoaderText);

		Parametrizable contentExtText = new StringParameters(ExecutionAlternativeEnum.CONTENT_EXTRACTION,p.get(ExecutionAlternativeEnum.CONTENT_EXTRACTION.toString()));

		tp.addParameter(ExecutionAlternativeEnum.CONTENT_EXTRACTION, contentExtText);

		Parametrizable gweText = new StringParameters(ExecutionAlternativeEnum.GENERAL_WORD_EXTRACTOR,p.get(ExecutionAlternativeEnum.GENERAL_WORD_EXTRACTOR.toString()));

		tp.addParameter(ExecutionAlternativeEnum.GENERAL_WORD_EXTRACTOR, gweText);

		Parametrizable uweText = new StringParameters(ExecutionAlternativeEnum.USEFUL_DOCUMENTS_WORD_EXTRACTOR,p.get(ExecutionAlternativeEnum.USEFUL_DOCUMENTS_WORD_EXTRACTOR.toString()));

		tp.addParameter(ExecutionAlternativeEnum.USEFUL_DOCUMENTS_WORD_EXTRACTOR, uweText);

		return tp;

	}

	@Override
	public Collection<Document> getUsefulDocuments(Sample sample, int usefulDocuments) {

		List<Document> documents = new ArrayList<Document>();

		try {

			//			if (PStmtgetSampleUsefulDocuments == null){
			//				PStmtgetSampleUsefulDocuments = getConnection().prepareStatement(getSampleUsefulDocumentsString);
			//			}else
			//				PStmtgetSampleUsefulDocuments.clearParameters();

			PreparedStatement PStmtgetSampleUsefulDocuments = getConnection().prepareStatement(getSampleUsefulDocumentsString);

			PStmtgetSampleUsefulDocuments.setInt(1, sample.getId());
			PStmtgetSampleUsefulDocuments.setInt(2, usefulDocuments);

			ResultSet RSgetSampleUsefulDocuments = PStmtgetSampleUsefulDocuments.executeQuery();

			while (RSgetSampleUsefulDocuments.next()){

				documents.add(new Document(getDatabase(RSgetSampleUsefulDocuments.getInt(1)),RSgetSampleUsefulDocuments.getLong(2)));

			}

			RSgetSampleUsefulDocuments.close();
			PStmtgetSampleUsefulDocuments.close();

		} catch (SQLException e) {
			return null;
		}

		return documents;

	}

	@Override
	public Set<Tuple> getTuples(Sample sample, Collection<Document> documents) {

		Set<Tuple> tuples = new HashSet<Tuple>();

		try {

			//			if (PStmtgetTuples == null){
			//				PStmtgetTuples = getConnection().prepareStatement(getTuplesString);
			//			}else
			//				PStmtgetTuples.clearParameters();

			PreparedStatement PStmtgetTuples = getConnection().prepareStatement(getTuplesString);

			PStmtgetTuples.setInt(1, sample.getId());

			ResultSet RSgetTuples = PStmtgetTuples.executeQuery();

			while (RSgetTuples.next()){

				int idDatabase = RSgetTuples.getInt(1);

				long idDocument = RSgetTuples.getLong(2);

				if (documents.contains(new Document(getDatabase(idDatabase), idDocument)))
					tuples.add(TupleReader.generateTuple(RSgetTuples.getString(3)));

			}

			RSgetTuples.close();

			PStmtgetTuples.close();

		} catch (SQLException e) {
			return null;
		}

		return tuples;

	}

	@Override
	public boolean hasGeneratedFullSample(
			SampleBuilderParameters sampleBuilderParameters, Sample sample) {

		try {

			//			if (PStmthasGeneratedFullSample == null){
			//				PStmthasGeneratedFullSample = getConnection().prepareStatement(hasGeneratedFullSampleString);
			//			}else
			//				PStmthasGeneratedFullSample.clearParameters();

			PreparedStatement PStmthasGeneratedFullSample = getConnection().prepareStatement(hasGeneratedFullSampleString);

			PStmthasGeneratedFullSample.setInt(1, sample.getId());
			PStmthasGeneratedFullSample.setBoolean(2, sampleBuilderParameters.getUnique());
			PStmthasGeneratedFullSample.setBoolean(3, sampleBuilderParameters.getLowerCase());
			PStmthasGeneratedFullSample.setBoolean(4, sampleBuilderParameters.getStemmed());

			PStmthasGeneratedFullSample.execute();

			PStmthasGeneratedFullSample.close();

		} catch (SQLException e) {
			return true; //duplicated key, someone has already written it.
		}

		return false;


	}

	@Override
	public boolean hasGeneratedSample(
			SampleBuilderParameters sampleBuilderParameters, Sample sample) {

		try {

			//			if (PStmthasGeneratedSample == null){
			//				PStmthasGeneratedSample = getConnection().prepareStatement(hasGeneratedSampleString);
			//			}else
			//				PStmthasGeneratedSample.clearParameters();

			PreparedStatement PStmthasGeneratedSample = getConnection().prepareStatement(hasGeneratedSampleString);

			PStmthasGeneratedSample.setInt(1, sample.getId());
			PStmthasGeneratedSample.setInt(2, sampleBuilderParameters.getId());
			PStmthasGeneratedSample.setInt(3, sample.getDatabase().getId());

			PStmthasGeneratedSample.execute();

			PStmthasGeneratedSample.close();

		} catch (SQLException e) {
			return true; //duplicated key, someone has already written it.
		}

		return false;


	}

	@Override
	public List<String> getSampleFilteredDocuments(Sample sample,
			SampleBuilderParameters sampleBuilderParameters) {

		List<String> documents = new ArrayList<String>();

		try {

			//			if (PStmtgetSampleFilteredDocuments == null){
			//				PStmtgetSampleFilteredDocuments = getConnection().prepareStatement(getSampleFilteredDocumentsString);
			//			}else
			//				PStmtgetSampleFilteredDocuments.clearParameters();

			PreparedStatement PStmtgetSampleFilteredDocuments = getConnection().prepareStatement(getSampleFilteredDocumentsString);

			PStmtgetSampleFilteredDocuments.setInt(1, sample.getId());
			PStmtgetSampleFilteredDocuments.setInt(2, sampleBuilderParameters.getId());

			ResultSet RSgetSampleFilteredDocuments = PStmtgetSampleFilteredDocuments.executeQuery();

			while (RSgetSampleFilteredDocuments.next()){

				documents.add(RSgetSampleFilteredDocuments.getString(1));

			}

			RSgetSampleFilteredDocuments.close();
			PStmtgetSampleFilteredDocuments.close();

		} catch (SQLException e) {
			return null;
		}

		return documents;

	}

	@Override
	public void writeFilteredDocuments(Sample sample,
			SampleBuilderParameters sp, int uselessSample, List<Document> filteredDocuments) {

		try {

			//			if (PStmtwriteFilteredDocuments == null){
			//				PStmtwriteFilteredDocuments = getConnection().prepareStatement(writeFilteredDocumentsString);
			//			}else
			//				PStmtwriteFilteredDocuments.clearParameters();

			PreparedStatement PStmtwriteFilteredDocuments = getConnection().prepareStatement(writeFilteredDocumentsString);

			for (int j = 0; j < filteredDocuments.size(); j++) {

				PStmtwriteFilteredDocuments.setInt(1, sample.getId());

				PStmtwriteFilteredDocuments.setBoolean(2, sp.getLowerCase());
				PStmtwriteFilteredDocuments.setBoolean(3, sp.getStemmed());
				PStmtwriteFilteredDocuments.setBoolean(4, sp.getUnique());
				PStmtwriteFilteredDocuments.setBoolean(5, sp.getTuplesAsStopWords());
				PStmtwriteFilteredDocuments.setInt(6, uselessSample);
				PStmtwriteFilteredDocuments.setInt(7, sp.getUsefulDocuments());
				
				PStmtwriteFilteredDocuments.setInt(8, sp.getUselessDocuments());
				
				PStmtwriteFilteredDocuments.setInt(9, j+1);

				PStmtwriteFilteredDocuments.setInt(10, filteredDocuments.get(j).getDatabase().getId());

				PStmtwriteFilteredDocuments.setLong(11, filteredDocuments.get(j).getId());

				PStmtwriteFilteredDocuments.addBatch();

			}

			PStmtwriteFilteredDocuments.executeBatch();
			PStmtwriteFilteredDocuments.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public List<Integer> getNotDoneSampleIds(int idWorkload, int idVersion, int idExtractor, int idRelationConfiguration, SampleBuilderParameters sampleBuilder, int idDatabase){

		Set<Integer> done = getDoneSamples(sampleBuilder.getId(),idDatabase,idWorkload,idVersion,idExtractor,idRelationConfiguration);

		List<Integer> all = getAllSamples(idWorkload,idVersion,idExtractor,idRelationConfiguration,idDatabase,sampleBuilder.getUsefulDocuments());

		all.removeAll(done);

		return all;
	}

	private List<Integer> getAllSamples(int idWorkload, int idVersion, int idExtractor, int idRelationConfiguration, int idDatabase, int usefulDocuments) {

		List<Integer> samples = new ArrayList<Integer>();

		try {

			//			if (PStmtgetAllSamples == null){
			//				PStmtgetAllSamples = getConnection().prepareStatement(getAllSamplesString);
			//			}else
			//				PStmtgetAllSamples.clearParameters();

			PreparedStatement PStmtgetAllSamples = getConnection().prepareStatement(getAllSamplesString);

			PStmtgetAllSamples.setInt(1, idDatabase);
			PStmtgetAllSamples.setInt(2, idVersion);
			PStmtgetAllSamples.setInt(3, idWorkload);
			PStmtgetAllSamples.setInt(4, usefulDocuments);
			PStmtgetAllSamples.setInt(5, idVersion);
			PStmtgetAllSamples.setInt(6, idWorkload);
			PStmtgetAllSamples.setInt(7, idRelationConfiguration);
			PStmtgetAllSamples.setInt(8, idExtractor);
			PStmtgetAllSamples.setInt(9, usefulDocuments);

			ResultSet RSgetAllSamples = PStmtgetAllSamples.executeQuery();

			while (RSgetAllSamples.next()){

				samples.add(RSgetAllSamples.getInt(1));

			}

			RSgetAllSamples.close();
			PStmtgetAllSamples.close();

		} catch (SQLException e) {
			return null;
		}

		return samples;


	}

	@Override
	public Set<Integer> getDoneSamples(int idSampleBuilder, int idDatabase, int idWorkload, int idVersion, int idExtractor, int idRelationConfiguration) {

		Set<Integer> samples = new HashSet<Integer>();

		try {

			//			if (PStmtgetDoneSamples == null){
			//				PStmtgetDoneSamples = getConnection().prepareStatement(getDoneSamplesString);
			//			}else
			//				PStmtgetDoneSamples.clearParameters();

			PreparedStatement PStmtgetDoneSamples = getConnection().prepareStatement(getDoneSamplesString);
			PStmtgetDoneSamples.setInt(1, idDatabase);
			PStmtgetDoneSamples.setInt(2, idSampleBuilder);
			PStmtgetDoneSamples.setInt(3, idWorkload);
			PStmtgetDoneSamples.setInt(4, idRelationConfiguration);
			PStmtgetDoneSamples.setInt(5, idVersion);
			PStmtgetDoneSamples.setInt(6, idExtractor);
			

			ResultSet RSgetDoneSamples = PStmtgetDoneSamples.executeQuery();

			while (RSgetDoneSamples.next()){

				samples.add(RSgetDoneSamples.getInt(1));

			}

			RSgetDoneSamples.close();
			PStmtgetDoneSamples.close();

		} catch (SQLException e) {
			return null;
		}

		return samples;

	}

	@Override
	public String getAttributeWithLargeDomain(String relation) {

		String ret = null;

		int id = getRelationshipTable().get(relation);

		try {

			//			if (PStmtgetAttributeWithLargeDomain == null){
			//				PStmtgetAttributeWithLargeDomain = getConnection().prepareStatement(getAttributeWithLargeDomainString);
			//			}else
			//				PStmtgetAttributeWithLargeDomain.clearParameters();

			PreparedStatement PStmtgetAttributeWithLargeDomain = getConnection().prepareStatement(getAttributeWithLargeDomainString);

			PStmtgetAttributeWithLargeDomain.setInt(1, id);

			ResultSet RSgetAttributeWithLargeDomain = PStmtgetAttributeWithLargeDomain.executeQuery();

			while (RSgetAttributeWithLargeDomain.next()){

				ret = RSgetAttributeWithLargeDomain.getString(1);

			}

			RSgetAttributeWithLargeDomain.close();
			PStmtgetAttributeWithLargeDomain.close();

		} catch (SQLException e) {
			return null;
		}

		return ret;

	}

	@Override
	public String getAttributeWithSmallDomain(String relation) {

		String ret = null;

		int id = getRelationshipTable().get(relation);

		try {

			//			if (PStmtgetAttributeWithSmallDomain == null){
			//				PStmtgetAttributeWithSmallDomain = getConnection().prepareStatement(getAttributeWithSmallDomainString);
			//			}else
			//				PStmtgetAttributeWithSmallDomain.clearParameters();

			PreparedStatement PStmtgetAttributeWithSmallDomain = getConnection().prepareStatement(getAttributeWithSmallDomainString);

			PStmtgetAttributeWithSmallDomain.setInt(1, id);

			ResultSet RSgetAttributeWithSmallDomain = PStmtgetAttributeWithSmallDomain.executeQuery();

			while (RSgetAttributeWithSmallDomain.next()){

				ret = RSgetAttributeWithSmallDomain.getString(1);

			}

			RSgetAttributeWithSmallDomain.close();
			PStmtgetAttributeWithSmallDomain.close();
			
		} catch (SQLException e) {
			return null;
		}

		return ret;

	}

	@Override
	public void writeSampleConfigurations(List<int[]> sc) {

		try {

			//			if (PStmtwriteSampleConfigurations == null){
			//				PStmtwriteSampleConfigurations = getConnection().prepareStatement(writeSampleConfigurationsString);
			//			}else
			//				PStmtwriteSampleConfigurations.clearParameters();

			PreparedStatement PStmtwriteSampleConfigurations = getConnection().prepareStatement(writeSampleConfigurationsString);

			for (int i = 0; i < sc.size(); i++){

				PStmtwriteSampleConfigurations.setInt(1, sc.get(i)[0]); //parameter

				PStmtwriteSampleConfigurations.setInt(2, sc.get(i)[1]); //version

				PStmtwriteSampleConfigurations.setInt(3, sc.get(i)[2]); //workload

				PStmtwriteSampleConfigurations.setInt(4, sc.get(i)[3]); //relationConfiguration

				PStmtwriteSampleConfigurations.setInt(5, sc.get(i)[4]); //extractionsystem

				PStmtwriteSampleConfigurations.setInt(6, sc.get(i)[5]); //qpe

				PStmtwriteSampleConfigurations.setInt(7, sc.get(i)[6]); //sg

				PStmtwriteSampleConfigurations.setInt(8, sc.get(i)[7]); //ua

				PStmtwriteSampleConfigurations.setInt(9, sc.get(i)[8]); //active

				PStmtwriteSampleConfigurations.setInt(10, sc.get(i)[9]); //rpq

				PStmtwriteSampleConfigurations.setInt(11, sc.get(i)[10]); //uf

				PStmtwriteSampleConfigurations.setInt(12, sc.get(i)[11]); //ul

				PStmtwriteSampleConfigurations.setInt(13, sc.get(i)[12]); //maxq

				PStmtwriteSampleConfigurations.setInt(14, sc.get(i)[13]); //maxd

				PStmtwriteSampleConfigurations.setInt(15, sc.get(i)[14]); //countAll

				PStmtwriteSampleConfigurations.addBatch();

				if ((i % 100) == 0){

					System.out.println("writing: " + i );

					PStmtwriteSampleConfigurations.executeBatch();

				}

			}

			PStmtwriteSampleConfigurations.executeBatch();

			PStmtwriteSampleConfigurations.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

	}


	@Override
	public String writeInternalTuples(int idDatabase, int idExtractionSystem, String content, long time) {

		try {

			//			if (PStmtwriteInternalTuples == null){
			//				PStmtwriteInternalTuples = getConnection().prepareStatement(writeInternalTuplesString,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtwriteInternalTuples.clearParameters();

			PreparedStatement PStmtwriteInternalTuples = getConnection().prepareStatement(writeInternalTuplesString,Statement.RETURN_GENERATED_KEYS);

			PStmtwriteInternalTuples.setInt(1, idDatabase);
			PStmtwriteInternalTuples.setInt(2, idExtractionSystem);
			PStmtwriteInternalTuples.setLong(3, time);
			PStmtwriteInternalTuples.setString(4,content);

			PStmtwriteInternalTuples.execute();

			ResultSet rs = PStmtwriteInternalTuples.getGeneratedKeys();
			int key = -1;

			if (rs != null && rs.next()) {
				key = rs.getInt(1);
			}

			PStmtwriteInternalTuples.close();
			rs.close();
			return Integer.toString(key);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;

	}

	private String prepareInternalTuples(int idDatabase, int idExtractionSystem, String content, long time) {

		try {

			accessInternalTuples.acquire();

			if (PStmtwriteInternalTuplesIds == null){
				PStmtwriteInternalTuplesIds = getConnection().prepareStatement(writeInternalTuplesString,Statement.RETURN_GENERATED_KEYS);
			}

			PStmtwriteInternalTuplesIds.setInt(1, idDatabase);
			PStmtwriteInternalTuplesIds.setInt(2, idExtractionSystem);
			PStmtwriteInternalTuplesIds.setLong(3, time);
			PStmtwriteInternalTuplesIds.setString(4,content);

			PStmtwriteInternalTuplesIds.addBatch();

			accessInternalTuples.release();

		} catch (SQLException e) {
			accessInternalTuples.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;

	}

	//	private synchronized int getNewInternalTuple() {
	//
	//		int res = 0;
	//
	//		try {
	//			
	//			StmtgetNewInternalTuple = getConnection().createStatement();
	//
	//			RSgetNewInternalTuple = StmtgetNewInternalTuple.executeQuery
	//					("SELECT LAST_INSERT_ID()");
	//
	//			while (RSgetNewInternalTuple.next()) {
	//
	//				res = RSgetNewInternalTuple.getInt(1);
	//
	//			}
	//			RSgetNewInternalTuple.close();
	//			
	//			StmtgetNewInternalTuple.close();
	//
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//
	//		return res;
	//
	//	}

	@Override
	public Map<String, String> loadDatabaseExtractions(Database db, int idExtractionSystem) {


		Map<String, String> res = new HashMap<String, String>();

		try {

			Statement StmtloadDatabaseExtractions = getConnection().createStatement();

			String loadDatabaseExtractionsString = "SELECT idInternalTupleExtraction,content FROM AutomaticQueryGeneration.InternalTupleExtraction where idDatabase = " + db.getId() + " AND idExtractionSystem = " + idExtractionSystem;

			ResultSet RSloadDatabaseExtractionse = StmtloadDatabaseExtractions.executeQuery(loadDatabaseExtractionsString);

			while (RSloadDatabaseExtractionse.next()) {

				res.put(Integer.toString(RSloadDatabaseExtractionse.getInt(1)),RSloadDatabaseExtractionse.getString(2));

			}
			RSloadDatabaseExtractionse.close();

			StmtloadDatabaseExtractions.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;


	}

	@Override
	public Map<Integer, List<String>> getInternalTupleMap() {

		Map<Integer, List<String>> res = new HashMap<Integer, List<String>>();

		try {

			Statement StmtgetInternalTupleMap = getConnection().createStatement();

			ResultSet RSgetInternalTupleMap = StmtgetInternalTupleMap.executeQuery(getInternalTupleMapString);

			while (RSgetInternalTupleMap.next()) {

				Integer db = RSgetInternalTupleMap.getInt(1);

				List<String> l = res.get(db);

				if (l == null){
					l = new ArrayList<String>();
					res.put(db, l);
				}

				l.add(RSgetInternalTupleMap.getString(2));

			}
			RSgetInternalTupleMap.close();

			StmtgetInternalTupleMap.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;

	}

	@Override
	public void finishBatchDownloader(int idDatabase) {

		//RawResult
		try {
			synchronized(prepareRawResultPage){
				if (prepareRawResultPage.get(idDatabase) != null)
					prepareRawResultPage.get(idDatabase).executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("RawResultPage");

		//QueryTime
		try {
			synchronized(prepareQueryTimeTable){
				if (prepareQueryTimeTable.get(idDatabase)!=null)
					prepareQueryTimeTable.get(idDatabase).executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("QueryTime");
		//ExtractedResult
		try {
			synchronized (prepareExtractedResultTable) {
				if (prepareExtractedResultTable.get(idDatabase) != null)
					prepareExtractedResultTable.get(idDatabase).executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("QueryResults");
		//ExtractedResultPage
		try {
			synchronized (prepareExtractedResultPageTable) {
				if (prepareExtractedResultPageTable.get(idDatabase) != null)
					prepareExtractedResultPageTable.get(idDatabase).executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("ExtractedResultPage");
		//InsertDocument
		try {
			synchronized (prepareStoredDownloadedDocumentTable) {
				if (prepareStoredDownloadedDocumentTable.get(idDatabase) != null)
					prepareStoredDownloadedDocumentTable.get(idDatabase).executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("WebDocument");
	}

	@Override
	public void prepareRawResultPage(int expId, int idDatabase, TextQuery texQuery,
			String navigationTechnique, int page) {


		long qId = getTextQuery(texQuery);

		try {

			PreparedStatement ps;

			synchronized (prepareRawResultPage) {

				ps =  prepareRawResultPage.get(idDatabase);

				if (ps == null){
					ps = getConnection().prepareStatement(saveRawResultPageString);
					prepareRawResultPage.put(idDatabase,ps);
				}


			}


			ps.setInt(1, expId);
			ps.setInt(2, idDatabase);
			ps.setLong(3, qId);
			ps.setInt(4, getNavigationTechniqueId(navigationTechnique));
			ps.setInt(5, page);

			ps.addBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}		

	}

	@Override
	public void prepareQueryTime(int expId, int idDatabase, TextQuery texQuery,
			int page, long time) {

		PreparedStatement ps; 

		long qId = getTextQuery(texQuery);

		try {

			synchronized (prepareQueryTimeTable) {

				ps =  prepareQueryTimeTable.get(idDatabase);

				if (ps == null){
					ps = getConnection().prepareStatement(insertQueryTimeString);
					prepareQueryTimeTable.put(idDatabase,ps);
				}


			}

			ps.setInt(1, expId);
			ps.setInt(2, idDatabase);
			ps.setLong(3, qId);
			ps.setInt(4, page);
			ps.setLong(5, time);

			ps.addBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void prepareExtractedResult(int experimentId, String extractionTechnique, String navigationTechnique,
			String rdhName, TextQuery query, Document document, int resultPage,
			int resultIndex) {

		PreparedStatement ps;

		Long qId = getTextQuery(query);

		try {

			synchronized (prepareExtractedResultTable) {

				ps =  prepareExtractedResultTable.get(document.getDatabase().getId());

				if (ps == null){
					ps = getConnection().prepareStatement(saveExtractedResultString);
					prepareExtractedResultTable.put(document.getDatabase().getId(),ps);
				}


			}

			ps.setInt(1, experimentId);
			ps.setInt(2, document.getDatabase().getId());
			ps.setLong(3, qId);
			ps.setLong(4, document.getId());
			ps.setInt(5, resultPage);
			ps.setInt(6, resultIndex);
			ps.setInt(7, getExtractionTechniqueId(extractionTechnique));
			ps.setInt(8, getNavigationTechniqueId(navigationTechnique));
			ps.setInt(9, getResultExtractionTechniqueId(rdhName));

			ps.addBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void prepareStoredDownloadedDocument(Document document, boolean sucess) {

		PreparedStatement ps;

		try {

			synchronized(prepareStoredDownloadedDocumentTable){

				ps =  prepareStoredDownloadedDocumentTable.get(document.getDatabase().getId());

				if (ps == null){
					ps = getConnection().prepareStatement(insertRetrievedURLString);
					prepareStoredDownloadedDocumentTable.put(document.getDatabase().getId(),ps);
				}

			}

			ps.setInt(1, document.getDatabase().getId());
			ps.setInt(2, document.getExperimentId());
			ps.setLong(3, document.getId());
			ps.setString(4, document.getFilePath(this).getAbsolutePath());
			ps.setString(5, document.getURL(this).toString());
			ps.setLong(6, document.getDownloadTime());
			ps.setBoolean(7, document.isSuccessful());

			ps.addBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void prepareExtractedResultPage(int experimentId, int idDatabase,
			TextQuery query, String extractionTechnique,
			String navigationTechnique, int resultPage) {

		PreparedStatement ps;

		long qId = getTextQuery(query);

		try {

			synchronized (prepareExtractedResultPageTable) {

				ps = prepareExtractedResultPageTable.get(idDatabase);

				if (ps == null){
					ps = getConnection().prepareStatement(saveExtractedResultPageString);
					prepareExtractedResultPageTable.put(idDatabase,ps);
				}

			}

			ps.setInt(1, experimentId);
			ps.setInt(2, idDatabase);
			ps.setLong(3, qId);
			ps.setInt(4, getExtractionTechniqueId(extractionTechnique));
			ps.setInt(5, getNavigationTechniqueId(navigationTechnique));
			ps.setInt(6, resultPage);

			ps.addBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void prepareES(int idDatabase, int idExperiment, int status,
			int host) {
		try {

			if (PStmtprepareES == null){
				PStmtprepareES = getConnection().prepareStatement(testTableString,Statement.RETURN_GENERATED_KEYS);
			}

			PStmtprepareES.setInt(1, host);
			PStmtprepareES.setInt(2, idDatabase);
			PStmtprepareES.setInt(3, idExperiment);
			PStmtprepareES.setInt(4, status);

			PStmtprepareES.addBatch();

		} catch (SQLException e) {

			e.printStackTrace();

		}

	}

	@Override
	public void batch() {

		try {

			PStmtprepareES.executeBatch();

			System.out.println("continutes");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Left the thread");
		}

		ResultSet rs;
		try {
			rs = PStmtprepareES.getGeneratedKeys();

			if (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int colCount = rsmd.getColumnCount();
				do {
					for (int i = 1; i <= colCount; i++) {
						String key = rs.getString(i);
						System.out.println("key " + i + "is " + key);
					}
				}
				while (rs.next());
			} 
			else {
				System.out.println("There are no generated keys.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}



	}

	@Override
	public DocumentHandler getDocumentHandler(Database database,
			int experimentId) {

		DocumentHandler docH = getDocumentHandlerMap(database).get(experimentId);

		if (docH == null){

			docH = new DocumentHandler(database,experimentId,this);
			getDocumentHandlerMap(database).put(experimentId, docH);

		}

		return docH;
	}

	private Map<Integer,DocumentHandler> getDocumentHandlerMap(Database database) {

		synchronized (dhMap) {

			Map<Integer,DocumentHandler> map = dhMap.get(database.getId());

			if (map == null){
				map = new HashMap<Integer, DocumentHandler>();
				dhMap.put(database.getId(), map);
			}

			return map;

		}

	}

	@Override
	public Collection<DocumentHandler> getDocumentHandler(Database database) {
		return getDocumentHandlerMap(database).values();
	}

	@Override
	public void finishNegativeBatchDownloader(int idDatabase) {

		//InsertNegativeEntry
		try {

			synchronized (prepareNegativeSampleEntryTable) {
				if (prepareNegativeSampleEntryTable.get(idDatabase) != null)
					prepareNegativeSampleEntryTable.get(idDatabase).executeBatch();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("NegativeSampling");

		finishBatchDownloader(idDatabase);

	}

	@Override
	public void InitializeExperimentStatus(ExperimentEnum experiment,
			int idDatabase, String computerName) {

		isExperimentAvailable(getExperiment(experiment), idDatabase, computerName);

	}

	@Override
	public void reportExperimentStatus(ExperimentEnum experiment, int idDatabase,
			String computerName, int status) {
		reportExperimentStatus(getExperiment(experiment), idDatabase, computerName, status);

	}

	@Override
	public void removeInformAsNotProcessed(int searchRoundId,
			List<Integer> toRemoveQueries, Database database,
			String extractionTechnique, String navigationTechnique,
			String resultExtractionTechnique) {

		toRemoveQueries = selectQueriesToRemove(toRemoveQueries, database);

		if (toRemoveQueries.isEmpty())
			return;

		String IN = generateIn(toRemoveQueries);

		String removeInformAsNotProcessedRawResult = "DELETE FROM RawResultPage where idExperiment = " + searchRoundId + " and idDatabase = " + database.getId() + " and navigationTechnique = " + getNavigationTechniqueId(navigationTechnique) + " AND idQuery IN " + IN;

		performTransaction(removeInformAsNotProcessedRawResult);

		String removeInformAsNotProcessedExtractedResult = "DELETE FROM ExtractedResultPage where idExperiment = " + searchRoundId + " and idDatabase = " + database.getId() + " and idNavigationTechnique = " + getNavigationTechniqueId(navigationTechnique) + " and idExtractionTechnique = " + getExtractionTechniqueId(extractionTechnique)  + " AND idQuery IN " + IN;

		performTransaction(removeInformAsNotProcessedExtractedResult);

		String removeInformAsNotProcessedQueryResult = "DELETE FROM QueryResults where idExperiment = " + searchRoundId + " and idDatabase = " + database.getId() + " and navigationTechnique = " + getNavigationTechniqueId(navigationTechnique) + " and extractionTechnique = " + getExtractionTechniqueId(extractionTechnique) + " and resultExtractionTechnique = " + getResultExtractionTechniqueId(resultExtractionTechnique)  + " AND idQuery IN " + IN;

		performTransaction(removeInformAsNotProcessedQueryResult);

		String removeInformAsNotProcessedQueryTime = "DELETE FROM QueryTime where idExperiment = " + searchRoundId + " and idDatabase = " + database.getId() + " AND idQuery IN " + IN;

		performTransaction(removeInformAsNotProcessedQueryTime);


	}

	private List<Integer> selectQueriesToRemove(List<Integer> queries, Database database) {

		//Find from Positive the ones to keep

		String IN = generateIn(queries);

		List<Integer> auxPos = new ArrayList<Integer>();

		try {

			Statement StmtselectQueriesToRemove = getConnection().createStatement();

			ResultSet RSselectQueriesToRemove = StmtselectQueriesToRemove.executeQuery
					("select distinct idQuery from QueryConsistency Q join ExperimentStatus E on (Q.idDatabase = E.idDatabase AND Q.idConsistency = E.idExperiment AND idQuery < status) where Q.idDatabase = " + database.getId() + " AND idQuery IN " + IN);

			while (RSselectQueriesToRemove.next()) {

				auxPos.add(RSselectQueriesToRemove.getInt(1));

			}

			RSselectQueriesToRemove.close();

			StmtselectQueriesToRemove.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		//Find the ones to keep from Negative.

		List<Pair<Integer,Integer>> consistNega = getNegativeConsistencies(database,queries);

		for (int i = 0; i < consistNega.size(); i++) {

			auxPos.addAll(getNegativeQueriesToKeep(database,consistNega.get(i).getFirst(),consistNega.get(i).getSecond()));

		}

		//Find the ones to keep from Tuple.

		try {

			Statement StmtselectQueriesToRemoveTuples = getConnection().createStatement();

			ResultSet RSselectQueriesToRemoveTuples = StmtselectQueriesToRemoveTuples.executeQuery
					("select * from QueryForTuples where idDatabase = "+database.getId()+" and position < (select position from QueryForTuples Q join " +
							"ExperimentStatus E on (Q.idDatabase = E.idDatabase) where E.idExperiment = "+getExperiment(getTupleConsistensy())+" and E.idDatabase = "+database.getId()+" and " +
							"idQuery = E.status);");

			while (RSselectQueriesToRemoveTuples.next()) {

				auxPos.add(RSselectQueriesToRemoveTuples.getInt(1));

			}

			RSselectQueriesToRemoveTuples.close();

			StmtselectQueriesToRemoveTuples.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		queries.removeAll(auxPos);

		return queries;

	}

	private List<Integer> getNegativeQueriesToKeep(
			Database database, int split, int lastQuery) {



		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetNegativeQueriesToKeep = getConnection().createStatement();

			ResultSet RSgetNegativeQueriesToKeep = StmtgetNegativeQueriesToKeep.executeQuery
					("select queryId from NegativeSampling where idDatabase="+database.getId()+" and split = "+split+" and position <= (select position from NegativeSampling where idDatabase = "+database.getId()+" and split = "+split+" and queryId = "+lastQuery+") order by position");

			while (RSgetNegativeQueriesToKeep.next()) {

				ret.add(RSgetNegativeQueriesToKeep.getInt(1));

			}

			RSgetNegativeQueriesToKeep.close();

			StmtgetNegativeQueriesToKeep.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	private List<Pair<Integer,Integer>> getNegativeConsistencies(Database database,
			List<Integer> queries) {

		List<Pair<Integer,Integer>> ret = new ArrayList<Pair<Integer,Integer>>();

		String IN = generateIn(queries);

		try {

			Statement StmtgetNegativeConsistencies = getConnection().createStatement();

			ResultSet RSgetNegativeConsistencies = StmtgetNegativeConsistencies.executeQuery
					("select distinct idConsistency,status from NegativeConsistency N join ExperimentStatus E on (N.idDatabase = E.idDatabase AND N.idConsistency = E.idExperiment) where E.idDatabase = "+database.getId()+" AND idQuery IN " + IN);

			while (RSgetNegativeConsistencies.next()) {

				ret.add(new Pair<Integer,Integer>(getSplit(RSgetNegativeConsistencies.getInt(1)),RSgetNegativeConsistencies.getInt(2)));

			}

			RSgetNegativeConsistencies.close();

			StmtgetNegativeConsistencies.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	private int getSplit(int experimentValue) {

		switch (experimentValue) {
		case NEGATIVE_CONSISTENSY_VALUE_1:
			return 1;
		case NEGATIVE_CONSISTENSY_VALUE_2:
			return 2;
		case NEGATIVE_CONSISTENSY_VALUE_3:
			return 3;
		case NEGATIVE_CONSISTENSY_VALUE_4:
			return 4;
		case NEGATIVE_CONSISTENSY_VALUE_5:
			return 5;
		default:
			return -1;
		}

	}

	@Override
	public int getExperimentConsistensyId(
			ExperimentEnum experimentConsistensyId, int split) {

		return getExperiment(experimentConsistensyId) - split + 1;

	}

	@Override
	public void insertInteractionError(int idDatabase,int idExperiment) {

		String insertInteractionErrorString = "INSERT INTO `AutomaticQueryGeneration`.`InteractionError` (`idExperiment`,`idDatabase`) VALUES ("+idExperiment+","+idDatabase+");";

		performTransaction(insertInteractionErrorString);

		trick("InteractionError");

	}

	@Override
	public void writeExperimentSplit(int split, int idDatabase) {

		String writeExperimentSplitString = "INSERT INTO `AutomaticQueryGeneration`.`DatabaseExperimentSet` (`experimentSplit`, `idDatabase`) VALUES ( "+split+", "+idDatabase+" )";

		performTransaction(writeExperimentSplitString);

	}

	@Override
	public void updateExperimentSplit(int split, int idDatabase) {

		String updateExperimentSplitString = "UPDATE `AutomaticQueryGeneration`.`DatabaseExperimentSet` SET `experimentSplit` = " + split  + " WHERE `idDatabase` = " +idDatabase;

		performTransaction(updateExperimentSplitString);

	}

	@Override
	public void reportInteractionError(int idDatabase, int idExperiment) {

		try {

			//			if (PStmtreportInteractionError == null){
			//				PStmtreportInteractionError = getConnection().prepareStatement(reportInteractionErrorString);
			//			}else 
			//				PStmtreportInteractionError.clearParameters();

			PreparedStatement PStmtreportInteractionError = getConnection().prepareStatement(reportInteractionErrorString);

			PStmtreportInteractionError.setInt(1, idExperiment);
			PStmtreportInteractionError.setInt(2, idDatabase);

			PStmtreportInteractionError.execute();

			PStmtreportInteractionError.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

	}

	@Override
	public List<Integer> loadQueriesforSampleRelation(int searchRoundId,
			Database database, String navigationTechnique, int relationConf, int minQuery, int maxQuery) {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtloadQueriesforSampleRelation = getConnection().createStatement();

			ResultSet RSloadQueriesforSampleRelation = StmtloadQueriesforSampleRelation.executeQuery
					("select idTextQuery from TextQueryRelationshipConfiguration where idRelationConfiguration = "+relationConf+" and idTextQuery > "+minQuery+" AND idTextQuery <= "+maxQuery+" order by idTextQuery");

			while (RSloadQueriesforSampleRelation.next()) {

				ret.add(RSloadQueriesforSampleRelation.getInt(1));

			}

			RSloadQueriesforSampleRelation.close();

			StmtloadQueriesforSampleRelation.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;


	}

	@Override
	public void reportQueryConsistency(int idDatabase, int idQuery,
			ExperimentEnum consistensy) {

		try {

			//			if (PStmtreportQueryConsistency == null){
			//				PStmtreportQueryConsistency = getConnection().prepareStatement(reportQueryConsistencyString);
			//			}else
			//				PStmtreportQueryConsistency.clearParameters();

			PreparedStatement PStmtreportQueryConsistency = getConnection().prepareStatement(reportQueryConsistencyString);

			PStmtreportQueryConsistency.setInt(1, idDatabase);
			PStmtreportQueryConsistency.setInt(2, idQuery);
			PStmtreportQueryConsistency.setInt(3, getExperiment(consistensy));

			PStmtreportQueryConsistency.execute();

			PStmtreportQueryConsistency.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		trick("QueryConsistency");

	}

	@Override
	public void reportNegativeQueryConsistency(int idDatabase, int idQuery,
			ExperimentEnum consistensy) {

		try {

			//			if (PStmtreportNegativeQueryConsistency == null){
			//				PStmtreportNegativeQueryConsistency = getConnection().prepareStatement(reportNegativeQueryConsistencyString);
			//			}else
			//				PStmtreportNegativeQueryConsistency.clearParameters();

			PreparedStatement PStmtreportNegativeQueryConsistency = getConnection().prepareStatement(reportNegativeQueryConsistencyString);

			PStmtreportNegativeQueryConsistency.setInt(1, idDatabase);
			PStmtreportNegativeQueryConsistency.setInt(2, idQuery);
			PStmtreportNegativeQueryConsistency.setInt(3, getExperiment(consistensy));

			PStmtreportNegativeQueryConsistency.execute();

			PStmtreportNegativeQueryConsistency.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		trick("NegativeConsistency");

	}

	@Override
	public int getLastSentQuery(int searchRoundId, Database database,
			String navigationTechnique, int relation) {

		int ret = 0;

		try {

			Statement StmtgetLastSentQuery = getConnection().createStatement();

			ResultSet RSgetLastSentQuery = StmtgetLastSentQuery.executeQuery
					("select max(idQuery) from QueryConsistency where idDatabase = "+database.getId()+" and idConsistency = " + getExperiment(getConsistensy(relation)));

			while (RSgetLastSentQuery.next()) {

				ret = RSgetLastSentQuery.getInt(1);

			}

			RSgetLastSentQuery.close();

			StmtgetLastSentQuery.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;


	}

	@Override
	public List<Integer> loadIdQueriesForTuple(int searchRoundId,
			Database database, int lastStoredQuery) {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			//			if (PStmtloadIdQueriesForTuple == null){
			//				PStmtloadIdQueriesForTuple = getConnection().prepareStatement(loadIdQueriesForTupleString);
			//			}else
			//				PStmtloadIdQueriesForTuple.clearParameters();

			PreparedStatement PStmtloadIdQueriesForTuple = getConnection().prepareStatement(loadIdQueriesForTupleString);

			PStmtloadIdQueriesForTuple.setInt(1, searchRoundId);
			PStmtloadIdQueriesForTuple.setInt(2, database.getId());
			PStmtloadIdQueriesForTuple.setInt(3, searchRoundId);
			PStmtloadIdQueriesForTuple.setInt(4, database.getId());
			PStmtloadIdQueriesForTuple.setInt(5, lastStoredQuery);

			ResultSet RSloadIdQueriesForTuple = PStmtloadIdQueriesForTuple.executeQuery();

			while (RSloadIdQueriesForTuple.next()) {

				ret.add(RSloadIdQueriesForTuple.getInt(1));

			}

			RSloadIdQueriesForTuple.close();
			PStmtloadIdQueriesForTuple.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<Long> getProcessedDocumentsForCandidateSentences(
			Database database, int relationConf, ContentExtractor ce) {

		List<Long> ret = new ArrayList<Long>();

		try {

			//			if (PStmtgetProcessedDocumentsForCandidateSentences == null){
			//				PStmtgetProcessedDocumentsForCandidateSentences = getConnection().prepareStatement(getProcessedDocumentsForCandidateSentencesString);
			//			}else
			//				PStmtgetProcessedDocumentsForCandidateSentences.clearParameters();

			PreparedStatement PStmtgetProcessedDocumentsForCandidateSentences = getConnection().prepareStatement(getProcessedDocumentsForCandidateSentencesString);

			PStmtgetProcessedDocumentsForCandidateSentences.setInt(1, database.getId());
			PStmtgetProcessedDocumentsForCandidateSentences.setInt(2, getContentExtractorId(ce));
			PStmtgetProcessedDocumentsForCandidateSentences.setInt(3, relationConf);


			ResultSet RSgetProcessedDocumentsForCandidateSentences = PStmtgetProcessedDocumentsForCandidateSentences.executeQuery();

			while (RSgetProcessedDocumentsForCandidateSentences.next()) {

				ret.add(RSgetProcessedDocumentsForCandidateSentences.getLong(1));

			}

			RSgetProcessedDocumentsForCandidateSentences.close();

			PStmtgetProcessedDocumentsForCandidateSentences.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void persistEntities() {

		try {

			accessEntity.acquire();
			if (PStmtsaveEntity != null)
				PStmtsaveEntity.executeBatch();
			accessEntity.release();

		} catch (SQLException e) {
			accessEntity.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		trick("Entity");

		try {
			accessExtractedEntity.acquire();
			if (PStmtsaveExtractedEntity != null)
				PStmtsaveExtractedEntity.executeBatch();
			accessExtractedEntity.release();
		} catch (SQLException e) {
			accessExtractedEntity.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		trick("ExtractedEntity");
	}

	@Override
	public void prepareEntity(int idDatabase, long idDocument,
			ContentExtractor ce, int informationExtractionSystem,
			int entityType, int start, int end) {

		try {

			accessEntity.acquire();

			if (PStmtsaveEntity == null){
				PStmtsaveEntity = getConnection().prepareStatement(saveEntityString);
			}

			PStmtsaveEntity.setInt(1, idDatabase);
			PStmtsaveEntity.setLong(2, idDocument);
			PStmtsaveEntity.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveEntity.setInt(4, informationExtractionSystem);
			PStmtsaveEntity.setInt(5, entityType);
			PStmtsaveEntity.setInt(6, start);
			PStmtsaveEntity.setInt(7, end);

			PStmtsaveEntity.addBatch();

			accessEntity.release();

		} catch (SQLException e) {
			accessEntity.release();
			e.printStackTrace();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void prepareExtractedDocument(int idDatabase, long idDocument,
			ContentExtractor ce, int idInformationExtractor, int entityId,
			long time) {

		try {

			accessExtractedEntity.acquire();

			if (PStmtsaveExtractedEntity == null){
				PStmtsaveExtractedEntity = getConnection().prepareStatement(saveExtractedEntityString);
			}

			PStmtsaveExtractedEntity.setInt(1, idDatabase);
			PStmtsaveExtractedEntity.setLong(2, idDocument);
			PStmtsaveExtractedEntity.setInt(3, getContentExtractorId(ce));
			PStmtsaveExtractedEntity.setInt(4, idInformationExtractor);
			PStmtsaveExtractedEntity.setInt(5, entityId);
			PStmtsaveExtractedEntity.setLong(6, time);

			PStmtsaveExtractedEntity.addBatch();

			accessExtractedEntity.release();

		} catch (SQLException e) {
			accessExtractedEntity.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void persistCandidateSentences() {

		System.out.println("Writing Cand Sent");

		CandidatesSentenceWriter.writeCandidateSentences();

		System.out.println("Written Cand Sent");

		try {

			accessCandidateSentences.acquire();
			if (PStmtsaveCandidateSentenceGeneration != null)
				PStmtsaveCandidateSentenceGeneration.executeBatch();
			accessCandidateSentences.release();

		} catch (SQLException e) {
			e.printStackTrace();
			accessCandidateSentences.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		trick("GeneratedCandidateSentence");

		try {

			accessGeneratedCandidateSentence.acquire();
			if (PStmtsaveGeneratedCandidateSentence != null)
				PStmtsaveGeneratedCandidateSentence.executeBatch();

			accessGeneratedCandidateSentence.release();

		} catch (SQLException e) {
			accessGeneratedCandidateSentence.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		trick("CandidateSentence");
	}

	@Override
	public void prepareCandidateSentenceGeneration(int idDatabase,
			long idDocument, ContentExtractor ce, int relationConfigurationId,
			String file, long time, int size) {

		try {

			accessCandidateSentence.acquire();

			if (PStmtsaveCandidateSentenceGeneration == null){
				PStmtsaveCandidateSentenceGeneration = getConnection().prepareStatement(saveCandidateSentenceGenerationString);
			}

			PStmtsaveCandidateSentenceGeneration.setInt(1, idDatabase);
			PStmtsaveCandidateSentenceGeneration.setLong(2, idDocument);
			PStmtsaveCandidateSentenceGeneration.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveCandidateSentenceGeneration.setInt(4, relationConfigurationId);
			PStmtsaveCandidateSentenceGeneration.setString(5, format(file));
			PStmtsaveCandidateSentenceGeneration.setLong(6, time);
			PStmtsaveCandidateSentenceGeneration.setInt(7, size);

			PStmtsaveCandidateSentenceGeneration.addBatch();

			accessCandidateSentence.release();


		} catch (SQLException e) {
			accessCandidateSentence.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void prepareGeneratedCandidateSentence(int idDatabase,
			long idDocument, int relationConfigurationId, ContentExtractor ce) {

		try {

			accessGeneratedCandidateSentence.acquire();

			if (PStmtsaveGeneratedCandidateSentence == null){
				PStmtsaveGeneratedCandidateSentence = getConnection().prepareStatement(saveGeneratedCandidateSentenceString);
			}else
				PStmtsaveGeneratedCandidateSentence.clearParameters();

			PStmtsaveGeneratedCandidateSentence.setInt(1, idDatabase);
			PStmtsaveGeneratedCandidateSentence.setLong(2, idDocument);
			PStmtsaveGeneratedCandidateSentence.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveGeneratedCandidateSentence.setInt(4, relationConfigurationId);

			PStmtsaveGeneratedCandidateSentence.addBatch();

			accessGeneratedCandidateSentence.release();



		} catch (SQLException e) {
			accessGeneratedCandidateSentence.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void persistOperableStructure() {

		System.out.println("Writing Operable Structures");

		CoreWriter.writeOperableStructures();

		System.out.println("Written Operable Structures");

		try {

			accessOperableStructure.acquire();
			if (PStmtsaveOperableStructureGeneration != null)
				PStmtsaveOperableStructureGeneration.executeBatch();
			accessOperableStructure.release();


		} catch (SQLException e) {
			accessOperableStructure.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		trick("OperableStructure");

		try {

			accessGeneratedOperableStructure.acquire();
			if (PStmtsaveGeneratedOperableStructure != null)
				PStmtsaveGeneratedOperableStructure.executeBatch();
			accessGeneratedOperableStructure.release();

		} catch (SQLException e) {
			accessGeneratedOperableStructure.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		trick("GeneratedOperableStructure");


		try {

			accessExtraction.acquire();
			if (PStmtinsertExtraction != null)
				PStmtinsertExtraction.executeBatch();
			accessExtraction.release();

		} catch (SQLException e) {
			accessExtraction.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		trick("Extraction");

	}

	@Override
	public void prepareGeneratedOperableStructure(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId) {

		try {

			accessGeneratedOperableStructure.acquire();

			if (PStmtsaveGeneratedOperableStructure == null){
				PStmtsaveGeneratedOperableStructure = getConnection().prepareStatement(saveGeneratedOperableStructureString);
			}

			PStmtsaveGeneratedOperableStructure.setInt(1, document.getDatabase().getId());
			PStmtsaveGeneratedOperableStructure.setLong(2, document.getId());
			PStmtsaveGeneratedOperableStructure.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveGeneratedOperableStructure.setInt(4, relationConf);
			PStmtsaveGeneratedOperableStructure.setInt(5, informationExtractionId);

			PStmtsaveGeneratedOperableStructure.addBatch();

			accessGeneratedOperableStructure.release();

		} catch (SQLException e) {
			accessGeneratedOperableStructure.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void prepareOperableStructureGeneration(Document document,
			ContentExtractor ce, int relationConf, int informationExtractionId,
			String file, long time) {

		try {

			accessOperableStructure.acquire();

			if (PStmtsaveOperableStructureGeneration == null){
				PStmtsaveOperableStructureGeneration = getConnection().prepareStatement(saveOperableStructureGenerationString);
			}
			PStmtsaveOperableStructureGeneration.setInt(1, document.getDatabase().getId());
			PStmtsaveOperableStructureGeneration.setLong(2, document.getId());
			PStmtsaveOperableStructureGeneration.setInt(3, getContentExtractorId(ce)); 
			PStmtsaveOperableStructureGeneration.setInt(4, relationConf);
			PStmtsaveOperableStructureGeneration.setInt(5, informationExtractionId);
			PStmtsaveOperableStructureGeneration.setString(6, file);
			PStmtsaveOperableStructureGeneration.setLong(7, time);


			PStmtsaveOperableStructureGeneration.addBatch();

			accessOperableStructure.release();

		} catch (SQLException e) {
			accessOperableStructure.release();

			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void prepareExtraction(int relationExtractionSystemId,
			Document document, String file, ContentExtractor ce) {

		prepareExtraction(relationExtractionSystemId,document.getDatabase().getId(), document.getId(), file, getContentExtractorId(ce));

	}

	@Override
	public void persistTuple() {


		//		String ff = writeInternalTuples(database.getId(),Base64.encode(os.toByteArray()),time);

		String[] internalArray;

		List<Object[]> auxInternalData;

		synchronized (internalData){

			for(int i = 0;i < internalData.size();i++){
				Object[] array = internalData.get(i);
				prepareInternalTuples((Integer)array[1],(Integer)array[0],(String)array[3],(Long)array[4]);
			}

			internalArray = new String[internalData.size()];

			auxInternalData = new ArrayList<Object[]>(internalData);

			internalData.clear();

		}

		if (internalArray.length > 0){

			try {

				accessInternalTuples.acquire();

				PStmtwriteInternalTuplesIds.executeBatch();

				accessInternalTuples.release();

				trick("InternalTupleExtraction");

			} catch (SQLException e) {
				accessInternalTuples.release();

				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			ResultSet rs;
			try {

				accessInternalTuples.acquire();

				rs = PStmtwriteInternalTuplesIds.getGeneratedKeys();

				accessInternalTuples.release();

				int pos = 0;

				while (rs.next()){
					internalArray[pos] = rs.getString(1); //Because of the structure of the table InternalExtraction.
					pos++;
				}

				rs.close();

			} catch (SQLException e) {
				accessInternalTuples.release();
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			//			simulating pW.insertExtraction(tuplesRelationExtractionId, doc, ff, ce);

			//			synchronized (internalData) {
			//				
			for(int i = 0;i < auxInternalData.size();i++){
				Object[] array = auxInternalData.get(i);
				prepareExtraction((Integer)array[0],(Integer)array[1],(Long)array[2],internalArray[i],(Integer)array[5]);
				System.out.println("prepared Extraction..." + i);
			}

			//				internalData.clear();
			auxInternalData.clear();


			//			}

			try {

				accessExtraction.acquire();

				if (PStmtinsertExtraction != null){
					PStmtinsertExtraction.executeBatch();
				}

				accessExtraction.release();

			} catch (SQLException e) {
				accessExtraction.release();
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			trick("Extraction");

		}


	}

	private void prepareExtraction(int relationExtractionSystemId, int database,
			long document, String file, int ce) {

		try {

			accessExtraction.acquire();

			if (PStmtinsertExtraction == null){
				PStmtinsertExtraction = getConnection().prepareStatement(insertExtractionString);
			}

			PStmtinsertExtraction.setInt(1, relationExtractionSystemId);
			PStmtinsertExtraction.setLong(2, database);
			PStmtinsertExtraction.setLong(3, document);
			PStmtinsertExtraction.setString(4, file);
			PStmtinsertExtraction.setInt(5, ce);

			PStmtinsertExtraction.addBatch();

			accessExtraction.release();

		} catch (SQLException e) {
			accessExtraction.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void prepareInternalExtraction(int tuplesRelationExtractionId,
			Document doc, List<Tuple> tuples, long time, ContentExtractor ce) {

		String encode = generateEncode(tuples);

		if (encode.length() > 65000){

			encode = getLargeTuplesEncode(tuples.size());

		}

		synchronized (internalData) {
			internalData.add(new Object[]{tuplesRelationExtractionId,doc.getDatabase().getId(),doc.getId(),encode,time,getContentExtractorId(ce)});
		}

	}

	private String getLargeTuplesEncode(int size) {

		Tuple t = new Tuple();

		List<Tuple> tup = Arrays.asList(t);

		t.setTupleField("large", Integer.toString(size));

		return generateEncode(tup);

	}

	private String generateEncode(List<Tuple> tuples) {

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			ObjectOutput out = new ObjectOutputStream(os);

			out.writeObject(tuples);

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Base64.encode(os.toByteArray());

	}

	@Override
	public void finishSampleGeneration(int idSample) {

		try {

			PreparedStatement PStmtinsertSampleGenerationQuery;

			synchronized (isgqTable) {

				PStmtinsertSampleGenerationQuery = isgqTable.remove(idSample);

			}

			if (PStmtinsertSampleGenerationQuery != null)
				PStmtinsertSampleGenerationQuery.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("SampleGenerationQueries");

		try {

			PreparedStatement PStmtinsertSampleGeneration;

			synchronized (tpdTable) {

				PStmtinsertSampleGeneration = tpdTable.remove(idSample);

			}

			if (PStmtinsertSampleGeneration != null)
				PStmtinsertSampleGeneration.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		trick("SampleGeneration");

	}

	private void trick(String tableName) {

		//		if (tableName.equals("InternalTupleExtraction")){
		//			performTransaction("LOCK TABLE "+tableName+" WRITE");
		//			performTransaction("UNLOCK TABLES");
		//		}

	}

	@Override
	public List<Pair<Long, Pair<Integer, Integer>>> getExtractedEntities(
			int idDatabase, long idDocument, int idEntityType,
			int idInformationExtractionSystem, ContentExtractor ce) {

		List<Pair<Long, Pair<Integer, Integer>>> ret = new ArrayList<Pair<Long, Pair<Integer, Integer>>>(); 

		try {

			//			if (PStmtgetExtractedEntities == null){
			//				PStmtgetExtractedEntities = getConnection().prepareStatement(getExtractedEntitiesString);
			//			}else
			//				PStmtgetExtractedEntities.clearParameters();

			PreparedStatement PStmtgetExtractedEntities = getConnection().prepareStatement(getExtractedEntitiesString);

			PStmtgetExtractedEntities.setInt(1, idDatabase);
			PStmtgetExtractedEntities.setLong(2, idDocument);
			PStmtgetExtractedEntities.setInt(3, getContentExtractorId(ce));
			PStmtgetExtractedEntities.setInt(4, idInformationExtractionSystem);
			PStmtgetExtractedEntities.setInt(5, idEntityType); 


			ResultSet RSgetExtractedEntities = PStmtgetExtractedEntities.executeQuery();

			while (RSgetExtractedEntities.next()) {

				ret.add(new Pair<Long, Pair<Integer, Integer>>(RSgetExtractedEntities.getLong(1),new Pair<Integer,Integer>(RSgetExtractedEntities.getInt(2),RSgetExtractedEntities.getInt(3))));

			}

			RSgetExtractedEntities.close();

			PStmtgetExtractedEntities.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public Set<CandidateSentence> getGeneratedCandidateSentences(
			int idDatabase, long idDocument, int relationConf,
			ContentExtractor ce) {

		//It's already cached...

		try {

			//			if (PStmtgetGeneratedCandidateSentence == null){
			//				PStmtgetGeneratedCandidateSentence = getConnection().prepareStatement(getGeneratedCandidateSentenceString);
			//			}else
			//				PStmtgetGeneratedCandidateSentence.clearParameters();

			PreparedStatement PStmtgetGeneratedCandidateSentence = getConnection().prepareStatement(getGeneratedCandidateSentenceString);

			PStmtgetGeneratedCandidateSentence.setInt(1, idDatabase);
			PStmtgetGeneratedCandidateSentence.setLong(2, idDocument);
			PStmtgetGeneratedCandidateSentence.setInt(3, getContentExtractorId(ce));
			PStmtgetGeneratedCandidateSentence.setInt(4, relationConf);


			ResultSet RSgetGeneratedCandidateSentence = PStmtgetGeneratedCandidateSentence.executeQuery();

			String file = null;

			while (RSgetGeneratedCandidateSentence.next()) {

				file = RSgetGeneratedCandidateSentence.getString(1);

			}

			RSgetGeneratedCandidateSentence.close();

			PStmtgetGeneratedCandidateSentence.close();

			return	new CandidatesSentenceReader().readCandidateSentencesInstance(file);

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;

	}

	@Override
	public void finishTupleExtractionFull() {

		persistCandidateSentences();
		persistOperableStructure();
		persistEntities();
		persistTuple();

	}

	@Override
	public void makeExperimentAvailable(int exp, int idDatabase) {

		performTransaction("delete from ExperimentStatus where idDatabase = " + idDatabase + " and idExperiment = " + exp);

	}

	@Override
	protected String getRelationName(int idWorkload) {

		String ret = null;

		try {

			Statement StmtgetRelationName = getConnection().createStatement();

			ResultSet RSgetRelationName = StmtgetRelationName.executeQuery
					("select description from WorkloadRelation W join RelationshipType R on (W.idRelationshipType = R.idRelationshipType) where idWorkload = " + idWorkload);

			while (RSgetRelationName.next()) {

				ret = RSgetRelationName.getString(1);

			}

			RSgetRelationName.close();

			StmtgetRelationName.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public void writeRulesFromSplitModel(String collection, int relationConf, int idWorkload,
			int idVersion, int realsize, int split, boolean tuplesAsStopWords,
			String filePath, int idInformationExtractionSystem) {

		int tasw = tuplesAsStopWords? 1:0;

		performTransaction("INSERT INTO `AutomaticQueryGeneration`.`RulesFiles` (`idInformationExtractionSystem`,`collection`, `idRelationConfiguration`,`idWorkload`, `idVersion`, `docsInTraining`, `split`, `tuplesAsStopWords`, `filePath`) " +
				"VALUES ("+idInformationExtractionSystem+",'"+format(collection)+"' ,"+ relationConf +","+idWorkload+" ,"+idVersion+" ,"+realsize+" ,"+split+" ,"+tasw+ ",'"+format(filePath)+"' );");

	}

	@Override
	public File getRelationRulesFile(int idInformationExtractionSystem, String collection, int relationConf, WorkloadModel workload,
			Version version, int docsInTraining, int version_seed,
			boolean tuplesAsStopWords) {

		File ret = null;

		try {

			int tasw = tuplesAsStopWords? 0:1;

			Statement StmtgetRelationRulesFile = getConnection().createStatement();

			String query = "SELECT filePath FROM AutomaticQueryGeneration.RulesFiles where idInformationExtractionSystem = " + idInformationExtractionSystem + " and collection = '"+format(collection)+"' and idWorkload = "+workload.getId()+" and " +
					" idRelationConfiguration = " + relationConf + " and " +
					"idVersion = "+version.getId()+" and docsInTraining  = "+docsInTraining+" and split = "+version_seed+" and tuplesAsStopWords = " + tasw;

			ResultSet RSgetRelationRulesFile = StmtgetRelationRulesFile.executeQuery
					(query);

			while (RSgetRelationRulesFile.next()) {

				ret = new File(RSgetRelationRulesFile.getString(1));

			}

			RSgetRelationRulesFile.close();

			StmtgetRelationRulesFile.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public Set<Long> getProcessedQueries(int experimentId, int idDatabase,
			String navigationTechnique) {

		Set<Long> ret = new HashSet<Long>();

		try {

			//			if (PStmtgetProcessedQueries == null){
			//				PStmtgetProcessedQueries = getConnection().prepareStatement(getProcessedQueriesString);
			//			}else
			//				PStmtgetProcessedQueries.clearParameters();

			PreparedStatement PStmtgetProcessedQueries = getConnection().prepareStatement(getProcessedQueriesString);

			PStmtgetProcessedQueries.setInt(1, experimentId);
			PStmtgetProcessedQueries.setInt(2, idDatabase);
			PStmtgetProcessedQueries.setInt(3, getNavigationTechniqueId(navigationTechnique));

			ResultSet RSgetProcessedQueries = PStmtgetProcessedQueries.executeQuery();

			while (RSgetProcessedQueries.next()) {

				ret.add(RSgetProcessedQueries.getLong(1));

			}

			RSgetProcessedQueries.close();
			PStmtgetProcessedQueries.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public synchronized Map<Long, List<Document>> getQueryResultsTable(
			int experimentId, int idDatabase, String navHandler,String extractionTechnique, String resultTechnique) {

		Map<Long, List<Document>> res = getCachedQueryResultsTable(experimentId,idDatabase);

		if (res == null){

			res = new HashMap<Long, List<Document>>();

			try {

				//			if (PStmtgetQueryResultsTable == null){
				//				PStmtgetQueryResultsTable = getConnection().prepareStatement(getQueryResultsTableString);
				//			}else
				//				PStmtgetQueryResultsTable.clearParameters();

				PreparedStatement PStmtgetQueryResultsTable = getConnection().prepareStatement(getQueryResultsTableString);

				PStmtgetQueryResultsTable.setInt(1, experimentId);
				PStmtgetQueryResultsTable.setInt(2, idDatabase);
				PStmtgetQueryResultsTable.setInt(3, getExtractionTechniqueId(extractionTechnique));
				PStmtgetQueryResultsTable.setInt(4, getNavigationTechniqueId(navHandler));
				PStmtgetQueryResultsTable.setInt(5, getResultExtractionTechniqueId(resultTechnique));


				ResultSet RSgetQueryResultsTable = PStmtgetQueryResultsTable.executeQuery();

				while (RSgetQueryResultsTable.next()) {

					Long tq = RSgetQueryResultsTable.getLong(1);

					List<Document> list = res.get(tq);

					if (list == null){

						list = new ArrayList<Document>();

						res.put(tq,list);


					}

					list.add(new Document(getDatabase(idDatabase),RSgetQueryResultsTable.getLong(2)));

				}

				RSgetQueryResultsTable.close();

				PStmtgetQueryResultsTable.close();

				saveCachedQueryResultsTable(experimentId,idDatabase,res);

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		return res;

	}

	private void saveCachedQueryResultsTable(int experimentId, int idDatabase,
			Map<Long, List<Document>> res) {
		getCachedQueryResultsTable().put(experimentId + "-" +idDatabase,res);		
	}

	private Map<Long, List<Document>> getCachedQueryResultsTable(
			int experimentId, int idDatabase) {
		return getCachedQueryResultsTable().get(experimentId + "-" +idDatabase);
	}

	private Map<String,Map<Long, List<Document>>> getCachedQueryResultsTable() {

		if (cachedQueryResultsTable == null){
			cachedQueryResultsTable = new HashMap<String, Map<Long,List<Document>>>();
		}
		return cachedQueryResultsTable;
	}

	@Override
	public void clearExperimentSplit(Database database, int idExperiment) {
		performTransaction("DELETE FROM SplitsToProcess where idDatabase = " + database.getId() + " AND idExperiment = " + idExperiment);
		performTransaction("DELETE FROM SplitDocsToProcess where idDatabase = " + database.getId() + " AND idExperiment = " + idExperiment);
	}

	@Override
	public void prepareExperimentSplit(Database database, int idExperiment,
			long idDoc, int split) {

		try {

			accessExperimentSplit.acquire();

			if (PStmtprepareExperimentSplit == null){
				PStmtprepareExperimentSplit = getConnection().prepareStatement(prepareExperimentSplitString);
			}

			PStmtprepareExperimentSplit.setInt(1, database.getId());
			PStmtprepareExperimentSplit.setInt(2, idExperiment);
			PStmtprepareExperimentSplit.setInt(3, split);
			PStmtprepareExperimentSplit.setLong(4, idDoc);

			PStmtprepareExperimentSplit.addBatch();

			accessExperimentSplit.release();

		} catch (SQLException e) {
			accessExperimentSplit.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void executeExperimentSplit() {

		try {
			accessExperimentSplit.acquire();
			if (PStmtprepareExperimentSplit!=null)
				PStmtprepareExperimentSplit.executeBatch();
			accessExperimentSplit.release();
		} catch (SQLException e) {
			accessExperimentSplit.release();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public synchronized boolean isAvailable(Database database, int idExperiment, int split) {
		try {

			//			if (PStmtisAvailable == null){
			//				PStmtisAvailable = getConnection().prepareStatement(isAvailableString);
			//			}else
			//				PStmtisAvailable.clearParameters();

			PreparedStatement PStmtisAvailable = getConnection().prepareStatement(isAvailableString);

			PStmtisAvailable.setInt(1, database.getId());
			PStmtisAvailable.setInt(2, idExperiment);
			PStmtisAvailable.setInt(3, split);

			PStmtisAvailable.execute();
			PStmtisAvailable.close();

		} catch (SQLException e) {
			return false; //duplicated key, someone has already written it.
		}

		return true;
	}

	@Override
	public Set<Long> getDocumentsInSplit(Database database, int idExperiment,
			int split) {

		Set<Long> ret = new HashSet<Long>();

		try {

			//			if (PStmtgetDocumentsInSplit == null){
			//				PStmtgetDocumentsInSplit = getConnection().prepareStatement(getDocumentsInSplitString);
			//			}else
			//				PStmtgetDocumentsInSplit.clearParameters();

			PreparedStatement PStmtgetDocumentsInSplit = getConnection().prepareStatement(getDocumentsInSplitString);

			PStmtgetDocumentsInSplit.setInt(1, database.getId());
			PStmtgetDocumentsInSplit.setInt(2, idExperiment);
			PStmtgetDocumentsInSplit.setInt(3, split);

			ResultSet RSgetDocumentsInSplit = PStmtgetDocumentsInSplit.executeQuery();

			while (RSgetDocumentsInSplit.next()) {

				ret.add(RSgetDocumentsInSplit.getLong(1));

			}

			RSgetDocumentsInSplit.close();
			PStmtgetDocumentsInSplit.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;


	}

	@Override
	public void insertExperimentStatus(int idExperiment, int idDatabase,
			String computerName, ExperimentStatusEnum status) {

		try {

			//			if (PStmtinsertExperimentStatus == null){
			//				PStmtinsertExperimentStatus = getConnection().prepareStatement(insertExperimentStatusString);
			//			}else
			//				PStmtinsertExperimentStatus.clearParameters();

			PreparedStatement PStmtinsertExperimentStatus = getConnection().prepareStatement(insertExperimentStatusString);

			PStmtinsertExperimentStatus.setInt(1, idDatabase);
			PStmtinsertExperimentStatus.setInt(2, idExperiment);
			PStmtinsertExperimentStatus.setString(3, computerName);
			PStmtinsertExperimentStatus.setInt(4, getValue(status));

			PStmtinsertExperimentStatus.execute();

			PStmtinsertExperimentStatus.close();

		} catch (SQLException e) {
			//duplicated key, someone has already written it.
		}


	}

	@Override
	public int getIncrementalParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double threshold, Double wfp, double betaE) {

		int ret = -1;

		try {

			//			if (PStmtgetIncrementalParameter == null){
			//				PStmtgetIncrementalParameter = getConnection().prepareStatement(getIncrementalParameterString);
			//			}else
			//				PStmtgetIncrementalParameter.clearParameters();

			PreparedStatement PStmtgetIncrementalParameter = getConnection().prepareStatement(getIncrementalParameterString);

			PStmtgetIncrementalParameter.setInt(1, maxQuerySize);
			PStmtgetIncrementalParameter.setInt(2, minSupport);
			PStmtgetIncrementalParameter.setInt(3, minSuppAfterUpdate);
			PStmtgetIncrementalParameter.setDouble(4, threshold);
			PStmtgetIncrementalParameter.setDouble(5, wfp);
			PStmtgetIncrementalParameter.setDouble(6, betaE);

			ResultSet RSgetIncrementalParameter = PStmtgetIncrementalParameter.executeQuery();

			while (RSgetIncrementalParameter.next()) {

				ret = RSgetIncrementalParameter.getInt(1);

			}

			RSgetIncrementalParameter.close();

			PStmtgetIncrementalParameter.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertIncremental(maxQuerySize, minSupport, minSuppAfterUpdate, threshold, wfp, betaE);			

		}

		return ret;

	}

	private int insertIncremental(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double threshold, Double wfp, double betaE) {


		int ret = -1;

		try {

			//			if (PStmtinsertIncremental == null){
			//				PStmtinsertIncremental = getConnection().prepareStatement(insertIncrementalString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertIncremental.clearParameters();

			PreparedStatement PStmtinsertIncremental = getConnection().prepareStatement(insertIncrementalString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertIncremental.setInt(1, maxQuerySize);
			PStmtinsertIncremental.setInt(2, minSupport);
			PStmtinsertIncremental.setInt(3, minSuppAfterUpdate);
			PStmtinsertIncremental.setDouble(4, threshold);
			PStmtinsertIncremental.setDouble(5, wfp);
			PStmtinsertIncremental.setDouble(6, betaE);

			PStmtinsertIncremental.execute();

			ResultSet rs = PStmtinsertIncremental.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertIncremental.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getMSCParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double min_precision,
			double minimum_support_SVM, int k, double pow) {

		int ret = -1;

		try {

			//			if (PStmtgetMSCParameter == null){
			//				PStmtgetMSCParameter = getConnection().prepareStatement(getMSCParameterString);
			//			}else
			//				PStmtgetMSCParameter.clearParameters();

			PreparedStatement PStmtgetMSCParameter = getConnection().prepareStatement(getMSCParameterString);

			PStmtgetMSCParameter.setInt(1, maxQuerySize);
			PStmtgetMSCParameter.setInt(2, minSupport);
			PStmtgetMSCParameter.setInt(3, minSuppAfterUpdate);
			PStmtgetMSCParameter.setDouble(4, min_precision);
			PStmtgetMSCParameter.setDouble(5, minimum_support_SVM);
			PStmtgetMSCParameter.setInt(6, k);
			PStmtgetMSCParameter.setDouble(7, pow);

			ResultSet RSgetMSCParameter = PStmtgetMSCParameter.executeQuery();

			while (RSgetMSCParameter.next()) {

				ret = RSgetMSCParameter.getInt(1);

			}

			RSgetMSCParameter.close();
			PStmtgetMSCParameter.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertMSC(maxQuerySize, minSupport, minSuppAfterUpdate, min_precision, minimum_support_SVM, k, pow);			

		}

		return ret;

	}

	private int insertMSC(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double min_precision,
			double minimum_support_SVM, int k, double pow) {

		int ret = -1;

		try {

			//			if (PStmtinsertMSC == null){
			//				PStmtinsertMSC = getConnection().prepareStatement(insertMSCString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertMSC.clearParameters();

			PreparedStatement PStmtinsertMSC = getConnection().prepareStatement(insertMSCString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertMSC.setInt(1, maxQuerySize);
			PStmtinsertMSC.setInt(2, minSupport);
			PStmtinsertMSC.setInt(3, minSuppAfterUpdate);
			PStmtinsertMSC.setDouble(4, min_precision);
			PStmtinsertMSC.setDouble(5, minimum_support_SVM);
			PStmtinsertMSC.setInt(6, k);
			PStmtinsertMSC.setDouble(7, pow);

			PStmtinsertMSC.execute();

			ResultSet rs = PStmtinsertMSC.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertMSC.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getOptimisticParameters(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double threshold, double min_weight,
			Double min_precision, double supp) {

		int ret = -1;

		try {

			//			if (PStmtgetOptimisticParameters == null){
			//				PStmtgetOptimisticParameters = getConnection().prepareStatement(getOptimisticParametersString);
			//			}else
			//				PStmtgetOptimisticParameters.clearParameters();

			PreparedStatement PStmtgetOptimisticParameters = getConnection().prepareStatement(getOptimisticParametersString);

			PStmtgetOptimisticParameters.setInt(1, maxQuerySize);
			PStmtgetOptimisticParameters.setInt(2, minSupport);
			PStmtgetOptimisticParameters.setInt(3, minSuppAfterUpdate);
			PStmtgetOptimisticParameters.setDouble(4, threshold);
			PStmtgetOptimisticParameters.setDouble(5, min_weight);
			PStmtgetOptimisticParameters.setDouble(6, min_precision);
			PStmtgetOptimisticParameters.setDouble(7, supp);

			ResultSet RSgetOptimisticParameters = PStmtgetOptimisticParameters.executeQuery();

			while (RSgetOptimisticParameters.next()) {

				ret = RSgetOptimisticParameters.getInt(1);

			}

			RSgetOptimisticParameters.close();
			PStmtgetOptimisticParameters.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertOptimistic(maxQuerySize, minSupport, minSuppAfterUpdate, threshold, min_weight, min_precision, supp);			

		}

		return ret;

	}

	private int insertOptimistic(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double threshold, double min_weight,
			Double min_precision, double supp) {

		int ret = -1;

		try {

			//			if (PStmtinsertOptimistic == null){
			//				PStmtinsertOptimistic = getConnection().prepareStatement(insertOptimisticString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertOptimistic.clearParameters();

			PreparedStatement PStmtinsertOptimistic = getConnection().prepareStatement(insertOptimisticString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertOptimistic.setInt(1, maxQuerySize);
			PStmtinsertOptimistic.setInt(2, minSupport);
			PStmtinsertOptimistic.setInt(3, minSuppAfterUpdate);
			PStmtinsertOptimistic.setDouble(4, threshold);
			PStmtinsertOptimistic.setDouble(5, min_weight);
			PStmtinsertOptimistic.setDouble(6, min_precision);
			PStmtinsertOptimistic.setDouble(7, supp);

			PStmtinsertOptimistic.execute();

			ResultSet rs = PStmtinsertOptimistic.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertOptimistic.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getQProberParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double minSVMSupport, double epsilon,
			double min_precision) {

		int ret = -1;

		try {

			//			if (PStmtgetQProberParameter == null){
			//				PStmtgetQProberParameter = getConnection().prepareStatement(getQProberParameterString);
			//			}else
			//				PStmtgetQProberParameter.clearParameters();

			PreparedStatement PStmtgetQProberParameter = getConnection().prepareStatement(getQProberParameterString);

			PStmtgetQProberParameter.setInt(1, maxQuerySize);
			PStmtgetQProberParameter.setInt(2, minSupport);
			PStmtgetQProberParameter.setInt(3, minSuppAfterUpdate);
			PStmtgetQProberParameter.setDouble(4, minSVMSupport);
			PStmtgetQProberParameter.setDouble(5, epsilon);
			PStmtgetQProberParameter.setDouble(6, min_precision);

			ResultSet RSgetQProberParameter = PStmtgetQProberParameter.executeQuery();

			while (RSgetQProberParameter.next()) {

				ret = RSgetQProberParameter.getInt(1);

			}

			RSgetQProberParameter.close();
			PStmtgetQProberParameter.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertQProber(maxQuerySize, minSupport, minSuppAfterUpdate, minSVMSupport,epsilon,min_precision);			

		}

		return ret;

	}

	private int insertQProber(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, double minSVMSupport, double epsilon,
			double min_precision) {

		int ret = -1;

		try {

			//			if (PStmtinsertQProber == null){
			//				PStmtinsertQProber = getConnection().prepareStatement(insertQProberString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertQProber.clearParameters();

			PreparedStatement PStmtinsertQProber = getConnection().prepareStatement(insertQProberString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertQProber.setInt(1, maxQuerySize);
			PStmtinsertQProber.setInt(2, minSupport);
			PStmtinsertQProber.setInt(3, minSuppAfterUpdate);
			PStmtinsertQProber.setDouble(4, minSVMSupport);
			PStmtinsertQProber.setDouble(5, epsilon);
			PStmtinsertQProber.setDouble(6, min_precision);

			PStmtinsertQProber.execute();

			ResultSet rs = PStmtinsertQProber.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertQProber.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getRipperParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int fold, double minNo,
			int optimizationRuns, long seedValue, boolean pruning,
			boolean checkErrorRate) {

		int ret = -1;

		try {

			//			if (PStmtgetRipperParameter == null){
			//				PStmtgetRipperParameter = getConnection().prepareStatement(getRipperParameterString);
			//			}else
			//				PStmtgetRipperParameter.clearParameters();

			PreparedStatement PStmtgetRipperParameter = getConnection().prepareStatement(getRipperParameterString);

			PStmtgetRipperParameter.setInt(1, maxQuerySize);
			PStmtgetRipperParameter.setInt(2, minSupport);
			PStmtgetRipperParameter.setInt(3, minSuppAfterUpdate);
			PStmtgetRipperParameter.setInt(4, fold);
			PStmtgetRipperParameter.setDouble(5, minNo);
			PStmtgetRipperParameter.setInt(6, optimizationRuns);
			PStmtgetRipperParameter.setLong(7, seedValue);
			PStmtgetRipperParameter.setBoolean(8, pruning);
			PStmtgetRipperParameter.setBoolean(9, checkErrorRate);

			ResultSet RSgetRipperParameter = PStmtgetRipperParameter.executeQuery();

			while (RSgetRipperParameter.next()) {

				ret = RSgetRipperParameter.getInt(1);

			}

			RSgetRipperParameter.close();
			PStmtgetRipperParameter.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertRipper(maxQuerySize, minSupport, minSuppAfterUpdate, fold, minNo, optimizationRuns, seedValue, pruning,
					checkErrorRate);			

		}

		return ret;

	}

	private int insertRipper(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int fold, double minNo,
			int optimizationRuns, long seedValue, boolean pruning,
			boolean checkErrorRate) {

		int ret = -1;

		try {

			//			if (PStmtinsertRipper == null){
			//				PStmtinsertRipper = getConnection().prepareStatement(insertRipperString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertRipper.clearParameters();

			PreparedStatement PStmtinsertRipper = getConnection().prepareStatement(insertRipperString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertRipper.setInt(1, maxQuerySize);
			PStmtinsertRipper.setInt(2, minSupport);
			PStmtinsertRipper.setInt(3, minSuppAfterUpdate);
			PStmtinsertRipper.setInt(4, fold);
			PStmtinsertRipper.setDouble(5, minNo);
			PStmtinsertRipper.setInt(6, optimizationRuns);
			PStmtinsertRipper.setLong(7, seedValue);
			PStmtinsertRipper.setBoolean(8, pruning);
			PStmtinsertRipper.setBoolean(9, checkErrorRate);

			PStmtinsertRipper.execute();

			ResultSet rs = PStmtinsertRipper.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertRipper.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getTupleParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, long hits_per_page,
			double querySubmissionPerUnitTime, long queryTimeConsumed,
			double ieSubmissionPerUnitTime, long ieTimeConsumed) {

		int ret = -1;

		try {

			//			if (PStmtgetTupleParameter == null){
			//				PStmtgetTupleParameter = getConnection().prepareStatement(getTupleParameterString);
			//			}else
			//				PStmtgetTupleParameter.clearParameters();

			PreparedStatement PStmtgetTupleParameter = getConnection().prepareStatement(getTupleParameterString);

			PStmtgetTupleParameter.setInt(1, maxQuerySize);
			PStmtgetTupleParameter.setInt(2, minSupport);
			PStmtgetTupleParameter.setInt(3, minSuppAfterUpdate);
			PStmtgetTupleParameter.setLong(4, hits_per_page);
			PStmtgetTupleParameter.setDouble(5, querySubmissionPerUnitTime);
			PStmtgetTupleParameter.setLong(6, queryTimeConsumed);
			PStmtgetTupleParameter.setDouble(7, ieSubmissionPerUnitTime);
			PStmtgetTupleParameter.setLong(8, ieTimeConsumed);

			ResultSet RSgetTupleParameter = PStmtgetTupleParameter.executeQuery();

			while (RSgetTupleParameter.next()) {

				ret = RSgetTupleParameter.getInt(1);

			}

			RSgetTupleParameter.close();
			PStmtgetTupleParameter.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertTupleAlgorithm(maxQuerySize, minSupport, minSuppAfterUpdate, hits_per_page,
					querySubmissionPerUnitTime, queryTimeConsumed,
					ieSubmissionPerUnitTime, ieTimeConsumed);			

		}

		return ret;

	}

	private int insertTupleAlgorithm(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, long hits_per_page,
			double querySubmissionPerUnitTime, long queryTimeConsumed,
			double ieSubmissionPerUnitTime, long ieTimeConsumed) {


		int ret = -1;

		try {

			//			if (PStmtinsertTupleAlgorithm == null){
			//				PStmtinsertTupleAlgorithm = getConnection().prepareStatement(insertTupleAlgorithmString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertTupleAlgorithm.clearParameters();

			PreparedStatement PStmtinsertTupleAlgorithm = getConnection().prepareStatement(insertTupleAlgorithmString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertTupleAlgorithm.setInt(1, maxQuerySize);
			PStmtinsertTupleAlgorithm.setInt(2, minSupport);
			PStmtinsertTupleAlgorithm.setInt(3, minSuppAfterUpdate);
			PStmtinsertTupleAlgorithm.setLong(4, hits_per_page);
			PStmtinsertTupleAlgorithm.setDouble(5, querySubmissionPerUnitTime);
			PStmtinsertTupleAlgorithm.setLong(6, queryTimeConsumed);
			PStmtinsertTupleAlgorithm.setDouble(7, ieSubmissionPerUnitTime);
			PStmtinsertTupleAlgorithm.setLong(8, ieTimeConsumed);

			PStmtinsertTupleAlgorithm.execute();

			ResultSet rs = PStmtinsertTupleAlgorithm.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertTupleAlgorithm.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getSignificantPhrases(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int ngrams, ContentExtractor ce,
			int generatedQueries) {
		
		int ret = -1;

		try {

			//			if (PStmtgetRipperParameter == null){
			//				PStmtgetRipperParameter = getConnection().prepareStatement(getRipperParameterString);
			//			}else
			//				PStmtgetRipperParameter.clearParameters();

			PreparedStatement PStmtgetSignificantPhraseParameter = getConnection().prepareStatement(getRipperParameterString);

			PStmtgetSignificantPhraseParameter.setInt(1, maxQuerySize);
			PStmtgetSignificantPhraseParameter.setInt(2, minSupport);
			PStmtgetSignificantPhraseParameter.setInt(3, minSuppAfterUpdate);
			PStmtgetSignificantPhraseParameter.setInt(4, ngrams);
			PStmtgetSignificantPhraseParameter.setDouble(5, getContentExtractorId(ce));
			PStmtgetSignificantPhraseParameter.setInt(6, generatedQueries);

			ResultSet RSgetSignificantPhrasesParameter = PStmtgetSignificantPhraseParameter.executeQuery();

			while (RSgetSignificantPhrasesParameter.next()) {

				ret = RSgetSignificantPhrasesParameter.getInt(1);

			}

			RSgetSignificantPhrasesParameter.close();
			PStmtgetSignificantPhraseParameter.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertSignificantPhrases(maxQuerySize, minSupport, minSuppAfterUpdate, ngrams, getContentExtractorId(ce), generatedQueries);			

		}

		return ret;

		
	}
	
	
	private int insertSignificantPhrases(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int ngrams, int contentExtractorId,
			int generatedQueries) {
		
		int ret = -1;

		try {

			//			if (PStmtinsertRipper == null){
			//				PStmtinsertRipper = getConnection().prepareStatement(insertRipperString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertRipper.clearParameters();

			PreparedStatement PStmtinsertSignificantPhrases = getConnection().prepareStatement(insertSignificanPhraseParameterString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertSignificantPhrases.setInt(1, maxQuerySize);
			PStmtinsertSignificantPhrases.setInt(2, minSupport);
			PStmtinsertSignificantPhrases.setInt(3, minSuppAfterUpdate);
			PStmtinsertSignificantPhrases.setInt(4, ngrams);
			PStmtinsertSignificantPhrases.setDouble(5, contentExtractorId);
			PStmtinsertSignificantPhrases.setInt(6, generatedQueries);

			PStmtinsertSignificantPhrases.execute();

			ResultSet rs = PStmtinsertSignificantPhrases.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertSignificantPhrases.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
		
	}

	@Override
	public int getCombinedCollaborativeFilteringParameters(
			boolean preserveOrder, int neighbors, String userSimilarity,
			String neightborhood, String recommender) {

		int ret = -1;

		try {

			//			if (PStmtgetCombinedCollaborativeFilteringParameters == null){
			//				PStmtgetCombinedCollaborativeFilteringParameters = getConnection().prepareStatement(getCombinedCollaborativeFilteringParametersString);
			//			}else
			//				PStmtgetCombinedCollaborativeFilteringParameters.clearParameters();

			PreparedStatement PStmtgetCombinedCollaborativeFilteringParameters = getConnection().prepareStatement(getCombinedCollaborativeFilteringParametersString);

			PStmtgetCombinedCollaborativeFilteringParameters.setBoolean(1, preserveOrder);
			PStmtgetCombinedCollaborativeFilteringParameters.setInt(2, neighbors);
			PStmtgetCombinedCollaborativeFilteringParameters.setString(3, userSimilarity);
			PStmtgetCombinedCollaborativeFilteringParameters.setString(4, neightborhood);
			PStmtgetCombinedCollaborativeFilteringParameters.setString(5, recommender);

			ResultSet RSgetCombinedCollaborativeFilteringParameters = PStmtgetCombinedCollaborativeFilteringParameters.executeQuery();

			while (RSgetCombinedCollaborativeFilteringParameters.next()) {

				ret = RSgetCombinedCollaborativeFilteringParameters.getInt(1);

			}

			RSgetCombinedCollaborativeFilteringParameters.close();
			PStmtgetCombinedCollaborativeFilteringParameters.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertCombinedCollaborativeFilteringParameters(preserveOrder,neighbors,userSimilarity,neightborhood,recommender);			

		}

		return ret;

	}

	private int insertCombinedCollaborativeFilteringParameters(
			boolean preserveOrder, int neighbors, String userSimilarity,
			String neightborhood, String recommender) {


		int ret = -1;

		try {

			//			if (PStmtinsertCombinedCollaborativeFilteringParameters == null){
			//				PStmtinsertCombinedCollaborativeFilteringParameters = getConnection().prepareStatement(insertCombinedCollaborativeFilteringParametersString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertCombinedCollaborativeFilteringParameters.clearParameters();

			PreparedStatement PStmtinsertCombinedCollaborativeFilteringParameters = getConnection().prepareStatement(insertCombinedCollaborativeFilteringParametersString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertCombinedCollaborativeFilteringParameters.setBoolean(1, preserveOrder);
			PStmtinsertCombinedCollaborativeFilteringParameters.setInt(2, neighbors);
			PStmtinsertCombinedCollaborativeFilteringParameters.setString(3, userSimilarity);
			PStmtinsertCombinedCollaborativeFilteringParameters.setString(4, neightborhood);
			PStmtinsertCombinedCollaborativeFilteringParameters.setString(5, recommender);

			PStmtinsertCombinedCollaborativeFilteringParameters.execute();

			ResultSet rs = PStmtinsertCombinedCollaborativeFilteringParameters.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertCombinedCollaborativeFilteringParameters.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	public void performStatement(String statement) {
		performTransaction(statement);
	}

	@Override
	public int getQXtractParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int fold, double minNo,
			int optimizationRuns, long seedValue, boolean pruning,
			boolean checkErrorRate) {

		int ret = -1;

		try {

			//			if (PStmtgetRipperParameter == null){
			//				PStmtgetRipperParameter = getConnection().prepareStatement(getRipperParameterString);
			//			}else
			//				PStmtgetRipperParameter.clearParameters();

			PreparedStatement PStmtgetQXtractParameter = getConnection().prepareStatement(getQXtractParameterString);

			PStmtgetQXtractParameter.setInt(1, maxQuerySize);
			PStmtgetQXtractParameter.setInt(2, minSupport);
			PStmtgetQXtractParameter.setInt(3, minSuppAfterUpdate);
			PStmtgetQXtractParameter.setInt(4, fold);
			PStmtgetQXtractParameter.setDouble(5, minNo);
			PStmtgetQXtractParameter.setInt(6, optimizationRuns);
			PStmtgetQXtractParameter.setLong(7, seedValue);
			PStmtgetQXtractParameter.setBoolean(8, pruning);
			PStmtgetQXtractParameter.setBoolean(9, checkErrorRate);

			ResultSet RSgetQXtractParameter = PStmtgetQXtractParameter.executeQuery();

			while (RSgetQXtractParameter.next()) {

				ret = RSgetQXtractParameter.getInt(1);

			}

			RSgetQXtractParameter.close();
			PStmtgetQXtractParameter.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertQXtract(maxQuerySize, minSupport, minSuppAfterUpdate, fold, minNo, optimizationRuns, seedValue, pruning,
					checkErrorRate);			

		}

		return ret;

	}

	private int insertQXtract(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate, int fold, double minNo,
			int optimizationRuns, long seedValue, boolean pruning,
			boolean checkErrorRate) {

		int ret = -1;

		try {

			//			if (PStmtinsertRipper == null){
			//				PStmtinsertRipper = getConnection().prepareStatement(insertRipperString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertRipper.clearParameters();

			PreparedStatement PStmtinsertQXtract = getConnection().prepareStatement(insertQXtractString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertQXtract.setInt(1, maxQuerySize);
			PStmtinsertQXtract.setInt(2, minSupport);
			PStmtinsertQXtract.setInt(3, minSuppAfterUpdate);
			PStmtinsertQXtract.setInt(4, fold);
			PStmtinsertQXtract.setDouble(5, minNo);
			PStmtinsertQXtract.setInt(6, optimizationRuns);
			PStmtinsertQXtract.setLong(7, seedValue);
			PStmtinsertQXtract.setBoolean(8, pruning);
			PStmtinsertQXtract.setBoolean(9, checkErrorRate);

			PStmtinsertQXtract.execute();

			ResultSet rs = PStmtinsertQXtract.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertQXtract.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<TextQuery> getQueriesForTuplesGeneration(int searchRoundId,
			int idDatabase) {

		List<TextQuery> ret = new ArrayList<TextQuery>();

		try {

			PreparedStatement PStmtgetQueriesForTuplesGeneration = getConnection().prepareStatement(selectQueryForTuplesGenerationString);

			PStmtgetQueriesForTuplesGeneration.setInt(1, searchRoundId);
			PStmtgetQueriesForTuplesGeneration.setInt(2, idDatabase);

			ResultSet RSgetQueriesForTuplesGeneration = PStmtgetQueriesForTuplesGeneration.executeQuery();

			while (RSgetQueriesForTuplesGeneration.next()) {

				ret.add(new TextQuery(RSgetQueriesForTuplesGeneration.getString(1)));

			}

			RSgetQueriesForTuplesGeneration.close();
			PStmtgetQueriesForTuplesGeneration.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public int getSampleConfigurationBaseParameter(int configuration) {

		int ret = -1;

		try {

			PreparedStatement PStmtgetSampleConfigurationBaseParameter = getConnection().prepareStatement(getSampleConfigurationBaseParameterString );

			PStmtgetSampleConfigurationBaseParameter.setInt(1, configuration);

			ResultSet RSgetSampleConfigurationBaseParameter = PStmtgetSampleConfigurationBaseParameter.executeQuery();

			while (RSgetSampleConfigurationBaseParameter.next()) {

				ret = RSgetSampleConfigurationBaseParameter.getInt(1);

			}

			RSgetSampleConfigurationBaseParameter.close();
			PStmtgetSampleConfigurationBaseParameter.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public List<Integer> getSampleConfiguration(int sampleAlgorithm, int workload,
			int relationConfiguration, boolean countAll, int sampleGenerator, boolean useAll,
			int version, int queryPoolExecutor, int documentPerQuery, int maxQueries,
			int uselessCount, int idInformationExtraction) {

		List<Integer> ret = new ArrayList<Integer>();

		try {

			PreparedStatement PStmtgetSampleConfiguration = getConnection().prepareStatement(getSampleConfigurationString);

			PStmtgetSampleConfiguration.setInt(1, sampleAlgorithm);
			PStmtgetSampleConfiguration.setInt(2, sampleAlgorithm);
			PStmtgetSampleConfiguration.setInt(3, version);
			PStmtgetSampleConfiguration.setInt(4, version);
			PStmtgetSampleConfiguration.setInt(5, workload);
			PStmtgetSampleConfiguration.setInt(6, workload);
			PStmtgetSampleConfiguration.setInt(7, relationConfiguration);
			PStmtgetSampleConfiguration.setInt(8, relationConfiguration);
			PStmtgetSampleConfiguration.setInt(9, idInformationExtraction);
			PStmtgetSampleConfiguration.setInt(10, idInformationExtraction);
			PStmtgetSampleConfiguration.setInt(11, queryPoolExecutor);
			PStmtgetSampleConfiguration.setInt(12, queryPoolExecutor);
			PStmtgetSampleConfiguration.setInt(13, sampleGenerator);
			PStmtgetSampleConfiguration.setInt(14, sampleGenerator);
			PStmtgetSampleConfiguration.setBoolean(15, useAll);
			PStmtgetSampleConfiguration.setBoolean(16, useAll);
			PStmtgetSampleConfiguration.setInt(17, documentPerQuery);
			PStmtgetSampleConfiguration.setInt(18, documentPerQuery);
			PStmtgetSampleConfiguration.setInt(19, uselessCount);
			PStmtgetSampleConfiguration.setInt(20, uselessCount);
			PStmtgetSampleConfiguration.setBoolean(21, countAll);
			PStmtgetSampleConfiguration.setBoolean(22, countAll);
			PStmtgetSampleConfiguration.setInt(23, maxQueries);
			PStmtgetSampleConfiguration.setInt(24, maxQueries);
			

			System.err.println(PStmtgetSampleConfiguration.toString());

			ResultSet RSgetSampleConfiguration = PStmtgetSampleConfiguration.executeQuery();

			while (RSgetSampleConfiguration.next()) {

				ret.add(RSgetSampleConfiguration.getInt(1));

			}

			RSgetSampleConfiguration.close();
			PStmtgetSampleConfiguration.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

//	@Override
//	public List<Integer> getSampleConfiguration(int sampleAlgorithm, int workload,
//			int relationConfiguration, boolean countAll, int sampleGenerator, boolean useAll,
//			int version, int queryPoolExecutor, int documentPerQuery, int maxQueries,
//			int uselessCount, int idInformationExtraction) {
//
//		List<Integer> ret = new ArrayList<Integer>();
//
//		try {
//
//			PreparedStatement PStmtgetSampleConfiguration = getConnection().prepareStatement(getSampleConfigurationStringM);
//
//			PStmtgetSampleConfiguration.setInt(1, sampleAlgorithm);
//			PStmtgetSampleConfiguration.setInt(2, sampleAlgorithm);
//			PStmtgetSampleConfiguration.setInt(3, version);
//			PStmtgetSampleConfiguration.setInt(4, version);
//			PStmtgetSampleConfiguration.setInt(5, workload);
//			PStmtgetSampleConfiguration.setInt(6, workload);
//			PStmtgetSampleConfiguration.setInt(7, relationConfiguration);
//			PStmtgetSampleConfiguration.setInt(8, relationConfiguration);
//			PStmtgetSampleConfiguration.setInt(9, idInformationExtraction);
//			PStmtgetSampleConfiguration.setInt(10, idInformationExtraction);
//			PStmtgetSampleConfiguration.setInt(11, queryPoolExecutor);
//			PStmtgetSampleConfiguration.setInt(12, queryPoolExecutor);
//			PStmtgetSampleConfiguration.setInt(13, sampleGenerator);
//			PStmtgetSampleConfiguration.setInt(14, sampleGenerator);
//			PStmtgetSampleConfiguration.setBoolean(15, useAll);
//			PStmtgetSampleConfiguration.setBoolean(16, useAll);
//			PStmtgetSampleConfiguration.setInt(17, documentPerQuery);
//			PStmtgetSampleConfiguration.setInt(18, documentPerQuery);
//			PStmtgetSampleConfiguration.setInt(19, uselessCount);
//			PStmtgetSampleConfiguration.setInt(20, uselessCount);
//			PStmtgetSampleConfiguration.setBoolean(21, countAll);
//			PStmtgetSampleConfiguration.setBoolean(22, countAll);
//			PStmtgetSampleConfiguration.setInt(23, maxQueries);
//			PStmtgetSampleConfiguration.setInt(24, maxQueries);
//			
//			System.err.println(PStmtgetSampleConfiguration.toString());
//
//			ResultSet RSgetSampleConfiguration = PStmtgetSampleConfiguration.executeQuery();
//
//			while (RSgetSampleConfiguration.next()) {
//
//				ret.add(RSgetSampleConfiguration.getInt(1));
//
//			}
//
//			RSgetSampleConfiguration.close();
//			PStmtgetSampleConfiguration.close();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		return ret;
//
//	}
	
	@Override
	public Map<Document, List<Tuple>> getSampleTuples(Sample sample) {

		return getSampleTuples(sample.getId());

	}

	private Map<Integer,Map<Document, List<Tuple>>> getCachedTuples() {

		if (cachedTuples == null){
			cachedTuples = new HashMap<Integer,Map<Document, List<Tuple>>>();
		}
		return cachedTuples;
	}

	@Override
	public String getSmallAttributeName(int idSample) {

		String ret = null;

		try {

			PreparedStatement PStmtgetSmallAttributeName = getConnection().prepareStatement(getSmallAttributeNameString );

			PStmtgetSmallAttributeName.setInt(1, idSample);

			ResultSet RSgetSmallAttributeName = PStmtgetSmallAttributeName.executeQuery();

			while (RSgetSmallAttributeName.next()) {

				ret = RSgetSmallAttributeName.getString(1);

			}

			RSgetSmallAttributeName.close();
			PStmtgetSmallAttributeName.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public String getLargeAttributeName(int idSample) {

		String ret = null;

		try {

			PreparedStatement PStmtgetLargeAttributeName = getConnection().prepareStatement(getLargeAttributeNameString );

			PStmtgetLargeAttributeName.setInt(1, idSample);

			ResultSet RSgetLargeAttributeName = PStmtgetLargeAttributeName.executeQuery();

			while (RSgetLargeAttributeName.next()) {

				ret = RSgetLargeAttributeName.getString(1);

			}

			RSgetLargeAttributeName.close();
			PStmtgetLargeAttributeName.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<String> getCandidateSentencesFileList() {

		List<String> ret = new ArrayList<String>();

		try {

			Statement StmtgetCandidateSentencesFileList = getConnection().createStatement();

			ResultSet RSgetCandidateSentencesFileList = StmtgetCandidateSentencesFileList.executeQuery
					("Select file from CandidateSentence where size > 0");

			while (RSgetCandidateSentencesFileList.next()) {

				ret.add(RSgetCandidateSentencesFileList.getString(1));

			}

			RSgetCandidateSentencesFileList.close();

			StmtgetCandidateSentencesFileList.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public List<String> getOperableStructureFileList(int extractor) {

		List<String> ret = new ArrayList<String>();

		try {

			Statement StmtgetOperableStructureFileList = getConnection().createStatement();

			ResultSet RSgetOperableStructureFileList = StmtgetOperableStructureFileList.executeQuery
					("Select file from OperableStructure where idInformationExtractionSystem = "+extractor+" and file != 'empty.os'");

			while (RSgetOperableStructureFileList.next()) {

				ret.add(RSgetOperableStructureFileList.getString(1));

			}

			RSgetOperableStructureFileList.close();

			StmtgetOperableStructureFileList.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;

	}

	@Override
	public Set<Long> getDocumentsInExtractedEntities(Database database, List<Integer> entities) {

		Set<Long> ret = new HashSet<Long>();

		try {

			Statement StmtgetDocumentsInExtractedEntities = getConnection().createStatement();

			ResultSet RSgetDocumentsInExtractedEntities = StmtgetDocumentsInExtractedEntities.executeQuery
					("Select distinct idDocument from Entity where idDatabase = "+database.getId()+" and idEntityType IN " + generateIn(entities));

			while (RSgetDocumentsInExtractedEntities.next()) {

				ret.add(RSgetDocumentsInExtractedEntities.getLong(1));

			}

			RSgetDocumentsInExtractedEntities.close();

			StmtgetDocumentsInExtractedEntities.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;		

	}

	@Override
	public void writeTextQueryCollection(long qId, String collection) {

		String writeTextQueryCollection = "INSERT INTO `AutomaticQueryGeneration`.`TextQueryCollection` " +
				"(`collection`, `idQuery`) VALUES ( '"+collection+"',"+qId+")";

		performTransaction(writeTextQueryCollection);

	}

	@Override
	public void writeTextQueryExtractor(long qId, int informationExtractionSystem, int relationConfiguration) {

		String writeTextQueryExtractor = "INSERT INTO `AutomaticQueryGeneration`.`RelationshipExtractorQueries` " +
				"(`idInformationExtractionSystem`, `idRelationConfiguration`, `idQuery`) VALUES ( "+informationExtractionSystem+","+relationConfiguration+", "+qId+")";

		performTransaction(writeTextQueryExtractor);

	}

	@Override
	public List<Long> getDocumentsWithTuples(int informationExtractionSystem,
			int relationConfiguration, int idDatabase) {

		int idInfExtSys = getRelationExtractionSystemId(relationConfiguration, informationExtractionSystem);

		List<Long> ret = new ArrayList<Long>();

		try {

			PreparedStatement PStmtgetDocumentsWithTuples = getConnection().prepareStatement(resgetDocumentsWithTuplesString);

			PStmtgetDocumentsWithTuples.setInt(1, idInfExtSys);
			PStmtgetDocumentsWithTuples.setInt(2, idDatabase);


			ResultSet RSgetDocumentsWithTuples = PStmtgetDocumentsWithTuples.executeQuery();

			while (RSgetDocumentsWithTuples.next()) {

				ret.add(RSgetDocumentsWithTuples.getLong(1));

			}

			RSgetDocumentsWithTuples.close();
			PStmtgetDocumentsWithTuples.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public void writeSignificantPhrase(int ieSystem, int relConf,
			String collection, int workload, int version, int split,
			boolean tupleAsStopWord, int position, TextQuery query) {

		long idQuery = getTextQuery(query);

		try {

			//			if (PStmtwriteRelationKeyword == null){
			//				PStmtwriteRelationKeyword = getConnection().prepareStatement(writeRelationKeywordString);
			//			}else
			//				PStmtwriteRelationKeyword.clearParameters();

			PreparedStatement PStmtwriteSignificantPhrase = getConnection().prepareStatement(writeSignificantPhraseString );

			PStmtwriteSignificantPhrase.setInt(1, ieSystem);
			PStmtwriteSignificantPhrase.setInt(2, relConf);
			PStmtwriteSignificantPhrase.setString(3, collection);
			PStmtwriteSignificantPhrase.setInt(4, workload);
			PStmtwriteSignificantPhrase.setInt(5, version);
			PStmtwriteSignificantPhrase.setInt(6, split);
			PStmtwriteSignificantPhrase.setBoolean(7, tupleAsStopWord);
			PStmtwriteSignificantPhrase.setInt(8, position);
			PStmtwriteSignificantPhrase.setLong(9, idQuery);

			PStmtwriteSignificantPhrase.execute();
			PStmtwriteSignificantPhrase.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Map<Long, Pair<Integer, String>> getCandidateSentencesMap(
			Database database, int relationConf, ContentExtractor ce,
			Set<Long> documents) {

		Map<Long, Pair<Integer, String>> ret = new HashMap<Long, Pair<Integer,String>>();

		if (documents.isEmpty())
			return ret;

		String IN = generateIn(new ArrayList<Long>(documents));

		try {

			Statement StmtgetCandidateSentencesMap = getConnection().createStatement();

			String qu = "SELECT C.idDocument,C.file,C.size from CandidateSentence C where C.idDatabase = " + database.getId() +
					" and C.idRelationConfiguration = "+relationConf+" and C.idContentExtractionSystem = "+getContentExtractorId(ce)+" and C.idDocument IN " + IN;

			System.out.println(qu);

			ResultSet RSgetCandidateSentencesMap = StmtgetCandidateSentencesMap.executeQuery
					(qu);

			while (RSgetCandidateSentencesMap.next()) {

				ret.put(RSgetCandidateSentencesMap.getLong(1), new Pair<Integer,String>(RSgetCandidateSentencesMap.getInt(3),RSgetCandidateSentencesMap.getString(2)));

			}

			RSgetCandidateSentencesMap.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<List<String>> loadNotProcessedHurry(Database database,
			int relationConf) {

		List<String> queries = getQueriesInRelConf(relationConf);

		String IN = generateIn(queries);

		List<List<String>> ret = new ArrayList<List<String>>();

		try {

			Statement StmtloadNotProcessedHurry = getConnection().createStatement();

			ResultSet RSloadNotProcessedHurry = StmtloadNotProcessedHurry.executeQuery
					("select Text from TextQueryRelationConfiguration T join TextQuery TQ on (T.idTextQuery = TQ.idQuery) where idRelationConfiguration = "+relationConf+" and idTextQuery NOT IN (select idQuery from RawResultPage where idDatabase = "+database.getId()+" and page = 0 and idQuery IN "+IN+");");

			while (RSloadNotProcessedHurry.next()) {

				ret.add(Arrays.asList(RSloadNotProcessedHurry.getString(1).split(" ")));

			}

			RSloadNotProcessedHurry.close();

			StmtloadNotProcessedHurry.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	private List<String> getQueriesInRelConf(int relationConf) {


		List<String> ret = new ArrayList<String>();

		try {

			Statement StmtqueriesInRelConf = getConnection().createStatement();

			ResultSet RSqueriesInRelConf = StmtqueriesInRelConf.executeQuery
					("select idTextQuery from TextQueryRelationConfiguration where idRelationConfiguration = " + relationConf);

			while (RSqueriesInRelConf.next()) {

				ret.add(RSqueriesInRelConf.getString(1));

			}

			RSqueriesInRelConf.close();

			StmtqueriesInRelConf.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public List<Long> getDocumentsBelowSplit(int db, int idExperiment, int split) {

		List<Long> ret = new ArrayList<Long>();

		try {

			Statement StmtDocumentBelowSplit = getConnection().createStatement();

			ResultSet RSDocumentBelowSplit = StmtDocumentBelowSplit.executeQuery
					("select idDocument from SplitDocsToProcess where idDatabase = "+db+" and idExperiment = "+idExperiment+" and abs(split) <= " + split);

			while (RSDocumentBelowSplit.next()) {

				ret.add(RSDocumentBelowSplit.getLong(1));

			}

			RSDocumentBelowSplit.close();

			StmtDocumentBelowSplit.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;


	}

	@Override
	public List<Long> getNotContainingCandidateSentence(int db, int relationConf) {

		List<Long> ret = new ArrayList<Long>();

		try {

			Statement StmtNotContainingCandidateSentence = getConnection().createStatement();

			ResultSet RSNotContainingCandidateSentence = StmtNotContainingCandidateSentence.executeQuery
					("select idDocument, from WebDocument where idDatabase = "+db+" and idDocument NOT IN (select idDocument from CandidateSentence where idRelationConfiguration = "+relationConf+" and idDatabase = "+db+" and size > 0)");

			while (RSNotContainingCandidateSentence.next()) {

				ret.add(RSNotContainingCandidateSentence.getLong(1));

			}

			RSNotContainingCandidateSentence.close();

			StmtNotContainingCandidateSentence.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;


	}

	@Override
	public List<Pair<Long, String>> getCandidateSentences(int idDatabase,
			int idContentExtractor, int idRelationConfiguration) {

		List<Pair<Long, String>> ret = new ArrayList<Pair<Long, String>>();

		try {

			Statement StmtNotContainingCandidateSentence = getConnection().createStatement();

			ResultSet RSNotContainingCandidateSentence = StmtNotContainingCandidateSentence.executeQuery
					("select idDocument,file from CandidateSentence where idDatabase = "+idDatabase+" and idRelationConfiguration = "+idRelationConfiguration+" and idContentExtractionSystem = " + idContentExtractor + " and size > 0");

			while (RSNotContainingCandidateSentence.next()) {

				ret.add(new Pair<Long,String>(RSNotContainingCandidateSentence.getLong(1),RSNotContainingCandidateSentence.getString(2)));

			}

			RSNotContainingCandidateSentence.close();

			StmtNotContainingCandidateSentence.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public synchronized persistentWriter createNewInstance(boolean copyTextQuery) {

		persistentWriter pW;

		if (getPoolInstance().isEmpty()){

			if (copyTextQuery)
				pW = new databaseWriter(this.prefix,this.textquerytable, this.maxIdQuery);
			else
				pW = new databaseWriter(this.prefix,null,-1);

		}else{
			pW = getPoolInstance().remove(0);
		}

		return pW;
	}

	private synchronized List<persistentWriter> getPoolInstance() {

		if (poolInstance == null){
			poolInstance = new ArrayList<persistentWriter>();
		}
		return poolInstance;
	}

	@Override
	public Map<Integer, Set<Double>> getDatabaseClusterValues(
			int clusterFunctionId, int similarityFunctionId, int relationshipType) {

		Map<Integer, Set<Double>> ret = new HashMap<Integer, Set<Double>>();

		try {

			Statement StmtgetDatabaseClusterValues = getConnection().createStatement();

			ResultSet RSgetDatabaseClusterValues = StmtgetDatabaseClusterValues.executeQuery
					("select idDatabase,value from DatabaseCluster D join RelationClusterSimilarity " +
							"R on (D.idCluster = R.idCluster) where R.idRelationshipType = " + relationshipType + " and idClusterFunction = "+clusterFunctionId+" and idSimilarity = "+similarityFunctionId+" order by idDatabase");

			while (RSgetDatabaseClusterValues.next()) {

				Integer idDatabase = RSgetDatabaseClusterValues.getInt(1);
				Double value = RSgetDatabaseClusterValues.getDouble(2);

				Set<Double> values = ret.get(idDatabase);

				if (values == null){
					values = new HashSet<Double>();
					ret.put(idDatabase, values);
				}

				values.add(value);

			}

			RSgetDatabaseClusterValues.close();

			StmtgetDatabaseClusterValues.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;

	}

	@Override
	public Map<Integer, Double> getClusterValues(int similarityFunctionId, int relationshipType) {

		Map<Integer, Double> ret = new HashMap<Integer, Double>();

		try {

			Statement StmtgetClusterValues = getConnection().createStatement();

			ResultSet RSgetClusterValues = StmtgetClusterValues.executeQuery
					("select R.idCluster,R.value from RelationClusterSimilarity R" +
							" where idSimilarity = "+similarityFunctionId + " and idRelationshipType = " + relationshipType);

			while (RSgetClusterValues.next()) {

				Integer idCluster = RSgetClusterValues.getInt(1);
				Double value = RSgetClusterValues.getDouble(2);

				ret.put(idCluster, value);

			}

			RSgetClusterValues.close();

			StmtgetClusterValues.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;		

	}

	@Override
	public List<Database> getClusterDatbases(
			ClusterFunctionEnum clusterFunction, int idRelationshipType) {

		List<Database> ret = new ArrayList<Database>();

		try {

			Statement StmtgetClusterDatabases = getConnection().createStatement();

			ResultSet RSgetClusterDatabases = StmtgetClusterDatabases.executeQuery
					("select idCluster from DatabaseCluster" +
							" where idRelationshipType = " + idRelationshipType + " and idClusterFunction = " + getClusterFunctionId(clusterFunction));

			while (RSgetClusterDatabases.next()) {

				Integer idCluster = RSgetClusterDatabases.getInt(1);

				ret.add(getDatabase(idCluster));

			}

			RSgetClusterDatabases.close();

			StmtgetClusterDatabases.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;		


	}

	@Override
	public synchronized void releaseInstance(persistentWriter pW) {
		getPoolInstance().add(pW);		
	}

	@Override
	public int getNextPossibleSplit(Database database, int idExperiment) {

		int next = 0;

		try {

			Statement StmtgetNextPossibleSplit = getConnection().createStatement();

			ResultSet RSgetNextPossibleSplit = StmtgetNextPossibleSplit.executeQuery
					("select max(split) from SplitsToProcess" +
							" where idDatabase = " + database.getId() + " and idExperiment = " + idExperiment);

			while (RSgetNextPossibleSplit.next()) {

				Integer split = RSgetNextPossibleSplit.getInt(1);

				if (split != null)
					next = split;

			}

			RSgetNextPossibleSplit.close();

			StmtgetNextPossibleSplit.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return next+1;				

	}

	@Override
	public List<String> getSignificantPhrases(int informationExtractionSystem,
			int relationConf, String collection, WorkloadModel workload,
			Version version, boolean tuplesAsStopwords, int version_seed,
			int docsInTraining) {

		List<String> significantPhrases = new ArrayList<String>();

		try {

			//			if (PStmtgetRelationKeywords == null){
			//				PStmtgetRelationKeywords = getConnection().prepareStatement(getRelationKeywordsString);
			//			}else
			//				PStmtgetRelationKeywords.clearParameters();

			PreparedStatement PStmtgetSignificantPhrases = getConnection().prepareStatement(getSignificantPhrasesString);

			PStmtgetSignificantPhrases.setInt(1, informationExtractionSystem);
			PStmtgetSignificantPhrases.setInt(2, relationConf);
			PStmtgetSignificantPhrases.setString(3, collection);
			PStmtgetSignificantPhrases.setInt(4, workload.getId());
			PStmtgetSignificantPhrases.setInt(5, version.getId());
			PStmtgetSignificantPhrases.setInt(6, version_seed);
			PStmtgetSignificantPhrases.setBoolean(7, tuplesAsStopwords);
			PStmtgetSignificantPhrases.setInt(8, docsInTraining);

			ResultSet RSgetSignificantPhrases = PStmtgetSignificantPhrases.executeQuery();

			while (RSgetSignificantPhrases.next()){

				significantPhrases.add(RSgetSignificantPhrases.getString(1));

			}

			RSgetSignificantPhrases.close();
			PStmtgetSignificantPhrases.close();

		} catch (SQLException e) {

			e.printStackTrace();

			return null;
		}

		return significantPhrases;
	}

	@Override
	public Map<Document, List<Tuple>> getSampleTuples(int sampleNumber) {

		Map<Document, List<Tuple>> tuples = getCachedTuples().get(sampleNumber);

		if (tuples == null){

			tuples = new HashMap<Document, List<Tuple>>();

			try {

				PreparedStatement PStmtgetSampleTuples = getConnection().prepareStatement(getSampleTuplesString );

				PStmtgetSampleTuples.setInt(1, sampleNumber);

				ResultSet RSgetSampleTuples = PStmtgetSampleTuples.executeQuery();

				while (RSgetSampleTuples.next()){

					Document d = new Document(getDatabase(RSgetSampleTuples.getInt(1)),RSgetSampleTuples.getLong(2));

					List<Tuple> tup = tuples.get(d);

					if (tup == null){
						tup = new ArrayList<Tuple>();
						tuples.put(d, tup);
					}

					tup.add(TupleReader.generateTuple(RSgetSampleTuples.getString(3).trim()));
				}

				RSgetSampleTuples.close();
				PStmtgetSampleTuples.close();

			} catch (SQLException e) {
				return null;
			}

			System.gc();

			getCachedTuples().put(sampleNumber,tuples);

		}

		return tuples;
	}

	public static void main(String[] args) {

		persistentWriter pW = PersistenceImplementation.getWriter();

		Sample s = Sample.getSample(pW.getDatabaseById(4), new DummyVersion("INDEPENDENT"), new DummyWorkload(17), 1, 1, new DummySampleConfiguration(23, -1, -1, -1, -1, -1, -1, 0.0, 0.0, false, "", -1));

		System.out.println(pW.writeSample(s));

	}

	@Override
	public Map<Integer,Pair<Integer,Integer>> getUselessSamples(int idWorkload, int idVersion,
			int idExtractor, int idRelationConfiguration, int uselessDocuments,
			int idDatabase) {

		Map<Integer,Pair<Integer,Integer>> samples = new HashMap<Integer,Pair<Integer,Integer>>();

		try {

			PreparedStatement PStmtgetUselessSamples = getConnection().prepareStatement(getUselessSamplesString);

			PStmtgetUselessSamples.setInt(1, idDatabase);
			PStmtgetUselessSamples.setInt(2, idVersion);
			PStmtgetUselessSamples.setInt(3, idWorkload);
			PStmtgetUselessSamples.setInt(4, uselessDocuments);
			PStmtgetUselessSamples.setInt(5, idRelationConfiguration);
			PStmtgetUselessSamples.setInt(6, idExtractor);
			PStmtgetUselessSamples.setInt(7, uselessDocuments);

			ResultSet RSgetAllSamples = PStmtgetUselessSamples.executeQuery();

			while (RSgetAllSamples.next()){

				samples.put(RSgetAllSamples.getInt(2),new Pair<Integer,Integer>(RSgetAllSamples.getInt(1),RSgetAllSamples.getInt(3)));

			}

			RSgetAllSamples.close();
			PStmtgetUselessSamples.close();

		} catch (SQLException e) {
			return null;
		}

		return samples;

	}

	@Override
	public void saveDoneSample(int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration,
			SampleBuilderParameters sampleBuilderParameters, int idDatabase,
			int idSample) {
		
		try {

			PreparedStatement PStmtsaveDoneSample = getConnection().prepareStatement(saveDoneSample );

			PStmtsaveDoneSample.setInt(1, idSample);
			PStmtsaveDoneSample.setInt(2, sampleBuilderParameters.getId());
			PStmtsaveDoneSample.setInt(3, idDatabase);
			PStmtsaveDoneSample.setInt(4, idWorkload);
			PStmtsaveDoneSample.setInt(5, idVersion);
			PStmtsaveDoneSample.setInt(6, idExtractor);
			PStmtsaveDoneSample.setInt(7, idRelationConfiguration);

			PStmtsaveDoneSample.execute();

			PStmtsaveDoneSample.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveBooleanModel(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample, int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration, int idDatabase,
			String arffBooleanModel) {
		
		saveModels(insertBooleanMoodel,sample,sampleBuilderParameters,uselessSample,idWorkload,idVersion,idExtractor,idRelationConfiguration,idDatabase,arffBooleanModel);
	
	}

	private void saveModels(String table, Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample,
			int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration, int idDatabase, String fileModel) {
		
		try {

			PreparedStatement PStmtsaveDoneSample = getConnection().prepareStatement(table);

			PStmtsaveDoneSample.setInt(1, sample.getId());
			PStmtsaveDoneSample.setInt(2, sampleBuilderParameters.getId());
			PStmtsaveDoneSample.setInt(3, uselessSample);
			PStmtsaveDoneSample.setInt(4, idWorkload);
			PStmtsaveDoneSample.setInt(5, idVersion);
			PStmtsaveDoneSample.setInt(6, idExtractor);
			PStmtsaveDoneSample.setInt(7, idRelationConfiguration);
			PStmtsaveDoneSample.setInt(8, idDatabase);
			PStmtsaveDoneSample.setString(9, fileModel);
			
			PStmtsaveDoneSample.execute();

			PStmtsaveDoneSample.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveTrueModel(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample, int idWorkload, int idVersion, int idExtractor,
			int idRelationConfiguration, int idDatabase,
			String arffBooleanModel) {
		saveModels(insertTrueMoodel,sample,sampleBuilderParameters,uselessSample,idWorkload,idVersion,idExtractor,idRelationConfiguration,idDatabase,arffBooleanModel);
	}

	@Override
	public List<String> getBooleanModelFiles() {
		
		List<String> ret = new ArrayList<String>();

		try {

			Statement StmtgetBooleanModelFiles = getConnection().createStatement();

			ResultSet RSgetNextBooleanModelFiles = StmtgetBooleanModelFiles.executeQuery
					("select file from TrueModels");

			while (RSgetNextBooleanModelFiles.next()) {

				String file = RSgetNextBooleanModelFiles.getString(1);

				ret.add(file);
				
			}

			RSgetNextBooleanModelFiles.close();

			StmtgetBooleanModelFiles.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;				
		
	}

	@Override
	public List<Integer> getActiveSampleConfigurationIds(int activeValue) {
		
		List<Integer> ret = new ArrayList<Integer>();

		try {

			Statement StmtgetSampleConfigurationId = getConnection().createStatement();

			ResultSet RSgetSampleConfigurationId = StmtgetSampleConfigurationId.executeQuery
					("SELECT S.idSampleConfigurationFROM SampleConfiguration S WHERE S.active =  " + activeValue + "ORDER BY S.idSampleConfiguration");

			while (RSgetSampleConfigurationId.next()) {

				ret.add(RSgetSampleConfigurationId.getInt(1));

			}

			RSgetSampleConfigurationId.close();

			StmtgetSampleConfigurationId.close();

		} catch (SQLException e) {

			e.printStackTrace();

		}

		return ret;
		
	}

	public ResultSet runQuery(String query) throws SQLException {
		
		Statement StmtrunQuery = getConnection().createStatement();

		return StmtrunQuery.executeQuery
				(query);
		
	}

	@Override
	public Document getDocument(Database database, int experimentId,
			long idDocument) {
		
		Document d = null;

		try {

			PreparedStatement PStmtgetDocument = getConnection().prepareStatement(getDocumentString);

			PStmtgetDocument.setInt(1, database.getId());
			PStmtgetDocument.setInt(2, experimentId);
			PStmtgetDocument.setLong(3, idDocument);

			ResultSet RSgetDocument = PStmtgetDocument.executeQuery();

			while (RSgetDocument.next()){

				d = new Document(database, RSgetDocument.getString(1),idDocument);

			}

			RSgetDocument.close();
			PStmtgetDocument.close();

		} catch (SQLException e) {
			return null;
		}

		return d;
		
	}

	@Override
	public int getSVMWordParameter(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate) {
		
		int ret = -1;

		try {

			//			if (PStmtgetQProberParameter == null){
			//				PStmtgetQProberParameter = getConnection().prepareStatement(getQProberParameterString);
			//			}else
			//				PStmtgetQProberParameter.clearParameters();

			PreparedStatement PStmtgetSVMWordParameter = getConnection().prepareStatement(getSVMWordParameterString);

			PStmtgetSVMWordParameter.setInt(1, maxQuerySize);
			PStmtgetSVMWordParameter.setInt(2, minSupport);
			PStmtgetSVMWordParameter.setInt(3, minSuppAfterUpdate);

			ResultSet RSgetSVMWordParameter = PStmtgetSVMWordParameter.executeQuery();

			while (RSgetSVMWordParameter.next()) {

				ret = RSgetSVMWordParameter.getInt(1);

			}

			RSgetSVMWordParameter.close();
			PStmtgetSVMWordParameter.close();


		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (ret == -1){

			return insertSVMWord(maxQuerySize, minSupport, minSuppAfterUpdate);			

		}

		return ret;
		
	}

	private int insertSVMWord(int maxQuerySize, int minSupport,
			int minSuppAfterUpdate) {
		
		int ret = -1;

		try {

			//			if (PStmtinsertQProber == null){
			//				PStmtinsertQProber = getConnection().prepareStatement(insertQProberString ,Statement.RETURN_GENERATED_KEYS);
			//			}else
			//				PStmtinsertQProber.clearParameters();

			PreparedStatement PStmtinsertSVMWord = getConnection().prepareStatement(insertSVMWordString ,Statement.RETURN_GENERATED_KEYS);

			PStmtinsertSVMWord.setInt(1, maxQuerySize);
			PStmtinsertSVMWord.setInt(2, minSupport);
			PStmtinsertSVMWord.setInt(3, minSuppAfterUpdate);

			PStmtinsertSVMWord.execute();

			ResultSet rs = PStmtinsertSVMWord.getGeneratedKeys();
			if (rs != null && rs.next()) {
				ret= rs.getInt(1);
			}

			PStmtinsertSVMWord.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
		
	}

	@Override
	public List<Long> getQueriesUsedToGenerateNegativeSample(int idExperiment, Database database, int split, String navHandler, String extTechnique, String resExtTechnique) {
		
		try {

			PreparedStatement ps = getConnection().prepareStatement(queriesUsedToGenerateNegativeSampleString );

			ps.setInt(1, idExperiment);
			ps.setInt(2, database.getId());
			ps.setInt(3, split);
			ps.setInt(4, getNavigationTechniqueId(navHandler));
			ps.setInt(5, getExtractionTechniqueId(extTechnique));
			ps.setInt(6, getResultExtractionTechniqueId(resExtTechnique));

			ResultSet rs = ps.executeQuery();

			List<Long> ret = new ArrayList<Long>();
			
			while (rs.next()) {

				ret.add(rs.getLong(1));

			}

			rs.close();
			ps.close();

			return ret;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}


}
