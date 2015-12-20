package sample.generation.relation.trec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import execution.workload.tuple.Tuple;
import extraction.relationExtraction.impl.RDFRelationExtractor;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class GenerateSplit {

	private static final int ERROR = 0;
	private static final int USEFUL = 1;
	private static final int USELESS = -1;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//First Algorithm of the sequence. Detects Useful Documents. Continue to GenerateDocumentList
		
		File[] files = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset_from_dbNoBackup/TREC/Extraction/").listFiles();

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		String extractor = "OpenCalais";
		
		Map<String, BufferedWriter> mapbw = new HashMap<String, BufferedWriter>();
		
		process(files,mapbw,pW,collection,extractor);
		
		for (BufferedWriter bws : mapbw.values()) {
			bws.close();
		}
		

	}

	private static void process(File[] files, Map<String, BufferedWriter> mapbw, persistentWriter pW, String collection, String extractor) throws IOException {
		
		for (int i = 0; i < files.length; i++) {

			File f = files[i];
			
			if (i % 1000 == 0){
				System.out.println("Processed: " + i + " of " + files.length + " in (" + f.getAbsolutePath() + ").");
			}
						
			if (f.isFile() && f.getName().endsWith(".rdf")){
				
				BufferedReader br = new BufferedReader(new FileReader(f));
				
				String line = br.readLine();
				
				br.close();
				
				String[] spls = line.split("<!--Relations: ");
				
				if (spls.length > 1){
					String[] rels = spls[1].split(", ");
					for (String rel : rels) {
						
						BufferedWriter freq = mapbw.get(rel);
						
						if (freq == null) {
							freq = new BufferedWriter(new FileWriter(pW.getUsefulDocumentsForCollection(collection,rel,extractor)));
							mapbw.put(rel, freq);
						}
						
						freq.write(f.getAbsolutePath().replace("/proj/db-files2/NoBackup/pjbarrio/Dataset_from_dbNoBackup/TREC/Extraction/", ""));
						freq.newLine();
						
					}
				}
				
			} else if (f.isDirectory()){
				
				process(f.listFiles(), mapbw, pW,collection, extractor);
				
			}
			
		}
		
	}

}
