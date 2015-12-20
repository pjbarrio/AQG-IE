package extraction.relationExtraction.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import opennlp.tools.util.InvalidFormatException;

import org.apache.tools.ant.filters.StringInputStream;

import com.google.gdata.util.common.base.Pair;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

import domain.caching.candidatesentence.CachCandidateSentenceRunnable;
import domain.caching.candidatesentence.CachCandidateSentencesRelationship;
import domain.caching.candidatesentence.tool.CandidateSentenceGenerator;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import domain.caching.entity.CachEntities;
import domain.caching.entity.CachExtractRunnable;
import domain.caching.operablestructure.OperableStructureGeneratorRunnable;
import domain.caching.tuple.TupleExtractorRunnable;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.document.DocumentHandler;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.re.Model;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import etxt2db.api.ClassificationExecutor.ClassifiedSpan;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import extraction.net.extractors.EntityExtractor;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.tool.ConcurrentLoad;

public class TupleRelationExtractionSystem extends RelationExtractionSystem {

	class EntityExtractorRunnable implements Runnable{

		private persistentWriter pW;
		private Document document;
		private EntityExtractor entityExtractor;
		private ContentExtractor contentExtractor;
		private Database database;
		private Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> ret;
		private Map<String, Integer> entityTable;
		
		public EntityExtractorRunnable(persistentWriter pW, Database database, Document document, EntityExtractor entityExtractor, ContentExtractor contentExtractor, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> ret, Map<String, Integer> entityTable) {
			this.pW = pW;
			this.database = database;
			this.document = document;
			this.entityExtractor = entityExtractor;
			this.contentExtractor = contentExtractor;
			this.ret = ret;
			this.entityTable = entityTable;
		}
		@Override
		public void run() {
			
			if (pW.hasExtractedEntities(database.getId(), document.getId(), entityExtractor.getTagIds(),entityExtractor.getId(), contentExtractor)){
				
				int[] tagId = entityExtractor.getTagIds();
				
				for(int t = 0 ; t < tagId.length ; t++){
					synchronized (ret){
						ret.put(entityExtractor.getTagIds()[t],pW.getExtractedEntities(db.getId(), document.getId(), tagId[t],entityExtractor.getId(), contentExtractor));
					}
				}
			}else{
				
				Map<String, List<ClassifiedSpan>> map = new HashMap<String,List<ClassifiedSpan>>(0);
				
				Thread t = new Thread(new CachExtractRunnable(database,entityExtractor, document.getId(),document.getContent(contentExtractor,pW),pW,contentExtractor,map));
				
				t.start();		
	
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				for (Entry<String, List<ClassifiedSpan>> entry : map.entrySet()){
					synchronized (ret) {
						ret.put(entityTable.get(entry.getKey()),generateList(entry.getValue()));
					}
					
				}
				
			}
			
		}
		
	}
	
	private int id;
	private String name;
	private Map<String,List<Tuple>> intTable;
	private int relationConf;
	private int idInformationExtraction;
	private int informationExtractionBase = -1;
	private Model ri;
	private StructureConfiguration structureConfiguration;
	private Set<String> tags;
	private Set<RelationshipType> relationshipTypes;
	private CandidateSentenceGenerator generator;
	private Map<Integer, String> entitiesTable;
	private EntityExtractor[] extractors;
	private boolean cached;
	private int informationExtractionModel = -1;
	private boolean takeCareOfStoring;
	private static final List<Tuple> emptyList = new ArrayList<Tuple>(0);
	private static long ids = 0;
	
	public TupleRelationExtractionSystem(Database db, persistentWriter pW,
			String[] relations, Map<Document, String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor, int id, int relationConf, int idInformationExtraction,String name, boolean cached, boolean takeCareOfStoring) {
		super(db, pW, relations, extractionTable, extractionFolder, contentExtractor);
		this.id = id;
		this.relationConf = relationConf;
		this.idInformationExtraction = idInformationExtraction;
		this.name = name;
		this.cached = cached;
		this.takeCareOfStoring = takeCareOfStoring;
		generateTable(pW.loadDatabaseExtractions(db,id));
	}

