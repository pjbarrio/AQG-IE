package execution.workload.tuple.querygeneration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import online.documentHandler.contentExtractor.impl.DummyContentExtractor;

import searcher.interaction.formHandler.TextQuery;
import utils.query.QueryParser;
import utils.word.extraction.WordExtractor;
import execution.workload.querygeneration.QueryGenerator;
import execution.workload.tuple.Tuple;

public class TupleQueryGenerator implements QueryGenerator<Tuple>{

	private WordExtractor we = new WordExtractor(new DummyContentExtractor(),null);
	private boolean unique;
	private boolean lowercase;
	private boolean stemmed;
	private List<String> ommitedTypes;
	
	public TupleQueryGenerator(List<String> inferredTypes,
			boolean unique, boolean lowercase, boolean stemmed, List<String> ommittedTypes){
		this.ommitedTypes = new ArrayList<String>(ommittedTypes);
		this.ommitedTypes.addAll(inferredTypes);
		this.unique = unique;
		this.lowercase = lowercase;
		this.stemmed = stemmed;
	}
	
	public TextQuery[] generateQueries(Tuple[] seedArray,List<String> inferredTypes,  boolean unique, boolean lowercase,boolean stemmed) {
		
		List<TextQuery> ret = new ArrayList<TextQuery>();
				
		for (int i = 0; i < seedArray.length; i++) {
			
			TextQuery tq = generateQuery(seedArray[i],inferredTypes,unique,lowercase,stemmed);
			
			ret.add(tq);
			
		}

		return ret.toArray(new TextQuery[ret.size()]);
		
	}

	public TextQuery generateQuery(Tuple t, List<String> inferredTypes,
			boolean unique, boolean lowercase, boolean stemmed) {
			
		String[] fields = t.getFieldNames();;
		
		List<String> query = new ArrayList<String>();
		
		for (int j = 0; j < fields.length; j++) {
			
			if (!inferredTypes.contains(fields[j]))
			
				query.addAll(getStringValue(t.getFieldValue(fields[j]),unique, lowercase,stemmed));
			
		}
		
//		if (query.isEmpty()){
//			System.err.println("here!");
//			return null;
//		}
		
		System.out.println(t.toString() + " --> " + query.toString());
		
		return new TextQuery(query);

	}

	private List<String> getStringValue(String fieldValue,boolean unique, boolean lowercase, boolean stemmed) {
	
//		String field = fieldValue.trim().replaceAll("[^(a-zA-Z|\\s)]", "");

		String[] terms = we.getWords(fieldValue,unique,lowercase,stemmed);
	
		List<String> ret = new ArrayList<String>();
		
		for (int i = 0; i < terms.length; i++) {
			
			if (!terms[i].trim().equals(""))
			
				ret.add(terms[i].trim());
			
		}
		
		return ret;
	}

	public TextQuery generateQuery(Tuple tuple) {
		return generateQuery(tuple, ommitedTypes, unique, lowercase, stemmed);
	}

}
