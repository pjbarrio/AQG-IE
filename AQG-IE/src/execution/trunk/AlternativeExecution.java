package execution.trunk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.List;

import utils.id.Idhandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import execution.model.policy.LimitedNumberPolicy;
import execution.model.scheduler.Scheduler;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Evaluation;
import exploration.model.Execution;
import exploration.model.ExecutionAlternative;
import exploration.model.Query;

public class AlternativeExecution {

	private static Execution execution;
	private static Scheduler<Evaluation, Query, LimitedNumberPolicy> scheduler;
	private static List<Evaluation> evaluation;
	private static long currentTime;
	private static Evaluation eval;
	private static Evaluation updatedEval;
	private static Scheduler<Query, Query, LimitedNumberPolicy> qSched;
	private static Query query;
	private static long finishTime;
	private static Document document;
	private static long finishingTime;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		System.setErr(new PrintStream(new File("/proj/dbNoBackup/pjbarrio/AlternativeExecution-1-to-43.txt")));
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
//		pW.cleanForExperiments();
//		
//		System.exit(0);
		
		List<ExecutionAlternative> executions = pW.getActiveExecutionAlternatives();
		
		for (ExecutionAlternative executionAlternative : executions){
			
			executionAlternative.selectInitialEvaluations(pW,null);
						
			while (executionAlternative.hasMoreInitialEvaluations()){
				
				Runtime.getRuntime().gc();
				
				execution = pW.insertExecution(executionAlternative);
				
				scheduler = execution.getScheduler();
				
				evaluation = executionAlternative.getNextEvaluations();
				
				for (Evaluation eval : evaluation) {
					
					execution.saveInitialEvaluation(eval);
					
					scheduler.addSchedulable(eval,0,execution.getDispatcher(eval.getEvaluableDatabase()));
					
					for (Query query : eval.getCombination().getQueries()){
						
						scheduler.addInitialTime(eval,query.getGenerationTime(),query);
						
					}
					
					scheduler.setReadyTime(eval,execution.getReadyTime(eval));
					
				}
				
				currentTime = 0;
				
				while (scheduler.hasMoreToProcess() && (eval = scheduler.getNext(currentTime))!=null ){
					
					//TODO ask for global update.
										
					if (execution.isStillOn(eval)){
					
						if (execution.needsAnUpdate(eval)){
							
							scheduler.pause(eval);
							
							updatedEval = execution.update(eval);
							
							scheduler.remove(eval,currentTime + execution.getReadyTime(updatedEval)); //leave it ready to be removed...
							
							scheduler.addSchedulable(updatedEval,currentTime,execution.getDispatcher(updatedEval.getEvaluableDatabase()));
							
							for (Query query : updatedEval.getCombination().getQueries()){
								
								scheduler.addInitialTime(updatedEval,query.getGenerationTime(),query);
								
							}
							
							scheduler.setReadyTime(updatedEval, currentTime + execution.getReadyTime(updatedEval));
							
							scheduler.restore();
							
						}

						qSched = execution.getQueryScheduler(eval);
						
						if (qSched.hasMoreToProcess() && (query = qSched.getNext(currentTime))!= null){
							
							currentTime = qSched.getCurrentTime();
							
							finishTime = execution.getDispatcher(eval.getEvaluableDatabase()).dispatch(query,currentTime);
							
							qSched.setProcessedTime(finishTime);
							
							execution.submitQuery(query,eval);
							
							int auxVarToGarbageCollection = 0;
							
							if (execution.isStillOn(eval,query)){
								
								//submit document...
								
								auxVarToGarbageCollection++;
								
								document = execution.getNextDocument(eval,query);
							
								finishingTime = execution.getInformationExtractionDispatcher(eval,query).dispatch(eval.getEvaluableDatabase() + "-" + document.toString(),execution.getNextDownloadedTime(finishTime,query,eval));
								
								execution.writeProcessedDocuments(query,eval,finishingTime);
								
								currentTime = execution.getCurrentTime();
								
								System.out.println("Processing..." + currentTime + " - " + finishingTime);
								
								if (auxVarToGarbageCollection >= 200){
									Runtime.getRuntime().gc();
									auxVarToGarbageCollection = 0;
								}
								
							} else {
				
								execution.setAsProcessed(eval,query);
								
								qSched.remove(query);
								
								scheduler.removeSchedulable(eval,query);
							
								Runtime.getRuntime().gc();
								
							}
							
						} else {
							
							execution.setAsProcessed(eval);
							
							scheduler.remove(eval);
							
							Runtime.getRuntime().gc();
							
						}
						
					} else {
						
						execution.setAsProcessed(eval);
						
						scheduler.remove(eval);
					
					}
					
				}
					
				execution.cleanExecution();
				
			}
						
		}

		System.out.println("DONE!");

		pW.endAlgorithm();

		
	}

	

}
