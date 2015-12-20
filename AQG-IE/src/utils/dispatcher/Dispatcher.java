package utils.dispatcher;

import java.util.Hashtable;

import exploration.model.Document;

public class Dispatcher<T> {

	public static final long TEST_ID = -1;
	
	private static final int U_O_T = 1000;
	
	private double sput;

	private int issued;

	private Hashtable<Long, Long> load;
	
	private Hashtable<T, Long> times;
	
	private long tc;

	private Long l;

	private long interval;

	private long lastInterval;

	public Dispatcher(double submissionPerUnitTime, long timeConsumed){
				
		times = new Hashtable<T, Long>();
		
		load = new Hashtable<Long, Long>();
		
		sput = submissionPerUnitTime;
		
		issued = 0;
		
		tc = timeConsumed;
	
	}
	
	public void submit(T sched, long time){
	
		issued++;
		
		interval = time / U_O_T;

		if (interval < lastInterval){
			interval = lastInterval;
			time = lastInterval*U_O_T;
		}
		
		while (!loaded(sched,interval,time)){
			
			interval = nextInterval(time);
			time = interval*U_O_T;
		}
		
	}

	private boolean loaded(T sched,long interval2, long time) {
		l = load.get(interval);
		
		if (l == null){
			
			l = new Long(0);
			
		}
		
		l++;
		
		if (l<=sput){
			save(sched,time,l);
			return true;
		}
		
		return false;
	}

	private long nextInterval(long time) {
				
		lastInterval = ((time/ U_O_T)+1);
		
		return lastInterval;
		
	}

	private void save(T sched, long time, long l) {
		
		load.put(interval, l);
		
		
		times.put(sched,time);
		
	}

	public long getProcessedTime(T document) {
		
		return times.get(document) + tc;
	
	}
	
	public void undo(long Id){
		
		long num = load.remove(interval);
		num--;
		load.put(interval, num);
		
		times.remove(Id);
		
	}
	
}
