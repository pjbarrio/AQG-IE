package execution.trunk.chart.classification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import execution.trunk.chart.SampleChartGeneration;
import execution.trunk.chart.classification.filter.Filter;
import execution.trunk.chart.classification.filter.IntervalFilter;
import exploration.model.enumerations.ClassificationClusterEnum;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.IntervalFilterEnum;
import exploration.model.enumerations.RelationshipTypeEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;

public class ExclusiveClassificationFilter extends ClassificationFilter {

	//Filters are in order. If a lower order filter is matched, we do not care. 
	
	private Filter[] orderedFilter;

	public ExclusiveClassificationFilter(persistentWriter pW,
			SimilarityFunctionEnum similarity, ClusterFunctionEnum cluster, RelationshipTypeEnum relation,
			Filter... filters) {
		super(pW, similarity, cluster, relation, filters);
		this.orderedFilter = filters;
	}

	public List<Integer> filter(ClassificationClusterEnum classification,
			List<Integer> dbs) {

		List<Integer> databases = new ArrayList<Integer>(dbs);
		
		int i = 0;
		
		while (!orderedFilter[i].getClassification().equals(classification)){ //Check higher ranked filters do not filter.
			
			List<Integer> list = filter(orderedFilter[i], databases);
			
			databases.removeAll(list);
			
			i++;
			
		}
		
		return filter(orderedFilter[i], databases);
		
	}
	
}
