package sample.generation.global;

import java.util.List;

import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import execution.trunk.chart.SampleChartGeneration;
import execution.trunk.chart.SampleIdHandler;
import execution.trunk.chart.classification.ClassificationFilter;
import execution.trunk.chart.classification.ExclusiveClassificationFilter;
import execution.trunk.chart.classification.filter.IntervalFilter;
import exploration.model.enumerations.ClassificationClusterEnum;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.IntervalFilterEnum;
import exploration.model.enumerations.RelationshipTypeEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;

public class Closeness {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RelationshipTypeEnum[] rels = {RelationshipTypeEnum.PERSONCAREER,RelationshipTypeEnum.NATURALDISASTER,RelationshipTypeEnum.MANMADEDISASTER,RelationshipTypeEnum.PERSONTRAVEL,RelationshipTypeEnum.VOTINGRESULT,RelationshipTypeEnum.INDICTMENTARRESTTRIAL};
		
		int[] idClusters = {2869,2873,2877,2881,2885,2889};
		
		ClusterFunctionEnum clusterFunction = ClusterFunctionEnum.CLOSENESS_CLASSIFICATION;
		
		databaseWriter pW = (databaseWriter)PersistenceImplementation.getWriter();

		for (int i = 0; i < idClusters.length; i++) {
			
			RelationshipTypeEnum rel = rels[i];
			int idCluster = idClusters[i];
			
			List<Integer> databases = SampleChartGeneration.generateListOfDatabases(pW.getSamplableDatabases(56));
			
			ClassificationFilter cf = new ExclusiveClassificationFilter(pW,SimilarityFunctionEnum.MANUAL_CLUSTER, ClusterFunctionEnum.CLASSIFICATION, rel, new IntervalFilter(ClassificationClusterEnum.RELATED,0.75,1,IntervalFilterEnum.G,IntervalFilterEnum.LE), 	new IntervalFilter(ClassificationClusterEnum.SOMEWHAT_RELATED,0.5,0.75,IntervalFilterEnum.G,IntervalFilterEnum.LE),new IntervalFilter(ClassificationClusterEnum.BARELY_RELATED,0.25,0.5,IntervalFilterEnum.G,IntervalFilterEnum.LE),
					new IntervalFilter(ClassificationClusterEnum.NOT_RELATED,0.0,0.25,IntervalFilterEnum.GE,IntervalFilterEnum.LE));
			
			List<Integer> list = cf.filter(ClassificationClusterEnum.RELATED, databases);
			
			write(pW,list,SampleIdHandler.getRelationshipTypeId(rel),idCluster, clusterFunction,ClassificationClusterEnum.RELATED.name(), rel.name(),i+1, 1);
			
			list = cf.filter(ClassificationClusterEnum.SOMEWHAT_RELATED, databases);
			
			write(pW,list,SampleIdHandler.getRelationshipTypeId(rel),idCluster+1, clusterFunction,ClassificationClusterEnum.SOMEWHAT_RELATED.name(), rel.name(),i+1, 0.66);
			
			list = cf.filter(ClassificationClusterEnum.BARELY_RELATED, databases);
			
			write(pW,list,SampleIdHandler.getRelationshipTypeId(rel),idCluster+2, clusterFunction,ClassificationClusterEnum.BARELY_RELATED.name(), rel.name(),i+1, 0.33);
			
			list = cf.filter(ClassificationClusterEnum.NOT_RELATED, databases);
			
			write(pW,list,SampleIdHandler.getRelationshipTypeId(rel),idCluster+3, clusterFunction,ClassificationClusterEnum.NOT_RELATED.name(), rel.name(),i+1, 0);

			
		}
		
		
		
	}

	private static void write(databaseWriter pW, List<Integer> list, int relationshipTypeId,
			int idCluster, ClusterFunctionEnum clusterFunction, String closeness, String relation, int idRelType, double value) {
		
		pW.insertDatabase(idCluster, relation + "_" + closeness, -1, "", "Clustered", 1, 0, "", 0, 1,0);
		
		pW.performStatement("INSERT INTO `AutomaticQueryGeneration`.`RelationClusterSimilarity`(`idCluster`,`idRelationshipType`,`idSimilarity`,`value`)VALUES("+idCluster+","+idRelType+",5,"+value+")");
		
		for (int i = 0; i < list.size(); i++) {
			
			pW.insertClusteredDatabase(idCluster, list.get(i), clusterFunction, -1, -1, relationshipTypeId, -1, -1, -1);
			
		}
		
	}
}
