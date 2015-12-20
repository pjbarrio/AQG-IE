package exploration.model.dummy;

import exploration.model.Database;

public class DummyDatabase extends Database {

	public DummyDatabase(String name) {
		super(0,name,0,"Type","ModelType");
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
		return false;
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
