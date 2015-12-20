package extraction.collectionProcessor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class CollectionCreator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String folderName  = "reuters21578";
		
		File toSave = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/Reuters-21578/CleanCollection");
		
		File toExtract = new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/Reuters-21578/");
		
		create(toExtract,toSave,folderName);		
		
	}

	private static void create(File toExtract, File toSave, String folderName) throws IOException {
		
		//create where to save
		File toSaveIn = new File(toSave,folderName);
		toSaveIn.mkdir();
		
		//see where to extract from
		File toExtractFrom = new File(toExtract,folderName);
		
		File[] files = toExtractFrom.listFiles();
		
		for (int i = 0; i < files.length; i++) {
			
			if (files[i].isDirectory()){
				create(toExtractFrom, toSaveIn, files[i].getName());
			}else{
				
				String content = FileUtils.readFileToString(files[i]);
				
				String[] contentSpl = content.split("</REUTERS>");
				
				for (int j = 0; j < contentSpl.length; j++) {
					
					FileUtils.writeStringToFile(new File(toSaveIn,files[i].getName() + "-" + j), contentSpl[j]+"</REUTERS>");
					
				}
				
			}
			
		}
		
	}

	

}
