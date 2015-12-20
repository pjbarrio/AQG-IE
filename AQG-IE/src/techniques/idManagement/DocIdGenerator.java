package techniques.idManagement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import utils.id.Idhandler;
import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class DocIdGenerator {

	private static Idhandler idhandler;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String database = "UsNews";
		
		String dbclass = "General";
		
		args = new String[3];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/"+ database + ".id";
		
		args[1] = "/proj/db/NoBackup/pjbarrio/sites/"+dbclass+"/"+database+"/";
		
		args[2] = "/proj/db/NoBackup/pjbarrio/OCOutput/"+database+".table";
			
		idhandler = new Idhandler(args[0], false);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(args[2])));
		
		String line = br.readLine();
		
		while (line!=null){
			
			if (line.trim().equals("")){
				
				line = br.readLine();
				
				continue;
			}
			String[] pair = null;//OpenCalaisRelationExtractor.processLine(line);
			
			idhandler.addDocument(pair[0]);
			
			line = br.readLine();
			
		}
		
		br.close();
		
//		idhandler.printFile();

	}

}
