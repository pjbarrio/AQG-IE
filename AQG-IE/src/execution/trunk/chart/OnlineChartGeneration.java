package execution.trunk.chart;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import exploration.model.enumerations.AlgorithmEnum;

import plot.data.ChartData;
import plot.generator.ChartGenerator;
import plot.generator.OverallSeriesGenerator;
import plot.generator.SeriesGenerator;
import plot.generator.impl.OverallDocumentsRecallByDatabaseLimit;
import plot.generator.impl.OverallPrecisionByDatabaseLimit;
import plot.generator.impl.OverallTuplesRecallByDatabaseLimit;
import plot.generator.impl.QueryPrecisionByProcessedDocuments;
import plot.generator.impl.QueryPrecisionByProcessingTime;
import plot.generator.impl.QueryPrecisionBySentQueries;
import plot.generator.impl.TuplesByProcessedDocuments;
import plot.generator.impl.TuplesByProcessingTime;
import plot.generator.impl.TuplesBySentQueries;
import plot.generator.impl.UsefulDocumentsByProcessedDocuments;
import plot.generator.impl.UsefulDocumentsByProcessingTime;
import plot.generator.impl.UsefulDocumentsBySentQueries;
import plot.selector.Selector;
import plot.selector.impl.AlgorithmSelector;
import plot.selector.impl.ExecutionAlternativeCombinationSelector;
import plot.selector.impl.ExecutionAlternativeFilteredAlgorithmSelector;
import plot.selector.impl.FixedSelection;
import plot.selector.impl.IntersectionSelector;
import plot.selector.impl.OverallByParameterSelector;
import plot.selector.impl.UnionSelector;
import utils.CommandLineExecutor;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class OnlineChartGeneration {

	
	private static final int L100 = 1;
	private static final int L250 = 2;
	private static final int L500 = 3;
	private static final int L1000 = 4;
	private static final int L50 = 12;
	private static final int L150 = 13;
	private static final int L200 = 14;
	private static final int L300 = 15;
	private static final int L350 = 16;
	private static final int L400 = 17;
	private static final int L450 = 18;
	

	private static final int DEFAULT_EXPERIMENT_LIMIT = L1000;
	private static final AlgorithmEnum TUPLE = AlgorithmEnum.TUPLE_1000;
	
	private static final int WORKLOAD_NUMBER = 1;
	
	private static final int LOCAL_SIMPLE = 1;
	private static final int CLUSTER_SIMPLE = 2;
	private static final int SIMILAR_SIMPLE = 3;
	private static final int GLOBAL_SIMPLE = 4;
	private static final int LOCAL_COMBINED_LOCAL = 5;
	private static final int SIMILAR_COMBINED_SIMILAR = 6;
	private static final int LOCAL_COMBINED_SIMILAR = 7;
	private static final int SIMILAR_COMBINED_LOCAL = 8;
	private static final int LOCAL_COMBINED_CLUSTER = 9;
	private static final int SIMILAR_COMBINED_CLUSTER = 10;
	private static final int CLUSTER_COMBINED_LOCAL = 11;
	private static final int CLUSTER_COMBINED_SIMILAR = 12;
	private static final int LOCAL_COMBINED_GLOBAL = 13;
	private static final int SIMILAR_COMBINED_GLOBAL= 14;
	private static final int GLOBAL_COMBINED_LOCAL = 15;
	private static final int GLOBAL_COMBINED_SIMILAR = 16;
	private static final int CLUSTER_COMBINED_CLUSTER = 17;
	private static final int CLUSTER_COMBINED_GLOBAL = 18;
	private static final int GLOBAL_COMBINED_CLUSTER = 19;
	private static final int GLOBAL_COMBINED_GLOBAL = 20;
	private static final int TOTAL_OFFLINE_ALTERNATIVES = 810;
	
	private static persistentWriter pW;

	private static String[] names = {"None","S_self","S_cluster","S_similar","S_global","C_self","C_similar","C_crossed","C_mutated","C_friendly",
		"C_hostile","C_selected","C_missed","C_universal","C_parallel","C_personal","C_impersonal","C_cluster","C_included","C_community","C_global"};
	private static List<Selector> exSel;
	private static List<Selector> overallSel;
	private static String Prefix;
	private static ArrayList<Double> limitsSimple;
	private static ArrayList<Double> limitsCombined;
	private static ArrayList<Integer> limitsSimpleValue;
	private static ArrayList<Integer> limitsCombinedValue;
	private static Hashtable<Double,AlgorithmEnum> tuplesAlgorithm;
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		loadTuplesAlgorithm();
		
		limitsSimple = new ArrayList<Double>();
		limitsSimple.add(50.0);
		limitsSimple.add(100.0);
		limitsSimple.add(150.0);
		limitsSimple.add(200.0);
		limitsSimple.add(250.0);
		limitsSimple.add(300.0);
		limitsSimple.add(350.0);
		limitsSimple.add(400.0);
		limitsSimple.add(450.0);
		limitsSimple.add(500.0);
		limitsSimple.add(1000.0);

		
		limitsSimpleValue = new ArrayList<Integer>();
		limitsSimpleValue.add(L50);
		limitsSimpleValue.add(L100);
		limitsSimpleValue.add(L150);
		limitsSimpleValue.add(L200);
		limitsSimpleValue.add(L250);
		limitsSimpleValue.add(L300);
		limitsSimpleValue.add(L350);
		limitsSimpleValue.add(L400);
		limitsSimpleValue.add(L450);
		limitsSimpleValue.add(L500);
		limitsSimpleValue.add(L1000);

		
		
		limitsCombined = new ArrayList<Double>();
		limitsCombined.add(50.0);
		limitsCombined.add(100.0);
		limitsCombined.add(150.0);
		limitsCombined.add(200.0);
		limitsCombined.add(250.0);
		limitsCombined.add(300.0);
		limitsCombined.add(350.0);
		limitsCombined.add(400.0);
		limitsCombined.add(450.0);
		limitsCombined.add(500.0);
		limitsCombined.add(1000.0);
		
		limitsCombinedValue = new ArrayList<Integer>();
		limitsCombinedValue.add(L50);
		limitsCombinedValue.add(L100);
		limitsCombinedValue.add(L150);
		limitsCombinedValue.add(L200);
		limitsCombinedValue.add(L250);
		limitsCombinedValue.add(L300);
		limitsCombinedValue.add(L350);
		limitsCombinedValue.add(L400);
		limitsCombinedValue.add(L450);
		limitsCombinedValue.add(L500);
		limitsCombinedValue.add(L1000);

//		String folder = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/charts/";
	
//		String folder = "/home/pablo/charts/";
		
		String folder = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Evaluation/Online/Charts/";
		
		pW = PersistenceImplementation.getWriter();
		
		ArrayList<AlgorithmEnum> algs = new ArrayList<AlgorithmEnum>();
		
		algs.add(AlgorithmEnum.QPROBER);
		algs.add(AlgorithmEnum.RIPPER);
		algs.add(AlgorithmEnum.INCREMENTAL);
		algs.add(AlgorithmEnum.OPTIMISTIC);
		algs.add(AlgorithmEnum.MSC);
//		algs.add(TUPLE);
		
		ArrayList<AlgorithmEnum> algsGlobal = new ArrayList<AlgorithmEnum>();
		
		algsGlobal.add(AlgorithmEnum.QPROBER);
		algsGlobal.add(AlgorithmEnum.RIPPER);
		algsGlobal.add(AlgorithmEnum.INCREMENTAL);
		algsGlobal.add(AlgorithmEnum.OPTIMISTIC);
		algsGlobal.add(AlgorithmEnum.MSC);
		
//		createAlgorithmsChart(algs,algsGlobal,folder);
				
		for (AlgorithmEnum algorithm : algs) {

			createLocalStrategies(algorithm);
			
			draw(folder + "Local_Strategies/" + algorithm.name() + "/",limitsSimple);
			
			createSimpleStrategies(algorithm);
			
			draw(folder + "Simple_Strategies/" + algorithm.name() + "/",limitsSimple);
			
			createCombinedStrategies(algorithm);
			
			draw(folder + "Combined_Strategies/" + algorithm.name() + "/",limitsCombined);
			
//			createCombinedConfigurations(algorithm);
//			
//			draw(folder + "Combined_Configurations" + algorithm.name() + "/",limitsCombined);
			
		}
		
	}

	private static void createLocalStrategies(AlgorithmEnum algorithm) {
		
		Prefix = "LOCAL_" + algorithm.toString();
		
//		ExecutionSelector alg = getAlgorithmSelector(algorithm);
		
		exSel = new ArrayList<Selector>();
		
		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_SIMPLE],algorithm, getExecutionAlternatives(LOCAL_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_SIMPLE], alg, getAlternative(LOCAL_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_COMBINED_LOCAL],algorithm, getExecutionAlternatives(LOCAL_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_COMBINED_LOCAL], alg, getAlternative(LOCAL_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));
		
		
	}

	private static void loadTuplesAlgorithm() {
		
		tuplesAlgorithm = new Hashtable<Double, AlgorithmEnum>();
		
		tuplesAlgorithm.put(50.0,AlgorithmEnum.TUPLE_50);
		tuplesAlgorithm.put(100.0,AlgorithmEnum.TUPLE_100);
		tuplesAlgorithm.put(150.0,AlgorithmEnum.TUPLE_150);
		tuplesAlgorithm.put(200.0,AlgorithmEnum.TUPLE_200);
		tuplesAlgorithm.put(250.0,AlgorithmEnum.TUPLE_250);
		tuplesAlgorithm.put(300.0,AlgorithmEnum.TUPLE_300);
		tuplesAlgorithm.put(350.0,AlgorithmEnum.TUPLE_350);
		tuplesAlgorithm.put(400.0,AlgorithmEnum.TUPLE_400);
		tuplesAlgorithm.put(450.0,AlgorithmEnum.TUPLE_450);
		tuplesAlgorithm.put(500.0,AlgorithmEnum.TUPLE_500);
		tuplesAlgorithm.put(1000.0,AlgorithmEnum.TUPLE_1000);

		
	}

	private static void draw(String folder, ArrayList<Double> limits) throws IOException {
		
		new File(folder).mkdirs();
		
		ChartGenerator cg = new ChartGenerator();
				
		SeriesGenerator[] series = {new TuplesByProcessedDocuments()/*,new TuplesByProcessingTime()*/,new TuplesBySentQueries(),new UsefulDocumentsByProcessedDocuments(),new UsefulDocumentsBySentQueries()/*,new UsefulDocumentsByProcessingTime()*//*,new QueryPrecisionByProcessedDocuments(),new QueryPrecisionBySentQueries(),new QueryPrecisionByProcessingTime()*/};
		
		String[] namesPrefix = {Prefix + "TuplesByProcessedDocuments"/*,Prefix + "TuplesByProcessingTime"*/,Prefix + "TuplesBySentQueries",Prefix + "UsefulDocumentsByProcessedDocuments",Prefix + "UsefulDocumentsByIssuedQueries"/*,Prefix + "UsefulDocumentsByProcessingTime",*//*Prefix + "QueryPrecisionByProcessedDocuments",Prefix + "QueryPrecisionByIssuedQueries",Prefix + "QueryPrecisionByProcessingTime"*/};

		for (int i = 0; i < namesPrefix.length; i++){

			ChartData data = cg.generateChart(namesPrefix[i],series[i], exSel, Prefix);
			
			data.plot(folder,series[i].getPercentX(),series[i].getPercentY());
			
			String ret = new CommandLineExecutor().getOutput("R CMD BATCH " + data.getOutputFile() + " " + data.getOutputOutFile());
			
			System.out.println(ret);
		
		}
		
//		OverallSeriesGenerator[] overallSeries = {new OverallTuplesRecallByDatabaseLimit(limits),new OverallPrecisionByDatabaseLimit(limits),new OverallDocumentsRecallByDatabaseLimit(limits)};
//		
//		String[] overallNamesPrefix = {Prefix + "OverallTuplesRecall",Prefix + "OverallPrecision",Prefix + "OverallDocumentsRecall"};
//		
//		for (int i = 0; i < overallNamesPrefix.length; i++) {
//			
//			ChartData data = cg.generateChart(overallNamesPrefix[i], overallSeries[i], overallSel,Prefix);
//			
//			data.plot(folder);
//			
//			String ret = new CommandLineExecutor().getOutput("R CMD BATCH " + data.getOutputFile() + " " + data.getOutputOutFile());
//			
//			System.out.println(ret);
//			
//		}
		
	}

	private static void createCombinedConfigurations(AlgorithmEnum algorithm) {
		// TODO Auto-generated method stub
		
	}

	private static OverallByParameterSelector createOverallSeries(String name, AlgorithmEnum algorithm, int alternative, ArrayList<Double> limits, ArrayList<Integer> limitsValue){

		OverallByParameterSelector obpsg = new OverallByParameterSelector(name);
		
		if (!algorithm.equals(TUPLE)){
		
			for (int i = 0; i < limits.size(); i++) {
				obpsg.addEntry(limits.get(i),getIntersection(algorithm,alternative,limitsValue.get(i)));
			}

		} else {
			
			for (int i = 0; i < limits.size(); i++) {
				obpsg.addEntry(limits.get(i),getIntersection(getTupleAlgorithm(limits.get(i)),alternative,limitsValue.get(i)));
			}
			
		}
		
		return obpsg;
		
	}
	
	private static AlgorithmEnum getTupleAlgorithm(Double limit) {
		return tuplesAlgorithm.get(limit);
	}

	private static Selector getIntersection(AlgorithmEnum algorithm,
			int alternative, int limit) {
		return new IntersectionSelector(getAlgorithmName(algorithm),getAlgorithmSelector(algorithm), getAlternative(alternative, limit));
	}

	private static void createCombinedStrategies(AlgorithmEnum algorithm) {
		
		Prefix = "COMBINED_" + algorithm.toString();
		
//		ExecutionSelector alg = getAlgorithmSelector(algorithm);
		
		exSel = new ArrayList<Selector>();

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_COMBINED_LOCAL],algorithm, getExecutionAlternatives(LOCAL_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_COMBINED_LOCAL], alg, getAlternative(LOCAL_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[SIMILAR_COMBINED_SIMILAR],algorithm, getExecutionAlternatives(SIMILAR_COMBINED_SIMILAR,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[SIMILAR_COMBINED_SIMILAR], alg, getAlternative(SIMILAR_COMBINED_SIMILAR,DEFAULT_EXPERIMENT_LIMIT)));

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_COMBINED_SIMILAR],algorithm, getExecutionAlternatives(LOCAL_COMBINED_SIMILAR,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_COMBINED_SIMILAR], alg, getAlternative(LOCAL_COMBINED_SIMILAR,DEFAULT_EXPERIMENT_LIMIT)));

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[SIMILAR_COMBINED_LOCAL],algorithm, getExecutionAlternatives(SIMILAR_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[SIMILAR_COMBINED_LOCAL], alg, getAlternative(SIMILAR_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_COMBINED_CLUSTER],algorithm, getExecutionAlternatives(LOCAL_COMBINED_CLUSTER,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_COMBINED_CLUSTER], alg, getAlternative(LOCAL_COMBINED_CLUSTER,DEFAULT_EXPERIMENT_LIMIT)));

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[SIMILAR_COMBINED_CLUSTER],algorithm, getExecutionAlternatives(SIMILAR_COMBINED_CLUSTER,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[SIMILAR_COMBINED_CLUSTER], alg, getAlternative(SIMILAR_COMBINED_CLUSTER,DEFAULT_EXPERIMENT_LIMIT)));
		
		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_COMBINED_GLOBAL],algorithm, getExecutionAlternatives(LOCAL_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_COMBINED_GLOBAL], alg, getAlternative(LOCAL_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[SIMILAR_COMBINED_GLOBAL],algorithm, getExecutionAlternatives(SIMILAR_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[SIMILAR_COMBINED_GLOBAL], alg, getAlternative(SIMILAR_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));

		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_COMBINED_GLOBAL],algorithm, getExecutionAlternatives(LOCAL_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_COMBINED_GLOBAL], alg, getAlternative(LOCAL_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));
		
