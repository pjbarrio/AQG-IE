package execution.workload.querygeneration;

import searcher.interaction.formHandler.TextQuery;

public interface QueryGenerator<T> {

	TextQuery generateQuery(T data);

}
