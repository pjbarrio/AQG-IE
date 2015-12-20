package execution.model.source;

import execution.model.Source;
import exploration.model.Database;
import exploration.model.DatabasesModel;

public class LocalSource extends Source {

	public LocalSource(DatabasesModel databaseModel, int maxOrder) {
		super(databaseModel,maxOrder);
	}

	@Override
	protected boolean matchExecution(Database processableDatabase,
			Database sampledDatabase, int maxOrder, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		return sampledDatabase.equals(processableDatabase);
	
	}

	@Override
	protected boolean matchEvaluation(Database processableDatabase,
			Database sampledDatabase, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		return !sampledDatabase.isGlobal() && sampledDatabase.equals(processableDatabase);
	
	}





}
