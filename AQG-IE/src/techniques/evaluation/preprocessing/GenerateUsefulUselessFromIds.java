package techniques.evaluation.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.id.Idhandler;
import utils.id.useful.UsefulUselessHandler;
import execution.informationExtraction.OpenCalaisExtractor;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.impl.condition.WorkLoadCondition;
import execution.workload.tuple.Tuple;
import exploration.model.DocumentHandle;

public class GenerateUsefulUselessFromIds {

	private static ArrayList<Long> useful;
	private static Idhandler idHandler;
	private static ArrayList<Long> useless;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String version = "Constrained";
		
		String database = "UsNews";
		String dbclass = "General";
		
		
		args = new String[8];
		
		args[0] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/Useful" + database + "1stVersion";
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useful" + database + "1stVersion"+ version;
		args[2] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.wl";
		args[3] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.ds";
		args[4] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/" + database + ".id";
		args[5] = "NaturalDisaster";
		args[6] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useless" + database + "1stVersion"+ version;
		args[7] = "/proj/db/NoBackup/pjbarrio/OCOutput/" + database + ".table"; //replace with /proj/dbNoBackup/pjbarrio/OCOutput/TheEconomist.table
		
		OpenCalaisExtractor oce = new OpenCalaisExtractor("OpenCalais", args[5], args[7],database,database + "-overlap");
		
		UsefulCondition condition = new WorkLoadCondition(args[2], args[3]);
		
		useful = new ArrayList<Long>();
		
		useless = new ArrayList<Long>();
		
		idHandler = new Idhandler(args[4],true);
		
		evaluate(condition,args[0],oce,database);
		
		WriteToFile(useful,args[1]);
		
		WriteToFile(useless,args[6]);
	
	}

	private static void WriteToFile(ArrayList<Long> array, String file) throws IOException {
		
		UsefulUselessHandler.write(array,file);
		
	}

	private static void evaluate(UsefulCondition condition, String files,
			OpenCalaisExtractor oce, String database) throws IOException {

//		BufferedReader br = new BufferedReader(new FileReader(new File(files)));
//		
//		String line = br.readLine();
//		
//		long index = 1;
//		
//		while (line!=null){
//			
//			System.out.println(index++ + " .- " + Long.valueOf(line));
//			
//			Tuple[] tuples = oce.execute(new DocumentHandle(idHandler.getDocument(Long.valueOf(line))));
//			
//			if (condition.isItUseful(tuples)){
//				useful.add(Long.valueOf(line));
//			} else if (condition.isItUseless(tuples)){
//				useless.add(Long.valueOf(line));
//			}
//			
//			line = br.readLine();
//		}
//		
//		br.close();
		
		throw new UnsupportedOperationException("IMPLEMENT!");
		
	}

}