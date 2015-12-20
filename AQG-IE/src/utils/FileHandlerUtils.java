package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;


public class FileHandlerUtils {


public static Integer[] generateRandomIndexes(int size, int cant) {
		
	
		if (cant>=size){
			
			Integer[] ret = new Integer[size];
			
			int pos = 0;
			
			while (pos<size){
				ret[pos] = pos++;
			}
			
			return ret;
			
		}
			
	
		Set<Integer> set = new HashSet<Integer>();
		
		while (set.size()<cant){
			
			set.add(new Integer((int) Math.floor(Math.random()*(double)size)));
		
		}
		
		return set.toArray(new Integer[0]);
		
	}
  
  public static ArrayList<String> getAllFileNames(File file,String newName) {
	
	  ArrayList<String> ret = new ArrayList<String>();
	  
	  if (file.isDirectory()){
		  String[] files = file.list();
		  
		  for (String string : files) {
			
			  ret.addAll(getAllFileNames(new File(file,string),newName));
			  
		}
		  
	  }else{
		  
		  if (FileAnalyzer.isSummarizable(file, newName))
			  ret.add(file.getAbsolutePath());
	  }
	  
	  return ret;
	  
}

  public static ArrayList<String> getAllResourceNames(File file) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		try {
		
			BufferedReader br = new BufferedReader(new FileReader(file));
		
			String line;
		
		
			while ((line = br.readLine()) != null){
				
				if (!line.trim().equals(""))
					ret.add(line);
				
			}
		
			br.close();
			
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return ret;
		
	}
  
  public static void writeFile(Collection<String> lines, String file){
	  
	  BufferedWriter bw;
	try {
		bw = new BufferedWriter(new FileWriter(new File(file)));
		
		for (String string : lines) {
			bw.write(string + "\n");
		}
		  
		  bw.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
		  
  }

	public static String loadFileasString(String file) {
	
		File f = new File(file);
		
		BufferedReader br;
		
		String content = "";
		
		try {
		
			br = new BufferedReader(new FileReader(f));
			
			String line = br.readLine();
			
			while (line!=null){
				
				if (line.startsWith("<meta http-equiv=\"refresh\"")){
					line = br.readLine();
					continue;
				}
				
				content = content + "\n" + line;
				
				line = br.readLine();
			}
			
			br.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
	}

	public static void writeFile(String output, String outputFile) {
		
		
		
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
			
			bw.write(output);
			
			bw.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}

	public static void writeFile(File outputFile, String string){
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			bw.write(string);
			
			bw.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void appendEntry(String outputFile, String string) {
		
		try {
		
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
			
			bw.write(string + "\n");

			bw.close();

		} catch (IOException e) {

			e.printStackTrace();

		}
		
		
	}

	public static synchronized String format(File file, String newName) {
		
		File fileName = new File(newName);
		
		try {
			FileUtils.copyFile(file, fileName, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return newName;

	}
  
}
