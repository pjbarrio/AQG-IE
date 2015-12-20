package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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

public class EntropyByProcessedDocuments extends
		SampleGenerationSeriesGenerator {

	
	
	private String xNormalization;
	private String yNormalization;
	private double value;
	private persistentWriter pW;
	private EntropyEnum which;

	public EntropyByProcessedDocuments(persistentWriter pW, EntropyEnum which, String yNormalization,
			String xNormalization, double value) {
		this.xNormalization = xNormalization;
		this.yNormalization = yNormalization;
		this.value = value;
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
	public List<String> generateAttributes() {
		
		List<String> ret = new ArrayList<String>(2);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION);
				
		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);
		
		return ret;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>(2);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION);
		
		ret.add(SeriesGenerator.DOCUMENT_IN_QUERY_POSITION);
		
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		Series ret = new Series(execution.toString());
		
		Sample sample = pW.getSample(execution);
		
		String attribute = "";
		
		if (which != EntropyEnum.ANY){
			if (which == EntropyEnum.LARGE){
				attribute = pW.getLargeAttributeName(execution);
			}else if (which == EntropyEnum.SMALL){
				attribute = pW.getSmallAttributeName(execution);
			}
		}
		
		List<Document> useful = new ArrayList<Document>(pW.getUsefulDocuments(sample, 10000));

		Map<Document,List<Tuple>> idtuplesTable= pW.getSampleTuples(sample);
		
		double processedDocument = 1/normalizeXValue;
		
		double lastProcessedDocument = value;
		
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		double added = 0;
		
		for (Hashtable<String, Double> hashtable : list) {
			
			processedDocument = Math.max(processedDocument,hashtable.get(DOCUMENT_POSITION)/normalizeXValue);
						
			while ((processedDocument > lastProcessedDocument) && (processedDocument <= lastProcessedDocument + value)){ //while?
				
				ret.addPair(Pair.getPair(lastProcessedDocument, calculateEntropy(tuples,attribute)));
								
				lastProcessedDocument += value;
				
			}
			
			double aux = hashtable.get(DOCUMENT_POSITION_IN_SAMPLE);
			
			if (aux > added){
				
				if (useful.isEmpty()){

					System.out.println("Empty sample: " + sample.getDatabase().getId());

				} else {
					for (int j = (int)added; j < (int) aux; j++) {
					
						if (idtuplesTable.containsKey(useful.get(j)))
							tuples.addAll(idtuplesTable.get(useful.get(j)));
						else{
							System.out.println("In EntropyByAddeDocument does not contain tuple: " + sample.getDatabase().getId() + " - " + sample.getSampleConfiguration().getId()  + " - " + sample.getVersionSeedPos() + "-" + sample.getVersionSeedNeg() + " - " + useful.get(j).getDatabase().getId() + " - " + useful.get(j).getId());
						}
										
					}
				}
								
				added = aux;
				
				
				
			}
			
		}
		
		ret.addPair(Pair.getPair(lastProcessedDocument, calculateEntropy(tuples,attribute)));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}

	@Override
	public String getTitle() {
		return "Entropy by Processed Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Processed Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Entropy";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
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
	
}
