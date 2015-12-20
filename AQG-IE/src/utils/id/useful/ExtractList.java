package utils.id.useful;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.io.FileUtils;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.execution.ExtractionTableHandler;
import utils.execution.runningExtractors.RunExtractor.ExtractRunnable;
import utils.id.TuplesLoader;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;
import extraction.com.clearforest.CalaisLocator;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;

public class ExtractList {

	public class ExtractRunnable implements Runnable{

		private File toSave;
		private File toSaveExtraction;

		public ExtractRunnable(File toSave, File toSaveExtraction) {
			this.toSave = toSave;
			this.toSaveExtraction = toSaveExtraction;
		}

		@Override
		public void run() {
			
			String result;
			
			try {
				
				int attemptsTotal = 3;

				int attempts = 0;

				do{

					try {
						Thread.sleep(3000 * attempts);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					ContentExtractor ce = new TikaContentExtractor();
					
					result = new CalaisLocator().getcalaisSoap().enlighten(licenseID , ce.extractContent(FileUtils.readFileToString(toSave)), paramsXML);
				
					attempts++;
					
				} while (result.startsWith("<Error") && attempts <= attemptsTotal); 
					
				FileUtils.writeStringToFile(toSaveExtraction, result);
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		}
		
	}
	
	private static String paramsXML;
	private static String licenseID = "shf6uggkkp2au4jexwej9hbt";
	private static persistentWriter pW;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		paramsXML = FileUtils.readFileToString(new File("/proj/dbNoBackup/pjbarrio/workspace/SampleGeneration/data/calaisParams.xml"));
		
		String[] database = {"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/","http://joehollywood.com/",
				"http://travel.state.gov/","http://northeasteden.blogspot.com/","http://www.muffslap.com/","http://www.paljorpublications.com/",
				"http://www.biostat.washington.edu/","http://www.brannan.co.uk/","http://www.improv.ca/","http://www.avclub.com/",
				"http://www.shopcell.com/","http://keep-racing.de","http://www.worldenergy.org","http://www.infoaxon.com/","http://www.codecranker.com/",
				"http://www.canf.org/","http://www.thecampussocialite.com/","http://micro.magnet.fsu.edu/","http://www.jamesandjames.com",
				"http://www.pokkadots.com/","http://www.time.com/"};
		
		int[] initials = {84010,102141};
		
		String[] version = {"INDEPENDENT"/*,"DEPENDENT"*/};
		
		int[] workload = {/*1,2,3,4,5*/6};
		
		pW = PersistenceImplementation.getWriter();
		
		for (int i = 0; i < database.length; i++) {
			
			Database dbase = pW.getDatabaseByName(database[i]);

			String table = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/relationExtraction/OpenCalais/TABLERENEW/" + dbase.getId() + ".usefultable";
			
			File folder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/relationExtraction/OpenCalais/" + dbase.getId());
			
			Hashtable<Document, String> tb = ExtractionTableHandler.load(new File(table));

			int initial = initials[i];
			
			List<String> useful = FileUtils.readLines(new File(""+dbase.getId()));
			
			for (int j = 0; j < useful.size(); j++) {
				
				System.out.println("Useful: + " + j + " out of " + useful.size());
				
				String f = tb.get(useful.get(j));
				
				if (f == null || !new File(f).exists()){
					
					File toSave;
					
					if (f == null){
					
						initial++;
						
						toSave = new File(folder,initial + ".rdf");

					}else{
						
						toSave = new File(f);
						
					}
					
					new ExtractList().extract(new File(useful.get(j)),toSave);
							
					if (f == null) //new file was created
					
						FileUtils.write(new File(table),"\n" +  useful.get(j) + "," + toSave.getAbsolutePath() , true);
					
				}
				
			}
			
		}
			
	}

	private void extract(File toExtract, File toSave) {
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Thread(new ExtractRunnable(toExtract, toSave)).start();
		
	}
				
}
