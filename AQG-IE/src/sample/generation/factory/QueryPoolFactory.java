package sample.generation.factory;

import sample.generation.model.SampleConfiguration;
import sample.generation.model.queryPool.QueryPool;
import sample.generation.model.queryPool.impl.CachedNegativeSampleQueryPool;
import sample.generation.model.queryPool.impl.OtherSourceQueryPool;
import sample.generation.model.queryPool.impl.RelationWordsQueryPool;
import sample.generation.model.queryPool.impl.RulesQueryPool;
import sample.generation.model.queryPool.impl.SignificantPhrasesQueryPool;
import sample.generation.model.queryPool.impl.TuplesQueryPool;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import execution.model.factory.ContentExtractorFactory;
import execution.model.factory.InteractionPersisterFactory;
import execution.model.parameters.Parametrizable;
import execution.workload.querygeneration.TextQueryGenerator;
import execution.workload.tuple.Tuple;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.QueryPoolEnum;

public class QueryPoolFactory {

	public static QueryPool generateInstance(String queryPoolName,
			Parametrizable parameters, SampleConfiguration sc, persistentWriter pW, InteractionPersister interactionPersister, boolean isForUseful) {
				
		switch (QueryPoolEnum.valueOf(queryPoolName)){
		
		case LIST_OF_WORDS:
			return new OtherSourceQueryPool(isForUseful,
							WordSelectionStrategyFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.WORD_SELECTION_STRATEGY).getString(),new WordExtractor(sc.getContentExtractor(),sc.getContentLoader()),
									WordLoaderFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.WORD_LOADER).getString(),
											parameters.loadParameter(ExecutionAlternativeEnum.WORD_LOADER_PARAMETERS))),
							QueryGeneratorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_GENERATOR).getString(),parameters.loadParameter(ExecutionAlternativeEnum.NEGATIVE_QUERY_GENERATOR_PARAMETERS),pW,pW.getRelation(sc.getWorkloadModel())));
		case CACHED_NEGATIVE_SAMPLE:
			return new CachedNegativeSampleQueryPool(Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.EXPERIMENT_ID).getString()),Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.PROCESSED_DOCUMENTS).getString()),new TextQueryGenerator(),isForUseful);
		
		case TUPLES:
			return new TuplesQueryPool(isForUseful,sc.getRelationExtractionSystem(), sc.getVersion().getCondition(),
					QueryGeneratorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR).getString(),parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR_PARAMETERS),pW,pW.getRelation(sc.getWorkloadModel())),
					sc.getVersion(),sc.getWorkloadModel(),
					sc.getContentExtractor(),sc.getWorkloadModel().getRelations(),interactionPersister,Integer.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.INITIAL_TUPLES).getString()));
		case RELATIONS:
			
			return new RelationWordsQueryPool(sc.getExtractionSystemId(),sc.getRelationConfiguration(),sc.getBaseCollection(),isForUseful,sc.getWorkloadModel(),sc.getVersion(),QueryGeneratorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR).getString(),parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR_PARAMETERS),pW,pW.getRelation(sc.getWorkloadModel())),
					Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.TUPLES_AS_STOP_WORDS).getString()),ASEEvaluatorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.FEATURE_WEIGHTER).getString()), sc.getDocsInTraining());
		case SIGNIFICANT_PHRASES:
			return new SignificantPhrasesQueryPool(sc.getExtractionSystemId(),sc.getRelationConfiguration(),sc.getBaseCollection(),isForUseful,sc.getWorkloadModel(),sc.getVersion(),QueryGeneratorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR).getString(),parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR_PARAMETERS),pW,pW.getRelation(sc.getWorkloadModel())),
					Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.TUPLES_AS_STOP_WORDS).getString()), sc.getDocsInTraining());	
		case RULES:
			return new RulesQueryPool(sc.getBaseCollection(),sc.getDocsInTraining(),sc.getExtractionSystemId(),sc.getRelationConfiguration(),isForUseful,QueryGeneratorFactory.generateInstance(parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR).getString(),parameters.loadParameter(ExecutionAlternativeEnum.POSITIVE_QUERY_GENERATOR_PARAMETERS),pW,pW.getRelation(sc.getWorkloadModel())), sc.getWorkloadModel(), sc.getVersion(), 
					Boolean.valueOf(parameters.loadParameter(ExecutionAlternativeEnum.TUPLES_AS_STOP_WORDS).getString()));
		default:
			return null;
		
		}
		
	}

}
