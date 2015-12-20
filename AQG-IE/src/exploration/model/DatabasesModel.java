package exploration.model;


import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import utils.dispatcher.MyComparator;
import exploration.model.clusterfunction.ClusterFunction;
import exploration.model.enumerations.ClusterFunctionEnum;
import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.source.similarity.SimilarityFunction;

public class DatabasesModel {
	
	private List<Database> databases;
	private Hashtable<Integer,Database> index;
	private Hashtable<String,Hashtable<ClusterFunctionEnum,Hashtable<Integer,HashSet<Integer>>>> groups;
	private Hashtable<SimilarityFunctionEnum,Hashtable<Integer,TreeSet<Integer>>> similarities;
	private Hashtable<String,Hashtable<SimilarityFunctionEnum,Hashtable<Integer,Hashtable<Integer,Double>>>> similaritiesTable;
	
	public DatabasesModel(List<Database> databases) {
		this.databases = databases;
		
		groups = new Hashtable<String,Hashtable<ClusterFunctionEnum, Hashtable<Integer,HashSet<Integer>>>>();

		similarities = new Hashtable<SimilarityFunctionEnum, Hashtable<Integer,TreeSet<Integer>>>();
		
		similaritiesTable = new Hashtable<String,Hashtable<SimilarityFunctionEnum, Hashtable<Integer,Hashtable<Integer,Double>>>>();
		
		createIndex();
	}

	private void createIndex() {
		
		index = new Hashtable<Integer, Database>();
		
		for (Database database : databases) {
			
			index.put(database.getId(), database);
			
		}
		
	}

	public boolean match(Database sampledDatabase, Database processableDatabase, 
			SimilarityFunction similarityFunction, int maxOrder, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		TreeSet<Integer> vector = getSimilarityVector(processableDatabase.getId(), similarityFunction.getEnum(), version_pos_seed, version_neg_seed, idWorkload);
		
		int i = 1;
		
		for (Iterator<Integer> iterator = vector.descendingIterator(); iterator.hasNext();) {
			
			if (i>maxOrder){
				return false;
			}
			
			Integer integer = iterator.next();
			
			if (sampledDatabase.getId() == integer)
				return true;
			
			i++;
		}
		
		return false;
		
	}

	public boolean belongs(Database processableDatabase,
			Database sampledDatabase, ClusterFunction clusterFunction, int version_pos_seed, int version_neg_seed, int idWorkload) {
		return getGroup(sampledDatabase.getId(), clusterFunction.getEnum(), version_pos_seed, version_neg_seed, idWorkload).contains(processableDatabase.getId());
	}

	public boolean match(Database processableDatabase,
			Database clusterDatabase, ClusterFunction clusterFunction,
			int maxOrder, int version_pos_seed, int version_neg_seed, int idWorkload) {
	
		// TODO has to be close to the centroid (the closest or the maxOrder closest)
		return false;
	
	}

	public void addSimilarity(int idDatabase1, int idDatabase2, SimilarityFunctionEnum similarity, double value, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		getSimilaritiesTable(idDatabase1,similarity,version_pos_seed, version_neg_seed,idWorkload).put(idDatabase2, value);
		
		getSimilarityVector(idDatabase1,similarity,version_pos_seed, version_neg_seed,idWorkload).add(idDatabase2);
		
	}

	private TreeSet<Integer> getSimilarityVector(int idDatabase1,
			SimilarityFunctionEnum similarity, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		Hashtable<Integer, TreeSet<Integer>> aux = getSimilarity(similarity);
		
		TreeSet<Integer> ret = aux.get(idDatabase1);
		
		if (ret == null){
			
			ret = new TreeSet<Integer>(new MyComparator<Integer,Double>(getSimilaritiesTable(idDatabase1,similarity,version_pos_seed,version_neg_seed,idWorkload)));
			
			aux.put(idDatabase1,ret);
			
		}
		
		return ret;
		
	}

	private Hashtable<Integer, Double> getSimilaritiesTable(int idDatabase1,
			SimilarityFunctionEnum similarity, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		Hashtable<Integer,Hashtable<Integer,Double>> dbsimTable = getSimilarityTable(similarity,version_pos_seed, version_neg_seed,idWorkload);
		
		Hashtable<Integer, Double> ret = dbsimTable.get(idDatabase1);
		
		if (ret == null){
			
			ret = new Hashtable<Integer, Double>();
			
			dbsimTable.put(idDatabase1, ret);
			
		}
		
		return ret;
	}

