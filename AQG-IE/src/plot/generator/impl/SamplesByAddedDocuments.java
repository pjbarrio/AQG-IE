package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.data.Pair;
import plot.data.Series;
import plot.generator.SampleGenerationSeriesGenerator;
import plot.generator.SeriesGenerator;

public class SamplesByAddedDocuments extends SampleGenerationSeriesGenerator {

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

		List<String> ret = new ArrayList<String>(2);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION);
		ret.add(SeriesGenerator.DOCUMENT_IN_QUERY_POSITION);
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		Series ret = new Series(execution.toString());
		
		double addedDocument = 0;
		
		double lastAddedDocument = NUMBER_OF_ADDED_DOCUMENTS;
		
		for (Hashtable<String, Double> hashtable : list) {
			
			addedDocument = Math.max(addedDocument,hashtable.get(DOCUMENT_POSITION_IN_SAMPLE));
						
			if (addedDocument == lastAddedDocument){
				
				ret.addPair(Pair.getPair((lastAddedDocument)/normalizeXValue, 1.0));
								
				lastAddedDocument += NUMBER_OF_ADDED_DOCUMENTS;
				
			}
			
		}
		
		//not sure if I should add this.
		
		if (addedDocument == lastAddedDocument)
			ret.addPair(Pair.getPair((lastAddedDocument)/normalizeXValue, 1.0));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}

	@Override
	public String getTitle() {
		return "Samples by Added Documents";
	}

	@Override
	public String getAxisXTitle() {
		return "Added documents";
	}

	@Override
	public String getAxisYTitle() {
		return "Number of Samples";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.numberOfSeries();
	}

}
