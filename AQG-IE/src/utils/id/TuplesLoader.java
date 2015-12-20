package utils.id;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.dummy.DummyDatabase;

import utils.FileHandlerUtils;

public class TuplesLoader {

	private static int numberOfTuples;
	private static int numberOfUsefulDocuments;

	public static Hashtable<Long, ArrayList<String>> loadIdtuplesTable(String tuples) {
		
		Hashtable<Long, ArrayList<String>> docTuplesTable = new Hashtable<Long, ArrayList<String>>();
		
		ArrayList<String> tuplesString = FileHandlerUtils.getAllResourceNames(new File(tuples));
		
		String tup;
		
		Long id;
		
		numberOfUsefulDocuments = 0;
		numberOfTuples = tuplesString.size();
		
		while (tuplesString.size()>0){
			
			String line = tuplesString.remove(0);
			
			id = Long.valueOf(line.substring(0, line.indexOf(',')));
			
			tup = line.substring(line.indexOf(',')+1,line.length());
			
			ArrayList<String> aux = docTuplesTable.get(id);
			
			if (aux == null){
				aux = new ArrayList<String>();
				numberOfUsefulDocuments++;
			}
			
			aux.add(tup);
			
			docTuplesTable.put(id, aux);
			
		}
		
		return docTuplesTable;
		
	}

	public static double getNumberOfTuples() {
		
		return numberOfTuples;
		
	}

	public static double getNumberOfDocuments() {
		
		return numberOfUsefulDocuments;
		
	}

	public static Hashtable<Document, ArrayList<Tuple>> loadDocumenttuplesTuple(Database database, String tuplesandSources) throws IOException {
		
		Hashtable<Document, ArrayList<Tuple>> idtuplesTable = new Hashtable<Document, ArrayList<Tuple>>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(tuplesandSources)));
		
		String line = br.readLine();
		
		ArrayList<Tuple> tuples;
		
		while (line!=null){
			
			String id = line.substring(0, line.indexOf(','));
			
			Document d = new Document(database, Long.valueOf(id));
			
			String tuple = line.substring(line.indexOf(',')+1);
			
			tuples = idtuplesTable.remove(d);
			
			if (tuples == null){
				
				tuples = new ArrayList<Tuple>();
				
			}
			
			tuples.add(TupleReader.generateTuple(tuple));
						
			idtuplesTable.put(d, tuples);
			
			line = br.readLine();
			
		}
		
		br.close();
	
		return idtuplesTable;
	}
	
}
