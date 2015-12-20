package execution.model.policy;

import execution.model.parameters.Parametrizable;
import exploration.model.Database;
import exploration.model.Evaluation;
import exploration.model.Query;
import exploration.model.enumerations.ExecutionAlternativeEnum;

public class ExecutionPolicy {

	private int databasesToContact;
	private int queriesPerDatabase;
	private double queriesASecond;
	private int queryProcessingTime;
	private int totalDocumentsToRetrieve;
	private int documentsPerQuery;
	private double extractionsASecond;
	private int extractionTime;
	private int retrievalTime;
	private int informationExtractionInstances;
	private boolean isIESequential;
	private boolean isQSequential;

	public ExecutionPolicy(Integer databasesToContact, Integer queriesPerDatabase, 
			Double queriesASecond, Integer queryProcessingTime,
			Integer totalDocumentsToRetrieve, Integer documentsPerQuery,
			Double extractionASecond, Integer extractionTime, Integer retrievalTime,
			Integer informationExtractionInstances, boolean isIESequential, boolean isQSequential) {
		
		this.databasesToContact = databasesToContact;
		this.queriesPerDatabase = queriesPerDatabase;
		this.queriesASecond = queriesASecond;
		this.queryProcessingTime = queryProcessingTime;
		this.totalDocumentsToRetrieve = totalDocumentsToRetrieve;
		this.documentsPerQuery = documentsPerQuery;
		this.extractionsASecond = extractionASecond;
		this.extractionTime = extractionTime;
		this.retrievalTime = retrievalTime;
		this.informationExtractionInstances = informationExtractionInstances;
		this.isIESequential = isIESequential;
		this.isQSequential = isQSequential;
		
//		informationExtractorDispatcher = new Dispatcher(extractionsASecond, this.extractionTime);

//		submittedDocuments = 0;
		
	}

	public static ExecutionPolicy generateInstance(Parametrizable parametrizable) {
			
		Integer databasesToContact = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.DATABASES_TO_CONTACT).getString());
		Integer queriesPerDatabase = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.QUERIES_PER_DATABASE).getString());
		Double queriesASecond = Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.QUERIES_A_SECOND).getString());
		Integer queryProcessingTime = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.QUERY_PROCESSING_TIME).getString());
		Integer totalDocumentsToRetrieve = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.TOTAL_DOCUMENTS_TO_RETRIEVE).getString());
		Integer DocumentsPerQuery = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.DATABASE_LIMIT).getString());
		Double ExtractionASecond = Double.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.DOCUMENTS_TO_EXTRACT_A_SECOND).getString());
		Integer ExtractionTime = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.EXTRACTION_TIME).getString());
		Integer RetrievalTime = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.RETRIEVAL_TIME).getString());
		Integer informationExtractionInstances = Integer.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.IE_INSTANCES).getString());
		Boolean isIESequential = Boolean.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.IE_SEQUENTIAL).getString());
		Boolean isQSequential = Boolean.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.Q_SEQUENTIAL).getString());
				
		return new ExecutionPolicy(databasesToContact,queriesPerDatabase,queriesASecond,
				queryProcessingTime,totalDocumentsToRetrieve,DocumentsPerQuery,ExtractionASecond,ExtractionTime,RetrievalTime,
				informationExtractionInstances, isIESequential, isQSequential);
		
	}


	public int getDatabaseLimit(Database database) {
		
		//TODO it's a percentage.
		
		return documentsPerQuery;
	}


	public long getQueryIssuingTime() {
		return queryProcessingTime;
	}


	public long getRetrievalTime(String document) {
		return retrievalTime;
	}

	public long getRetrievedDocumentReadyTime(long queryFinishingTime, Query query,
			Evaluation evaluation, long position) {

		return queryFinishingTime + position*retrievalTime;

	}

	public int getDatabasesToContact(){
		return databasesToContact;
	}
	
	public int getRetrievablebyDatabase(Database database) {
		return totalDocumentsToRetrieve;
	}

	public int NumberOfInformationExtractionSystems() {
		return informationExtractionInstances;
	}

	public boolean isExtractionSequential() {
		return isIESequential;
	}

	public double getExtractionsASecond() {
		return extractionsASecond;
	}

	public long getExtractionTime() {
		return extractionTime;
	}

	public double getQueriesAtAtaTime() {
		return queriesASecond;
	}

	public boolean isQueryingSequential() {
		return isQSequential;
	}

	public long getQueriesPerDatabase(Database database) {
		return queriesPerDatabase;
	}

}
