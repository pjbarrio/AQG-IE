package techniques.evaluation.results;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import utils.arff.myArffHandler;
import utils.query.QueryParser;
import utils.results.ResultHandler;
import weka.core.Instances;


public class ResultsDocsInSample {

	private static ArrayList<Long> tp;
	private static ArrayList<Long> fp;
	private static ArrayList<Integer> must_words;
	private static ArrayList<Integer> must_not_words;
	private static String line;
	private static HashSet<Long> covered;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		must_not_words = new ArrayList<Integer>();
		must_words = new ArrayList<Integer>();

		
		args = new String[2];
				
		args[0] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/arff/randomSample1stVersionConstrainedReady.arff";
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/queries/ready/listOfFiles1stConstrained";
			
		String queriesFolder = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/queries/ready/";
		String outputPfx = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/output/outputSample";
		
		Instances data = myArffHandler.loadInstances(args[0]);
		
		BufferedReader bq = new BufferedReader(new FileReader(new File(args[1])));
		
		String file = bq.readLine();
		
		while (file!=null){
		
			BufferedReader br = new BufferedReader(new FileReader(new File(queriesFolder + file)));
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPfx + file + ".txt")));
						
			line = br.readLine();
			
			covered = new HashSet<Long>();
			
			
			System.out.println(file);
			
			while (line!=null){
				
//				System.out.println("LOADING: " + line);
				
				parseQuery(line,data);
				
				processQuery(bw,data);
				
				line = br.readLine();
				
			}
			
			System.out.println(covered.size());
			
			br.close();
					
			bw.close();
		
			file = bq.readLine();
			
		}
		
		bq.close();
			
	}

	private static void processQuery(BufferedWriter bw, Instances data) throws IOException {
		
		ArrayList<Long> tpMust = myArffHandler.getInstancesContainingFeatures(data,must_words, myArffHandler.YES_VALUE, myArffHandler.YES_VALUE);
		
		if (!must_not_words.isEmpty()){		
		
			ArrayList<Long> tpMust_Not = myArffHandler.getInstancesContainingFeatures(data, must_words, myArffHandler.YES_VALUE, myArffHandler.NO_VALUE);
		
			tp = intersection(tpMust,tpMust_Not);
			
		}
		else
			tp = tpMust;
		
		ArrayList<Long> fpMust = myArffHandler.getInstancesContainingFeatures(data, must_words, myArffHandler.NO_VALUE, myArffHandler.YES_VALUE);
		
		if (!must_not_words.isEmpty()){
	
			ArrayList<Long> fpMust_Not = myArffHandler.getInstancesContainingFeatures(data, must_words, myArffHandler.NO_VALUE, myArffHandler.NO_VALUE);

			fp = intersection(fpMust,fpMust_Not);
			
		}
		else
			fp = fpMust;
	
		
		covered.addAll(tp);
		
		String tpString = ResultHandler.generateSequence(tp);
		String fpString = ResultHandler.generateSequence(fp);
		
		bw.write(ResultHandler.generateTriple(0,line, tpString, fpString,"NOTHING"));
		
//		System.out.println(line);
//		System.out.println(tpString);
//		System.out.println(fpString);
		
		
	}

	private static ArrayList<Long> intersection(ArrayList<Long> tpMust,
			ArrayList<Long> tpMustNot) {
		
		ArrayList<Long> ret = new ArrayList<Long>();

		for (Long longValue : tpMustNot) {
			
			if (tpMust.contains(longValue))
				ret.add(longValue.longValue());
			
		}

		return ret;
	}

	private static void parseQuery(String keywords, Instances data) {
		
		must_words.clear();
		must_not_words.clear();
		
		ArrayList<String> aux_must_words = new ArrayList<String>();
		
		ArrayList<String> aux_must_not_words = new ArrayList<String>();
		
		QueryParser.parseQuery(keywords, aux_must_words, aux_must_not_words);
		
		for (String string : aux_must_words) {
			
			must_words.add(data.attribute(string).index());
			
		}
		for (String string : aux_must_not_words) {
			
			must_not_words.add(data.attribute(string).index());
			
		}
		
	}
	
}
