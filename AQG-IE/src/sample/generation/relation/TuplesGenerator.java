package sample.generation.relation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import searcher.interaction.formHandler.TextQuery;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Document;
import extraction.relationExtraction.impl.RDFRelationExtractor;

public class TuplesGenerator {

	private static HashMap<TextQuery, List<Tuple>> tuples;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//Fifth and last of the sequence.
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		
		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
				"VotingResult","ProductIssues","Quotation","PollsResult"*/"Indictment-Arrest-Trial"};
		
		for (int rel = 0; rel < relations.length; rel++) {
			
			TupleQueryGenerator tqg = new TupleQueryGenerator(pW.getInferredTypes(relations[rel]), true, true, false, pW.getOmittedFields(relations[rel]));
			
			System.out.println(relations[rel]);
			
			String matchingTuplesFile = pW.getMatchingTuplesWithSources(collection,relations[rel]);

			Hashtable<Document, ArrayList<Tuple>> tuplesTable = TuplesLoader.loadDocumenttuplesTuple(matchingTuplesFile);
			
			tuples = new HashMap<TextQuery, List<Tuple>>();
			
			Set<Tuple> tuplesSet = new HashSet<Tuple>();
			
			for (Entry<Document,ArrayList<Tuple>> entry : tuplesTable.entrySet()){
				
				tuplesSet.addAll(entry.getValue());
				
				ArrayList<Tuple> aTuples = entry.getValue();
				
				for (int i = 0; i < aTuples.size(); i++) {
					
					TextQuery s = tqg.generateQuery(aTuples.get(i));
					
					List<Tuple> list = tuples.get(s);
					
					if (list == null){
						list = new ArrayList<Tuple>();
						tuples.put(s, list);
					}
					
					list.add(aTuples.get(i));
				}
				
			}
			
			FileUtils.writeLines(pW.getTuplesFile(collection,relations[rel]), tuplesSet);
			
			List<TextQuery> queries = new ArrayList<TextQuery>(tuples.keySet());
			
			Collections.sort(queries, new Comparator<TextQuery>(){

				@Override
				public int compare(TextQuery q1, TextQuery q2) {
					return Double.compare(tuples.get(q2).size(), tuples.get(q1).size());
				}
				
			});
			
			List<Tuple> finalf = new ArrayList<Tuple>(queries.size());
			
			for (int i = 0; i < queries.size(); i++) {
				
				finalf.add(tuples.get(queries.get(i)).get(0));
				
			}
			
			FileUtils.writeLines(pW.getSortedTuplesFile(collection,relations[rel]), finalf);
			
		}

	}

}
