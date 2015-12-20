package utils.formsFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

public class FormsFinder {

	private static SearchableFormChecker sfc;
	private static boolean processable;

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
	
		String directory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Top/";
		
		sfc = new SearchableFormChecker();
		
		System.setOut(new PrintStream(new File("/proj/dbNoBackup/pjbarrio/sites/Directory/databases2.txt")));
		
		System.setErr(new PrintStream(new File("/proj/dbNoBackup/pjbarrio/sites/Directory/nonMatchingForms2.txt")));
		
		processable = false;
		
		process(new File(directory));
		
	}

	private static void process(File file) {
		
		if (file.isDirectory()) {			  // if a directory
		      String[] files = file.list();		  // list its files
		      Arrays.sort(files);			  // sort the files
		      for (int i = 0; i < files.length; i++)	  // recursively index them
		        process(new File(file, files[i]));

		 } else {
			 
			  if (!processable)
				 if (file.getAbsolutePath().endsWith("923100.html"))
					 processable = true;
				 else
					 return;
			 
			 if (sfc.hasForm(file.getAbsolutePath())){
				 if (sfc.isSearchable()){
					 System.out.println("HTML: " + file);
					 
					 for (String forms : sfc.getSearchableForms()) {
						 
						 System.out.println("FORM: " + forms);
						 
					}
					 
					 if (sfc.hasNonSearchableForms()){
					 
						 System.err.println("HTML: " + file);
						 
						 for (String forms : sfc.getNonSearchableForms()) {
							 
							 System.err.println("FORM: " + forms);
							 
						}

					 }
					 
				 } else {
					 
					 System.err.println("HTML: " + file);
					 
					 for (String forms : sfc.getNonSearchableForms()) {
						 
						 System.err.println("FORM: " + forms);
						 
					}
			
				 }
			 
			 }
			 
		 }
	}

}
