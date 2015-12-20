package plot.generator.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Sample;
import exploration.model.enumerations.EntropyEnum;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;
import utils.algorithms.EntropyCalculator;
import utils.id.TuplesLoader;
import utils.persistence.persistentWriter;

public class EntropyByAddedDocuments extends SampleGenerationSeriesGenerator {

	private persistentWriter pW;
	private EntropyEnum which;
	private static Hashtable<String,Hashtable<Document, ArrayList<Tuple>>> tuplesTable;

	public EntropyByAddedDocuments(persistentWriter pW, EntropyEnum which) {
		this.pW = pW;
		this.which = which;
	}

	@Override
	protected String getNormalizedYAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	protected String getNormalizedXAttribute() {
		return NO_NORMALIZATION;
	}

	@Override
	public List<String> generateAttributes() {

		List<String> ret = new ArrayList<String>(2);

		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);

		return ret;

	}

	@Override
	public List<String> generateOrderBy() {

		List<String> ret = new ArrayList<String>(1);

		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);

		return ret;

	}

	@Override
	public String generateWhere() {
		return DOCUMENT_POSITION_IN_SAMPLE  + "> 0";
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

		double lastSampledDocument = NUMBER_OF_SAMPLED_DOCUMENTS;

		List<Tuple> tuples = new ArrayList<Tuple>();

		boolean needlast = false;

		for (int i = 0; i < useful.size(); i++) {

			if (i==lastSampledDocument){

				ret.addPair(Pair.getPair(lastSampledDocument, calculateEntropy(tuples,attribute)));

				lastSampledDocument += NUMBER_OF_SAMPLED_DOCUMENTS;

				needlast = false;
			} else {
				needlast = true;
			}

			if (idtuplesTable.containsKey(useful.get(i)))
				tuples.addAll(idtuplesTable.get(useful.get(i)));
			else{
				System.out.println("In EntropyByAddeDocument does not contain tuple: " + sample.getDatabase().getId() + " - " + sample.getSampleConfiguration().getId()  + " - " + sample.getVersionSeedPos() + "-" + sample.getVersionSeedNeg() + " - " + useful.get(i).getDatabase().getId() + " - " + useful.get(i).getId());
			}
		}

		if (needlast)
			ret.addPair(Pair.getPair(lastSampledDocument, calculateEntropy(tuples,attribute)));

		return ret;

	}

	private double calculateEntropy(List<Tuple> tuples, String attribute) {

		if (attribute.isEmpty())
			return EntropyCalculator.calculateShannonEntropy(tuples);
		else{
			List<String> filtered = filterByAttribute(tuples,attribute);
			return EntropyCalculator.calculateShannonEntropy(filtered);
		}
			

	}

	private List<String> filterByAttribute(List<Tuple> tuples,
			String attributeToKeep) {
		
		List<String> ret = new ArrayList<String>(tuples.size());
		
		for (int i = 0; i < tuples.size(); i++) {
			
			ret.add(tuples.get(i).getFieldValue(attributeToKeep));
			
		}
		
		return ret;
	}

	private Hashtable<Document, ArrayList<Tuple>> getTuplesTable(
			String matchingTuplesFile,Database db) throws IOException {

		Hashtable<Document, ArrayList<Tuple>> idtuplesTable =  getTuplesTable().get(matchingTuplesFile);

		if (idtuplesTable == null){
			idtuplesTable = TuplesLoader.loadDocumenttuplesTuple(db,matchingTuplesFile);
			getTuplesTable().put(matchingTuplesFile, idtuplesTable);
		}

		return idtuplesTable;
	}

	private Hashtable<String,Hashtable<Document, ArrayList<Tuple>>> getTuplesTable() {

		if (tuplesTable == null){
			tuplesTable = new Hashtable<String,Hashtable<Document, ArrayList<Tuple>>>();
		}
		return tuplesTable;
	}

	@Override
	public String getTitle() {
		return "Entropy of collected Sample";
	}

	@Override
	public String getAxisXTitle() {
		return "Sample Size";
	}

	@Override
	public String getAxisYTitle() {
		return "Entropy";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}

}
