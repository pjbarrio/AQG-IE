package utils.id.useful;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import utils.FileHandlerUtils;
import utils.persistence.databaseWriter;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import extraction.relationExtraction.impl.RDFRelationExtractor;

public class GenerateTuplesSample {

	private static HashMap<String, Set<String>> table;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] database = {"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/","http://joehollywood.com/",
				"http://travel.state.gov/","http://northeasteden.blogspot.com/","http://www.muffslap.com/","http://www.paljorpublications.com/",
				"http://www.biostat.washington.edu/","http://www.brannan.co.uk/","http://www.improv.ca/","http://www.avclub.com/",
				"http://www.shopcell.com/","http://keep-racing.de","http://www.worldenergy.org","http://www.infoaxon.com/","http://www.codecranker.com/",
				"http://www.canf.org/","http://www.thecampussocialite.com/","http://micro.magnet.fsu.edu/","http://www.jamesandjames.com",
				"http://www.pokkadots.com/","http://www.time.com/"}; 
		
		databaseWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");
		
		String[] relations = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel","PollsResult",
				"VotingResult","ProductIssues","Quotation"};
				
		for (int i = 0; i < database.length; i++) {
			
			Database db = pW.getDatabaseByName(database[i]);
			
			for (int j = 0; j < relations.length; j++) {
				
				File outp = pW.getTuplesInDatabaseFile(db.getName(), relations[j]);
				
				if (!outp.exists())
					continue;
					
				getForRelation(relations[j]).addAll(FileUtils.readLines(outp));
				
				
			}
						
		}

		for (Entry<String, Set<String>> entry : getTable().entrySet()) {
			
//			System.out.println("\n\n\nRelation: " + entry.getKey());
//			
//			System.out.println("\n\n\n");
//			int i = 0;
//			
//			for (String string : entry.getValue()) {
//				
//				if (i < 20){
//					System.out.println(string);
//					
//					i++;
//					
//				}else{
//					break;
//				}
//				
//			}
			
			System.out.println(entry.getKey() + " - " + entry.getValue().size());
			
		}
		
	}

	private static Set<String> getForRelation(String string) {
		
		Set<String> r = getTable().get(string);
		
		if (r == null){
			r = new HashSet<String>();
			
			getTable().put(string, r);
		}
		
		return r;
	}

	private static Map<String,Set<String>> getTable() {

		if (table == null){
		
			table = new HashMap<String, Set<String>>();
		
		}
		
		return table;
	}

}