	private void generateTable(
			Map<String, String> extractions) {
		
		intTable = new HashMap<String, List<Tuple>>(extractions.size());
		
		for (Entry<String,String> extract : extractions.entrySet()) {
			
			intTable.put(extract.getKey(), loadTupleSet(extract.getValue()));
			
		}
		
	}

	private List<Tuple> loadTupleSet(String value) {
		
		try {
			
			InputStream buffer = new ByteArrayInputStream(Base64.decode(value));
			ObjectInput input = new ObjectInputStream ( buffer );
			List<Tuple> tuples = (List<Tuple>)input.readObject();
			buffer.close();
			input.close();
			return tuples;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Base64DecoderException e) {
			e.printStackTrace();
		}
		
		return null;
	
	}

	public TupleRelationExtractionSystem(persistentWriter pW,
			int relationConf, int idInformationExtraction, boolean cached, boolean takeCareOfStoring) {
		
		super(pW);
		this.id = pW.getRelationExtractionSystemId(relationConf,idInformationExtraction);
		this.relationConf = relationConf;
		this.idInformationExtraction = idInformationExtraction;
		this.name = pW.getInformationExtractionSystemName(id);
		this.cached = cached;
		this.takeCareOfStoring = takeCareOfStoring;
	}

	@Override
	protected int getId() {
		return id;
	}
	
	@Override
	public synchronized Tuple[] execute(Document document, String[] relations){
		
		List<Tuple> ret = new ArrayList<Tuple>();
		
		if (cached || hasProcessed(document)){
			
			for (String relation : relations) {
				
				ret.addAll(extractProcessed(relation,table.get(document)));
				
			}
			
		}else{
		
			for (int i = 0 ; i < relations.length ; i++){
				
				ret.addAll(executeNotSeen(document,relations[i]));
				
			}

		}
		
		return ret.toArray(new Tuple[ret.size()]);

	}
	
	private List<Tuple> executeNotSeen(Document document,
			String relation) {

		//prepares Internal Tuple
		
		if (informationExtractionModel == -1)
			informationExtractionModel  = RelationConfiguration.getInformationExtractionBaseIdFromTuples(idInformationExtraction);
		
		if (informationExtractionBase == -1)
			informationExtractionBase = RelationConfiguration.getCachedInformationExtractionSystemSource(idInformationExtraction);
		
		
		if (ri == null)
			ri = getRelationExtractionInstance(informationExtractionModel,relationConf);
		
		System.out.println("About to generate OS");
		
		Set<OperableStructure> opStruct = getOperableStructure(ri,document,relation,relationConf,informationExtractionBase);
		
		System.out.println("Generated OS");
		
		List<Tuple> ret = new ArrayList<Tuple>(0);
		
		System.out.println("About to generate Tup");
		
		Thread t = new Thread(new TupleExtractorRunnable(document,opStruct,ri,db,contentExtractor,pW,id,relation,ret));
		
		t.start();

		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Generate Tup");
		
		//they'll be safe when stored
		
		table.put(document,document.getFilePath(pW).getAbsolutePath());
		
		intTable.put(document.getFilePath(pW).getAbsolutePath(),ret);
		
		if (takeCareOfStoring){
			System.out.println("About to save...");
			pW.finishTupleExtractionFull();
			System.out.println("Saved...");
		}
		
		
		return ret;
	}

	private Set<OperableStructure> getOperableStructure(Model ri,
			Document document, String relation, int relationConf, int informationExtractionId) {
		
//		ask if already done? it does not make sense. If it didn't extract tuples, it didn't do operable structures...
		
		if (structureConfiguration == null)
			structureConfiguration = getStructureConfiguration(relation,informationExtractionId);
		
		System.out.println("About to generate CS");
		
		Set<CandidateSentence> candidateSentences = getCandidateSentences(relation,relationConf, document);
		
		System.out.println("Generate CS");
		
		Set<OperableStructure> ret = new HashSet<OperableStructure>(candidateSentences.size());
		
		System.out.println("Generating OS");
		
		Thread t = new Thread(new OperableStructureGeneratorRunnable(db,structureConfiguration,document,pW,contentExtractor,candidateSentences,relationConf,informationExtractionId,ret));
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Returning from OS");
		
		return ret;
		
	}

