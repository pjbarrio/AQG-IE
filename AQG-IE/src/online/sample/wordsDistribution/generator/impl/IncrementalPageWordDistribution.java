package online.sample.wordsDistribution.generator.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import opennlp.tools.tokenize.TokenizerME;
import online.sample.wordsDistribution.generator.WordsDistributionGenerator;

public class IncrementalPageWordDistribution extends WordsDistributionGenerator {

	private File dbFolder;
	private int results;
	private HashMap<Integer, Map<String, Integer>> resultsMap;

	public IncrementalPageWordDistribution(TokenizerME tokenizer) {
		super(tokenizer);
	}

	@Override
	public Map<String, Integer> getWords(int i) {
		return resultsMap.get(i);
	}

	@Override
	public String getOutPutName(int i) {
		
		return dbFolder.getName() + "/0-" + i + ".txt";
		
	}

	@Override
	public int generatedDistributions() {
		return results;
	}

	@Override
	public void generateWords(File file) {

		this.dbFolder = file;
		
		File[] files = dbFolder.listFiles();
		
		this.results = files.length;
		
		this.resultsMap = new HashMap<Integer, Map<String,Integer>>();
		
		Map<String,Integer> map = new HashMap<String, Integer>();
		
		for (int i = 0; i < files.length; i++) {
			
			System.out.println("Size: " + i);
			
			map = new HashMap<String,Integer>(map);
			
			resultsMap.put(i,map);
			
			String plainText = toPlainText(files[i]);
			
			String[] tokens = tokenize(plainText);
			
			for (String token : tokens) {
				
				updateMap(token,map);
				
			}
			
		}
		
	}

	@Override
	public String getParentFolder(File file) {
		return file.getName();
	}

}
