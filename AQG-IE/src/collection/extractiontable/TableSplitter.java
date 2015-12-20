package collection.extractiontable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TableSplitter {

	private final static String FINALE = ".table";
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String database = "TheEconomist";
		
		int number = 500;
		
		String table = "/proj/db/NoBackup/pjbarrio/OCOutput/" + database;
		
		File f1 = new File(table + FINALE);
		
		BufferedReader br = new BufferedReader(new FileReader(f1));
		
		String line = br.readLine();
		
		int round = 0;
		
		int processed = 0;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(table + "-" + round + FINALE)));
		
		while (line !=null){
						
			if (processed == number){
				bw.close();
				processed = 0;
				round++;
				bw = new BufferedWriter(new FileWriter(new File(table + "-" + round + FINALE)));
			
			}
			
			if (processed != 0){
				bw.write("\n");
			}
			
			bw.write(line);
			
			line = br.readLine();
		
			processed++;
		}
		
		System.out.println("ROUND: " + round);
		
		bw.close();
		
		br.close();
	}

}
