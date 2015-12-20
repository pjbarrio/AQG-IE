package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import com.hp.hpl.jena.graph.GetTriple;

public class CategorySelector {

	private static Hashtable<Integer, Hashtable<String,ArrayList<String>>> levelsTable;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String input = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/SortedCategoryNumbersImproved.txt";
		String descriptiveOutput = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/SelectedCategoryWithFrequencies.txt";
		String output = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/SelectedCategories.txt";
		
		int[] numberOfCategoriesByLevel = {1,8,5,5};
		
		for (int level = 0; level < numberOfCategoriesByLevel.length; level++) {
			
			BufferedReader br = new BufferedReader(new FileReader(new File(input)));
			
			String line = br.readLine();
			
			while (line != null){
			
				String directory = line.substring(line.indexOf(",")+1);
				
				if (level == getLevel(directory)){
					
					String parent = getParent(directory);
									
//					System.out.println("BEFORE: " + level + " - " + parent);
					
					if (exists(level,parent)){
						
//						System.out.println("EXISTS: " + level + " - " + parent);
						
						if (getArrayLevel(level,parent).size() < numberOfCategoriesByLevel[level]){
							
//							System.out.println("ADDING: " + level + " - " + parent);
							
							getArrayLevel(level,parent).add(line);
							
							if (level + 1 <= numberOfCategoriesByLevel.length){
								
								getArrayLevel(level, directory);
								
							}
							
							
							
						}

					}
				}
			
				line = br.readLine();
				
			}
			br.close();
		
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(descriptiveOutput)));
		
		for (int i = 0; i < numberOfCategoriesByLevel.length; i++) {
			
			Integer level = i;
			
			bw.write("LEVEL:" + level + "\n");
			
			for (Enumeration<String> e2 = getLevelsTable().get(level).keys();e2.hasMoreElements();){
				
				String parent = e2.nextElement();
				
				for (String cat : getArrayLevel(level, parent)) {
					
					bw.write(cat + "\n");
					
				}
				
			}
			
		}
		
		bw.close();

		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(output)));

		for (Enumeration<String> e2 = getLevelsTable().get(numberOfCategoriesByLevel.length-1).keys();e2.hasMoreElements();){
			
			String parent = e2.nextElement();
			
			for (String cat : getArrayLevel(numberOfCategoriesByLevel.length-1, parent)) {
				
				bw2.write(cat.substring(cat.indexOf(",")+1) + "\n");
				
			}
			
		}
		
		bw2.close();
	}

	private static boolean exists(int level, String parent) {
		
		if (level == 0){
			return true; 
		}
		
		Hashtable<String, ArrayList<String>> aux = getLevelsTable().get(level-1);
		
		return aux.containsKey(parent);
		
	}

	private static String getParent(String directory) {
		
		return directory.substring(0, directory.lastIndexOf("/"));
		
	}

	private static ArrayList<String> getArrayLevel(int level, String parent) {
		
		Hashtable<String,ArrayList<String>> aux = getLevelsTable().get(level);
		
		if (aux == null){
			
			aux = new Hashtable<String, ArrayList<String>>();
			
			getLevelsTable().put(level, aux);
			
		}
		
		ArrayList<String> ret = aux.get(parent);
		
		if (ret == null){
			
			ret = new ArrayList<String>();
			
			aux.put(parent, ret);
		}
		
		return ret;
		
	}

	private static Hashtable<Integer,Hashtable<String,ArrayList<String>>> getLevelsTable() {
		
		if (levelsTable == null){
			levelsTable = new Hashtable<Integer,Hashtable<String,ArrayList<String>>>();
		}
		return levelsTable;
	}

	private static int getLevel(String directory) {
		
		String file = directory.replace("/proj/dbNoBackup/pjbarrio/sites/Directory/Top", "");
		
		int ret = file.split("/").length;
		
		return ret-1;
		
	}

}
