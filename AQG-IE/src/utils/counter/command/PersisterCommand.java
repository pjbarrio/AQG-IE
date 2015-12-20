package utils.counter.command;

import utils.persistence.persistentWriter;

public abstract class PersisterCommand {

	protected persistentWriter pW;

	public PersisterCommand(persistentWriter pW){
		this.pW = pW;
	}

	public abstract void execute();
	
}
