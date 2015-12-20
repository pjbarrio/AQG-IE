package exploration.model.database;

import exploration.model.Database;

public abstract class SimpleDatabase extends Database {

	public SimpleDatabase(int id, String name, int size, String type,
			String modelType) {
		super(id, name, size, type, modelType);
	}

}
