package collection.processing.listing;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;


public class RelationDistributionStatistics {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String file = RelationsListing.output;
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		
		String line = br.readLine();
		
		String help;
		
		String[] help2;
		
		Hashtable<String, Integer> stats = new Hashtable<String, Integer>();
		
		Integer help3;
		
		while (line != null){
			
			help = line.replace(" ", "");
			
			help2 = help.split(RelationsListing.TABLE_SEPARATOR);
			
			for (int i = 0; i < help2.length; i++){
				
				help3 = stats.get(help2[i]);
				
				if (help3 == null){
					stats.put(help2[i], 1);
					continue;
				}
				
				help3 = help3 + 1;
				
				stats.put(help2[i],help3);
			}
			
			line = br.readLine();
		}

		for(Enumeration<String> e = stats.keys();e.hasMoreElements();){
			String key = e.nextElement();
			Integer aux = stats.get(key);
			System.out.println(key + "," + aux);
		}
		
		br.close();
		
	}

}
