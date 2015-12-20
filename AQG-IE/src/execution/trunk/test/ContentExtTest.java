package execution.trunk.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;
import etxt2db.api.ClassificationExecutor.ClassifiedSpan;
import exploration.model.Document;
import extraction.net.extractors.EntityExtractor;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.document.DocumentHandler;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;

public class ContentExtTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static void main(String[] args) throws IOException, ClassCastException, ClassNotFoundException {
		
		String file = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/2/847/RESULTS/CHNH/TEDW/ALLLINKS/main/0/17.html";
		
		String content = FileUtils.readFileToString(new File(file));
		
		ContentExtractor ce = new TikaContentExtractor();

		String exCont = ce.extractContent(content);
		
		System.out.println(content.length() + " - " + exCont.length());
		
//		System.out.println(exCont);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
//		
		EntityExtractor extr = RelationConfiguration.createEntityExtractor(pW, 4);
		
		Map<String, List<ClassifiedSpan>> list = extr.getClassifiedSpans(exCont);
		
		for (Entry<String,List<ClassifiedSpan>> entry : list.entrySet()) {
			
			System.out.println("Extracting: " + entry.getKey() + " - " + entry.getValue().toString());
			
		}
		
//		
//		DocumentHandler dh = new DocumentHandler(pW.getDatabaseById(2523), -1, pW,true);
//		
//		System.out.println("Loading Works...");
//		
//		List<Document> docs = new ArrayList<Document>(dh.getDocuments());
//		
//		Collections.shuffle(docs);
//		
//		for (Document doc : docs) {
//			
//			if (doc.getId() != 927)
//				continue;
//			
//			Map<String, List<ClassifiedSpan>> list = extr.getClassifiedSpans(ce.extractContent(doc.getContent()));
//			
//			
//			
//			for (Entry<String,List<ClassifiedSpan>> entry : list.entrySet()) {
//				
//				System.out.println("Extracting: " + doc.getId() + " - " + entry.getKey() + " - " + entry.getValue().toString());
//				
//			}
//			
//		}
		
		
	}

}
