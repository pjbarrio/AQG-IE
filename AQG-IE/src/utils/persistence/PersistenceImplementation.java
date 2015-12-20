package utils.persistence;

public class PersistenceImplementation {

	private static persistentWriter writer = null;
	
	public static persistentWriter getWriter() {
		
//		return new FileWriter("/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/");
		if (writer == null)
//			writer =  new databaseWriter("/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/");
			writer = create();
		return writer;
	}
	
	private static persistentWriter create() {
		return new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/",null,-1);
	}

	public static persistentWriter getNewWriter(){
		return create();
	}
	
}
