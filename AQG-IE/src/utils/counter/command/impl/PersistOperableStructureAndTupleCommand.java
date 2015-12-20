package utils.counter.command.impl;

import utils.counter.command.PersisterCommand;
import utils.persistence.persistentWriter;

public class PersistOperableStructureAndTupleCommand extends PersisterCommand {

	public PersistOperableStructureAndTupleCommand(persistentWriter pW) {
		super(pW);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() {
		pW.persistOperableStructure();
		pW.persistTuple();
	}

}
