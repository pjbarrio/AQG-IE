package execution.trunk.chart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gdata.util.common.base.Pair;

import execution.trunk.chart.classification.ClassificationFilter;
import execution.trunk.chart.classification.ExclusiveClassificationFilter;
import execution.trunk.chart.classification.filter.IntervalFilter;
import exploration.model.Database;
import exploration.model.enumerations.ClassificationClusterEnum;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.DocumentPerQueryEnum;
import exploration.model.enumerations.EntropyEnum;
import exploration.model.enumerations.InformationExtractionSystemEnum;
import exploration.model.enumerations.IntervalFilterEnum;
import exploration.model.enumerations.QueryPoolEnum;
import exploration.model.enumerations.QueryPoolExecutorEnum;
import exploration.model.enumerations.RelationConfigurationEnum;
import exploration.model.enumerations.RelationshipTypeEnum;
import exploration.model.enumerations.SampleAlgorithmEnum;
import exploration.model.enumerations.SampleExecutorEnum;
import exploration.model.enumerations.SampleGeneratorEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.enumerations.VersionEnum;
import exploration.model.enumerations.WorkloadEnum;

import plot.data.ChartData;
import plot.generator.ChartGenerator;
import plot.generator.SeriesGenerator;
import plot.generator.impl.AddedDocumentsByProcessedDocuments;
import plot.generator.impl.AddedDocumentsBySentQueries;
import plot.generator.impl.EntropyByAddedDocuments;
import plot.generator.impl.EntropyByProcessedDocuments;
import plot.generator.impl.EntropyBySentQueries;
import plot.generator.impl.IndependentUniqueTuplesBySampledDocuments;
import plot.generator.impl.UniqueTuplesByProcessedDocuments;
import plot.generator.impl.UniqueTuplesBySampledDocuments;
import plot.generator.impl.UniqueTuplesBySentQueries;
import plot.generator.impl.GeneratedQueriesByAddedDocuments;
import plot.generator.impl.IndependentAddedDocumentsByProcessedDocuments;
import plot.generator.impl.IndependentAddedDocumentsBySentQueries;
import plot.generator.impl.PrecisionAtKinDifferentStartingPositions;
import plot.generator.impl.PrecisionAtQ;
import plot.generator.impl.PrecisionsAtK;
import plot.generator.impl.ProcessedDocumentsByAddedDocuments;
import plot.generator.impl.ProcessedDocumentsByRound;
import plot.generator.impl.ProcessedDocumentsBySentQueries;
import plot.generator.impl.QueriesByRound;
import plot.generator.impl.QueriesWithNoResults;
import plot.generator.impl.RoundBySampleSize;
import plot.generator.impl.SamplesByAddedDocuments;
import plot.generator.impl.SamplesByRound;
import plot.generator.impl.SentQueriesByAddedDocuments;
import plot.generator.impl.UsefulDocumentsByQueryPosition;
import plot.generator.impl.UsefulDocumentsByRound;
import plot.selector.SampleGenerationSelector;
import plot.selector.impl.FixedSampleGenerationSelector;
import sample.generation.model.SampleConfiguration;
import searcher.interaction.formHandler.TextQuery;
import utils.CommandLineExecutor;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class SampleChartGeneration {

	class DrawRunnable implements Runnable{

		private int id;
		private SeriesGenerator<SampleGenerationSelector> series;
		private String name;
		private List<SampleGenerationSelector> exSel;
		private String prefix;
		private String folder;
		private int intervals;

		public DrawRunnable(int i,
				SeriesGenerator<SampleGenerationSelector> series, String name,
				List<SampleGenerationSelector> exSel, String prefix, String folder, int intervals) {

			this.id = i;
			this.series = series;
			this.name = name;
			this.exSel = exSel;
			this.prefix = prefix;
			this.folder = folder;
			this.intervals = intervals;

		}

		@Override
		public void run() {

			ChartGenerator<SampleGenerationSelector> cg = new ChartGenerator<SampleGenerationSelector>();

			ChartData data = cg.generateChart(name,series, exSel, prefix);

			try {
				data.plot(folder,series.getPercentX(),series.getPercentY(),intervals);

				String ret = new CommandLineExecutor().getOutput("R CMD BATCH " + data.getOutputFile() + " " + data.getOutputOutFile());

				System.out.println(data.getOutputOutFile());

			} catch (IOException e) {
				e.printStackTrace();
			}



		}

	}

	private static final int WORKLOAD_NUMBER = 17;

	//	private static final int DP_OS_RI = 51;
	//	private static final int DP_OS_RC = 52;
	//	private static final int DP_OS_RR = 53;
	//	private static final int DP_OS_RIAC = 54;
	//	private static final int DP_OS_RRA = 55;
	//	private static final int UA_OS_RI = 56;
	//	private static final int UA_OS_RC = 57;
	//	private static final int UA_OS_RR = 58;
	//	private static final int UA_OS_RIAC = 59;
	//	private static final int UA_OS_RRA = 60;
	//	private static final int DP_IW_RI = 61;
	//	private static final int DP_IW_RC = 62;
	//	private static final int DP_IW_RR = 63;
	//	private static final int DP_IW_RIAC = 64;
	//	private static final int DP_IW_RRA = 65;
	//	private static final int UA_IW_RI = 66;
	//	private static final int UA_IW_RC = 67;
	//	private static final int UA_IW_RR = 68;
	//	private static final int UA_IW_RIAC = 69;
	//	private static final int UA_IW_RRA = 70;

	//SIMPLE STRATEGIES



	//	private static final int TUPLES = 272;
	//	private static final int RELATIONS_SVM = 273;
	//	private static final int RELATIONS_SVM_TASW = 274;
	//	private static final int RULES = 275;
	//	private static final int RULES_TASW = 276;
	//	private static final int TUPLES_NO_PERSON = 277;
	//	private static final int TUPLES_NO_POSITION = 278;
	//	private static final int TUPLES_NO_COMPANY = 279;
	//	private static final int RELATIONS_IG = 280;
	//	private static final int RELATIONS_IG_TASW = 281;



	//	private static persistentWriter pW;

	//	private static String[] names = {"None","DP_OS_RI","DP_OS_RC","DP_OS_RR","DP_OS_RIAC","DP_OS_RRA","UA_OS_RI","UA_OS_RC","UA_OS_RR","UA_OS_RIAC","UA_OS_RRA",
	//									"DP_IW_RI","DP_IW_RC","DP_IW_RR","DP_IW_RIAC","DP_IW_RRA","UA_IW_RI","UA_IW_RC","UA_IW_RR","UA_IW_RIAC","UA_IW_RRA"};

	//	private static String[] names = {"TUPLES","RELATIONS_SVM","RELATIONS_SVM_TASW","RULES","RULES_TASW","TUPLES_NO_LARGE_DOMAIN","TUPLES_NO_SMALL_DOMAIN","RELATIONS_IG","RELATIONS_IG_TASW","RELATIONS_CHI2","RELATIONS_CHI2_TASW"};

	//	private static List<SampleGenerationSelector> exSel;

	//	private static String Prefix;

	private static String[] names_db = {"Inner","Close","Distant","Outer"};

	private static String[] names_agg = {"Global","Inner","Close","Distant"};

	private static String[] agg = {"Global","Cluster","PropGlobal","PropCluster"};


	/**
	 * @param args
	 * @throws IOException 
	 */

	static int uselessCountForUsefulOnly = 0;

	static int uselessCountForUselessOnly = 400;

	static InformationExtractionSystemEnum infExtSysCompareAll = InformationExtractionSystemEnum.ANY;

	static WorkloadEnum workloadCompareAll = WorkloadEnum.PERSONCAREER;

	static RelationConfigurationEnum relConfCompareAll = RelationConfigurationEnum.PERSON_S_CAREER_CRF;

	static DocumentPerQueryEnum docsperQuery = DocumentPerQueryEnum._10;

	private static DocumentPerQueryEnum docperQueryOthers = DocumentPerQueryEnum._10;

	private static DocumentPerQueryEnum docsperQuerySimple1 = DocumentPerQueryEnum._30;

	private static DocumentPerQueryEnum docsperQuerySimple2 = DocumentPerQueryEnum._1000;

	static SampleGeneratorEnum sampleGenerator = SampleGeneratorEnum.SIMPLE; // SampleGeneratorEnum.RR_CLUSTER_SAME_CARDINALITY;//

	static boolean usesAll = true;

	static boolean countAll = true;

	static VersionEnum version = VersionEnum.INDEPENDENT;

	static QueryPoolExecutorEnum queryPoolExecutor = QueryPoolExecutorEnum.SIMPLE;

	private static int interval = 1000000;

	private static int maxQueriesfixed = 1000;
	
	private static int maxQueriesAQG = 1000;
	
	private static int maxQueriestuples = 1000;
	
	//	private String prefixFolder = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Evaluation/Online/CIKMPaper/";

	private String prefixFolder = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Evaluation/Online/Quality-QQ/";


	public static void main(String[] args) throws IOException {

		int group = Integer.valueOf(args[0]);

		int groupCluster = Integer.valueOf(args[1]);

		int groupGlobal = Integer.valueOf(args[2]);

		int which = Integer.valueOf(args[3]);

		int limit = Integer.valueOf(args[4]);

		int wl = Integer.valueOf(args[5]);

		switch (limit) {
		case 10:

			docsperQuery = DocumentPerQueryEnum._10;

			break;

		case 20:
			docsperQuery = DocumentPerQueryEnum._20;

			break;
		case 30:

			docsperQuery = DocumentPerQueryEnum._30;

			break;
		case 40:

			docsperQuery = DocumentPerQueryEnum._40;

			break;
		case 50:

			docsperQuery = DocumentPerQueryEnum._50;

			break;
		case 100:

                        docsperQuery = DocumentPerQueryEnum._100;

                        break;
		case 300:

                        docsperQuery = DocumentPerQueryEnum._300;

                        break;
		case 500:

                        docsperQuery = DocumentPerQueryEnum._500;

                        break;

		default:
			break;
		}

		persistentWriter pW = PersistenceImplementation.getWriter();

		List<Integer> databases = generateListOfDatabases(pW.getSamplableDatabases(group));

		List<Integer> clustereddatabases = generateListOfDatabases(pW.getSamplableDatabases(groupCluster));

		List<Integer> globalDatabases = generateListOfDatabases(pW.getSamplableDatabases(groupGlobal));

		int ommitedValidValue = -1;

		//COMPARE ALL TECHNIQUES BASED ON ONE RELATION

		List<Thread> ts = new ArrayList<Thread>();

		List<Pair<String,Pair<List<Integer>,List<Integer>>>> sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();

		if (which == 1){

			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RELATIONS_SVM.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_SVM,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RELATIONS_SVM_TASW.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_SVM_TASW,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RULES.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RULES,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RULES_TASW.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RULES_TASW,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RELATIONS_IG.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_IG,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RELATIONS_IG_TASW.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_IG_TASW,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RELATIONS_CHI2.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.RELATIONS_CHI2_TASW.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_50.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_50,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_50_NO_LARGE_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_50_NO_LARGE_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_50_NO_SMALL_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_50_NO_SMALL_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.SIGNIFICANT_PHRASES.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.SIGNIFICANT_PHRASES,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.SIGNIFICANT_PHRASES_TASW.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.SIGNIFICANT_PHRASES_TASW,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesAQG, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));

			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("ALL_TECHNIQUES",sgsa,ommitedValidValue,workloadCompareAll,relConfCompareAll,infExtSysCompareAll, sampleGenerator, PersistenceImplementation.getNewWriter(),interval));

		}

		//		List<List<Integer>> list = createList();
		//		
		//		List<List<Integer>> listAgg = createListAgg();
		//		
		//		List<Integer> KforPrecision = new ArrayList<Integer>();
		//		
		//		List<Integer> startingPositions = new ArrayList<Integer>();
		//		
		//		for (int i = 1; i < 11; i++) {
		//			
		//			KforPrecision.add(10*i);
		//			
		//			startingPositions.add(10*i);
		//			
		//		}

		List<SampleAlgorithmEnum> sg = new ArrayList<SampleAlgorithmEnum>();

		//		sg.add(SampleAlgorithmEnum.RELATIONS_SVM);
		//		sg.add(SampleAlgorithmEnum.RELATIONS_SVM_TASW);
		//		sg.add(SampleAlgorithmEnum.RULES);
		//		sg.add(SampleAlgorithmEnum.RULES_TASW);
		//		sg.add(SampleAlgorithmEnum.RELATIONS_IG);
		//		sg.add(SampleAlgorithmEnum.RELATIONS_IG_TASW);
		//		sg.add(SampleAlgorithmEnum.RELATIONS_CHI2);
		//		sg.add(SampleAlgorithmEnum.RELATIONS_CHI2_TASW);
		//		sg.add(SampleAlgorithmEnum.TUPLES_50);
		//		sg.add(SampleAlgorithmEnum.TUPLES_50_NO_LARGE_DOMAIN);
		//		sg.add(SampleAlgorithmEnum.TUPLES_50_NO_SMALL_DOMAIN);

		//		for (int i = 0; i < sg.size(); i++) {

		//			ts.addAll(new SampleChartGeneration().createLimitComparison("ALL_TECHNIQUES",sg.get(i),ommitedValidValue,databases,workloadCompareAll,relConfCompareAll,infExtSysCompareAll,PersistenceImplementation.getNewWriter(),interval));

		//			createClusterComparison(sg.get(i),folder,ommitedValidValue,list);

		//			ts.addAll(new SampleChartGeneration().createAggregatedSampleComparison(sg.get(i),folder,ommitedValidValue,listAgg);
		//		
		//			createPrecisionAtKComparison(sg.get(i),folder,ommitedValidValue,KforPrecision);
		//			
		//			createStartingPrecisions(sg.get(i),folder,ommitedValidValue,startingPositions);

		//			ts.addAll(new SampleChartGeneration().createExecutionComparison("ALL_TECHNIQUES",sg.get(i),ommitedValidValue,docsperQuerySimple,docperQueryOthers,databases,workloadCompareAll,relConfCompareAll,infExtSysCompareAll, PersistenceImplementation.getNewWriter(), interval));

		//		}



		if (which == 2){		

			sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();

			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_10.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_10,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples,uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_10_NO_LARGE_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_10_NO_LARGE_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_10_NO_SMALL_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_10_NO_SMALL_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_20.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_20,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_20_NO_LARGE_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_20_NO_LARGE_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_20_NO_SMALL_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_20_NO_SMALL_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_30.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_30,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_30_NO_LARGE_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_30_NO_LARGE_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_30_NO_SMALL_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_30_NO_SMALL_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_40.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_40,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_40_NO_LARGE_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_40_NO_LARGE_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_40_NO_SMALL_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_40_NO_SMALL_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_50.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_50,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_50_NO_LARGE_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_50_NO_LARGE_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.TUPLES_50_NO_SMALL_DOMAIN.name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.TUPLES_50_NO_SMALL_DOMAIN,workloadCompareAll,relConfCompareAll,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriestuples, uselessCountForUsefulOnly,infExtSysCompareAll),databases)));

			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("ALL_TUPLES",sgsa,ommitedValidValue,workloadCompareAll,relConfCompareAll,infExtSysCompareAll, sampleGenerator, PersistenceImplementation.getNewWriter(),interval));
		}

		//		for (int i = 0; i < list.size(); i++) {
		//			
		//			createQueryGenerationComparison(list.get(i),sg,folder,ommitedValidValue, names_db[i]);
		//			
		//		}

		//COMPARE ALL RELATIONS

		if (which == 3){

			sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("PERSON_CAREER_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.PERSONCAREER,RelationConfigurationEnum.PERSON_S_CAREER_CRF,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("PERSON_CAREER_CHI2_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.PERSONCAREER,RelationConfigurationEnum.PERSON_S_CAREER_CRF,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("NATURAL_DISASTER_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("NATURAL_DISASTER_CHI2_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("MAN_MADE_DISASTER_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("MAN_MADE_DISASTER_CHI2_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("PERSON_TRAVEL_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.PERSONTRAVEL,RelationConfigurationEnum.PERSON_S_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("PERSON_TRAVEL_CHI2_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.PERSONTRAVEL,RelationConfigurationEnum.PERSON_S_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("CHARGE_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.INDICTMENT_ARREST_TRIAL,RelationConfigurationEnum.CHARGE_CRF_PERSON_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("CHARGE_CHI2_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.INDICTMENT_ARREST_TRIAL,RelationConfigurationEnum.CHARGE_CRF_PERSON_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("VOTING_RESULT_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.VOTINGRESULT,RelationConfigurationEnum.POLITICALEVENT_CRF_PERSON_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("VOTING_RESULT_CHI2_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.VOTINGRESULT,RelationConfigurationEnum.POLITICALEVENT_CRF_PERSON_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));

			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("ALL_RELATIONS",sgsa,ommitedValidValue,WorkloadEnum.ANY,RelationConfigurationEnum.ANY,InformationExtractionSystemEnum.ANY, sampleGenerator, PersistenceImplementation.getNewWriter(),interval));

		}

		if (which == 4 || which == 9 || which == 10 || which == 11 || which == 12 || which == 13 || which == 15 || which ==16 || which == 17 || which == 18) {	

			String id = "EXECUTION";
			QueryPoolExecutorEnum[] names = SampleIdHandler.execution_names;

			switch (which) {
			case 9: //DEFAULT FOR EDBT, for changing K

				id = "EXECUTION_VARY_K";
				names = SampleIdHandler.execution_names_for_varying_K;
				break;

			case 10: //For changing M

				id = "EXECUTION_VARY_M";
				names = SampleIdHandler.execution_names_for_varying_M;
				break;

			case 11: //For changing N 

				id = "EXECUTION_VARY_N";
				names = SampleIdHandler.execution_names_for_varying_N;
				break;

			case 12: //For changing P

				id = "EXECUTION_VARY_P";
				names = SampleIdHandler.execution_names_for_varying_P;
				break;

			case 13: //For changing MP

				id = "EXECUTION_VARY_MP";
				names = SampleIdHandler.execution_names_for_varying_MP;
				break;

			case 15: //For changing MP

				id = "EXECUTION_NOF";
				names = SampleIdHandler.execution_names_for_no_filter;
				break;
				
			case 16: //For FQXtract

				id = "EXECUTION_FQXTRACT";
				names = SampleIdHandler.execution_names_fq;
				break;

			case 17: //For Cyclic

				id = "EXECUTION_CYCLIC";
				names = SampleIdHandler.execution_names_cyclic;
				break;
				
			case 18: //For Cyclic

				id = "EXECUTION_SCHECRITERIA";
				names = SampleIdHandler.execution_names_schecriteria;
				break;
				
			default:

				break;

			}

			sg = new ArrayList<SampleAlgorithmEnum>();

			sg.add(SampleAlgorithmEnum.RELATIONS_CHI2);
			sg.add(SampleAlgorithmEnum.RELATIONS_CHI2_TASW);

			for (int i = 0; i < sg.size(); i++) {

				//				for (int j = 0; j < SampleIdHandler.workloads.length; j++) {

				ts.addAll(new SampleChartGeneration().createExecutionComparison(id,sg.get(i),ommitedValidValue,docsperQuerySimple1,docsperQuerySimple2,docsperQuery,/*docperQueryOthers,*/databases,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],infExtSysCompareAll, PersistenceImplementation.getNewWriter(),interval,names));

				//				}

			}

		}


		//USELESS

		if (which == 5){

			WorkloadEnum[] workloads = {WorkloadEnum.PERSONCAREER,WorkloadEnum.NATURALDISASTER,WorkloadEnum.MANMADEDISASTER,WorkloadEnum.PERSONTRAVEL,WorkloadEnum.VOTINGRESULT,WorkloadEnum.INDICTMENT_ARREST_TRIAL};		

			RelationConfigurationEnum[] relConfs = {RelationConfigurationEnum.PERSON_S_CAREER_CRF,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S, RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S, RelationConfigurationEnum.PERSON_S_LOCATION_S, RelationConfigurationEnum.POLITICALEVENT_CRF_PERSON_S, RelationConfigurationEnum.CHARGE_CRF_PERSON_S};

			for (int i = 0; i < relConfs.length; i++) {

				sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleAlgorithmEnum.USELESS_COLLECTION.name()+workloads[i].name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.USELESS_COLLECTION,workloads[i],relConfs[i],countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,maxQueriesAQG, uselessCountForUselessOnly,infExtSysCompareAll),databases)));

			}

			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("ALL_USELESS",sgsa,ommitedValidValue,WorkloadEnum.ANY,RelationConfigurationEnum.ANY,infExtSysCompareAll, sampleGenerator, PersistenceImplementation.getNewWriter(),interval));


		}

		//RELATION BETWEEN CLUSTER AND RELATION
		if (which == 6){
			ts.addAll(createRelationCluster(pW,ommitedValidValue,databases,SampleAlgorithmEnum.RELATIONS_CHI2_TASW,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],SampleIdHandler.relationshipTypes[wl],interval));
			ts.addAll(createRelationCluster(pW,ommitedValidValue,databases,SampleAlgorithmEnum.RELATIONS_CHI2,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],SampleIdHandler.relationshipTypes[wl],interval));

			//and where is the generation? 
		}

		//HOW CLUSTERING AFFECTS FOR DIFFERENT CLOSENESS

		if (which == 7){

			ClassificationClusterEnum[] classifications = {ClassificationClusterEnum.RELATED, ClassificationClusterEnum.SOMEWHAT_RELATED, ClassificationClusterEnum.BARELY_RELATED,ClassificationClusterEnum.NOT_RELATED};

			//			for (int j = 0; j < relConfsRC.length; j++) {

			ClassificationFilter cf = new ClassificationFilter(pW,SimilarityFunctionEnum.MANUAL_CLUSTER, ClusterFunctionEnum.CLOSENESS_CLASSIFICATION,SampleIdHandler.relationshipTypes[wl], new IntervalFilter(ClassificationClusterEnum.RELATED,0.75,1,IntervalFilterEnum.G,IntervalFilterEnum.LE), 	new IntervalFilter(ClassificationClusterEnum.SOMEWHAT_RELATED,0.5,0.75,IntervalFilterEnum.G,IntervalFilterEnum.LE),new IntervalFilter(ClassificationClusterEnum.BARELY_RELATED,0.25,0.5,IntervalFilterEnum.G,IntervalFilterEnum.LE),
					new IntervalFilter(ClassificationClusterEnum.NOT_RELATED,0.0,0.25,IntervalFilterEnum.GE,IntervalFilterEnum.LE));

			List<Integer> clust = generateListOfDatabases(pW.getClusterDatbases(ClusterFunctionEnum.CLOSENESS_CLASSIFICATION, SampleIdHandler.getRelationshipTypeId(SampleIdHandler.relationshipTypes[wl])));

			sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();

			for (int i = 0; i < classifications.length; i++) {

				List<Integer> dats = cf.filter(classifications[i],databases);

				sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("LOCAL_" + classifications[i].name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),dats)));

				sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("CLUSTER_" + classifications[i].name(),new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_CLUSTER_CLOSENESS_SAME_CARDINALITY,usesAll,version,queryPoolExecutor,docsperQuery,maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),cf.filterClusters(classifications[i],clust))));

				ts.addAll(new SampleChartGeneration().createExecutionComparison("CLOSENESS_"+ classifications[i].name()+"_EXECUTION",SampleAlgorithmEnum.RELATIONS_CHI2_TASW,ommitedValidValue,docsperQuery,docsperQuery,docsperQuery,dats,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],infExtSysCompareAll, PersistenceImplementation.getNewWriter(), interval,SampleIdHandler.execution_names));

			}

			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("CLOSENESS",sgsa,ommitedValidValue,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],infExtSysCompareAll, SampleGeneratorEnum.ANY, PersistenceImplementation.getNewWriter(),interval));

			//			}

		}


		//COMPARE CLUSTER-GLOBAL-LOCAL

		/*

				WorkloadEnum[] workloads = {WorkloadEnum.PERSONCAREER,WorkloadEnum.NATURALDISASTER};		

				RelationConfigurationEnum[] relConfs = {RelationConfigurationEnum.PERSON_S_CAREER_CRF,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S};

				String[] name = {"Person_Career","Natural_Disaster"};
		 */

		if (which == 8){

			sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();

			//					for (int i = 0; i < name.length; i++) {

			//sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_SIMPLE",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.SIMPLE,usesAll,version,queryPoolExecutor,docsperQuery,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_CLUSTER_C",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_CLUSTER_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.CYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),clustereddatabases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_GLOBAL_C",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_GLOBAL_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.CYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),globalDatabases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_CLUSTER_SC",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_CLUSTER_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.SMARTCYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),clustereddatabases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_GLOBAL_SC",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_GLOBAL_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.SMARTCYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),globalDatabases)));

			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_CLUSTER_C_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_CLUSTER_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.CYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),clustereddatabases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_GLOBAL_C_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_GLOBAL_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.CYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),globalDatabases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_CLUSTER_SC_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_CLUSTER_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.SMARTCYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),clustereddatabases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>(SampleIdHandler.workloads[wl].name() + "_GLOBAL_SC_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],countAll,SampleGeneratorEnum.RR_GLOBAL_SAME_CARDINALITY,usesAll,version,QueryPoolExecutorEnum.SMARTCYCLIC,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),globalDatabases)));

			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("AGGREGATED",sgsa,ommitedValidValue,SampleIdHandler.workloads[wl],SampleIdHandler.relationConfs[wl],InformationExtractionSystemEnum.ANY,SampleGeneratorEnum.ANY,PersistenceImplementation.getNewWriter(),interval));

			//					}

		}

		if (which == 14){
			
			queryPoolExecutor = QueryPoolExecutorEnum.CYCLIC;
			
			sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_50",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,50,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_50_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,50,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_100",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,100,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_100_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,100,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_150",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,150,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_150_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,150,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_200",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,200,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_200_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,200,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_250",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,250,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_250_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,250,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_300_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,300,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_300_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,300,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_350",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,350,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_350_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,350,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_400",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,400,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_400_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,400,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_450",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,450,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_450_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,450,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_500_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,500,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_500_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.NATURALDISASTER,RelationConfigurationEnum.NATURALDISASTER_SVM_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,500,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));

			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("ALL_MAX_QUERIES",sgsa,ommitedValidValue,WorkloadEnum.ANY,RelationConfigurationEnum.ANY,InformationExtractionSystemEnum.ANY, sampleGenerator, PersistenceImplementation.getNewWriter(),interval));

			
		}
		
		if (which == 19){
			
			queryPoolExecutor = QueryPoolExecutorEnum.CYCLIC;
			
			sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_100_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,100,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_100_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,100,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_200_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,200,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_200_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,200,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_300_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,300,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_300_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,300,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_400_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,400,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_400_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor ,docsperQuery,400,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_500_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,500,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("Q_500_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,500,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));

			//Add those of other query pools with 500 max queries
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_100_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_100,docsperQuery,100,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_100_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_100,docsperQuery,100,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_200_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_200,docsperQuery,200,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_200_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_200,docsperQuery,200,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_300_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_300,docsperQuery,300,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_300_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_300,docsperQuery,300,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_400_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_400,docsperQuery,400,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
//			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_400_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_400,docsperQuery,400,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_500_CHI2",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_500,docsperQuery,500,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));
			sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RQ_500_TASW",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(SampleAlgorithmEnum.RELATIONS_CHI2_TASW,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,countAll,sampleGenerator,usesAll,version,QueryPoolExecutorEnum.R_CYCLIC_500,docsperQuery,500,uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),databases)));

			
			ts.addAll(new SampleChartGeneration().createSampleGenerationChart("ALL_QEXECUTIONORDERS_QUERIES",sgsa,ommitedValidValue,WorkloadEnum.MANMADEDISASTER,RelationConfigurationEnum.MANMADEDISASTER_CRF_LOCATION_S,InformationExtractionSystemEnum.ANY, sampleGenerator, PersistenceImplementation.getNewWriter(),interval));

			
		}
		
		for (Thread thread : ts) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private static List<Thread> createRelationCluster(persistentWriter pW, int ommitedValidValue, List<Integer> databases, SampleAlgorithmEnum sa, WorkloadEnum wl, RelationConfigurationEnum rc, RelationshipTypeEnum relation, int intervals) throws IOException {

		ClassificationFilter cf = new ExclusiveClassificationFilter(pW,SimilarityFunctionEnum.MANUAL_CLUSTER, ClusterFunctionEnum.CLASSIFICATION, relation, new IntervalFilter(ClassificationClusterEnum.RELATED,0.75,1,IntervalFilterEnum.G,IntervalFilterEnum.LE), 	new IntervalFilter(ClassificationClusterEnum.SOMEWHAT_RELATED,0.5,0.75,IntervalFilterEnum.G,IntervalFilterEnum.LE),new IntervalFilter(ClassificationClusterEnum.BARELY_RELATED,0.25,0.5,IntervalFilterEnum.G,IntervalFilterEnum.LE),
				new IntervalFilter(ClassificationClusterEnum.NOT_RELATED,0.0,0.25,IntervalFilterEnum.GE,IntervalFilterEnum.LE));

		List<Pair<String,Pair<List<Integer>,List<Integer>>>> sgsa = new ArrayList<Pair<String,Pair<List<Integer>,List<Integer>>>>();

		sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("RELATED",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(sa,wl,rc,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),cf.filter(ClassificationClusterEnum.RELATED,databases))));

		sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("SOMEWHAT_RELATED",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(sa,wl,rc,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),cf.filter(ClassificationClusterEnum.SOMEWHAT_RELATED,databases))));

		sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("BARELY_RELATED",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(sa,wl,rc,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery,maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),cf.filter(ClassificationClusterEnum.BARELY_RELATED,databases))));

		sgsa.add(new Pair<String,Pair<List<Integer>,List<Integer>>>("NOT_RELATED",new Pair<List<Integer>,List<Integer>>(SampleIdHandler.getSampleGeneration(sa,wl,rc,countAll,sampleGenerator,usesAll,version,queryPoolExecutor,docsperQuery, maxQueriesfixed, uselessCountForUsefulOnly,InformationExtractionSystemEnum.ANY),cf.filter(ClassificationClusterEnum.NOT_RELATED,databases))));

		return new SampleChartGeneration().createSampleGenerationChart("ClusterSimilarity_" + sa.name(),sgsa,ommitedValidValue,wl,rc,infExtSysCompareAll, sampleGenerator, PersistenceImplementation.getNewWriter(), intervals);

	}

	public static List<Integer> generateListOfDatabases(
			List<Database> databases) {

		List<Integer> ret = new ArrayList<Integer>(databases.size());

		for (int i = 0; i < databases.size(); i++) {

			ret.add(databases.get(i).getId());

		}

		return ret;


	}

	private static void createAggregatedSampleComparison(Integer sampleConfiguration,
			String folder, int ommitedValidValue, List<List<Integer>> listAgg) throws IOException {

		for (int i = 0; i < agg.length; i++) {

			CreateAggregateClusterComparison(sampleConfiguration,ommitedValidValue,listAgg,488+i);

			draw(folder + "Positive-Documents-Retrieval-Clustered-Aggregated" + "/");

		}



	}

	private static void CreateAggregateClusterComparison(
			Integer sampleConfiguration, int ommitedValidValue,
			List<List<Integer>> listAgg, int sc) {

		Prefix = agg[sc-488] + "_" + getSimpleName(sampleConfiguration) + "_ClusteredGeneration";

		exSel = new ArrayList<SampleGenerationSelector>();

		for (int i = 0; i < listAgg.size(); i++) {
			exSel.add(new FixedSampleGenerationSelector(sc, names_agg [i],pW,ommitedValidValue,listAgg.get(i)));
		}

	}

	private static List<List<Integer>> createListAgg() {

		List<List<Integer>> list = new ArrayList<List<Integer>>();

		List<Integer> global = new ArrayList<Integer>();

		global.add(2854);

		List<Integer> almost_no_related = new ArrayList<Integer>();


		almost_no_related.add(2867);
		almost_no_related.add(2861);
		almost_no_related.add(2863);

		List<Integer> almost_related = new ArrayList<Integer>();

		almost_related.add(2857);
		almost_related.add(2859);
		almost_related.add(2865);

		List<Integer> related = new ArrayList<Integer>();

		related.add(2860);
		related.add(2862);
		related.add(2858);

		list.add(global);
		list.add(related);
		list.add(almost_related);
		list.add(almost_no_related);


		return list;

	}


	private List<Thread> createExecutionComparison(String experimentId, SampleAlgorithmEnum sampleAlgorithm,int ommitedValidValue, DocumentPerQueryEnum limit_simple1, DocumentPerQueryEnum limit_simple2, DocumentPerQueryEnum limit_others, List<Integer> databases, WorkloadEnum workload,
			RelationConfigurationEnum relConf, InformationExtractionSystemEnum infExtSys, persistentWriter pW, int intervals, QueryPoolExecutorEnum[] execution_names) throws IOException {

		String folder = prefixFolder + workload.name() +
				"_" + relConf.name() + "/" + version.name() + "_ALL_EXECUTIONS" + "/" + sampleGenerator.name() + "_" + countAll + "_" + limit_simple1.name() + "_" + limit_simple2 + "_" + limit_others.name() + "/" + infExtSys.name() + "/" + experimentId + "/";

		String Prefix = sampleAlgorithm.name() + "_Execution";

		List<SampleGenerationSelector> exSel = new ArrayList<SampleGenerationSelector>();

		for (int i = 0; i < execution_names.length; i++) {

			List<Integer> sg;

			if (execution_names[i].equals(QueryPoolExecutorEnum.SIMPLE)){
				sg = SampleIdHandler.getSampleGeneration(sampleAlgorithm, workload, relConf, countAll, sampleGenerator, usesAll, version, execution_names[i], limit_simple1, maxQueriesfixed, uselessCountForUsefulOnly, infExtSys);
				exSel.add(new FixedSampleGenerationSelector(sg, execution_names[i].name() + "first",pW,ommitedValidValue,databases));

				if (limit_simple1 != limit_simple2){
					sg = SampleIdHandler.getSampleGeneration(sampleAlgorithm, workload, relConf, countAll, sampleGenerator, usesAll, version, execution_names[i], limit_simple2, maxQueriesfixed, uselessCountForUsefulOnly, infExtSys);
					exSel.add(new FixedSampleGenerationSelector(sg, execution_names[i].name() + "second",pW,ommitedValidValue,databases));
				}
			}
			else{
				sg = SampleIdHandler.getSampleGeneration(sampleAlgorithm, workload, relConf, countAll, sampleGenerator, usesAll, version, execution_names[i], limit_others, maxQueriesfixed, uselessCountForUsefulOnly, infExtSys);
				exSel.add(new FixedSampleGenerationSelector(sg, execution_names[i].name(),pW,ommitedValidValue,databases));

			}
		}

		return draw(folder + "Positive-Documents-Executions-Limit-40/",exSel,Prefix,pW, intervals);

	}

	private static void createStartingPrecisions(Integer sampleConfiguration,
			String folder, int ommitedValidValue,
			List<Integer> startingPositions) throws IOException {

		Prefix = getSimpleName(sampleConfiguration) + "_PatK";

		exSel = new ArrayList<SampleGenerationSelector>();

		exSel.add(new FixedSampleGenerationSelector(sampleConfiguration, "StartingAt",pW,ommitedValidValue));

		List<SeriesGenerator<SampleGenerationSelector>> series = new ArrayList<SeriesGenerator<SampleGenerationSelector>>();

		series.add(new PrecisionAtKinDifferentStartingPositions(startingPositions));

		draw(folder + "Precision_At/",series);

	}

	private static void createPrecisionAtKComparison(Integer sampleConfiguration,
			String folder, int ommitedValidValue, List<Integer> kforPrecision) throws IOException {

		Prefix = getSimpleName(sampleConfiguration) + "_PatKGeneration";

		exSel = new ArrayList<SampleGenerationSelector>();

		exSel.add(new FixedSampleGenerationSelector(sampleConfiguration, "PrecisionAt",pW,ommitedValidValue));

		List<SeriesGenerator<SampleGenerationSelector>> series = new ArrayList<SeriesGenerator<SampleGenerationSelector>>();

		series.add(new PrecisionsAtK(kforPrecision));

		draw(folder + "Precision_At/",series);

	}

	private static void draw(String folder,List<SeriesGenerator<SampleGenerationSelector>> series) throws IOException {

		new File(folder).mkdirs();

		ChartGenerator<SampleGenerationSelector> cg = new ChartGenerator<SampleGenerationSelector>();

		//			{,new UsefulAddedDocumentsBySentQueries(),new UselessDocumentsBySentQueries()};

		String[] namesPrefix = {Prefix + "PrecisionsAtK"};

		for (int i = 0; i < exSel.size(); i++){

			for (int j = 0; j < series.size(); j++) {

				ChartData data = cg.generateChart(namesPrefix[i],series.get(j), exSel.get(i), Prefix);

				data.plot(folder,series.get(j).getPercentX(),series.get(j).getPercentY());

				String ret = new CommandLineExecutor().getOutput("R CMD BATCH " + data.getOutputFile() + " " + data.getOutputOutFile());

			}

		}

	}

	private List<Thread> createLimitComparison(String experimentId, SampleAlgorithmEnum sampleAlgorithm, 
			int ommitedValidValue,List<Integer> databases, WorkloadEnum workload, RelationConfigurationEnum relConf, InformationExtractionSystemEnum infExtSys, persistentWriter pW, int intervals) throws IOException {

		String folder = prefixFolder + workload.name() +
				"_" + relConf.name() + "/" + version.name() + "_" + queryPoolExecutor.name() + "/" + sampleGenerator.name() + "_" + countAll + "_ALL_LIMITS" + "/" + infExtSys.name() + "/" + experimentId + "/";

		return CreateLimitComparison(sampleAlgorithm,ommitedValidValue,databases, workload,relConf,infExtSys,folder,queryPoolExecutor,pW, intervals);

	}

	private List<Thread> CreateLimitComparison(SampleAlgorithmEnum sampleAlgorithm,
			int ommitedValidValue, List<Integer> databases, WorkloadEnum workload, RelationConfigurationEnum relConf, InformationExtractionSystemEnum infExtSys, String folder, QueryPoolExecutorEnum executor, persistentWriter pW, int intervals) throws IOException {

		String Prefix = sampleAlgorithm.name() + "_Limits";

		List<SampleGenerationSelector> exSel = new ArrayList<SampleGenerationSelector>();

		for (int i = 0; i < SampleIdHandler.limits.length; i++) {
			exSel.add(new FixedSampleGenerationSelector(SampleIdHandler.getSampleGeneration(sampleAlgorithm, workload, relConf, countAll, sampleGenerator,usesAll, version, executor, SampleIdHandler.limits[i], maxQueriesfixed, uselessCountForUsefulOnly, infExtSys), "Limit" + SampleIdHandler.limits[i].name(),pW,ommitedValidValue,databases));
		}

		return draw(folder + "Positive-Documents-Retrieval-Limits" + "/",exSel,Prefix,pW, intervals);
	}


	private static void createClusterComparison(Integer sampleConfiguration,
			String folder, int ommitedValidValue, List<List<Integer>> list) throws IOException {

		CreateClusterComparison(sampleConfiguration,ommitedValidValue,list);

		draw(folder + "Positive-Documents-Retrieval-Clustered" + "/");

	}

	private static void CreateClusterComparison(
			List<Integer> sampleConfiguration, int ommitedValidValue,
			List<List<Integer>> list) {

		Prefix = getSimpleName(sampleConfiguration) + "_ClusteredGeneration";

		exSel = new ArrayList<SampleGenerationSelector>();

		for (int i = 0; i < list.size(); i++) {
			exSel.add(new FixedSampleGenerationSelector(sampleConfiguration, names_db [i],pW,ommitedValidValue,list.get(i)));
		}

	}

	private List<Thread> draw(String folder, List<SampleGenerationSelector> exSel, String Prefix, persistentWriter pW, int intervals) throws IOException {

		new File(folder).mkdirs();


		//		List<SeriesGenerator<SampleGenerationSelector>> series = new ArrayList<SeriesGenerator<SampleGenerationSelector>>();

		List<Pair<SeriesGenerator<SampleGenerationSelector>, String>> ser_pair = new ArrayList<Pair<SeriesGenerator<SampleGenerationSelector>,String>>();

		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new SamplesByAddedDocuments(),Prefix + "SamplesByAddedDocument"));
		
		//General
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new ProcessedDocumentsBySentQueries(),Prefix + "ProcessedDocumentsBySentQueries"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new AddedDocumentsByProcessedDocuments(SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "AddedDocumentByProcessedDocuments"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new AddedDocumentsBySentQueries(),Prefix + "AddedDocumentsBySentQueries"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new ProcessedDocumentsByAddedDocuments(SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "ProcessedDocumentsByAddedDocuments"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new SentQueriesByAddedDocuments(),Prefix + "SentQueriesByAddedDocuments"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyByAddedDocuments(pW,EntropyEnum.ANY),Prefix + "EntropyByAddedDocuments"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyByAddedDocuments(pW,EntropyEnum.LARGE),Prefix + "EntropyByAddedDocuments_LARGE"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyByAddedDocuments(pW,EntropyEnum.SMALL),Prefix + "EntropyByAddedDocuments_SMALL"));
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new SamplesByAddedDocuments(),Prefix + "SamplesByAddedDocument"));

