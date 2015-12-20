package utils.execution;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.io.FileUtils;

import exploration.model.Document;

public class ExtractionTableHandler {

	public static Hashtable<Document, String> load(File table){
		
		Hashtable<Document, String> ret = new Hashtable<Document, String>();
		
		List<String> lines = null;
		try {
			
			if (!table.exists()){
				table.createNewFile();
			}
			
			lines = FileUtils.readLines(table);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] pair;
		
		for (String line : lines) {
			
			if (line.trim().isEmpty())
				continue;
			
			pair = processLine(line);
			
//XXX COMMENTED			ret.put(pair[0], pair[1]);
						
		}
		
		
		throw new UnsupportedOperationException("Implement");

		
//		return ret;
		
	}

	private static String[] processLine(String line) {
		
		String[] pair = new String[2];
		
		int ind = line.lastIndexOf(",");
		
		pair[0] = line.substring(0, ind);

		pair[1] = line.substring(ind+1);
		
		return pair;
	
	}
	
	public static void main(String[] args) {
		
		Hashtable<Document, String> table = ExtractionTableHandler.load(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/relationExtraction/OpenCalais/TABLE/28.table"));
		
		String t = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/0/28/RESULTS/CHNH/TEDW/ALLLINKS/proxy/0/1.html";
		
		System.out.println(table.containsKey(t));
		
		System.out.println(table.size());
		
		
		
	}
	
}
