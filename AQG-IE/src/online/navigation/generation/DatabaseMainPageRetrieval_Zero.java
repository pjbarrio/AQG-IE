package online.navigation.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import online.documentRetriever.download.Downloader;

public class DatabaseMainPageRetrieval_Zero {

	private static Hashtable<Integer, String> indexTable;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String databaseIndex = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt";
		
		String outputFolder = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Wrappers/Data/";
		
		loadDatabaseIndex(databaseIndex);

		for (Entry<Integer,String> entry : indexTable.entrySet()) {
			
			System.out.println("Database: " + entry.getKey());
			
			File f = new File(outputFolder,entry.getKey() + ".html");
			
			if (f.exists())
				continue;
			
			new DatabaseMainPageRetrieval_Zero().retrieve(f,entry.getValue());
			
		}
		
	}

	private void retrieve(final File f, final String url) {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				
				try {
					
					String content = new Downloader().download(new URL(url));
					
					FileUtils.write(f, content);
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
		
		
	}

	private static void loadDatabaseIndex(String databaseIndex) throws IOException {

		indexTable = new Hashtable<Integer, String>();
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(databaseIndex));
		
		while ((line=br.readLine())!=null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			indexTable.put(index, website);
		}

		br.close();
		
	}
	
}
