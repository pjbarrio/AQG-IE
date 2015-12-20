package extraction.relationExtraction;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;

import org.apache.commons.io.FileUtils;

import utils.FileHandlerUtils;
import utils.clock.Clock;
import utils.execution.ExtractionTableHandler;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;

public abstract class RelationExtractionSystem {

	private String[] relations;
	protected Map<Document, String> table;
	private File extractionFolder;
	protected ContentExtractor contentExtractor;
	protected persistentWriter pW;
	private static Hashtable<String, RelationExtractionSystem> instances;
	private int nextFile;
	protected Database db;
	private File file;

	public RelationExtractionSystem(persistentWriter pW){
		this.pW = pW;
	}
	
	public RelationExtractionSystem(Database db, persistentWriter pW, String[] relations, Map<Document,String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor) {
		
		this.db = db;
		
		this.pW = pW;
		
		this.extractionFolder = extractionFolder;
		
		this.relations = relations;
		
		table = extractionTable;
		
		this.contentExtractor = contentExtractor;
		
		nextFile = getLastIndex(table.values());
		
	}

	private int getLastIndex(Collection<String> elements) {
		
		int max = -1;
		
		for (String el : elements) {
			
			el = el.replaceAll(extractionFolder.getAbsolutePath()+"/", "");
			
			el = el.replaceAll("." + getExtractedFormat(), "");
			
			Integer l = Integer.valueOf(el);
			
			if (l > max)
				max = l;
			
		}
		
		return max;
		
	}

	public Tuple[] execute(Document document){
		return execute(document,relations);
	}
	
	public Tuple[] execute(Document document, String[] relations){
		
		List<Tuple> ret = new ArrayList<Tuple>();
		
		if (hasProcessed(document)){
			
			for (String relation : relations) {
				
				ret.addAll(extractProcessed(relation,table.get(document)));
				
			}
			
			return ret.toArray(new Tuple[ret.size()]);
			
		} else{
					
			String id = Long.toString(document.getId());
			
			Clock.startTime(id);
			
			ret.addAll(executeNotSeen(document,relations));
			
			Clock.stopTime(id);
			
			long time = Clock.getMeasuredTime(id);
			
			String idString = getGeneratedId();
			
			pW.saveExtractionTime(document,getName(),time);
			
			pW.insertExtraction(getId(), document, idString,contentExtractor);
			
			table.put(document, idString);

			
		}
		
		return ret.toArray(new Tuple[ret.size()]);
		
	}

	protected String getGeneratedId() {
		return file.getAbsolutePath();
	}

	protected List<Tuple> executeNotSeen(Document document,
			String[] relations) {
		
		List<Tuple> ret = new ArrayList<Tuple>();
		
		try {
			
			file = generateNextFile();
			
			String content = document.getContent(pW);
			
			StringWriter sw = new StringWriter();
			
			extract(contentExtractor.extractContent(content),sw);
			
			FileUtils.writeStringToFile(file, sw.toString());
			
			for (String relation : relations) {
				
				ret.addAll(extractRecentlyProcessed(relation,sw.toString()));
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
		
		
	}

	protected abstract int getId();

	protected abstract List<Tuple> extractRecentlyProcessed(
			String relation, String string);

	protected abstract void extract(String content, Writer writer);

	private synchronized File generateNextFile() {
		
		nextFile++;
		
		return new File(extractionFolder,nextFile + "." + getExtractedFormat());
		
	}

	protected abstract String getExtractedFormat();

	protected boolean hasProcessed(Document document) {
		
		return table.containsKey(document);
	}

	protected abstract List<Tuple> extractProcessed(String relation, String identifier);

	public synchronized RelationExtractionSystem createInstance(Database website,
			InteractionPersister interactionPersister, ContentExtractor contentExtractor, String... relations) {
		
		String id = generateId(website,relations,interactionPersister,contentExtractor,getId());
		
		RelationExtractionSystem rel = getInstances().get(id);
		
		if (rel == null){
			rel = createInstance(website,pW,interactionPersister.getExtractionTable(website, getId(),contentExtractor),interactionPersister.getExtractionFolder(website, getName()), contentExtractor,relations);
			getInstances().put(id,rel);
		}
		
		return rel;
		
	}

	protected abstract String generateId(Database website, String[] relations,
			InteractionPersister interactionPersister,
			ContentExtractor contentExtractor, int id);

	private synchronized static Hashtable<String, RelationExtractionSystem> getInstances() {
		
		if (instances == null){
			instances = new Hashtable<String, RelationExtractionSystem>();
		}
		return instances;
	}

	protected abstract RelationExtractionSystem createInstance(Database website, persistentWriter pW, Map<Document,String> extractionTable, File extractionFolder, ContentExtractor contentExtractor, String... relations);

	public abstract String getName();

	public void clear() {
		table.clear();
		_clear();
	}

	protected abstract void _clear();
	
}
