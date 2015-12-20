package exploration.model.database;

import exploration.model.Database;

public class LocalDatabase extends SimpleDatabase {

	private String index;

	public LocalDatabase(int id, String name, int size, String type,
			String modelType, String index) {
		super(id, name, size, type, modelType);
		this.index = index;
	}

	@Override
	public boolean isGlobal() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	@Override
	public boolean isCrossable() {
		return true;
	}

	@Override
	public String getIndex() {
		return index;
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
