package techniques.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.gdata.util.common.base.Pair;

import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import techniques.algorithms.factory.RecommenderFactory;
import techniques.algorithms.factory.UserNeighborhoodFactory;
import techniques.algorithms.factory.UserSimilarityFactory;
import utils.FileHandlerUtils;
import utils.arff.myArffHandler;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.id.TuplesLoader;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import execution.workload.impl.condition.WorkLoadCondition;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.Combination;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Query;
import exploration.model.Sample;
import exploration.model.enumerations.RecommenderEnum;
import exploration.model.enumerations.UserNeighborhoodEnum;
import exploration.model.enumerations.UserSimilarityEnum;

public class CombinedAlgorithm implements ExecutableAlgorithm {

	private boolean startTaste;
	private TextQuery[] wlQueries;
	private Combination comb;
	private ArrayList<String> idsSample;
	private int tuplesInWorkload;
	private int threshold;
	private Hashtable<Document, ArrayList<Tuple>> idtuplesTable;
	private Instances data;
	private WorkLoadCondition condition;
	private double weightCoverage;
	private double weightSpecificity;
	private boolean preserveOrder;
	private int neighbors;
	private UserSimilarityEnum userSimilarityName;
	private UserNeighborhoodEnum neighborhoodName;
	private RecommenderEnum recommenderName;
	private float[][] d;
	private float aux;
	private Hashtable<String, Float> table;
	private Sample crossableSample;
	private TextQuery[] queries;
	private ArrayList<Float> TotalVals;
	private ArrayList<String> output;
	private String actualQuery;


	public CombinedAlgorithm (Combination comb, Sample crossableSample, int tuplesInWorkload, int threshold, 
			double weightCoverage, double weightSpecificity, boolean preserveOrder, int neighbors, 
			UserSimilarityEnum userSimilarityName, UserNeighborhoodEnum neighborhoodName, RecommenderEnum recommenderName, WorkLoadCondition condition){
		this.comb = comb;
		this.crossableSample = crossableSample;
		this.tuplesInWorkload = tuplesInWorkload;
		this.threshold = threshold;
		this.weightCoverage = weightCoverage;
		this.weightSpecificity = weightSpecificity;
		this.preserveOrder = preserveOrder;
		this.neighbors = neighbors;
		this.userSimilarityName = userSimilarityName;
		this.neighborhoodName = neighborhoodName;
		this.recommenderName = recommenderName;
		this.condition = condition;
	}
	
	@Override
	public void executeAlgorithm(Instances sample, persistentWriter pW, SampleBuilderParameters sp)
			throws Exception {
		
		Clock.startTime(ClockEnum.COMBINED);
		
		this.data = sample;
		
		String idTupleFile = pW.getSampleFile(crossableSample); //Ids of sampling
		
		String matchingTuplesFile = pW.getMatchingTuplesWithSourcesFile(crossableSample);
		
		idsSample = FileHandlerUtils.getAllResourceNames(new File(idTupleFile));
		
		idtuplesTable = TuplesLoader.loadDocumenttuplesTuple(matchingTuplesFile);
		
//		pW.setReadCombination(comb);
		
		String output = pW.getScoresFile(comb, crossableSample);
		
		String outputTaste = pW.getTasteFile(comb, crossableSample);
		
		int size = tuplesInWorkload + 2;
		
		CalculateScores(size,output,threshold,outputTaste,comb);
		
		loadWLQueries(comb.getWorkload().getQueriesFile());
		
		int lastItemId = wlQueries.length;
		
//		pW.setReadCombination(comb);
		
		long lasttime = comb.getTime();
		
		int w_combParameter = getParameterId(pW);
		
		int w_combinationId = pW.setCombinedAlgorithm(comb,crossableSample,pW.getConfiguration(weightCoverage,weightSpecificity),w_combParameter);
		
//		int w_combinationId = combis[0]; 
		
		DataModel dm;
		
		try {

			 dm = new FileDataModel(new File(outputTaste));
			
		} catch (Exception e) {

			System.err.println("EmptyFile: " + outputTaste);
			
			pW.endAlgorithm();
			
			return;
		
		}
		
		UserSimilarity userSimilarity = UserSimilarityFactory.getInstance(userSimilarityName,dm);
		
		UserNeighborhood neighborhood = UserNeighborhoodFactory.getInstance(neighborhoodName,Math.min(dm.getNumUsers(),neighbors), userSimilarity, dm);
		
		Recommender recommender = RecommenderFactory.getRecommender(recommenderName,dm, neighborhood, userSimilarity);
		
//		Recommender cachingRecommender = new CachingRecommender(recommender);
		
		readTotal(pW.getScoresFile(comb,crossableSample));
		
		int lastUserId = queries.length;		
		
		d = new float[lastUserId+1][lastItemId];
		
		int tindex = 0;
		
		for (Float totalVal : TotalVals) {
			
			d[0][tindex] = totalVal.floatValue();
			
			tindex++;
		
		}
		
		for (LongPrimitiveIterator users = dm.getUserIDs(); users.hasNext();) {
			int i = (int)((Long) users.next()).longValue();
		
			for (LongPrimitiveIterator items = dm.getItemIDs(); items.hasNext();) {
				int j = (int)((Long) items.next()).longValue();
		
				try {
					
					System.out.println(i + " - " + j);
					
//					aux = cachingRecommender.estimatePreference(i, j);
					
					aux = recommender.estimatePreference(i, j);
					
					System.out.println(i + " - " + j + " - " + aux);
					
					if (Float.isNaN(aux))
						d[i][j-1] = 0;
					else {
						d[i][j-1] = aux;
					}
				} catch (TasteException e) {
					
					d[i][j-1] = 0;
					
				} 
			}
		
		}
		
		table = new Hashtable<String, Float>();
		
		for (int i = 1; i < lastUserId; i++) {
			for (int j = 0; j < lastItemId; j++) {
				
				double c = getC(i, j);
				
				double s = getS(i, j);
				
				double f = getFinalScore(c,s,weightCoverage,weightSpecificity);
				
				if (!Double.isNaN(f)){
				
					System.out.println("Query: " + i + " Tuple: " + (j+1) + " Coverage: " + c + " Specificity: " + s + " Score: " + f);
					
					table.put(new String (i + "-" + (j+1)), new Float(f));
				}
				
			}
		}
		
		sortTable();

		Clock.stopTime(ClockEnum.COMBINED);
		
		lasttime = lasttime + Clock.getMeasuredTime(ClockEnum.COMBINED);
		
		writeOutput(w_combinationId, preserveOrder,lasttime,pW);
		
		pW.updateCurrentAlgorithmTime(w_combinationId,lasttime);
		
	}

