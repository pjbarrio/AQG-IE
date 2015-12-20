package techniques.algorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import online.documentHandler.contentExtractor.ContentExtractor;

import sample.generation.model.SampleBuilderParameters;
import searcher.interaction.formHandler.TextQuery;
import utils.clock.Clock;
import utils.clock.Clock.ClockEnum;
import utils.persistence.persistentWriter;
import weka.core.Instances;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;
import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.CachCandidateSentencesRelationship;

import exploration.model.Document;
import exploration.model.Sample;

class DocumentCacher implements Runnable {

	private Document document;
	private Semaphore sp;
	private persistentWriter pW;

	public DocumentCacher(Document document, Semaphore sp, persistentWriter pW) {
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


public class SignificantPhrases extends ExecutableSimpleAlgorithm {

	private int ngrams;
	private ContentExtractor ce;
	TokenizerFactory tokenizerFactory;
	private int generatedQueries;
	
	public SignificantPhrases(Sample sample, int max_query_size,
			int min_support, int min_supp_after_update, int ngrams, ContentExtractor ce, int generatedQueries) {
		super(sample, max_query_size, min_support, min_supp_after_update);
		this.ngrams = ngrams;
		this.ce = ce;
		this.generatedQueries = generatedQueries;
		tokenizerFactory = new EnglishStopTokenizerFactory(new LowerCaseTokenizerFactory(new  RegExFilteredTokenizerFactory(new IndoEuropeanTokenizerFactory(), Pattern.compile("[^\\p{Punct}]*"))));
		
	}

	@Override
	protected int getParameterId(persistentWriter pW, int maxQuerySize,
			int minSupport, int minSuppAfterUpdate) {
		return pW.getSignificantPhrases(maxQuerySize,minSupport,minSuppAfterUpdate,ngrams,ce,generatedQueries);
	}

	@Override
	protected List<Pair<TextQuery, Long>> execute(Instances sample,
			persistentWriter pW, SampleBuilderParameters sp) throws Exception {
		
		Collection<Document> documents = pW.getUsefulDocuments(this.sample, sp.getUsefulDocuments());
		
		List<String> content = loadContent(documents,pW);
		
		System.out.println("Training background model");
		TokenizedLM backgroundModel = buildModel(tokenizerFactory,ngrams,content);

		backgroundModel.sequenceCounter().prune(min_support);

		System.out.println("\nAssembling collocations in Training");
		SortedSet<ScoredObject<String[]>> coll = backgroundModel.collocationSet(ngrams,min_support,generatedQueries);

		System.out.println("\nCollocations in Order of Significance:");
		
		Clock.stopTime(ClockEnum.SIMPLE_ALGORITHM);
		
		long time = Clock.getMeasuredTime(ClockEnum.SIMPLE_ALGORITHM);
		
		return report(coll,time, generatedQueries);
		
	}

	private List<Pair<TextQuery, Long>> report(SortedSet<ScoredObject<String[]>> nGrams,long time, int limit) {
	    
		List<Pair<TextQuery,Long>> ret = new ArrayList<Pair<TextQuery,Long>>(Math.min(nGrams.size(), limit));
		
		for (ScoredObject<String[]> nGram : nGrams) {
	        String[] toks = nGram.getObject();
	        ret.add(new Pair<TextQuery, Long>(new TextQuery(Arrays.asList(toks)), time));
	    }
		
		return ret;
	}
	    
	private List<String> loadContent(Collection<Document> documents, persistentWriter pW) {
		
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
			
			Thread t = new Thread(new DocumentCacher(document,sp,pW));
			
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
	
	@Override
	protected String getName() {
		return "Significant-"+ngrams+"-Phrases";
	}

	private TokenizedLM buildModel(TokenizerFactory tokenizerFactory,
			int ngram, List<String> contents)
					throws IOException {

		TokenizedLM model = new TokenizedLM(tokenizerFactory,ngram);

		for (int j = 0; j < contents.size(); ++j) {
			
			model.handle(contents.get(j));
		
		}
		return model;
	}
	
}
