package sample.weka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.AbstractLoader;
import weka.core.converters.ArffLoader.ArffReader;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;
import exploration.model.Document;
import exploration.model.DocumentHandle;


public class ArrayListLoader extends AbstractLoader {

	/**
	 * 
	 */
	private Instances instances;
	private Instances m_structure;
	private String arffContent;
	private ArffReader m_ArffReader;
	private BufferedReader m_sourceReader;
//	private RDFWordExtractor UsefulwE;
	
	private HashSet<String> stopWords;
	private boolean stemmed;
	private WordExtractorAbs UsefulwE;
	private WordExtractorAbs GeneralwE;
	private boolean frequency;
	private boolean lowercase;
	private persistentWriter pW;
	
	public ArrayListLoader(persistentWriter pW, Collection<Document> sample,
			Collection<Document> useful, String stopWordsFile, Set<Tuple> tuples, boolean frequency, boolean stemmed, boolean lowercase, boolean createInstances,
			WordExtractorAbs usefulWE, WordExtractorAbs generalWE) {
		
//		UsefulwE = new RDFWordExtractor(relation, table,oce,condition);
		
		this.pW = pW;
		
		UsefulwE = usefulWE;
		GeneralwE = generalWE;
		
		this.stemmed = stemmed;
		this.frequency = frequency;
		this.lowercase = lowercase;
		
		loadStopWords(stopWordsFile);
		
		if (tuples!=null){
			setTuplesAsStopWords(tuples);
		}
		
		generateInstances(sample,useful,frequency,createInstances);

	}

	private void setTuplesAsStopWords(Set<Tuple> tuples) {
		
		String[] fields, splitted;
		
		String value;
		
		for (Tuple tuple : tuples) {
			
			fields = tuple.getFieldNames();
			
			for (int i = 0; i < fields.length; i++) {
				
				value = tuple.getFieldValue(fields[i]);
					
				splitted = GeneralwE.getWords(value,!frequency,lowercase,stemmed);
					
				for (String string : splitted) {
						
					stopWords.add(string);
						
				}
				
			}
			
		}
		
	}

