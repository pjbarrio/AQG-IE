package execution.informationExtraction;


import java.io.File;
import java.util.ArrayList;

import utils.FileHandlerUtils;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.WorkloadModel;
import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class OpenCalaisExtractor extends ExtractionSystemImpl {

	private OpenCalaisRelationExtractor oCRE;

	private String Relation;

	private String auxPrefix;

	private String actualExperiment;

	public OpenCalaisExtractor(String Id, String Relation, String table, String auxPrefix, String actualExperiment) {
		
		super(Id);
		
		oCRE = null;//new OpenCalaisRelationExtractor("src/extraction/calaisParams.xml",table);
		
		this.Relation = Relation;
		
		this.auxPrefix = auxPrefix.replaceAll("[\\W]", "_");
		
		this.actualExperiment = actualExperiment;
		
	}



	@Override
	public Tuple[] execute(Document document) {
		
		String output = null;//oCRE.IEProcess(handle.getDocHandle(),new String (actualExperiment + auxPrefix + "-auxextract.txt"));
		
		ArrayList<ArrayList<String>> tuples = null;//oCRE.extractTuples(Relation, output);
		
		Tuple[] ts = new Tuple[tuples.size()];
		
		int pos = 0;
		
		for (ArrayList<String> arrayList : tuples) {
			
			Tuple t = new Tuple();
			
			for (String attributeValue : arrayList) {
				
				String[] attv = null;//OpenCalaisRelationExtractor.processAttributeValue(attributeValue);
				
				if (attv.length > 1)
				
					t.setTupleField(attv[0], attv[1]);
				else
					
					System.out.println(document + " , " + attributeValue);
			}
		
			ts[pos++] = t;
			
		}
		
		return ts;
		
	}



	public static String getTable(persistentWriter pW, Database evaluableDatabase) {
		
		return pW.getDatabaseExtractionTable(evaluableDatabase);
		
	}



	public static String getRelation(WorkloadModel workload) {
		ArrayList<String> relations = FileHandlerUtils.getAllResourceNames(new File(workload.getRelationsFile()));
		return relations.get(0);
	}

}
