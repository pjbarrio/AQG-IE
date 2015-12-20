package extraction.collectionProcessor.local;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

import exploration.model.Database;
import exploration.model.Document;
import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class LoadLocalCollectionToDB {

	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		
		String collection = "Reuters-21578";
		
		int idDatabase = 3002;

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Database db = pW.getDatabaseById(idDatabase);
		
		File prefix = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/"+collection+"/CleanCollection");
		
		process(pW,prefix,1,db);
		
		pW.finishBatchDownloader(db.getId());
		
	}

	private static int process(persistentWriter pW, File prefix, int index,Database db) throws MalformedURLException {
		
		File[] files = prefix.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			
			if (files[i].isDirectory()){
				index = process(pW,files[i],index,db);
			}else{
				
				File f = files[i];
				
				index++;
				
				Document document = new Document(db,index,f,new URL("http://local.com/" + index),1,0,true);
				
				pW.prepareStoredDownloadedDocument(document);
				
				if ((index % 1000) == 0)
					System.out.println("Processing ..." + index);
					pW.finishBatchDownloader(db.getId());
			
			}
			
		}
		
		return index;
	}
	
}
