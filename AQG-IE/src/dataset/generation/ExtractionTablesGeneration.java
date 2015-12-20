package dataset.generation;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import edu.columbia.cs.ref.model.re.Model;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;

public class ExtractionTablesGeneration {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int group = Integer.valueOf(args[0]); //-10 has all the databases

		persistentWriter pW = PersistenceImplementation.getWriter();

		ContentExtractor ce = new TikaContentExtractor();
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		
		int[] relationConfs = new int[]{7,8,9,11,12}; //Omit 4 because has been too expensive... (?)
		
		int[] informationExtractionIds = new int[]{17,19};

		for (Database database : databases) {
			System.out.println("Db: " + database.getId());
			for (int i = 0; i < relationConfs.length; i++) {
				System.out.println("Rel: " + i);
				int relationExp = relationConfs[i];
				for (int k = 0; k < informationExtractionIds.length; k++) {
					System.out.println("Ie: " + k);					
					int informationExtractionId = informationExtractionIds[k];
					TupleRelationExtractionSystem rel = (TupleRelationExtractionSystem)RelationConfiguration.getRelationExtractionSystem(relationExp,pW,informationExtractionId,true,false,database.getId(),ce);
					
					try{
						//use buffering


						OutputStream file = new FileOutputStream( "cachingExperiments/" + database.getId() + "-" + getInfo(informationExtractionId) + "-" + getRel(relationExp) + ".ser" );
						OutputStream buffer = new BufferedOutputStream( file );
						ObjectOutput output = new ObjectOutputStream( buffer );

						try{
							output.writeObject(createTupleTable(rel.getInternalTuples(),rel.getTable()));
						}
						finally{
							output.close();
						}
					}  
					catch(IOException ex){
						ex.printStackTrace();
					}
					
					System.gc();
				}
			}
			
		}

	}

	private static Map<Long,List<Tuple>> createTupleTable(
			Map<String, List<Tuple>> internalTuples, Map<Document, String> table) {
		
		Map<Long,List<Tuple>> tups = new HashMap<Long, List<Tuple>>();
		
		for (Entry<Document,String> entry : table.entrySet()) {
			List<Tuple> tuples = internalTuples.get(entry.getValue());
			if (!tuples.isEmpty())
				tups.put(entry.getKey().getId(), tuples);
		}
		
		return tups;
	}

	private static String getRel(int relationExp) {
		return RelationConfiguration.getRelationName(relationExp);
	}

	private static String getInfo(int informationExtractionId) {
		if (informationExtractionId == 17)
			return "BONG";
		return "SSK";
	}

}
