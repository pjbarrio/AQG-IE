package execution.model.factory;

import java.util.HashMap;
import java.util.Map;

import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import execution.model.parameters.Parametrizable;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import exploration.model.enumerations.InteractionPersisterEnum;

public class InteractionPersisterFactory {

	private static Map<String, InteractionPersister> persisters;

	public synchronized static InteractionPersister generateInstance(String name,
			Parametrizable parameters, persistentWriter pW) {
		
		switch (InteractionPersisterEnum.valueOf(name)) {
		
		case DISK_BASED:
			
			String dp = parameters.loadParameter(ExecutionAlternativeEnum.DISK_PREFIX).getString();
			
			String fi = parameters.loadParameter(ExecutionAlternativeEnum.FILE_INDEX).getString();
			
			InteractionPersister ip = getPersisters().get(dp+fi);
			
			if (ip == null){
				ip = new DiskBasedInteractionPersister(dp, fi, pW);
				getPersisters().put(dp+fi,ip);
			}
			return ip;

		default:
			return null;
		}
		
	}

	private static Map<String,InteractionPersister> getPersisters() {
		
		if (persisters == null){
			persisters = new HashMap<String, InteractionPersister>();
		}
		return persisters;
	}

}
