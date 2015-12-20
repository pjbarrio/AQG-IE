package domain.caching.entity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import online.documentHandler.contentExtractor.ContentExtractor;

import utils.clock.Clock;
import utils.counter.Counter;
import utils.persistence.persistentWriter;

import etxt2db.api.ClassificationExecutor;
import etxt2db.api.ClassificationExecutor.ClassifiedSpan;
import etxt2db.api.ClassificationModel;
import exploration.model.Database;
import extraction.net.extractors.EntityExtractor;

public class CachExtractRunnable implements Runnable {

	private static final int MAX_SIZE = 15000;
	private static final int MAX_SEGMENTS = 10;
	private persistentWriter pW;
	private ContentExtractor ce;
	private String content;
	private Database database;
	private long idDocument;
	private EntityExtractor entityExtractor;
	private Map<String, List<ClassifiedSpan>> res;

	public CachExtractRunnable(Database database,EntityExtractor entityExtractor, long idDocument, String content,
			persistentWriter pW, ContentExtractor ce, Map<String,List<ClassifiedSpan>> result) {
		this.database = database;
		this.entityExtractor = entityExtractor;
		this.idDocument = idDocument;
		this.pW = pW;
		this.ce = ce;
		this.content = content;
		this.res = result;
	}

	@Override
	public void run() {
						
		String id = database.getId() + "-" + idDocument + "-" + entityExtractor;
		
		Clock.startTime(id);
		
		Map<String, List<ClassifiedSpan>> result;
		
		if (entityExtractor.getId() != 15 && content.length() > MAX_SIZE){
			
			result = new HashMap<String, List<ClassifiedSpan>>();
			
			System.out.println("Splitting document content...");
			
			List<String> segments = generateSegments(content,MAX_SIZE);
			
			int actualOffset = 0;
			
			for (int i = 0; i < segments.size() && i < MAX_SEGMENTS; i++) {
				
				System.out.println("Processing segment: " + i + " out of " + segments.size() + " of document: " + idDocument + " in " + database.getId());
				
				Map<String, List<ClassifiedSpan>> aux = entityExtractor.getClassifiedSpans(segments.get(i));
				
				for (Entry<String, List<ClassifiedSpan>> entry : aux.entrySet()) {
					
					List<ClassifiedSpan> list = result.get(entry.getKey());
					
					if (list == null){
						
						list = new ArrayList<ClassifiedSpan>();
						
						result.put(entry.getKey(), list);
						
					}
					
					for (ClassifiedSpan classifiedSpan : entry.getValue()) {
						
						addOffset(classifiedSpan,actualOffset);
						
						list.add(classifiedSpan);
						
					}
					
				}
				
				actualOffset += segments.get(i).length();
			}
			
		}else{
		
			result = entityExtractor.getClassifiedSpans(content);
			
		}
			 
		
		Clock.stopTime(id);
		
		long time = Clock.getMeasuredTime(id);
		
		for (Entry<String,List<ClassifiedSpan>> entry : result.entrySet()) {
			
			res.put(entry.getKey(),entry.getValue());
			
			List<ClassifiedSpan> spans = entry.getValue();
			
			int entityType = entityExtractor.getTagId(entry.getKey());
			
			for (int j = 0; j < spans.size(); j++) {
				
//				System.out.println("Extraction: " + entityType + " - " + content.substring(spans.get(j).getStart(), spans.get(j).getEnd()));
				
				pW.prepareEntity(database.getId(),idDocument,ce,entityExtractor.getId(),entityType,spans.get(j).getStart(),spans.get(j).getEnd());
				
			}
			
		}

		pW.prepareExtractedDocument(database.getId(),idDocument,ce,entityExtractor.getId(),entityExtractor.getTagIds(),time);
		
	}

	private void addOffset(ClassifiedSpan classifiedSpan,
			int offset) {
		classifiedSpan.setStart(classifiedSpan.getStart() + offset);
		classifiedSpan.setEnd(classifiedSpan.getEnd() + offset);
	}

	private List<String> generateSegments(String content, int maxSize) {
		
		List<String> ret = new ArrayList<String>((int)Math.ceil((double)content.length() / (double)maxSize));
		
		int actualOffset = 0;
		
		while (actualOffset < content.length()){
			ret.add(content.substring(actualOffset, Math.min(actualOffset+maxSize, content.length())));
			actualOffset+=maxSize;
		}
		
		return ret;
	}

}
