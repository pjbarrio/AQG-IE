package techniques.algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.google.gdata.util.common.base.Pair;

import sample.generation.model.SampleBuilderParameters;
import searcher.Searcher;
import searcher.interaction.formHandler.TextQuery;
import searcher.lucene.LuceneSearcher;
import utils.dispatcher.Dispatcher;
import utils.id.Idhandler;
import utils.id.TuplesLoader;
import utils.id.useful.UsefulUselessHandler;
import utils.persistence.persistentWriter;
import utils.query.QueryParser;
import utils.word.extraction.WordExtractor;
import weka.core.Instances;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.impl.condition.WorkLoadCondition;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import extraction.relationExtraction.RelationExtractionSystem;

public class Tuples extends ExecutableSimpleAlgorithm {

	//Needs utils.execution.initial.InitialTuplesExtractor and then 
	//techniques.baseline.Tuples.seedGeneration.SeedTupleGeneration and then
	//techniques.baseline.Tuples.algorithm.TuplesQueryGenerator 
	
	private long hits_per_page;
	private double querySubmissionPerUnitTime;
	private long queryTimeConsumed;
	private double ieSubmissionPerUnitTime;
	private long ieTimeConsumed;
	private Hashtable<TextQuery, Long> queryTime;
	private HashSet<Document> processed;
	private Set<Tuple> tuplesDetected;
	private Dispatcher<Integer> querydispatcher;
	private Dispatcher<Document> docDispatcher;
	private ArrayList<String> must_words;
	private ArrayList<String> must_not_words;
	private long RetrievalTime;
	private int seedTupleNumber;
	private RelationExtractionSystem relationExtractionSystem;
	private Searcher searcher;
	private UsefulCondition condition;
	private int initialTuples;
	private int max_number_of_queries;
	private TupleQueryGenerator tqg;

	public Tuples(Sample sample, int max_query_size,
			int min_support, int min_supp_after_update, long hits_per_page,
			double querySubmissionPerUnitTime, long queryTimeConsumed,
			double ieSubmissionPerUnitTime, long ieTimeConsumed, long RetrievalTime,int seedTupleNumber, RelationExtractionSystem relExtSys, Searcher searcher, int initialTuples, int max_number_ofqueries, TupleQueryGenerator tqg) {
		super(sample, max_query_size,min_support,min_supp_after_update);
		this.hits_per_page = hits_per_page;
		this.querySubmissionPerUnitTime = querySubmissionPerUnitTime;
		this.queryTimeConsumed = queryTimeConsumed;
		this.ieSubmissionPerUnitTime = ieSubmissionPerUnitTime;
		this.ieTimeConsumed = ieTimeConsumed;
		this.RetrievalTime = RetrievalTime;
		this.seedTupleNumber = seedTupleNumber;
		this.relationExtractionSystem = relExtSys;
		this.searcher = searcher;
		Version v = Version.generateInstance(sample.getVersion().getName(), sample.getWorkload());
		condition = v.getCondition();
		this.initialTuples = initialTuples;
		this.max_number_of_queries = max_number_ofqueries;
		this.tqg = tqg;
	}

	@Override
	public List<Pair<TextQuery,Long>> execute(Instances sample, persistentWriter pW, SampleBuilderParameters sp)
			throws IOException {
				
		queryTime = new Hashtable<TextQuery, Long>();
		
		processed = new HashSet<Document>();
		
		tuplesDetected = new HashSet<Tuple>();
		
		String database = this.database.getName();
				
//		pW.setTupleAdditionalParameters(w_parameter_ID,hits_per_page,querySubmissionPerUnitTime, queryTimeConsumed,ieSubmissionPerUnitTime, ieTimeConsumed);
		
		String seedTuples = pW.getSeedTuples(database,version,super.sample.getWorkload(),seedTupleNumber, relationExtractionSystem.getName());
		
		boolean lowercase = true;
		
		boolean stemmed = false;
		
		boolean unique = true;
		
		querydispatcher = new Dispatcher<Integer>(querySubmissionPerUnitTime, queryTimeConsumed);
		
		ArrayList<Long> times = new ArrayList<Long>();
		
		ArrayList<TextQuery> queries = loadTuplesAsQueries(seedTuples,unique,lowercase,stemmed,times,initialTuples);
		
		docDispatcher = new Dispatcher<Document>(ieSubmissionPerUnitTime, ieTimeConsumed);
		
		startQuerying(queries,times, unique,lowercase,stemmed);
		
		return getQueries(queries,times,pW);


	}

	@Override
	protected String getName() {
		
		return "TUPLE_" + hits_per_page;
		
	}

	private List<Pair<TextQuery,Long>> getQueries(ArrayList<TextQuery> queries, ArrayList<Long> times,persistentWriter pW) throws IOException {
		
		List<Pair<TextQuery,Long>> ret = new ArrayList<Pair<TextQuery,Long>>(queries.size());
		
		while (queries.size()>0){
			
			ret.add(new Pair<TextQuery,Long>(queries.remove(0), times.remove(0)));
			
		}
		
		return ret;
	}