//		if (!algorithm.equals(TUPLE)){
//			exSel.add(new IntersectionExecutionSelector(names[CLUSTER_COMBINED_LOCAL], alg, getAlternative(CLUSTER_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[CLUSTER_COMBINED_SIMILAR], alg, getAlternative(CLUSTER_COMBINED_SIMILAR,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[GLOBAL_COMBINED_LOCAL], alg, getAlternative(GLOBAL_COMBINED_LOCAL,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[GLOBAL_COMBINED_SIMILAR], alg, getAlternative(GLOBAL_COMBINED_SIMILAR,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[CLUSTER_COMBINED_CLUSTER], alg, getAlternative(CLUSTER_COMBINED_CLUSTER,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[CLUSTER_COMBINED_GLOBAL], alg, getAlternative(CLUSTER_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[GLOBAL_COMBINED_CLUSTER], alg, getAlternative(GLOBAL_COMBINED_CLUSTER,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[GLOBAL_COMBINED_GLOBAL], alg, getAlternative(GLOBAL_COMBINED_GLOBAL,DEFAULT_EXPERIMENT_LIMIT)));
//		}
		
//		overallSel = new ArrayList<ExecutionSelector>();
//		
//		overallSel.add(createOverallSeries(names[LOCAL_COMBINED_LOCAL], algorithm, LOCAL_COMBINED_LOCAL,limitsCombined,limitsCombinedValue));
//		overallSel.add(createOverallSeries(names[SIMILAR_COMBINED_LOCAL], algorithm, SIMILAR_COMBINED_LOCAL,limitsCombined,limitsCombinedValue));
//		overallSel.add(createOverallSeries(names[SIMILAR_COMBINED_SIMILAR], algorithm, SIMILAR_COMBINED_SIMILAR,limitsCombined,limitsCombinedValue));
//		overallSel.add(createOverallSeries(names[LOCAL_COMBINED_GLOBAL], algorithm, LOCAL_COMBINED_GLOBAL,limitsCombined,limitsCombinedValue));
//		if (!algorithm.equals(TUPLE)){
//			overallSel.add(createOverallSeries(names[GLOBAL_COMBINED_GLOBAL], algorithm, GLOBAL_COMBINED_GLOBAL,limitsCombined,limitsCombinedValue));
//			overallSel.add(createOverallSeries(names[GLOBAL_COMBINED_LOCAL], algorithm, GLOBAL_COMBINED_LOCAL,limitsCombined,limitsCombinedValue));
//		}
	
	}

	private static void createSimpleStrategies(AlgorithmEnum algorithm) {
		
		Prefix = "SIMPLE_" + algorithm.toString();
		
//		ExecutionSelector alg = getAlgorithmSelector(algorithm);
		
		exSel = new ArrayList<Selector>();
		
		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[LOCAL_SIMPLE],algorithm, getExecutionAlternatives(LOCAL_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[LOCAL_SIMPLE], alg, getAlternative(LOCAL_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
		exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[SIMILAR_SIMPLE],algorithm, getExecutionAlternatives(SIMILAR_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
//		exSel.add(new IntersectionExecutionSelector(names[SIMILAR_SIMPLE], alg, getAlternative(SIMILAR_SIMPLE, DEFAULT_EXPERIMENT_LIMIT)));
				
		if (!algorithm.equals(TUPLE)){
			exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[CLUSTER_SIMPLE],algorithm, getExecutionAlternatives(CLUSTER_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[CLUSTER_SIMPLE], alg, getAlternative(CLUSTER_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
			exSel.add(getExecutionAlternativeFilteredAlgorithmSelector(names[GLOBAL_SIMPLE],algorithm, getExecutionAlternatives(GLOBAL_SIMPLE,DEFAULT_EXPERIMENT_LIMIT)));
//			exSel.add(new IntersectionExecutionSelector(names[GLOBAL_SIMPLE], alg, getAlternative(GLOBAL_SIMPLE, DEFAULT_EXPERIMENT_LIMIT)));
		}
//		overallSel = new ArrayList<ExecutionSelector>();
//		
//		overallSel.add(createOverallSeries(names[LOCAL_SIMPLE], algorithm, LOCAL_SIMPLE,limitsSimple,limitsSimpleValue));
//		overallSel.add(createOverallSeries(names[SIMILAR_SIMPLE], algorithm, SIMILAR_SIMPLE,limitsSimple,limitsSimpleValue));
//		overallSel.add(createOverallSeries(names[CLUSTER_SIMPLE], algorithm, CLUSTER_SIMPLE,limitsSimple,limitsSimpleValue));
//		
//		if (!algorithm.equals(TUPLE))
//			overallSel.add(createOverallSeries(names[GLOBAL_SIMPLE], algorithm, GLOBAL_SIMPLE,limitsSimple,limitsSimpleValue));
		
	}

	private static List<Integer> getExecutionAlternatives(int alternative,
			int limit) {
		
		List<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < WORKLOAD_NUMBER; i++) {
			
			int altNum = TOTAL_OFFLINE_ALTERNATIVES + alternative /*+ MUST DO SOMETHING WITH LIMIT*/;
			
			ret.add(altNum);
			
		}
		
		return ret;
	}

	private static Selector getAlternative(int alternative, int limit) {
		
		List<Selector> ret = new ArrayList<Selector>(WORKLOAD_NUMBER);
		
		for (int i = 0; i < WORKLOAD_NUMBER; i++) {
			
			int altNum = TOTAL_OFFLINE_ALTERNATIVES + alternative /*+ MUST DO SOMETHING WITH LIMIT FOR THE OVERALL*/;
			
			System.out.println("ALTERNATIVE: " + altNum);
			
			ret.add(new ExecutionAlternativeCombinationSelector(altNum, names[alternative], pW));
			
		}
		
		return new UnionSelector(names[alternative], ret.toArray(new Selector[WORKLOAD_NUMBER]));
	
	}

	private static Selector getAlgorithmSelector(
			AlgorithmEnum algorithm) {
		return new AlgorithmSelector(algorithm, pW);
	}

	private static Selector getExecutionAlternativeFilteredAlgorithmSelector(
			String name, AlgorithmEnum algorithm, List<Integer> executionAlternative) {
		return new ExecutionAlternativeFilteredAlgorithmSelector(name,algorithm,executionAlternative, pW);
	}
	
	private static void createAlgorithmsChart(ArrayList<AlgorithmEnum> algs, ArrayList<AlgorithmEnum> algsGlobal, String folder) throws IOException {
		
		CreateAlgorithmComparison(algs,folder,DEFAULT_EXPERIMENT_LIMIT,LOCAL_SIMPLE,limitsSimple,limitsSimpleValue);
		
		draw(folder + names[LOCAL_SIMPLE] + "/",limitsSimple);
		
		CreateAlgorithmComparison(algs,folder,DEFAULT_EXPERIMENT_LIMIT,LOCAL_COMBINED_LOCAL,limitsCombined,limitsCombinedValue);
		
		draw(folder + names[LOCAL_COMBINED_LOCAL] + "/",limitsCombined);
		
//		CreateAlgorithmComparison(algs,folder,DEFAULT_EXPERIMENT_LIMIT,SIMILAR_SIMPLE,limitsSimple,limitsSimpleValue);
//		
//		draw(folder + names[SIMILAR_SIMPLE] + "/",limitsSimple);
//		
//		CreateAlgorithmComparison(algs,folder,DEFAULT_EXPERIMENT_LIMIT,SIMILAR_COMBINED_LOCAL,limitsCombined,limitsCombinedValue);
//		
//		draw(folder + names[SIMILAR_COMBINED_LOCAL] + "/",limitsCombined);
//		
//		CreateAlgorithmComparison(algs,folder,DEFAULT_EXPERIMENT_LIMIT,SIMILAR_COMBINED_SIMILAR,limitsCombined,limitsCombinedValue);
//		
//		draw(folder + names[SIMILAR_COMBINED_SIMILAR] + "/",limitsCombined);
//		
//		CreateAlgorithmComparison(algsGlobal,folder,DEFAULT_EXPERIMENT_LIMIT,GLOBAL_SIMPLE,limitsSimple,limitsSimpleValue);
//		
//		draw(folder + names[GLOBAL_SIMPLE] + "/",limitsSimple);
//		
//		CreateAlgorithmComparison(algs,folder,DEFAULT_EXPERIMENT_LIMIT,LOCAL_COMBINED_GLOBAL,limitsCombined,limitsCombinedValue);
//		
//		draw(folder + names[LOCAL_COMBINED_GLOBAL] + "/",limitsCombined);
//		
//		CreateAlgorithmComparison(algsGlobal,folder,DEFAULT_EXPERIMENT_LIMIT,GLOBAL_COMBINED_GLOBAL,limitsCombined,limitsCombinedValue);
//		
//		draw(folder + names[GLOBAL_COMBINED_GLOBAL] + "/",limitsCombined);
//		
//		CreateAlgorithmComparison(algsGlobal,folder,DEFAULT_EXPERIMENT_LIMIT,GLOBAL_COMBINED_LOCAL,limitsCombined,limitsCombinedValue);
//		
//		draw(folder + names[GLOBAL_COMBINED_LOCAL] + "/",limitsCombined);
		
	}

	private static void CreateAlgorithmComparison(
			ArrayList<AlgorithmEnum> algs, String folder, int limit,
			int alternative, ArrayList<Double> limits, ArrayList<Integer> limitsValue) {
		
		Prefix = names[alternative];
		
		exSel = new ArrayList<Selector>();
		
		Selector simple = getAlternative(alternative, limit);

		for (AlgorithmEnum algorithm : algs) {
			exSel.add(new IntersectionSelector(getAlgorithmName(algorithm), getAlgorithmSelector(algorithm), simple));
		}
		
		
	}

	private static String getAlgorithmName(AlgorithmEnum algorithm) {
		
		if (algorithm.equals(TUPLE)){
			return "TUPLES";
		}
		return algorithm.name();
	}

}
