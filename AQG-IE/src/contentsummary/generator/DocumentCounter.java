package contentsummary.generator;

import java.io.File;

import utils.FileAnalyzer;

public class DocumentCounter {

	File[] list;
	
	private static int count = 0;
	
	public long documentsCount (File database, String newName){
		
		count++;
		
		if (count>500){
			count = 0;
			Runtime r = Runtime.getRuntime();
			r.gc();
		}
		
		System.out.println(database);
		
		long ret = 0;
		
		if (database.isDirectory()){
			list = database.listFiles();
			for (File file : list) {
				ret +=documentsCount(file,newName);
			}			
		}else{
			if (FileAnalyzer.isSummarizable(database, newName)){
				return 1;
			}
		}
		
		return ret;
	}

}
