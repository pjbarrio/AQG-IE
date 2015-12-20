package utils.id.useful;

import java.io.File;
import java.io.IOException;
import java.util.List;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;

import org.apache.commons.io.FileUtils;

import utils.persistence.databaseWriter;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import extraction.relationExtraction.impl.RDFRelationExtractor;

public class GenerateTuplesList {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		String[] database = {"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/"};
//		String[] database = {"http://joehollywood.com/","http://travel.state.gov/","http://northeasteden.blogspot.com/"};
//		String[] database = {"http://www.muffslap.com/","http://www.paljorpublications.com/", "http://www.biostat.washington.edu/"};
//		String[] database = {"http://www.brannan.co.uk/","http://www.improv.ca/","http://www.avclub.com/"};
//		String[] database = {"http://www.shopcell.com/","http://keep-racing.de","http://www.worldenergy.org","http://www.infoaxon.com/"};
//		String[] database = {"http://www.codecranker.com/", "http://www.canf.org/","http://www.thecampussocialite.com/"};
		String[] database = {"http://micro.magnet.fsu.edu/","http://www.jamesandjames.com",	"http://www.pokkadots.com/","http://www.time.com/"}; 
		
		databaseWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");
				
		String[] relations = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel","PollsResult",
				"VotingResult","ProductIssues","Quotation"};
				
		for (int i = 0; i < database.length; i++) {
			
			Database db = pW.getDatabaseByName(database[i]);
			
			RDFRelationExtractor rdf = new RDFRelationExtractor();
						
			String output = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/relationExtraction/OpenCalais/" + db.getName();
			
			File[] e = new File(output).listFiles();
			
			int indx = 0;
			
			for (File doc : e) {
				
				if (indx % 100 == 0)
					System.out.println(db.getIndex() + " - File: " + indx + " of " + e.length);
				
				indx++;
				
				for (int j = 0; j < relations.length; j++) {
					
					List<Tuple> tuples = rdf.extract(relations[j], FileUtils.readFileToString(doc));
					
					File outp = pW.getTuplesInDatabaseFile(db.getName(), relations[j]);
					
					for (int k = 0; k < tuples.size(); k++) {

						FileUtils.write(outp, tuples.get(k).toString() + "\n", true);
									
					}
					
				}
				
				
			}
			
		}
		
	}


}