	private void startQuerying(ArrayList<TextQuery> queries, ArrayList<Long> times, boolean unique, boolean lowercase, boolean stemmed) {
		
		long i = 0;
		
		List<Document> results = new ArrayList<Document>();

		must_words = new ArrayList<String>();

		must_not_words = new ArrayList<String>();
		
		while (i<queries.size() && i < max_number_of_queries){
			
			must_words.clear();

			must_not_words.clear();
			
			QueryParser.parseQuery(queries.get((int)i), must_words, must_not_words);
			
			System.err.println(i + " - " + must_words.toString());
			
			searcher.doSearch(must_words, must_not_words);
				
//			results.clear();
			
			results = searcher.retrieveMaxAllowedDocuments(must_words, must_not_words);//.retrieveMaxAllowedDocuments(i);
			
			processResults(results,queries,times, unique,lowercase,stemmed,queryTime.get(queries.get((int)i)),hits_per_page);
			
			searcher.cleanQuery(must_words,must_not_words);
			
			i++;
			
		}
		
	}

	private void processResults(List<Document> results,
			ArrayList<TextQuery> queries,ArrayList<Long> times, boolean unique, boolean lowercase, boolean stemmed, long Querytime, long hits_per_page) {
		
		int i = 0;
		
		while (i < hits_per_page && i < results.size()) {
			
			Document document = results.get(i);
			
			i++;
			
			if (processed.contains(document))
				continue;
			
			docDispatcher.submit(document, Querytime + i*RetrievalTime);
			
			processed.add(document);
			
			System.out.println(i + ".- Processing: " + document.getId() + " out of: " + results.size());
			
			Tuple[] tuples = relationExtractionSystem.execute(/*super.database.getId(), */document);
			
			for (Tuple tuple : tuples) {
				
				if (condition.isItUseful(tuple)){
					
					processTuples(tuple,queries,times,unique,lowercase,stemmed,document);
					
				}
				
			}
					
		}
		
	}

	private void processTuples(Tuple tuple, ArrayList<TextQuery> queries, ArrayList<Long> times, boolean unique,boolean lowercase, boolean stemmed, Document document) {
		
		if (tuplesDetected.add(tuple)){
			
			TextQuery query = generateQuery(tuple,unique,lowercase,stemmed);
			
			if (!query.getText().isEmpty() && !queries.contains(query)){
				
				querydispatcher.submit(queries.size(), docDispatcher.getProcessedTime(document));
				
				queryTime.put(query, docDispatcher.getProcessedTime(document));
				
				System.out.println("ADDED: " + query);
				
				queries.add(query);
				
				times.add(docDispatcher.getProcessedTime(document));
			
			}
		
		}
		
	}

	private TextQuery generateQuery(Tuple tuple, boolean unique, boolean lowercase, boolean stemmed) {
		
		return tqg.generateQuery(tuple);
		
//		List<String> queryWords = new ArrayList<String>();
//		
//		for (String field : tuple.getFieldNames()){
//			
//			if (!inferred.contains(field)){
//			
//				String value = tuple.getFieldValue(field);
//				
//				String[] words = wE.getWords(value,unique,lowercase,stemmed);
//				
//				for (int i = 0; i < words.length; i++) {
//					if (!queryWords.contains(words[i]))
//						queryWords.add(words[i]);
//				}
//				
//			}
//			
//		}
		
//		return new TextQuery(queryWords);
		
	}

	private ArrayList<TextQuery> loadTuplesAsQueries(String file, boolean unique, boolean lowercase, boolean stemmed, ArrayList<Long> times, int initialTuples) throws IOException {

		ArrayList<TextQuery> ret = new ArrayList<TextQuery>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String line = br.readLine();
		
		while (line!=null && ret.size() < initialTuples){
			
			Tuple t = TupleReader.generateTuple(line);
			
			if (!tuplesDetected.contains(t)){
			
				tuplesDetected.add(t);
				
				TextQuery query = generateQuery(t,unique,lowercase,stemmed);
				
				if (!query.getText().isEmpty() && !ret.contains(query)){
					
					querydispatcher.submit(ret.size(), 0);
					
					queryTime.put(query, querydispatcher.getProcessedTime(ret.size()));
					
					times.add(querydispatcher.getProcessedTime(ret.size()));
					
					System.out.println("ADDED START: " + query);
					
					ret.add(query);
				}
			}
			line = br.readLine();
			
		}
		
		br.close();
		
		return ret;
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getTupleParameter(maxQuerySize,minSupport,minSuppAfterUpdate,hits_per_page,querySubmissionPerUnitTime, queryTimeConsumed,ieSubmissionPerUnitTime, ieTimeConsumed);
	}

}
