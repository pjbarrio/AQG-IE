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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FinalDatabasesRetriever {

	public class Retriever implements Runnable{

		private String url;
		private String databasesInterfaces;
		private int databaseNumber;
		private BufferedReader r;

		public Retriever(String url, String databasesInterfaces, int databaseNumber){
			this.url = url;
			this.databasesInterfaces = databasesInterfaces;
			this.databaseNumber = databaseNumber;
		}
		
		@Override
		public void run() {
			
			BufferedReader r = retrieveWebPage(databaseNumber, url);

			write(databasesInterfaces,databaseNumber,r,url);
			
		}
		
		private void write(String databasesInterfaces, int databaseNumber,
				BufferedReader r, String url) {
			
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(new File(databasesInterfaces + databaseNumber + ".html")));
				
				String fileLine;
				
				while ((fileLine = r.readLine())!=null){
					
					out.write(fileLine);
					out.newLine();
					
				}
				out.close();
				r.close();
			} catch (IOException e) {

				System.out.println(databaseNumber + "," + url);
				
			}

			
		}

		private BufferedReader retrieveWebPage(int databaseIndex, String urlString) {
			
			URL url;
			try {
				url = new URL(urlString);
				return new BufferedReader(new InputStreamReader(url.openStream()));
			} catch (MalformedURLException e) {
				
				System.out.println(databaseIndex + "," + urlString);
				
			} catch (IOException e) {

				System.out.println(databaseIndex + "," + urlString);
				
			}
			
			return null;
			
		}

		
	}

	private static HashSet<String> indexTable;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String oldIndex = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/OldDataIndex.txt";
		
		String databases = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/randomWebSelector-2.txt";
		
		String databasesInterfaces = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/Data/";
		
		String exit = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/DataIndex.txt";
		
		List<String> Index = new ArrayList<String>();
		
		loadOldIndex(oldIndex);
		
		System.setOut(new PrintStream(new File(exit + "error")));
		
		BufferedReader br = new BufferedReader(new FileReader(new File(databases)));

		String line;
		
		int databaseNumber = 1401;
		
		boolean start = false;
		
		while ((line = br.readLine()) != null){
			
			System.err.println(line);
			
			if (line.startsWith("CATEGORY"))
				continue;
			
			if (indexTable.contains(line)){
				continue;
			}
			
			if (start)
				new FinalDatabasesRetriever().execute(line,databasesInterfaces,databaseNumber);
			
			if (!start && line.equals("http://www.dcjs.virginia.gov/")){
				start = true;
			} else {
			
				Index.add(databaseNumber+","+line);
				
				databaseNumber++;
					
			}
						
		}
		
		br.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(exit)));
		
		for (String string : Index) {
			
			bw.write(string);
			bw.newLine();
			
		}
		
		bw.close();
	}

	private static void loadOldIndex(String oldIndex) throws IOException {
		
		indexTable = new HashSet<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(oldIndex)));
		
		String line;
		
		while ((line = br.readLine()) != null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(",")+1);
			
			indexTable.add(website);
			
		}
		
		br.close();
		
	}

	void execute(String url, String databasesInterfaces, int databaseNumber) {
		
		Thread t = new Thread(new Retriever(url,databasesInterfaces,databaseNumber));
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println(databasesInterfaces + "," + url);
		}
		
	}


}
