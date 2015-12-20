package utils.id.useful.extraction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import utils.execution.ExtractionTableHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

import execution.workload.querygeneration.TextQueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;

public class TableMigrator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/relationExtraction/OpenCalais/TABLE/";
		
		String[][] databases = {{"http://www.mauconline.net/","bootstrappedSample"},{"http://www.carmeuse.com","bootstrappedSample"},{"http://diversifiedproduct.com/","bootstrappedSample"},
				{"http://joehollywood.com/","bootstrappedSample"}/*,{"http://sociologically.net","bootstrappedSample"}*/,{"http://northeasteden.blogspot.com/","bootstrappedSample"},
				{"http://www.paljorpublications.com/","bootstrappedSample"},{"http://www.brannan.co.uk/","bootstrappedSample"},{"http://www.improv.ca/","bootstrappedSample"},{"http://www.avclub.com/","boostrappedSample"},
				{"http://www.shopcell.com/","bootstrappedSample"},{"http://www.123aspx.com/","bootstrappedSample"},{"http://www.infoaxon.com/","bootstrappedSample"},
				{"http://www.thecampussocialite.com/","bootstrappedSample"},{"http://www.time.com/","bootstrappedSample"},{"http://www.ddj.com/","bootstrappedSample"},{"http://www.biostat.washington.edu/","bootstrappedSample"},{"http://micro.magnet.fsu.edu/","bootstrappedSample"},
				{"http://www.worldenergy.org","bootstrappedSample"},{"http://travel.state.gov/","bootstrappedSample"},{"http://www.aminet.net/","bootstrappedSample"},{"http://www.codecranker.com/","bootstrappedSample"},{"http://www.eulerhermes.com/","bootstrappedSample"},{"http://www.pokkadots.com/","boostrappedSample"},
				{"http://www.muffslap.com/","bootstrappedSample"},{"http://keep-racing.de","bootstrappedSample"},{"http://www.canf.org/","bootstrappedSample"},{"http://www.jamesandjames.com","bootstrappedSample"}};

		int idExtractionSystem = 1;

		for (int i = 0; i < databases.length; i++) {
			
			Database d = pW.getDatabaseByName(databases[i][0]);
			
			String tableFile = prefix + d.getId() + ".table";
			
			Hashtable<Document,String> t = ExtractionTableHandler.load(new File(tableFile));
			
			for (Entry<Document, String> entry : t.entrySet()) {
				
				pW.insertExtraction(idExtractionSystem,entry.getKey(),entry.getValue(),null);
				
			}
			
		}
		
	}

}
