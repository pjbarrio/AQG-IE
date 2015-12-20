package utils.thread;

import utils.thread.TimedOutTask;

public class IntegerWrap {

	private int i;

	public IntegerWrap(int i) {
		this.i = TimedOutTask.START;
	}

	public synchronized void setValue(int i) {
		this.i = i;
	}

	public synchronized int getValue() {
		return i;
	}

}
