package execution.model.scheduler;

import execution.dispatcher.SchedulableDispatcher;
import execution.model.parameters.Parametrizable;
import execution.model.policy.ExecutablePolicy;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.SchedulerEnum;

public class Scheduler<T extends Schedulable,S extends Schedulable,P extends ExecutablePolicy> {

	//we need to know if we process documents while we generate queries...
	//if not, we just use the last time available for all the evaluations...
	
	protected boolean keepDuringUpdate;
	protected P executablePolicy;
	
	public Scheduler(boolean keepDuringUpdate, P executablePolicy){
		
		this.keepDuringUpdate = keepDuringUpdate;
		this.executablePolicy = executablePolicy;
	}
	
	public Scheduler<T,S,P> getInstance(String string, Parametrizable parametrizable) {
		
		switch (SchedulerEnum.valueOf(string)) {
		
		case FCFS:
			
			return new FirstComeFirstServeSchedule<T,S,P>(keepDuringUpdate,executablePolicy);

		case ROUND_ROBIN:
			
			return new RoundRobinSchedule<T,S,P>(keepDuringUpdate,executablePolicy,Long.valueOf(parametrizable.loadParameter(ExecutionAlternativeEnum.ROUND_ROBIN_QUANTUM).getString()));
			
		default:
			
			return null;
			
		}
		
	}

	public void addSchedulable(T elem, long initialTime, SchedulableDispatcher<S> schedulableDispatcher){
		
	}

	public boolean hasMoreToProcess(){
		return false;
	}

	public T getNext(long currentTime){
		return null;
	}

	public void remove(T elem, long updatedTime){
		
	}

	public void remove(T elem){
		
	}

	public void addInitialTime(T updatedEval, long generationTime, S schedElem){
		
	}

	public void pause(T elem){
		
	}

	public void restore(){
		
	}

	public void setReadyTime(T eval, long readyTime){
		
	}

	public long getCurrentTime() {
		return 0;
	}

	public void removeSchedulable(T elem, S schedElem) {
		
	}

	public Scheduler<T, S, P> newInstance(
			P executablePolicy) {
		return null;
	}

	public void setProcessedTime(long finishTime) {
				
	}

	public void terminate() {
		;
		
	}

}
