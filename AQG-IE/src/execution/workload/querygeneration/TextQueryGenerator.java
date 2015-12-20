package execution.workload.querygeneration;

import java.util.Arrays;

import searcher.interaction.formHandler.TextQuery;
import utils.query.QueryParser;

public class TextQueryGenerator implements QueryGenerator<String> {

	@Override
	public TextQuery generateQuery(String data) {
		
		return new TextQuery(Arrays.asList(data.split(" ")));
		
//		if (data.contains(" "))
//			return QueryParser.MUST_SYMBOL + data.replaceAll(" ", " " + QueryParser.MUST_SYMBOL);
//		return QueryParser.MUST_SYMBOL + data;
	}

}
