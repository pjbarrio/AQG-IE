package utils.counter.command.impl;

import utils.counter.command.PersisterCommand;
import utils.persistence.persistentWriter;

public class PersistCandidateSentenceCommand extends PersisterCommand {

	public PersistCandidateSentenceCommand(persistentWriter pW) {
		super(pW);
	}

	@Override
	public void execute() {
		pW.persistCandidateSentences();
	}

}
