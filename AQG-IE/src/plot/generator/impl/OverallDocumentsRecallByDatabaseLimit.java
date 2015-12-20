package plot.generator.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import plot.generator.OverallSeriesGenerator;

public class OverallDocumentsRecallByDatabaseLimit extends OverallSeriesGenerator {

	public OverallDocumentsRecallByDatabaseLimit(List<Double> parameters) {
		super(parameters);
		
	}

	@Override
	protected String getNormalizedYAttribute() {
		return USEFUL_TOTAL_DOCUMENTS;
	}

	@Override
	public List<String> generateAttributes() {
		List<String> arr = new ArrayList<String>();
		
		arr.add(USEFUL_TUPLES);
		
		return arr;
	}

	@Override
	public List<String> generateOrderBy() {
		List<String> arr = new ArrayList<String>();
		
		arr.add(PROCESSED_DOCUMENT_POSITION);
		
		return arr;
	}

	@Override
	public String getTitle() {
		return "Documents Recall by Number of Documents to Retrieve By Query";
	}

	@Override
	public String getAxisXTitle() {
		return "Number of Documents to Retrieve By Query";
	}

	@Override
	public String getAxisYTitle() {
		return "Documents Recall";
	}

	@Override
	protected Double getOverallValue(List<Hashtable<String, Double>> list,
			Integer execution, double total) {
		
		double sum = 0;
		for (Hashtable<String, Double> hashtable : list) {
			double useful = hashtable.get(USEFUL_TUPLES);
			if (useful > 0)
				sum++;
		}
		return sum/total;
	}

	@Override
	protected String getNormalizedXAttribute() {
		
		return NO_NORMALIZATION;
	}

}
