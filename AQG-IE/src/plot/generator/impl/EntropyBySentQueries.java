package plot.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class EntropyBySentQueries extends SampleGenerationSeriesGenerator {
	
	private Map<Double, Set<Double>> queryGeneratedTable;
	private persistentWriter pW;
	private EntropyEnum which;

	public EntropyBySentQueries(persistentWriter pW, EntropyEnum any) {
		this.pW = pW;
		this.which = any;
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
		
		ret.add(SeriesGenerator.QUERY_GENERATED);
		
		ret.add(SeriesGenerator.QUERY_ROUND);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION_IN_SAMPLE);
		
		return ret;
		
	}

	@Override
	public List<String> generateOrderBy() {
		
		List<String> ret = new ArrayList<String>(1);
		
		ret.add(SeriesGenerator.DOCUMENT_POSITION);
		ret.add(SeriesGenerator.DOCUMENT_IN_QUERY_POSITION);
		return ret;
		
	}

	@Override
	protected Series generateSeries(List<Hashtable<String, Double>> list,
			Integer execution, double normalizeXValue, double normalizeYValue) {
		
		getQueryGeneratedTable().clear();
		
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
		
		Series ret = new Series(execution.toString());
		
		double issuedQuery = 0;
		
		List<Tuple> tuples = new ArrayList<Tuple>();
		
		double lastIssuedQuery = NUMBER_OF_ISSUED_QUERIES;
		
		double added = 0;
				
		for (Hashtable<String, Double> hashtable : list) {
			
			if (!hasProcessed(hashtable.get(QUERY_GENERATED),hashtable.get(QUERY_ROUND))){
				issuedQuery++;
				getQueryGenerated(hashtable.get(QUERY_GENERATED)).add(hashtable.get(QUERY_ROUND));
			}
						
			if (issuedQuery > lastIssuedQuery){
				
				ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, calculateEntropy(tuples,attribute)));
								
				lastIssuedQuery += NUMBER_OF_ISSUED_QUERIES;
				
			}
			
			double aux = hashtable.get(DOCUMENT_POSITION_IN_SAMPLE);
			
			if (aux > added){
				
				for (int j = (int)added; j < (int) aux; j++) {
					
					if (useful.isEmpty()){
						System.out.println("Empty useful document " + sample.getDatabase().getId());
					} else{

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
		
		ret.addPair(Pair.getPair(lastIssuedQuery/normalizeXValue, calculateEntropy(tuples,attribute)));
		
//		return ret.normalize(normalizeXValue,normalizeYValue).segmentate(NUMBER_OF_ISSUED_QUERIES);
		
		return ret;
		
	}

	private boolean hasProcessed(Double queryGenerated, Double round) {
		
		//return getQueryGenerated(queryGenerated).contains(round); XXX this counts for rounds
		
		return !getQueryGenerated(queryGenerated).isEmpty();
		
	}

	private Set<Double> getQueryGenerated(Double queryGenerated) {
		
		Set<Double> ret = getQueryGeneratedTable().get(queryGenerated);
		
		if (ret == null){
			ret = new HashSet<Double>();
			getQueryGeneratedTable().put(queryGenerated,ret);
		}
		
		return ret;
	}

	private Map<Double,Set<Double>> getQueryGeneratedTable() {
		
		if (queryGeneratedTable == null){
			queryGeneratedTable = new HashMap<Double, Set<Double>>();
		}
		return queryGeneratedTable;
	}

	@Override
	public String getTitle() {
		return "Entropy by sent queries";
	}

	@Override
	public String getAxisXTitle() {
		return "Number of queries Sent";
	}

	@Override
	public String getAxisYTitle() {
		return "Entropy";
	}

	@Override
	protected Series invokeAverage(Series ret) {
		return ret.averageSeries();
//		return ret.independentAverage();
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
