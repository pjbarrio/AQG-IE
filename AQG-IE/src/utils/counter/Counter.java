package utils.counter;

import utils.counter.command.PersisterCommand;

public class Counter {

	private PersisterCommand pc;
	private int limit;
	private int current;

	public Counter(int limit, PersisterCommand  pc){
		
		this.pc = pc;
		this.limit = limit;
		this.current = limit;
		
	}
	
	public synchronized void inform(){
		current--;
		if (current <= 0){
			current = limit;
			pc.execute();
		}
	}
	
}
