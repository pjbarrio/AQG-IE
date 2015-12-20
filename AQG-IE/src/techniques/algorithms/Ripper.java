package techniques.algorithms;

import java.util.List;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Database;
import exploration.model.Sample;
import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import techniques.baseline.Ripper.queryManagement.MyJRip;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.core.Instances;

public class Ripper extends ExecutableSimpleAlgorithm {

	private int fold;
	private double minNo;
	private int optimizationRuns;
	private long seedValue;
	private boolean pruning;
	private boolean checkErrorRate;

	public Ripper(Sample sample, int max_query_size, int min_support, int min_support_after_update, int fold,
			double minNo, int optimizationRuns, long seedValue,
			boolean pruning, boolean checkErrorRate) {
		super(sample, max_query_size,min_support,min_support_after_update);
		this.fold = fold;
		this.minNo = minNo;
		this.optimizationRuns = optimizationRuns;
		this.seedValue = seedValue;
		this.pruning = pruning;
		this.checkErrorRate = checkErrorRate;
	}

	@Override
	protected List<Pair<TextQuery,Long>> execute(Instances sample, persistentWriter pW, SampleBuilderParameters sp)
			throws Exception {
	
//		pW.setRipperAdditionalParameters(w_parameter_ID,fold,minNo,optimizationRuns,seedValue,pruning,checkErrorRate);
		
		MyJRip ripper = new MyJRip();
		
		ripper.setCheckErrorRate(checkErrorRate);
		
		ripper.setFolds(fold);
		
		ripper.setMinNo(minNo);
		
		ripper.setOptimizations(optimizationRuns);
		
		ripper.setSeed(seedValue);
		
		ripper.setUsePruning(pruning);
		
		ripper.buildClassifier(sample);
		
		Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
		
		return ripper.getQueries(pW,sample.attribute(sample.classIndex()),Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM));
		
	}

	@Override
	protected String getName() {
		return "Ripper";
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getRipperParameter(maxQuerySize,minSupport,minSuppAfterUpdate,fold,minNo,optimizationRuns,seedValue,pruning,checkErrorRate);
	}

}
