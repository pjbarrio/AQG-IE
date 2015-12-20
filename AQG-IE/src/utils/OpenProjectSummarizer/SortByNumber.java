package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortByNumber {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String rootDirectory = "/proj/dbNoBackup/pjbarrio/sites/Directory/Useful/";
		
		System.setOut(new PrintStream(new File(rootDirectory + "SortedCategoryNumbersImproved.txt")));

		BufferedReader br = new BufferedReader(new FileReader(new File(rootDirectory + "CategoryNumbersImproved.txt")));
		
		String line = br.readLine();
		
		ArrayList<String> r = new ArrayList<String>();
		
		while (line != null){
			
			r.add(line);
			
			line = br.readLine();
		}
		
		Collections.sort(r, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				
				Integer cant = Integer.valueOf(o1.substring(0,o1.indexOf(',')));
				
				Integer cant2 = Integer.valueOf(o2.substring(0, o2.indexOf(',')));
				
				return cant2.compareTo(cant);
				
			}
		});
		
		for (String string : r) {
			System.out.println(string);
		}
		
		br.close();
	}

}
