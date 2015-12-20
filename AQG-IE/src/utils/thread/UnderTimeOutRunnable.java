package utils.thread;


public abstract class UnderTimeOutRunnable<T> implements Runnable {

	private IntegerWrap waitingFor;
	private T output = null;

	@Override
	public void run(){
		output = execute();
		notifyDone();
	}

	private void notifyDone() {

		synchronized (waitingFor) {
			waitingFor.setValue(TimedOutTask.DONE);
			waitingFor.notify();
		}
		
	}

	protected abstract T execute();

	public void setWaitingFor(IntegerWrap waitingFor) {
		this.waitingFor = waitingFor;
	}

	public T getOutput() {
		return output;
	}
	
}
