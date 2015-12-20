package utils.OpenProjectSummarizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;

import utils.FileAnalyzer;

public class CategoryNumbers {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		String rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/";
		
		String Directory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Top/";
		
		System.setOut(new PrintStream(new File(rootDirectory + "CategoryNumbers.txt")));
		
		generateStatistics(new File(Directory));
	
	}

	private static long generateStatistics(File file){
		
		if (file.isDirectory()) {			  // if a directory
		      String[] files = file.list();		  // list its files
		      long ret = 0;
		      for (int i = 0; i < files.length; i++)	  // recursively index them
		        ret += generateStatistics(new File(file, files[i]));
		      System.out.println(ret + "," + file);
		      return ret;
		 } else {
			 return 1;
		 } 
		
	}

}