	private int getParameterId(persistentWriter pW) {
		
		return pW.getCombinedCollaborativeFilteringParameters(preserveOrder,neighbors,userSimilarityName.toString(),neighborhoodName.toString(),recommenderName.toString());
		
	}

	private void writeOutput(int wCombination_Id, boolean preserveOrder, long lasttime, persistentWriter pW) throws IOException {
		
		TextQuery query;
		
		List<Pair<TextQuery,Long>> queryTime = new ArrayList<Pair<TextQuery,Long>>(output.size());
		
		for (String comb : output) {
			
			String[] spl = comb.split("-");
			
			int q = Integer.valueOf(spl[0]);
			
			int wl = Integer.valueOf(spl[1])-1;
			
			if (!preserveOrder){
			
				List<String> qq = new ArrayList<String>(queries[q].getWords());
				
				qq.addAll(wlQueries[wl].getWords());
				
//				query = queries[q].trim() + " " + wlQueries[wl].trim();
	
				query = new TextQuery(qq);
				
				System.out.println(query + " Score: " + table.get(comb));
				
//				pW.writeQuery(wCombination_Id,query, lasttime);
				
				queryTime.add(new Pair<TextQuery,Long>(query,lasttime));
				
			} else {
				
				ArrayList<Integer> prevQueries = getPrevQueries(q,wl+1);
				
				for (Integer queryNumber : prevQueries) {
					
//					query = queries[queryNumber].trim() + " " + wlQueries[wl].trim();
					
					List<String> qq = new ArrayList<String>(queries[queryNumber].getWords());
					
					qq.addAll(wlQueries[wl].getWords());
					
					query = new TextQuery(qq);
					
					String combination = new String(queryNumber + "-" + (wl+1));
					
					System.out.println(query + " Score: " + table.get(combination));
					
					table.remove(combination);
					
//					pW.writeQuery(wCombination_Id,query, lasttime);
					
					queryTime.add(new Pair<TextQuery,Long>(query,lasttime));
					
				}
				
			}
			
		}
		
		pW.writeQueries(wCombination_Id,queryTime);
		
	}

	private ArrayList<Integer> getPrevQueries(int q, int wl) {
	
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i <= q; i++) {
			
			if (table.containsKey(new String (i + "-" + wl))){
				ret.add(i);
			}
		}
		
