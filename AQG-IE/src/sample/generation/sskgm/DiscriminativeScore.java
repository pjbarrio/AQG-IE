package sample.generation.sskgm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import online.sample.wordsDistribution.HashBasedComparator;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class DiscriminativeScore {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//Third after GenerateVerbChunksMaps
		
		int minSupport = Integer.valueOf(args[0]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		
		String[] relationName = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment-Arrest-Trial","PersonTravel","VotingResult"};
		
		int splits = 1;
		
		int alts = 5;
		
		int size = 10000;		
		
		for (int j = 1; j <= alts; j++) {

			Map<String,Integer> global = new HashMap<String, Integer>();
			
			Map<String,Map<String,Integer>> rmaps = new HashMap<String, Map<String,Integer>>();
			
			for (int rel = 0; rel < relationName.length; rel++) {
				
				System.out.println("Split: " + j);
				
				for (int i = 1; i <= splits; i++) {
					
					int realsize = size;
					
					System.out.println("Size: " + realsize);
					
					Map<String, Integer> map = pW.getChunksForRelations(collection,relationName[rel],realsize,j);
					
					Set<String> toRemove = new HashSet<String>();
					
					for (Entry<String,Integer> entry : map.entrySet()) {
						
						if (entry.getValue() < minSupport){
							toRemove.add(entry.getKey());
						}else{
						
							Integer freq = global.remove(entry.getKey());
							
							if (freq == null){
								freq = 0;
							}
							
							freq += entry.getValue();
							
							global.put(entry.getKey(), freq);

						}
						
					}
					
					map.keySet().removeAll(toRemove);
					
					rmaps.put(relationName[rel],map);
					
				}
				
			}				

			System.out.println("Calculating Significances...");
			
			Map<String,Map<String,Double>> significances = calculateSignificances(global,rmaps);
			
			System.out.println("Calculating Clarities...");
			
			Map<String,Double> clarity = calculateClarities(significances);
			
			System.out.println("Calculating Discriminativeness...");
			
			Map<String,Map<String,Double>> discrimi = calculateDiscriminative(significances,clarity,relationName);
			
			for (Entry<String,Map<String,Double>> entry : discrimi.entrySet()) {
				
				Map<String,Double> map = entry.getValue();
				
				List<String> list = new ArrayList<String>(map.keySet());
				
				Collections.sort(list, new HashBasedComparator<String>(map));
				
				pW.saveDiscriminativeRules(collection,entry.getKey(),j,size,list,map,minSupport);
				
			}
			
		}
		
		
	}

	private static Map<String, Map<String, Double>> calculateDiscriminative(
			Map<String, Map<String, Double>> significances,
			Map<String, Double> clarity, String[] rel) {
		
//		receives significances chunk,<rel,val>
// 		receives clarity chunk,val
		
		Map<String,Map<String,Double>> ret = new HashMap<String, Map<String,Double>>(); //relation,<chunk,discr>
		
		for (int i = 0; i < rel.length; i++) {
			ret.put(rel[i], new HashMap<String, Double>());
		}
		
		double sqrt2 = Math.sqrt(2.0);

		int proc = 0;
		
		for (Entry<String,Map<String,Double>> entry : significances.entrySet()) {
			
			for (Entry<String,Double> entry2 : entry.getValue().entrySet()) {
				
				if (proc % 100 == 0){
					System.out.println("Processed Dis: " + proc);
				}
				
				proc++;
				
				double sig2 = Math.pow(entry2.getValue(), 2.0);
				
				double cl2 = Math.pow(clarity.get(entry.getKey()), 2.0);
				
				double discriminativness = (((sig2)*(cl2))/(Math.sqrt((sig2)+(cl2))))*(sqrt2);
				
				ret.get(entry2.getKey()).put(entry.getKey(), discriminativness);
				
			}
			
		}
		
		return ret;
		
	}

	private static Map<String, Double> calculateClarities(
			Map<String, Map<String, Double>> significances) {
		
//		receives chunk,<rel,val>
		
		Map<String,Double> ret = new HashMap<String, Double>(); //chunk, clarity

		int proc = 0;
		
		for (Entry<String,Map<String,Double>> entry : significances.entrySet()) {
			
			if (proc % 100 == 0){
				System.out.println("Processed Clar: " + proc);
			}
			
			proc++;

			
			Collection<Double> c = entry.getValue().values();
			
			double clarity;
			
			if (c.size() == 1){
				clarity = 1.0;
			}else{
			
				clarity = (Math.log((c.size()*max(c))/(sum(c))))*(1.0 / Math.log(c.size()));
			
			}
			ret.put(entry.getKey(),clarity);
			
		}
		
		return ret;
	}

	private static double sum(Collection<Double> c) {
		double sum = 0.0;
		for (Double doubled : c) {
			sum+=doubled;
		}
		return sum;
	}

	private static double max(Collection<Double> c) {
		double max = 0.0;
		for (Double doubled : c) {
			max = (doubled>max)? doubled:max;
		}
		return max;
	}

	private static Map<String, Map<String, Double>> calculateSignificances(
			Map<String, Integer> global, Map<String, Map<String, Integer>> rmaps) {
		
		Map<String,Map<String,Double>> ret = new HashMap<String, Map<String,Double>>(); //chunk,<rel,val>
	
		double log2 = Math.log(2);
		
		int proc = 0;
		
		for (Entry<String,Map<String,Integer>> entry : rmaps.entrySet()) {
			
			for (Entry<String,Integer> entry2 : entry.getValue().entrySet()) {
				
				double sig = (Math.log((double)(entry2.getValue() + 1))/log2)/(Math.log((double)(global.get(entry2.getKey()) + 1))/log2);
				
				if (proc % 100 == 0){
					System.out.println("Processed Sig: " + proc);
				}
				
				proc++;
				
				Map<String,Double> maux = ret.get(entry2.getKey());
				
				if (maux == null){
					maux = new HashMap<String, Double>();
					ret.put(entry2.getKey(), maux);
				}
				
				maux.put(entry.getKey(), sig);
			}
			
		}
		
		return ret;
		
	}
	
}
