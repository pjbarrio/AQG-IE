package collection.extractiontable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class TableGeneratorByFiltering {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
				
		File f1 = new File("/proj/db/NoBackup/pjbarrio/OCOutput/WorkloadTest.table");
		String s2 = new String ("/proj/db/NoBackup/pjbarrio/OCOutput/UsNews.table");
		String comp = "/proj/db/NoBackup/pjbarrio/sites/General/UsNews/";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(s2, true));
		
		
		BufferedReader bf = new BufferedReader(new FileReader(f1));
		
		String line = bf.readLine();
		long i = 0;
		while (line!=null){
			System.out.println(i++);
			if (line.startsWith(comp)){
				bw.write(line + "\n");
			}
			
			line = bf.readLine();
		}
		
		bf.close();
		bw.close();
	}

}
