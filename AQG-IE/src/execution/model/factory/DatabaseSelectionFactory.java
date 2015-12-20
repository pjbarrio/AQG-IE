package execution.model.factory;

import execution.model.adaptivestrategy.databaseSelection.CostModelBasedDatabaseSelection;
import execution.model.adaptivestrategy.databaseSelection.DatabaseSelection;
import execution.model.adaptivestrategy.databaseSelection.FixedDatabaseSelection;
import exploration.model.enumerations.DatabaseSelectionEnum;

public class DatabaseSelectionFactory {

	public static DatabaseSelection generateInstance(String string) {
		
		switch (DatabaseSelectionEnum.valueOf(string)) {
		
		case FIXED:
			
			return new FixedDatabaseSelection();

		case COSTMODELBASED:
			
			return new CostModelBasedDatabaseSelection();
			
		default:
			
			return null;
		}
		
	}

	
}
