package sample.generation.sskgm.significantPhrases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.DummyContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentLoader.impl.SgmlContentLoader;

import sample.generation.model.SampleBuilderParameters;
import sample.generation.sskgm.significantPhrases.tokenizer.ValidWordTokenizer;
import sample.weka.SampleArffGenerator;
import searcher.interaction.formHandler.TextQuery;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.document.DocumentHandler;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import weka.core.Instances;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;
import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.CachCandidateSentencesRelationship;
import domain.caching.candidatesentence.tool.RelationConfiguration;

import execution.workload.tuple.Tuple;
import exploration.model.Document;
import exploration.model.Sample;
import extraction.relationExtraction.RelationExtractionSystem;

class DocumentCacher implements Runnable {

	private Document document;
	private Semaphore sp;
	private persistentWriter pW;

	public DocumentCacher(persistentWriter pW, Document document, Semaphore sp) {
		this.document = document;
		this.sp = sp;
		this.pW = pW;
	}

	@Override
	public void run() {
		
		document.getContent(pW);
		
		sp.release();
		
	}
	
}


public class SignificantPhrases {

	public static void main(String[] args) throws IOException {
		
		System.out.println("Using the validator...");
		
		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
				
		int db = Integer.valueOf(args[3]); //3000
		
		int size = Integer.valueOf(args[4]);
		
		int[] spl = {Integer.valueOf(args[5])};//{1,2,3,4,5/*,6,7,8,9,10*/};
		
		boolean[] tuplesAsStopWords = {Boolean.valueOf(args[6])};//{true,false};
		
		int version = 1;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment,pW,ieSystem,true,false,db,new SgmlContentExtraction());
		
		int relConf = RelationConfiguration.getRelationConf(relationExperiment);
		
		int min_support = 3;
		
		int ngrams = 2;

		ContentExtractor ce = new SgmlContentExtraction();
	
		String[] relations = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		TokenizerFactory tokenizerFactoryPrev = new ValidWordTokenizer(new StopTokenizerFactory(new EnglishStopTokenizerFactory(new LowerCaseTokenizerFactory(new  RegExFilteredTokenizerFactory(new IndoEuropeanTokenizerFactory(), Pattern.compile("[^\\p{Punct}]*")))),new HashSet<String>(FileUtils.readLines(new File(pW.getStopWords())))));
		
		int[] workload = new int[]{RelationConfiguration.getWorkload(relationExperiment)};
		
		int generatedQueries = 1000;
	
		DocumentHandler dh = new DocumentHandler(pW.getDatabaseById(db), 1, pW);
		
		List<Long> times = new ArrayList<Long>();
		
