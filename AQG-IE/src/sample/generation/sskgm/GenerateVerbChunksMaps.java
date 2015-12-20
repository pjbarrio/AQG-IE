package sample.generation.sskgm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import com.google.gdata.util.common.base.Pair;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.Segment;
import edu.columbia.cs.ref.model.Sentence;
import edu.columbia.cs.ref.model.Span;
import edu.columbia.cs.ref.tool.chunker.Chunker;
import edu.columbia.cs.ref.tool.chunker.impl.OpenNLPChunker;
import edu.columbia.cs.ref.tool.postagger.POSTagger;
import edu.columbia.cs.ref.tool.postagger.impl.OpenNLPPOSTagger;
import edu.columbia.cs.ref.tool.tokenizer.Tokenizer;
import edu.columbia.cs.ref.tool.tokenizer.impl.OpenNLPTokenizer;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import execution.workload.tuple.Tuple;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.rdf.RDFPESExtractor;

public class GenerateVerbChunksMaps {

	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//Second after GenerateList and before DiscriminativeScore
		
		int val = Integer.valueOf(args[0]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		
		String[][] relations = new String[][]{{"PersonCareer"},{"NaturalDisaster"},{"ManMadeDisaster"},{"Indictment","Arrest","Trial"},{"PersonTravel"},{"VotingResult"}};
		
		String[] relationName = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment-Arrest-Trial","PersonTravel","VotingResult"};
		
		int splits = 1;
		
		int alts = 5;
		
		int size = 10000;		
		
		for (int j = 1; j <= alts; j++) {

			for (int rel = val; rel <= val; rel++) {
				
				System.out.println("Split: " + j);
				System.out.println("Relation: " + Arrays.toString(relations[rel]));
				
				for (int i = 1; i <= splits; i++) {
					
					int realsize = size;
					
					System.out.println("Size: " + realsize);
					
					Map<String,Map<String,Integer>> map;
					
					map = pW.getSentencesForRelation(collection,relations[rel][0],realsize,j);
					
					for (int k = 1; k < relations[rel].length; k++) {
						
						updateMap(map,pW.getSentencesForRelation(collection,relations[rel][k],realsize,j));
						
					}
					
					pW.saveChunksForRelations(collection,relationName[rel],realsize,j,generateChunk(map));
					
				}
				
			}				

			
		}
		
		
	}

	private static Map<String,Integer> generateChunk(Map<String, Map<String,Integer>> map) {
		
		Tokenizer tokenizer = new OpenNLPTokenizer("en-token.bin");
		POSTagger pos = new OpenNLPPOSTagger("en-pos-maxent.bin");
		Chunker chunker = new OpenNLPChunker("en-chunker.bin");
		
		Map<String,Integer> ret = new HashMap<String, Integer>();
		
		int p = 0;
		
		for(Entry<String,Map<String,Integer>> entry : map.entrySet()){
			
			if (p % 50 == 0)
//				System.out.println(p + " - " + entry.getKey() + " - " + entry.getValue());
			
			p++;
			
			Span[] tokenization = tokenizer.tokenize(entry.getKey());
			String[] tokenizationStrings = new String[tokenization.length];
			for(int j=0; j<tokenization.length; j++){
				Span s = tokenization[j];
				tokenizationStrings[j]=entry.getKey().substring(s.getStart(),s.getEnd());
			}
			
			String[] posTags = pos.tag(tokenizationStrings);
			
			String[] chunks = chunker.chunk(tokenizationStrings, posTags);
			
//			System.out.println(Arrays.toString(tokenizationStrings));
//			
//			System.out.println(Arrays.toString(chunks));
			
			for (Entry<String,Integer> entryFreq : entry.getValue().entrySet()) {
				
				List<String> vchunks = getVerbChunks(tokenization,tokenizationStrings,chunks,entryFreq.getKey());
				
				for (int i = 0; i < vchunks.size(); i++) {
					
					Integer freq = ret.remove(vchunks.get(i));
					
					if (freq == null){
						freq = 0;
					}
					
					freq+=entryFreq.getValue();
					
					ret.put(vchunks.get(i),freq);
				}
				
			}
			
		}
		
		return ret;
	}

	private static List<String> getVerbChunks(Span[] tokenization,String[] tokenizationStrings,
			String[] chunks, String offset_length) {
		
		String[] spl = offset_length.split("-");
		
		int offset = Integer.valueOf(spl[0]);
		int length = Integer.valueOf(spl[1]);
		
		int lastOffset = offset + length;
		
		List<String> ret = new ArrayList<String>();
		
		boolean carryingvb = false;
		
		StringBuilder sb = null;
		
		int fi = 0;
		
		while (fi < tokenization.length && tokenization[fi].getStart() < offset)
			fi++;
		
		if (fi >= tokenization.length){ //it happens in person career.
			
//						[_Rep.]
//						[O]
//						1
//						70
//						[Other, big, recipients, of, defense, PAC, money, included, :, _Rep.]
//						[B-NP, I-NP, I-NP, B-PP, B-NP, I-NP, I-NP, B-VP, O, O]
//						56
//						85
//						[---Ms.]
//						[O]
//						3
//						72
//						[Among, other, research, presented, at, the, meeting, :, _Dr.]
//						[B-PP, B-NP, I-NP, B-VP, B-PP, B-NP, I-NP, O, O]
//						51
//						56
//						[_Dr.]
//						[O]
//						1
//						51
//						[-D.]
//						[O]
//						1
//						72
//						[---Mr.]
//						[O]
//						3
//						51			
			System.out.println(Arrays.toString(tokenizationStrings));
			System.out.println(Arrays.toString(chunks));
			System.out.println(offset);
			System.out.println(length);
		}
		
		for (int i = fi; i < chunks.length && tokenization[i].getStart() < lastOffset; i++) {
		
			if (!carryingvb && chunks[i].equals("B-VP")){
				 //first one
				sb = new StringBuilder();
				sb.append(tokenizationStrings[i]);
				carryingvb = true;
			} else if (carryingvb && chunks[i].equals("I-VP")){
					
				sb.append(" " + tokenizationStrings[i]);

			} else {
				if (carryingvb){
//					System.out.println(sb.toString());
					ret.add(sb.toString());
					carryingvb = false;
					sb = null;
				}
			}
			
		}

		if (carryingvb){ //the sentence finished and there was one more
//			System.out.println(sb.toString());
			ret.add(sb.toString());
			sb = null;
		}

		
		return ret;
		
	}

	private static void updateMap(Map<String, Map<String,Integer>> map,
			Map<String, Map<String,Integer>> sentencesForRelation) {
		
		for (Entry<String,Map<String,Integer>> entry : sentencesForRelation.entrySet()) {
			
			Map<String,Integer> freqTable = map.remove(entry.getKey());
			
			if (freqTable == null){
				freqTable = new HashMap<String, Integer>();
			}
			
			for (Entry<String,Integer> entryFreq : entry.getValue().entrySet()) {
				
				Integer freq = freqTable.remove(entryFreq.getKey());
						
				if (freq == null){
					freq = 0;
				}
					
				freq += entryFreq.getValue();

				freqTable.put(entryFreq.getKey(), freq);
				
			}
			
			map.put(entry.getKey(), freqTable);
		}
		
	}

}
