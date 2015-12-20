package execution.trunk.fix.interaction;

import java.io.File;
import java.util.Map;

import exploration.model.Database;

public class ExplorerRunnable implements Runnable {

	private File[] queries;
	private Database d;
	private Map<Integer, String> mapOtherQueries;

	public ExplorerRunnable(Database d, File[] queries,
			Map<Integer, String> mapOtherQueries) {
		this.d = d;
		this.queries = queries;
		this.mapOtherQueries = mapOtherQueries;
	}

	@Override
	public void run() {
		
		for (int j = 0; j < queries.length; j++) {
			
			if (queries[j].list().length > 1){
				
				System.out.println(d.getId()+" - "+queries[j].getName());
				
				synchronized (mapOtherQueries) {
					mapOtherQueries.put(d.getId(), queries[j].getName());
				}
				break;
			}
		}
		

	}

}
