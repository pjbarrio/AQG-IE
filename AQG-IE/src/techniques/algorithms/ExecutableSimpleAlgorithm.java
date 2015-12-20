package techniques.algorithms;

import java.util.List;

import com.google.gdata.util.common.base.Pair;

import sample.generation.model.SampleBuilderParameters;
import sample.generation.model.impl.DummySampleConfiguration;
import searcher.interaction.formHandler.TextQuery;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.core.Instances;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;

public abstract class ExecutableSimpleAlgorithm implements ExecutableAlgorithm{

	protected Database database;
	protected String version;
	protected int max_query_size;
	protected int min_support;
	protected int min_support_after_update;
	protected int workload;
	protected Sample sample;
//	private int w_combination_ID;
//	private int[] w_algorithm;
	protected int w_parameter_ID;
	private int sample_configuration;
	private int version_pos_seed;
	private int version_neg_seed;


	protected ExecutableSimpleAlgorithm(Sample sample, int max_query_size,
			int min_support, int min_supp_after_update) {
		this.database = sample.getDatabase();
		this.version = sample.getVersion().getName();
		this.workload = sample.getWorkload().getId();
		this.version_pos_seed = sample.getVersionSeedPos();
		this.version_neg_seed = sample.getVersionSeedNeg();
		this.sample_configuration = sample.getSampleConfiguration().getId();
		
		this.sample = sample;
		
		this.max_query_size = max_query_size;
		this.min_support = min_support;
		this.min_support_after_update = min_supp_after_update;
	}
	
	public void executeAlgorithm(Instances sample, persistentWriter pW, SampleBuilderParameters sp) throws Exception{
		
		int w_parameter_ID = getParameterId(pW,max_query_size,min_support,min_support_after_update);
		
		int w_combination_ID = pW.setAlgorithm(getName(),database.getId(),version,workload, version_pos_seed, version_neg_seed, sample_configuration, sp.getId(),w_parameter_ID);
		
//		w_combination_ID = w_algorithm[0]; //First stores the combination
//		
//		w_parameter_ID = w_algorithm[1]; //Second stores the parameterId
		
		Clock.startTime(ClockEnum.OVERALL_ALGORITHM);
		
		Clock.startTime(ClockEnum.SIMPLE_ALGORITHM);
		
		List<Pair<TextQuery,Long>> queries = execute(sample, pW,sp);
		
		Clock.stopTime(ClockEnum.OVERALL_ALGORITHM);
		
		pW.writeQueries(w_combination_ID,queries);
		
		pW.updateCurrentAlgorithmTime(w_combination_ID, Clock.getMeasuredTime(ClockEnum.OVERALL_ALGORITHM));
		
	}
	
	protected abstract int getParameterId(persistentWriter pW, int maxQuerySize, int minSupport, int minSuppAfterUpdate);

	protected abstract List<Pair<TextQuery,Long>> execute(Instances sample, persistentWriter pW, SampleBuilderParameters sp) throws Exception;
	
	protected abstract String getName();
	
	
}
