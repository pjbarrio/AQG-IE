package sample.generation.model.executor;

import searcher.interaction.formHandler.TextQuery;
import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Document;

public abstract class QueryPoolExecutor {

	private boolean retrievedLast = true;
	private boolean lastHasMoreQueries = false;
	
	public boolean hasMoreQueries(){
		
		if (retrievedLast){
			
			retrievedLast = false;
			
			lastHasMoreQueries = _hasMoreQueries();
			
		}
		
		return lastHasMoreQueries;
	}
	
	protected abstract boolean _hasMoreQueries();

	public TextQuery getNextQuery(){
		retrievedLast = true;
		return _getNextQuery();
	}
	
	protected abstract TextQuery _getNextQuery();

	public abstract  void informDocument();
	
	public abstract  void informHit();
	
	public void initialize(Database database, persistentWriter pW, int version_seed){
		retrievedLast = true;
		_initialize(database,pW,version_seed);
	}

	protected abstract void _initialize(Database database, persistentWriter pW,
			int version_seed);

	public abstract boolean retrievesUseful();

	public abstract void updateQueries(Document document);

	public abstract void informExhausted();
	
}
