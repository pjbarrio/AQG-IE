package sample.generation.relation.trec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.DummyContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentLoader.ContentLoader;
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
import exploration.model.Database;
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
		
		int first = Integer.valueOf(args[0]);
		int last = Integer.valueOf(args[1]);
		
		String extractor = "OpenCalais";
		
		String collection = "TREC";
		
		int size = 5000;
		
		int[] spl = {1,2,3,4,5};//{1,2,3,4,5/*,6,7,8,9,10*/};
		
		boolean[] tuplesAsStopWords = {false};// {Boolean.valueOf(args[6])};//{true,false};
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Database db = pW.getDatabaseById(3000);
		
		int[] sizes = {1};
		
		for (int rel = first; rel < Math.min(last, GenerateDocumentsList.relations.length); rel++) {
			
			for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {
				
				for (int a=0;a<spl.length; a++) {
					
					System.gc();
					
					int j = spl[a];
					
					System.out.println("Split: " + j);
					
					for (int b = 0; b < sizes.length; b++) {
						
						int i = sizes[b];
						
						int realsize = size * i;
						
						System.out.println("Size: " + realsize);
						
						String usefulSplit = pW.getUsefulDocumentExtractionForRelation(collection,GenerateDocumentsList.relations[rel],realsize,j,extractor);
						
						String uselessSplit = pW.getUselessDocumentExtractionForRelation(collection,GenerateDocumentsList.relations[rel],realsize,j,extractor);

						List<String> usefulFiles = FileUtils.readLines(new File(usefulSplit));
						
						List<String> uselessFiles = FileUtils.readLines(new File(uselessSplit));

						File output = new File(pW.getArffModelForSplit(collection,GenerateDocumentsList.relations[rel],realsize,j,tuplesAsStopWords[tasw],extractor));
						
						System.err.println(output);

						output.createNewFile();
						
						createSample(db, usefulFiles,uselessFiles,pW,output.getAbsolutePath(),GenerateDocumentsList.relations[rel],tuplesAsStopWords[tasw]);
						
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

	private static void createSample(Database db, List<String> usefulFiles,
			List<String> uselessFiles, persistentWriter pW, String outputFile, String relation, boolean tuplesAsStopWords) {
				
		WordExtractorAbs we = new WordExtractor(new DummyContentExtractor(), new SgmlContentLoader());
		
		SampleArffGenerator sarffG = new SampleArffGenerator(pW.getStopWords(),we,we);
		
		Set<Tuple> tuples = new HashSet<Tuple>();
		
//		if (tuplesAsStopWords){
//			
//			for (String useful : usefulFiles) {
//				
//				System.out.println(useful);
//				
//				tuples.addAll(tuplesTable.get(useful));
//				
//			}
//			
//		}
		
		List<Document> usefulFilesDoc = transform(db,usefulFiles);;
		List<Document> uselessFilesDoc = transform(db,uselessFiles);;
		
		uselessFilesDoc.addAll(usefulFilesDoc);
		
		sarffG.learn(null, null, pW, uselessFilesDoc, usefulFilesDoc,outputFile,tuples,false,false,true,false);
				
	}

	private static List<Document> transform(Database db, List<String> files) {
		
		List<Document> tFiles = new ArrayList<Document>(files.size());
		
		for (int i = 0; i < files.size(); i++) {
		
			tFiles.add(transform(db, files.get(i)));
			
		}
		
		return tFiles;
		
	}

	private static Document transform(Database db, String string) {
		
		return new Document(db, new File(string));
		
	}

}
