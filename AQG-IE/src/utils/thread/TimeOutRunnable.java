package utils.thread;

import utils.thread.TimedOutTask;

public class TimeOutRunnable<T> implements Runnable {

	private UnderTimeOutRunnable<T> dr;
	private long wait;
	private IntegerWrap i;

	public TimeOutRunnable(long wait, UnderTimeOutRunnable<T> dr) {
		this.wait = wait;
		this.dr = dr;
		i = new IntegerWrap(TimedOutTask.START);
	}

	@Override
	public void run() {
		
		Thread t = new Thread(dr);
		
		dr.setWaitingFor(i);
		
		t.start();
		
		try {
			
			synchronized (i) {
				i.wait(wait);
			}
			
			if (t.isAlive()){
				i.setValue(TimedOutTask.TIME_OUT);
				t.stop();
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("finished");
		} 
					
	}

	public int getExitValue() {
		return i.getValue();
	}

	public T getOutput() {
		return dr.getOutput();
	}

}
