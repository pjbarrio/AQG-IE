package execution.trunk.chart.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.persistence.persistentWriter;

import execution.trunk.chart.SampleIdHandler;
import execution.trunk.chart.classification.filter.Filter;
import execution.trunk.chart.classification.filter.IntervalFilter;
import exploration.model.enumerations.ClassificationClusterEnum;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.RelationshipTypeEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;

public class ClassificationFilter {

	private Map<ClassificationClusterEnum, Filter> map;
	private Map<Integer, Set<Double>> dbValues;
	private Map<Integer, Double> clusterValues;

	//This one allows a database to belong to many different categories.
	
	public ClassificationFilter(persistentWriter pW, SimilarityFunctionEnum similarity,
			ClusterFunctionEnum cluster, RelationshipTypeEnum relation, Filter ...filters) {
		
		map = new HashMap<ClassificationClusterEnum,Filter>();
		
		for (int i = 0; i < filters.length; i++) {
			
			map.put(filters[i].getClassification(),filters[i]);
			
		}
		
		dbValues = pW.getDatabaseClusterValues(SampleIdHandler.getClusterFunctionId(cluster),SampleIdHandler.getSimilarityFunctionId(similarity), SampleIdHandler.getRelationshipTypeId(relation));
		
		clusterValues = pW.getClusterValues(SampleIdHandler.getSimilarityFunctionId(similarity),SampleIdHandler.getRelationshipTypeId(relation));
		
	}

	public List<Integer> filter(ClassificationClusterEnum classification,
			List<Integer> databases) {
		
		return filter(map.get(classification),databases);
		
	}

	public final List<Integer> filter(Filter filter, List<Integer> databases) {
		
		List<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < databases.size(); i++) {
			
			Set<Double> values = dbValues.get(databases.get(i));
			
			if (filter.accepts(values)){
				ret.add(databases.get(i));
			}
			
		}
		
		return ret;
	}

	public List<Integer> filterClusters(
			ClassificationClusterEnum classification,
			List<Integer> clusters) {
		
		Filter filter = map.get(classification);
		
		List<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < clusters.size(); i++) {
			
			Double value = clusterValues.get(clusters.get(i));
			
			if (filter.accepts(value)){
				ret.add(clusters.get(i));
			}
			
		}
		
		return ret;
		
	}


}
