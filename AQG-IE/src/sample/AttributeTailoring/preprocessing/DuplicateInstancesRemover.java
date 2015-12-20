package sample.AttributeTailoring.preprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Sample;
import exploration.model.dummy.DummyDatabase;
import exploration.model.dummy.DummyVersion;
import exploration.model.dummy.DummyWorkload;

public class DuplicateInstancesRemover {

	private static persistentWriter pW;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int workload = 1;
		
		int version_pos = 1;
		
		int version_neg = 1;
		
		String[] version = {"INDEPENDENT","DEPENDENT"}; 
		
//		String dbclass = "Trip";
		
		String[][] database = {/*{"Business","Bloomberg","randomSample","FullyRandomSample"},*/{"Trip","TheCelebrityCafe","randomSample","FullyRandomSample"}/*,{"Business","TheEconomist","randomSample","FullyRandomSample"},{"General","UsNews","randomSample","FullyRandomSample"},{"Trip","Variety","randomSample","FullyRandomSample"},{"","GlobalSample","Mixed","Mixed"}*/};
		
		pW = PersistenceImplementation.getWriter();
		
		for (int i = 0; i < database.length; i++) {
			
			for (int j = 0; j < version.length; j++) {
			
				args = new String[2];
				
				args[0] = pW.getDatabaseIds(database[i][1]);
				
				args[1] = pW.getPrefix(database[i][0], database[i][1]);
				
				Sample sample = Sample.getSample(pW.getDatabaseByName(database[i][1]), new DummyVersion(version[j]), new DummyWorkload(workload), version_pos,version_neg,new DummySampleConfiguration(1));
				
				String input = pW.getArffRawModel(sample);
				
				String output = pW.getArffRawFilteredModel(sample);
				
				String inputSample = pW.getSampleFile(sample);
				
				String outputSample = pW.getSampleFilteredFile(sample);
				
				BufferedReader br = new BufferedReader(new FileReader(new File(input)));
				
				String line = br.readLine();
				
				ArrayList<String> lines = new ArrayList<String>();
				
				while (!line.toLowerCase().equals("@data")){
					
					lines.add(line);
					
					line = br.readLine();

				}
				
				lines.add(line); //@data
				
				line = br.readLine(); //first line
				
				while (line.trim().equals("")){
					
					lines.add(line);
					
					line = br.readLine();
				}
				
				int d = 0;
				
				BufferedReader brs = new BufferedReader(new FileReader(new File(inputSample)));
				
				BufferedWriter brw = new BufferedWriter(new FileWriter(new File(outputSample)));
				
				String lineSample = brs.readLine();
				
				while (line != null){
					
					if (!lines.contains(line)){
						
						lines.add(line);
						
						brw.write(lineSample + "\n");
						
					} else {
						
						d++;
						
						System.out.println("Duplicated!" + d);
					
					}
					
					line = br.readLine();
				
					lineSample = brs.readLine();
					
					
				}

				brw.close();
				
				brs.close();
				
				br.close();
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
				
				for (String string : lines) {
					
					bw.write(string + "\n");
					
				}
				
				bw.close();
				
			}
			
		}
		
	}

}
