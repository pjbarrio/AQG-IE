package collection.processing.indetifying;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import utils.id.Idhandler;
import utils.id.useful.UsefulUselessHandler;
import execution.informationExtraction.OpenCalaisExtractor;
import execution.workload.impl.condition.TuplesCondition;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;
import exploration.model.DocumentHandle;

public class MatchingTuplesDetection {

	private static OpenCalaisExtractor ocre;
	private static UsefulCondition condition;
	private static Idhandler idhandler;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String dbclass = "Business";
		
		String database = "TheEconomist";
		
		String version = "Constrained";
		
		args = new String[9];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/Useful" + database + "1stVersion" + version;
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/"+ database +".id";
		args[2] = "/proj/db/NoBackup/pjbarrio/sites/" + dbclass + "/" + database + "/";
		args[3] = "/proj/db/NoBackup/pjbarrio/OCOutput/" + database + ".table"; //replace with /proj/db/NoBackup/pjbarrio/OCOutput/TheEconomist.table
		args[4] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.wl";
		args[5] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.ds";
		args[6] = "NaturalDisaster";
		args[7] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/MatchingTuples"+ database +"1st"+ version +".tuples";
		args[8] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/MatchingTuplesWithSources"+database+"1st"+ version +".tuples";
		
//		condition = new WorkLoadCondition(args[4], args[5]);
	
		condition = new TuplesCondition();
		
		ocre = new OpenCalaisExtractor("OpenCalais",args[6],args[3],database, database + "matchingTuple");
		
		idhandler = new Idhandler(args[1],true);
		
//		ArrayList<String> useful = UsefulUselessHandler.loadFile(args[0], idhandler);
//		
//		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(args[8])));
//		
//		ArrayList<Tuple> tuples = getMatchingTuples(useful,bw,database);
//
//		bw.close();
//		
//		writeOutuput(tuples,args[7]);

		throw new UnsupportedOperationException("IMPLEMENT!");
		
	}

	private static void writeOutuput(ArrayList<Tuple> tuples, String file) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
		
		for (Tuple tuple : tuples) {
			
			bw.write(tuple.toString() + "\n");
			
//			bw.write(tuple.toString() + generateSourcesText(tuple.getDocuments()) + "\n");
			
		}
		
		bw.close();
		
		
	}

	private static ArrayList<Tuple> getMatchingTuples(ArrayList<String> useful, BufferedWriter bw, String database) throws IOException {
		
		ArrayList<Tuple> ret = new ArrayList<Tuple>();
		
		long i = 1;
		
		for (String usefulDoc : useful) {
			
			System.out.println(i++ + " Processing: " + usefulDoc + " Outof: " + useful.size());
			
			Collection<Tuple> t = (Collection<Tuple>) getMatchingTuples(usefulDoc, database);
			
			for (Tuple tuple : t) {
				
				bw.write(idhandler.get(usefulDoc) + "," + tuple.toString() + "\n");
				
			}
			
			ret.addAll(t);
			
		}
		
		return ret;
	}

	private static Collection<? extends Tuple> getMatchingTuples(
			String usefulDoc,String database) {
		
//		ArrayList<Tuple> ret = new ArrayList<Tuple>();
//		
//		Tuple[] tuples = ocre.execute(new DocumentHandle(usefulDoc));
//		
//		for (Tuple tuple : tuples) {
//			
//			if (condition.isItUseful(tuple)){
//				tuple.setDocument(usefulDoc);
//				ret.add(tuple);
//			}
//		}
//		
//		return ret;
		
		throw new UnsupportedOperationException("IMPLEMENT!");

		
	}

}
