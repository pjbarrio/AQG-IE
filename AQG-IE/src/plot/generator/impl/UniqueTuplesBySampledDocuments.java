package plot.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

import execution.workload.tuple.Tuple;
import exploration.model.Document;
import exploration.model.Sample;
import exploration.model.enumerations.EntropyEnum;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;
import utils.algorithms.EntropyCalculator;
import utils.persistence.persistentWriter;

public class UniqueTuplesBySampledDocuments extends
		SampleGenerationSeriesGenerator {

	
	
	private String xNormalization;
	private String yNormalization;
	private persistentWriter pW;
	private EntropyEnum which;

	public UniqueTuplesBySampledDocuments(persistentWriter pW, EntropyEnum which, String yNormalization,
			String xNormalization) {
		this.xNormalization = xNormalization;
		this.yNormalization = yNormalization;
		this.pW = pW;
		this.which = which;
	}

	@Override
	protected String getNormalizedYAttribute() {
		return yNormalization;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return xNormalization;//TOTAL_DOCUMENTS; //NO_NORMALIZATION for count
	}

	@Override
	public String generateWhere() {
		return DOCUMENT_POSITION_IN_SAMPLE  + "> 0";
	}

	
	@Override
	public List<String> generateAttributes() {
		
		List<String> ret = new ArrayList<String>(2);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);
		
		return ret;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>(2);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);
		
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer sampleGeneration, double normalizeXValue, double normalizeYValue) {
		
		Series ret = new Series(sampleGeneration.toString());

		Sample sample = pW.getSample(sampleGeneration);

//		sample.setId(sampleGeneration);
		
		String attribute = "";
		
		if (which != EntropyEnum.ANY){
			if (which == EntropyEnum.LARGE){
				attribute = pW.getLargeAttributeName(sampleGeneration);
			}else if (which == EntropyEnum.SMALL){
				attribute = pW.getSmallAttributeName(sampleGeneration);
			}
		}
		
		List<Document> useful = new ArrayList<Document>(pW.getUsefulDocuments(sample, 10000));

		Map<Document,List<Tuple>> idtuplesTable= pW.getSampleTuples(sample);

		double lastSampledDocument = NUMBER_OF_SAMPLED_DOCUMENTS_UT;

		Set<Tuple> tuples = new HashSet<Tuple>();

		Set<String> attValues = new HashSet<String>();
		
		boolean needlast = false;

		for (int i = 0; i < useful.size(); i++) {

			if (i==lastSampledDocument){

				ret.addPair(Pair.getPair(lastSampledDocument, calculateEntropy(tuples,attValues)));

				lastSampledDocument += NUMBER_OF_SAMPLED_DOCUMENTS_UT;

				needlast = false;
			} else {
				needlast = true;
			}

			if (idtuplesTable.containsKey(useful.get(i))){
				List<Tuple> tups = idtuplesTable.get(useful.get(i));
				tuples.addAll(tups);
				if (!attribute.isEmpty()){
					for (Tuple tup : tups) {
						attValues.add(tup.getFieldValue(attribute));
					}
				}
			}
			else{
				System.out.println("In EntropyByAddeDocument does not contain tuple: " + sample.getDatabase().getId() + " - " + sample.getSampleConfiguration().getId()  + " - " + sample.getVersionSeedPos() + "-" + sample.getVersionSeedNeg() + " - " + useful.get(i).getDatabase().getId() + " - " + useful.get(i).getId());
			}
		}

		if (needlast)
			ret.addPair(Pair.getPair(lastSampledDocument, calculateEntropy(tuples,attValues)));

		return ret;

		
	}

	@Override
	public String getTitle() {
		return "Unique Tuples by Sampled Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Sampled Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Unique Tuples";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}

	private double calculateEntropy(Set<Tuple> tuples, Set<String> attributes) {

		if (attributes.isEmpty())
			return tuples.size();
		else{
			return attributes.size();
		}
			

	}
	
	
}
