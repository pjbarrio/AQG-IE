package execution.model.source;

import execution.model.Source;
import exploration.model.Database;
import exploration.model.DatabasesModel;
import exploration.model.source.similarity.SimilarityFunction;

public class SimilarSource extends Source {

	private SimilarityFunction databaseSimilarityCalculator;

	public SimilarSource(DatabasesModel databaseModel, SimilarityFunction databaseSimilarityCalculator, int maxOrder) {
		
		super(databaseModel,maxOrder);
		
		this.databaseSimilarityCalculator = databaseSimilarityCalculator;
	}

	@Override
	protected boolean matchExecution(Database processableDatabase,
			Database sampledDatabase, int maxOrder, int version_pos_seed, int version_neg_seed, int idWorkload) {

		return getDatabasesModel().match(sampledDatabase, processableDatabase, databaseSimilarityCalculator,maxOrder, version_pos_seed,version_neg_seed,idWorkload);

	}

	@Override
	protected boolean matchEvaluation(Database processableDatabase,
			Database sampledDatabase, int version_pos_seed, int version_neg_seed, int idWorkload) {

		return getDatabasesModel().match(sampledDatabase, processableDatabase, databaseSimilarityCalculator,1,version_pos_seed, version_neg_seed,idWorkload);
		
	}




}
