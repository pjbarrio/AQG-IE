package techniques.algorithms;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Database;
import exploration.model.Sample;
import sample.generation.model.SampleBuilderParameters;
import sample.generation.relation.attributeSelection.impl.OkapiAttributeEval;
import sample.generation.relation.attributeSelection.impl.SMOAttributeEval;
import searcher.interaction.formHandler.TextQuery;
import techniques.baseline.Ripper.queryManagement.MyJRip;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;

public class QXtract extends ExecutableSimpleAlgorithm {

	class RipperRunnable implements Runnable{

		private MyJRip ripper;
		private Instances sample;

		public RipperRunnable(MyJRip ripper, Instances sample) {
			this.ripper = ripper;
			this.sample = sample;
		}

		@Override
		public void run() {
			try {
				ripper.buildClassifier(sample);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	class ASSRunnable implements Runnable{

		private AttributeSelection attsel;
		private Instances sample;

		public ASSRunnable(AttributeSelection attsel, Instances sample) {
			this.attsel = attsel;
			this.sample = sample;
		}

		@Override
		public void run() {
			try {
				attsel.SelectAttributes(sample);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private int fold;
	private double minNo;
	private int optimizationRuns;
	private long seedValue;
	private boolean pruning;
	private boolean checkErrorRate;

	public QXtract(Sample sample, int max_query_size, int min_support, int min_support_after_update, int fold,
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
		
		Thread tripper = new Thread(new RipperRunnable(ripper,sample));
		
		tripper.start();
		
		AttributeSelection attselsvm = new AttributeSelection();
		
		ASSearch searchsvm = new Ranker();
		
		attselsvm.setEvaluator(new SMOAttributeEval());

		attselsvm.setSearch(searchsvm);
		
		Thread tsvm = new Thread(new ASSRunnable(attselsvm,sample));
		
		tsvm.start();
		
		AttributeSelection attselokapi = new AttributeSelection();
		
		ASSearch searchokapi = new Ranker();
		
		attselokapi.setEvaluator(new OkapiAttributeEval());

		attselokapi.setSearch(searchokapi);
		
		Thread tokapi = new Thread(new ASSRunnable(attselokapi,sample));
		
		tokapi.start();
		
		tripper.join();
		tsvm.join();
		tokapi.join();
		
		Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
		
		long time = Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM);
		
		List<Pair<TextQuery, Long>> ripperQueries = ripper.getQueries(pW,sample.attribute(sample.classIndex()),time);

		List<Pair<TextQuery,Long>> svmQueries = selectAttributes(attselsvm,time,sample);
		
		List<Pair<TextQuery, Long>> okapiQueries = selectAttributes(attselokapi,time,sample);
		
		return roundrobin(okapiQueries,ripperQueries,svmQueries);
		
	}

	private List<Pair<TextQuery, Long>> roundrobin(
			List<Pair<TextQuery, Long>> ...lists) {
		
		int size =  0;
		
		for (int i = 0; i < lists.length; i++) {
			size+=lists[i].size();
		}
		
		List<Pair<TextQuery, Long>> ret = new ArrayList<Pair<TextQuery,Long>>(size);
		
		boolean added = true;
		
		while (added){
			added = false;
			for (int i = 0; i < lists.length; i++) {
				if (!lists[i].isEmpty()){
					ret.add(lists[i].remove(0));
					added = true;
				}
			}
		}
		
		return ret;
		
	}

	private List<Pair<TextQuery, Long>> selectAttributes(
			AttributeSelection attsel, long time, Instances sample) {
		
		double[][] indices = null;
		try {
			indices = attsel.rankedAttributes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Pair<TextQuery, Long>> ret = new ArrayList<Pair<TextQuery,Long>>();
		
		for (int i = 0; i < indices.length; i++) {

			Attribute att = sample.attribute((int)indices[i][0]);

			AttributeStats values = sample.attributeStats((int)indices[i][0]);

			int usefuls = values.nominalCounts[1];

			int useless = values.nominalCounts[0];

			if (usefuls > useless){ //the attribute separates

				ret.add(new Pair<TextQuery, Long>(new TextQuery(att.name()), time));

			}

		}
		
		return ret;
		
	}

	@Override
	protected String getName() {
		return "QXtract";
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getQXtractParameter(maxQuerySize,minSupport,minSuppAfterUpdate,fold,minNo,optimizationRuns,seedValue,pruning,checkErrorRate);
	}

}
