package execution.model.scheduler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

import execution.dispatcher.SchedulableDispatcher;
import execution.model.policy.ExecutablePolicy;


public class FirstComeFirstServeSchedule<T extends Schedulable,S extends Schedulable,P extends ExecutablePolicy> extends Scheduler<T,S,P> {

	private ArrayList<T> elements;
	private Hashtable<T, Long> startTimes;
	private Hashtable<T, SchedulableDispatcher<S>> dispatchers;
	private Hashtable<T, Long> readyTimes;
	private long currentTime;
	private T toRemove;
	private long whenToRemove;
	private Hashtable<T,Hashtable<S,Long>> schedulable;
	
	
	public FirstComeFirstServeSchedule(boolean keepDuringUpdate,
			P executablePolicy) {
		super(keepDuringUpdate,executablePolicy);
		elements = new ArrayList<T>();
		startTimes = new Hashtable<T, Long>();
		dispatchers = new Hashtable<T, SchedulableDispatcher<S>>();
		readyTimes = new Hashtable<T, Long>();
		toRemove = null;
		whenToRemove = -1;
		schedulable = new Hashtable<T, Hashtable<S,Long>>();
		
	}

	@Override
	public void addSchedulable(T elem, long initialTime,
			SchedulableDispatcher<S> schedulableDispatcher) {
		
		elements.add(elem);
		
		startTimes.put(elem,initialTime);
		
		dispatchers.put(elem,schedulableDispatcher);
		
	}

	@Override
	public boolean hasMoreToProcess() {
		return elements.size() > 0;
	}

	@Override
	public T getNext(long currentTime) {
	
		if (toRemove != null && currentTime > whenToRemove){

			remove(toRemove);
			
			toRemove = null;
			whenToRemove = -1;

		}
		
		T ret = elements.get(0);
		
		if (!this.executablePolicy.accepts(ret)){
		
			if (this.executablePolicy.willNeverBeAccepted(ret))
				remove((T)ret);
			
			if (this.hasMoreToProcess())
				ret = getNext(currentTime);
			else
				return null;
			
		}
		if (ret != null)
			this.executablePolicy.addNew(ret);
		
		return ret;
	
	}

	@Override
	public void remove(T elem, long updatedTime) {
		
		toRemove = elem;
		
		whenToRemove = updatedTime;
		
	}

	@Override
	public void remove(T elem) {
		
		elements.remove(elem);
		startTimes.remove(elem);
		dispatchers.remove(elem);
		readyTimes.remove(elem);
		schedulable.remove(elem);
		
	}

	@Override
	public void addInitialTime(T updatedElem, long generationTime, S schedElem){

		getSchedElementsInitialTimeTable(updatedElem).put(schedElem,generationTime);
		
	}

	private Hashtable<S, Long> getSchedElementsInitialTimeTable(T updatedElem) {
		Hashtable<S,Long> ret = schedulable.get(updatedElem);
		
		if (ret == null){
			
			ret = new Hashtable<S, Long>();
			
			schedulable.put(updatedElem,ret);
			
		}
		
		return ret;
		
	}

	@Override
	public void pause(T elem) {
		
		if (!keepDuringUpdate)
			remove(elem);
		
	}

	@Override
	public void restore() {
		;
	}

	@Override
	public void setReadyTime(T elem, long readyTime) {
		readyTimes.put(elem, readyTime);
	}
	
	
	@Override
	public long getCurrentTime() {
		
		long aux = getMinimumAvailableTime(elements.get(0));
		
		currentTime = Math.max(currentTime, aux);
		
		return currentTime;
	
	}

	private long getMinimumAvailableTime(T elem) {
		
		Hashtable<S, Long> scheds = schedulable.get(elem);
		
		long min = Long.MAX_VALUE;
		
		for (Enumeration<S> e = scheds.keys(); e.hasMoreElements(); ){
			
			S schedul = e.nextElement();
			
			long val = dispatchers.get(elem).availableTime(schedul,scheds.get(schedul));
			
			if (val < min){
				min = val;
			}
			
		}
		
		return min;
	}

	@Override
	public void removeSchedulable(T elem, S schedElem) {
		
		getSchedElementsInitialTimeTable(elem).remove(schedElem);
		
	}
	
	@Override
	public Scheduler<T, S, P> newInstance(
			P executablePolicy) {
		return new FirstComeFirstServeSchedule<T, S, P>(keepDuringUpdate, executablePolicy);
	}
	
	public void setProcessedTime(long finishTime) {
		currentTime = finishTime;
	}

	@Override
	public void terminate() {
		
		elements.clear();
		startTimes.clear();
		readyTimes.clear();
		currentTime = -1;
		
		for (Entry<T, SchedulableDispatcher<S>> entry : dispatchers.entrySet()) {
			entry.getValue().terminate();
		}
		
		dispatchers.clear();
		
		for (Entry<T, Hashtable<S, Long>> entry : schedulable.entrySet()) {
			entry.getValue().clear();
		}
		
		schedulable.clear();
		
	}
}
