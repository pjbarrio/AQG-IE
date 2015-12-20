package execution.workload.impl.wordextraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import utils.FileHandlerUtils;
import utils.word.extraction.WordExtractorAbs;
import utils.word.extraction.WordTokenizer;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

import execution.informationExtraction.ExtractionSystem;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;
import exploration.model.Document;
import exploration.model.DocumentHandle;

public class RDFWordExtractor extends WordExtractorAbs {

	private static final String TYPE_PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String TYPE_TEXT = "type";
	private static final String TABLE_SEPARATOR = ",";
	private String relationText;
	private Hashtable<Document, String> hashTable;
	private ExtractionSystem oce;
	private UsefulCondition condition;
	private WordTokenizer wt;
	
	public RDFWordExtractor(String relation, String table, ExtractionSystem oce, UsefulCondition condition){
			
		this.relationText = "http://s.opencalais.com/1/type/em/r/" + relation;
		this.oce = oce;
		this.condition = condition;
		loadTable(table);
		wt = new WordTokenizer();
	}
	
	private void loadTable(String table) {
		
		hashTable = new Hashtable<Document, String>();
		
		ArrayList<String> lines = FileHandlerUtils.getAllResourceNames(new File(table));
		
		for (String line : lines) {
			
			System.out.println(table + " ---- " + line);
			
			String[] pair = processLine(line);
			
//XXX COMMENTED			hashTable.put(pair[0], pair[1]);
			
			throw new UnsupportedOperationException("Implement");

		}
		
	}

	private String[] processLine(String line) {
		
		String[] pair = new String[2];
			
		int ind = line.lastIndexOf(TABLE_SEPARATOR);
		
		pair[0] = line.substring(0, ind);
		pair[1] = line.substring(ind+1);
		
		return pair;
	}
	
	@Override
	protected String[] _getWords(Document document) {
		
		Tuple[] tuples = oce.execute(document); 
		
		ArrayList<Integer> indexes = generateIndexes(tuples);
		
		String file = hashTable.get(document);
		
		file = "file://" + file;
		
		Graph g = new GraphMem();
		ModelCom model = new ModelCom(g);
		
		try {
		
		model.read(file);
		
		}
		catch (Exception e) {
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/pjbarrio/workspace/Utils/BADFILES.txt"),true));
				bw.write(file + "\n");
				bw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
						
			return new String[0];
		}
		
		Node nn = Node.createURI(relationText);
		
		ResIterator resIt = model.listResourcesWithProperty(new PropertyImpl(TYPE_PROPERTY,TYPE_TEXT), new LiteralImpl(nn, model));
		
		String ReducedText = "";
		
		int tupleindex = 0;
		
		while (resIt.hasNext()){
		
			Resource res = resIt.next();

			if (!indexes.contains(tupleindex))
				continue;
			
			Node complete = Node.createURI(res.toString());
			
			ResIterator resItComplete = model.listResourcesWithProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","subject"), new LiteralImpl(complete, model));
		
			while (resItComplete.hasNext()){
			
				Resource res2 = resItComplete.next();
				
				String prefix = res2.getProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","prefix")).getObject().toString();
				String exact = res2.getProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","exact")).getObject().toString();
				String suffix  = res2.getProperty(new PropertyImpl("http://s.opencalais.com/1/pred/","suffix")).getObject().toString();
				
				ReducedText = ReducedText + prefix + " " + exact + " " + suffix + " ";
				
			}
			
			tupleindex++;
			
		}
		
		return processWords(wt.getWords(ReducedText));
		
	}

	private ArrayList<Integer> generateIndexes(Tuple[] tuples) {
		
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		for (int i = 0; i < tuples.length; i++) {
			
			if (condition.isItUseful(tuples[i])){
				
				ret.add(i);
				
			}
			
		}
		
		return ret;
		
	}

	@Override
	protected String[] _getWords(String string){
		return wt.getWords(string);
	}

}
