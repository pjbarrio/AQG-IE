package utils;

import java.io.File;

public class FileAnalyzer {
	
	private static CommandLineExecutor cle = new CommandLineExecutor();
	
	public static synchronized boolean isSummarizable(File dbfile, String newName) {
		

		if (dbfile.getPath().endsWith(".html") || dbfile.getPath().endsWith(".htm") || dbfile.getPath().endsWith(".asp"))  // index .htm || .html files
			 return true;
		
//		String ext = getExtension(dbfile.getName());
		
//		if (ext.startsWith("asp") || ext.startsWith("jsp"))
//			return true;
		
		if (dbfile.getPath().endsWith(".mp3") || dbfile.getPath().endsWith(".mp4") || dbfile.getPath().endsWith(".avi") || dbfile.getPath().endsWith(".pdf") || dbfile.getPath().endsWith(".js")  || dbfile.getPath().endsWith(".css") || dbfile.getPath().endsWith(".jpg") || dbfile.getPath().endsWith(".wmv") || dbfile.getPath().endsWith(".gif") || dbfile.getPath().endsWith(".png") || dbfile.getPath().endsWith(".swf") || dbfile.getPath().endsWith(".m4v")){
			return false;
		}
		
		String output = cle.getOutput("file -b " + FileHandlerUtils.format(dbfile,newName));
		
		output = output.toLowerCase();
		
		
		
		if (output.contains("htm"))
			return true;
		if (output.contains("html"))
			return true;
		
		append(dbfile.getAbsolutePath(),output);
		return false;
	}

	private static void append(String file, String output) {
		
		String string = file + " <-> " + output;
		
		FileHandlerUtils.appendEntry("falseFileoutput.txt", string);
		
	}

}
