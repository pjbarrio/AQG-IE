package utils.thread;

public class TimedOutTask<T> {

	public static final int DONE = 1;
	public static final int START = -1;
	public static final int TIME_OUT = 0;
	public static final int NULL_OUT = -2;
	private int wait;
	private UnderTimeOutRunnable<T> dr;

	public TimedOutTask(int wait, UnderTimeOutRunnable<T> dr) {
		
		this.wait = wait;
		this.dr = dr;
		
	}

	public T execute() {
		
		TimeOutRunnable<T> tor = new TimeOutRunnable<T>(wait,dr);
		
		Thread t = new Thread(tor);
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tor.getOutput();
		
		
	}

	
	
}
