package extraction.relationExtraction.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.entity.Entity;
import edu.columbia.cs.ref.model.re.Model;
import edu.columbia.cs.ref.tool.io.CoreReader;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import extraction.relationExtraction.RelationExtractionSystem;

public class LocalCachedRelationExtractionSystem extends
		RelationExtractionSystem {

	private int id;
	private String name;
	private String fileModel;
	private Model model;

	public LocalCachedRelationExtractionSystem(Database db,persistentWriter pW,
			String[] relations, Map<Document, String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor, int id, String name, String fileModel) {
		super(db,pW, relations, extractionTable, extractionFolder, contentExtractor);
		this.id = id;
		this.name = name;
		try {
			model = (Model) edu.columbia.cs.ref.tool.io.SerializationHelper.read(fileModel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public LocalCachedRelationExtractionSystem(persistentWriter pW, int idRelationConfiguration, int idInformationExtractionSystem, String fileModel){
		super(pW);
		this.id = pW.getRelationExtractionSystemId(idRelationConfiguration, idInformationExtractionSystem);
		this.name = pW.getInformationExtractionSystemName(id);
		this.fileModel = fileModel;
		
	}
	
	@Override
	protected int getId() {
		return id;
	}

	@Override
	protected List<Tuple> extractRecentlyProcessed(String relation,
			String string) {
		//it's cached, so it should never get in here.
		return null;
	}

	@Override
	protected void extract(String content, Writer writer) {
	
		//it's cached, so it should never get in here.

	}

	@Override
	protected String getExtractedFormat() {
		return "os";
	}

	@Override
	protected synchronized List<Tuple> extractProcessed(String relation, String identifier){
		
		List<Tuple> t = new ArrayList<Tuple>();
		
		Set<OperableStructure> operableStructures;
		
		try {
			
			operableStructures = CoreReader.readOperableStructures(identifier);
			
			for (OperableStructure operableStructure : operableStructures) {
				
				if (model.predictLabel(operableStructure).contains(relation)){
					
					t.add(generateTuple(operableStructure));
					
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
		return t;
	}

	public synchronized static Tuple generateTuple(OperableStructure operableStructure) {
		
		Tuple ret = new Tuple();
		
		Entity[] entities = operableStructure.getCandidateSentence().getEntities();
		
		for (int i = 0; i < entities.length; i++) {
			
			ret.setTupleField(entities[i].getEntityType(), entities[i].getValue());
			
		}
		
		return ret;
	}
	

	@Override
	protected String generateId(Database database, String[] relations,
			InteractionPersister interactionPersister,
			ContentExtractor contentExtractor, int id) {
		
		//it's general, though relation will be always length = 1
		
		return new String(id + " - " + name + database.getId()+"-" + Arrays.toString(relations) + "-" +interactionPersister.getName()+"-"+contentExtractor.getName());
	}

	@Override
	protected RelationExtractionSystem createInstance(Database db,persistentWriter pW, Map<Document, String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor, String... relations) {
		return new LocalCachedRelationExtractionSystem(db, pW, relations, extractionTable, extractionFolder, contentExtractor,id,name, fileModel);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected void _clear() {
			
		;//nothing to clean.
	
	}

}
