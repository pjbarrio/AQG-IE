package online.maintenance.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import online.maintenance.HTMLValidator;
import exploration.model.Database;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;

public class HTMLTreeStructureValidatorThresholdSaver {

	private static double threshold;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		DiskBasedInteractionPersister persister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
		
//		String[][] database = {{"http://www.mauconline.net/","bootstrappedSample"},{"http://www.carmeuse.com","bootstrappedSample"},{"http://diversifiedproduct.com/","bootstrappedSample"},{"http://joehollywood.com/","bootstrappedSample"},
//				{"http://northeasteden.blogspot.com/","bootstrappedSample"},{"http://www.paljorpublications.com/","bootstrappedSample"},{"http://www.brannan.co.uk/","bootstrappedSample"},{"http://www.improv.ca/","bootstrappedSample"},
//				{"http://www.avclub.com/","boostrappedSample"},{"http://www.shopcell.com/","bootstrappedSample"},{"http://www.123aspx.com/","bootstrappedSample"},{"http://www.infoaxon.com/","bootstrappedSample"},
//				{"http://www.thecampussocialite.com/","bootstrappedSample"},{"http://www.time.com/","bootstrappedSample"}};

		List<Database> database = pW.getDatabases();
		
		for (int i = 0; i < database.size(); i++) {
			
			Database db = database.get(i);
			
//			HTMLValidator htmlvalidator = new HTMLTreeStructureValidator(db, pW);
			
			String name = "CTM";
			
			File maintenance = persister.getMaintenanceFolder(name);
			
			File f = new File(maintenance,db.getId() + "/out.txt");
			
			if (!f.exists())
				continue;
			
			loadThresholds(f);
			
			pW.saveHTMLValidatorThreshold(db, name,threshold);
		}

	}

	private static void loadThresholds(File file) {
		
		threshold = 1.1;
		
		try {
			
			List<String> lines = FileUtils.readLines(file);
			
			for (String line : lines) {
				
				if (line.trim().isEmpty())
					continue;
				
				double thres = retrieveThreshold(line);
				
				if (threshold > thres){
					threshold = thres;
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}

	private static double retrieveThreshold(String line) {
		
		System.out.println(line);
		
		String[] spl = line.split("-");
		
		try {
			return  Double.valueOf(spl[spl.length-1]);
		} catch (Exception e) {
			return 1.1;
		}
				
	}
	
}
