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

public class UniqueTuplesByProcessedDocuments extends
		SampleGenerationSeriesGenerator {

	
	
	private String xNormalization;
	private String yNormalization;
	private double value;
	private persistentWriter pW;
	private EntropyEnum which;

	public UniqueTuplesByProcessedDocuments(persistentWriter pW, EntropyEnum which, String yNormalization,
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
		
		Map<Document,List<String>> idattsTable = new HashMap<Document,List<String>>();

		if (attribute != ""){


			for(Entry<Document,List<Tuple>> entry : idtuplesTable.entrySet()){
				
				List<String> aux = new ArrayList<String>();
				
				for (int i = 0; i < entry.getValue().size(); i++) {
	

					String atb = entry.getValue().get(i).getFieldValue(attribute);

					if (atb != null)

	       		        	 	aux.add(atb.toLowerCase());

               			 }
				
				idattsTable.put(entry.getKey(),aux);

			}	
		
		}

		double processedDocument = 1/normalizeXValue;
		
		double lastProcessedDocument = value;
		
		Set<Tuple> tuples = new HashSet<Tuple>();

		Set<String> atts = new HashSet<String>();
		
		double added = 0;
		
		for (Hashtable<String, Double> hashtable : list) {
			
			processedDocument = Math.max(processedDocument,hashtable.get(DOCUMENT_POSITION)/normalizeXValue);
						
			while ((processedDocument > lastProcessedDocument) && (processedDocument <= lastProcessedDocument + value)){ //while?
				
				ret.addPair(Pair.getPair(lastProcessedDocument, calculateEntropy(tuples,atts,attribute)));
								
				lastProcessedDocument += value;
				
			}
			
			double aux = hashtable.get(DOCUMENT_POSITION_IN_SAMPLE);
			
			if (aux > added){
				
				if (useful.isEmpty()){

					System.out.println("Empty sample: " + sample.getDatabase().getId());

				} else {
					for (int j = (int)added; j < (int) aux; j++) {
					
						if (idtuplesTable.containsKey(useful.get(j))){
							tuples.addAll(idtuplesTable.get(useful.get(j)));
							if (attribute!="")
								atts.addAll(idattsTable.get(useful.get(j)));
						}
						else{
							System.out.println("In EntropyByAddeDocument does not contain tuple: " + sample.getDatabase().getId() + " - " + sample.getSampleConfiguration().getId()  + " - " + sample.getVersionSeedPos() + "-" + sample.getVersionSeedNeg() + " - " + useful.get(j).getDatabase().getId() + " - " + useful.get(j).getId());
						}
										
					}
				}
								
				added = aux;
				
				
				
			}
			
		}
		
		ret.addPair(Pair.getPair(lastProcessedDocument, calculateEntropy(tuples,atts,attribute)));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}

	@Override
	public String getTitle() {
		return "Unique Tuples by Processed Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Processed Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Unique Tuples";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}

	private double calculateEntropy(Set<Tuple> tuples, Set<String> attributes, String attribute) {

		if (attribute.isEmpty())
			return tuples.size();
		else{
			return attributes.size();
		}
			

	}
	
}
