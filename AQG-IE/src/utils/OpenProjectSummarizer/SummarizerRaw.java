package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SummarizerRaw {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(new File("/local/pjbarrio/Files/Project/Sites/content.rdf.u8")));
		
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter("/local/pjbarrio/Files/Project/Sites/contentSummarized.rdf.u8"));
		
		bw.write(br.readLine() + "\n");
		bw.write(br.readLine() + "\n");
		bw.write(br.readLine() + "\n");
		
		String line = br.readLine();
		
		while (line!= null){
			
			if (line.contains("<Topic") && !line.contains("<Topic r:id=\"Top/World/")){

				while (line != null && !line.contains("</Topic")){
					
					bw.write(line + "\n");
				
					line = br.readLine();
					
				} 
			
				if (line!=null){
					
					bw.write(line + "\n");
					
				}
				
			} else {
				
				line = br.readLine();
				
			}
			
			
			
		}
		
		bw.write("</RDF>");
		
		bw.close();
		
		br.close();
	}

}
