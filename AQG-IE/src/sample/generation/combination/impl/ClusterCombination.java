package sample.generation.combination.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import exploration.model.Database;
import exploration.model.enumerations.ClusterFunctionEnum;

import sample.generation.combination.CombinationGenerator;
import utils.id.DatabaseIndexHandler;
import utils.persistence.databaseWriter;

public class ClusterCombination extends CombinationGenerator {

	private File[] clusters;
	private String name;

	public ClusterCombination(databaseWriter pW, File file, String name) {
		super(pW);
		this.clusters = file.listFiles();
		this.name = name;
	}

	@Override
	public List<Database> getCombination(int i) {
		try {
			List<String> files = FileUtils.readLines(clusters[i]);
			List<Database> ret = new ArrayList<Database>(files.size());
			for (int j = 0; j < files.size(); j++) {
	
				ret.add(pW.getDatabaseByName(files.get(j)));
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int numberOfCombinations(){
		return clusters.length;
	}

	@Override
	public String getDatabaseName(int i) {
		return clusters[i].getName() + "-" + name;
	}

	@Override
	public String getSampleType(int i) {
		return "ClusterMixed-"+name;
	}

	public static void main(String[] args) throws IOException {
		
		String name = "Category";
		
		Map<String, Integer> index = DatabaseIndexHandler.loadDatabaseIndex(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt"));
		
		CombinationGenerator cg = new ClusterCombination(null,new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/SampleGeneration/Clusters/"+name+"/smalltraining"),name);
		
		List<String> a = Arrays.asList(new String[]{"28","217","231","321","452","739","790","1174","1367","1387","1482","1769","2086","2098","2167","2175","2694","2746"});
	
		for (int i = 0; i < cg.numberOfCombinations(); i++) {
		
			Set<String> aset = new HashSet<String>(a);
			
//			List<String> r = Arrays.asList(cg.getCombination(i));
			
//			aset.retainAll(r);
			
			System.out.println(aset.toString());
			
		}
		
	}

	@Override
	public int isGlobal() {
		return 0;
	}

	@Override
	public int isCluster() {
		return 1;
	}

	@Override
	public ClusterFunctionEnum getClusteredFunction() {
		return ClusterFunctionEnum.CLASSIFICATION;
	}
	
}
