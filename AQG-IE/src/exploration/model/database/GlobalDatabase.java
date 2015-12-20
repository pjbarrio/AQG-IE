package exploration.model.database;

import exploration.model.Database;

public class GlobalDatabase extends GroupDatabase {

	public GlobalDatabase(int id, String name) {
		super(id, name, -1, "", "");
	}

	@Override
	public boolean isGlobal() {
		
		return true;
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
		return false;
	}

}
