package contentsummary.reader;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import contentsummary.generator.ContentSummaryGenerator;

public class ContentSummaryReader {
	
	private Hashtable<String, Long> contentSummary;
	
	public ContentSummaryReader(String file){
		
		BufferedReader br;
		try {
			
			br = new BufferedReader(new FileReader(new File(file)));
			
			String line;
			
			contentSummary = new Hashtable<String, Long>();
			
			while ((line=br.readLine())!= null){
				String[] wf = line.split(ContentSummaryGenerator.CONTENT_SUMMARY_SEPARATOR);
				contentSummary.put(wf[0], new Long(wf[1]));
			}
			
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	public Enumeration<String> enumeration(){
		return contentSummary.keys();
	}
	
	public long getFrequency(String word){
		
		Long l = contentSummary.get(word);
		
		if (l==null)
			return 0;
		
		return l.longValue();
	}
	
	public ArrayList<String> getWords(){
		
		ArrayList<String> words = new ArrayList<String>();
		
		for(Enumeration<String> e = contentSummary.keys();e.hasMoreElements();){
			words.add(e.nextElement());
		}
		
		return words;
	}
	
	public long size(){
		
		return contentSummary.size();
		
	}
}
