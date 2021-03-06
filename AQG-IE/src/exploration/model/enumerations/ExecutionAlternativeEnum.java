package exploration.model.enumerations;

public enum ExecutionAlternativeEnum{
	
	CROSSABLE_SOURCE, 
	
	CROSSABLE_CONFIGURATION, 

	ADAPTATION_STRATEGY,

	ADAPTATION_CONDITION,

	DOCUMENTS_PERFORMANCE_COMPARATOR,
	
	QUERY_PERFORMANCE_COMPARATOR,
	
	SOURCE,
	
	GENERATION,
	
	FINISHING_STRATEGY,
	
	ADAPTIVE_STRATEGY,
	
	COLLECTING_DOCUMENT_STRATEGY,

	PERFORMANCE_CHECKER,
	
	SAMPLE_EVALUATOR,
	
	TOKEN_COMPARATOR,
	
	NUMBER_OF_TOKENS, 
	
	PERFORMANCE_THRESHOLD, 
	
	USELESS_QUERY_CONDITION, 
	
	USELESS_QUERY_THRESHOLD, 
	
	NUMBER_OF_DOCUMENTS,
	
	NUMBER_OF_WORDS, 
	
	PROBABILISTIC_DISTRIBUTION,
	
	PROBABILISTIC_DISTRIBUTION_SIMILARITY_THRESHOLD,
	
	DOCUMENTS_PERFORMANCE_THRESHOLD,
	
	QUERIES_PERFORMANCE_THRESHOLD,
	
	FINISHING_USELESS_QUERY_CONDITION,
	
	FINISHING_USELESS_QUERY_THRESHOLD, 
	
	POWER_LAW_ALPHA, 
	
	POWER_LAW_BETA, 
	
	POWER_LAW_EPSILON,
	
	WEIBULL_NU,
	
	WEIBULL_BETA,
	
	DATABASE_SIMILARITY,
	
	CLUSTER_FUNCTION,
	
	K_MEANS_SIMILARITY_FUNCTION,
	
	K_MEANS_K_VALUE,
	
	FUZZY_C_SIMILARITY_FUNCTION,
	
	FUZZY_C_K_VALUE,

	DATABASE_LIMIT,
	
	DATABASES_TO_CONTACT,
	
	QUERIES_PER_DATABASE,
	
	QUERIES_A_SECOND,
	
	QUERY_PROCESSING_TIME,
	
	TOTAL_DOCUMENTS_TO_RETRIEVE,
	
	DOCUMENTS_TO_EXTRACT_A_SECOND,
	
	EXTRACTION_TIME,
	
	RETRIEVAL_TIME,
	
	MIX_EVALUATIONS,
	
	STOP_WHILE_UPDATE,
	
	DATABASE_SELECTION,
	
	STATISTICS_FOR_SAMPLE_SELECTOR,
	
	SCHEDULER,
	
	EVALUATIONS_CONCURRENTLY,
	
	ROUND_ROBIN_QUANTUM,
	
	EXECUTION_ORDER,
	
	AGGREGATED_QUERIES,
	
	QUERY_SCHEDULER,
	
	IE_INSTANCES, 
	
	IE_SEQUENTIAL,
	
	Q_SEQUENTIAL,
	
//	VERSION,
	
	ALGORITHM_SELECTION,
	
	QUERY_PERFORMANCE_FINISHING,
	
	GENERATION_SOURCE,
	
	GENERATION_PARAMETERS,
	
	FINISHING_STRATEGY_PARAMETERS,
	
	ADAPTIVE_STRATEGY_PARAMETERS,
	
	COLLECTING_DOCUMENT_STRATEGY_PARAMETERS,
	
	SCHEDULER_PARAMETERS,
	
	QUERY_SCHEDULER_PARAMETERS,
	
	ALGORITHM_SELECTION_PARAMETERS,
	
	EXECUTION_POLICY_PARAMETERS,
	
	ADAPTATION_CONDITION_PARAMETERS,
	
	ADAPTATION_STRATEGY_PARAMETERS,
	
	DOCUMENT_PERFORMANCE_COMPARATOR_PARAMETERS,
	
	QUERY_PERFORMANCE_FINISHING_PARAMETERS,
	
	CROSSABLE_SOURCE_PARAMETERS,
	
	CLUSTER_FUNCTION_PARAMETERS,
	
	PERFORMANCE_CHECKER_PARAMETERS,
	
	SAMPLE_EVALUATOR_PARAMETERS,
	
	PROBABILISTIC_DISTRIBUTION_PARAMETERS,

	TUPLE_100,
	
	TUPLE_250,
	
	TUPLE_500,
	
	TUPLE_1000,
	
	TUPLE_2500,

	TUPLE_5000,
	
	TUPLE_10000,
	
	TUPLE_25000,
	
	TUPLE_50000,
	
	TUPLE_100000,
	
	TUPLE_700000,
	
	QPROBER,
	
	RIPPER,

	OPTIMISTIC,
	
	MSC,

	INCREMENTAL,
	
	//ADDITIONAL FOR EXPERIMENT
	
	TUPLE_50,
	
	TUPLE_150,
	
	TUPLE_200,
	
	TUPLE_300,
	
