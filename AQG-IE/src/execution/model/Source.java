package execution.model;

import exploration.model.Database;
import exploration.model.DatabasesModel;

public abstract class Source {

	public static final int SOURCE_UNIQUE = -1;
	
	private DatabasesModel databaseModel;
	private int maxOrder;

	public Source(DatabasesModel databaseModel, int maxOrder){
		this.databaseModel = databaseModel;
		this.maxOrder = maxOrder;
	}
	
	protected DatabasesModel getDatabasesModel() {
		return databaseModel;
	}
	
	public boolean match(Database processableDatabase, Database sampledDatabase, int version_pos_seed, int version_neg_seed, int idWorkload){
		
		if (maxOrder == SOURCE_UNIQUE){
			return matchEvaluation(processableDatabase,sampledDatabase,version_pos_seed, version_neg_seed, idWorkload);
		} else{
			return matchExecution(processableDatabase,sampledDatabase,maxOrder,version_pos_seed, version_neg_seed, idWorkload);
		}
		
	}

	protected abstract boolean matchExecution(Database processableDatabase,
			Database sampledDatabase, int maxOrder, int version_pos_seed, int version_neg_seed, int idWorkload);

	protected abstract boolean matchEvaluation(Database processableDatabase,
			Database sampledDatabase, int version_pos_seed, int version_neg_seed, int idWorkload);
	
}
