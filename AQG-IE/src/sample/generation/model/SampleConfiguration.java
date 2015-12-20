package sample.generation.model;

import java.util.List;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import sample.generation.model.queryPool.QueryPool;
import utils.persistence.InteractionPersister;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordExtractorAbs;
import execution.workload.querygeneration.QueryGenerator;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import extraction.relationExtraction.RelationExtractionSystem;

public class SampleConfiguration {

	private int resultsPerQuery;
	private int usefulNumber;
	private int uselessNumber;
	private int allowedNumberOfQueries;
	private int allowedNumberOfProcessedDocuments;
	private Version version;
	private int parameterId;
	private ContentExtractor contentExtractor;
	private int workloadModelId;
	private RelationExtractionSystem relationExtractionSystem;
	private WorkloadModel workloadModel;
	private ContentLoader contentloader;
	private int id;
	private int versionId;
	private InteractionPersister interactionPersister;
	private int idInformationExtractionSystem;
	private SampleExecutor sampleExecutor;
	private int idQueryPoolExecutor;
	private int idSampleGenerator;
	private boolean countsAll;
	private int idRelationConfiguration;
	private String baseCollection;
	private int docsInTraining;
	private boolean useAll;
	
	public SampleConfiguration(int id,int parameterId, int versionId, int workloadModelId, int idRelationConfiguration, int idExtractionSystem, int idQueryPoolExecutor, int idSampleGenerator, boolean useAll,
			int resultsPerQuery, int usefulNumber, int uselessNumber, int allowedNumberOfQueries, int allowedNumeberOfProcessedDocuments, boolean countsAll, String baseCollection, int docsInTrainig){
		this.id = id;
		this.parameterId = parameterId;
		this.versionId = versionId;
		this.workloadModelId = workloadModelId;
		this.idRelationConfiguration = idRelationConfiguration;
		this.idInformationExtractionSystem = idExtractionSystem;
		this.idQueryPoolExecutor = idQueryPoolExecutor;
		this.idSampleGenerator = idSampleGenerator;
		this.useAll = useAll;
		this.resultsPerQuery = resultsPerQuery;
		this.usefulNumber = usefulNumber;
		this.uselessNumber = uselessNumber;
		this.allowedNumberOfQueries = allowedNumberOfQueries;
		this.allowedNumberOfProcessedDocuments = allowedNumeberOfProcessedDocuments;
		this.countsAll = countsAll;
		this.baseCollection = baseCollection;
		this.docsInTraining = docsInTrainig;
		
	}
		
	public int getResultsPerQuery() {
		
		return resultsPerQuery;
		
	}

	public boolean keepProcessing(Sample sample) {
		
		if (sample.getQueriesSent() >= allowedNumberOfQueries)
			return false;
		
		if (ready(sample)){
			return false;
		}
		if (sample.getProcessedDocuments().size() >= allowedNumberOfProcessedDocuments)
			return false;
					
		return true;
		
	}

	public boolean ready(Sample sample) {
		return sample.getUseful().size() == usefulNumber && sample.getUseless().size() == uselessNumber;
	}

	public int getId() {
		return id;
	}

	public int getUsefulNumber() {
		return usefulNumber;
	}

	public int getUselessNumber() {
		return uselessNumber;
	}

	public int getParameterId() {
		return parameterId;
	}

	public void setContentExtractor(ContentExtractor ce) {
		this.contentExtractor = ce;		
	}

	public int getWorkloadModelId() {
		return workloadModelId;
	}

	public void setInformationExtractionSystem(
			RelationExtractionSystem res) {
		this.relationExtractionSystem = res;		
	}

	public void setWorkloadModel(WorkloadModel wm) {
		this.workloadModel = wm;		
	}

	public ContentLoader getContentLoader() {
		return contentloader;
	}

	public ContentExtractor getContentExtractor() {
		return contentExtractor;
	}

	public RelationExtractionSystem getRelationExtractionSystem() {
		return relationExtractionSystem;
	}

	public Version getVersion() {
		return version;
	}

	public void setContentLoader(ContentLoader contentLoader) {
		this.contentloader = contentLoader;
	}

	public WorkloadModel getWorkloadModel() {
		return workloadModel;
	}

	public int getVersionId() {
		return versionId;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public InteractionPersister getInteractionPersister() {
		return interactionPersister;
	}

	public void setInteractionPersister(
			InteractionPersister interactionPersister) {
		this.interactionPersister = interactionPersister;
	}

	public int getExtractionSystemId() {
		return idInformationExtractionSystem;
	}

	public SampleConfiguration createReducedCopy(int usefulDocuments,
			int uselessDocuments) {
		
		SampleConfiguration ret = new SampleConfiguration(id,parameterId,versionId,workloadModelId,idRelationConfiguration,idInformationExtractionSystem,idQueryPoolExecutor,idSampleGenerator,useAll,resultsPerQuery,usefulDocuments,uselessDocuments,
				allowedNumberOfQueries,allowedNumberOfProcessedDocuments, countsAll,baseCollection,docsInTraining);

		ret.version = this.version;
		ret.contentExtractor = this.contentExtractor;
		ret.idInformationExtractionSystem = this.idInformationExtractionSystem;
		ret.idQueryPoolExecutor = this.idQueryPoolExecutor;
		ret.idSampleGenerator = idSampleGenerator;
		ret.workloadModel = this.workloadModel;
		ret.contentloader = this.contentloader;
		ret.interactionPersister = this.interactionPersister;
		
		return ret;
		
	}

	public void setSampleExecutor(SampleExecutor sampleExecutor) {
		this.sampleExecutor = sampleExecutor;
	}

	public SampleExecutor getSampleExecutor() {
		return sampleExecutor;
	}
	
	public int getAllowedNumberOfQueries(){
		return allowedNumberOfQueries;
	}

	public int getQueryPoolExecutor() {
		return idQueryPoolExecutor;
	}

	public int getSampleGenerator() {
		return idSampleGenerator;
	}

	public boolean countsAll() {
		return countsAll;
	}

	public int getRelationConfiguration() {
		return idRelationConfiguration;
	}

	public String getBaseCollection() {
		return baseCollection;
	}

	public int getDocsInTraining() {
		return docsInTraining;
	}

	public boolean useAll() {
		return useAll;
	}

	public int getAllowedNumberOfDocuments() {
		return allowedNumberOfProcessedDocuments;
	}

}
