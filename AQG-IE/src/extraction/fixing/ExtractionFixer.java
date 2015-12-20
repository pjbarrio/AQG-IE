package extraction.fixing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import extraction.com.clearforest.ExtractorRunnable;
import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class ExtractionFixer {

	private static Hashtable<String, String> table;
	private static ArrayList<String> badfiles;
	private static final String preffix = "file://";
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		args = new String[2];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/OCOutput/TheEconomist.table";
		args[1] = "/home/pjbarrio/workspace/Utils/BADFILES.txt";
		
		loadBadFiles(args[1]);
		
		loadTable(args[0]);
		
		processDocuments();
	}

	private static void loadBadFiles(String file) throws IOException {
		
		badfiles = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String line = br.readLine();
		
		while (line!=null){
			
			badfiles.add(line.replace(preffix, ""));
			
			line = br.readLine();
		}
		
		br.close();
	}

	private static void processDocuments() {
		
		for (Enumeration<String> e = table.keys();e.hasMoreElements();){
			
			String rdf = e.nextElement();
			
			String file = table.get(rdf);
			
			try {
				Thread.sleep(OpenCalaisRelationExtractor.WAITING_CONCURRENT_TIME);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			
			ExtractorRunnable er = null;//new ExtractorRunnable(file, "fixing", rdf, OpenCalaisRelationExtractor.licenseID, "/home/pjbarrio/workspace/opencalais/calaisParams.xml");
			
			new Thread(er).start();
		}
		
	}

	private static void loadTable(String file) throws IOException {
		
		table = new Hashtable<String, String>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String line = br.readLine();
		
		while (line!=null){
			
			String[] pair = null;//OpenCalaisRelationExtractor.processLine(line);
			
			if (badfiles.contains(pair[1])){

				System.out.println("Loaded: " + pair[0] + " <--> " + pair[1]);
				
				table.put(pair[1], pair[0]);
				
			}
			line = br.readLine();
		
		}
		
		br.close();
		
	}

}