	private Hashtable<Integer, Hashtable<Integer, Double>> getSimilarityTable(
			SimilarityFunctionEnum similarity, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		Hashtable<Integer, Hashtable<Integer, Double>> ret = getTableForSampleWorkload(version_pos_seed, version_neg_seed,idWorkload).get(similarity);
		
		if (ret == null){
			
			ret = new Hashtable<Integer, Hashtable<Integer,Double>>();
			
			getTableForSampleWorkload(version_pos_seed,version_neg_seed,idWorkload).put(similarity, ret);
			
		}
		
		return ret;
		
	}

	private Hashtable<SimilarityFunctionEnum, Hashtable<Integer,Hashtable<Integer,Double>>> getTableForSampleWorkload(int version_pos_seed, int version_neg_seed,
			int idWorkload) {
		
		Hashtable<SimilarityFunctionEnum, Hashtable<Integer,Hashtable<Integer,Double>>> ret = similaritiesTable.get(generateIdForSampleAndWorkload(version_pos_seed, version_neg_seed,idWorkload));
		
		if (ret == null){
			
			ret = new Hashtable<SimilarityFunctionEnum, Hashtable<Integer,Hashtable<Integer,Double>>>();
			
			similaritiesTable.put(generateIdForSampleAndWorkload(version_pos_seed,version_neg_seed,idWorkload), ret);
			
		}
		
		return ret;
		
	}

	private String generateIdForSampleAndWorkload(int version_pos_seed, int version_neg_seed,
			int idWorkload) {
		
		return version_pos_seed + "-" + version_neg_seed + "-" + idWorkload;
		
	}

	private Hashtable<Integer, TreeSet<Integer>> getSimilarity(
			SimilarityFunctionEnum similarity) {
		
		Hashtable<Integer,TreeSet<Integer>> ret = similarities.get(similarity);
		
		if (ret == null){
			
			ret = new Hashtable<Integer, TreeSet<Integer>>();
			
			similarities.put(similarity, ret);
			
		}
	
		return ret;
		
	}

	public void addBelongsToGroup(int idGroup, int idDatabase,
			ClusterFunctionEnum valueOf, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		getGroup(idGroup,valueOf,version_pos_seed, version_neg_seed,idWorkload).add(idDatabase);
		
	}

	private HashSet<Integer> getGroup(int idGroup, ClusterFunctionEnum clusterFunction, int version_pos_seed, int version_neg_seed, int idWorkload) {
		
		Hashtable<Integer, HashSet<Integer>> aux = getGroups(clusterFunction, version_pos_seed, version_neg_seed, idWorkload);
	
		HashSet<Integer> ret = aux.get(idGroup);
		
		if (ret == null){
			
			ret = new HashSet<Integer>();
			
			aux.put(idGroup,ret);
			
		}
		
		return ret;
	
	}

	private Hashtable<Integer, HashSet<Integer>> getGroups(ClusterFunctionEnum clusterFunction, int version_pos_seed, int version_neg_seed, int idWorkload){
		
		Hashtable<Integer,HashSet<Integer>> ret = getGroups(version_pos_seed,version_neg_seed,idWorkload).get(clusterFunction);
		
		if (ret == null){
			
			ret = new Hashtable<Integer, HashSet<Integer>>();
			
			getGroups(version_pos_seed,version_neg_seed,idWorkload).put(clusterFunction, ret);
			
		}
	
		return ret;
	
	}

	private Hashtable<ClusterFunctionEnum,Hashtable<Integer,HashSet<Integer>>> getGroups(int version_pos_seed, int version_neg_seed, int idWorkload) {

		Hashtable<ClusterFunctionEnum,Hashtable<Integer,HashSet<Integer>>> ret = groups.get(generateIdForSampleAndWorkload(version_pos_seed, version_neg_seed, idWorkload));
		
		if (ret == null){
			
			ret = new Hashtable<ClusterFunctionEnum, Hashtable<Integer,HashSet<Integer>>>();
			
			groups.put(generateIdForSampleAndWorkload(version_pos_seed,version_neg_seed, idWorkload), ret);
			
		}
		
		return ret;
		
	}


}
