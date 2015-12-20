package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class SecondRoundFixer {

	private static final String VERSION = "2"; 
	
	private static String rootDirectory;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/";
		
		String file = rootDirectory + "log.txt";
		
		System.setErr(new PrintStream(new File(rootDirectory + "log" + VERSION + ".txt")));

		System.setOut(new PrintStream(new File(rootDirectory + "index" + VERSION + ".txt")));
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String directory = br.readLine();
		
		while (directory != null){
		
			String url = br.readLine().replace("URL: ", "");
			
			int fil = Integer.valueOf(br.readLine().replace("FILE: ", ""));
		
			String dir = directory.replace("DIRECTORY: ", "");
			
			new Thread(new URLDownloader(fil, url, dir)).start();			
			
			directory = br.readLine();
		}
		
		br.close();
	}

}
