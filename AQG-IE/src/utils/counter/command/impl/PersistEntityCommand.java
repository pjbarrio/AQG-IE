package utils.counter.command.impl;

import utils.counter.command.PersisterCommand;
import utils.persistence.persistentWriter;

public class PersistEntityCommand extends PersisterCommand {

	public PersistEntityCommand(persistentWriter pW) {
		super(pW);
	}

	@Override
	public void execute() {
		pW.persistEntities();
	}

}
