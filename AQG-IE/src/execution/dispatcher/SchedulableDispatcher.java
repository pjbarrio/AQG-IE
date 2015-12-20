package execution.dispatcher;

import java.util.Hashtable;

import utils.dispatcher.Dispatcher;

public class SchedulableDispatcher <T> {

	private long timeConsumed;
	private Dispatcher dispatcher;
	private Hashtable<Long, Long> submitted;
	private Hashtable<T, Long> ids;
	private long lastCurrentTime;
	private boolean sequential;
	private Long retAvailableTime;

	public SchedulableDispatcher (double submissionPerUnitTime, long timeConsumed, boolean sequential){
	
		this.sequential = sequential;
		
		this.timeConsumed = timeConsumed;
		
		dispatcher = new Dispatcher(submissionPerUnitTime, timeConsumed);
		
		submitted = new Hashtable<Long, Long>();
		
		ids = new Hashtable<T, Long>();
		
		lastCurrentTime = -1;
	
	}
	
	public long dispatch(T element, long currentTime) {
		
		lastCurrentTime = currentTime;
		
		Long id = ids.get(element);

		if (id == null){
			
			id = (long)ids.size()+1;
			
			ids.put(element, id);
			
			dispatcher.submit(id, currentTime);
			
			long time = dispatcher.getProcessedTime(id);
			
			submitted.put(id, time);
			
		}
		
		return submitted.get(id);
	
	}

	public long availableTime(T element, long initialTime) {
			
		retAvailableTime = ids.get(element);
		
		if (retAvailableTime == null){
			
			dispatcher.submit(Dispatcher.TEST_ID, Math.max(lastCurrentTime,initialTime));
			
			long time = dispatcher.getProcessedTime(Dispatcher.TEST_ID);
			
			dispatcher.undo(Dispatcher.TEST_ID);
			if (!sequential)
				return time - timeConsumed;
			return time;
		}
		
		if (!sequential)
			return submitted.get(retAvailableTime) - timeConsumed;
		
		return submitted.get(retAvailableTime);
	}

	public void terminate() {
		
		submitted.clear();
		
		ids.clear();
		
	}

}
