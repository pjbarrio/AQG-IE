package online.sample.wordsDistribution.generator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gdata.util.common.html.HtmlToText;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import opennlp.tools.tokenize.TokenizerME;

public abstract class WordsDistributionGenerator {

	private TokenizerME tokenizer;

	private ContentExtractor ce = new TikaContentExtractor();
	
	public WordsDistributionGenerator(TokenizerME tokenizer) {
		this.tokenizer = tokenizer;
	}

	public abstract Map<String, Integer> getWords(int i);

	protected String toPlainText(File file) {
		try {
			
			return ce.extractContent(FileUtils.readFileToString(file));
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	protected String[] tokenize(String plainText) {
		return tokenizer.tokenize(plainText);
	}

	public abstract String getOutPutName(int i);

	public abstract int generatedDistributions();

	public abstract void generateWords(File file);
	
	protected static void updateMap(String token, Map<String, Integer> map) {
		
		Integer freq = map.get(token);
		
		if (freq == null){
			freq = 0;
		}
		
		map.put(token, freq+1);
	}

	public abstract String getParentFolder(File file);
	
}
