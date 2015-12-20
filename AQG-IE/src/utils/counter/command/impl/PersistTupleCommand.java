package utils.counter.command.impl;

import utils.counter.command.PersisterCommand;
import utils.persistence.persistentWriter;

public class PersistTupleCommand extends PersisterCommand {

	public PersistTupleCommand(persistentWriter pW) {
		super(pW);
	}

	@Override
	public void execute() {
		pW.persistTuple();
	}

}
