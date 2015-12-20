package techniques.evaluation.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.Database;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;

public class GenerateUsefulUselessFromIndependent {

	private static databaseWriter pW;
	private static Hashtable<Long,ArrayList<String>> tuples;
	private static ArrayList<Long> useless;
	private static ArrayList<Long> newUseless;
	private static Hashtable<Long,ArrayList<Tuple>> newUseful;
	private static Hashtable<String, Hashtable<Long,ArrayList<String>>> tuplesFiletable = new Hashtable<String, Hashtable<Long,ArrayList<String>>>();
	private static Hashtable<String, ArrayList<Long>> usefulFiletable = new Hashtable<String, ArrayList<Long>>();

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		TupleReader t;
		
		pW = (databaseWriter)PersistenceImplementation.getWriter();
		
		String relation = "NaturalDisaster";

		int[] database = {1,2,3,4,5}; //databases Ids.
		
		int[] workload = {1,2,3,4,5};
		
		String[] version = {"INDEPENDENT","DEPENDENT"};
		
		for (int i = 0; i < workload.length; i++) {
			
			WorkloadModel wm = pW.getWorkloadModel(workload[i]);
			
			for (int j = 0; j < database.length; j++) {
				
				for (int k = 0; k < version.length; k++) {
					
					Database db = pW.getDatabase(database[j]);
					
					String tuplesFile = pW.getRelationTuples(db,relation);
					
					tuples = loadTuplesFile(tuplesFile);
					
					String uselessFile = pW.getRelationUseless(db,relation);
					
					useless = loadDocumentsFile(uselessFile);
					
					Version v = Version.generateInstance(version[k], wm);
					
					newUseful = new Hashtable<Long, ArrayList<Tuple>>();
					
					newUseless = new ArrayList<Long>();
					
					evaluate(v.getCondition());
							
					writeUseless(pW.getUselessFiles(db.getName(), v.getName(), wm));
					
					writeUseful(pW.getUsefulFiles(db.getName(), v.getName(), wm));
					
//XXX class not used anymore					writeMatchingTuples(pW.getMatchingTuplesWithSourcesFile(db.getName(), v.getName(), wm));
					
				}
				
			}
			
		}
		
	}

	private static void writeMatchingTuples(String matchingTuplesWithSourcesFile) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(matchingTuplesWithSourcesFile)));
		
		for (Enumeration<Long> e = newUseful.keys(); e.hasMoreElements();){
			
			Long id = e.nextElement();
			
			ArrayList<Tuple> tupls = newUseful.get(id);
			
			for (Tuple tuple : tupls) {
				bw.write(id + "," + tuple.toString() + "\n");
			}
			
			
			
		}
		
		bw.close();
		
	}

	private static void writeUseful(String usefulFiles) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(usefulFiles)));
		
		for (Enumeration<Long> e = newUseful.keys(); e.hasMoreElements();){
			
			 bw.write(e.nextElement() + "\n");
			
		}
		
		bw.close();
		
	}

	private static void writeUseless(String uselessFiles) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(uselessFiles)));
		
		for (Long uselessId : useless) {
			
			bw.write(uselessId + "\n");
			
		}
		
		for (Long uselessId : newUseless){
			
			 bw.write(uselessId + "\n");
			
		}
		
		bw.close();
		
	}

	private static void evaluate(UsefulCondition condition) {
		
		for (Enumeration<Long> e = tuples.keys(); e.hasMoreElements();){
			
			Long id = e.nextElement();
			
			Tuple[] tupl = getTuples(tuples.get(id));
			
			if (condition.isItUseful(tupl)){
				
				for (int i = 0; i < tupl.length; i++) {
					
					if (condition.isItUseful(tupl[i])){
						getUsefulTuples(id).add(tupl[i]);
					}
					
				}
				
			} else {
				newUseless.add(id);
			}
			
		}
		
	}

	private static ArrayList<Tuple> getUsefulTuples(Long id) {
		
		ArrayList<Tuple> ret = newUseful.get(id);
		
		if (ret == null){
			ret = new ArrayList<Tuple>();
			newUseful.put(id, ret);
		}
		return ret;
	}

	private static Tuple[] getTuples(ArrayList<String> tuples) {
		
		Tuple[] ret = new Tuple[tuples.size()];
		
		int i = 0;
		
		for (String tuple : tuples) {
			
			ret[i] = TupleReader.generateTuple(tuple);
			
			i++;
		}
		
		return ret;
	}

	private static ArrayList<Long> loadDocumentsFile(String usefulFile) throws IOException {
		
		ArrayList<Long> ret = usefulFiletable .get(usefulFile);
		
		if (ret == null){
			
			ret = new ArrayList<Long>();
			
			BufferedReader br = new BufferedReader(new FileReader(new File(usefulFile)));
			
			String line = br.readLine();
			
			while (line != null){
				
				ret.add(Long.valueOf(line));
				
				line = br.readLine();
		
			}
			
			br.close();
			
			usefulFiletable.put(usefulFile,ret);
			
		}
		
		return ret;
		
	}

	private static Hashtable<Long,ArrayList<String>> loadTuplesFile(String tuplesFile) {
		
		Hashtable<Long,ArrayList<String>> ret = tuplesFiletable .get(tuplesFile);
		
		if (ret == null){
			
			ret = TuplesLoader.loadIdtuplesTable(tuplesFile);
			
			tuplesFiletable.put(tuplesFile,ret);
		}
		
		return ret;
	}

}
