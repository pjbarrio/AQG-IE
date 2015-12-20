package techniques.newideas.MSC.queryManagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import techniques.newideas.MSC.model.MSCSet;

public class MSCToQueries {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String ent = "MSC2ndVersion";
		
		args = new String[2];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/queries/" + ent;
		
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/queries/ready/" + ent;
		
		String file = args[0];
		
		String output = args[1];
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String line = br.readLine();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		while (line != null){
		
			String q = MSCSet.fromStringToQuery(line);
			
			bw.write(q + "\n");
			
			line = br.readLine();
			
		}
		
		bw.close();
		
		br.close();
	
	}

}
