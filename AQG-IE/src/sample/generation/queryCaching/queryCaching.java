package sample.generation.queryCaching;

/* QxtractProcessor.java
*
* Created on January 28, 2005, 9:39 PM
*/

import init.initialization.similarity.FileSelector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
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
import utils.query.QueryParser;
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
//This is an old implementation.

public class queryCaching {

	private static DiskBasedInteractionPersister interactionPersister;
	private static ClusterHeuristicNavigationHandler nh;
	private static int experimentId;
	private static Database db;
	private HashSet<Tuple> seed;
	private ArrayList<Document> sampleDocuments;
	private ArrayList<Document> useful;
	private ArrayList<Document> badexamples;
	private RelationExtractionSystem extractionSystem;
	private Searcher searcher;
	private List<String> must;
	private List<String> must_not;
	
	private double max_Sample_Size;
	private persistentWriter pW;
	private Sample sample;
	private double max_seed_results;
	 
	 public queryCaching(Searcher searcher, RelationExtractionSystem extractionSystem, int max_Sample_Size, int max_seed_results, Sample sample, persistentWriter pW) {
	   
		 must = new ArrayList<String>();
		 must_not = new ArrayList<String>();
		 
		 sampleDocuments    = new ArrayList<Document>();
	   useful    = new ArrayList<Document>();
	   badexamples = new ArrayList<Document>();
	   this.searcher = searcher;
	   this.extractionSystem = extractionSystem;
	   this.max_Sample_Size = max_Sample_Size;
	   this.max_seed_results = max_seed_results;
	   this.pW = pW;
	   this.sample = sample;
	 }
	 
	 public void execute(String randomQueries) {
	 
		documentSample(randomQueries, max_Sample_Size,sample,pW);
		
	}
	 
	
	private void addToSeed(Tuple[] tuples, UsefulCondition usefulCondition) {
		
		if (tuples == null) return;
		
			for (int i=0; i< tuples.length; i++) {
		
				if (tuples[i] != null)
				
					if (usefulCondition.matchCondition(tuples[i]))
					
						seed.add(tuples[i]);
			
			}
	
	}
	 
	public void documentSample(String randomQueries, double maxSampleSize, Sample sample, persistentWriter pW) {
	
		try {
			List<String> rrqueries = FileUtils.readLines(new File(randomQueries));
			
			List<String> rqueries = new ArrayList<String>();

			int nnq = 0;

			int nq = 0;
			
			for (String quer : rrqueries) {
				
				if (nnq % 25 == 0)
					System.gc();
					
				if (!hasProcessed(quer)){
					
					System.out.println(nq + " - " + nnq + " - " + quer);
					
					nq++;
					
					rqueries.add(quer);
					
					
					
				}
				
				nnq++;
			}

			
			Collections.shuffle(rqueries);
			
			Document[] docIds = null; //XXX FIX ! searcher.getDocuments(rqueries.toArray(new String[rqueries.size()]), -1);
			
			appendToSampleForAllTuples(docIds,sample,pW);
			   
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	 
	private boolean hasProcessed(String quer) {
		
		must.clear();
		
		must_not.clear();
		
		QueryParser.parseQuery(quer, must, must_not);
		
		TextQuery texQuery= new TextQuery(must);
		
		return interactionPersister.hasProcessedQuery(experimentId, db, texQuery, nh.getName());
		
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
	 
	 
	public static void main(String[] args) throws FileNotFoundException {
				
		experimentId = 0;
		
		ResultDocumentHandler rdh = new AllHrefResultDocumentHandler();
		
		nh = new ClusterHeuristicNavigationHandler(experimentId);
		
		QueryResultPageHandler rpw = new TreeEditDistanceBasedWrapper();
		
		HTMLTagCleaner htmlTagCleaner = new HTMLCleanerBasedCleaner();
		
		OnlineDocumentHandler onlineDocumentHandler = new OnlineDocumentHandler(rpw, nh, rdh,htmlTagCleaner);
		
		
		String databaseIndex = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt";
		
		persistentWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");
				
		interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/",databaseIndex,pW);
		
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

	String[] websites = {"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/",
			"http://joehollywood.com/",/*"http://sociologically.net",*/"http://northeasteden.blogspot.com/",
			"http://www.paljorpublications.com/","http://www.brannan.co.uk/","http://www.improv.ca/",
			"http://www.avclub.com/","http://www.shopcell.com/","http://keep-racing.de",
			"http://www.123aspx.com/","http://www.infoaxon.com/","http://www.canf.org/","http://www.thecampussocialite.com/",
			"http://www.jamesandjames.com","http://www.time.com/"};	
		
	Integer websiteI = Integer.valueOf(args[0]);
		
	String website = websites[websiteI];
	
	db = pW.getDatabaseByName(website);

		int index = db.getId();
		
//		System.setErr(new PrintStream(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/queries/output/" + index + ".err")));
		
		Searcher searcher = new OnLineSearcher(1000,"UTF-8",db,
				"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+index+".html",
				"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+index+".txt",10,3,onlineDocumentHandler,experimentId,htmlTagCleaner,interactionPersister);
			
		String[] relations = new String[]{"PersonCareer"};
		
		ContentExtractor ce = new TikaContentExtractor();
		
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
		
		queryCaching b = new queryCaching(searcher, extractionSystem, max_Sample_Size, max_seed_results, sample, pW);
		
		String randomqueriesFile = pW.getQueries(sample);
		
		b.execute(randomqueriesFile);
		
		System.out.println("DONE: " + website);
		
	}
	
}
