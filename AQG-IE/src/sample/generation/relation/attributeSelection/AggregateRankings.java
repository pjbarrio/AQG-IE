package sample.generation.relation.attributeSelection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;
import extraction.relationExtraction.RelationExtractionSystem;

import utils.execution.MapBasedComparator;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;

public class AggregateRankings {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
		
		int db = Integer.valueOf(args[3]); //3000
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
//		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
//				"VotingResult","ProductIssues","Quotation","PollsResult",*/"Indictment-Arrest-Trial"};
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment, pW, ieSystem, true, false, db, new SgmlContentExtraction());

		String relation = RelationConfiguration.getRelationName(relationExperiment);
		
	    AttributeSelection attsel = new AttributeSelection();
	   
	    ASEvaluation eval = new InfoGainAttributeEval();
	    
	    ASSearch search = new Ranker();
	    
	    attsel.setEvaluator(eval);
	    
	    attsel.setSearch(search);
		
	    int splits = 10;
		
		int size = 1000;

		int[] spl = {1,2,3,4,5,6,7,8,9,10};
		
		Set<String> words = new HashSet<String>();
		
		List<Map<String,Integer>> ranks = new ArrayList<Map<String, Integer>>();
		
		boolean[] tuplesAsStopWords = {true,false};
		
		for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {

			for (int a=0;a<spl.length; a++) {
				
				System.gc();
				
				int j = spl[a];
				
				System.out.println("Split: " + j);
				
				for (int i = 1; i <= splits; i++) {
					
					int realsize = size * i;
					
					System.out.println("Size: " + realsize);
					
					File input = new File(pW.getRelationWordsFromSplitModel(collection,relation,realsize,j,eval,search,tuplesAsStopWords[tasw],tr.getName()));
					
					if (input.exists()){
						
						read(input,words,ranks);
						
					}
					
				}
							
			}

			List<String> fWords = createRanking(words,ranks);
			
			File output = new File(pW.getAggregatedRanking(collection,relation,eval,search,tuplesAsStopWords[tasw],tr.getName()));
			
			FileUtils.writeLines(output, fWords);

			
		}
		
		
		
	}

	private static List<String> createRanking(Set<String> words,
			List<Map<String, Integer>> ranks) {
		
		int pos = 0;
		
		Map<String,Double> map = new HashMap<String, Double>();
		
		for (String word : words) {
			
			pos++;
			
			if (pos % 1000 == 0)
				System.out.println("Processed: " + pos + " out Of " + words.size());
			
			double ranking = calculateRanking(word,ranks);
			
			map.put(word, ranking);
			

		}
		
		List<String> finalWords = new ArrayList<String>(words);
		
		Collections.sort(finalWords, Collections.reverseOrder(new MapBasedComparator<String,Double>(map)));
		
		return finalWords;

		
	}

	private static double calculateRanking(String word,
			List<Map<String, Integer>> ranks) {
		
		double added = 0;
		
		double cantNoFound = 0.0;
		
		double lastRanking = -1.0;
		
		for (int i = 0; i < ranks.size(); i++) {
			
			Integer pos = ranks.get(i).get(word);
			
			if (pos == null){
				cantNoFound++;
			}else{
				if (pos>lastRanking)
					lastRanking = (double)pos;
				added+=pos;
			}
		
		}
		
		return (added+(lastRanking*cantNoFound))/(double)ranks.size();
		
	}

	private static void read(File input, Set<String> words,
			List<Map<String, Integer>> ranks) throws IOException {
		
		List<String> rank = FileUtils.readLines(input);
		
		Map<String,Integer> map = new HashMap<String, Integer>();
		
		double lastValue = -1.0;
		int ranking = 0;
		
		for (int i = 0; i < rank.size(); i++) {
			
			String line = rank.get(i);
			
			int index = line.indexOf(',');
			
			double value = Double.valueOf(line.substring(0, index));
						
			String word = line.substring(index+1);
			
			words.add(word);
			
			if (value != lastValue){
				ranking++;
				lastValue = value;
			}
								
			map.put(word, ranking);
			
		}
		
		ranks.add(map);
		
	}

}
