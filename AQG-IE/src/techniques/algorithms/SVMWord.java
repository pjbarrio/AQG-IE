package techniques.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.persistentWriter;
import weka.core.Instances;

import com.google.gdata.util.common.base.Pair;

import exploration.model.Sample;

public class SVMWord extends ExecutableSimpleAlgorithm {

	public SVMWord(Sample sample, int max_query_size, int min_support,
			int min_supp_after_update) {
		super(sample, max_query_size, min_support, min_supp_after_update);
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getSVMWordParameter(maxQuerySize,minSupport,minSuppAfterUpdate);
	}

	@Override
	protected List<Pair<TextQuery, Long>> execute(Instances sample,
			persistentWriter pW, SampleBuilderParameters sp) throws Exception {
		
		List<Pair<TextQuery, Long>> ret = new ArrayList<Pair<TextQuery,Long>>(sample.numAttributes());
		
		for (int i = 0; i < sample.numAttributes(); i++) {
			
			ret.add(new Pair<TextQuery, Long>(new TextQuery(Arrays.asList(sample.attribute(i).name())), 0L));
			
		}
		
		return ret;
		
	}

	@Override
	protected String getName() {
		return "SVM_WORD";
	}

}
