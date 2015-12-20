package domain.caching.entity.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;

import org.apache.commons.io.FileUtils;

import domain.caching.candidatesentence.tool.RelationConfiguration;
import domain.caching.entity.CachEntities;


import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import etxt2db.api.ClassificationExecutor.ClassifiedSpan;
import extraction.net.extractors.EntityExtractor;

public class TestExtractedEntitySpan {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ClassCastException 
	 */
	public static void main(String[] args) throws ClassCastException, IOException, ClassNotFoundException {
		
		int enti = Integer.valueOf(args[0]);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		EntityExtractor extractor = RelationConfiguration.createEntityExtractor(pW, enti);
		
		List<String> files = FileUtils.readLines(new File("/local/pjbarrio/Files/enttest.txt"));
		
		for (int i = 0; i < files.size(); i++) {
			
//			String f = FileUtils.readFileToString(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/1/777/RESULTS/CHNH/TEDW/ALLLINKS/damage/0/0.html"));
			
			String f = FileUtils.readFileToString(new File(files.get(i).trim()));
			
			ContentExtractor ce = new TikaContentExtractor();
			
			String content = ce.extractContent(f);

			Map<String, List<ClassifiedSpan>> list = extractor.getClassifiedSpans(content);
			
			for (Entry<String, List<ClassifiedSpan>> string : list.entrySet()) {
				
				System.out.println(string.getKey());
				
				for (ClassifiedSpan span : string.getValue()) {
					
					System.out.println(span + " - " + content.substring(span.getStart(), span.getEnd()));
					
				}
				
			}
			
		}
		
	}

}
