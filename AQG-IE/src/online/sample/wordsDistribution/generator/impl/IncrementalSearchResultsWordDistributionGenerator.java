package online.sample.wordsDistribution.generator.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import opennlp.tools.tokenize.TokenizerME;
import online.sample.wordsDistribution.generator.WordsDistributionGenerator;
import weka.gui.ExtensionFileFilter;

public class IncrementalSearchResultsWordDistributionGenerator extends
		WordsDistributionGenerator {

	private File bySizeFolder;
	private File orderedFolder;
	private int results;
	private String dbName;
	private Map<Integer,Map<String, Integer>> resultMap;

	public IncrementalSearchResultsWordDistributionGenerator(
			TokenizerME tokenizer, File bySizeFolder, File orderedFolder) {
		super(tokenizer);
		this.bySizeFolder = bySizeFolder;
		this.orderedFolder = orderedFolder;
	}

	@Override
	public Map<String, Integer> getWords(int i) {
		return resultMap.get(i);
	}

	@Override
	public String getOutPutName(int i) {
		return dbName + "/0-" + i + ".txt";
	}

	@Override
	public int generatedDistributions() {
		return results;
	}

	@Override
	public void generateWords(File file) {
		
		String name = file.getName(); //dbName
		
		File toProcess = new File(bySizeFolder,name+".list");
		
		try {
			
			List<String> files = FileUtils.readLines(toProcess);
			
			List<File> folders = new ArrayList<File>();
			
			for (String fname : files) {
				
				String fold = FilenameUtils.getBaseName(fname);
				
				folders.add(new File(orderedFolder,fold + "/" + name + "/"));
				
			}
			
			this.results = folders.size();
			
			this.dbName = name;
			
			createMaps(folders);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	private void createMaps(List<File> folders) {
		
		this.resultMap = new HashMap<Integer, Map<String,Integer>>();
		
		Map<String,Integer> map = new HashMap<String, Integer>();
		
		for (int i = 0; i < folders.size(); i++) {
			
			System.out.println("Size: " + i);
			
			map = new HashMap<String,Integer>(map);
			
			resultMap.put(i,map);
			
			File[] results = folders.get(i).listFiles(new ExtensionFileFilter("html", "html file"));			
			
			for (File file : results) {
				
				System.out.println("Processing: " + file);
				
				String plainText = toPlainText(file);
				
				String[] tokens = tokenize(plainText);
				
				for (String token : tokens) {
					
					updateMap(token,map);
					
				}
				
			}
			
		}
		
		
	}

	@Override
	public String getParentFolder(File file) {
		return file.getName();
	}

}
