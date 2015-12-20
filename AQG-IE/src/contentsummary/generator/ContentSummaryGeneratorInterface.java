package contentsummary.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;



public abstract class ContentSummaryGeneratorInterface {

	
	public static final String CONTENT_SUMMARY_SEPARATOR = "~";
	
	private HashSet<String> stopWords;
	
	private Hashtable<String, Long> contentSummary;
	
	protected void newContentSummary(){
		
		Runtime r = Runtime.getRuntime();
		r.gc();

		
		if (contentSummary!=null)
			contentSummary.clear();
		
		contentSummary = new Hashtable<String, Long>();
		
	}
	
	protected Hashtable<String, Long> getContentSummary(){
		
		return contentSummary;
		
	}
	
	public ContentSummaryGeneratorInterface(String swfile) {
		if (stopWords == null)
			readStopWords(swfile);
		else
			this.stopWords = null;
	}
	
	public abstract void generateContentSummary(String dbfolder,String outputFile, boolean lowercase, boolean documentLevel, boolean stemmed);

	
	private void readStopWords(String swfile) {
		
		stopWords = new HashSet<String>();
		
		BufferedReader br;
		try {
			
			br = new BufferedReader(new FileReader(new File(swfile)));
			
			String line;
			
			while ((line=br.readLine())!=null){
				
				stopWords.add(line);
				
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	protected HashSet<String> getStopWords(){
		return stopWords;
	}

	protected void addWordsToSummary(String[] words, boolean unique) {
		
		if (unique){
			
			int i = 0;
			
			while (i < words.length){
				
				updateContentSummary(words[i++], 1);
				
			}
			
			return;
			
		}
		
		Arrays.sort(words);
		
		int i=0;
		
		int freq = 0;
		
		String actualWord = words[0].toLowerCase(); 
		
		while (i < words.length){
			
			if (actualWord.equals(words[i].toLowerCase())){
				
				freq++;
			
			}
			else {
				
				updateContentSummary(actualWord,freq);
				
				actualWord = words[i].toLowerCase();
				
				freq = 1;
				
			}
			
			i++;
		}
		
		updateContentSummary(actualWord, freq);
		
	}

	protected void updateContentSummary(String actualWord, long l) {
		
		if (getStopWords()!=null){
			if (getStopWords().contains(actualWord))
				return;
		}
		
		Long actualFreq = contentSummary.get(actualWord);
		
		if (actualFreq == null){
			actualFreq = new Long(0);
		}
		
		long newValue = actualFreq.longValue() + l;
		
		contentSummary.put(actualWord, newValue);
		
	}

	protected void writeOutPutFile(String outputFile) {
		
		Long frequency;
		String word;
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
			
			for (Enumeration<String> e = getContentSummary().keys();e.hasMoreElements();){
				
				word = e.nextElement();
				
				frequency = getContentSummary().get(word);
				
				bw.write(generateLine(word,frequency));
				
			}
			
			bw.close();
		
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		
	}

	public static String generateLine(String word, Long frequency) {
		
		return word + CONTENT_SUMMARY_SEPARATOR + frequency + "\n";
		
	}
	
}
