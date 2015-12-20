package techniques.evaluation.preprocessing.parallel;

import java.io.IOException;

public class GenerateUsefulUselessIdFile {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String version = "";
		String database = "TheEconomist";
		String dbclass = "Business";
		int starting = 0;
		int total = 50; //up to 1390.
		
		for (int i = starting; i <= total; i++) {
			
			UsefulUselessCreator uuc = new UsefulUselessCreator(dbclass, database, i, version);
			
			new Thread(uuc).start();
			
		}
	
	}

	

}
