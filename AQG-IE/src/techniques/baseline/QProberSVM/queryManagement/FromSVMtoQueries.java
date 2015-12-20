package techniques.baseline.QProberSVM.queryManagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import utils.SVM.Rules.SVMRule;

public class FromSVMtoQueries {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String ent = "QProber3rdVersion";
		
		args = new String[2];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/queries/" + ent;
		
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/queries/ready/" + ent;
		
		String file = args[0];
		
		String output = args[1];
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String line = br.readLine();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		while (line != null){
		
			String[] spl = SVMRule.getWords(line);
			
			String q = "";
			
			for (int i = 0; i < spl.length; i++) {
				
				q = q + "+" + spl[i] + " ";
				
			}			
			
			bw.write(q.substring(0,q.length()-1) + "\n");
			
			line = br.readLine();
			
		}
		
		bw.close();
		
		br.close();
	}

}