		for (int rel = 0; rel < relations.length; rel++) {

			String matchingTuplesFile = pW.getMatchingTuplesWithSources(collection,relations[rel],tr.getName());
			
			Hashtable<Document, ArrayList<Tuple>> tuplesTable = TuplesLoader.loadDocumenttuplesTuple(pW.getDatabaseById(db),matchingTuplesFile);
			
			for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {
				
				for (int a=0;a<spl.length; a++) {

					File output = new File(pW.getSignificantPhrasesFromSplitModel(collection,relations[rel],size,spl[a],tuplesAsStopWords[tasw],tr.getName()));					
					
					TokenizerFactory tokenizerFactory;
					
					File useful = new File(pW.getUsefulDocumentExtractionForRelation(collection, relations[rel], size, spl[a], tr.getName()));
					
					Collection<Document> documents = generateDocuments(dh,FileUtils.readLines(useful));
					
					if (tuplesAsStopWords[tasw]){
						tokenizerFactory = new StopTokenizerFactory(tokenizerFactoryPrev, loadTuplesAsStopWords(documents,tuplesTable));
					}else{
						tokenizerFactory = tokenizerFactoryPrev;
					}
					
					System.gc();
					
					System.out.println("Loading Content");
					
					List<String> content = loadContent(documents,ce,pW);
					
					System.out.println("Training background model");
					TokenizedLM backgroundModel = buildModel(tokenizerFactory,ngrams,content);

					backgroundModel.sequenceCounter().prune(min_support);

					System.out.println("\nAssembling collocations in Training");
					SortedSet<ScoredObject<String[]>> coll = backgroundModel.collocationSet(ngrams,min_support,generatedQueries);

					System.out.println("\nCollocations in Order of Significance:");
					
					List<String> list = report(coll,generatedQueries);
					
					FileUtils.writeLines(output, list);
					
					for (int l = 0; l < list.size() && l < generatedQueries; l++) {
						
						TextQuery qq = new TextQuery(list.get(l));
						
						pW.writeSignificantPhrase(ieSystem,relConf,collection, workload[rel],version,spl[a],tuplesAsStopWords[tasw],l+1,qq);
						
					}
					
					System.err.println(System.currentTimeMillis());
					
					times.add(System.currentTimeMillis());
					
				}
				
			}

			
		}
		
		
		System.err.println(times.toString());
		
		
	}

	private static Set<String> loadTuplesAsStopWords(Collection<Document> documents, Hashtable<Document, ArrayList<Tuple>> tuplesTable) {
		
		WordExtractorAbs we = new WordExtractor(new DummyContentExtractor(), new SgmlContentLoader());
		
		Set<Tuple> tuples = new HashSet<Tuple>();
		
		for (Document useful : documents) {
				
			tuples.addAll(tuplesTable.get(useful));
			
		}

		Set<String> stopWords = new HashSet<String>();
		
		for (Tuple tuple : tuples) {
			
			String[] fields = tuple.getFieldNames();
				
			for (int i = 0; i < fields.length; i++) {
				
				String[] splitted = we.getWords(tuple.getFieldValue(fields[i]),true,true,false);
					
				for (String string : splitted) {
						
					stopWords.add(string);
						
				}
				
			}
			
		}
		
		return stopWords;
	}

	private static Collection<Document> generateDocuments(DocumentHandler dh,
			List<String> docs) {
		
		List<Document> doc = new ArrayList<Document>(docs.size());
		
		for (int i = 0; i < docs.size(); i++) {
			
			doc.add(dh.getDocument(docs.get(i)));
			
		}
		
		return doc;
	}

	private static List<String> report(SortedSet<ScoredObject<String[]>> nGrams, int limit) {
	    
		List<String> ret = new ArrayList<String>(Math.min(nGrams.size(), limit));
		
		for (ScoredObject<String[]> nGram : nGrams) {
	        String[] toks = nGram.getObject();
	        ret.add(generateString(toks));
	    }
		
		return ret;
	}
	    
	private static String generateString(String[] toks) {
		
		String s = toks[0];
		
		for (int i = 1; i < toks.length; i++) {
			s += " " + toks[i];
		}
		
		return s;
	}

	private static List<String> loadContent(Collection<Document> documents, ContentExtractor ce, persistentWriter pW) {
		
		cachContent(documents,pW);
		
		List<String> ret = new ArrayList<String>(documents.size());
		
		for (Document document : documents) {
		
			ret.add(ce.extractContent(document.getContent(pW)));
			
		}
				
		return new ArrayList<String>(ret);

	}

	private static void cachContent(Collection<Document> documents, persistentWriter pW) {
		
		System.out.println("Caching content...");
		
		Semaphore sp = new Semaphore(50);
		
		List<Thread> ts = new ArrayList<Thread>(documents.size());
		
		for (Document document : documents) {
			
			try {
				sp.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if ((ts.size() % 50)==0)
				System.out.println("Status ... " + ts.size());
			
			Thread t = new Thread(new DocumentCacher(pW,document,sp));
			
			t.start();
			
			ts.add(t);
			
		}
		
		for (Thread thread : ts) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static TokenizedLM buildModel(TokenizerFactory tokenizerFactory,
			int ngram, List<String> contents)
					throws IOException {

		TokenizedLM model = new TokenizedLM(tokenizerFactory,ngram);

		for (int j = 0; j < contents.size(); ++j) {
			
			model.handle(contents.get(j));
		
		}
		return model;
	}
	
}
