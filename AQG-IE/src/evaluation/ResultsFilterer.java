package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ResultsFilterer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[4];
		
		String ent = "SVMRules1stVersion";
		
		args[0] = "/home/pjbarrio/workspace/Experiments/workload/precision_recall/" + ent + "Precision.csv"; //input

		args[1] = "/home/pjbarrio/workspace/Experiments/workload/precision_recall/" + ent + "PrecisionReduced.csv"; //output
		
		args[2] = "106009.0"; //total
			
		args[3] = "25000.0"; //final number
		
		double count = 0.0;
		
		double total = Double.valueOf(args[2]);
		double finalN = Double.valueOf(args[3]);
		
		double TOP = total/finalN;
		
		BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(args[1])));
		
		String line = br.readLine();
		
		bw.write(line + "\n");
		
		line = br.readLine();
		
		while (line!=null){
			if (count <= 1.0){
				bw.write(line + "\n");
				count = TOP;
			}
			line = br.readLine();
			count-=1.0;	
		}
		
		br.close();
		bw.close();
	}

}
