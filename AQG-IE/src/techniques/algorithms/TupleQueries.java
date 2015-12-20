package techniques.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import techniques.baseline.Tuples.algorithm.TuplesQueryGenerator;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.core.Instances;

import com.google.gdata.util.common.base.Pair;

import execution.workload.tuple.Tuple;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Document;
import exploration.model.Sample;

public class TupleQueries extends ExecutableSimpleAlgorithm {

	private String name;
	private TupleQueryGenerator tqg;

	public TupleQueries(Sample sample, int max_query_size, int min_support,
			int min_supp_after_update, String name, TupleQueryGenerator tqg) {
		super(sample, max_query_size, min_support, min_supp_after_update);
		this.name = name;
		this.tqg = tqg;
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		
		return 1; //The parameters do not affect.

	}

	@Override
	protected List<Pair<TextQuery, Long>> execute(Instances sample,
			persistentWriter pW, SampleBuilderParameters sp) throws Exception {
		
		Collection<Document> documents = pW.getUsefulDocuments(this.sample, sp.getUsefulDocuments());
		
		Set<Tuple> tuples = pW.getTuples(this.sample, documents);
		
		System.err.println("is it relevant in order?");
		
		List<Pair<TextQuery,Long>> ret = new ArrayList<Pair<TextQuery,Long>>(tuples.size());
		
		for (Tuple tuple : tuples) {
			
			ret.add(new Pair<TextQuery, Long>(tqg.generateQuery(tuple),0L));
			
		}
		
		Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
		
//		long time = Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM);
		
		return ret;
		
	}

	@Override
	protected String getName() {
		return name;
	}
}
