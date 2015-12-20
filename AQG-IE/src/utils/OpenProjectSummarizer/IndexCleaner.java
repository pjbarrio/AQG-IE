package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class IndexCleaner {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/";
		
		BufferedReader br = new BufferedReader(new FileReader(new File(rootDirectory + "finalIndex.txt")));
		
		System.setOut(new PrintStream(new File(rootDirectory + "finalIndexCleaned.txt")));
		
		String line = br.readLine();
		
		while (line != null){
			
			System.out.println(cleanLine(line));
			
			line = br.readLine();
			
		}

		br.close();

	}

	private static String cleanLine(String line) {
		
		String prefix = line.substring(0, line.indexOf("//")) + "//";
		
		line = line.replaceFirst(prefix, "");
		
		if (line.contains("/")){
			
			return prefix + line.substring(0, line.indexOf("/")+1);
			
		}
		
		return prefix + line;
		
	}

}
