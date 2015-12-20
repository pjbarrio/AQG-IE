package utils.word.extraction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import exploration.model.Document;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;

import utils.algorithms.Porter;
import utils.persistence.persistentWriter;

public abstract class WordExtractorAbs {

	public static final String SPECIAL_CLASS = "class";
	public static final String SPECIAL_REPLACEMENT = "sslaclass";
	private boolean lw;
	private boolean st;
	private Porter stemmer;
	private boolean unique;
	
	public String[] getWords(Document document, boolean unique, boolean lwcase, boolean stem,persistentWriter pW){
		setLowerCase(lwcase);
		setStemmed(stem);
		setUnique(unique);
		return getWords(document,pW);
	}
	
	public String[] getWords(Document document,persistentWriter pW){
		return processWords(_getWords(document,pW));
	}
	
	private void setUnique(boolean unique) {
		this.unique = unique;
	}

	protected abstract String[] _getWords(Document document,persistentWriter pW);
	
	public WordExtractorAbs(){
		stemmer = new Porter();
	}
	
	protected String processWord(String string) {
		
		String word;

		if (lw){
			word = string.toLowerCase();
		}
		else{
			word = string;
		}
		
		if (st){
			word = stemWord(word);
		}
		
		if (word.toLowerCase().equals(SPECIAL_CLASS)){
			word = SPECIAL_REPLACEMENT;
		}
		
		return word;
		
	}

	protected String stemWord(String word) {
		return stemmer.stripAffixes(word);
	}

	public String[] getWords(String string, boolean unique, boolean lowercase, boolean stemmed){
		setLowerCase(lowercase);
		setStemmed(stemmed);
		setUnique(unique);
		return processWords(_getWords(string));
	}
	
	protected abstract String[] _getWords(String string);

	private void setLowerCase(boolean val){
		this.lw = val;
	}
	
	private void setStemmed(boolean val){
		this.st = val;
	}
	
	protected String[] processWords(String[] words){
		
		ArrayList<String> repwords = new ArrayList<String>();
		
		Set<String> set = new HashSet<String>();		
		
		for (int i = 0; i < words.length; i++) {
			
			if (!words[i].trim().equals("")){
			
				String w = processWord(words[i].trim());
				
				if (!unique || set.add(w))
					repwords.add(w);

			}
				
			
		}
		
		return repwords.toArray(new String[repwords.size()]);
		
	}
	
}
