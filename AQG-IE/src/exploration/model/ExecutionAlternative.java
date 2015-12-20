package exploration.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.navigation.utils.NavigationUtils;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;

import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import searcher.lucene.LuceneSearcher;
import utils.persistence.InteractionPersister;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import execution.model.AdaptiveStrategy;
import execution.model.FinishingStrategy;
import execution.model.Generation;
import execution.model.Source;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.algorithmSelection.AlgorithmSelection;
import execution.model.collector.StatisticsForSampleSelector;
import execution.model.policy.ExecutionPolicy;
import execution.model.policy.LimitedNumberPolicy;
import execution.model.scheduler.Scheduler;
import execution.model.updateStrategy.UpdateStrategy;
import extraction.relationExtraction.RelationExtractionSystem;


public class ExecutionAlternative {

	private Integer id;
	private Generation generation;
	private FinishingStrategy finishingStrategy;
	private AdaptiveStrategy adaptiveStrategy;
	private Source source;
	private ExecutionPolicy executionPolicy;
	private UpdateStrategy updateStrategy;
	private int parameterId;
	private Version version;
	private int workloadId;
	private DatabaseSelection databaseSelection;
	private StatisticsForSampleSelector statisticsForSampleSelector;
	private Scheduler<Evaluation, Query, LimitedNumberPolicy> scheduler;
	private List<List<Evaluation>> evaluations;
	private Scheduler<Query, Query, LimitedNumberPolicy> queryscheduler;
	private AlgorithmSelection algorithmSelection;
	private OnlineDocumentHandler odh;
	private HTMLTagCleaner htmlTagCleaner;
	private InteractionPersister interactionPersister;
	private RelationExtractionSystem informationExtraction;
	private ContentExtractor contentExtractor;
	private int versionId;
	private int extractionSystemId;
	private int idRelationConfiguration;

	private static Hashtable<String,RelationExtractionSystem> informationExtractors = new Hashtable<String, RelationExtractionSystem>();;
	private static Hashtable<Integer,Searcher> searcher = new Hashtable<Integer, Searcher>();;


	
	public ExecutionAlternative(int id, int parameterId, int version, int workloadId, int idRelationConfiguration, int extractionSystemId){
		this.id = id;
		this.parameterId = parameterId;
		this.versionId = version;
		this.workloadId = workloadId;
		this.idRelationConfiguration = idRelationConfiguration;
		this.extractionSystemId = extractionSystemId;
	}

	public int getId() {
		return id;
	}

	public Source getSource() {
		return source;
	}

	public Version getVersion() {
		return version;
	}

	public Generation getGeneration() {
		return generation;
	}

	public FinishingStrategy getFinishingStrategy() {
		return finishingStrategy;
	}

	public AdaptiveStrategy getAdaptiveStrategy() {
		return adaptiveStrategy;
	}

	public void setSource(Source source) {
		
		this.source = source;
		
	}

	public void setGeneration(Generation generation) {
		this.generation = generation;
	}

	public void setFinishingStrategy(FinishingStrategy finishingStrategy) {
		this.finishingStrategy = finishingStrategy;
	}

	public void setAdaptiveStrategy(AdaptiveStrategy adaptiveStrategy) {
		this.adaptiveStrategy = adaptiveStrategy;
	}

	public void selectInitialCombinations(List<Combination> initialCombinations, Database database) {
		
		
	}

	public void setExecutionPolicy(ExecutionPolicy executionPolicy) {
		
		this.executionPolicy = executionPolicy;
		
	}

	public ExecutionPolicy getExecutionPolicy() {
		
		return executionPolicy;
		
	}

	public void setUpdateStrategy(
			UpdateStrategy updateStrategy) {
		this.updateStrategy = updateStrategy;
		
	}

	public UpdateStrategy getUpdateStrategy() {
		
		return updateStrategy;
		
	}

	public int getParameterId() {
		
		return parameterId;
		
	}

	public int getWorkloadModelId() {
		
		return workloadId;
		
	}

	public boolean equals(Object o){
		return id.equals(((ExecutionAlternative)o).id);
	}

	public void setDatabaseSelection(DatabaseSelection databaseSelection) {
		
		this.databaseSelection = databaseSelection;
		
	}

	public DatabaseSelection getDatabaseSelection() {
		return databaseSelection;
	}

	public StatisticsForSampleSelector getSelector() {
		
		return statisticsForSampleSelector;
	
	}

	public void setSelector(StatisticsForSampleSelector selector) {
		
		this.statisticsForSampleSelector = selector;
		
	}

