package collection.processing.indetifying.offline;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import exploration.model.Document;

import utils.execution.ExtractionTableHandler;
import utils.id.Idhandler;

public class MatchingTuplesDetector {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] dbclasses = {/*"Business","Business","General","Trip",*/"Trip"};
		
		String[] databases = {/*"Bloomberg","TheEconomist","UsNews","TheCelebrityCafe",*/"Variety"};
		
		String relation = "PersonCareer";
		
		for (int i = 0; i < databases.length; i++) {
		
			String database = databases[i];
			
			String dbclass = dbclasses[i];
			
			File table = new File("/proj/db/NoBackup/pjbarrio/OCOutput/" + database + ".table");
		
			File outputFile = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Evaluation/"+relation+"/" + database + ".tuples");
			
			Hashtable<Document, String> extractionTable = ExtractionTableHandler.load(table);
			
			String idFile = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/id/"+ database +".id";
			
			String prefix = "/proj/db/NoBackup/pjbarrio/sites/" + dbclass + "/" + database + "/";
			
			Idhandler idhandler = new Idhandler(idFile,true);
			
			new MatchingTuplesDetector().execute(extractionTable,outputFile,relation,idhandler);
			
		}
		

	}

	private void execute(Hashtable<Document, String> extractionTable,
			File outputFile, String relation, Idhandler idhandler) {
		
		Thread t = new Thread(new MathchingTuplesDetectorRunnable(extractionTable,outputFile,relation,idhandler));
		
		t.start();
		
	}

}
