package sample.generation.bootstrapping;

/* QxtractProcessor.java
*
* Created on January 28, 2005, 9:39 PM
*/

import init.initialization.similarity.FileSelector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentHandler.contentLoader.impl.FileContentLoader;
import online.documentHandler.navigation.NavigationHandler;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.navigation.utils.NavigationUtils;
import online.queryResultPageHandler.QueryResultPageHandler;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.ResultDocumentHandler;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;

import org.apache.commons.io.FileUtils;

import sample.generation.model.impl.DummySampleConfiguration;
import sample.generation.utils.SampleGenerationUtils;
import sample.weka.SampleArffGenerator;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.database.OnlineDatabase;
import exploration.model.enumerations.VersionEnum;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;

/**
*
* @author  Alpa
*/

//This class performs the entire Bootstrapping sample process.

public class BootstrappingSampleGeneration {

	private HashSet<Tuple> seed;
	private ArrayList<Document> sampleDocuments;
	private ArrayList<Document> useful;
	private ArrayList<Document> badexamples;
	private RelationExtractionSystem extractionSystem;
	private Searcher searcher;
	
	private double max_Sample_Size;
	private persistentWriter pW;
	private Sample sample;
	private boolean frequency;
	private boolean stemmed;
	private ArrayList<String> no_filtering_fields;
	private double max_seed_results;
	private ArrayList<String> inferred_types;
	private String[] relations;
	private WordExtractorAbs usefulWE;
	private WordExtractorAbs generalWE;
	 
	 public BootstrappingSampleGeneration(Searcher searcher, RelationExtractionSystem extractionSystem, int max_Sample_Size, int max_seed_results, Sample sample, persistentWriter pW, boolean frequency, boolean stemmed, ArrayList<String> no_filtering_fields, ArrayList<String> inferred_types, String[] relations, WordExtractorAbs usefulWE, WordExtractorAbs generalWE) {
	   sampleDocuments    = new ArrayList<Document>();
	   useful    = new ArrayList<Document>();
	   badexamples = new ArrayList<Document>();
	   this.searcher = searcher;
	   this.extractionSystem = extractionSystem;
	   this.max_Sample_Size = max_Sample_Size;
	   this.max_seed_results = max_seed_results;
	   this.pW = pW;
	   this.sample = sample;
	   this.frequency = frequency;
	   this.stemmed = stemmed;
	   this.no_filtering_fields = no_filtering_fields;
	   this.inferred_types = inferred_types;
	   this.relations = relations;
	   this.usefulWE = usefulWE;
	   this.generalWE = generalWE;
	 }
	 
