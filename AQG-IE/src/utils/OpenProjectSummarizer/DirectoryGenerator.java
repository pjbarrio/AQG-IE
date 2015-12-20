package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

class URLDownloader implements Runnable{

	private int i;
	private String urlString;
	private String directory;

	protected URLDownloader(int i, String urlString, String directory){
		this.i = i;
		this.urlString = urlString;
		this.directory = directory;
	}
	
	@Override
	public void run() {
		
		File file=new File(directory + i + ".html");
		    
		BufferedReader reader = null;
		
		BufferedWriter writer = null;
		
		try {
			
			URL url = new URL(urlString);
			
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			
			writer = new BufferedWriter(new FileWriter(file));
			
			String line;
			
			while ((line = reader.readLine()) != null) {
			
				writer.write(line);
				
				writer.newLine();
			
			}
			
			reader.close();
			
			writer.close();
		
			System.out.println(i + "," + urlString);			
			System.err.println(i + "," + urlString);
		} catch (MalformedURLException e) {
		
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (IOException e1) {

				System.err.println(i + " 1: " + urlString + " " + e.getClass());
				
			}
			
			System.err.println(i+ " 2: " + urlString + " " + e.getClass());
			
		}  catch (IOException e) {

			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (IOException e1) {
				
				System.err.println(i + " 3: " + urlString + " " + e.getClass());
			
			}

			System.err.println(i + " 4: " + urlString + " " + e.getMessage());
			
			
		} catch (NullPointerException e){
			
			try {
				if (reader != null)
					reader.close();
				if (writer != null)
					writer.close();
			} catch (IOException e1) {
				
				System.err.println(i + " 5: " + urlString + " " + e.getClass());
				
			}

			System.err.println(i+ " 6: " + urlString + " " + e.getClass());
			
		}
		
	}
	
}

public class DirectoryGenerator {

	private static final String VERSION = "131"; 
	private static long next = 2343665;
//	private static final int minI = 2500000;
//	private static final int maxI = 2600000;
	private static boolean newdirectory;
	private static String directory;
	private static String link;
	private static String rootDirectory;
	private static int i;
	private static boolean newlink;
	private static int index1;
	private static int index2;
	private static File file;

	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/";
		
		BufferedReader br = new BufferedReader(new FileReader(new File("/proj/db/NoBackup/pjbarrio/Directory/contentSummarized.rdf.u8")));
		
		String line = br.readLine();
		
		i = 1;
		
		System.setOut(new PrintStream(new File(rootDirectory + "index" + VERSION + ".txt")));
		
//		System.setErr(new PrintStream(new File(rootDirectory + "log" + VERSION + ".txt")));
		
		while (line!=null){
			
			processLine(line);
			
			if (newdirectory){
				createDirectory(directory);
			} else {
				
				if (newlink){
				
					if (saveFile(i,link,directory)){
					
						i++;
						
					}
				
				}
				
			}
			
			line = br.readLine();
			
		}

		System.err.println(i + " FILE READ!");
		
		br.close();
	}

	private static boolean saveFile(int i, String urlString, String directory) {
		
		System.err.println(i);
		
		if (i < next)
			return true;
		
		file = new File(directory + i + ".html");
	    
		if (file.exists())
			return true;
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			System.err.println(i + " INTERRUPTED");
//		}
//		
//		new Thread(new URLDownloader(i, urlString, directory)).start();
		
		new URLDownloader(i,urlString,directory).run();
		
		return true;
	
	}

	private static void createDirectory(String dir) {
		
		File f = new File(dir);
		
		if (f.exists())
			return;
		
		boolean success = (new File(dir)).mkdir();

		if (!success){
			System.out.println("NO ESCRIBE DIR!" + dir);
			System.exit(0);
		}
		
	}

	private static void processLine(String line) {
		
		newdirectory = false;

		newlink = false;
		
		if (line.startsWith("  <Topic")){
		
			newdirectory = true;
		
			directory = rootDirectory + extractText(line) + "/";
		
		}
		
		else if (line.startsWith("    <link")){
		
			newlink = true;
			
			link = extractText(line);
		
		}
	
	}

	private static String extractText(String line) {
		
		index1 = line.indexOf("\"");
		index2 = line.lastIndexOf("\"");
		
		return line.substring(index1+1,index2);
		
	}

}