		return ret;
	
	}

	
	private void sortTable() {
		
		output = new ArrayList<String>();
		
		for (Enumeration<String> e = table.keys();e.hasMoreElements();){
			output.add(e.nextElement());
		}
		
		Collections.sort(output,new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return table.get(o2).compareTo(table.get(o1));
			}
		});
		
		
	}

	
	private double getFinalScore(double c, double s, double wc, double ws) {
		return wc*c + ws*s;
	}
	
	private void CalculateScores(int tuplesInWorkload,String output, int threshold, String outputTaste, Combination comb) throws IOException {
		
		int[] score = new int[tuplesInWorkload];
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(outputTaste)));
		
		int q = 0;
		
		startTaste = false;
		
		List<Query> queries = new ArrayList<Query>();
		
		queries.add(new Query(comb, TextQuery.emptyQuery, 0, 0));
		
		queries.addAll(comb.getQueries());
		
		for (Query que : queries) {
			
			TextQuery query = que.getText();
			
			score = calculateScore(query,tuplesInWorkload);
			
			writeMyOutput(bw,query,score);
			
			if (startTaste)
				writeTasteOutput(bw2,q,score,threshold);
			
			startTaste = true;
			
			q++;
			
		}
		
		
		bw.close();
		
		bw2.close();
	}

	private void writeTasteOutput(BufferedWriter bw2, int q, int[] score, int threshold) throws IOException {
		
		for (int i = 2; i < score.length; i++) {
			
			if (score[i]>threshold)
			
				bw2.write(q + "," + Integer.toString(i-1) + "," + score[i] + "\n");
			
		}
		
	}

	private void writeMyOutput(BufferedWriter bw, TextQuery query, int[] score) throws IOException {
		
		String print = "[";
		
		for (int i = 0; i < score.length; i++) {
			
			print = print + score[i] + "|";
			
		}
		
		print = print.substring(0, print.length()-1);
		
		print = print + "]," + query.getText();
		
		bw.write(print + "\n");
		
	}

	private int[] calculateScore(TextQuery query, int size) {
		
		int[] scoreRet = new int[size];
		
		for (int i = 0; i < scoreRet.length; i++) {
			scoreRet[i] = 0;
		}
		
		boolean[] b;
		
		ArrayList<Integer> match = myArffHandler.getTPInstances(data, myArffHandler.getAttributes(data,query));
		
		scoreRet[0] = match.size();
		
		scoreRet[1] = myArffHandler.getFP(data, myArffHandler.getAttributes(data,query));
		
		ArrayList<String> idsMatch = new ArrayList<String>();
		
		for (Integer integer : match) {
			
			idsMatch.add(idsSample.get(integer));
			
		}

		for (String docid : idsMatch) {
			
			ArrayList<Tuple> tuples = idtuplesTable.get(docid);
			
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

	

	private void loadWLQueries(String string) throws IOException {
		
//		BufferedReader br = new BufferedReader(new FileReader(new File(string)));
//		
//		String line = br.readLine();
//		
//		ArrayList<String> t = new ArrayList<String>(); 
//		
//		while (line!=null){
//			
//			t.add(line);
//			
//			line = br.readLine();
//		}
//		
//		br.close();
		
		List<String> t = FileUtils.readLines(new File(string));
		
		wlQueries = new TextQuery[t.size()];
		
		for (int i = 0; i < t.size(); i++) {
			wlQueries[i] = new TextQuery(t.get(i));
		}
		
//		wlQueries = t.toArray(new String[0]);
		
	}

	private void readTotal(String file) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String line = br.readLine();
		
		String[] split = getValues(line);
		
		TotalVals = new ArrayList<Float>();
		
		for (int i = 2; i < split.length; i++) {
		
			TotalVals.add(Float.valueOf(split[i]));
			
		}
		
		ArrayList<String> quer = new ArrayList<String>();
		
		quer.add("");
		
		line = br.readLine();
		
		while (line!=null){
			
			split = getValues(line);
			
			quer.add(actualQuery);
			
			line = br.readLine();
			
		}
		
		br.close();
		
		//XXX do not get what's going on
		
		queries = quer.toArray(new String[0]);
		
	}

	private String[] getValues(String line) {
		
		String[] spl = line.split(",");
			
		line = spl[0];
		
		actualQuery = "";
		
		for (int i = 1; i < spl.length; i++) {
			
			actualQuery += spl[i];
			
			if (i < (spl.length-1))
				actualQuery += ",";
			
		}
		
		line = line.replace('[', ',').replace(']', ',').replace(",", "");
		
		String[] split = line.split("\\|");
		
		return split;
	}
	
	private float getC(int i, int j) {
		
		return d[i][j]/d[0][j];
		
	}
	
	private float getS(int i, int j) {
		float sum = 0;
		
		for (int k = 0; k < d[i].length; k++) {
			
			if (!Float.isNaN(d[i][k]))
				sum+=d[i][k];
		}
		return d[i][j] / sum;
	}
}