	TUPLE_350,
	
	TUPLE_400,
	
	TUPLE_450,
	
	CROSSABLE_DATABASE_SIMILARITY,
	
	CROSSABLE_CLUSTER_FUNCTION, 
	
	CROSSABLE_K_MEANS_K_VALUE, 
	
	CROSSABLE_K_MEANS_SIMILARITY_FUNCTION, 
	
	CROSSABLE_FUZZY_C_K_VALUE, 
	
	CROSSABLE_FUZZY_C_SIMILARITY_FUNCTION,
	
	QUERY_ROUND_ROBIN_QUANTUM,
	
	MEASURE_AFTER_N_DOCUMENTS_FINISHING,
	
	MEASURE_AFTER_N_DOCUMENTS,
	
//	INFORMATION_EXTRACTION,
	
	CONTENT_EXTRACTION,
		
	NEXT_GENERATION_ALGORITHM, 
	
	NEW_DATABASE,
	
	NEXT_GENERATION,
	
	NEXT_CROSSABLE_SOURCE,
	
	NEXT_CROSSABLE_CONFIGURATION,
	
	NEXT_CROSSABLE_DATABASE_SIMILARITY,
	
	NEXT_CROSSABLE_CLUSTER_FUNCTION,
	
	NEXT_CROSSABLE_K_MEANS_K_VALUE,
	
	NEXT_CROSSABLE_K_MEANS_SIMILARITY_FUNCTION,
	
	NEXT_CROSSABLE_FUZZY_C_K_VALUE,
	
	NEXT_CROSSABLE_FUZZY_C_SIMILARITY_FUNCTION,
	
	NEXT_GENERATION_PARAMETERS,
	
	NEW_ALGORITHM,
	
	NEW_ALGORITHM_PARAMETERS, 
	
	SAMPLE_GENERATOR,
	
	POSITIVE_QUERY_POOL,
	
	POSITIVE_QUERY_POOL_PARAMETERS,
	
	NEGATIVE_QUERY_POOL,
	
	NEGATIVE_QUERY_POOL_PARAMETERS,
	
	CONTENT_LOADER, 
	
	POSITIVE_QUERY_GENERATOR_PARAMETERS, 
	
	WORD_LOADER,
	
	INFERRED_TYPES,
	
	UNIQUE,
	
	LOWERCASE,
	
	STEMMED,
	
	WORD_LOADER_PARAMETERS,
	
	QUERY_OTHER_SOURCE,
	
	WORD_SELECTION_STRATEGY,
	
	INTERACTION_PERSISTER,
	
	INTERACTION_PERSISTER_PARAMETERS,
	
	DISK_PREFIX,
	
	FILE_INDEX,
	
	POSITIVE_QUERY_GENERATOR,
	
	NEGATIVE_QUERY_GENERATOR,
	
	NEGATIVE_QUERY_GENERATOR_PARAMETERS,
	
	POSITIVE_FIRST,
	
	NO_FILTERING_FIELDS,
	
	TUPLES_AS_STOP_WORDS,
	
	SAMPLE_EXECUTOR, 
	
	SAMPLE_EXECUTOR_PARAMETERS,
	
	CARDINALITY_FUNCTION,
	
	CARDINALITY_FUNCTION_PARAMETERS,
	
	OMMITED_ATTRIBUTES, 
	
	POSITIVE_QUERY_POOL_EXECUTOR_PARAMETERS,
	
	POSITIVE_QUERY_POOL_EXECUTOR,
	
	NEGATIVE_QUERY_POOL_EXECUTOR,
	
	NEGATIVE_QUERY_POOL_EXECUTOR_PARAMETERS,
	
	QUERY_POOL_PERFORMANCE_CHECKER_PARAMETERS,
	
	QUERY_POOL_PERFORMANCE_CHECKER,
	
	QUERY_PERFORMANCE_CHECKER,
	
	QUERY_PERFORMANCE_CHECKER_PARAMETERS,
	
	MEMORY_FOR_QUERIES,
	
	NUMBER_OF_USELESS_QUERIES, 
	
	MINIMUM_PRECISION_IN_RESULTS, 
	
	NUMBER_OF_QUERIES, 
	
	PRECISION_OF_QUERY_POOL, 
	
	EXPERIMENT_ID,
	
	PROCESSED_DOCUMENTS,
	
	USEFUL_DOCUMENTS_WORD_EXTRACTOR,
	
	GENERAL_WORD_EXTRACTOR,
	
	FEATURE_WEIGHTER, 
	
	INITIAL_TUPLES,
	
	ALEATORIZE_DBS,
	
	CARDINALITY_FUNCTION_LIMIT, 
	
	MAX_DOCS_PER_QUERY, 
	
	REVERSE_QUERY_POOL, 
	
	NUMBER_OF_QUERIES_FROM_POOL;
	
			
//	RESULT_DOCUMENT_HANDLER,
//	
//	NAVIGATION_HANDLER,
//	
//	NAVIGATION_HANDLER_PARAMETERS,
//	
//	SEARCH_ROUND_ID,
//	
//	QUERY_RESULT_PAGE_HANDLER,
//	
//	HTML_TAG_CLEANER;

	
	
}
