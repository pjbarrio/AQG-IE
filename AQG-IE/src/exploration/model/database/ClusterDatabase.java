package exploration.model.database;

import java.util.Set;

import exploration.model.Database;
import exploration.model.clusterfunction.ClusterFunction;

public class ClusterDatabase extends GroupDatabase {

	public ClusterDatabase(int id, String name) {
		
		super(id, name, -1, "", "");

	}
		
	@Override
	public boolean isGlobal() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return false;
	}

	@Override
	public boolean isCrossable() {
		return true;
	}

	@Override
	public String getIndex() {
		return null;
	}

	@Override
	public boolean isOnline() {
		return false;
	}

	@Override
	public boolean isCluster() {
		return true;
	}

	
}
