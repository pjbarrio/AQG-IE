package utils.clock;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

public class Clock {

	public enum ClockEnum {
		QUERY_SENT, COMBINED, OVERALL_ALGORITHM, SIMPLE_ALGORITHM, ISSUED_QUERY, NEXT_REQUIRED, DOWNLOAD_DOCUMENT
	}
	
	private static Hashtable<ClockEnum, Long> start = new Hashtable<Clock.ClockEnum, Long>();
	
	private static Hashtable<ClockEnum, Long> end = new Hashtable<Clock.ClockEnum, Long>();
	private static Hashtable<ClockEnum, Long> startReal = new Hashtable<Clock.ClockEnum, Long>();
	private static Hashtable<ClockEnum, Long> endReal = new Hashtable<Clock.ClockEnum, Long>();
	private static Hashtable<ClockEnum, ThreadMXBean> bean = new Hashtable<ClockEnum, ThreadMXBean>();

	private static Hashtable<String, Long> startId = new Hashtable<String, Long>();
	
	private static Hashtable<String, Long> endId = new Hashtable<String, Long>();
	private static Hashtable<String, Long> startRealId = new Hashtable<String, Long>();
	private static Hashtable<String, Long> endRealId = new Hashtable<String, Long>();
	private static Hashtable<String, ThreadMXBean> beanId = new Hashtable<String, ThreadMXBean>();
	
	public static void startTime(ClockEnum timer) {
		
		bean.remove(timer);
		
		start.put(timer, System.currentTimeMillis());	

		startReal.put(timer, getSystemTime(timer));
		
	}

	public static void stopTime(ClockEnum timer) {
		
		end.put(timer,System.currentTimeMillis());
		
		endReal.put(timer, getSystemTime(timer));
		
	}

	public static long getWallTime(ClockEnum timer) {
		
		return end.get(timer) - start.get(timer);
		
	}
	
	public static String getDateTime() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    Date date = new Date();
	    return dateFormat.format(date);

	}

	public static String getFileName(String string) {
		return new String(string + ".time");
	}

	public static long getMeasuredTime(ClockEnum timer) {
		return (endReal.get(timer) - startReal.get(timer))/1000000;
	}

	/** Get user time in nanoseconds. */
	public static long getUserTime( ClockEnum timer) {
	    ThreadMXBean bean = getThreadBean(timer);
    
	    return bean.getCurrentThreadUserTime( );
	}
	
	/** Get system time in nanoseconds. 
	 * @param timer */
	public synchronized static long getSystemTime(ClockEnum timer) {
	    
		ThreadMXBean bean = getThreadBean(timer);
	    
	    return bean.getCurrentThreadCpuTime() + bean.getCurrentThreadUserTime();
	    
	}
	
	private static ThreadMXBean getThreadBean(ClockEnum timer) {
		
		ThreadMXBean b = bean.get(timer);
		
		if (b == null){
			b = ManagementFactory.getThreadMXBean( );
			bean.put(timer, b);
		}
		return b;
	}

	public static void startTime(String timer) {
		
		beanId.remove(timer);
		
		startId.put(timer, System.currentTimeMillis());	

		startRealId.put(timer, getSystemTime(timer));
		
	}

	private static Long getSystemTime(String timer) {
		
		ThreadMXBean bean = getThreadBean(timer);
	    
	    return bean.getCurrentThreadCpuTime() + bean.getCurrentThreadUserTime();
		
	}

	private static ThreadMXBean getThreadBean(String timer) {
		
		ThreadMXBean b = beanId.get(timer);
		
		if (b == null){
			b = ManagementFactory.getThreadMXBean( );
			beanId.put(timer, b);
		}
		return b;
		
	}

	public static void stopTime(String timer) {
		
		endId.put(timer,System.currentTimeMillis());
		
		endRealId.put(timer, getSystemTime(timer));
		
	}

	public static long getMeasuredTime(String timer) {
		
		return (endRealId.remove(timer) - startRealId.remove(timer))/1000000;
		
	}

	public static long getWallTime(String timer) {
		
		return endId.get(timer) - startId.get(timer);
		
	}
	
}
