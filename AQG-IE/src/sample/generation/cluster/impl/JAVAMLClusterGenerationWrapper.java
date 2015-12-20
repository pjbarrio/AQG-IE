package sample.generation.cluster.impl;

import init.initialization.similarity.FileSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.reasoner.rulesys.builtins.Table;

import online.sample.wordsDistribution.WordsDistributionLoader;

import external.javaML.InstanceGenerator;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.core.Dataset;

import sample.generation.cluster.ClusterGenerator;

public class JAVAMLClusterGenerationWrapper implements ClusterGenerator {

	private Clusterer clusterer;
	private int level;
	private FileSelector fs;
	private InstanceGenerator ig;
	private WordsDistributionLoader wl;
	private Map<String, Integer> indexTable;
	private Map<Integer, String> revTable;

	public JAVAMLClusterGenerationWrapper(Clusterer clusterer, int level, FileSelector fs, Map<String,Integer> indexTable){
		
		this.clusterer = clusterer;
		this.level = level;
		this.fs = fs;
		ig = new InstanceGenerator();
		wl = new WordsDistributionLoader();
		this.indexTable = indexTable;
	
		this.revTable = new HashMap<Integer,String>();
		
	}
	
	@Override
	public void addDatabase(String dbname) {
		
		int index = indexTable.get(dbname);
		
		revTable.put(index, dbname);
		
		ig.addInstance(wl.loadFile(fs.getWordFrequency(index, level),10),index);
		
	}

	@Override
	public Map<String, Set<String>> getClusters() {
		
		Map<String,Set<String>> ret = new HashMap<String, Set<String>>();
		
		Dataset d = ig.getDataset();
		
		Dataset[] cl = clusterer.cluster(d);
		
		for (int i = 0; i < cl.length; i++) {
		
			Set<String> dbs = new HashSet<String>();
			
			for (int j = 0; j < cl[i].size(); j++) {
				
				dbs.add(revTable.get(cl[i].get(j).classValue()));
				
			}
			
			ret.put(Integer.toString(i), dbs);
			
		}
		
		return ret;
	
	}



}
