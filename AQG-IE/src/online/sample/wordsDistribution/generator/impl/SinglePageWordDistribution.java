package online.sample.wordsDistribution.generator.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gdata.util.common.html.HtmlToText;

import opennlp.tools.tokenize.TokenizerME;
import online.sample.wordsDistribution.generator.WordsDistributionGenerator;

public class SinglePageWordDistribution extends WordsDistributionGenerator{

	private HashMap<String, Integer> map;
	private File file;

	public SinglePageWordDistribution(TokenizerME tokenizer) {
		super(tokenizer);
	}

	@Override
	public Map<String, Integer> getWords(int i) {
		return map;
	}

	@Override
	public String getOutPutName(int i) {
		
		return FilenameUtils.removeExtension(file.getName());
	}

	@Override
	public int generatedDistributions() {
		
		return 1;
		
	}

	@Override
	public void generateWords(File file) {
		
		this.file = file;
		
		map = new HashMap<String, Integer>();
		
		String plainText = toPlainText(file);
		
		String[] tokens = tokenize(plainText);
		
		for (String token : tokens) {
			
			updateMap(token,map);
			
		}
		
		
	}

	@Override
	public String getParentFolder(File file) {
		return null;
	}


}
