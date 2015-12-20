package sample.summary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.navigation.NavigationHandler;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.queryResultPageHandler.QueryResultPageHandler;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.ResultDocumentHandler;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.VersionEnum;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;
import sample.generation.model.impl.DummySampleConfiguration;
import utils.persistence.InteractionPersister;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;

public class sampleTupleSummaryGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		String[][] database = {{"http://www.mauconline.net/","bootstrappedSample"},{"http://www.carmeuse.com","bootstrappedSample"},{"http://diversifiedproduct.com/","bootstrappedSample"},
//				{"http://joehollywood.com/","bootstrappedSample"},{"http://sociologically.net","bootstrappedSample"},{"http://northeasteden.blogspot.com/","bootstrappedSample"},
//				{"http://www.paljorpublications.com/","bootstrappedSample"},{"http://www.brannan.co.uk/","bootstrappedSample"},{"http://www.improv.ca/","bootstrappedSample"},{"http://www.avclub.com/","boostrappedSample"},
//				{"http://www.shopcell.com/","bootstrappedSample"},{"http://www.123aspx.com/","bootstrappedSample"},{"http://www.infoaxon.com/","bootstrappedSample"},
//				{"http://www.thecampussocialite.com/","bootstrappedSample"},{"http://www.time.com/","bootstrappedSample"},{"GlobalSample-PersonCareer","Mixed"},{"News-PersonCareer-Category","ClusterMixed-Category"},{"Sports-PersonCareer-Category","ClusterMixed-Category"},
//				{"Science-PersonCareer-Category","ClusterMixed-Category"},{"Games-PersonCareer-Category","ClusterMixed-Category"},{"Reference-PersonCareer-Category","ClusterMixed-Category"},
//				{"Recreation-PersonCareer-Category","ClusterMixed-Category"},{"Arts-PersonCareer-Category","ClusterMixed-Category"},
//				{"Health-PersonCareer-Category","ClusterMixed-Category"}}; 
		
//		String[][] database = {{"http://www.ddj.com/","bootstrappedSample"},{"http://www.biostat.washington.edu/","bootstrappedSample"},{"http://micro.magnet.fsu.edu/","bootstrappedSample"},
//				{"http://www.worldenergy.org","bootstrappedSample"},{"http://travel.state.gov/","bootstrappedSample"},
//				{"http://www.aminet.net/","bootstrappedSample"},{"http://www.codecranker.com/","bootstrappedSample"},{"http://www.eulerhermes.com/","bootstrappedSample"},{"http://www.pokkadots.com/","boostrappedSample"},
//				{"http://www.muffslap.com/","bootstrappedSample"}};
		
//		String[][] database = {{"http://keep-racing.de","bootstrappedSample"}};
	
//		String[][] database = {{"http://www.canf.org/","bootstrappedSample"}};
		
		String[][] database = {{"http://www.jamesandjames.com","bootstrappedSample"}};
		
		String databaseIndex = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt";
		
		databaseWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");
				
		InteractionPersister interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/",databaseIndex,pW);

		ContentExtractor ce = new TikaContentExtractor();
		
		String[] relations = new String[]{"PersonCareer"};
		
		int sample_number = 1;
		
		int wload = 6;
		
		WorkloadModel wm = new WorkloadModel(wload,"/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.ds","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.wl","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadQueries","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadRelations");
		
		Version v = Version.generateInstance(VersionEnum.INDEPENDENT.name(), wm);
		
		for (int i = 0; i < database.length; i++) {
			
			Database db = pW.getDatabaseByName(database[i][0]);
			
			OCRelationExtractionSystem extractionSystem = (OCRelationExtractionSystem)new OCRelationExtractionSystem(pW).createInstance(db,interactionPersister,ce,relations);//Id, relation, interactionPersister.getExtractionTable(website,Id), interactionPersister.getExtractionFolder(website,Id));

//			Sample sample = Sample.getSample(db, v, wm, sample_number,new DummySampleConfiguration(1));
			
			Sample sample = null;
			
			List<Document> sampleFiles = null;
			
//	COMMENTED		List<Document> sampleFiles = FileUtils.readLines(new File(pW.getSampleFilteredFile(sample)));
//TODO join all the samples...?
//XXX class to be modified			File output = new File(pW.getMatchingTuplesWithSourcesFile(db.getName(), v.getName(), wm));
			
			String output = "";
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			
			for (Document doc : sampleFiles) {
				
				Tuple[] tuples = extractionSystem.execute(/*db.getId(), */doc);
				
				for (int j = 0; j < tuples.length; j++) {
					
					bw.write(doc + "," + tuples[j].toString());
					
					bw.newLine();
				
				}
				
			}
			
			bw.close();
			
		}
		
	}


}
