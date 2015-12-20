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

import com.google.gdata.util.common.base.Pair;

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

public class ExtractionTablesStatistics {

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
			
			System.out.print("\n" + database.getId());
			
			List<Pair<Integer, String>> list = (List<Pair<Integer,String>>)StatisticsFromCollection.deserialize("cachingExperiments/" + database.getId() + ".ser");

			if (list == null)
				continue;
			
			System.out.print(", " + list.size());
			
			for (int i = 0; i < relationConfs.length; i++) {
				int relationExp = relationConfs[i];
				
//				System.out.print(", " + getRel(relationExp));
				
				for (int k = 0; k < informationExtractionIds.length; k++) {
					
					int informationExtractionId = informationExtractionIds[k];
					
					String name = database.getId() + "-" + getInfo(informationExtractionId) + "-" + getRel(relationExp);
					
//					System.out.print(", " + getInfo(informationExtractionId));
					
					String file = "cachingExperiments/" + name + ".ser";
					
					Map<Long,List<Tuple>> map = (Map<Long, List<Tuple>>) StatisticsFromCollection.deserialize(file);
					
					int count = 0;
					
					if (map.size() > 0){
					
						for (int j = 0; j < list.size(); j++) {
							
							if (map.containsKey(list.get(j).first))
								count++;
						}
						
					}

					System.out.print("," + count);
											
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
