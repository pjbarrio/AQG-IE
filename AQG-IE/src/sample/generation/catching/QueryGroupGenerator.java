package sample.generation.catching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.RelationConfiguration;

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
import extraction.relationExtraction.RelationExtractionSystem;

public class QueryGroupGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
				
		int db = Integer.valueOf(args[3]); //3000
		
		int size = Integer.valueOf(args[4]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Map<String,String[]> types = new HashMap<String, String[]>();
		
		types.put("PersonCareer", new String[]{"PERSON","CAREER"});
		types.put("NaturalDisaster", new String[]{"NATURALDISASTER","LOCATION"});
		types.put("ManMadeDisaster", new String[]{"MANMADEDISASTER","LOCATION"});
		types.put("PersonTravel", new String[]{"PERSON","LOCATION"});
		types.put("VotingResult", new String[]{"POLITICALEVENT","PERSON"});
		types.put("Indictment-Arrest-Trial", new String[]{"CHARGE","PERSON"});
		
		Map<String,Pair<Integer, Integer>> extr = new HashMap<String, Pair<Integer,Integer>>();
		
		String[] relationses = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment,pW,ieSystem,true,false,db,new SgmlContentExtraction());
		
		int relationConf = RelationConfiguration.getRelationConf(relationExperiment);
		
		extr.put(tr.getName(), new Pair<Integer, Integer>(ieSystem, relationConf));
		
		int queries = 500;
		
		int[] splits = new int[]{1,2,3,4,5};

		boolean[] tasw = new boolean[]{false,true};

		QueryGenerator<String> relationQG = new TextQueryGenerator();

		QueryGenerator<String> ruleQG = new RipperRuleQueryGenerator();
		
		Map<String,Integer> entityTypeTable = pW.getEntityTypeTable();

		Map<String,List<Integer>> typeIds = createTypeIds(types,entityTypeTable);
		
		Map<String,Integer> relationshipTable = pW.getRelationshipTable();
		
		ASEvaluation[] eval = new ASEvaluation[]{/*new InfoGainAttributeEval(),new SMOAttributeEval(),*/new ChiSquaredWithYatesCorrectionAttributeEval()};

		ASSearch search = new Ranker();
		
		Classifier[] classifier = new Classifier[]{new JRip()};
		
		List<TextQuery> queriesList = new ArrayList<TextQuery>();
		
		Set<TextQuery> queriesSet = new HashSet<TextQuery>();
		
		Map<TextQuery,Set<Integer>> queryEntitiesMap = new HashMap<TextQuery, Set<Integer>>();		
		
		Map<TextQuery,Set<Integer>> queryRelationshipMap = new HashMap<TextQuery, Set<Integer>>();
		
		Map<TextQuery,Set<String>> queryExtractorMap = new HashMap<TextQuery, Set<String>>();
		
		Map<TextQuery,Set<String>> queryCollectionMap = new HashMap<TextQuery, Set<String>>();
		
		for (int i = 0; i < relationses.length; i++) {

			for (int j = 0; j < splits.length; j++) {

				for (int k = 0; k < tasw.length; k++) {

//					case TUPLE_QUERY_GENERATOR:
//						return new TupleQueryGenerator(InferredTypeFactory.getInferredTypes(parameter.loadParameter(ExecutionAlternativeEnum.INFERRED_TYPES).getString(),pW),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.UNIQUE).getString()).booleanValue(),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.LOWERCASE).getString()).booleanValue(),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.STEMMED).getString()).booleanValue(),CSVToStringFactory.generateList(parameter.loadParameter(ExecutionAlternativeEnum.OMMITED_ATTRIBUTES).getString()));
					
					for (int q = 0; q < eval.length; q++) {

						List<String> rel = FileUtils.readLines(pW.getRelationKeywordsFile(collection,relationses[i],tasw[k],splits[j],eval[q],tr.getName()));

						for (int l = 0; l < rel.size() && l < queries; l++) {
							
							TextQuery qq = relationQG.generateQuery(rel.get(l));
							
							queriesList.add(qq);							
						
							if (queriesSet.add(qq)){ //first time
								queryEntitiesMap.put(qq, new HashSet<Integer>());
								queryRelationshipMap.put(qq, new HashSet<Integer>());
								queryExtractorMap.put(qq, new HashSet<String>());
								queryCollectionMap.put(qq, new HashSet<String>());
							}
							
							queryEntitiesMap.get(qq).addAll(typeIds.get(relationses[i]));
							
							queryRelationshipMap.get(qq).add(relationshipTable.get(relationses[i]));
							
							queryExtractorMap.get(qq).add(tr.getName());
							
							queryCollectionMap.get(qq).add(collection);
							
						}
						
					}
/*
					for (int cl = 0; cl < classifier.length; cl++) {
						
						List<String> quer = transformRulesIntoQueries(new File(pW.getRulesFromSplitModel(collection,relationses[i],size,splits[j],classifier[cl],tasw[k],tr.getName())));

						for (int l = 0; l < quer.size() && l < queries; l++) {
							
							TextQuery qq = ruleQG.generateQuery(quer.get(l));
							
							queriesList.add(qq);
							
							if (queriesSet.add(qq)){ //first time
								queryEntitiesMap.put(qq, new HashSet<Integer>());
								queryRelationshipMap.put(qq, new HashSet<Integer>());
								queryExtractorMap.put(qq, new HashSet<String>());
								queryCollectionMap.put(qq, new HashSet<String>());
							}
							
							queryEntitiesMap.get(qq).addAll(typeIds.get(relationses[i]));
							
							queryRelationshipMap.get(qq).add(relationshipTable.get(relationses[i]));
							
							queryExtractorMap.get(qq).add(tr.getName());
							
							queryCollectionMap.get(qq).add(collection);
							
						}
						
					}*/
					
					//Significant Phrases					
					
//					List<String> quer = FileUtils.readLines(new File(pW.getSignificantPhrasesFromSplitModel(collection, relationses[i], size, splits[j], tasw[k], tr.getName())));
//
//					for (int l = 0; l < quer.size() && l < queries; l++) {
//						
//						TextQuery qq = relationQG.generateQuery(quer.get(l));
//						
//						queriesList.add(qq);
//						
//						if (queriesSet.add(qq)){ //first time
//							queryEntitiesMap.put(qq, new HashSet<Integer>());
//							queryRelationshipMap.put(qq, new HashSet<Integer>());
//							queryExtractorMap.put(qq, new HashSet<String>());
//							queryCollectionMap.put(qq, new HashSet<String>());
//						}
//						
//						queryEntitiesMap.get(qq).addAll(typeIds.get(relationses[i]));
//						
//						queryRelationshipMap.get(qq).add(relationshipTable.get(relationses[i]));
//						
//						queryExtractorMap.get(qq).add(tr.getName());
//						
//						queryCollectionMap.get(qq).add(collection);
//						
//					}
					
					
				}

			}

		}

		int total = 0;
		
		for (int i = 0; i < queriesList.size(); i++) {
			
			if (queriesSet.remove(queriesList.get(i))){
				
				total++;
				
				TextQuery t = queriesList.get(i);
				
				long qId = pW.getTextQuery(t);
				
				Set<Integer> entities = queryEntitiesMap.get(queriesList.get(i));

				for (Integer idEntityType : entities) {
					
					pW.writeTextQueryEntity(qId,idEntityType);
					
				}
				
				Set<Integer> relationships = queryRelationshipMap.get(queriesList.get(i));
					
				for (Integer idRelationship : relationships) {
					
					pW.writeTextQueryRelation(qId,idRelationship);
					
				}
				
				Set<String> extractors = queryExtractorMap.get(queriesList.get(i));
				
				for (String extractor : extractors){
					
					pW.writeTextQueryExtractor(qId,extr.get(extractor).first,extr.get(extractor).second);
					
				}
				
				Set<String> collections = queryCollectionMap.get(queriesList.get(i));
				
				for (String coll : collections){
					
					pW.writeTextQueryCollection(qId,coll);
					
				}
				
				System.out.println(total + " - " + t.getText());
				
			}
			
		}
		
		//	case TUPLE_QUERY_GENERATOR:
		//		return new TupleQueryGenerator(InferredTypeFactory.getInferredTypes(parameter.loadParameter(ExecutionAlternativeEnum.INFERRED_TYPES).getString(),pW),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.UNIQUE).getString()).booleanValue(),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.LOWERCASE).getString()).booleanValue(),Boolean.valueOf(parameter.loadParameter(ExecutionAlternativeEnum.STEMMED).getString()).booleanValue(),CSVToStringFactory.generateList(parameter.loadParameter(ExecutionAlternativeEnum.OMMITED_ATTRIBUTES).getString()));

	}

	private static Map<String,List<Integer>> createTypeIds(Map<String,String[]> types,
			Map<String, Integer> entityTypeTable) {
		
		Map<String,List<Integer>> ret = new HashMap<String,List<Integer>>(types.size());
		
		for (Entry<String,String[]> entry : types.entrySet()) {
			
			ret.put(entry.getKey(),createList(entry.getValue(),entityTypeTable));
			
		}
		
		return ret;
	}

	private static List<Integer> createList(String[] strings,
			Map<String, Integer> entityTypeTable) {
		
		List<Integer> ret = new ArrayList<Integer>(strings.length);
		
		for (int i = 0; i < strings.length; i++) {
			
			ret.add(entityTypeTable.get(strings[i]));
			
		}
		
		return ret;
	}

	private static List<String> transformRulesIntoQueries(File rulesFile) throws IOException{

		List<String> rules = FileUtils.readLines(rulesFile);

		List<String> queries = new ArrayList<String>();

		for (int i = 0; i < rules.size(); i++) {

			if (rules.get(i).startsWith("("))
				queries.add(rules.get(i));

		}

		return queries;

	}

//	private List<Tuple> loadInitialSeed(Database database, persistentWriter pW, Version version, WorkloadModel workload, int version_seed) {
//
//		File tuplesFile = new File(pW.getSeedTuples(database.getName(), version.getName(), workload, version_seed));
//
//		List<String> tuples;
//		try {
//
//			tuples = FileUtils.readLines(tuplesFile);
//
//			List<Tuple> ret = new ArrayList<Tuple>(tuples.size());
//
//			for (int i = 0; i < tuples.size(); i++) {
//
//				ret.add(TupleReader.generateTuple(tuples.get(i)));
//
//			}
//
//			return ret;
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//
//	}

}
