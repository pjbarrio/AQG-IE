package exploration.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sample.generation.model.SampleConfiguration;

import execution.workload.tuple.Tuple;

public class Sample {

	private static Hashtable<String, Sample> sampleTables = null;
	private Database database;
	private Version version;
	private WorkloadModel workload;
	private String id;
	private SampleConfiguration sampleConfiguration;
	private List<Document> useful;
	private List<Document> useless;
	private List<Long> myQueries;
	private Hashtable<Document, List<Tuple>> documentsTupleTable;
	private int idSample;
	private Map<Tuple, Integer> tupleTable;
	private Set<Document> myDocuments;
	private int generatedQuery;
	private int version_seed_pos;
	private int version_seed_neg;
	
	private Sample(Database database, Version version, WorkloadModel workload,
			int version_seed_pos,int version_seed_neg, String id, SampleConfiguration sampleConfiguration) {
		super();
		this.database = database;
		this.version = version;
		this.workload = workload;
		this.version_seed_pos = version_seed_pos;
		this.version_seed_neg = version_seed_neg;
		this.id = id;
		this.sampleConfiguration = sampleConfiguration;
		generatedQuery = 0;
	
	}

	public synchronized static Sample getSample(Database database, Version version, WorkloadModel workload,
			int version_seed_pos, int version_seed_neg, SampleConfiguration sampleConfiguration){
		
		String id = generateId(database,version,workload,version_seed_pos,version_seed_neg,sampleConfiguration);
		
		Sample ret = getCachedSample(id);
		
		if (ret == null){
			ret = new Sample(database,version,workload,version_seed_pos,version_seed_neg,id,sampleConfiguration);
			getSampleTables().put(id,ret);
		}
		
		return ret;
	}
	
	private synchronized static Sample getCachedSample(String id) {
		return getSampleTables().get(id);
	}

	private synchronized static Hashtable<String, Sample> getSampleTables() {
		
		if (sampleTables == null){
			sampleTables  = new Hashtable<String, Sample>();
		}
		return sampleTables;
	}

	private synchronized  static String generateId(Database database, Version version,
			WorkloadModel workload, int version_seed_pos, int version_seed_neg, SampleConfiguration sampleConfiguration) {
		return database.getId() + "-" + version.getName() + "-" + workload.getId() + "-" + version_seed_pos + "-" + version_seed_neg + "-" + sampleConfiguration.getId();
	}

	public Database getDatabase() {
		return database;
	}

	public Version getVersion() {
		return version;
	}

	public WorkloadModel getWorkload() {
		return workload;
	}

	public int getVersionSeedPos() {
		return version_seed_pos;
	}

	public int getVersionSeedNeg() {
		return version_seed_neg;
	}
	
	public boolean equals(Object o){
		Sample s = (Sample)o;
		
		return id.equals(s.id);

	}

	public boolean addUsefulDocument(Document document, SampleConfiguration sampleConfiguration) {
		if (this.getUseful().size() < sampleConfiguration.getUsefulNumber()){
			getMyUseful().add(document);
			getMyDocuments().add(document);
			return true;
		}
		return false;
	}

	private Set<Document> getMyDocuments() {
		
		if (myDocuments == null){
			myDocuments = new HashSet<Document>();
		}
		return myDocuments;
	}

	public boolean addUselessDocument(Document document, SampleConfiguration sampleConfiguration) {
		if (this.getUseless().size() < sampleConfiguration.getUselessNumber()){
			getMyUseless().add(document);
			getMyDocuments().add(document);
			return true;
		}	
		return false;
	}

	public List<Document> getUseful() {
		return getMyUseful();
	}

	private List<Document> getMyUseful() {
		
		if (useful == null){
			useful = new ArrayList<Document>(sampleConfiguration.getUsefulNumber());
		}
		return useful;
	}

	public List<Document> getUseless() {
		return getMyUseless();
	}

	private List<Document> getMyUseless() {
		if (useless == null){
			useless = new ArrayList<Document>(sampleConfiguration.getUselessNumber());
		}
		return useless;
	}

	public void addQuery(Long query) {
		getMyQueries().add(query);
	}

	private List<Long> getMyQueries() {
		if (myQueries == null){
			myQueries = new ArrayList<Long>(sampleConfiguration.getAllowedNumberOfQueries());
		}
		
		return myQueries;
	}

	public void addProcessedDocument(Document document) {
		if (!getDocumentsTupleTable().containsKey(document))
			getDocumentsTupleTable().put(document, new ArrayList<Tuple>(0));		
	}

	public Set<Document> getProcessedDocuments() {
		return getDocumentsTupleTable().keySet();
	}

	public void addTuple(Document document, Tuple tuple) {
		
		getTuples(document).add(tuple);
		
		addFrequency(tuple);
		
	}

	private void addFrequency(Tuple tuple) {
		
		Integer freq = getTupleTable().remove(tuple);
		
		if (freq == null){
			freq = 0;
		}
		
		getTupleTable().put(tuple,freq+1);
	}

	private Map<Tuple, Integer> getTupleTable() {
		
		if (tupleTable == null){
			tupleTable = new HashMap<Tuple, Integer>();
		}
		return tupleTable;
	}

	public List<Tuple> getTuples(Document document) {
		
		return getDocumentsTupleTable().get(document);

	}

	private Hashtable<Document, List<Tuple>> getDocumentsTupleTable() {
		
		if (documentsTupleTable == null){
			documentsTupleTable = new Hashtable<Document, List<Tuple>>();
		}
		return documentsTupleTable;
	}

	public SampleConfiguration getSampleConfiguration() {
		return sampleConfiguration;
	}

	public void setId(int idSample) {
		this.idSample = idSample;
	}

	public int getId() {
		
		return idSample;
	
	}

	public Set<Tuple> getTuples() {
		return getTupleTable().keySet();
	}

	public Set<Document> getDocuments() {
		return getMyDocuments();
	}

	public void reportGeneratedQuery() {
		generatedQuery++;
	}

	public int getGeneratedQueries() {
		return generatedQuery;
	}

	public int getQueriesSent() {
		return getMyQueries().size();
	}

	public int getSize() {
		return getUseful().size() + getUseless().size();
	}

	public Hashtable<Document, List<Tuple>> getTuplesTable() {
		return getDocumentsTupleTable();
	}

	public void setVersionSeedNeg(int version_seed_neg) {
		this.version_seed_neg = version_seed_neg;
	}
	
}