//*		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyByProcessedDocuments(pW,EntropyEnum.ANY,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "EntropyByProcessedDocuments"));
//*		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyBySentQueries(pW,EntropyEnum.ANY),Prefix + "EntropyBySentQueries"));
//*		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyByProcessedDocuments(pW,EntropyEnum.LARGE,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "EntropyByProcessedDocuments_LARGE"));
//*		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyBySentQueries(pW,EntropyEnum.LARGE),Prefix + "EntropyBySentQueries_LARGE"));
//*		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyByProcessedDocuments(pW,EntropyEnum.SMALL,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "EntropyByProcessedDocuments_SMALL"));
//*		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new EntropyBySentQueries(pW,EntropyEnum.SMALL),Prefix + "EntropyBySentQueries_SMALL"));

		//Unique Tuples by Sampled
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesBySampledDocuments(pW,EntropyEnum.ANY,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "UniqueTuplesBySampledDocuments"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesBySampledDocuments(pW,EntropyEnum.LARGE,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "UniqueTuplesBySampledDocuments_LARGE"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesBySampledDocuments(pW,EntropyEnum.SMALL,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "UniqueTuplesBySampledDocuments_SMALL"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentUniqueTuplesBySampledDocuments(pW,EntropyEnum.ANY,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "IndependentUniqueTuplesBySampledDocuments"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentUniqueTuplesBySampledDocuments(pW,EntropyEnum.LARGE,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "IndependentUniqueTuplesBySampledDocuments_LARGE"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentUniqueTuplesBySampledDocuments(pW,EntropyEnum.SMALL,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "IndependentUniqueTuplesBySampledDocuments_SMALL"));
		
		
		//Unique Tuples
		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesByProcessedDocuments(pW,EntropyEnum.ANY,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "UniqueTuplesByProcessedDocuments"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesBySentQueries(pW,EntropyEnum.ANY),Prefix + "UniqueTuplesBySentQueries"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesByProcessedDocuments(pW,EntropyEnum.LARGE,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "UniqueTuplesByProcessedDocuments_LARGE"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesBySentQueries(pW,EntropyEnum.LARGE),Prefix + "UniqueTuplesBySentQueries_LARGE"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesByProcessedDocuments(pW,EntropyEnum.SMALL,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "UniqueTuplesByProcessedDocuments_SMALL"));
        ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UniqueTuplesBySentQueries(pW,EntropyEnum.SMALL),Prefix + "UniqueTuplesBySentQueries_SMALL"));


		//Normalized
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new AddedDocumentsByProcessedDocuments(SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.TOTAL_DOCUMENTS,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NORMALIZED),Prefix + "AddedDocumentByProcessedDocumentsNormalized"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new ProcessedDocumentsByAddedDocuments(SeriesGenerator.TOTAL_DOCUMENTS,SeriesGenerator.NO_NORMALIZATION),Prefix + "ProcessedDocumentsByAddedDocumentsNormalized"));


		//Independent Statistics
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentAddedDocumentsByProcessedDocuments(SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NUMBER_OF_PROCESSED_DOCUMENTS_NUMBER),Prefix + "IndependentAddedDocumentByProcessedDocuments"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentAddedDocumentsBySentQueries(),Prefix + "IndependentAddedDocumentsBySentQueries"));
				ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentProcessedDocumentsByAddedDocuments(SeriesGenerator.NO_NORMALIZATION,SeriesGenerator.NO_NORMALIZATION),Prefix + "IndependentProcessedDocumentsByAddedDocuments"));
				ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentSentQueriesByAddedDocuments(),Prefix + "IndependentSentQueriesByAddedDocuments"));

		//Independent and Normalized
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new IndependentProcessedDocumentsByAddedDocuments(SeriesGenerator.TOTAL_DOCUMENTS,SeriesGenerator.NO_NORMALIZATION),Prefix + "IndependentProcessedDocumentsByAddedDocumentsNormalized"));

		//Round Statistics

		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UsefulDocumentsByRound(),Prefix + "UsefulDocumentsByRound"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new ProcessedDocumentsByRound(),Prefix + "ProcessedDocumentsByRound"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new SamplesByRound(),Prefix + "SamplesByRound"));

		//Miscelaneous Statistics		

		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new QueriesWithNoResults(),Prefix + "QueriesWithNoResults"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new QueriesByRound(),Prefix + "QueriesByRound"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new RoundBySampleSize(),Prefix + "RoundBySampleSize"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new GeneratedQueriesByAddedDocuments(),Prefix + "GeneratedQueriesByAddedDocuments"));
		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new UsefulDocumentsByQueryPosition(),Prefix + "UsefulDocumentsByQueryPosition"));

		//		ser_pair.add(new Pair<SeriesGenerator<SampleGenerationSelector>, String>(new PrecisionAtQ(),Prefix + "PrecisionAtQ"));



		//			{,new UsefulAddedDocumentsBySentQueries(),new UselessDocumentsBySentQueries()};

		List<Thread> ts = new ArrayList<Thread>();

		for (int i = 0; i < ser_pair.size(); i++){

			Thread t = new Thread(new SampleChartGeneration().new DrawRunnable(i,ser_pair.get(i).getFirst(),ser_pair.get(i).getSecond(),exSel,Prefix,folder,intervals));

			ts.add(t);

			t.start();

		}

		return ts;

	}

	private List<Thread> createSampleGenerationChart(String experimentId, List<Pair<String,Pair<List<Integer>,List<Integer>>>> sampleGeneration, int ommitedValidValue, WorkloadEnum workload,RelationConfigurationEnum relConf, InformationExtractionSystemEnum infExtSys, SampleGeneratorEnum sampleGenerator, persistentWriter pW, int intervals) throws IOException {

		String folder = prefixFolder  + workload.name() +
				"_" + relConf.name() + "/" + version.name() + "_" + queryPoolExecutor.name() + "/" + sampleGenerator.name() + "_" + countAll + "_" + docsperQuery + "/" + infExtSys.name() + "/" + experimentId + "/";

		return CreateSampleGenerationComparison(sampleGeneration,ommitedValidValue,folder,pW, intervals);

	}

	private List<Thread> CreateSampleGenerationComparison(
			List<Pair<String,Pair<List<Integer>,List<Integer>>>> sg, int ommitedValidValue, String folder, persistentWriter pW, int intervals) throws IOException {

		String Prefix = "Generation";

		List<SampleGenerationSelector> exSel = new ArrayList<SampleGenerationSelector>();

		for (Pair<String,Pair<List<Integer>,List<Integer>>> configuration : sg) {
			exSel.add(new FixedSampleGenerationSelector(configuration.getSecond().getFirst(), configuration.getFirst(),pW,ommitedValidValue,configuration.getSecond().getSecond()));
		}

		return draw(folder + "Positive-Documents-Retrieval" + "/",exSel,Prefix, pW, intervals);

	}

	private static void createQueryGenerationComparison(List<Integer> databases,
			List<Integer> sg, String folder, int ommitedValidValue, String prefix) throws IOException {

		CreateQueryGenerationComparison(databases,sg,ommitedValidValue,prefix);

		draw(folder + "Positive-Documents-Retrieval-Algorithm-Clustered" + "/");

	}

	private static void CreateQueryGenerationComparison(List<Integer> databases,
			List<Integer> sg, int ommitedValidValue, String prefix) {

		Prefix = prefix + "_Generation";

		exSel = new ArrayList<SampleGenerationSelector>();

		for (Integer configuration : sg) {
			exSel.add(new FixedSampleGenerationSelector(configuration, getSimpleName(configuration),pW,ommitedValidValue,databases));
		}


	}


}
