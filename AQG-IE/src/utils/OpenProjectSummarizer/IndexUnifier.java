package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class IndexUnifier {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/";
		
		System.setOut(new PrintStream(new File(rootDirectory + "finalIndex.txt")));
		
		int max = 131;
		
		for (int i = 1; i <= max; i++) {
			
			File f = new File(rootDirectory + "index" + i + ".txt");
			
			if (f.exists()){
				
				BufferedReader br = new BufferedReader(new FileReader(f));
				
				String line = br.readLine();
				
				while (line!=null){
					
					System.out.println(line);
					
					line = br.readLine();
					
				}
				
				br.close();
			}
			
		}

	}

}
