package utils.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import searcher.interaction.formHandler.TextQuery;
import utils.word.extraction.WordExtractorAbs;

public class QueryParser {
	public static final String MUST_SYMBOL = "+";
	public static final String MUST_NOT_SYMBOL = "-";

	private synchronized static void parseQuery(String keywords, List<String> must,List<String> mustNot) {
		
		must.clear();
		
		mustNot.clear();
		
		String[] words = keywords.split(" ");
		
		String aux;
		
		for (String keyword : words) {
			if (keyword.trim().startsWith(MUST_SYMBOL)){
				aux = keyword.substring(1).toLowerCase().intern();
				
				if (aux.equals(WordExtractorAbs.SPECIAL_REPLACEMENT))
					must.add(WordExtractorAbs.SPECIAL_CLASS);
				else{
					must.add(aux);
				}
			}else if (keyword.trim().startsWith(MUST_NOT_SYMBOL)){
				
				aux = keyword.substring(1).toLowerCase().intern();
				
				if (aux.equals(WordExtractorAbs.SPECIAL_REPLACEMENT))
					mustNot.add(WordExtractorAbs.SPECIAL_CLASS);
				else{
					mustNot.add(aux);
				}
				
			}
		}
		
//		Collections.sort(must);
//		Collections.sort(mustNot);
		
	}

	public static void parseQuery(TextQuery query,
			List<String> must_words, List<String> must_not_words) {
		
		must_words.addAll(query.getWords());
				
	}
}
