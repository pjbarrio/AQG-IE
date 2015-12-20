package execution.trunk;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;

import sample.generation.factory.InferredTypeFactory;
import sample.generation.factory.OmittedAttributeFactory;
import sample.generation.model.SampleBuilderParameters;
import sample.generation.model.SampleConfiguration;
import sample.generation.model.impl.DummySampleBuilderParameters;
import sample.generation.model.impl.DummySampleConfiguration;
import techniques.algorithms.Incremental;
import techniques.algorithms.MSC;
import techniques.algorithms.Optimistic;
import techniques.algorithms.QProber;
import techniques.algorithms.QXtract;
import techniques.algorithms.Ripper;
import techniques.algorithms.SVMWord;
import techniques.algorithms.SignificantPhrases;
import techniques.algorithms.TupleQueries;
import techniques.input.Params;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import execution.workload.impl.WorkLoad;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.ExperimentStatusEnum;
import exploration.model.enumerations.OmittedAttributeEnum;

public class GenerationAlgorithmExecutor {

	private final static int QPROBER = 12;
	private final static int RIPPER = 13;
	private final static int OPTIMISTIC = 14;
	private final static int MSC = 15;
	private final static int INCREMENTAL = 16;
	private static final int QXTRACT = 24;
	private static final int SIGNIFICANTPHRASES = 25;
	private static final int TUPLES = 26;
	private static final int TUPLES_LARGE = 27;
	private static final int TUPLES_SMALL = 28;
	private static final int SVM_WORD = 29;


	private static persistentWriter pW;
	private static Instances dataTrue;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		String relation;

		int group = Integer.valueOf(args[0]);

		String algorithmsString = args[1];

		int[] algorithms = SampleBuilder.generateArray(new String[]{algorithmsString});

		int idExtractor = Integer.valueOf(args[2]);

		int idRelationConfiguration = Integer.valueOf(args[3]);

		int idWorkload = Integer.valueOf(args[4]);

		int activeValue = Integer.valueOf(args[5]);

		int[] sbps = SampleBuilder.generateArray(Arrays.copyOfRange(args, 6, args.length));

		pW = PersistenceImplementation.getWriter();

		SampleBuilderParameters[] sp = new SampleBuilderParameters[sbps.length];

		for (int i = 0; i < sp.length; i++) {
			sp[i] = new DummySampleBuilderParameters(sbps[i]);
		}

		WorkloadModel wkload = pW.getWorkloadModel(idWorkload);

		String[] version = {"INDEPENDENT"};

		//		List<Integer> sample_configuration = pW.getActiveSampleConfigurationIds(activeValue); 

		List<SampleConfiguration> sample_configuration = pW.getActiveSampleConfigurations(activeValue);

		List<Database> database = pW.getSamplableDatabases(group);

		Collections.shuffle(database);

		int[] version_seed_pos = {1,2,3,4,5};

