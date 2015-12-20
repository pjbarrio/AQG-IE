package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class IndexOfFilesGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/";
		
		BufferedReader br = new BufferedReader(new FileReader(new File(rootDirectory + "finalDatabasesHTML.txt")));
		
		System.setOut(new PrintStream(new File(rootDirectory + "finalIndexDatabases.txt")));
		
		String line = br.readLine();
		
		while (line != null){
			
			System.out.println(generateIndex(line));
			
			line = br.readLine();
			
		}

		br.close();

	}

	private static String generateIndex(String line) {
		
		line = line.replace("HTML: ", "");
		
		String index = line.substring(line.lastIndexOf("/")+1).replace(".html", "");
		
		return index + "," + line.substring(0, line.lastIndexOf("/")+1);
		
	}

}
