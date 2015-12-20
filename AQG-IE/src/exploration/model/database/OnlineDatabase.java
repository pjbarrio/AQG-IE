package exploration.model.database;

import exploration.model.Database;

public class OnlineDatabase extends SimpleDatabase {

	private String index;
	private String website;

	public OnlineDatabase(int id, String website, String modelType, String index) {
		super(id,website,-1,"",modelType);
		this.website = website;
		this.index = index;
	}

	@Override
	public String getName() {

		return Integer.toString(getId());

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

	public String getWebsite() {
		return website;
	}

	@Override
	public boolean isOnline() {
		return true;
	}
	
	@Override
	public boolean isCluster() {
		return false;
	}
	
}