	public Scheduler<Evaluation, Query, LimitedNumberPolicy> getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler<Evaluation, Query, LimitedNumberPolicy> scheduler) {
		
		this.scheduler = scheduler;
		
	}

	public void setQueryScheduler(Scheduler<Query, Query, LimitedNumberPolicy> scheduler) {
		
		this.queryscheduler = scheduler;
		
	}
	
	public boolean hasMoreInitialEvaluations() {
		return evaluations.size()>0;
	}

	public void selectInitialEvaluations(persistentWriter ce, String computername) {
		
		evaluations = new ArrayList<List<Evaluation>>();
		
		List<Combination> combinations = ce.getCombinations(this.version,this.workloadId);
		
		List<Database> searchableDatabases = ce.getSearchableDatabases(computername);
		
		for (Database database : searchableDatabases) {
			
			for (Combination combination : combinations) {
				
				if (generation.filter(combination, source, database,algorithmSelection)){
					
					combination.setQueries(ce.getQueries(combination));
					
					Evaluation ev = Evaluation.getEvaluation(combination, database, executionPolicy.getDatabaseLimit(database));
					
					if (!ce.existsEvaluation(ev,this.id)){
					
						ArrayList<Evaluation> arr = new ArrayList<Evaluation>();
						
						arr.add(ev);
						
						evaluations.add(arr);

					}
					
				}
				
			}
			
		}

	}

	public List<Evaluation> getNextEvaluations() {
		
		return evaluations.remove(0);
	
	}

	public Scheduler<Query, Query, LimitedNumberPolicy> getQueryScheduler() {
		return queryscheduler;
	}

	public Searcher getSearcher(String index, Database database, String stopWords, persistentWriter pW) {
		
		Searcher ret = searcher.get(database.getId());
		
		if (ret == null){
			
			if (!database.isOnline()){
				
				ret = new LuceneSearcher(database,index,
						getExecutionPolicy().getDatabaseLimit(database),
						(long)database.getSize(),
						stopWords);
				
				
			} else {
				
				ret = new OnLineSearcher(1000,"UTF-8",database,
						"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+database.getId()+".html",
						"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+database.getId()+".txt",10,3,getOnlineDocumentHandler(),getSearchRoundId(),getHtmlTagCleaner(),getInteractionPersister(pW));
				
				
			}

			searcher.put(database.getId(), ret);

			
		}
		
		return ret;
		
	}
	
	private InteractionPersister getInteractionPersister(persistentWriter pW) {
		
		if (interactionPersister == null){
			
			interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
		}
		return interactionPersister;
	}

	private HTMLTagCleaner getHtmlTagCleaner() {
		
		if (htmlTagCleaner == null){
			
			htmlTagCleaner = new HTMLCleanerBasedCleaner();
			
		}
		
		return htmlTagCleaner;
	}

	private OnlineDocumentHandler getOnlineDocumentHandler() {
		
		if (odh == null){
			odh = new OnlineDocumentHandler(new TreeEditDistanceBasedWrapper(), new ClusterHeuristicNavigationHandler(getSearchRoundId()), new AllHrefResultDocumentHandler(),getHtmlTagCleaner());
		}
		return odh;
	}

	private int getSearchRoundId() {
		return 0;
	}

	public RelationExtractionSystem getInformationExtractor(Database evaluableDatabase, WorkloadModel workload, persistentWriter pW) {
		
		RelationExtractionSystem ret = informationExtractors.get(evaluableDatabase.getId() + " - " + workload.getId());
		
		if (ret == null){
			
//			ret = new OpenCalaisExtractor(Integer.toString(evaluableDatabase.getId()), OpenCalaisExtractor.getRelation(workload), OpenCalaisExtractor.getTable(pW,evaluableDatabase));
			
			ret = getInformationExtractionSystem().createInstance(evaluableDatabase, getInteractionPersister(pW), getContentExtractor(), workload.getRelations());
			
//			ret = new CachedInformationExtractor(Integer.toString(evaluableDatabase.getId()), evaluableDatabase, version, workload, pW);
			
			informationExtractors.put(evaluableDatabase.getId() + " - " + workload.getId(), ret);
			
		}
		
		return ret;
	}

	private RelationExtractionSystem getInformationExtractionSystem() {
		
		return informationExtraction;
	}

	private ContentExtractor getContentExtractor() {
		
		return contentExtractor;
		
	}

	public void setAlgorithmSelection(AlgorithmSelection algorithmSelection) {
		
		this.algorithmSelection = algorithmSelection;
		
	}

	public AlgorithmSelection getAlgorithmSelection() {
		return algorithmSelection;
	}

	public void setInformationExtractionSystem(
			RelationExtractionSystem informationExtraction) {
		
		this.informationExtraction = informationExtraction;
		
	}

	public void setContentExtractor(ContentExtractor contentExtractor) {
		
		this.contentExtractor = contentExtractor;
		
	}

	public void delete() {
		
		for (Entry<Integer,Searcher> entry : searcher.entrySet()) {
			
			entry.getValue().cleanSearcher();
			
		}
		
		scheduler.terminate();
		evaluations.clear();
		queryscheduler.terminate();
		
		
	}

	public int getVersionId() {
		return versionId;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public int getExtractionSystemId() {
		return extractionSystemId;
	}

	public int getRelationConfiguration() {
		return idRelationConfiguration;
	}

}
