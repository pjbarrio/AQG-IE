package extraction.relationExtraction;

import java.io.File;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;

public class OnTheFlyRelationExtractionSystem extends RelationExtractionSystem {

	
	private int id;

	public OnTheFlyRelationExtractionSystem(int id, Database db, persistentWriter pW,
			String[] relations, Map<Document, String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor) {
		super(db, pW, relations, extractionTable, extractionFolder, contentExtractor);
		this.id = id;
	}

	@Override
	protected int getId() {
		return id;
	}

	@Override
	protected List<Tuple> extractRecentlyProcessed(String relation,
			String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void extract(String content, Writer writer) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getExtractedFormat(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<Tuple> extractProcessed(String relation, String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String generateId(Database website, String[] relations,
			InteractionPersister interactionPersister,
			ContentExtractor contentExtractor, int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected RelationExtractionSystem createInstance(Database website,
			persistentWriter pW, Map<Document, String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor,
			String... relations) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void _clear() {
		// TODO Auto-generated method stub

	}

}
