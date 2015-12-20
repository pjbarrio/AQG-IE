package collection.processing.indetifying.offline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import utils.id.Idhandler;

import execution.workload.tuple.Tuple;
import exploration.model.Document;
import extraction.relationExtraction.impl.RDFRelationExtractor;

public class MathchingTuplesDetectorRunnable implements Runnable {

	private Hashtable<Document, String> extractionTable;
	private File outputFile;
	private String relation;
	private RDFRelationExtractor extractionSystem;
	private Idhandler idhandler;

	public MathchingTuplesDetectorRunnable(
			Hashtable<Document, String> extractionTable, File outputFile,
			String relation, Idhandler idhandler) {
		
		this.extractionTable = extractionTable;
		this.outputFile = outputFile;
		this.relation = relation;
		
		extractionSystem = new RDFRelationExtractor();
		this.idhandler = idhandler;
	}

	@Override
	public void run(){
		
		try {
		
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			int i = 0;
			
			for (Entry<Document, String> entry : extractionTable.entrySet()) {
				
				if (i%1000 == 0){
					
					System.out.println(i + " - " + outputFile);
					
				}
				
				i++;
				
				String f = entry.getKey();
				
				File te = new File(entry.getValue());
				
				List<Tuple> tuples = extractionSystem.extract(relation, FileUtils.readFileToString(te));
				
				if (tuples != null && tuples.size() > 0){
				
					Long id = idhandler.get(f);
					
					for (Tuple tuple : tuples) {
						
						bw.write(id + "," + tuple.toString());
						
						bw.newLine();
						
					}
					
				}
				
			}
			
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
