package techniques.evaluation.postprocessing.combining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import sample.generation.model.impl.DummySampleConfiguration;
import searcher.interaction.formHandler.TextQuery;
import utils.FileHandlerUtils;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import execution.workload.impl.condition.WorkLoadCondition;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.Combination;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.WorkloadModel;

public class QueryWorkloadScore {

	private static Hashtable<Long, ArrayList<Tuple>> idtuplesTable;
	private static Instances data;
	private static ArrayList<String> idsSample;
	private static WorkLoadCondition condition;
	private static boolean startTaste;
	private static persistentWriter pW;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[5];
		
		args[0] = "10"; //Tuples in workload
		
		args[3] = "3"; //minimum value to be sure that it's a real value.
		
		int threshold = Integer.valueOf(args[3]);

		pW = PersistenceImplementation.getWriter();

		int workload = 1;
		
		WorkloadModel dummyWorkload = new WorkloadModel(workload,"/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/Workload.ds","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/Workload.wl","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/WorkloadQueries","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/WorkloadRelations");
		
		condition = new WorkLoadCondition(dummyWorkload.getTuples(), dummyWorkload.getDescription());
		
		int size = Integer.valueOf(args[0]) + 2; //+1 for True and False Positives in position 0 and 1
				
		List<Database> crossDatabases = pW.getCrossableDatabases();

		int sample_number = 1;
		
		for (Database crossDatabase : crossDatabases) {
			
			pW.initializeSimpleExplorator();
			
			while (pW.hasMoreCombinations()){
				
				Combination config = pW.nextCombination();
				
				Sample crossSample = Sample.getSample(crossDatabase,config.getVersion(),config.getWorkload(),sample_number,new DummySampleConfiguration(1));
				
				args[1] = pW.getArffBooleanModel(crossSample);
				
				data = myArffHandler.loadInstances(args[1]);
					
				args[2] = pW.getSampleFilteredFile(crossSample); //Ids of sampling
				
//XXX class not used anymore				args[4] = pW.getMatchingTuplesWithSourcesFile(crossDatabase.getName(),config.getVersion().getName(),config.getWorkload());
				
				idsSample = FileHandlerUtils.getAllResourceNames(new File(args[2]));
				
				loadIdTuple(args[4]);
				
//				pW.setReadCombination(config);
				
				String output = pW.getScoresFile(config, crossSample);
				
				String outputTaste = pW.getTasteFile(config, crossSample);
				
				CalculateScores(size,output,threshold,outputTaste);
						
			}
			
		}
					
	}

	private static void CalculateScores(int tuplesInWorkload,String output, int threshold, String outputTaste) throws IOException {
		
		int[] score = new int[tuplesInWorkload];
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(outputTaste)));
		
		int q = 0;
		
		startTaste = false;
		
		while (pW.hasMoreQueries()){
			
			TextQuery query = pW.getNextQuery();
			
			score = calculateScore(query,tuplesInWorkload);
			
			writeMyOutput(bw,query,score);
			
			if (startTaste)
				writeTasteOutput(bw2,q,score,threshold);
			
			startTaste = true;
			
			q++;
			
		}
		
		pW.finishProcessingQueries();
		
		bw.close();
		
		bw2.close();
	}

	private static void writeTasteOutput(BufferedWriter bw2, int q, int[] score, int threshold) throws IOException {
		
		for (int i = 2; i < score.length; i++) {
			
			if (score[i]>threshold)
			
				bw2.write(q + "," + Integer.toString(i-1) + "," + score[i] + "\n");
			
		}
		
	}

	private static void writeMyOutput(BufferedWriter bw, String query, int[] score) throws IOException {
		
		String print = "[";
		
		for (int i = 0; i < score.length; i++) {
			
			print = print + score[i] + "|";
			
		}
		
		print = print.substring(0, print.length()-1);
		
		print = print + "]," + query;
		
		bw.write(print + "\n");
		
	}

	private static int[] calculateScore(TextQuery query, int size) {
		
		int[] scoreRet = new int[size];
		
		for (int i = 0; i < scoreRet.length; i++) {
			scoreRet[i] = 0;
		}
		
		boolean[] b;
		
		ArrayList<Integer> match = myArffHandler.getTPInstances(data, myArffHandler.getAttributes(data,query));
		
		scoreRet[0] = match.size();
		
		scoreRet[1] = myArffHandler.getFP(data, myArffHandler.getAttributes(data,query));
		
		ArrayList<Long> idsMatch = new ArrayList<Long>();
		
		for (Integer integer : match) {
			
			idsMatch.add(Long.valueOf(idsSample.get(integer)));
			
		}

		for (Long long1 : idsMatch) {
			
			ArrayList<Tuple> tuples = idtuplesTable.get(long1);
			
			if (tuples==null)
				continue;
			
			for (Tuple tuple : tuples) {
				
				b = condition.getMatchingArray(tuple);

				for (int i = 0; i < b.length; i++) {
					
					if (b[i]){
						
						scoreRet[i+2]++;
						
					}
					
				}
				
			}
			
			
		}
		
		return scoreRet;
	
	}

	private static void loadIdTuple(String tuplesandSources) throws IOException {
		
		idtuplesTable = new Hashtable<Long, ArrayList<Tuple>>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(tuplesandSources)));
		
		String line = br.readLine();
		
		ArrayList<Tuple> tuples;
		
		while (line!=null){
			
			Long id = Long.valueOf(line.substring(0, line.indexOf(',')));
			
			String tuple = line.substring(line.indexOf(',')+1);
			
			tuples = idtuplesTable.get(id);
			
			if (tuples == null){
				
				tuples = new ArrayList<Tuple>();
				
			}
			
			tuples.add(TupleReader.generateTuple(tuple));
			
			idtuplesTable.remove(id);
			
			idtuplesTable.put(id, tuples);
			
			line = br.readLine();
			
		}
		
		br.close();
		
	}

}
