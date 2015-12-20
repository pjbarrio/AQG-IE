package techniques.evaluation.postprocessing.combining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.google.gdata.util.common.base.Pair;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Combination;
import exploration.model.Database;
import exploration.model.Sample;

public class CFQueryCombiner {

	private static float[][] d;
	private static String[] queries;
	private static String actualQuery;
	private static String[] wlQueries;
	private static Hashtable<String, Float> table;
	private static ArrayList<String> output;
	private static ArrayList<Float> TotalVals;
	private static persistentWriter pW;
	private static int combinationID;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TasteException 
	 */
	public static void main(String[] args) throws IOException, TasteException {
		
		int sample_number_cross = 1;
		
		double[][] configuration = {{1.0,0.0},{0.0,1.0},{0.5,0.5}};
		
		args = new String[2];
		
		pW = PersistenceImplementation.getWriter();
		
		args[0] = "false"; //preserve order
		
		boolean preserveOrder = Boolean.valueOf(args[0]);
		
		args[1] = "10"; //max number of neighbors
		
		int neighbors = Integer.valueOf(args[1]);
		
		float aux;
		
		List<Database> crossableDatabases = pW.getCrossableDatabases();
		
		pW.initializeSimpleExplorator();
		
		while (pW.hasMoreCombinations()){
			
			Combination config = pW.nextCombination();
			
			loadWLQueries(config.getWorkload().getQueriesFile());
			
			int lastItemId = wlQueries.length;
			
//			pW.setReadCombination(config);
			
			long lasttime = config.getTime();
			
			for (int co = 0; co < configuration.length; co++) {
				
				double wc = configuration[co][0];

				double ws = 1 - wc;
				
				for (Database crossableDatabase : crossableDatabases) {
					
					Sample crossableSample = Sample.getSample(crossableDatabase,config.getVersion(),config.getGeneratorSample().getWorkload(),sample_number_cross,new DummySampleConfiguration(1));
					
					String tasteVersion = pW.getTasteFile(config,crossableSample);
					
					int[] comb = pW.setCombinedAlgorithm(config,crossableSample,co,preserveOrder,neighbors,"TanimotoCoefficientSimilarity","NearestNUserNeighborhood","GenericUserBasedRecommender");
					
					combinationID = comb[0]; 
					 
					DataModel dm;
					
					try {

						 dm = new FileDataModel(new File(tasteVersion));
						
					} catch (Exception e) {

						System.err.println("EmptyFile: " + tasteVersion);
						
						pW.endAlgorithm();
						
						continue;
					
					}
										
					UserSimilarity userSimilarity = new TanimotoCoefficientSimilarity(dm);
					
					UserNeighborhood neighborhood = new NearestNUserNeighborhood(Math.min(dm.getNumUsers(),neighbors), userSimilarity, dm);
					
					Recommender recommender = new GenericUserBasedRecommender(dm, neighborhood, userSimilarity);
					
					Recommender cachingRecommender = new CachingRecommender(recommender);
					
					readTotal(pW.getScoresFile(config,crossableSample));
					
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
								
								aux = cachingRecommender.estimatePreference(i, j);
								
								System.out.println(i + " - " + j + " - " + aux);
								
								if (Float.isNaN(aux))
									d[i][j-1] = 0;
								else {
									d[i][j-1] = aux;
								}
							} catch (NoSuchUserException e) {
								
								d[i][j-1] = 0;
								
							} 
						}
					
					}
					
					table = new Hashtable<String, Float>();
					
					for (int i = 1; i < lastUserId; i++) {
						for (int j = 0; j < lastItemId; j++) {
							
							double c = getC(i, j);
							
							double s = getS(i, j);
							
							double f = getFinalScore(c,s,wc,ws);
							
							if (!Double.isNaN(f)){
							
								System.out.println("Query: " + i + " Tuple: " + (j+1) + " Coverage: " + c + " Specificity: " + s + " Score: " + f);
								
								table.put(new String (i + "-" + (j+1)), new Float(f));
							}
							
						}
					}
					
					sortTable();

					writeOutput(combinationID, preserveOrder,lasttime);
					
					pW.endAlgorithm();
					
				}
					
			}
			
		}
					
	}

	private static void writeOutput(int combinationID, boolean preserveOrder, long lasttime) throws IOException {
		
		String query = "";
		
		List<Pair<String,Long>> queryTime = new ArrayList<Pair<String,Long>>();
		
		for (String comb : output) {
			
			String[] spl = comb.split("-");
			
			int q = Integer.valueOf(spl[0]);
			
			int wl = Integer.valueOf(spl[1])-1;
			
			if (!preserveOrder){
			
				query = queries[q].trim() + " " + wlQueries[wl].trim();
				
				System.out.println(query + " Score: " + table.get(comb));
				
				queryTime.add(new Pair<String,Long>(query,lasttime));
				
//				pW.writeQuery(combinationID, query, lasttime);
				
			} else {
				
				ArrayList<Integer> prevQueries = getPrevQueries(q,wl+1);
				
				for (Integer queryNumber : prevQueries) {
					
					query = queries[queryNumber].trim() + " " + wlQueries[wl].trim();
					
					String combination = new String(queryNumber + "-" + (wl+1));
					
					System.out.println(query + " Score: " + table.get(combination));
					
					table.remove(combination);
					
					queryTime.add(new Pair<String,Long>(query,lasttime));
					
//					pW.writeQuery(combinationID,query, lasttime);
					
				}
				
			}
			
		}
		
		pW.writeQueries(combinationID,queryTime);
		
	}

	private static ArrayList<Integer> getPrevQueries(int q, int wl) {
	
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i <= q; i++) {
			
			if (table.containsKey(new String (i + "-" + wl))){
				ret.add(i);
			}
		}
		
		return ret;
	
	}

	private static void sortTable() {
		
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

	private static void loadWLQueries(String string) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(new File(string)));
		
		String line = br.readLine();
		
		ArrayList<String> t = new ArrayList<String>(); 
		
		while (line!=null){
			
			t.add(line);
			
			line = br.readLine();
		}
		
		br.close();
		
		wlQueries = t.toArray(new String[0]);
		
	}

	private static double getFinalScore(double c, double s, double wc, double ws) {
		return wc*c + ws*s;
	}

	private static void readTotal(String file) throws IOException {
		
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
		
		queries = quer.toArray(new String[1]);
		
	}

	private static String[] getValues(String line) {
		
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

	private static float getC(int i, int j) {
		
		return d[i][j]/d[0][j];
		
	}

	private static float getS(int i, int j) {
		float sum = 0;
		
		for (int k = 0; k < d[i].length; k++) {
			
			if (!Float.isNaN(d[i][k]))
				sum+=d[i][k];
		}
		return d[i][j] / sum;
	}

}
