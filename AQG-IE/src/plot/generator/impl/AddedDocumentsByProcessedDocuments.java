package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;

public class AddedDocumentsByProcessedDocuments extends
		SampleGenerationSeriesGenerator {

	
	
	private String xNormalization;
	private String yNormalization;
	private double value;

	public AddedDocumentsByProcessedDocuments(String yNormalization,
			String xNormalization, double value) {
		this.xNormalization = xNormalization;
		this.yNormalization = yNormalization;
		this.value = value;
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
		
		double processedDocument = 1/normalizeXValue;
		
		double lastProcessedDocument = value;
		
		double added = 0;
				
		for (Hashtable<String, Double> hashtable : list) {
			
			processedDocument = Math.max(processedDocument,hashtable.get(DOCUMENT_POSITION)/normalizeXValue);
						
			while ((processedDocument > lastProcessedDocument) && (processedDocument <= lastProcessedDocument + value)){ //while?
				
				ret.addPair(Pair.getPair(lastProcessedDocument, added/normalizeYValue));
								
				lastProcessedDocument += value;
				
			}
			
			double aux = hashtable.get(DOCUMENT_POSITION_IN_SAMPLE);
			
			if (aux > added){
				added = aux;
			}
			
		}
		
		ret.addPair(Pair.getPair(lastProcessedDocument, added/normalizeYValue));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}

	@Override
	public String getTitle() {
		return "Added Documents by Processed Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Processed Documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Added Documents";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
	}

}
