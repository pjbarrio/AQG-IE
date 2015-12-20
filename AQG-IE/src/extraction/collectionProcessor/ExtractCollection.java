package extraction.collectionProcessor;

import java.io.File;

import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class ExtractCollection {

	public static void main(String[] args) {
		
		//934 933 923 941 942 931 943 921 944 932 924 911 922
		
		String trec = "ft";
		
		File prefix = new File("/proj/dbNoBackup/pjbarrio/Dataset/TREC/CleanCollection/tipster_vol_4");
		
		File toSave = new File("/proj/dbNoBackup/pjbarrio/Dataset/TREC/Extraction/tipster_vol_4");
		
		OpenCalaisRelationExtractor oCRE = new OpenCalaisRelationExtractor("/proj/dbNoBackup/pjbarrio/Exp/src/extraction/calaisParams.xml");
		
		process(oCRE,prefix,toSave,trec);
	
	}

	private static void process(OpenCalaisRelationExtractor oCRE, File prefix, File toSave, String trec) {
		
		File toSaveIn = new File(toSave,trec);
		
		toSaveIn.mkdir();
		
		File toExtractFrom = new File(prefix,trec);
		
		File[] files = toExtractFrom.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			
			if (files[i].isDirectory()){
				process(oCRE,toExtractFrom,toSaveIn,files[i].getName());
			}else{
				
				File f = new File(toSaveIn,files[i].getName() + OpenCalaisRelationExtractor.SUFFIX);
				if (!f.exists())
					oCRE.ConcurrentProcess(files[i], f);
				else
					System.out.println("exists...");
			}
			
		}
		
	}

	
	
}
