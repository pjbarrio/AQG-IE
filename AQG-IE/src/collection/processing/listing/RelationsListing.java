package collection.processing.listing;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class RelationsListing {

	public static final String TABLE_SEPARATOR = ",";
	public static final String output = "relations.csv";
	private static final CharSequence FIRST_LINE = "<!--Use of the Calais Web Service is governed by the Terms of Service located at http://www.opencalais.com. By using this service or the results of the service you agree to these terms of service.--><!--Relations: ";
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String f1 = "/proj/db/NoBackup/pjbarrio/OCOutput/WorkloadTest.table";
		
		BufferedReader bf = new BufferedReader(new FileReader(new File(f1)));
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(output, true));
		
		String line = bf.readLine();
		
		int i = 0;
		
		while (line != null){
			
			i++;
			
			String file = getFile(line);

			System.out.println(i + " - " + file);
			
			bw.write(showRelations(file) + "\n");
			
			line = bf.readLine();
		}

		bf.close();
		
		bw.close();
	}

	private static String showRelations(String file) throws IOException {
		
		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(new File(file)));
		} catch (FileNotFoundException e) {
			System.out.println("No File: " + file);
			return "No File" + file;
		}
		
		String line = bf.readLine();
		
		if (line==null){
			bf.close();
			
			return "No Content" + file;
		}
		String out = line.replace(FIRST_LINE, "");
		
		bf.close();
		
		System.out.println(out);
		
		return out;
	}

	private static String getFile(String line) {
		
		String[] pair = new String[2];
		
		int ind = line.lastIndexOf(TABLE_SEPARATOR);
		
		pair[1] = line.substring(ind+1);
		
		return pair[1];
	}

}
