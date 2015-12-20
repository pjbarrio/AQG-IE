package online.sample.wordsDistribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.word.extraction.WordValidator;

public class WordsDistributionLoader {

	private Map<String, Long> global = new HashMap<String, Long>();
	
	private Map<String, Long> globalOccurrence = new HashMap<String, Long>(); //the number of documents where a word appears.
	
	private Map<Integer,Map<String,Long>> local = new HashMap<Integer,Map<String, Long>>();
	
	public void load(File[] files) throws IOException {
		
		for (int i = 0; i < files.length; i++) {
			
			Map<String,Long> loc = new HashMap<String, Long>();
			
			if (files[i].isFile()){
			
				local.put(Integer.valueOf(files[i].getName()), loc);
				
				loadFile(files[i],loc,global,globalOccurrence);

			}
			
		}
		
		
		
	}

	public static Map<String, Long> loadFile(File file){
		
		return loadFile(file, 0);
		
	}
	
	public static Map<String, Long> loadFile(File file, int min_supp){
		
		WordValidator wv = new WordValidator();
		
		Map<String,Long> ret = new HashMap<String, Long>();
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			
			while ((line = br.readLine())!=null){
				
				String[] spl = line.split(WordsDistribution.SEPARATOR);
				
				String word = spl[0];
				
				if (!wv.isValid(word)){
					continue;
				}
				
				Long freq = Long.valueOf(spl[1]);
				
				if (freq >= min_supp)
					ret.put(word, freq);
				
				
			}
			
			br.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
		
	}
	
	private void loadFile(File file,
			Map<String, Long> local, Map<String, Long> global, Map<String, Long> globalOccurrence) throws IOException {
		
		WordValidator wv = new WordValidator();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		
		while ((line = br.readLine())!=null){
			
			String[] spl = line.split(WordsDistribution.SEPARATOR);
			
			String word = spl[0];
			
			if (!wv.isValid(word)){
				continue;
			}
			
			Long freq = Long.valueOf(spl[1]);
			
			local.put(word, freq);
			
			updateMap(global,word,freq);
			
			updateMap(globalOccurrence,word,1L);
			
		}
		
		br.close();
	}

	private void updateMap(Map<String, Long> map, String word,
			Long newfreq) {
		Long freq = map.get(word);
		if(freq==null){
			freq = 0L;
		}
		map.put(word, freq+newfreq);
	}
	
	public Set<Integer> getFiles(){
		return local.keySet();
	}
	
	public Map<String,Long> getWordMap(Integer index){
		return local.get(index);
	}
	
	public Long getGlobalValue(String word){
		return global.get(word);
	}
	
	public Set<String> getWords(){
		return global.keySet();
	}
	
	public Long getDocumentFrequency(String word){
		return globalOccurrence.get(word);
	}

	public static List<String> loadWordsOnly(File file,String separator,int index) throws IOException {
		
		WordValidator wv = new WordValidator();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		
		List<String> list = new ArrayList<String>();
		
		while ((line = br.readLine())!=null){
			
			String[] spl = line.split(separator);
			
			String word = spl[index];
			
			if (!wv.isValid(word)){
				continue;
			}
			
			list.add(word);
			
		}
		
		br.close();
		
		return list;
	}
	
	public static List<String> loadWordsOnly(File file) throws IOException{
		return loadWordsOnly(file, WordsDistribution.SEPARATOR,0);
	}
}
