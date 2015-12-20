package execution.model.scheduler;

import execution.dispatcher.SchedulableDispatcher;
import execution.model.policy.ExecutablePolicy;

public class RoundRobinSchedule<T extends Schedulable,S extends Schedulable,P extends ExecutablePolicy> extends Scheduler<T,S,P> {

	private long quantum;

	public RoundRobinSchedule(boolean keepDuringUpdate, P executablePolicy,
			long quantum) {
		super(keepDuringUpdate,executablePolicy);
		this.quantum = quantum;
	}

	@Override
	public void addSchedulable(T elem, long initialTime,
			SchedulableDispatcher<S> schedulableDispatcher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasMoreToProcess() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T getNext(long currentTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(T elem, long updatedTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(T elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInitialTime(T updatedEval, long generationTime, S schedElem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause(T elem) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restore() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReadyTime(T eval, long readyTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getCurrentTime() {
		
		//TODO implement
		return 0;
	}
	
	public void setProcessedTime(long finishTime) {
		//TODO implement
	}
	
}
