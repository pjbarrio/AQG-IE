package execution.model.source;

import execution.model.Source;
import exploration.model.Database;
import exploration.model.DatabasesModel;
import exploration.model.clusterfunction.ClusterFunction;

public class ClusteredSource extends Source {

	private ClusterFunction clusterFunction;

	public ClusteredSource(DatabasesModel databaseModel, ClusterFunction clusterFunction, int maxOrder) {
		super(databaseModel,maxOrder);
		this.clusterFunction = clusterFunction;
	}

	@Override
	protected boolean matchExecution(Database processableDatabase,
			Database sampledDatabase, int maxOrder, int version_pos_seed, int version_neg_seed, int idWorkload) {
		return getDatabasesModel().match(processableDatabase, sampledDatabase, clusterFunction, maxOrder, version_pos_seed,version_neg_seed,idWorkload);
	}

	@Override
	protected boolean matchEvaluation(Database processableDatabase,
			Database sampledDatabase, int version_pos_seed, int version_neg_seed, int idWorkload) {
		return getDatabasesModel().belongs(processableDatabase,sampledDatabase,clusterFunction, version_pos_seed,version_neg_seed, idWorkload);
	}





}
