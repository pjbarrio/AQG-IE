package execution.workload.impl.queryManagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import searcher.interaction.formHandler.TextQuery;

import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;


public class QueryFromWorkload {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[3];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/Experiments/QXtract/Workload.wl";
		
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/queries/ready/WorkloadQueries";
		
		args[2] = "date,naturaldisaster";
		
		String[] infTypes = args[2].split(",");
		
		ArrayList<String> inferredTypes = new ArrayList<String>();
		
		for (int i = 0; i < infTypes.length; i++) {
			inferredTypes.add(infTypes[i]);
		}
		
		TupleReader tr = new TupleReader();
		
		Tuple[] tuples = tr.readTuples(args[0]);
		
		TupleQueryGenerator qg = new TupleQueryGenerator(inferredTypes,true,true,false,new ArrayList<String>(0));
		
		TextQuery[] queries = qg.generateQueries(tuples,inferredTypes,true,true,false);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(args[1])));
		
		boolean first = true;
		
		for (TextQuery query : queries) {
			
			if (!first)
				bw.write("\n");
			
			bw.write(query.getText());
			
			first = false;
		}
		
		bw.close();
	}

}
