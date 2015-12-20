package techniques.evaluation.postprocessing.combining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import utils.clock.Clock;


public class CombinedQueryTimeGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] version = {"","Constrained"};
		
		String[] algorithm = {"Incremental","MSC","Optimistic","QProber","Ripper"};
		
		String[] database = {"Bloomberg","TheCelebrityCafe","TheEconomist","UsNews","Variety","GlobalSample"};
		
		int[] configuration = {0,1,2};
		
		args = new String[3];
		
		args[0] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/queries/final/combined/"; //prefix of queries
		
		args[1] = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/queries/final/"; //prefix of single files.
		
		for (int i = 0; i < version.length; i++) {
			
			for (int j = 0; j < database.length; j++) {
				
				for (int k = 0; k < algorithm.length; k++) {
					
					String exp = args[1] + algorithm[k] + "/" + algorithm[k] + database[j] + "1stVersion" + version[i];
					
					System.out.println("Original: " + exp);
					
					for (int l = 0; l < database.length; l++) {
						
						for (int g = 0; g < configuration.length; g++) {
							
							String comb = algorithm[k] + "/" + algorithm[k] + database[j] + "-On-" + database[l] + "1stVersion" + version[i] + "-" + configuration[g];
							
							createFile(args[0],comb,exp);
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}

	private static void createFile(String prefix, String file, String orig) throws IOException {

		System.out.println("Comb: " + file);
		
		String fileTime = Clock.getFileName(orig);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(fileTime)));
		
		long lastTime = 0;
		
		String time = br.readLine();
		
		while (time != null){
			
			lastTime = Long.valueOf(time);
			
			time = br.readLine();
		}
		
		br.close();
		
		br = new BufferedReader(new FileReader(new File(prefix + file)));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Clock.getFileName(prefix + file))));
		
		String query = br.readLine();
		
		while (query != null){
			
			bw.write(lastTime + "\n");
			
			query = br.readLine();
		}
		
		br.close();
		
		bw.close();
		
	}

	

}
