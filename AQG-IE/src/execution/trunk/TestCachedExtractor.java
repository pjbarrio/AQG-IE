package execution.trunk;

import utils.persistence.PersistenceImplementation;
import execution.informationExtraction.CachedInformationExtractor;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyDatabase;
import exploration.model.enumerations.VersionEnum;

public class TestCachedExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DummyDatabase d = new DummyDatabase("DATABASENAME");
		
		WorkloadModel wm = new WorkloadModel(1,"/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/Workload.ds","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/Workload.wl","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/WorkloadQueries","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/1/WorkloadRelations");
		
		Version v = Version.generateInstance(VersionEnum.DEPENDENT.name(), wm);
		
		new CachedInformationExtractor(d.getName(), d, v, wm, PersistenceImplementation.getWriter());
		
	}

}
