package sample.generation.sskgm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import com.google.gdata.util.common.base.Pair;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

import domain.caching.candidatesentence.tool.RelationConfiguration;
import domain.caching.tuple.CachTuple;

import edu.columbia.cs.ref.model.Span;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.entity.Entity;
import edu.columbia.cs.ref.tool.chunker.Chunker;
import edu.columbia.cs.ref.tool.chunker.impl.OpenNLPChunker;
import edu.columbia.cs.ref.tool.postagger.POSTagger;
import edu.columbia.cs.ref.tool.postagger.impl.OpenNLPPOSTagger;
import edu.columbia.cs.ref.tool.tokenizer.Tokenizer;
import edu.columbia.cs.ref.tool.tokenizer.impl.OpenNLPTokenizer;
import execution.workload.tuple.Tuple;
import exploration.model.Document;
import extraction.relationExtraction.RelationExtractionSystem;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.rdf.RDFPESExtractor;

//class SentenceSplitter{
//	
//	/** The sentence detector. */
//	private SentenceDetectorME sentenceDetector;
//	
//	/**
//	 * Instantiates a OpenNLP sentence splitter.
//	 *
//	 * @param path the path to the model to be used.
//	 * @throws FileNotFoundException the file not found exception
//	 */
//	public SentenceSplitter(String path) throws FileNotFoundException{
//		InputStream modelIn = new FileInputStream(path);
//		SentenceModel model=null;
//		try {
//			model = new SentenceModel(modelIn);
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		finally {
//			if (modelIn != null) {
//				try {
//					modelIn.close();
//				}
//				catch (IOException e) {
//				}
//			}
//		}
//
//		sentenceDetector = new SentenceDetectorME(model);
//	}
//	
//	/* (non-Javadoc)
//	 * @see edu.columbia.cs.ref.tool.document.splitter.SentenceSplitter#split(edu.columbia.cs.ref.model.Document)
//	 */
//	public Sentence[] split(String content) {
//	
//		Document d = new Document(Arrays.asList(new Segment(content, 0)));
//		
//		List<Sentence> sents = new ArrayList<Sentence>();
//		
//		Span[] sentsSpans = sentenceDetector.sentPosDetect(content);
//		for(Span s : sentsSpans){
//			int startSpan = s.getStart();
//			int endSpan = s.getEnd();
//			Sentence newSent = new Sentence(d, startSpan, endSpan-startSpan);
//			sents.add(newSent);
//		}
//	
//		Sentence[] result = new Sentence[sents.size()];
//		return sents.toArray(result);
//		
//	}
//	
//}

//class LoadRunnable implements Runnable {
//
//	private String file;
//	private List<String> contents;
//
//	public LoadRunnable(String file, List<String> contents) {
//		this.file = file;
//		this.contents = contents;
//	}
//
//	@Override
//	public void run() {
//		
//		String content;
//		try {
//			content = FileUtils.readFileToString(new File(file));
//
//			synchronized (contents) {
//				contents.add(content);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		
//	}
//	
//}

public class GenerateList {

	
	static Tokenizer tokenizer = new OpenNLPTokenizer("en-token.bin");
	static POSTagger pos = new OpenNLPPOSTagger("en-pos-maxent.bin");
	static Chunker chunker = new OpenNLPChunker("en-chunker.bin");

	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		//First Algorithm of Self-supervised
		
		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
		
		int db = Integer.valueOf(args[3]); //3000
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int relConf = RelationConfiguration.getRelationConf(relationExperiment);
		
		String[] relations = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		int informationExtractionSystemBase = RelationConfiguration.getInformationExtractionSystemId(ieSystem);
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment,pW,ieSystem,true,false,db,new SgmlContentExtraction());
		
		ContentExtractor ce = new SgmlContentExtraction();
		
		int splits = 1;
		
		int alts = 5;
		
		int size = 50;		
		
		for (int j = 1; j <= alts; j++) {

			for (int rel = 0; rel < relations.length; rel++) {
				
				System.out.println("Split: " + j);
				
				for (int i = 1; i <= splits; i++) {
					
					int realsize = size;
					
					System.out.println("Size: " + realsize);
					
					List<Long> docsWithTuples = pW.getDocumentsWithTuples(ieSystem,relConf,db);

					Map<Document, String> opStructMap = pW.getExtractionTable(db, pW.getRelationExtractionSystemId(relConf, informationExtractionSystemBase),ce,new ArrayList<Long>(docsWithTuples));
					
					Map<String,Integer> map = generateMap(opStructMap);
					
//					pW.saveSentencesForRelation(collection,relations[rel],realsize,j,map,tr.getName());
					
					pW.saveChunksForRelations(collection,relations[rel],realsize,j,map,tr.getName());
					
				}
				
			}				

			
		}
		
		
	}

	private static Map<String, Integer> generateMap(
			Map<Document, String> opStructMap) throws IOException, ClassNotFoundException{
		
		Map<String,Integer> map = new HashMap<String, Integer>();
		
		List<Document> docs = new ArrayList<Document>(opStructMap.keySet());
		
		int toLoad = 50; 
		
		int alreadyLoaded = 0;
		
		Map<Document,String> auxMap = null;
		
		for (int i = 0; i < docs.size(); i++) {
			
			if ((alreadyLoaded%toLoad)==0){
				if (i>0){
					
					Map<Document, Set<OperableStructure>> opStructs = CachTuple.loadOperableStructures(auxMap);
					
					process(opStructs,map);
					
					auxMap.clear();
					
				}
				
				auxMap = new HashMap<Document,String>(toLoad);
				
				auxMap.put(docs.get(i), opStructMap.get(docs.get(i)));
			
			}
			
			alreadyLoaded++;
			
		}
		
		if (auxMap != null && auxMap.size() > 0){
			
			Map<Document, Set<OperableStructure>> opStructs = CachTuple.loadOperableStructures(auxMap);
			
			process(opStructs,map);
			
		}
		
		return map;
		
	}

	private static void process(
			Map<Document, Set<OperableStructure>> opStructs,
			Map<String, Integer> map) {
		
		for (Entry<Document, Set<OperableStructure>> entry : opStructs.entrySet()) {
			
			for (OperableStructure operableStructure : entry.getValue()) {
				
				int sentOff = operableStructure.getCandidateSentence().getSentence().getOffset();
				
				Entity[] ents = operableStructure.getCandidateSentence().getEntities();
				
				if (ents[0].getOffset() > ents[1].getOffset()){ //the first one is the one that appears first
					Entity aux = ents[1];
					ents[1] = ents[0];
					ents[0] = aux;
				}
				
				if (ents[0].getOffset() + ents[0].getLength() < ents[1].getOffset()){
					
					updateChunks(sentOff,operableStructure.getCandidateSentence().getSentence().getValue(),ents,map);
					
				}
				
			}
		}
	}

	private static void updateChunks(int sentOff, String sentence, Entity[] ents,
			Map<String, Integer> map) {
		
		Span[] tokenization = tokenizer.tokenize(sentence);
		String[] tokenizationStrings = new String[tokenization.length];
		for(int j=0; j<tokenization.length; j++){
			Span s = tokenization[j];
			tokenizationStrings[j]=sentence.substring(s.getStart(),s.getEnd());
		}
		
		String[] posTags = pos.tag(tokenizationStrings);
		
		String[] chunks = chunker.chunk(tokenizationStrings, posTags);
		
		List<String> vchunks = getVerbChunks(tokenization,tokenizationStrings,chunks,ents[0].getOffset()-sentOff + ents[0].getLength(),ents[1].getOffset());
		
		for (int i = 0; i < vchunks.size(); i++) {
			
			Integer freq = map.remove(vchunks.get(i));
			
			if (freq == null){
				freq = 0;
			}
			
			freq+=1;
			
			map.put(vchunks.get(i),freq);
		
		}
		
	}

	private static List<String> getVerbChunks(Span[] tokenization,String[] tokenizationStrings,
			String[] chunks, int offset, int lastOffset) {
		
		List<String> ret = new ArrayList<String>();
		
		boolean carryingvb = false;
		
		StringBuilder sb = null;
		
		int fi = 0;
		
		while (fi < tokenization.length && tokenization[fi].getStart() < offset)
			fi++;
		
		if (fi >= tokenization.length){ //it happens in person career.
			System.out.println(Arrays.toString(tokenizationStrings));
			System.out.println(Arrays.toString(chunks));
			System.out.println(offset);
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
	
//	private static Map<String, Map<String,Integer>> generateMap(Map<Document, String> opStructMap, String relation) {
//		
////		ContentExtractor ce = new SgmlContentExtraction();
////		
////		SentenceSplitter sp = null;
////		try {
////			sp = new SentenceSplitter("en-sent.bin");
////		} catch (FileNotFoundException e1) {
////			e1.printStackTrace();
////		}
//		
//		int SIZE = 100;
//		
//		Map<String,Map<String,Integer>> map = new HashMap<String, Map<String,Integer>>();
//		
//		for (int h = 0; h < opStructMap.size(); ) {
//
//			System.out.println("Opening: " + h + " out of " + opStructMap.size());
//			
//			List<String> contents = new ArrayList<String>(SIZE);
//			
//			List<Thread> ts = new ArrayList<Thread>(SIZE);
//			
//			for (int i = 0; i < SIZE && h+i<opStructMap.size(); i++,h++) {
//				
//				Thread t = new Thread(new LoadRunnable(opStructMap.get(h+i),contents));
//				
//				t.start();
//				
//				ts.add(t);
//				
//			}
//			
//			for (Thread thread : ts) {
//				try {
//					thread.join();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			for (int j = 0; j < contents.size(); j++) {
//				
//////				String file = "file://" + extractedFiles.get(j);
////				
////				Graph g = new GraphMem();
////				
////				ModelCom model = new ModelCom(g);
////				
////				try {
////				
////					model.read(new StringReader(contents.get(j)),null);
////				
////				} catch (Exception e) {
////					
////					e.printStackTrace();
////					
////				}
////				
////				ResIterator resIte = model.listResourcesWithProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","document"));
////				
////				String content = "";
////				
////				while (resIte.hasNext()){ //There's only one
////					
////					Resource res = resIte.next();
////					
////					content = res.getProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","document")).getObject().toString();
////					
////				}
//				
//				String content = ce.extractContent(contents.get(j));
//				
//				Sentence[] sents = sp.split(content);
//				
//				List<Pair<Tuple, String[]>> tuples = RDFPESExtractor.extract(model, relation);
//				
//				for (Pair<Tuple, String[]> pair : tuples) {
//					
//					int offset = Integer.valueOf(pair.getSecond()[3]);
//					int length = Integer.valueOf(pair.getSecond()[4]);
//					
//					for (int i = 0; i < sents.length; i++) {
//						
//						if (sents[i].getOffset()>offset){
//							
//							int offsetInSent = offset-sents[i-1].getOffset();
//							
//							add(sents[i-1],offsetInSent,length,map);
//							
//							break;
//						}
//						
//					}
//					
//				}
//				
//			}
//			
//			
//		}
//		
//		return map;
//		
//		
//	}
//
//	private static void add(Sentence sentence, int offset, int length, Map<String, Map<String,Integer>> map) {
//		
//		String val = offset + "-" + length;
//		
//		String lc = sentence.getValue();
//		
//		Map<String,Integer> freqTable = map.remove(lc);
//
//		if (freqTable == null){
//			freqTable = new HashMap<String, Integer>();
//			
//		}
//		
//		Integer freq = freqTable.remove(val);
//		
//		if (freq == null){
//			freq = 0;
//		}
//		
//		freq++;
//		
//		freqTable.put(val, freq);
//		
//		map.put(lc,freqTable);
//
//	}

}
