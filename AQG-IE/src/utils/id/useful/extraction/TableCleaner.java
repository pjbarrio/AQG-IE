package utils.id.useful.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import exploration.model.Document;

import utils.execution.ExtractionTableHandler;
import utils.id.TuplesLoader;

public class TableCleaner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/relationExtraction/OpenCalais/TABLE/";
		
		for (int i = 0; i < 2900; i++) {
			
			String table = prefix + i + "-2.table";
			
			String table2 = prefix + i + ".table";
			
			File f = new File(table);
			
			if (!f.exists())
				continue;
			
			System.out.println(i);

			Hashtable<Document,String> t = ExtractionTableHandler.load(f);
			
			Hashtable<String,Document> t2 = new Hashtable<String, Document>();
			
			Set<String> toRemove = new HashSet<String>();
			
			for (Entry<Document,String> e : t.entrySet()) {
				
				if (t2.containsKey(e.getValue())){
					toRemove.add(e.getValue());
				}else{
					t2.put(e.getValue(), e.getKey());
				}
			}
			
			try {
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(table2)));
				
				for (Entry<String,Document> e : t2.entrySet()) {
					
					if (!toRemove.contains(e.getKey())){
						
//COMMENTED FOR ERROR HIDING						bw.write(e.getValue() + "," + e.getKey() xxxx);
						
						bw.newLine();
						
					}
					
				}
				
				bw.close();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			throw new UnsupportedOperationException("IMPLEMENT!");

			
		}
	
	}

}
