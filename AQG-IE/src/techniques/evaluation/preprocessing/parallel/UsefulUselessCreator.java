package techniques.evaluation.preprocessing.parallel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.id.Idhandler;
import utils.id.useful.UsefulUselessHandler;
import execution.informationExtraction.OpenCalaisExtractor;
import execution.workload.impl.condition.TuplesCondition;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;
import exploration.model.DocumentHandle;
import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class UsefulUselessCreator implements Runnable {

	private ArrayList<Long> useful;
	private Idhandler idHandler;
	private ArrayList<Long> useless;
	private String database;
	private int round;
	private String version;
	private String dbclass;
	
	public UsefulUselessCreator(String dbclass,String database, int round, String version){
		this.dbclass = dbclass;
		this.database = database;
		this.round = round;
		this.version = version;
	}
	
	@Override
	public void run() {
		
		String databaseVersion = database + "-" + round;
		
		String[] args = new String[7];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/OCOutput/"+ databaseVersion +".table"; //replace with "/proj/db/NoBackup/pjbarrio/Experiments/workload/DocumentsRelation.txt"
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useful"+ databaseVersion + "1stVersion" + version;
		args[2] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.wl";
		args[3] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.ds";
		args[4] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/"+ database +".id";
		args[5] = "NaturalDisaster";
		args[6] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useless"+ databaseVersion + "1stVersion" + version;
		
		OpenCalaisExtractor oce = new OpenCalaisExtractor("OpenCalais", args[5], args[0],database,database + "-" + round  + "-overlap");
		
		UsefulCondition condition = new TuplesCondition();
		
		useful = new ArrayList<Long>();
		
		useless = new ArrayList<Long>();
				
		try {
			
			idHandler = new Idhandler(args[4],true);
			
			evaluate(condition,args[0],oce);
			
			WriteToFile(useful,args[1]);
			
			WriteToFile(useless,args[6]);
		
		} catch (IOException e) {
			System.out.println("ERROR IN THREAD: " + round);
			e.printStackTrace();
		}
		
		
	}

	private void WriteToFile(ArrayList<Long> array, String file) throws IOException {
		
		UsefulUselessHandler.write(array,file);
		
	}

	private void evaluate(UsefulCondition condition, String files,
			OpenCalaisExtractor oce) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File(files)));
		
		String line = br.readLine();
		
		long index = 1;
		
		while (line!=null){
			
			String[] pair = null;//OpenCalaisRelationExtractor.processLine(line);
			
			System.out.println(round + " - " + index++ + " .- " + pair[0]);
			
//XXX FIX!			Tuple[] tuples = oce.execute(new DocumentHandle(pair[0]));
			
			Tuple[] tuples = null;
			
			if (condition.isItUseful(tuples)){
				useful.add(idHandler.get(pair[0]));
			} else if (condition.isItUseless(tuples)){
				useless.add(idHandler.get(pair[0]));
			}
			
			line = br.readLine();
		}
		
		br.close();
		
	}
	
}
