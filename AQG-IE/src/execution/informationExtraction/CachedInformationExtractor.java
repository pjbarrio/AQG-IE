package execution.informationExtraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import utils.id.Idhandler;
import utils.id.TuplesLoader;
import utils.persistence.persistentWriter;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Version;
import exploration.model.WorkloadModel;

public class CachedInformationExtractor extends ExtractionSystemImpl {

	private Database database;
	private WorkloadModel workloadModel;
	private HashSet<Long> useful;
	private Hashtable<Document, ArrayList<String>> docTuples;

	public CachedInformationExtractor(String Id, Database database, Version version,WorkloadModel workloadmodel, persistentWriter pW) {
		
		super(Id);
		this.database = database;
		this.workloadModel = workloadmodel;
			
//XXX class not used		docTuples = TuplesLoader.loadIdtuplesTable(pW.getMatchingTuplesWithSourcesFile(database.getName(), 
//				version.getName(),workloadmodel));
			
	}

	@Override
	public Tuple[] execute(Document document) {
		
		ArrayList<String> tuple = docTuples.get(document);
		
		if (tuple == null)
			return new Tuple[0];
		
		Tuple[] ret = new Tuple[tuple.size()];
		
		int i = 0;
		
		for (String tup : tuple) {
		
			ret[i] = TupleReader.generateTuple(tup);
			
			i++;
		}
		
		return ret;
	
	}

}
