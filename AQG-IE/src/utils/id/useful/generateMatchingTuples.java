package utils.id.useful;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;

import org.apache.commons.io.FileUtils;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;

public class generateMatchingTuples {

	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int min_sample_configuration = 92;
		int max_sample_configuration = 421;

		String[] database = {"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/","http://joehollywood.com/",
				"http://travel.state.gov/","http://northeasteden.blogspot.com/","http://www.muffslap.com/","http://www.paljorpublications.com/",
				"http://www.biostat.washington.edu/","http://www.brannan.co.uk/","http://www.improv.ca/","http://www.avclub.com/",
				"http://www.shopcell.com/","http://keep-racing.de","http://www.worldenergy.org","http://www.infoaxon.com/","http://www.codecranker.com/",
				"http://www.canf.org/","http://www.thecampussocialite.com/","http://micro.magnet.fsu.edu/","http://www.jamesandjames.com",
				"http://www.pokkadots.com/","http://www.time.com/"}; 		
		
//		String[] database = {/*"http://northeasteden.blogspot.com/",*/"http://www.time.com/"};
		
		String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/};
		
		int[] workload = {/*1,2,3,4,5*/6};
		
		int[] sample_number = {1,2,3,4,5};
		
		pW = PersistenceImplementation.getWriter();
		
		OCRelationExtractionSystem relSys = new OCRelationExtractionSystem(pW);
		
		InteractionPersister interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/",
				"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
		
		ContentExtractor contentExtractor = new TikaContentExtractor();
		
		String[] relations = {"PersonCareer"};
		
		for (int i = 0; i < database.length; i++) {
			
			Database dbase = pW.getDatabaseByName(database[i]);

			System.out.println("Database " + dbase.getId() + " - " + i + " - " + database.length);
			
			RelationExtractionSystem rsys = relSys.createInstance(dbase, interactionPersister, contentExtractor, relations);
			
			for (int w = 0; w < workload.length; w++) {
				
				String saveUseful = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/Useful/" + workload[w] + "_added/";
				
				WorkloadModel wm = new DummyWorkload(workload[w]);
				
				for (int j = 0; j < version.length; j++) {
			
					Version ver = new DummyVersion(version[j]);
					
					String matchingTuples = pW.getMatchingTuplesWithSourcesFile(dbase.getName(), ver.getName(), wm);
					
					Set<Document> documents = new HashSet<Document>();
					
					for (int sconf = min_sample_configuration; sconf <= max_sample_configuration; sconf++) {
						
						for (int s = 0; s < sample_number.length; s++) {
							
							Sample sample = Sample.getSample(dbase,ver,wm,sample_number[s],new DummySampleConfiguration(sconf));
								
							String usefulDoc = pW.getSampleUsefulDocuments(sample);
									
							if (!new File(usefulDoc).exists()){
								continue;
							}
							
							List<Document> usefulDocs = FileUtils.readLines(new File(usefulDoc));
							
							documents.addAll(usefulDocs);
							
						}
													
					}
					
//					FileUtils.writeLines(new File(saveUseful + dbase.getId() + ""), documents);
//					
//					continue;
					
					BufferedWriter bw = new BufferedWriter(new FileWriter(new File(matchingTuples)));
					
					int ttt = 0;
					
					for (Document doc : documents) {
						
						System.out.println(ttt++ + " - " + documents.size());
						
						Tuple[] tuples = rsys.execute(/*dbase.getId(), */doc);
						
						for (int k = 0; k < tuples.length; k++) {
							
							bw.write(doc + "," + tuples[k].toString());
							
							bw.newLine();
							
						}		
								
					}
					
					bw.close();
					
					documents.clear();
					
				}
						
			}

			rsys.clear();
			
		}
	
	}

}
