package utils.counter.command.impl;

import utils.counter.command.PersisterCommand;
import utils.persistence.persistentWriter;

public class PersistOperableStructureCommand extends PersisterCommand {

	public PersistOperableStructureCommand(persistentWriter pW) {
		super(pW);
	}

	@Override
	public void execute() {
		pW.persistOperableStructure();
	}

}
