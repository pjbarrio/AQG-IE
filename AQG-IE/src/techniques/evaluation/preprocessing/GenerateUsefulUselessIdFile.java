package techniques.evaluation.preprocessing;

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

public class GenerateUsefulUselessIdFile {

	private static ArrayList<Long> useful;
	private static Idhandler idHandler;
	private static ArrayList<Long> useless;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String version = "";
		String database = "TheEconomist";
		String dbclass = "Business";
		args = new String[7];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/OCOutput/"+ database +".table"; //replace with "/proj/db/NoBackup/pjbarrio/Experiments/workload/DocumentsRelation.txt"
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useful"+ database + "1stVersion" + version;
		args[2] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.wl";
		args[3] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.ds";
		args[4] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/"+ database +".id";
		args[5] = "NaturalDisaster";
		args[6] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useless"+ database + "1stVersion" + version;
		
		OpenCalaisExtractor oce = new OpenCalaisExtractor("OpenCalais", args[5], args[0],database,database + "-overlap");
		
		UsefulCondition condition = new TuplesCondition();
		
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
//			String[] pair = null;//OpenCalaisRelationExtractor.processLine(line);
//			
//			System.out.println(index++ + " .- " + pair[0]);
//			
//			Tuple[] tuples = oce.execute(new DocumentHandle(pair[0]));
//			
//			if (condition.isItUseful(tuples)){
//				useful.add(idHandler.get(pair[0]));
//			} else if (condition.isItUseless(tuples)){
//				useless.add(idHandler.get(pair[0]));
//			}
//			
//			line = br.readLine();
//		}
//		
//		br.close();
		
		throw new UnsupportedOperationException("IMPLEMENT!");

		
	}

}