	private void loadStopWords(String stopWordsFile) {
		
		if (stopWordsFile==null){
			stopWords = new HashSet<String>();
			return;
		}
			
		try {
			stopWords = new HashSet<String>();
			
			BufferedReader br = new BufferedReader(new FileReader(new File(stopWordsFile)));
			
			String line = br.readLine();
			
			while (line!=null){
				
				stopWords.add(line.toLowerCase());
				
				line = br.readLine();
				
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void generateInstances(Collection<Document> sample, Collection<Document> useful, boolean frequency, boolean createInstances) {
		
		if (!frequency)
			generateArffStructure(sample,useful);
		else 
			generateArffStructureFrequency(sample,useful);
		
		if (createInstances){
		
			try {
				
				m_sourceReader = new BufferedReader(new StringReader(arffContent));
				
				instances = new Instances(m_sourceReader);
				
				instances.setClassIndex(0);
			
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
			
	}

	private void generateArffStructureFrequency(
			Collection<Document> sample, Collection<Document> useful) {
		
		String data = "\n\n@DATA\n\n";
		String word;
		Integer index;
		ArrayList<Integer> auxData = new ArrayList<Integer>();
		Hashtable<Integer,Integer> auxFrequency = new Hashtable<Integer,Integer>();
		Hashtable<String, Integer> wordIndex = new Hashtable<String, Integer>();
		ArrayList<String> wordSet = new ArrayList<String>();
		int newIndexOfWord = 1;
		
		int inindex = 0;
		
		for (Document document : sample) {
			
			System.out.println(inindex++ + ": out of: " + sample.size() + " -> " + document);
			
			String[] words;
			
			if (useful.contains(document)){
				words = UsefulwE.getWords(document, false,lowercase,stemmed,pW);
			}
			else{
				words = GeneralwE.getWords(document, false,lowercase,stemmed,pW);
			}
			
			data = data + "\n{";
			
			if (useful.contains(document)) {
				
				data = data + "0 1";
			
			}
			else {
			
				data = data + "0 0";
			
			}
			
			auxData.clear();
			
			Hashtable<String,Integer> wordFrequency = generateTable(words);
			
			for (Enumeration<String> e=wordFrequency.keys();e.hasMoreElements();) {
				
				word = e.nextElement();
				
				if (!validForQuerying(word))
					continue;
								
				index = wordIndex.get(word);
												
				if (index==null){
					index = new Integer(newIndexOfWord++);
					wordIndex.put(word,index);
					wordSet.add(word);
				}
				
				auxData.add(index);
				auxFrequency.put(index,wordFrequency.get(word));
			}
			
			data = data + generateCSVFrequency(auxData,auxFrequency);
			
			data = data + "}";
			
		}
		
		String header = "@RELATION qxtract\n";
		
		String attributes = "";
		
		attributes = attributes + "\n@ATTRIBUTE class {0,1}";
		
		for (String string : wordSet) {
			attributes = attributes + "\n@ATTRIBUTE " + Utils.quote(string) + " NUMERIC";
		}
		
		arffContent = header + attributes + data;
		
		
		//This step is provisional
		
	}

	private String generateCSVFrequency(ArrayList<Integer> auxData,
			Hashtable<Integer,Integer> auxFrequency) {
		
		String ret = "";
		
		Collections.sort(auxData);
		
		for (Integer integer : auxData) {
			ret = ret + ", " + integer.toString() + " " + auxFrequency.get(integer).toString();
		}
		
		return ret;
		
	}

	private Hashtable<String,Integer> generateTable(String[] words) {
		
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		
		for (String string : words) {
			
			if (stopWords.contains(string))
				continue;
			
			Integer freq = table.get(string);
			
			if (freq==null){
				freq = new Integer(0);
			}
			
			freq++;
			
			table.put(string, freq);
		}
		
		
		return table;
		
		
	}

	private void generateArffStructure(Collection<Document> sample,
			Collection<Document> useful) {
		//Write the string out in case of a backup is needed

		String data = "\n\n@DATA\n\n";
		String word;
		Integer index;
		ArrayList<Integer> auxData = new ArrayList<Integer>();
		Hashtable<String, Integer> wordIndex = new Hashtable<String, Integer>();
		ArrayList<String> wordSet = new ArrayList<String>();
		int newIndexOfWord = 1;
		
		int inindex = 0;
		
		for (Document document : sample) {
			
			inindex++;
			
			if (inindex % 500 == 0)
				System.out.println(inindex + ": out of: " + sample.size() + " -> " + document);
			
			String[] words;
			if (useful.contains(document)){
				words = UsefulwE.getWords(document, true,lowercase,stemmed,pW);
			}
			else{
				words = GeneralwE.getWords(document, true,lowercase,stemmed,pW);
			}
			
			data = data + "\n{";
			
			if (useful.contains(document)){
				data = data + "0 1";
			}
			else{
				data = data + "0 0";
			}
			
			auxData.clear();
			
			for (int i = 0; i < words.length; i++) {
				
				word = words[i].toLowerCase();
				
				if (!validForQuerying(word))
					continue;
				
				index = wordIndex.get(word);
				
				if (index==null){
					index = new Integer(newIndexOfWord++);
					wordIndex.put(word,index);
					wordSet.add(word);
				}
				
				auxData.add(index);
			}
			
			data = data + generateCSV(auxData);
			
			data = data + "}";
			
		}
		
		String header = "@RELATION qxtract\n";
		
		String attributes = "";
		
		attributes = attributes + "\n@ATTRIBUTE class {0,1}";
		
		for (String string : wordSet) {
			
			attributes = attributes + "\n@ATTRIBUTE " + Utils.quote(string) + " {0,1}";

		}
		
		arffContent = header + attributes + data;
		
		
		//This step is provisional
		
	}

	private boolean validForQuerying(String word) {
		
		if (stopWords.contains(word) || word.trim().isEmpty())
			return false;
		
		char[] vec = word.toCharArray();
		
		for (int i = 0; i < vec.length; i++) {
			
			if (!Character.isLetter(vec[i]))
				return false;
			
		}
		
		return true;
		
	}

	private String generateCSV(ArrayList<Integer> auxData) {
		
		String ret = "";
		
		Collections.sort(auxData);
		
		for (Integer integer : auxData) {
			ret = ret + ", " + integer.toString() + " 1";
		}
		
		return ret;
		
	}

//	private String[] getDifferentWords(String docHandle, String newName) {
//		
//		return wE.getWords(docHandle, true,true,stemmed, newName);
//		
//	}

	@Override
	public Instances getDataSet() throws IOException {
		return instances;
	}

	@Override
	public Instance getNextInstance(Instances structure) throws IOException {
		
	    m_structure = structure;

	    if (getRetrieval() == BATCH) {
	      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
	    }
	    setRetrieval(INCREMENTAL);

	    Instance current = m_ArffReader.readInstance(m_structure);
	   
	    if (current == null) {
	      try {
	        // close the stream
	        m_sourceReader.close();
	        //        reset();
	      } catch (Exception ex) {
	        ex.printStackTrace();
	      }
	    }
	    return current;
	}

	@Override
	public Instances getStructure() throws IOException {
	    if (m_structure == null) {
	        try {
	        	m_sourceReader = new BufferedReader(new StringReader(arffContent), 1);
			  	m_ArffReader = new ArffReader(m_sourceReader);
			  	m_structure  = m_ArffReader.getStructure();
			 } catch (Exception ex) {
			  	throw new IOException("Unable to determine structure as arff (Reason: " + ex.toString() + ").");
			 }
	    }

	    return new Instances(m_structure, 0);
	}

	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 4985 $");
	}

	public void printFile(String output){
		
		BufferedWriter bw;
		try {
			
			bw = new BufferedWriter(new FileWriter(new File(output)));
			
			bw.write(arffContent);
			
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
