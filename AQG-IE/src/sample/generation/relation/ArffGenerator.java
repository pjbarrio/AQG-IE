package sample.generation.relation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import online.documentHandler.contentExtractor.impl.DummyContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentLoader.impl.SgmlContentLoader;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import sample.weka.SampleArffGenerator;
import utils.document.DocumentHandler;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import execution.workload.tuple.Tuple;
import exploration.model.Document;
import extraction.relationExtraction.RelationExtractionSystem;

public class ArffGenerator {

	private static Hashtable<Document, ArrayList<Tuple>> tuplesTable;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//Third algorithm, after Generate Document List. Continue to Reduction
		
		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
		
		int db = Integer.valueOf(args[3]); //3000
		
		int size = Integer.valueOf(args[4]);
		
		int[] spl = {Integer.valueOf(args[5])};//{1,2,3,4,5/*,6,7,8,9,10*/};
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
//		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
//				"VotingResult","ProductIssues","Quotation","PollsResult",*/"Indictment-Arrest-Trial"};
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment, pW, ieSystem, true, false, db, new SgmlContentExtraction());
		
		String[] relationses = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		DocumentHandler dh = new DocumentHandler(pW.getDatabaseById(db), 1, pW,false);
		
		int[] sizes = {1/*,2,3,4,5,6,7,8,9,10*/};
		
		for (int rel = 0; rel < relationses.length; rel++) {
			
			File relations = pW.getRelationsFile(relationses[rel]);
			
			String matchingTuplesFile = pW.getMatchingTuplesWithSources(collection,relationses[rel],tr.getName());
			
			tuplesTable = TuplesLoader.loadDocumenttuplesTuple(pW.getDatabaseById(db),matchingTuplesFile);
			
			boolean[] tuplesAsStopWords = {Boolean.valueOf(args[6])};//{true,false};
			
			for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {
				
				for (int a=0;a<spl.length; a++) {
					
					System.gc();
					
					int j = spl[a];
					
					System.out.println("Split: " + j);
					
					for (int b = 0; b < sizes.length; b++) {
						
						int i = sizes[b];
						
						int realsize = size * i;
						
						System.out.println("Size: " + realsize);
						
//						String usefulSplit = pW.getUsefulDocumentListForRelation(collection,relationses[rel],realsize,j);
//						
//						String uselessSplit = pW.getUselessDocumentListForRelation(collection,relationses[rel],realsize,j);
//
//						List<String> usefulFiles = FileUtils.readLines(new File(usefulSplit));
//						
//						List<String> uselessFiles = FileUtils.readLines(new File(uselessSplit));

						List<Document> usefulFiles = createList(pW.getUsefulDocumentExtractionForRelation(collection,relationses[rel],realsize,j,tr.getName()),dh);
						
						List<Document> uselessFiles = createList(pW.getUselessDocumentExtractionForRelation(collection,relationses[rel],realsize,j,tr.getName()),dh);
						
						File output = new File(pW.getArffModelForSplit(collection,relationses[rel],realsize,j,tuplesAsStopWords[tasw],tr.getName()));
						
						System.err.println(output);
						
//						if (output.exists())
//							continue;
//						else
							output.createNewFile();
						
						createSample(usefulFiles,uselessFiles,pW,relations, output.getAbsolutePath(),relationses[rel],tuplesAsStopWords[tasw]);
						
					}
				
				}
				
			}
			
		}
			
	}

	private static List<Document> createList(
			String documentList, DocumentHandler dh) {
		
		try {
			
			List<String> doc = FileUtils.readLines(new File(documentList));
			
			List<Document> ret = new ArrayList<Document>(doc.size());
			
			for (int i = 0; i < doc.size(); i++) {
				
				ret.add(dh.getDocument(doc.get(i)));
				
			}
			
			return ret;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	private static void createSample(List<Document> usefulFiles,
			List<Document> uselessFiles, persistentWriter pW, File relations, String outputFile, String relation, boolean tuplesAsStopWords) {
				
		WordExtractorAbs we = new WordExtractor(new DummyContentExtractor(), new SgmlContentLoader());
		
		SampleArffGenerator sarffG = new SampleArffGenerator(pW.getStopWords(),we,we);
		
		Set<Tuple> tuples = new HashSet<Tuple>();
		
		if (tuplesAsStopWords){
			
			for (Document useful : usefulFiles) {
				
				System.out.println(useful);
				
				tuples.addAll(tuplesTable.get(useful));
				
			}
			
		}
		
//NEED TO CLEAN?		usefulFiles = transform(usefulFiles);
//		
//		uselessFiles = transform(uselessFiles);
		
		uselessFiles.addAll(usefulFiles);
					
		sarffG.learn(uselessFiles, usefulFiles,outputFile,tuples,false,false,true,false);
				
	}

	private static List<String> transform(List<String> files) {
		
		List<String> tFiles = new ArrayList<String>(files.size());
		
		for (int i = 0; i < files.size(); i++) {
		
			tFiles.add(transform(files.get(i)));
			
		}
		
		return tFiles;
		
	}

	private static String transform(String string) {
		
		return string.replaceAll("Extraction", "CleanCollection").replaceAll(".rdf", "");
		
	}

}
