package sample.generation.cluster.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import sample.generation.cluster.ClusterGenerator;
import utils.id.DatabaseCategoryHandler;
import utils.id.DatabaseIndexHandler;

public class ClassificationBasedClusterGenerator implements ClusterGenerator{

	private Map<String, List<String>> catTable;
	private Map<String,Set<String>> clusters;

	public ClassificationBasedClusterGenerator(File catFile) {
		try {
			catTable = DatabaseCategoryHandler.loadDatabaseCategoryIngerted(catFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		clusters = new HashMap<String, Set<String>>();
	}

	@Override
	public void addDatabase(String dbName) {
		
		List<String> cats = catTable.get(dbName);
		
		if (cats == null){
			System.out.println("Has to be reviewed: " + dbName);
			return;
		}
		for (String cat : cats) {
			
			String rcat = reduce(cat);
			
			Set<String> clu = clusters.remove(rcat);
			
			if (clu == null){
				clu = new HashSet<String>();
			}
			
			clu.add(dbName);
			
			clusters.put(rcat, clu);

		}
		
	}

	private static String reduce(String cat) {

		String rcat = cat.replaceAll("/proj/dbNoBackup/pjbarrio/sites/Directory/Top/", "");
		
		return rcat.substring(0,rcat.indexOf("/"));
	
	}

	@Override
	public Map<String, Set<String>> getClusters() {
		return clusters;
	}
	
}