		for (int j = 0; j < database.size(); j++) {

			for (int sconf = 0; sconf < sample_configuration.size(); sconf++) {

				Database dbase = database.get(j);

				for (int s = 0; s < version_seed_pos.length; s++) {

					for (int i = 0; i < version.length; i++) {

						Version vers = Version.generateInstance(version[i], wkload);

						for (int p = 0; p < sp.length ; p++){

							Sample sample = Sample.getSample(dbase,vers,wkload,version_seed_pos[s],1 /*When looking for positive samples*/,sample_configuration.get(sconf));

							Map<Integer, Pair<Integer, Integer>> ulessSamplesComp = pW.getUselessSamples(wkload.getId(),vers.getId(),idExtractor,idRelationConfiguration,sp[j].getUselessDocuments(), dbase.getId()); 

							Map<Integer,List<Integer>> ulessSmp = SampleBuilder.generateByRQP(ulessSamplesComp);

							List<Integer> ulessSamples = ulessSmp.get(sample_configuration.get(sconf).getResultsPerQuery());

							Collections.sort(ulessSamples);

							for (int n = 0; n < ulessSamples.size(); n++) {

								System.err.println("Be careful with the multithread");

								sample.setVersionSeedNeg(ulessSamplesComp.get(ulessSamples.get(n)).getSecond());

								for (int alg = 0; alg < algorithms.length; alg++) {

									int algorithm = algorithms[alg];

									if (pW.hasGeneratedQueries(sample.getId(),algorithm,sp[p]))
										continue;

									pW.writeGeneratedQueries(sample.getId(),sp[p],algorithm,ExperimentStatusEnum.RUNNING);

									// int[] version_seed_neg = {1,2,3,4,5}; should find the corresponding numbers for each db

									switch (algorithm) {
									case QPROBER:

										runQProber(sample,sp[p],ulessSamples.get(n));
										break;

									case RIPPER:
										runRipper(sample,sp[p],ulessSamples.get(n));
										break;
									case OPTIMISTIC:
										runOptimistic(sample,sp[p],ulessSamples.get(n));
										break;
									case MSC:
										runMSC(sample,sp[p],ulessSamples.get(n));
										break;
									case INCREMENTAL:
										runIncremental(sample,sp[p],ulessSamples.get(n));
										break;
									case QXTRACT:
										runQXtract(sample,sp[p],ulessSamples.get(n));
										break;
									case SIGNIFICANTPHRASES:
										runSignificantPhrases(sample,sp[p],ulessSamples.get(n));
										break;
									case TUPLES: /*Needs only one, since useless does not count*/

										relation = RelationConfiguration.getRelationNameFromConf(sample.getSampleConfiguration().getRelationConfiguration());

										runTuples(sample,sp[p],ulessSamples.get(n),"TUPLES",new TupleQueryGenerator(InferredTypeFactory.getInferredTypes(relation,pW), sp[p].getUnique(), sp[p].getLowerCase(), sp[p].getStemmed(), OmittedAttributeFactory.generateList(pW,relation,OmittedAttributeEnum.NONE.name())));
										break;
									case TUPLES_LARGE: /*Needs only one, since useless does not count*/

										relation = RelationConfiguration.getRelationNameFromConf(sample.getSampleConfiguration().getRelationConfiguration());


										runTuples(sample,sp[p],ulessSamples.get(n),"TUPLES_LARGE",new TupleQueryGenerator(InferredTypeFactory.getInferredTypes(relation,pW), sp[p].getUnique(), sp[p].getLowerCase(), sp[p].getStemmed(), OmittedAttributeFactory.generateList(pW,relation,OmittedAttributeEnum.LARGE_DOMAIN.name())));
										break;
									case TUPLES_SMALL: /*Needs only one, since useless does not count*/

										relation = RelationConfiguration.getRelationNameFromConf(sample.getSampleConfiguration().getRelationConfiguration());


										runTuples(sample,sp[p],ulessSamples.get(n),"TUPLES_SMALL",new TupleQueryGenerator(InferredTypeFactory.getInferredTypes(relation,pW), sp[p].getUnique(), sp[p].getLowerCase(), sp[p].getStemmed(), OmittedAttributeFactory.generateList(pW,relation,OmittedAttributeEnum.SMALL_DOMAIN.name())));
										break;
									case SVM_WORD:
										runSvmWord(sample,sp[p],ulessSamples.get(n));
										break;
									
									default:
										break;
									}

									pW.writeGeneratedQueries(sample.getId(),sp[p],algorithm,ExperimentStatusEnum.FINISHED);
									
								}

							}

							Runtime.getRuntime().gc();

						}

					}

				}

			}

		}



	}

	private static void runSvmWord(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, Integer uselessSample) throws Exception {
		
		String arffFile = pW.getArffBooleanModel(sample,sampleBuilderParameters,uselessSample);

		Instances data = myArffHandler.loadInstances(arffFile);

		new SVMWord(sample, -1, -1, -1).executeAlgorithm(data,pW,sampleBuilderParameters);
		
	}

	private static void runTuples(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample,
			String name, TupleQueryGenerator tupleQueryGenerator) throws NumberFormatException, Exception {

		new TupleQueries(sample, Integer.valueOf(Params.getMaxQuerySize(sample.getDatabase().getName())), Integer.valueOf(Params.getMinSupport(sample.getDatabase().getName())), Integer.valueOf(Params.getMinSupportAfterUpdate(sample.getDatabase().getName())), name, tupleQueryGenerator).executeAlgorithm(null, pW, sampleBuilderParameters);

	}

	private static void runSignificantPhrases(Sample sample,
			SampleBuilderParameters sampleBuilderParameters, int uselessSample) throws Exception {

		int max_query_size = Integer.valueOf(Params.getMaxQuerySize(sample.getDatabase().getName()));

		int min_support = Integer.valueOf(Params.getMinSupport(sample.getDatabase().getName()));

		int min_supp_after_update = Integer.valueOf(Params.getMinSupportAfterUpdate(sample.getDatabase().getName()));

		int ngrams = 2;

		ContentExtractor ce = new TikaContentExtractor();

		int generatedQueries = 1000;

		new SignificantPhrases(sample, max_query_size, min_support, min_supp_after_update, ngrams, ce, generatedQueries).executeAlgorithm(null, pW, sampleBuilderParameters);

	}

	private static void runOptimistic(Sample sample, SampleBuilderParameters sp, int uselessSample) throws Exception {

		Instances data = myArffHandler.loadInstances(pW.getArffBooleanModel(sample,sp,uselessSample));

		Instances dataTrue = myArffHandler.loadInstances(pW.getArffBooleanTrueModel(sample,sp,uselessSample));

		double threshold = Double.valueOf(Params.getThresholdLost(sample.getDatabase().getName()));  

		int min_supp = Integer.valueOf(Params.getMinSupport(sample.getDatabase().getName()));

		double supp = Params.getSVMThreshold(pW.getSMOWekaOutput(sample,sp,uselessSample));

		double min_weight = 0.0;

		int min_support_after_update = Integer.valueOf(Params.getMinSupportAfterUpdate(sample.getDatabase().getName()));

		double min_precision = Double.valueOf(Params.getPrecision(sample.getDatabase().getName()));

		int max_query_size = Integer.valueOf(Params.getMaxQuerySize(sample.getDatabase().getName()));

		new Optimistic(sample, max_query_size, min_supp, min_support_after_update,threshold,min_weight,min_precision,supp,dataTrue,uselessSample).executeAlgorithm(data,pW,sp);



	}

	private static void runIncremental(Sample sample, SampleBuilderParameters sp, int uselessSample) throws Exception {

		Instances instances = myArffHandler.loadInstances(pW.getArffBooleanModel(sample,sp,uselessSample));

		int max_query_size = Integer.valueOf(Params.getMaxQuerySize(sample.getDatabase().getName()));

		int min_support = Integer.valueOf(Params.getMinSupport(sample.getDatabase().getName()));

		double threshold = Double.valueOf(Params.getPerformanceThreshold(sample.getDatabase().getName()));

		int min_supp_after_update = Integer.valueOf( Params.getMinSupportAfterUpdate(sample.getDatabase().getName()));

		double wfp = Double.valueOf(Params.getFPIncremental(sample.getDatabase().getName()));

		double betaE = Double.valueOf(Params.getBetaEfficiency(sample.getDatabase().getName()));

		new Incremental(sample,  max_query_size,min_support,min_supp_after_update,threshold,wfp,betaE).executeAlgorithm(instances,pW,sp);

	}

	private static void runRipper(Sample sample, SampleBuilderParameters sp, int uselessSample) throws Exception {

		String arffFile = pW.getArffBooleanModel(sample,sp,uselessSample);

		Instances data = myArffHandler.loadInstances(arffFile);

		int fold = 3;
		double minNo = 2.0;
		int optimizationRuns = 2;
		long seedValue = 1;
		boolean pruning = true;
		boolean checkErrorRate = true;

		data = myArffHandler.generateInstanceWithMissingValues(data, data.classIndex());

		new Ripper(sample, -1, -1, -1, fold, minNo, optimizationRuns, seedValue, pruning, checkErrorRate).executeAlgorithm(data,pW,sp);


	}

	private static void runQProber(Sample sample, SampleBuilderParameters sp, int uselessSample) throws Exception {

		Instances data = myArffHandler.loadInstances(pW.getArffBooleanModel(sample,sp,uselessSample));

		double minSVMSupport = Params.getSVMThreshold(pW.getSMOWekaOutput(sample,sp,uselessSample));

		double epsilon = 0.0; //min Support

		int min_supp = Integer.valueOf(Params.getMinSupport(sample.getDatabase().getName()));

		double min_precision = Double.valueOf(Params.getPrecision(sample.getDatabase().getName()));

		dataTrue = myArffHandler.loadInstances(pW.getArffBooleanTrueModel(sample,sp,uselessSample));

		int min_supp_after_update = Integer.valueOf(Params.getMinSupportAfterUpdate(sample.getDatabase().getName()));

		int max_query_size = Integer.valueOf(Params.getMaxQuerySize(sample.getDatabase().getName()));

		new QProber(sample, max_query_size, min_supp, min_supp_after_update,minSVMSupport,epsilon,min_precision,dataTrue,uselessSample).executeAlgorithm(data,pW,sp);


	}

	private static void runMSC(Sample sample, SampleBuilderParameters sp, int uselessSample) throws Exception {

		String model = pW.getArffBooleanModel(sample,sp,uselessSample); //Model

		double min_precision = 0.5;

		int minSupport = Integer.valueOf(Params.getMinSupport(sample.getDatabase().getName()));

		double minimum_support_SVM = Double.valueOf(Params.getSVMThreshold(pW.getSMOWekaOutput(sample,sp,uselessSample)));

		int min_after_update = Integer.valueOf(Params.getMinSupportAfterUpdate(sample.getDatabase().getName()));

		int maxNumberOfTerms = Integer.valueOf(Params.getMaxQuerySize(sample.getDatabase().getName()));

		int K = Integer.valueOf(Params.getMaxNumberOfQueries(sample.getDatabase().getName()));

		double pow = Double.valueOf(Params.getPowerForBenefit(sample.getDatabase().getName()));

		Instances instances = loadElements(model);

		new MSC(sample, maxNumberOfTerms, minSupport, min_after_update,min_precision,minimum_support_SVM,K,pow,uselessSample).executeAlgorithm(instances,pW,sp);


	}

	private static void runQXtract(Sample sample, SampleBuilderParameters sp, int uselessSample) throws Exception {

		String arffFile = pW.getArffBooleanModel(sample,sp,uselessSample);

		Instances data = myArffHandler.loadInstances(arffFile);

		int fold = 3;
		double minNo = 2.0;
		int optimizationRuns = 2;
		long seedValue = 1;
		boolean pruning = true;
		boolean checkErrorRate = true;

		data = myArffHandler.generateInstanceWithMissingValues(data, data.classIndex());

		new QXtract(sample, -1, -1, -1, fold, minNo, optimizationRuns, seedValue, pruning, checkErrorRate).executeAlgorithm(data,pW,sp);


	}

	private static Instances loadElements(String arffFile) {
		return myArffHandler.loadInstances(arffFile);
	}

}
