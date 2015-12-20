package sample.generation.catching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import sample.generation.factory.CSVToStringFactory;
import sample.generation.factory.InferredTypeFactory;
import sample.generation.relation.attributeSelection.impl.ChiSquaredWithYatesCorrectionAttributeEval;
import sample.generation.relation.attributeSelection.impl.SMOAttributeEval;
import searcher.interaction.formHandler.TextQuery;
import techniques.baseline.Ripper.queryManagement.RipperRuleQueryGenerator;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.query.QueryParser;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import execution.workload.querygeneration.QueryGenerator;
import execution.workload.querygeneration.TextQueryGenerator;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Database;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.ExecutionAlternativeEnum;

public class RelationKeywordsWriter{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String collection = "TREC";
		
		int size = 10000;
		
		persistentWriter pW = PersistenceImplementation.getWriter();

		int queries = 500;
		
		int[] version = new int[]{1,2,3,4,5};

		boolean[] tasw = new boolean[]{true,false};

		QueryGenerator<String> relationQG = new TextQueryGenerator();

		String[] relationses = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","PersonTravel",
				"VotingResult","Indictment-Arrest-Trial"};
		
		Map<String,Integer> relationshipTable = pW.getRelationshipTable();
		
		List<Integer> relIds = createList(relationses,relationshipTable);
		
		ASEvaluation[] eval = new ASEvaluation[]{new InfoGainAttributeEval(),new SMOAttributeEval(),new ChiSquaredWithYatesCorrectionAttributeEval()};

		for (int i = 0; i < relationses.length; i++) {

			for (int j = 0; j < version.length; j++) {

				for (int k = 0; k < tasw.length; k++) {

					for (int q = 0; q < eval.length; q++) {

						List<String> rel = FileUtils.readLines(pW.getRelationKeywordsFile(collection,relationses[i],tasw[k],version[j],eval[q]));

						for (int l = 0; l < rel.size() && l < queries; l++) {
							
//							TextQuery qq = new TextQuery(Arrays.asList(relationQG.generateQuery(rel.get(l))));
							
							TextQuery qq = new TextQuery(Arrays.asList(rel.get(l)));
							
//							System.out.println(rel.get(l));
							
							pW.writeRelationKeyword(i+17,1/*relIds.get(i)*/,version[j],tasw[k],eval[q],l+1,qq);
							
						}
						
					}

				}

			}

		}
		
	}

	

	private static List<Integer> createList(String[] types,
			Map<String, Integer> typeTable) {
		
		List<Integer> ret = new ArrayList<Integer>(types.length);
		
		for (int i = 0; i < types.length; i++) {
			
			ret.add(typeTable.get(types[i]));
			
		}
		
		return ret;
	}

	
}