	 public void execute(String seedTuplesLocation, String randomQueries) {
	 
		documentSample(seedTuplesLocation, randomQueries, max_Sample_Size,sample,pW);
		
		System.out.println("Samples =" + sampleDocuments.size());
		System.out.println("Useful =" + useful.size());
		System.out.println("Bad examples =" + badexamples.size());
   
		// add someBadSamples Here
		
		printSample(pW.getSampleFile(sample));
		
		saveTuples(seed,pW.getSampleTuples(sample));
		
		saveUsefulDocuments(useful,pW.getSampleUsefulDocuments(sample));
		
		System.out.println(pW.getStopWords());
				
		SampleArffGenerator sarffG = new SampleArffGenerator(pW.getStopWords(),usefulWE,generalWE);
		
//XXX class not used anymore		sarffG.learn(sampleDocuments, useful,pW.getArffRawModel(sample),seed,pW.getTempFile(sample,"bootstrapping"),frequency,stemmed,no_filtering_fields,sample.getVersion().getCondition(),false);
			 
//		return;
		
//		try {
//			new SampleGenerationUtils().removeDuplicates(sample,pW,sample.getDatabase().getId());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		//Generate full sample:
		
		SampleArffGenerator sarffGf = new SampleArffGenerator(pW.getStopWords(),usefulWE,generalWE);
		
//		sarffGf.learn(sampleDocuments, useful,pW.getArffFullModel(sample),new HashSet<Tuple>(),pW.getTempFile(sample,"bootstrapping"),frequency,stemmed,no_filtering_fields,sample.getVersion().getCondition(),false);
		
		String[] args = new String[4];
		
		args[2] = "0.003"; //%min
		
		args[3] = "0.9"; //%max
		
		try {
//			new SampleGenerationUtils().tailorAttributes(sample,args,pW);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		args = new String[2];
		
		try {
//			new SampleGenerationUtils().runSVM(sample,args,pW);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		args = new String[4];
		
		args[1] = "700";
		
		try {
//			new SampleGenerationUtils().removeAttributesBasedOnSVM(sample,args,pW,sample.getDatabase().getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			new SampleGenerationUtils(null).generateTrueModel(sample,pW);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	 
	private void saveUsefulDocuments(ArrayList<Document> useful,
			String sampleUsefulDocuments) {
		
//		try {
//			
//			FileUtils.writeLines(new File(sampleUsefulDocuments),useful);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		throw new UnsupportedOperationException("IMPLEMENT!");

		
	}

	private void saveTuples(HashSet<Tuple> tuples, String sampleTuplesFile) {
		
		List<String> tups = new ArrayList<String>();
		
		for (Tuple tuple : tuples) {
			
			tups.add(tuple.toString());
			
		}
		
		try {
			FileUtils.writeLines(new File(sampleTuplesFile), tups);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printSample(String SampleOutputFile) {
		
//		BufferedWriter bw;
//		try {
//			bw = new BufferedWriter(new FileWriter(new File(SampleOutputFile)));
//			
//			for (Document doc : sampleDocuments) {
//				
//				bw.write(doc.getDocHandle() + "\n");
//				
//			}
//			
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		throw new UnsupportedOperationException("IMPLEMENT!");

		
	}
	
	private void addToSeed(Tuple[] tuples, UsefulCondition usefulCondition) {
		
		if (tuples == null) return;
		
			for (int i=0; i< tuples.length; i++) {
		
				if (tuples[i] != null)
				
					if (usefulCondition.matchCondition(tuples[i]))
					
						seed.add(tuples[i]);
			
			}
	
	}
	 
	public void documentSample(String seedTuplesLocation, String randomQueries, double maxSampleSize, Sample sample, persistentWriter pW) {
	
		seed = new HashSet<Tuple>();
		
		TupleReader reader  = new TupleReader();
		
		Tuple[] seedTuples  = reader.readTuples(seedTuplesLocation);
		
		System.out.println("Read tuples " + seedTuples.length);
		
		addToSeed(seedTuples, sample.getVersion().getCondition());
		
		System.out.println("Seed size " + seed.size());
		
		try {
			List<String> rqueries = FileUtils.readLines(new File(randomQueries));
			
			Document[] docIds = searcher.getDocuments(rqueries.toArray(new String[rqueries.size()]), -1);
			
			appendToSampleForAllTuples(docIds,sample,pW);
			   
			generateDocumentSample(maxSampleSize,sample,pW);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	
	}
	 
	private void appendToSampleForAllTuples(Document[] docIds, Sample sample, persistentWriter pW) {
		
		int numTuples = 0;
		   
		for (int index =0; index < docIds.length; index++) {
		     
			System.out.println(index + " out of: " + docIds.length);
			   	
			Tuple[] currTuples = extractionSystem.execute(/*sample.getDatabase().getId(),*/docIds[index]);
			   
			if (sample.getVersion().getCondition().isItUseful(currTuples)){
				// Documents that generated a tuple are good and so, we add them to the list of useful docs.
				addToSeed(currTuples,sample.getVersion().getCondition());
				numTuples +=  currTuples.length;
				if(!(useful.contains(docIds[index]))) {
					useful.add(docIds[index]);
					if (useful.size() < ((max_Sample_Size + max_seed_results) / 2.0)){
						sampleDocuments.add(docIds[index]);
					}
				}
			} else {

				badexamples.add(docIds[index]);

				if (badexamples.size() < ((max_Sample_Size + max_seed_results) / 2.0)){
				
					sampleDocuments.add(docIds[index]);
				
				}
			
			}
		
		}
   
		System.out.println(" --- > " + numTuples);

	}
	 
	private void appendToSampleForOnlyGoodTuples(Document[] docIds, Sample sample, persistentWriter pW) {
	
		int numTuples = 0;
	
		for (int index =0; index < docIds.length; index++) {
	
			Tuple[] currTuples = extractionSystem.execute(/*sample.getDatabase().getId(),*/docIds[index]);
		    
			if (sample.getVersion().getCondition().isItUseful(currTuples)){
	
				ArrayList<Tuple> goodTuples = new ArrayList<Tuple>();
			
				ArrayList<Tuple> badTuples  = new ArrayList<Tuple>();
			
				splitTuplesIntoGoodAndBad(currTuples, goodTuples, badTuples, sample.getVersion().getCondition());
			
				for(Tuple aCurrTuple: currTuples) {
			
					if (seed.contains(aCurrTuple)) {
			
						if(!goodTuples.contains(aCurrTuple)) {
			
							System.out.println("Adding the seed tuple to good tuples");
			
							goodTuples.add(aCurrTuple);
			
						} if(badTuples.contains(aCurrTuple)) {
			
							System.out.println("Removing the seed tuple from bad tuples");
			
							badTuples.remove(aCurrTuple);
			
						}
		
					}
	
				}
	
				addToSeed(goodTuples.toArray(new Tuple[goodTuples.size()]), sample.getVersion().getCondition());
			
				numTuples +=  goodTuples.size();
			
				if(goodTuples.size() > 0) {
			
					if(!useful.contains(docIds[index])) {
			
						useful.add(docIds[index]);
			
					}
			
				}
			
				if(badTuples.size() > 0) {
			
					if(!badexamples.contains(docIds[index])) {
			
						badexamples.add(docIds[index]);
			
					}
			
				}
			
			}
			
			sampleDocuments.add(docIds[index]);
			
		}
			
		System.out.println(" --- > Useful so far " + useful.size());
			
	}
	 
	private void splitTuplesIntoGoodAndBad(Tuple[] currTuples,
			ArrayList<Tuple> goodTuples, ArrayList<Tuple> badTuples,
			UsefulCondition condition) {
	
		for (int i = 0; i < currTuples.length; i++) {
			
			if (condition.isItUseful(currTuples[i])){
				goodTuples.add(currTuples[i]);
			} else {
				badTuples.add(currTuples[i]);
			}
			
		}
	}

	private void generateDocumentSample(double maxSampleSize, Sample sample, persistentWriter pW) {

		long seedSize = -1;
		
		long lastResult = -1;
		
		while (sampleDocuments.size() < maxSampleSize && (lastResult > 0 || seedSize != seed.size())) {
		
			System.out.println("Looping: " + sampleDocuments.size() + " , " + lastResult + " , " + seedSize);
		
			seedSize = seed.size();
		
			ArrayList<DocumentHandle> likelyUsefulDocs  = new ArrayList<DocumentHandle>();
		
			Tuple[] seedArray = (Tuple[]) seed.toArray(new Tuple[seed.size()]);
		
			TupleQueryGenerator queryGen = new TupleQueryGenerator(inferred_types,true,true,false, new ArrayList<String>(0));
		
			TextQuery[] queries = queryGen.generateQueries(seedArray,inferred_types,true,true,false);
		
			System.out.println("Nr. of queries generated from seed :" + queries.length);
		
			DocumentHandle [] docsForCurrQueries = null;//XXX FIX ! searcher.getDocuments(queries, max_seed_results);
		
			lastResult = docsForCurrQueries.length;
	
			System.out.println("Returned docs: " + lastResult);
		
			// Add to the list of likely useful documents list if it doesn't exist.
		
			for (int i=0; i < docsForCurrQueries.length; i++ ) {
		
				if (!(likelyUsefulDocs.contains(docsForCurrQueries[i])))
		
					likelyUsefulDocs.add(docsForCurrQueries[i]);
		
			}
		
			appendToSampleForAllTuples(likelyUsefulDocs.toArray(new Document[likelyUsefulDocs.size()]),sample,pW);
		
		}
		
		System.out.println("Size of the seeds set after documentSample is " + seed.size());
		
	}
	 
	
	public static void main(String[] args) {
				
		int experimentId = 0;
		
		ResultDocumentHandler rdh = new AllHrefResultDocumentHandler();
		
		NavigationHandler nh = new ClusterHeuristicNavigationHandler(experimentId);
		
		QueryResultPageHandler rpw = new TreeEditDistanceBasedWrapper();

		HTMLTagCleaner htmlTagCleaner = new HTMLCleanerBasedCleaner();

		
		OnlineDocumentHandler onlineDocumentHandler = new OnlineDocumentHandler(rpw, nh, rdh,htmlTagCleaner);
		
		
		String databaseIndex = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt";
		
		persistentWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");
				
		InteractionPersister interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/",databaseIndex,pW);
		
///*Pos: 35, Id: 28*/ String website = "http://www.mauconline.net/";
///*Pos: 7, Id: 217*/ String website = "http://www.carmeuse.com"; 
///*Pos: 22, Id: 231*/ String website = "http://diversifiedproduct.com/";
///*Pos: 24, Id: 321*/ String website = "http://joehollywood.com/";
///*Pos: 10, Id: 452*/ String website = "http://sociologically.net";
///*Pos: 37, Id: 739*/ String website = "http://northeasteden.blogspot.com/";
///*Pos: 34, Id: 790*/ String website = "http://www.paljorpublications.com/";
///*Pos: 9, Id: 1174*/ String website = "http://www.brannan.co.uk/";
///*Pos: 36, Id: 1367*/ String website = "http://www.improv.ca/";
///*Pos: 45, Id: 1387*/ String website = "http://www.avclub.com/";
///*Pos: 19, Id: 1482*/ String website = "http://www.shopcell.com/";
///*Pos: 26, Id: 1769*/ String website = "http://keep-racing.de"; //no useful documents but finishes
///*Pos: 27, Id: 2086*/ String website = "http://www.123aspx.com/";
///*Pos: 23, Id: 2098*/ String website = "http://www.infoaxon.com/";
///*Pos: 49, Id: 2167*/ String website = "http://www.canf.org/"; 
///*Pos: 41, Id: 2175*/ String website = "http://www.thecampussocialite.com/";
///*Pos: 30, Id: 2694*/ String website = "http://www.jamesandjames.com"; //no bad examples but finishes
///*Pos: 5, Id: 2746*/ String website = "http://www.time.com/";

//AND
		
//		/*Pos: 11, Id: 680*/ String website = "http://www.ddj.com/";
//		/*Pos: 12, Id: 956*/ String website = "http://www.biostat.washington.edu/";
//		/*Pos: 14, Id: 2612*/ String website = "http://micro.magnet.fsu.edu/";
//		/*Pos: 15, Id: 1947*/ String website = "http://www.worldenergy.org"; //has two follow links
//		/*Pos: 17, Id: 662*/ String website = "http://travel.state.gov/";
//		/*Pos: 18, Id: 2039*/ String website = "http://www.aminet.net/"; 
//		/*Pos: 21, Id: 2106*/ String website = "http://www.codecranker.com/"; 
//		/*Pos: 42, Id: 1269*/ String website = "http://www.eulerhermes.com/"; //&retrieve everything remain process
//		/*Pos: 47, Id: 2697*/ String website = "http://www.pokkadots.com/";
//		/*Pos: 50, Id: 767*/ String website = "http://www.muffslap.com/";
		
		
		
/*Pos: 1, Id: 350*/ String website = "http://www.snapstream.com";// NO RESULT
///*Pos: 2, Id: 2561*/ String website = "http://www.flmnh.ufl.edu/"; //script
///*Pos: 3, Id: 1345*/ String website = "http://www.concept2.co.uk"; //scripted
///*Pos: 4, Id: 1066*/ String website = "http://www.bbc.co.uk/"; //refine to category. see the site. add : "results" ?
///*Pos: 6, Id: 387*/ String website = "http://www.cosort.com/"; //searcher not working


///*Pos: 20, Id: 521*/ String website = "http://www.guardian.co.uk/"; SEARCHER BROKEN
                              

///*Pos: 25, Id: 1101*/ String website = "http://www.companionplants.com/"; //no results
///*Pos: 32, Id: 1329*/ String website = "http://www.weaselette.com/"; //does not work

///*Pos: 38, Id: 835*/ String website = "http://www.allergybegone.com/"; Gettin 503
///*Pos: 39, Id: 796*/ String website = "http://www.salafiaudio.com/"; //--------------SEE WHAT'S GOING ON IN REMOVEATTRIBUTESBASEDONSVM
///*Pos: 40, Id: 2731*/ String website = "http://www.ti.com/"; //javascript
//--HEAP/*Pos: 43, Id: 2644*/ String website = "http://www.altera.com/"; //see how the ../ works ....
///*Pos: 44, Id: 1371*/ String website = "http://www.lofte.com/"; --EMPTY
///*Pos: 48, Id: 2316*/ String website = "http://www.fairviewebenezer.org/"; ERRORS

//		String website = "";
		
		

		//HEAP	/*Pos: 13, Id: 1922*/ String website = "http://britishfiction.suite101.com/";
		// PDF	/*Pos: 16, Id: 2052*/ String website = "http://www.aiatsis.gov.au/";
//		HEAP/*Pos: 28, Id: 1474*/ String website = "http://www.cbalaw.org/";
		// HEAP	/*Pos: 29, Id: 438*/ String website = "http://www.sal.org.uk/";
//			HEAP/*Pos: 31, Id: 2146*/ String website = "http://rebelpixel.com/";
//				ERROR/*Pos: 33, Id: 1911*/ String website = "http://community.livejournal.com/";
//		HEAP/*Pos: 46, Id: 2615*/ String website = "http://www.med.yale.edu/";

		
	Database db = pW.getDatabaseByName(website);

		int index = db.getId();
		
		Searcher searcher = new OnLineSearcher(1000,"UTF-8",db,
				"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+index+".html",
				"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+index+".txt",10,3,onlineDocumentHandler,experimentId,htmlTagCleaner,interactionPersister);
			
		String[] relations = new String[]{"PersonCareer"};
		
		ContentExtractor ce = new TikaContentExtractor();
		
		ContentLoader cl = new FileContentLoader();
		
		RelationExtractionSystem extractionSystem = new OCRelationExtractionSystem(pW).createInstance(db,interactionPersister,ce,relations);//Id, relation, interactionPersister.getExtractionTable(website,Id), interactionPersister.getExtractionFolder(website,Id));
		
		int max_Sample_Size = 1000;
		
		int max_seed_results = 150;
		
		int sample_number = 1;
		
		int wload = 6;
		
		WorkloadModel wm = new WorkloadModel(wload,"/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.ds","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.wl","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadQueries","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadRelations");
		
		Version v = Version.generateInstance(VersionEnum.INDEPENDENT.name(), wm);
				
//		Sample sample = Sample.getSample(db, v, wm, sample_number,new DummySampleConfiguration(1));
		
		Sample sample = null;
		
		boolean frequency = false;
		
		boolean stemmed = false;
		
		ArrayList<String> no_filtering_fields = new ArrayList<String>();
		
//		no_filtering_fields.add("effect");
		
		ArrayList<String> inferred_types = new ArrayList<String>();
		
//		inferred_types.add("date");
	
		inferred_types.add("note");
		inferred_types.add("careertype");
		inferred_types.add("status");
		
		WordExtractorAbs usefulWE = new WordExtractor(ce,cl);
		
		WordExtractorAbs generalWE = new WordExtractor(ce,cl);
				
		BootstrappingSampleGeneration b = new BootstrappingSampleGeneration(searcher, extractionSystem, max_Sample_Size, max_seed_results, sample, pW, frequency, stemmed, no_filtering_fields, inferred_types,relations,usefulWE,generalWE);
		
		String randomqueriesFile = "/proj/dbNoBackup/pjbarrio/Experiments/QXtract/experiment1/randomQueries.txt";
		
		b.execute(wm.getTuples(), randomqueriesFile);
		
		System.out.println("DONE: " + website);
		
	}
	
}
