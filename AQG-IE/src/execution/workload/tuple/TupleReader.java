package execution.workload.tuple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TupleReader {

	private static final String wildcard = "???";
	
	public Tuple[] readTuples(String seedTuplesLocation) {
		
		BufferedReader br;
		
		ArrayList<Tuple> tuples = new ArrayList<Tuple>();
		
		try {
			
//			System.out.println("TupleReader: " + seedTuplesLocation);
			
			br = new BufferedReader(new FileReader(new File(seedTuplesLocation)));
			
			String line = br.readLine();
			
			while (line!=null){
				
				tuples.add(generateTuple(line));
				
				line = br.readLine();
			}
			
			br.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		
		} catch (IOException e) {

			e.printStackTrace();
		
		}
		
		return tuples.toArray(new Tuple[0]);
	}

	public static Tuple generateTuple(String line) {
		
		//Tuples are stored: field:value;field:value;field:value

		Tuple t = new Tuple();
		
		String[] pairs = line.split(";");
		
		for (int i = 0; i < pairs.length; i++) {
			
			String[] fv = pairs[i].split(":");

			if (!wildcard.equals(fv[0]) && fv.length > 1)
			
				t.setTupleField(fv[0], fv[1]);
		}
		
		return t;
	
	}

}
