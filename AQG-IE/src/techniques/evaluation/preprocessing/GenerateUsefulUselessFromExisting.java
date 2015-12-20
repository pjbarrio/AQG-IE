package techniques.evaluation.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import utils.id.Idhandler;

public class GenerateUsefulUselessFromExisting {

	private static HashSet<Long> used;

	/**
	 * @param args
	 * Useful will be the same, but useless will be the result of TOTAL-USELESS1ST-USEFUL1ST
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String version = "2ndVersion";
		String Compversion = "1stVersion";
		
		args = new String[6];
		
		args[0] = "/home/pjbarrio/workspace/Experiments/workload/UsefulTheEconomist" + version;
		args[1] = "/home/pjbarrio/workspace/Experiments/workload/UselessTheEconomist" + version;
		args[2] = "/home/pjbarrio/workspace/Experiments/workload/TheEconomist.id";
		args[3] = "/home/pjbarrio/workspace/Experiments/workload/UsefulTheEconomist" + Compversion;
		args[4] = "/home/pjbarrio/workspace/Experiments/workload/UselessTheEconomist" + Compversion;
		args[5] = "/proj/db/NoBackup/pjbarrio/sites/Business/TheEconomist/";
		
		Idhandler idhandler = new Idhandler(args[2], true);
		
		used = new HashSet<Long>();
		
		copyUseful(args[0],args[3]);
		
		createUseless(idhandler.getSize(),args[1],args[4]);
		
	}

	private static void createUseless(long size, String uselessDestiny,
			String uselessSource) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(new File(uselessSource)));
		
		String line = br.readLine();
		
		while (line!=null){
			
			System.out.println("Creating...: " + line);
			
			used.add(Long.valueOf(line));
			
			line = br.readLine();
			
		}
		
		br.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(uselessDestiny));
		
		for (int i = 1; i <= size; i++) {
			
			if (!used.contains(Long.valueOf(i))){
				
				System.out.println("Adding... " + i);
				
				bw.write(i + "\n");
			}
			
		}
		
		bw.close();
		
	}

	private static void copyUseful(String usefulDestiny, String usefulSource) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(new File(usefulSource)));
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(usefulDestiny)));
		
		String line = br.readLine();
		
		while (line!=null){
		
			System.out.println("Copying...: " + line);
			
			bw.write(line + "\n");
			
			used.add(Long.valueOf(line));
			
			line = br.readLine();
		}
		
		bw.close();
		br.close();
		
	}

}