	private Set<CandidateSentence> getCandidateSentences(String relation,
			int relationConf, Document document) {
		
		if (pW.hasGeneratedCandidateSentence(db.getId(),document.getId(), relationConf, contentExtractor)){
			
			Set<CandidateSentence> ret = null;
			
			try{ //might not exist
				ret = pW.getGeneratedCandidateSentences(db.getId(),document.getId(), relationConf, contentExtractor);
			} catch (Exception e){
				ret = null;
			}
			
			if (ret != null)
				return ret;
		}
		
			if (tags == null)
				tags = RelationConfiguration.getTags(relationConf);
			
			if (relationshipTypes == null)
				relationshipTypes = CachCandidateSentencesRelationship.getRelationshipType(relationConf,tags);
			
			try {
				
				if (generator == null)
					generator = CachCandidateSentencesRelationship.createCandidateSentenceGenerator(pW,relationConf, tags, relationshipTypes);
				
				if (entitiesTable == null)
					entitiesTable = CachCandidateSentencesRelationship.getEntitiesTable(pW);
				
				System.out.println("About to generate Ent");
				
				Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> entitiesMap = createEntitiesMap(document,relationConf, pW.getEntityTypeTable());
				
				System.out.println("About to generate Ent");
				
				Set<CandidateSentence> ret = new HashSet<CandidateSentence>(0);
				
				System.out.println("Generating CS");
				
				try {
				
					Thread t = new Thread(new CachCandidateSentenceRunnable(db,generator, document.getId(),contentExtractor.extractContent(document.getContent(pW)),pW,contentExtractor, entitiesMap,entitiesTable, ret));
				
					t.start();
				
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e){//Usually a NotSuchMethodError
					
					e.printStackTrace();
					
				}
				
				System.out.println("Returning CS");
				
				return ret;
				
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 
			
			return null;

	
		
	}

	private Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> createEntitiesMap(
			Document document, int relationConf, Map<String,Integer> entityTable) {
		
		if (extractors == null)
			extractors = getExtractors(relationConf);		

		Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> ret = new HashMap<Integer, List<Pair<Long, Pair<Integer, Integer>>>>();

		for (int i = 0 ; i < extractors.length ; i++){

			List<Thread> ts = new ArrayList<Thread>(extractors.length);
			
			Thread t = new Thread(new EntityExtractorRunnable(pW,db,document,extractors[i],contentExtractor,ret,entityTable));
			
			ts.add(t);
			
			for (Thread thread : ts) {
				thread.start();
			}
			
			
			for (Thread thread : ts) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
						
		}
	
		System.out.println("Return Ent");
		
		return ret;
				
	}

	private List<Pair<Long, Pair<Integer, Integer>>> generateList(
			List<ClassifiedSpan> value) {
		
		List<Pair<Long, Pair<Integer, Integer>>> ret = new ArrayList<Pair<Long, Pair<Integer, Integer>>>(value.size());
		
		for(int i = 0; i < value.size() ; i++){
			ret.add(generateElement(value.get(i)));
		}
		
		return ret;
		
	}

	private Pair<Long, Pair<Integer, Integer>> generateElement(
			ClassifiedSpan classifiedSpan) {
		
		return new Pair<Long,Pair<Integer,Integer>>(ids++,new Pair<Integer,Integer>(classifiedSpan.getStart(),classifiedSpan.getEnd()));
		
	}

	private EntityExtractor[] getExtractors(int relationConf) {
		
		int[][] entities = RelationConfiguration.getEntities(relationConf);
		
		System.out.println("Entities: " + Arrays.toString(entities));
		
		List<Integer> entExp = new ArrayList<Integer>(entities.length); 
		
		for(int i = 0; i < entities.length ; i++){
			
			int exp = RelationConfiguration.getForExtractor(entities[i][1]);
			
			System.out.println("Experiment: " + exp);
			
			if (!entExp.contains(exp))
				entExp.add(exp);
		}
		
		EntityExtractor[] ret = new EntityExtractor[entExp.size()];
		
		for(int i = 0; i < ret.length ; i++){
			try {
				ret[i] = RelationConfiguration.createEntityExtractor(pW,entExp.get(i));
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}

	private StructureConfiguration getStructureConfiguration(String relation,
			int informationExtraction) {
		try {
			return RelationConfiguration.generateStructureConfiguration(informationExtraction);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Model getRelationExtractionInstance(int informationExtractionId, int relationConf) {
		
		return RelationConfiguration.generateRelationExtractionSystem(pW,informationExtractionId,relationConf);
		
	}

	@Override
	protected String getExtractedFormat() {
		return "ser";
	}

	@Override
	protected synchronized List<Tuple> extractProcessed(String relation, String identifier) {
		
		if (identifier == null) //probably this is always the case when it does not exist
			return emptyList ;
		
		List<Tuple> ret = intTable.get(identifier);
		
		if (ret == null)
			return emptyList;
		
		return ret; //XXX don't think I have to check for types. I'm using only one relation
		
	}

	@Override
	protected synchronized String generateId(Database database, String[] relations,
			InteractionPersister interactionPersister,
			ContentExtractor contentExtractor, int id) {
		
		return new String(id + " - " + name + database.getId()+"-" + Arrays.toString(relations) + "-" +interactionPersister.getName()+"-"+contentExtractor.getName());
		
	}

	@Override
	protected synchronized RelationExtractionSystem createInstance(Database website,
			persistentWriter pW, Map<Document, String> extractionTable,
			File extractionFolder, ContentExtractor contentExtractor,
			String... relations) {
		return new TupleRelationExtractionSystem(website, pW, relations, extractionTable, extractionFolder, contentExtractor,id,relationConf,idInformationExtraction,name,cached,takeCareOfStoring);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	protected void _clear() {
		
		intTable.clear();

	}

	@Override
	protected List<Tuple> extractRecentlyProcessed(String relation,
			String string) {
		// should never get in here
		return null;
	}

	@Override
	protected void extract(String content, Writer writer) {
		// should never get in here
		return;
		
	}

	public static void main(String[] args){
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int db = Integer.valueOf(args[0]); //3000 to 3002
		
		int relationexperiment = Integer.valueOf(args[1]); //7 to 12
		
		int ieSystem = Integer.valueOf(args[2]); //17 to 20
		
		int concurrentLoad = Integer.valueOf(args[3]);
		
		int relationConf = RelationConfiguration.getRelationConf(relationexperiment);
		
		RelationExtractionSystem tr = new TupleRelationExtractionSystem(pW, relationConf, ieSystem,false,false); //Person Career
		
		String[] relations = new String[]{RelationConfiguration.getRelationName(relationexperiment)};		
		
		
		System.out.println("Loading Works...");
		
		ContentExtractor ce = new SgmlContentExtraction();
		
		InteractionPersister interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);

		tr = tr.createInstance(pW.getDatabaseById(db), interactionPersister , ce, relations);
		
		DocumentHandler dh = new DocumentHandler(pW.getDatabaseById(db), -1, pW,true,null);
		
		List<Document> documents = new ArrayList<Document>(dh.getDocuments());
		
		int loaded = 0;
		
		for (int i = 0; i < documents.size(); i++) {
			
			if ((loaded%concurrentLoad)==0){
				
				int j = 0;
				
				List<Thread> ts = new ArrayList<Thread>();
				
				while (j<concurrentLoad && j+i < documents.size()){
					
					if ((j%10) == 0){
						System.out.println("Loading... " + j);
					}
					
					Thread t = new Thread(new ConcurrentLoad(documents.get(j+i),pW));
					
					t.start();
					
					ts.add(t);
					
					j++;
					
				}
				
				for (Thread thread : ts) {
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
			
			Document doc = documents.get(i);
			
			System.out.println("Extracting: " + doc.getId());
			
			tr.execute(doc, relations);
			
			loaded++;
			
			if ((loaded%concurrentLoad)==0){
				pW.finishTupleExtractionFull();
			}
			
		}
		
		pW.finishTupleExtractionFull(); //in case there are some that have to be stored still.
		
	}

	
	
	public Map<String, List<Tuple>> getInternalTuples() {
		return intTable;
	}

	public Map<Document, String> getTable() {
		return table;
	}
	
}
