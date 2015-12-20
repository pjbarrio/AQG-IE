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
import java.util.List;

import utils.persistence.PersistenceImplementation;

public class FinalDatabasesRetrieverFromIndex {

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
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		PersistenceImplementation.getWriter()
		
		String databases = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/NoInputIndex";
		
		String databasesInterfaces = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/DataNoInput/";
		
		BufferedReader br = new BufferedReader(new FileReader(new File(databases)));

		String line;
		
		while ((line = br.readLine()) != null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			System.err.println(line);
			
			new FinalDatabasesRetriever().execute(website,databasesInterfaces,index);
			
			
		}
		
		br.close();
		
	}

	private void execute(String url, String databasesInterfaces, int databaseNumber) {
		
		Thread t = new Thread(new Retriever(url,databasesInterfaces,databaseNumber));
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println(databasesInterfaces + "," + url);
		}
		
	}


}
